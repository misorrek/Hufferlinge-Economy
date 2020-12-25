package huff.economy.listener;

import java.util.HashMap;
import java.util.Map;
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
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import huff.economy.EconomyInterface;
import huff.economy.storage.Storage;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.ItemHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.helper.StringHelper;

public class InventoryListener implements Listener
{
	private Map<UUID, Integer> pickedUpSlot = new HashMap<>();
	
	public InventoryListener(@NotNull EconomyInterface economyInterface)
	{
		Validate.notNull((Object) economyInterface, "The economy-interface cannot be null.");
		
		this.economy = economyInterface;
	}
	private final EconomyInterface economy;
	
	// I N V E N T O R Y - D E N Y
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onDenyHumanInventoryClick(InventoryClickEvent event)
	{
		if (event.getClickedInventory() == null)
		{
			if (pickedUpSlot.containsKey(event.getView().getPlayer().getUniqueId()))
			{
				pickedUpSlot.remove(event.getView().getPlayer().getUniqueId());
			}
			return;
		}	
		final InventoryType inventoryType = event.getClickedInventory().getType();
		final InventoryAction inventoryAction = event.getAction();
		final ItemStack currentItem = event.getCurrentItem();
		final ItemStack cursorItem = event.getCursor();		
		final HumanEntity human = event.getView().getPlayer();
		
		final boolean isWalletItemCase = (economy.getConfig().equalsWalletItem(cursorItem) && inventoryType != InventoryType.PLAYER) || 
										 (economy.getConfig().equalsWalletItem(currentItem) && inventoryAction == InventoryAction.MOVE_TO_OTHER_INVENTORY &&
					                      event.getView().countSlots() > human.getInventory().getSize());
		final boolean isValueItemCase = (economy.getConfig().equalsValueItem(cursorItem) && !InventoryHelper.isContainerInventory(inventoryType)) ||
										(economy.getConfig().equalsValueItem(currentItem) && inventoryAction == InventoryAction.MOVE_TO_OTHER_INVENTORY &&
										 !InventoryHelper.isContainerInventory(event.getView().getTopInventory().getType()));
		
		//Bukkit.getConsoleSender().sendMessage("ViewSlots: " + event.getView().countSlots());
		//Bukkit.getConsoleSender().sendMessage("InvSize: " + human.getInventory().getSize());
		//Bukkit.getConsoleSender().sendMessage("CurrentItem: " + currentItem.toString());
		//Bukkit.getConsoleSender().sendMessage("CursorItem: " + cursorItem.toString());
		//Bukkit.getConsoleSender().sendMessage("InventoryTyp: " + inventoryType.toString());
		//Bukkit.getConsoleSender().sendMessage("InventoryAction: " + inventoryAction.toString());
		//Bukkit.getConsoleSender().sendMessage("ClickType: " + event.getClick().toString());		
			
		if (InventoryHelper.isPickupAction(inventoryAction) && (economy.getConfig().equalsWalletItem(currentItem) || 
				                                                economy.getConfig().equalsValueItem(currentItem)))
		{
			if (pickedUpSlot.containsKey(human.getUniqueId()))
			{
				pickedUpSlot.remove(human.getUniqueId());
			}
			pickedUpSlot.put(human.getUniqueId(), event.getRawSlot());
			return;
		}
		
		if (isWalletItemCase || isValueItemCase)			
		{		
			if (pickedUpSlot.containsKey(human.getUniqueId()))
			{
				final int rawPickedUpSlot = pickedUpSlot.get(human.getUniqueId());
				final ItemStack rawPickedUpSlotItem = event.getView().getItem(rawPickedUpSlot);
				final ItemStack replaceItem = inventoryAction == InventoryAction.MOVE_TO_OTHER_INVENTORY ? currentItem : cursorItem;
				
				if (rawPickedUpSlotItem.isSimilar(replaceItem))
				{
					cursorItem.setAmount(replaceItem.getAmount() + rawPickedUpSlotItem.getAmount());
					event.getView().setItem(rawPickedUpSlot, replaceItem);	
				}
				else
				{
					event.getView().setItem(rawPickedUpSlot, replaceItem);	
				}					
				
				if (inventoryAction != InventoryAction.MOVE_TO_OTHER_INVENTORY)
				{
					event.getView().setCursor(null);
				}		
				pickedUpSlot.remove(human.getUniqueId());
			}			
			event.setCancelled(true);
		}
		else if (inventoryAction == InventoryAction.SWAP_WITH_CURSOR && economy.getConfig().equalsValueItem(cursorItem))
		{
			final ItemStack clickedSlotItem = event.getView().getItem(event.getRawSlot());
			
			if (economy.getConfig().equalsValueItem(clickedSlotItem))
			{				
				final int cursorItemAmount = inventoryAction == InventoryAction.PLACE_ONE ? 1 : cursorItem.getAmount();
				final int clickedSlotItemAmount = clickedSlotItem.getAmount();
				int clickedSlotSpace = event.getClickedInventory().getMaxStackSize() < clickedSlotItem.getMaxStackSize() ? 
						               event.getClickedInventory().getMaxStackSize() - clickedSlotItemAmount : 
						               clickedSlotItem.getMaxStackSize() - clickedSlotItemAmount;
				
				//Bukkit.getConsoleSender().sendMessage("CursorAmount : " + cursorItemAmount);
				//Bukkit.getConsoleSender().sendMessage("ClickedAmount : " + clickedSlotItemAmount);
				//Bukkit.getConsoleSender().sendMessage("ClickedSlotSpace : " + clickedSlotSpace);
				
				if (cursorItemAmount < clickedSlotSpace)
				{
					clickedSlotSpace = cursorItemAmount;
				}			
				int cursorSignatureValueAmount = economy.getSignature().getSignatureValueAmount(cursorItem.getItemMeta().getLore(), clickedSlotSpace);
				int clickedSignatureValueAmount = economy.getSignature().getSignatureValueAmount(clickedSlotItem.getItemMeta().getLore(), clickedSlotItemAmount);
							
				if (cursorSignatureValueAmount > clickedSlotSpace)
				{
					cursorSignatureValueAmount = clickedSlotSpace;
				}
				
				if (clickedSignatureValueAmount > clickedSlotItemAmount)
				{
					clickedSignatureValueAmount = clickedSlotItemAmount;
				}			
				//Bukkit.getConsoleSender().sendMessage("ClickedSlotSpaceUpdated : " + clickedSlotSpace);
				//Bukkit.getConsoleSender().sendMessage("CursorSig : " + cursorSignatureValueAmount);
				//sBukkit.getConsoleSender().sendMessage("ClickedSig : " + clickedSignatureValueAmount);
				
				if (cursorSignatureValueAmount == -1)
				{
					cursorSignatureValueAmount = 0;
				}
				
				if (clickedSignatureValueAmount == -1)
				{
					event.getView().setItem(event.getRawSlot(), null);
				}
								
				cursorItem.setAmount(cursorItemAmount - clickedSlotSpace);
		
				final ItemStack valueItem = economy.getConfig().getValueItem();
				
				valueItem.setAmount(cursorSignatureValueAmount + clickedSignatureValueAmount);
				
				ItemHelper.applyLore(valueItem, economy.getSignature().createSignatureLore(cursorSignatureValueAmount + clickedSignatureValueAmount));
							
				event.getView().setItem(event.getRawSlot(), valueItem);
				event.setCancelled(true);
			}			
		}
		
		if (pickedUpSlot.containsKey(human.getUniqueId()))
		{
			pickedUpSlot.remove(human.getUniqueId());
		}
	}
	
