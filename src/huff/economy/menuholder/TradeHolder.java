package huff.economy.menuholder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.economy.EconomyInterface;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.ItemHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.interfaces.Action;
import huff.lib.menuholder.MenuExitType;
import huff.lib.menuholder.MenuHolder;

public class TradeHolder extends MenuHolder
{
	private static final Integer[] TRADERLEFT_SLOTS = {10,11,12,19,20,21,28,29,30,37,38,39};
	private static final Integer[] TRADERRIGHT_SLOTS = {14,15,16,23,24,25,32,33,34,41,42,43};
	private static final String NOTREADY_NAME = "§6Handel ausstehend...";
	private static final Material NOTREADY_MATERIAL = Material.ORANGE_STAINED_GLASS_PANE;
	private static final String READY_NAME = "§aHandel akzeptiert";
	private static final Material READY_MATERIAL = Material.LIME_STAINED_GLASS_PANE;
	
	public TradeHolder(@NotNull EconomyInterface economyInterface, @NotNull UUID leftTrader, @NotNull UUID rightTrader)
	{
		super("economy:trade", InventoryHelper.INV_SIZE_6, economyInterface.getConfig().getTradeInventoryName(), MenuExitType.CLOSE);
		
		Validate.notNull((Object) economyInterface, "The economy-interface cannot be null.");
		Validate.notNull((Object) leftTrader, "The left trader uuid cannot be null.");
		Validate.notNull((Object) rightTrader, "The right trader uuid cannot be null.");	
		
		this.economy = economyInterface;
		this.leftTrader = new Trader(leftTrader);
		this.rightTrader = new Trader(rightTrader);
		
		initInventory();
		updateValues();
		openInventory();
	}

	private final EconomyInterface economy;
	private final Trader leftTrader;
	private final Trader rightTrader;
	private boolean tradeFinished = false;
	
	@Override
	public boolean handleClick(@NotNull InventoryClickEvent event)
	{
		Validate.notNull((Object) event, "The inventory click event cannot be null.");
		
		final HumanEntity human = event.getWhoClicked();
		final int clickedSlot = event.getSlot();
		final boolean isLeftTrader = human.getUniqueId().equals(leftTrader.getUUID());
		
		if (!event.isCancelled() && slotActionAllowed(isLeftTrader, event))
		{
			updateTraderSlots();
			return false;
		}
		else if (isValueSlot(clickedSlot, isLeftTrader))
		{
			openValueChooser(human, isLeftTrader);
		}
		else if (isStatusSlot(clickedSlot, isLeftTrader))
		{
			changeTraderState(isLeftTrader);
		}
		
		if (leftTrader.isReady() && rightTrader.isReady())
		{
			finishTrade();
		}
		return true;
	}
	
	@Override
	public boolean handleDrag(InventoryDragEvent event)
	{
		Validate.notNull((Object) event, "The inventory drag event cannot be null.");
		
		final boolean isLeftTrader = event.getWhoClicked().getUniqueId().equals(leftTrader.getUUID());
		final int inventorySize = event.getView().getTopInventory().getSize();
		final List<Integer> tradeSlots = Arrays.asList(isLeftTrader ? TRADERLEFT_SLOTS : TRADERRIGHT_SLOTS);
		boolean tradeInventoryChange = false;
		
		for (int slot : event.getNewItems().keySet())
		{
			if (slot < inventorySize)
			{		
				if (!tradeSlots.contains(slot))
				{
					return true;
				}
				else if (!tradeInventoryChange)
				{
					tradeInventoryChange = true;
				}			
			}
		}
		
		if (tradeInventoryChange)
		{
			revokeTraderReady(isLeftTrader);
		}	
		updateTraderSlots();
		return false;
	}
	
	@Override
	public void handleClose(InventoryCloseEvent event)
	{
		handleAbort((Player) event.getPlayer());
	}
	
