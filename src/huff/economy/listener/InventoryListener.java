package huff.economy.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
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
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
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
	
	@EventHandler (priority = EventPriority.LOW)
	public void onDenyHumanInventoryClick(InventoryClickEvent event)
	{
		if (event.getClickedInventory() == null)
		{
			pickedUpSlot.remove(event.getView().getPlayer().getUniqueId());
			return;
		}
		final HumanEntity human = event.getView().getPlayer();
		final InventoryType inventoryType = event.getClickedInventory().getType();
		final InventoryAction inventoryAction = event.getAction();
		final ItemStack currentItem = event.getCurrentItem();
		final ItemStack cursorItem = event.getCursor();			
		
		if (InventoryHelper.isPickupAction(inventoryAction) && (economy.getConfig().equalsWalletItem(currentItem) || 
                                                                economy.getConfig().equalsValueItem(currentItem)))
		{
			pickedUpSlot.put(human.getUniqueId(), event.getRawSlot());
			return;
		}
		final boolean isWalletItemCase = (economy.getConfig().equalsWalletItem(cursorItem) && inventoryType != InventoryType.PLAYER) || 
										 (economy.getConfig().equalsWalletItem(currentItem) && inventoryAction == InventoryAction.MOVE_TO_OTHER_INVENTORY &&
					                      !InventoryHelper.isInternalCraftView(event.getView()));
		final boolean isValueItemCase = (economy.getConfig().equalsValueItem(cursorItem) && !InventoryHelper.isContainerInventory(inventoryType)) ||
										(economy.getConfig().equalsValueItem(currentItem) && inventoryAction == InventoryAction.MOVE_TO_OTHER_INVENTORY &&
										 !InventoryHelper.isContainerInventory(event.getView().getTopInventory().getType()));
		
		if (isWalletItemCase || isValueItemCase)			
		{		
			if (inventoryAction != InventoryAction.MOVE_TO_OTHER_INVENTORY && pickedUpSlot.containsKey(human.getUniqueId()))
			{
				moveBackToPickup(human, event.getView(), cursorItem);
				event.getView().setCursor(null);
			}			
			event.setCancelled(true);
		}
		pickedUpSlot.remove(human.getUniqueId());
	}
	
	@EventHandler (priority = EventPriority.LOW)
	public void onDenyHumanInventoryDrag(InventoryDragEvent event)
	{	
		final ItemStack oldCursorItem = event.getOldCursor();		
		final boolean isWalletItemCase = economy.getConfig().equalsWalletItem(oldCursorItem);
		final boolean isValueItemCase = economy.getConfig().equalsValueItem(oldCursorItem) && !InventoryHelper.isContainerInventory(event.getView().getTopInventory().getType());
		
		if (isWalletItemCase || isValueItemCase)
		{
			final int inventorySize = event.getView().getTopInventory().getSize();
			
			for (int slot : event.getNewItems().keySet())
			{
				if (slot < inventorySize)
				{
					final HumanEntity human = event.getWhoClicked();
					
					if (pickedUpSlot.containsKey(human.getUniqueId()))
					{
						moveBackToPickup(human, event.getView(), oldCursorItem);
						Bukkit.getScheduler().runTaskLater(economy.getPlugin(), () -> event.getView().setCursor(null), 1);						
					}
					event.setCancelled(true);	
					return;
				}
			}
		}		
	}	
	
	@EventHandler
	public void onDenyHumanInventoryPickup(EntityPickupItemEvent event)
	{
		if (event.getEntityType() != EntityType.PLAYER)
		{
			return;
		}
		final Player player = (Player) event.getEntity();
		
		if (pickedUpSlot.containsKey(player.getUniqueId()))
		{		
			final InventoryView view = player.getOpenInventory();
		    final ItemStack item = event.getItem().getItemStack();
			final List<Integer> freeSlots = InventoryHelper.getFreeSlots(view, false, item);
	
			freeSlots.remove((Object) view.convertSlot(pickedUpSlot.get(player.getUniqueId())));
			
			final int openAmount = InventoryHelper.addToInventorySlots(view.getBottomInventory(), freeSlots, item);
								
			if (openAmount != item.getAmount())
			{
				event.getItem().remove();
				
				if (openAmount > 0)
				{
					item.setAmount(openAmount);
					player.getWorld().dropItem(event.getItem().getLocation(), item).setVelocity(new Vector(0, 0, 0));
				}
				player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 100, 1);
			}
			event.setCancelled(true);
		}
	}

	// I N V E N T O R Y - H U M A N
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onHumanInverntoryClick(InventoryClickEvent event)
	{	
		if (event.getClickedInventory() == null)
		{
			return;
		}
		final HumanEntity human = event.getWhoClicked();
		final InventoryAction inventoryAction = event.getAction();
		final ItemStack cursorItem = event.getCursor();
		final ItemStack slotItem = event.getView().getItem(event.getRawSlot());
		
		if (event.getClickedInventory().getType() == InventoryType.PLAYER &&
			economy.getConfig().equalsValueItem(cursorItem) && economy.getConfig().equalsWalletItem(slotItem))
		{
			event.setCancelled(true);
			
			if (human.getGameMode() == GameMode.CREATIVE || human.getGameMode() == GameMode.SPECTATOR)
			{
				human.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du kannst in deinem Spielmodus nicht in den ", economy.getConfig().getWalletName(), " einlagern."));
				return;
			}	
			handleWalletIn(human, cursorItem, event.isShiftClick());			
		}
		else if (inventoryAction == InventoryAction.SWAP_WITH_CURSOR && 
				 economy.getConfig().equalsValueItem(cursorItem) && economy.getConfig().equalsValueItem(slotItem))
		{					
			final int cursorItemAmount = inventoryAction == InventoryAction.PLACE_ONE ? 1 : cursorItem.getAmount();
			final int clickedSlotItemAmount = slotItem.getAmount();
			final int maxStackSize = slotItem.getMaxStackSize() - clickedSlotItemAmount;
			final int clickedSlotSpace = cursorItemAmount < maxStackSize ? cursorItemAmount : maxStackSize;		                		
			int cursorSignatureValueAmount = economy.getSignature().getSignatureValueAmount(cursorItem.getItemMeta().getLore(), clickedSlotSpace);
			int clickedSignatureValueAmount = economy.getSignature().getSignatureValueAmount(slotItem.getItemMeta().getLore(), clickedSlotItemAmount);		
			final int valueAmount = cursorSignatureValueAmount + clickedSignatureValueAmount;
			ItemStack valueItem = null;
			
			if (valueAmount > 0)
			{
				valueItem = economy.getConfig().getValueItem();
				valueItem.setAmount(cursorSignatureValueAmount + clickedSignatureValueAmount);
				ItemHelper.applyLore(valueItem, economy.getSignature().createSignatureLore(valueAmount));
			}
			cursorItem.setAmount(cursorItemAmount - clickedSlotSpace);			
			event.getView().setItem(event.getRawSlot(), valueItem);
			event.setCancelled(true);		
		}
	}
	
	private void moveBackToPickup(@NotNull HumanEntity human, @NotNull InventoryView view, @NotNull ItemStack itemStack)
	{
		final int rawPickedUpSlot = pickedUpSlot.get(human.getUniqueId());
		final ItemStack rawPickedUpSlotItem = view.getItem(rawPickedUpSlot);

		if (itemStack.isSimilar(rawPickedUpSlotItem))
		{
			itemStack.setAmount(itemStack.getAmount() + rawPickedUpSlotItem.getAmount());
			view.setItem(rawPickedUpSlot, itemStack);	
		}
		else
		{
			view.setItem(rawPickedUpSlot, itemStack);	
		}					
		pickedUpSlot.remove(human.getUniqueId());
	}
	
	private boolean checkValueItem(@NotNull ItemStack valueItem, @NotNull HumanEntity human, boolean withSoundFeedback)
	{
		if (!economy.getConfig().equalsValueItem(valueItem))
		{
			return false;
		}
		int valueAmount = valueItem.getAmount();
		final int signatureValueAmount = economy.getSignature().getSignatureValueAmount(valueItem.getItemMeta().getLore(), valueAmount);
		
		if (signatureValueAmount == 0)
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
	
	private void handleWalletIn(@NotNull HumanEntity human, @NotNull ItemStack cursorItem, boolean isShiftClick)
	{
		if (checkValueItem(cursorItem, human, true))
		{
			cursorItem.setAmount(0);
		} 
		
		if (isShiftClick)
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
}