	@EventHandler
	public void onDenyHumanInventoryPickup(EntityPickupItemEvent event)
	{
		if (pickedUpSlot.containsKey(event.getEntity().getUniqueId()) && InventoryHelper.getFreeSlotsAfterAdding(((Player) event.getEntity()).getInventory(), event.getItem().getItemStack()) < 1)
		{			
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onDenyHumanInventoryDrag(InventoryDragEvent event)
	{	
		if (InventoryHelper.isContainerInventory(event.getView().getTopInventory().getType()) && 
			InventoryHelper.isContainerInventory(event.getView().getBottomInventory().getType()))
		{
			return;
		}
		final ItemStack oldCursorItem = event.getOldCursor();	
		
		if (economy.getConfig().equalsWalletItem(oldCursorItem) || economy.getConfig().equalsValueItem(oldCursorItem))
		{
			final int inventorySize = event.getView().getTopInventory().getSize();
			
			for (int slot : event.getNewItems().keySet())
			{
				if (slot < inventorySize)
				{		
					event.setCancelled(true);				
				}
			}
		}		
	}	

	// I N V E N T O R Y - H U M A N
	
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
				event.getView().setCursor(null);
				human.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du kannst in deinem Spielmodus nicht in den ", economy.getConfig().getWalletName(), " einlagern."));
				return;
			}	
		    
			if (checkValueItem(cursorItem, human, true))
			{
				event.getView().setCursor(null);
			}
			
			if (event.isShiftClick())
			{
				handleWalletInShift(human);
			}		
			event.setCancelled(true);			
		}
	}
	
	private boolean checkValueItem(@NotNull ItemStack valueItem, @NotNull HumanEntity human, boolean withSoundFeedback)
	{
		if (!economy.getConfig().equalsValueItem(valueItem))
		{
			return false;
		}
		int valueAmount = valueItem.getAmount();
		final int signatureValueAmount = economy.getSignature().getSignatureValueAmount(valueItem.getItemMeta().getLore(), valueAmount);
		
		if (signatureValueAmount == -1)
		{
			if (withSoundFeedback)
			{
				((Player) human).playSound(human.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1, 2);
			}
			return true;
		}
		else
		{
			if (valueAmount > signatureValueAmount)
			{
				valueAmount = signatureValueAmount;
			}							
			
			if (economy.getStorage().updateWallet(human.getUniqueId(), valueAmount, false) == Storage.CODE_SUCCESS)
			{		
				if (withSoundFeedback)
				{					
					((Player) human).playSound(human.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1, 2);	
				}
				return true;		
			}	
		}
		return false;
	}
	
	private void handleWalletInShift(@NotNull HumanEntity human)
	{
		for (ItemStack currentItemStack : human.getInventory().getStorageContents())
		{
			if (checkValueItem(currentItemStack, human, false))
			{
				human.getInventory().remove(currentItemStack);			
			}
		}
	}
}