	public void handleAbort(@NotNull Player player)
	{
		if (tradeFinished)
		{
			return;
		}
		Validate.notNull((Object) player, "The player cannot be null.");
		
		final boolean isLeftTrader = player.getUniqueId().equals(leftTrader.getUUID());
		
		if ((isLeftTrader && leftTrader.isValueChoosing()) || (!isLeftTrader && rightTrader.isValueChoosing()))
		{
			return;
		}
		final HumanEntity otherHuman = Bukkit.getPlayer(isLeftTrader ? rightTrader.getUUID() : leftTrader.getUUID());
		
		tradeFinished = true;
		
		if (player.isOnline())
		{
			player.getInventory().addItem(getTradeItems(isLeftTrader).toArray(new ItemStack[0]));
			player.sendMessage(MessageHelper.PREFIX_HUFF + "Der Handel wurde §cabgebrochen§7.");
		}
		MenuHolder.close(otherHuman);
		otherHuman.getInventory().addItem(getTradeItems(!isLeftTrader).toArray(new ItemStack[0]));
		otherHuman.sendMessage(MessageHelper.PREFIX_HUFF + "Der Handel wurde §cabgebrochen§7.");
	}
	
	public void handlePickup()
	{
		updateTraderSlots();
	}

	private void initInventory()
	{
		final ItemStack borderItem = InventoryHelper.getBorderItem();
		final OfflinePlayer leftPlayer = Bukkit.getOfflinePlayer(leftTrader.getUUID());
		final OfflinePlayer rightPlayer = Bukkit.getOfflinePlayer(rightTrader.getUUID());
		
		InventoryHelper.setBorder(this.getInventory(), borderItem);
		InventoryHelper.setItem(this.getInventory(), 2, 5, borderItem);
		InventoryHelper.setItem(this.getInventory(), 3, 5, borderItem);
		InventoryHelper.setItem(this.getInventory(), 4, 5, borderItem);
		InventoryHelper.setItem(this.getInventory(), 5, 5, borderItem);
		
		InventoryHelper.setItem(this.getInventory(), 1, 1, ItemHelper.getSkullWithMeta(leftPlayer, MessageHelper.getHighlighted(leftPlayer.getName(), false , false)));	
		
	    InventoryHelper.setItem(this.getInventory(), 1, 3, ItemHelper.getItemWithMeta(economy.getConfig().getValueMaterial(), 
				                                                                      MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(leftTrader.getValue()), false , false))); 
	    InventoryHelper.setItem(this.getInventory(), 1, 7, ItemHelper.getItemWithMeta(economy.getConfig().getValueMaterial(), 
                                                                                      MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(rightTrader.getValue()), false , false)));	
	    InventoryHelper.setItem(this.getInventory(), 1, 9, ItemHelper.getSkullWithMeta(rightPlayer, MessageHelper.getHighlighted(rightPlayer.getName(), false , false)));
	    
		InventoryHelper.setItem(this.getInventory(), 6, 3, ItemHelper.getItemWithMeta(NOTREADY_MATERIAL, NOTREADY_NAME));
		InventoryHelper.setItem(this.getInventory(), 6, 7, ItemHelper.getItemWithMeta(NOTREADY_MATERIAL, NOTREADY_NAME));
		
		updateTraderSlots();
		this.setMenuExitItem();
	}
	
	private void openInventory()
	{
		final Player leftPlayer = Bukkit.getPlayer(leftTrader.getUUID());
		final Player rightPlayer = Bukkit.getPlayer(rightTrader.getUUID());
		
		if (checkPlayerState(leftPlayer, true) && checkPlayerState(rightPlayer, true))
		{
			MenuHolder.open(leftPlayer, this);
			MenuHolder.open(rightPlayer, this);
		}
	}
	
	private void updateValues()
	{
		ItemHelper.updateItemWithMeta(InventoryHelper.getItem(this.getInventory(), 1, 3), MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(leftTrader.getValue()), false , false));
		ItemHelper.updateItemWithMeta(InventoryHelper.getItem(this.getInventory(), 1, 7), MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(rightTrader.getValue()), false , false));
	}
	
	private void updateTraderSlots()
	{
		Bukkit.getScheduler().runTaskLater(economy.getPlugin(), () ->
		{
			final Player leftPlayer = Bukkit.getPlayer(leftTrader.getUUID());
			final Player rightPlayer = Bukkit.getPlayer(rightTrader.getUUID());
			
			if (checkPlayerState(leftPlayer, true) && checkPlayerState(rightPlayer, true))
			{
				checkTradeSlots(rightPlayer, leftPlayer, TRADERRIGHT_SLOTS);
				checkTradeSlots(leftPlayer, rightPlayer, TRADERLEFT_SLOTS);
			}
		}, 1);
	}
	
	private void checkTradeSlots(@NotNull Player player, @NotNull Player otherPlayer, @NotNull Integer[] tradeSlots)
	{
		final ItemStack slotBlockItem = getSlotBlocker();
		int freeSlots = InventoryHelper.getFreeSlotCount(otherPlayer.getInventory());

		for (int i = 0; i < tradeSlots.length; i++)
		{
			final int currentSlot = tradeSlots[i];
			final ItemStack currentItem = this.getInventory().getItem(currentSlot);

			if (freeSlots > 0)
			{
				if (slotBlockItem.equals(currentItem))
				{
					this.getInventory().setItem(currentSlot, null);
				}
				freeSlots--;
			}
			else
			{
				if (currentItem != null && !currentItem.equals(slotBlockItem))
				{
					player.getInventory().addItem(currentItem);
				}
				
				if (!slotBlockItem.equals(currentItem))
				{
					this.getInventory().setItem(currentSlot, slotBlockItem);
				}
			}
		}
	}
	
	private @NotNull ItemStack getSlotBlocker()
	{
		final List<String> lore = new ArrayList<>();
		
		lore.add("§7Der Verhandlungspartner hat");
		lore.add("§7nicht aussreichend Platz.");
		
		return ItemHelper.getItemWithMeta(InventoryHelper.MATERIAL_FILL, "§7§lBlockiert", lore);
	}

	private boolean checkPlayerState(@Nullable Player player, boolean withoutViewing)
	{
		return player != null && player.isOnline() && 
			   (withoutViewing || InventoryHelper.isViewer(this.getInventory(), player));
	}
	
	private boolean slotActionAllowed(boolean isLeftTrader, @NotNull InventoryClickEvent event)
	{
		final InventoryAction action = event.getAction();
		final ItemStack currentItem = event.getCurrentItem();
		final ItemStack cursorItem = event.getCursor();
		final List<Integer> tradeSlots = Arrays.asList(isLeftTrader ? TRADERLEFT_SLOTS : TRADERRIGHT_SLOTS);
		
		if (action == InventoryAction.COLLECT_TO_CURSOR && cursorItem != null)
		{		
			if (handleCollectToCursor(event.getView(), tradeSlots, cursorItem))
			{
				revokeTraderReady(isLeftTrader);
				updateTraderSlots();
			}
			return false;
		}
		
		if (event.getClickedInventory().getType() == InventoryType.PLAYER)
		{
			if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY && currentItem != null)
			{
				currentItem.setAmount(InventoryHelper.addToInventorySlots(this.getInventory(), tradeSlots, currentItem));
				revokeTraderReady(isLeftTrader);
				updateTraderSlots();
				return false;
			}
			return true;
		}
		final int slot = event.getSlot();
		final boolean blockCase = getSlotBlocker().equals(this.getInventory().getItem(slot));
		final boolean leftCase = isLeftTrader && tradeSlots.contains(slot);				                 
		final boolean rightCase = !isLeftTrader && tradeSlots.contains(slot);
		
		if (blockCase || (!leftCase && !rightCase))
		{
			return false;
		}
		revokeTraderReady(isLeftTrader);
		return true;
	}
	
	private boolean handleCollectToCursor(InventoryView view, List<Integer> tradeSlots, ItemStack collectItem)
	{
		final int slotCount = view.countSlots();
		final int inventorySize = this.getInventory().getSize();
		final int maxStackSize = collectItem.getMaxStackSize();
		int openAmount = maxStackSize - collectItem.getAmount();
		boolean changedTradeSlot = false;
		
		for (int i = 0; i < slotCount && openAmount > 0; i++)
		{
			boolean isTradeSlot = tradeSlots.contains(i);
			
			if (i < inventorySize && !isTradeSlot)
			{
				continue;
			}
			final ItemStack currentItem = view.getItem(i);
			
			if (collectItem.isSimilar(currentItem))
			{
				int currentAmount = currentItem.getAmount();
				
				if (openAmount < currentAmount)
				{
					currentItem.setAmount(currentAmount - openAmount);
					openAmount = 0;
				}
				else
				{
					currentItem.setAmount(0);
					openAmount -= currentAmount;
				}
				
				if (isTradeSlot && !changedTradeSlot)
				{
					changedTradeSlot = true;
				}
			}
		}
		collectItem.setAmount(maxStackSize - openAmount);
		return changedTradeSlot;
	}
	
	private boolean isValueSlot(int slot, boolean expectLeft)
	{
		return slot == InventoryHelper.getSlotFromRowColumn(this.getInventory().getSize(), 1, expectLeft ? 3 : 7);
	}
	
	private void openValueChooser(@NotNull HumanEntity human, boolean isLeftTrader) 
	{
		if ((isLeftTrader && !leftTrader.isReady()) || (!isLeftTrader && !rightTrader.isReady()))
		{
			double currentValue = 0;
			
			if (isLeftTrader)
			{
				currentValue = leftTrader.getValue();
				leftTrader.setValueChoosing(true);
			}
			else
			{
				currentValue = rightTrader.getValue();
				rightTrader.setValueChoosing(true);
			}
			MenuHolder.open(human, new TransactionHolder(economy, TransactionKind.WALLET_CHOOSE, human.getUniqueId(),
                                   currentValue, getValueFinishAction(isLeftTrader)));
		}
	}
	
	private Action getValueFinishAction(boolean isLeftTrader)
	{
		return params -> 
		{	
			if (params != null && params.length > 0 && params[0] instanceof Double) 
			{
				if (isLeftTrader)
				{
					leftTrader.addValue((double) params[0]);
				}
				else
				{
					rightTrader.addValue((double) params[0]);
				}
				updateValues();
			}	
			final Player player = Bukkit.getPlayer(isLeftTrader ? leftTrader.getUUID() : rightTrader.getUUID());
			
			if (checkPlayerState(player, true))
			{
				if (isLeftTrader)
				{
					leftTrader.setValueChoosing(false);
				}
				else
				{
					rightTrader.setValueChoosing(false);
				}
				MenuHolder.back(player);
			}
		};
	}
	
	private boolean isStatusSlot(int slot, boolean expectLeft)
	{
		return slot == InventoryHelper.getSlotFromRowColumn(this.getInventory().getSize(), 6, expectLeft ? 3 : 7);
	}
	
	private void revokeTraderReady(boolean isLeftTrader)
	{
		if (isLeftTrader ? leftTrader.isReady() : rightTrader.isReady())
		{
			changeTraderState(isLeftTrader);		
		}
	}
	
	private void changeTraderState(boolean isLeftTrader) 
	{	
		if (isLeftTrader)
		{
			leftTrader.changeReady();
		}
		else
		{
			rightTrader.changeReady();
		}
		final ItemStack statusItem = InventoryHelper.getItem(this.getInventory(), 6, isLeftTrader ? 3 : 7);
		
		if (isLeftTrader ? leftTrader.isReady() : rightTrader.isReady())
		{
			Bukkit.getScheduler().runTaskLater(economy.getPlugin(), () ->
			{
				statusItem.setType(READY_MATERIAL);
				ItemHelper.updateItemWithMeta(statusItem, READY_NAME);
			}, 1);
		}
		else
		{
			Bukkit.getScheduler().runTaskLater(economy.getPlugin(), () ->
			{
				statusItem.setType(NOTREADY_MATERIAL);
				ItemHelper.updateItemWithMeta(statusItem, NOTREADY_NAME);
			}, 1);
		}
	}
	
	private void finishTrade()
	{
		tradeFinished = true;
		
		final Player leftPlayer = Bukkit.getPlayer(leftTrader.getUUID());
		final Player rightPlayer = Bukkit.getPlayer(rightTrader.getUUID());
		
		if (checkPlayerState(leftPlayer, false) && checkPlayerState(rightPlayer, false))
		{
			if (leftTrader.getValue() != 0)
			{
				economy.getStorage().runTransaction(leftTrader.getUUID(), rightTrader.getUUID(), 
						                            leftTrader.getValue(), false);
			}
			
			if (rightTrader.getValue() != 0)
			{
				economy.getStorage().runTransaction(rightTrader.getUUID(), leftTrader.getUUID(), 
						                            rightTrader.getValue(), false);
			}		
			leftPlayer.getInventory().addItem(getTradeItems(false).toArray(new ItemStack[0]));		
			rightPlayer.getInventory().addItem(getTradeItems(true).toArray(new ItemStack[0]));	
			leftPlayer.sendMessage(getTradeOverview(rightPlayer.getName(), leftTrader, rightTrader));
			rightPlayer.sendMessage(getTradeOverview(leftPlayer.getName(), rightTrader, leftTrader));
			MenuHolder.close(leftPlayer);
			MenuHolder.close(rightPlayer);
		}
	}
	
	private List<ItemStack> getTradeItems(boolean isLeftTrader)
	{
		final List<ItemStack> tradeItems = new ArrayList<>();
		final List<Integer> tradeSlots = Arrays.asList(isLeftTrader ? TRADERLEFT_SLOTS : TRADERRIGHT_SLOTS);
		final int inventorySize = this.getInventory().getSize();
		final ItemStack slotBlockItem = getSlotBlocker();	
		
		for (int i = 0; i < inventorySize; i++)
		{
			if (tradeSlots.contains(i))
			{
				final ItemStack currentItem = this.getInventory().getItem(i);
				
				if (currentItem == null || currentItem.equals(slotBlockItem))
				{
					continue;
				}				
				tradeItems.add(currentItem);
				
				if (isLeftTrader)
				{
					leftTrader.addItems(currentItem.getAmount());
				}
				else
				{
					rightTrader.addItems(currentItem.getAmount());
				}
			}
		}
		return tradeItems;
	}
	
	private String getTradeOverview(@NotNull String partnerName, @NotNull Trader traderSelf, @NotNull Trader traderOther)
	{
		return String.format("§8☰ §7Handel §aabgeschlossen §7- Übersicht\n \n" +
	                         "§8☷ §7Verhandlungspartner §8↔ §9%1$s\n \n" +
	                         "§8☷ §7%2$-12s §8→ §9%3$.0f\n" +
	                         "§8☷ §7%5$-12s §8→ §9%6$d\n" +
	                         "§8☷ §7%2$-12s §8← §9%4$.0f\n" +
	                         "§8☷ §7%5$-12s §8← §9%7$d", 
	                         partnerName, economy.getConfig().getValueName(), 
	                         traderSelf.getValue(), traderOther.getValue(),
	                         "Güter",
	                         traderSelf.getItems(), traderOther.getItems());
	}
}
