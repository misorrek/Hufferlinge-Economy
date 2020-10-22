package huff.economy.listener;

import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import huff.economy.EconomyInterface;
import huff.economy.TransactionInventory;
import huff.economy.TransactionKind;
import huff.economy.storage.EconomyStorage;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.helper.StringHelper;
import huff.lib.inventories.PlayerChooser;

public class InventoryListener implements Listener
{
	private static final String PLAYERCHOOSER_KEY = "Transaction";
	
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
		
		final boolean isWalletItemCase = economy.getConfig().equalsWalletItem(cursorItem) && 
			                         (inventoryType != InventoryType.PLAYER || inventoryAction == InventoryAction.MOVE_TO_OTHER_INVENTORY);
		final boolean isValueItemCase = economy.getConfig().equalsValueItem(cursorItem) && (!isContainerInventory(inventoryType) ||
			                         (inventoryAction == InventoryAction.MOVE_TO_OTHER_INVENTORY && !isContainerInventory(event.getView().getTopInventory().getType()))); //TODO Sinn überprüfen
		
		if (isWalletItemCase || isValueItemCase)			
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
		final InventoryView view = event.getView();
		final String viewTitle = view.getTitle();
		
		if (viewTitle.equals(economy.getConfig().getWalletInventoryName()) || viewTitle.equals(economy.getConfig().getBankInventoryName()))
		{
			event.setCancelled(true);
			
			final ItemStack currentItem = event.getCurrentItem();
			
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
				handleTransactionOpen((Player) event.getWhoClicked(), currentItemName);
			}
		}
	}
	
	public void handleTransactionOpen(@NotNull Player player, @NotNull String currentItemName)
	{
		if (TransactionKind.isTransaction(currentItemName))
		{
			final TransactionKind transactionKind = TransactionKind.valueOf(currentItemName);
			
			player.closeInventory();
			
			if (transactionKind == TransactionKind.BANK_OTHER)
			{
				player.openInventory(new PlayerChooser(PLAYERCHOOSER_KEY, economy.getStorage().getUsers(), InventoryHelper.INV_SIZE_6, null, true));
			}
			else
			{						
				player.openInventory(new TransactionInventory(transactionKind));
			}
		}
	}
	
	@EventHandler
	public void onPlayerChooserInventoryClick(InventoryClickEvent event)
	{
		if (event.getInventory() instanceof PlayerChooser && ((PlayerChooser) event.getInventory()).getKey().equals(PLAYERCHOOSER_KEY))
		{
			final HumanEntity human = event.getWhoClicked();
			final UUID currentUUID = ((PlayerChooser) event.getInventory()).handleEvent(event.getCurrentItem());
			
			human.closeInventory();
			
			if (currentUUID != null)
			{
				human.openInventory(new TransactionInventory(TransactionKind.BANK_OTHER, currentUUID));
			}
			else
			{
				human.sendMessage(MessageHelper.PREFIX_HUFF + "Die Auswahl einer Person war nicht erfolgreich.");
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onTransactionInventoryClick(InventoryClickEvent event)
	{
		if (event.getInventory() instanceof TransactionInventory)
		{			
			((TransactionInventory) event.getInventory()).handleEvent(economy, event.getCurrentItem(), event.getWhoClicked());
			
			event.setCancelled(true);
		}
	}
}
