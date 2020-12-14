package huff.economy.menuholders;

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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import huff.economy.EconomyInterface;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.ItemHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.interfaces.Action;
import huff.lib.various.MenuHolder;
import huff.lib.various.Pair;

public class TradeHolder extends MenuHolder
{
	public static final String MENU_IDENTIFIER = "menu:economy:interaction";
	
	private static final String NOTREADY_NAME = "§6Handel ausstehend...";
	private static final Material NOTREADY_MATERIAL = Material.ORANGE_STAINED_GLASS_PANE;
	private static final String READY_NAME = "§aHandel akzeptiert";
	private static final Material READY_MATERIAL = Material.LIME_STAINED_GLASS_PANE;
	private static final Integer[] TRADERLEFT_SLOTS = {10,11,12,19,20,21,28,29,30,37,38,39};
	private static final Integer[] TRADERRIGHT_SLOTS = {14,15,16,23,24,25,32,33,34,41,42,43};
	
	public TradeHolder(@NotNull EconomyInterface economyInterface, @NotNull UUID traderLeft, @NotNull UUID traderRight)
	{
		super(MENU_IDENTIFIER, InventoryHelper.INV_SIZE_6, economyInterface.getConfig().getTradeInventoryName());
		
		Validate.notNull((Object) economyInterface, "The economy-interface cannot be null.");
		Validate.notNull((Object) traderLeft, "The left trader cannot be null.");
		Validate.notNull((Object) traderRight, "The right trader cannot be null.");	
		
		this.economy = economyInterface;
		this.traderLeft = traderLeft;
		this.traderRight = traderRight;
		
		initInventory();
		updateValues();
		openInventory();
	}

	private final EconomyInterface economy;
	private final UUID traderLeft;
	private final UUID traderRight;
	private double leftValue = 0;
	private double rightValue = 0;
	private int leftItems = 0;
	private int rightItems = 0;
	private boolean leftReady = false;
	private boolean rightReady = false; 

	public boolean handleEvent(@NotNull int clickedSlot, @NotNull HumanEntity human)
	{
		Validate.notNull((Object) human, "The human cannot be null.");
		
		final boolean isLeftTrader = human.getUniqueId().equals(traderLeft);
		
		if (itemPlaceAllowed(isLeftTrader, clickedSlot))
		{
			return false;
		}
		else if (isValueSlot(clickedSlot, isLeftTrader))
		{
			openValueChooser(human, isLeftTrader);
		}
		else if (isStatusSlot(clickedSlot, isLeftTrader))
		{
			setTraderReady(isLeftTrader);
		}
		
		if (leftReady && rightReady)
		{
			finishTrade();
		}
		return true;
	}

