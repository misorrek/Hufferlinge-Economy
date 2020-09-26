package huff.economy.listener;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.economy.EconomyConfig;
import huff.economy.EconomyUtil;
import huff.economy.TransactionKind;
import huff.economy.storage.EconomyBank;
import huff.economy.storage.EconomySignature;
import huff.economy.storage.EconomyStorage;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.helper.StringHelper;

public class WalletListener implements Listener
{
	public WalletListener(@NotNull EconomyConfig economyConfig, @NotNull EconomyStorage economyStorage, @NotNull EconomySignature economySignature, @NotNull EconomyBank economyBank)
	{
		Validate.notNull((Object) economyConfig, "The economy-config cannot be null.");
		Validate.notNull((Object) economyStorage, "The economy-table cannot be null.");
		Validate.notNull((Object) economyBank, "The economy-bank cannot be null");
		
		this.economyConfig = economyConfig;
		this.economyStorage = economyStorage;
		this.economySignature = economySignature;
		this.economyBank = economyBank;
	}
	private final EconomyConfig economyConfig;
	private final EconomyStorage economyStorage;
	private final EconomySignature economySignature;
	private final EconomyBank economyBank;
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		final ItemStack placedItem = event.getItemInHand();
		
		if (equalsWalletItem(placedItem) || equalsValueItem(placedItem))
		{
			event.setCancelled(true);
			return;
		}
		event.setCancelled(handleBankBlockInteract(event.getPlayer(), event.getBlock()));
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event)
	{
		event.setCancelled(handleBankBlockInteract(event.getPlayer(), event.getBlock()));
	}
	
	private boolean handleBankBlockInteract(@NotNull Player player, @NotNull Block block)
	{
		if (block.getType() == economyConfig.getBankMaterial())
		{
			final Location breakedBlockLocation = block.getLocation();
			
			for (Location bankLocation : economyBank.getBankLocations())
			{
				if (bankLocation.distance(breakedBlockLocation) < 2)
				{
					player.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Das erlaubt der ", economyConfig.getBankName(), " nicht."));
					return true;
				}
			}
		}
		return false;
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onDrop(PlayerDropItemEvent event)
	{
		if (equalsWalletItem(event.getItemDrop().getItemStack()))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		final Action action = event.getAction();
		final Player player = event.getPlayer();
		final ItemStack playerMainItem = player.getInventory().getItemInMainHand();
		
		if ((action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) &&
			equalsWalletItem(playerMainItem))
		{
			player.openInventory(EconomyUtil.getWalletInventory(economyConfig, economyStorage.getWallet(player.getUniqueId())));
			player.playSound(player.getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 2);	
			event.setCancelled(true);
		}
		else if (action == Action.RIGHT_CLICK_BLOCK && equalsBankSpawnItem(playerMainItem))
		{
			if (economyBank.handleBankAdd(player, economyConfig) == EconomyBank.CODE_SUCCESS)
			{
				player.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, economyConfig.getBankName(), " platziert.\n",
                                                      MessageHelper.PREFIX_HUFF, "Denke an die Öffnungszeiten von ", MessageHelper.getTimeLabel(economyConfig.getBankOpen()),
                                                                                 "bis ", MessageHelper.getTimeLabel(economyConfig.getBankClose()), "."));
				player.getInventory().setItemInMainHand(null); //AMOUNT -1 ?
			}
			else
			{
				player.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du bist zu nah an einem anderen ", economyConfig.getBankName(), "."));
			}
		}
	}
	
	@EventHandler
	public void onInteractAtEntity(PlayerInteractAtEntityEvent event)
	{		
		event.setCancelled(handleEntityInteract(event.getPlayer(), event.getRightClicked()));
	}
	
	@EventHandler
	public void onHitAtEntity(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() instanceof Player)
		{
			event.setCancelled(handleEntityInteract((Player) event.getDamager(), event.getEntity()));
		}
	}
	
	private boolean handleEntityInteract(@NotNull Player player, @NotNull Entity entity)
	{
		if (entity instanceof Player && 
		    (equalsWalletItem(player.getInventory().getItemInMainHand()) ||
			 equalsWalletItem(player.getInventory().getItemInOffHand())))
		{
			player.closeInventory();
			player.openInventory(EconomyUtil.getTransactionInventory(economyConfig, TransactionKind.WALLET_OUT,  economyStorage.getWallet(player.getUniqueId()), ((Player) entity).getName()));		
			return true;
		}
		
		if (entity instanceof Villager &&
			entity.getCustomName().equals(economyConfig.getBankEntityName()))
		{
			final UUID playerUUID = player.getUniqueId();
			
			player.openInventory(EconomyUtil.getBankInventory(economyConfig, economyStorage.getBalance(playerUUID), 
					                                          economyStorage.getWallet(playerUUID), 
					                                          economyBank.isOwner(playerUUID, entity.getLocation())));
			return true;
		}	
		return false;
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onHumanInverntoryClick(InventoryClickEvent event) //TODO Handle full inventory
	{	
		if (event.getClickedInventory() != null && event.getCursor() != null)
		{
		    final InventoryType inventoryType = event.getClickedInventory().getType();
			final ItemStack cursorItem = event.getCursor();
			
			if (equalsWalletItem(cursorItem) && (inventoryType != InventoryType.PLAYER ||
			                                     event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)))
			{			
				event.setCancelled(true);
			}
			else if (equalsValueItem(cursorItem))
			{
				if (inventoryType == InventoryType.PLAYER) 
				{								
					final ItemStack slotItem = event.getView().getItem(event.getRawSlot());
					
					if (slotItem != null && equalsWalletItem(slotItem))
					{
						final HumanEntity human = event.getWhoClicked();
						
						if (human.getGameMode() == GameMode.CREATIVE || human.getGameMode() == GameMode.SPECTATOR)
						{
							human.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du kannst in deinem Spielmodus nicht in den ", economyConfig.getWalletName(), " einlagern."));
							return;
						}	
					    int wantedValueAmount = cursorItem.getAmount();
						final int signatureValueAmount = economySignature.getSignatureValueAmount(cursorItem.getItemMeta().getLore(), wantedValueAmount);
						
						if (signatureValueAmount == -1)
						{
							event.getView().setCursor(null);
							((Player) human).playSound(human.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1, 2);	
						} 
						else
						{
							if (wantedValueAmount > signatureValueAmount)
							{
								wantedValueAmount = signatureValueAmount;
							}							
							final int feedbackCode = economyStorage.updateWallet(human.getUniqueId(), wantedValueAmount, false);
							
							if (feedbackCode == EconomyStorage.CODE_SUCCESS)
							{						
								event.getView().setCursor(null);
								((Player) human).playSound(human.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1, 2);						
							}	
						}
						event.setCancelled(true);
					}				
				}
				else if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) && isContainerInventory(event.getView().getTopInventory().getType()))
				{
					event.setCancelled(true);
				}
				else if (isContainerInventory(inventoryType))
				{
					event.setCancelled(true);
				}	
			}
		}
	}
	
	private boolean isContainerInventory(@NotNull InventoryType inventoryType)
	{
		return inventoryType != InventoryType.CHEST || inventoryType != InventoryType.BARREL || inventoryType != InventoryType.SHULKER_BOX ||
			   inventoryType != InventoryType.ENDER_CHEST || inventoryType != InventoryType.HOPPER ||
			   inventoryType != InventoryType.DISPENSER || inventoryType != InventoryType.DROPPER;
	}
	
	@EventHandler
	public void onWalletInventoryClick(InventoryClickEvent event)
	{
		final HumanEntity human = event.getWhoClicked();
		final InventoryView view = event.getView();
		final String viewTitle = view.getTitle();
		
		if (viewTitle.equals(economyConfig.getWalletInventoryName()))
		{
			event.setCancelled(true);
			
			handleWalletInventoryActions(human, view, event.getCurrentItem());
		}
		else if (viewTitle.equals(economyConfig.getTransactionInventoryName(TransactionKind.WALLET_OUT)))
		{		
			event.setCancelled(true);
			
			final ItemStack currentItem = event.getCurrentItem();
			
			if (currentItem == null)
			{
				return;
			}
			String currentItemName = currentItem.getItemMeta().getDisplayName();
			
			if (currentItemName.equals(InventoryHelper.ITEM_ABORT))
			{
				human.closeInventory();
				human.openInventory(EconomyUtil.getWalletInventory(economyConfig, economyStorage.getWallet(human.getUniqueId())));
			}
			else if (currentItemName.equals(EconomyUtil.getPerformItemName(TransactionKind.WALLET_OUT)))
			{
				final Player targetPlayer = EconomyUtil.getPayTargetPlayer(event.getInventory());
				
				if (targetPlayer != null)
				{
					if (targetPlayer.isOnline())
					{
						final double valueAmount = EconomyUtil.getPayValueAmount(event.getInventory());
						final String formattedValueAmount = MessageHelper.getHighlighted(economyConfig.getValueFormatted(valueAmount));
						
						if (economyStorage.updateWallet(human.getUniqueId(), valueAmount, true) == EconomyStorage.CODE_SUCCESS &&
							economyStorage.updateWallet(targetPlayer.getUniqueId(), valueAmount, false) == EconomyStorage.CODE_SUCCESS)
						{
							human.closeInventory();
							human.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du hast", formattedValueAmount, "an",
					                                             MessageHelper.getHighlighted(targetPlayer.getName()), "übergeben."));
							targetPlayer.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du hast", formattedValueAmount, "von",
					                                                    MessageHelper.getHighlighted(human.getName()), "erhalten."));
							return;
						}						
					}
				}
				else
				{
					double valueAmount = EconomyUtil.getPayValueAmount(event.getInventory());
					final String formattedValueAmount = MessageHelper.getHighlighted(economyConfig.getValueFormatted(valueAmount));
					final ItemStack valueItem = economyConfig.getValueItem();
					
					if (economyStorage.updateWallet(human.getUniqueId(), valueAmount, true) == EconomyStorage.CODE_SUCCESS)
					{
						int maxStackSize = valueItem.getMaxStackSize();
						
						while ((int) valueAmount > 0)
						{
							if ((int) valueAmount >= maxStackSize)
							{
								valueItem.setAmount(maxStackSize);
								applyLore(valueItem, economySignature.createSignatureLore(maxStackSize));
								valueAmount -= maxStackSize;
							}
							else
							{
								valueItem.setAmount((int) valueAmount);
								applyLore(valueItem, economySignature.createSignatureLore((int) valueAmount));
								valueAmount = 0;
							}						
							human.getInventory().addItem(valueItem);
						}
						human.closeInventory();
						human.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du hast", formattedValueAmount, "aus deinem ",
								                             economyConfig.getWalletName(), " herausgenommen."));
						return;
					}		
				}
				human.closeInventory();
				human.sendMessage(MessageHelper.PREFIX_HUFF + "Das Herausnehmen des Geldes konnte nicht abgeschlossen werden.");		
			}
			else if (StringHelper.contains(false, currentItemName, "+", "-"))
			{
				try
				{
					final Pattern valuePattern = Pattern.compile("§.([+-]) ([0-9]*)");
					final Matcher matcher = valuePattern.matcher(currentItemName);				
					
					while (matcher.find())
					{					
						boolean negativeChange = matcher.group(1).equals("-");
						double valueChange = Double.parseDouble(matcher.group(2));
						double valueAmount = EconomyUtil.getPayValueAmount(event.getInventory());
						double valueSum = valueAmount + (negativeChange ?  valueChange * -1 : valueChange);
						double currentWallet = economyStorage.getWallet(human.getUniqueId());
						
						if (valueSum < 0)
						{
							if (valueAmount > 0)
							{
								valueSum = 0;
							}
							else
							{
								((Player) human).playSound(human.getLocation(), Sound.ENTITY_EGG_THROW, 1, 2);
								return;
							}
						}	
						else if (valueSum > currentWallet)
						{
							if (valueAmount < currentWallet)
							{
								valueSum = currentWallet;
							}
							else
							{
								((Player) human).playSound(human.getLocation(), Sound.ENTITY_EGG_THROW, 1, 2);
								return;
							}
						}
						EconomyUtil.setPayValueAmount(event.getInventory(), economyConfig.getValueFormatted(valueSum)); 
						((Player) human).playSound(human.getLocation(), (negativeChange ? Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF : Sound.ENTITY_EXPERIENCE_ORB_PICKUP), 1, 2);
					}
				}
				catch (NumberFormatException exception)
				{
					Bukkit.getLogger().log(Level.WARNING, "The value-change is invalid.", exception);
				}
			}
		}
	}
	
	private void applyLore(@NotNull ItemStack loreItem, @NotNull List<String> lore)
	{
		ItemMeta loreMeta = loreItem.getItemMeta();
		loreMeta.setLore(lore);
		loreItem.setItemMeta(loreMeta);
	}
	
	private void handleWalletInventoryActions(@NotNull HumanEntity player, @NotNull InventoryView view, @Nullable ItemStack currentItem)
	{	
		if (currentItem == null)
		{
			return;
		}
		final String currentItemName = currentItem.getItemMeta().getDisplayName();
		
		if (currentItemName.equals(InventoryHelper.ITEM_CLOSE))
		{
			view.close();
		}
		else if (currentItemName.equals(economyConfig.getTransactionInventoryName(TransactionKind.WALLET_OUT)))
		{
			player.closeInventory();
			player.openInventory(EconomyUtil.getTransactionInventory(economyConfig, TransactionKind.WALLET_OUT, economyStorage.getWallet(player.getUniqueId()), null));
		}
	}

	private boolean equalsWalletItem(ItemStack item)
	{
		return item.getType().equals(economyConfig.getWalletMaterial()) && item.getItemMeta().getDisplayName().equals("§6§l" + economyConfig.getWalletName());
	}
	
	private boolean equalsValueItem(ItemStack item)
	{
		return item.getType().equals(economyConfig.getValueMaterial()) && item.getItemMeta().getDisplayName().equals("§e§l" + economyConfig.getValueName());
	}
	
	private boolean equalsBankSpawnItem(ItemStack item)
	{
		return item.getType().equals(economyConfig.getBankSpawnMaterial()) && item.getItemMeta().getDisplayName().equals("§e§l" + economyConfig.getBankName());
	}
}