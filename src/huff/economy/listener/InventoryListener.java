package huff.economy.listener;

import java.util.List;
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
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.economy.EconomyInterface;
import huff.economy.EconomyUtil;
import huff.economy.TransactionKind;
import huff.economy.storage.EconomyStorage;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.helper.StringHelper;

public class InventoryListener
{
	public InventoryListener(@NotNull EconomyInterface economyInterface)
	{
		Validate.notNull((Object) economyInterface, "The economy-interface cannot be null.");
		
		this.economy = economyInterface;
	}
	private final EconomyInterface economy;
	
	@EventHandler (priority = EventPriority.HIGHEST) //TODO Handle full inventory
	public void onDenyHumanInventoryClick(InventoryClickEvent event)
	{
		final InventoryType inventoryType = event.getClickedInventory().getType();
		final InventoryAction inventoryAction = event.getAction();
		final ItemStack cursorItem = event.getCursor();
		
		if ((economy.getConfig().equalsWalletItem(cursorItem) && 
			 (inventoryType != InventoryType.PLAYER ||
			  inventoryAction == InventoryAction.MOVE_TO_OTHER_INVENTORY)) //TODO Sinn überprüfen
			||
			(economy.getConfig().equalsValueItem(cursorItem) && 
		     (!isContainerInventory(inventoryType) ||
			  (inventoryAction == InventoryAction.MOVE_TO_OTHER_INVENTORY && 
			   !isContainerInventory(event.getView().getTopInventory().getType())))))
		{			
			Bukkit.getConsoleSender().sendMessage("InventoryTyp: " + inventoryType.toString());
			Bukkit.getConsoleSender().sendMessage("InventoryAction: " + inventoryAction.toString());
			
			event.setCancelled(true);
		}
	}
	
	private boolean isContainerInventory(@NotNull InventoryType inventoryType)
	{
		return inventoryType == InventoryType.CHEST || inventoryType == InventoryType.BARREL || inventoryType == InventoryType.SHULKER_BOX ||
			   inventoryType == InventoryType.ENDER_CHEST || inventoryType == InventoryType.HOPPER ||
			   inventoryType == InventoryType.DISPENSER || inventoryType == InventoryType.DROPPER;
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onHumanInverntoryClick(InventoryClickEvent event)
	{	
		final ItemStack cursorItem = event.getCursor();
		final ItemStack slotItem = event.getView().getItem(event.getRawSlot());
		
		if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER &&
			cursorItem != null && slotItem != null &&
			economy.getConfig().equalsValueItem(cursorItem) && economy.getConfig().equalsWalletItem(slotItem))
		{
			final HumanEntity human = event.getWhoClicked();
			
			if (human.getGameMode() == GameMode.CREATIVE || human.getGameMode() == GameMode.SPECTATOR)
			{
				human.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du kannst in deinem Spielmodus nicht in den ", economy.getConfig().getWalletName(), " einlagern."));
				return;
			}	
		    int valueAmount = cursorItem.getAmount();
			final int signatureValueAmount = economy.getSignature().getSignatureValueAmount(cursorItem.getItemMeta().getLore(), valueAmount);
			
			if (signatureValueAmount == -1)
			{
				event.getView().setCursor(null);
				((Player) human).playSound(human.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1, 2);	
			} 
			else
			{
				if (valueAmount > signatureValueAmount)
				{
					valueAmount = signatureValueAmount;
				}							
				
				if (economy.getStorage().updateWallet(human.getUniqueId(), valueAmount, false) == EconomyStorage.CODE_SUCCESS)
				{						
					event.getView().setCursor(null);
					((Player) human).playSound(human.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1, 2);						
				}	
			}
			event.setCancelled(true);			
		}
	}
	
	@EventHandler
	public void onEconomyStorageInventoryClick(InventoryClickEvent event)
	{
		final HumanEntity human = event.getWhoClicked();
		final InventoryView view = event.getView();
		final String viewTitle = view.getTitle();
		
		if (viewTitle.equals(economy.getConfig().getWalletInventoryName()) || viewTitle.equals(economy.getConfig().getBankInventoryName()))
		{
			event.setCancelled(true);
			
			if (currentItem == null)
			{
				return;
			}
			final String currentItemName = currentItem.getItemMeta().getDisplayName();
			
			if (currentItemName.equals(InventoryHelper.ITEM_CLOSE))
			{
				view.close();
			}
			else
			{
				handleTransactionOpen(player, currentItemName)
			}
		}
	}
	
	public void handleTransactionOpen(@NotNull Player player, @NotNull String currentItemName)
	{
		if (isTransaction(currentItemName))
		{
			final TransactionKind transactionKind = TransactionKind.valueOf(currentItemName);
			
			if (transactionKind == TransactionKind.BANK_OTHER)
			{
				//TODO Open Player Chooser
			}
			else
			{		
				player.closeInventory();
				player.openInventory(EconomyUtil.getTransactionInventory(economy.getConfig(), , economy.getStorage().getWallet(player.getUniqueId()), null));
			}
		}
	}
	