	private void initInventory()
	{
		final ItemStack borderItem = InventoryHelper.getBorderItem();
		final OfflinePlayer traderLeftPlayer = Bukkit.getOfflinePlayer(traderLeft);
		final OfflinePlayer traderRightPlayer = Bukkit.getOfflinePlayer(traderRight);
		
		InventoryHelper.setBorder(this.getInventory(), borderItem);
		InventoryHelper.setItem(this.getInventory(), 2, 5, borderItem);
		InventoryHelper.setItem(this.getInventory(), 3, 5, borderItem);
		InventoryHelper.setItem(this.getInventory(), 4, 5, borderItem);
		InventoryHelper.setItem(this.getInventory(), 5, 5, borderItem);
		
		InventoryHelper.setItem(this.getInventory(), 1, 1, ItemHelper.getSkullWithMeta(traderLeftPlayer, MessageHelper.getHighlighted(traderLeftPlayer.getName(), false , false)));	
		
	    InventoryHelper.setItem(this.getInventory(), 1, 3, ItemHelper.getItemWithMeta(economy.getConfig().getValueMaterial(), 
				                                                                      MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(leftValue), false , false))); 
	    InventoryHelper.setItem(this.getInventory(), 1, 7, ItemHelper.getItemWithMeta(economy.getConfig().getValueMaterial(), 
                                                                                      MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(rightValue), false , false)));	
	    InventoryHelper.setItem(this.getInventory(), 1, 9, ItemHelper.getSkullWithMeta(traderRightPlayer, MessageHelper.getHighlighted(traderRightPlayer.getName(), false , false)));
		
		InventoryHelper.setItem(this.getInventory(), 6, 3, ItemHelper.getItemWithMeta(NOTREADY_MATERIAL, NOTREADY_NAME));
		InventoryHelper.setItem(this.getInventory(), 6, 5, InventoryHelper.getAbortItem());
		InventoryHelper.setItem(this.getInventory(), 6, 7, ItemHelper.getItemWithMeta(NOTREADY_MATERIAL, NOTREADY_NAME));
	}
	
	private void updateValues()
	{
		ItemHelper.updateItemWithMeta(InventoryHelper.getItem(this.getInventory(), 1, 3), MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(leftValue), false , false));
		ItemHelper.updateItemWithMeta(InventoryHelper.getItem(this.getInventory(), 1, 7), MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(rightValue), false , false));
	}
	
	private void openInventory()
	{
		final Player leftPlayer = Bukkit.getPlayer(traderLeft);
		final Player rightPlayer = Bukkit.getPlayer(traderRight);
		
		leftPlayer.closeInventory();
		leftPlayer.openInventory(this.getInventory());
		
		rightPlayer.closeInventory();
		rightPlayer.openInventory(this.getInventory());
	}
	
	private boolean itemPlaceAllowed(boolean isLeftTrader, int slot)
	{
		final Pair<Integer, Integer> rowColumn = InventoryHelper.getRowColumnFromSlot(slot);
		final boolean leftCase = isLeftTrader && !leftReady && 
				                 rowColumn.value1 >= 2 && rowColumn.value1 < 6 && 
				                 rowColumn.value2 >= 2 && rowColumn.value2 < 5;
				                 
		final boolean rightCase = !isLeftTrader && !rightReady && 
				                  rowColumn.value1 >= 2 && rowColumn.value1 < 6 && 
				                  rowColumn.value2 >= 6 && rowColumn.value2 < 9;
		
		return leftCase || rightCase;
	}
	
	private boolean isValueSlot(int slot, boolean expectLeft)
	{
		return slot == InventoryHelper.getSlotFromRowColumn(this.getInventory().getSize(), 1, expectLeft ? 3 : 7);
	}
	
	private void openValueChooser(@NotNull HumanEntity human, boolean isLeftTrader) 
	{
		human.closeInventory();
		human.openInventory(new TransactionHolder(economy, getValueFinishAction(isLeftTrader)).getInventory());	
	}
	
	private Action getValueFinishAction(boolean isLeftTrader)
	{
		return params -> 
		{	
			if (params != null && params.length > 0 && params[0] instanceof Double) 
			{
				if (isLeftTrader)
				{
					leftValue += (double) params[0];
				}
				else
				{
					rightValue += (double) params[0];
				}
				updateValues();
			}	
			final Player player = Bukkit.getPlayer(isLeftTrader ? traderLeft : traderRight);
			
			player.closeInventory();
			player.openInventory(getInventory());			
		};
	}
	
	private boolean isStatusSlot(int slot, boolean expectLeft)
	{
		return slot == InventoryHelper.getSlotFromRowColumn(this.getInventory().getSize(), 6, expectLeft ? 3 : 7);
	}
	
	private void setTraderReady(boolean isLeftTrader) 
	{
		final ItemStack statusItem = InventoryHelper.getItem(this.getInventory(), 6, isLeftTrader ? 3 : 7);
		
		statusItem.setType(READY_MATERIAL);
		ItemHelper.updateItemWithMeta(statusItem, READY_NAME);
		
		if (isLeftTrader)
		{
			leftReady = true;
		}
		else
		{
			rightReady = true;
		}
	}
	
	private void finishTrade()
	{
		if (leftValue != 0)
		{
			economy.getStorage().updateBalance(traderLeft, leftValue, true, false);
			economy.getStorage().updateBalance(traderRight, leftValue, false, false);
		}
		
		if (rightValue != 0)
		{
			economy.getStorage().updateBalance(traderRight, rightValue, true, false);
			economy.getStorage().updateBalance(traderLeft, rightValue, false, false);
		}
		final Player leftPlayer = Bukkit.getPlayer(traderLeft);
		final Player rightPlayer = Bukkit.getPlayer(traderRight);
		
		leftPlayer.getInventory().addItem(getTradeItems(false).toArray(new ItemStack[0]));
		leftPlayer.sendMessage(getTradeOverview(rightPlayer.getName(), leftValue, rightValue, leftItems, rightItems));
		leftPlayer.closeInventory();
		
		rightPlayer.getInventory().addItem(getTradeItems(true).toArray(new ItemStack[0]));
		rightPlayer.sendMessage(getTradeOverview(leftPlayer.getName(), rightValue, leftValue, rightItems, leftItems));
		rightPlayer.closeInventory();
	}
	
	private List<ItemStack> getTradeItems(boolean isLeftTrader)
	{
		final List<ItemStack> tradeItems = new ArrayList<>();
		final List<Integer> tradeSlots = Arrays.asList(isLeftTrader ? TRADERLEFT_SLOTS : TRADERRIGHT_SLOTS);
		final int inventorySize = this.getInventory().getSize();
			
		for (int i = 0; i < inventorySize; i++)
		{
			if (tradeSlots.contains(i))
			{
				final ItemStack currentItem = this.getInventory().getItem(i);
				
				tradeItems.add(currentItem);
				
				if (isLeftTrader)
				{
					leftItems += currentItem.getAmount();
				}
				else
				{
					rightItems += currentItem.getAmount();
				}
			}
		}
		return tradeItems;
	}
	
	private String getTradeOverview(@NotNull String partnerName, double valueOut, double valueIn, int itemsOut, int itemsIn)
	{
		return String.format("§8☰ §7Handel §aabgeschlossen - Übersicht\n\n" +
	                         "§8☷ §7Verhandlungspartner: §9%s\n" +
	                         "§8☷ §7%2$s abgegeben: §9%.0f\n" +
	                         "§8☷ §7%2$s bekommen: §9%.0f\n" +
	                         "§8☷ §7Güter abgegeben: §9%d\n" +
	                         "§8☷ §7Güter bekommen: §9%d", 
	                         partnerName, economy.getConfig().getValueName(), valueOut, valueIn, itemsOut, itemsIn);
	}
}
