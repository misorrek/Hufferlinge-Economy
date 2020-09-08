package huff.economy.listener;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.economy.EconomyConfig;
import huff.economy.EconomyTable;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.helper.StringHelper;

public class WalletListener implements Listener
{
	public WalletListener(@NotNull EconomyConfig economyConfig, @NotNull EconomyTable economyTable)
	{
		Validate.notNull((Object) economyConfig, "The economy-config cannot be null.");
		Validate.notNull((Object) economyTable, "The economy-table cannot be null.");
		
		this.economyConfig = economyConfig;
		this.economyTable = economyTable;
	}
	private final EconomyConfig economyConfig;
	private final EconomyTable economyTable;
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		ItemStack placedItem = event.getItemInHand();
		
		if (equalsWalletItem(placedItem) || equalsValueItem(placedItem))
		{
			event.setCancelled(true);
		}
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
		Player player = event.getPlayer();
		
		if ((event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) &&
			equalsWalletItem(player.getInventory().getItemInMainHand()))
		{
			player.openInventory(WalletUtil.getWalletInventory(economyConfig, economyTable.getWallet(player.getUniqueId())));
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInteractAtEntity(PlayerInteractAtEntityEvent event)
	{
		Player player = event.getPlayer();
		
		if (event.getRightClicked() instanceof Player && 
		    (equalsWalletItem(event.getPlayer().getInventory().getItemInMainHand()) ||
			 equalsWalletItem(event.getPlayer().getInventory().getItemInOffHand())))
		{
			player.openInventory(WalletUtil.getPayInventory(economyConfig, ((Player) event.getRightClicked()).getName()));
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onHumanInverntoryClick(InventoryClickEvent event) //TODO Handle full inventory
	{	
		if (event.getClickedInventory() != null && event.getCursor() != null)
		{
		    InventoryType inventoryType = event.getClickedInventory().getType();
			ItemStack cursorItem = event.getCursor();
			
			if (equalsWalletItem(cursorItem) && (inventoryType != InventoryType.PLAYER ||
			                                     event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)))
			{			
				event.setCancelled(true);
			}
			else if (equalsValueItem(cursorItem))
			{
				if (inventoryType == InventoryType.PLAYER) 
				{
					final HumanEntity human = event.getWhoClicked();
					
					if (human.getGameMode() == GameMode.CREATIVE || human.getGameMode() == GameMode.SPECTATOR)
					{
						human.sendMessage(MessageHelper.PREFIX_HUFF + "Du kannst in deinem Spielmodus nicht in den " + economyConfig.getWalletName() + " einlagern.");
						return;
					}				
					final ItemStack slotItem = event.getView().getItem(event.getRawSlot());
					
					if (slotItem != null && equalsWalletItem(slotItem))
					{
						final int feedbackCode = economyTable.updateWallet(human.getUniqueId(), cursorItem.getAmount(), false);
						
						if (feedbackCode == EconomyTable.CODE_SUCCESS)
						{						
							event.getView().setCursor(null);
							((Player) human).playSound(human.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1, 1);						
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
		HumanEntity human = event.getWhoClicked();
		InventoryView view = event.getView();
		String viewTitle = view.getTitle();
		
		if (viewTitle.equals(WalletUtil.getWalletInventoryName(economyConfig.getWalletName())))
		{
			event.setCancelled(true);
			
			handleWalletInventoryActions(human, view, event.getCurrentItem());
		}
		else if (viewTitle.equals(WalletUtil.getPayInventoryName(economyConfig.getValueName())))
		{		
			event.setCancelled(true);
			
			ItemStack currentItem = event.getCurrentItem();
			
			if (currentItem == null)
			{
				return;
			}
			String currentItemName = currentItem.getItemMeta().getDisplayName();
			
			if (currentItemName.equals(InventoryHelper.ITEM_ABORT))
			{
				human.closeInventory();
				human.openInventory(WalletUtil.getWalletInventory(economyConfig, economyTable.getWallet(human.getUniqueId())));
			}
			else if (currentItemName.equals(WalletUtil.ITEM_PERFORMPAY))
			{
				Player targetPlayer = WalletUtil.getPayTargetPlayer(event.getInventory());
				
				if (targetPlayer != null)
				{
					if (targetPlayer.isOnline())
					{
						double valueAmount = WalletUtil.getPayValueAmount(event.getInventory());
						String formattedValueAmount = MessageHelper.getHighlighted(economyConfig.getValueFormatted(valueAmount));
						
						if (economyTable.updateWallet(human.getUniqueId(), valueAmount, true) == EconomyTable.CODE_SUCCESS &&
							economyTable.updateWallet(targetPlayer.getUniqueId(), valueAmount, false) == EconomyTable.CODE_SUCCESS)
						{
							human.closeInventory();
							human.sendMessage(MessageHelper.PREFIX_HUFF + "Du hast" + formattedValueAmount + "an" +
					                                          MessageHelper.getHighlighted(targetPlayer.getName() + "übergeben."));
							targetPlayer.sendMessage(MessageHelper.PREFIX_HUFF + "Du hast" + formattedValueAmount + "von" +
					                                 MessageHelper.getHighlighted(human.getName()) + "erhalten.");
							return;
						}						
					}
				}
				else
				{
					double valueAmount = WalletUtil.getPayValueAmount(event.getInventory());
					String formattedValueAmount = MessageHelper.getHighlighted(economyConfig.getValueFormatted(valueAmount));
					ItemStack valueItem = WalletUtil.getValueItem(economyConfig);
					
					if (economyTable.updateWallet(human.getUniqueId(), valueAmount, true) == EconomyTable.CODE_SUCCESS)
					{
						int maxStackSize = valueItem.getMaxStackSize();
						
						while ((int) valueAmount > 0)
						{
							if ((int) valueAmount >= maxStackSize)
							{
								valueItem.setAmount(maxStackSize);
								valueAmount -= maxStackSize;
							}
							else
							{
								valueItem.setAmount((int) valueAmount);
								valueAmount = 0;
							}						
							human.getInventory().addItem(valueItem);
						}
						human.closeInventory();
						human.sendMessage(MessageHelper.PREFIX_HUFF + "Du hast" + formattedValueAmount + "aus deinem " + economyConfig.getWalletName() + " herausgenommen.");
						return;
					}		
				}
				human.closeInventory();
				human.sendMessage(MessageHelper.PREFIX_HUFF + "Das Herausnehmen des Geldes konnte nicht abgeschlossen werden.");		
			}
			else if (StringHelper.isIn(false, currentItemName, WalletUtil.ITEM_PAY_ADD_1,
															   WalletUtil.ITEM_PAY_ADD_2,
															   WalletUtil.ITEM_PAY_ADD_3,
															   WalletUtil.ITEM_PAY_REMOVE_1,
															   WalletUtil.ITEM_PAY_REMOVE_2,
															   WalletUtil.ITEM_PAY_REMOVE_3))
			{
				try
				{
					Pattern valuePattern = Pattern.compile("§.([+-]) ([0-9]*)");
					Matcher matcher = valuePattern.matcher(currentItemName);				
					
					while (matcher.find())
					{					
						boolean negativeChange = matcher.group(1).equals("-");
						double valueChange = Double.parseDouble(matcher.group(2));
						double valueAmount = WalletUtil.getPayValueAmount(event.getInventory());
						double valueSum = valueAmount + (negativeChange ?  valueChange * -1 : valueChange);
						double currentWallet = economyTable.getWallet(human.getUniqueId());
						
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
						WalletUtil.setPayValueAmount(event.getInventory(), economyConfig.getValueName(), valueSum); 
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
	
	private void handleWalletInventoryActions(@NotNull HumanEntity player, @NotNull InventoryView view, @Nullable ItemStack currentItem)
	{	
		if (currentItem == null)
		{
			return;
		}
		String currentItemName = currentItem.getItemMeta().getDisplayName();
		
		if (currentItemName.equals(InventoryHelper.ITEM_CLOSE))
		{
			view.close();
		}
		else if (currentItemName.equals(WalletUtil.getPayInventoryName(economyConfig.getValueName())))
		{
			player.closeInventory();
			player.openInventory(WalletUtil.getPayInventory(economyConfig, null));
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
}