	@EventHandler
	public void onTransactionInventoryClick(InventoryClickEvent event)
	{
		final HumanEntity human = event.getWhoClicked();
		final InventoryView view = event.getView();
		final String viewTitle = view.getTitle();
		final TransactionKind transactionKind = TransactionKind.getTransaction(viewTitle);
				
		if (transactionKind == null)
		{
			return;
		}		
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
			human.openInventory(EconomyUtil.getWalletInventory(economy.getConfig(), economy.getStorage().getWallet(human.getUniqueId())));
			
			//TODO Allgemein Lösung in Lib (InventoryOpenEvent/InventoryCloseEvent -> Solange speichern bis komplett Close)
		}
		else if (StringHelper.contains(false, currentItemName, "+", "-"))
		{
			modifyTransactionValue(event.getInventory(), (Player) human, transactionKind.isBankTransaction() ? economy.getStorage.getBank(human.getUniqueId() : economy.getStorage.getWallet(human.getUniqueId()));
		}
		else if (StringUtils.containsIgnoreCase(currentItemName, transactionKind.getLabel()))
		{		
			final Player targetPlayer = EconomyUtil.getPayTargetPlayer(event.getInventory());
			
			if (targetPlayer != null) // 2 Kinds FROM BANK IN BANK / FROM WALLET IN WALLET
			{
				if (targetPlayer.isOnline())
				{
					final double valueAmount = EconomyUtil.getPayValueAmount(event.getInventory());
					final String formattedValueAmount = MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(valueAmount));
					
					final int selfFeedbackcode = transactionKind.isBankTransaction() ? economy.getStorage().updateBank(human.getUniqueId(), valueAmount, true) : economy.getStorage().updateWallet(human.getUniqueId(), valueAmount, true)
					final int otherFeedbackcode = transactionKind.isBankTransaction() ?economy.getStorage().updateBank(targetPlayer.getUniqueId(), valueAmount, false) : economy.getStorage().updateWallet(targetPlayer.getUniqueId(), valueAmount, false)
					
					if (selfFeedbackcode == EconomyStorage.CODE_SUCCESS && otherFeedbackcode == EconomyStorage.CODE_SUCCESS)
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
			else // 2 Kinds FROM WALLET IN INV / FROM BANK IN WALLET
			{
					double valueAmount = EconomyUtil.getPayValueAmount(event.getInventory());
				final String formattedValueAmount = MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(valueAmount));
				final ItemStack valueItem = economy.getConfig().getValueItem();
				
				if (economy.getStorage().updateWallet(human.getUniqueId(), valueAmount, true) == EconomyStorage.CODE_SUCCESS)
				{
					int maxStackSize = valueItem.getMaxStackSize();
					
					while ((int) valueAmount > 0)
					{
						if ((int) valueAmount >= maxStackSize)
						{
							valueItem.setAmount(maxStackSize);
							applyLore(valueItem, economy.getSignature().createSignatureLore(maxStackSize));
							valueAmount -= maxStackSize;
						}
						else
						{
							valueItem.setAmount((int) valueAmount);
							applyLore(valueItem, economy.getSignature().createSignatureLore((int) valueAmount));
							valueAmount = 0;
						}						
						human.getInventory().addItem(valueItem);
					}
					human.closeInventory();
					human.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du hast", formattedValueAmount, "aus deinem ",
														 economy.getConfig().getWalletName(), " herausgenommen."));
					return;
				}		
			}
			human.closeInventory();
			human.sendMessage(MessageHelper.PREFIX_HUFF + "Das Herausnehmen des Geldes konnte nicht abgeschlossen werden.");	
		}
		
	}
	
	private void modifyTransactionValue(@NotNull Inventory transactionInventory, @NotNull Player player, double currentStorageValue)
	{
		try
		{
			final Pattern valuePattern = Pattern.compile("§.([+-]) ([0-9]*)");
			final Matcher matcher = valuePattern.matcher(currentItemName);				
			
			if (matcher.find())
			{					
				final boolean negativeChange = matcher.group(1).equals("-");
				final double changeValue = Double.parseDouble(matcher.group(2));
				final double currentValue = EconomyUtil.getPayValueAmount(transactionInventory); //TODO Inspect
				final double summedUpValue = currentValue + (negativeChange ?  changeValue * -1 : changeValue);
				
				if (summedUpValue < 0)
				{
					if (currentValue > 0)
					{
						summedUpValue = 0;
					}
					else
					{
						player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1, 2); //TODO Can Human playSound?
						return;
					}
				}	
				else if (summedUpValue > currentStorageValue)
				{
					if (currentValue < currentStorageValue)
					{
						summedUpValue = currentStorageValue;
					}
					else
					{
						player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1, 2);
						return;
					}
				}
				EconomyUtil.setPayValueAmount(event.getInventory(), economy.getConfig().getValueFormatted(summedUpValue)); //TODO Inspect
				player.playSound(player.getLocation(), (negativeChange ? Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF : Sound.ENTITY_EXPERIENCE_ORB_PICKUP), 1, 2);
			}
		}
		catch (NumberFormatException exception)
		{
			Bukkit.getLogger().log(Level.WARNING, "The transaction-change-value is invalid.", exception);
		}
	}
	
	private void m
	
	private void applyLore(@NotNull ItemStack loreItem, @NotNull List<String> lore)
	{
		ItemMeta loreMeta = loreItem.getItemMeta();
		loreMeta.setLore(lore);
		loreItem.setItemMeta(loreMeta);
	}
	
	private void handleWalletInventoryActions(@NotNull HumanEntity player, @NotNull InventoryView view, @Nullable ItemStack currentItem)
	{	
		
	}
}
