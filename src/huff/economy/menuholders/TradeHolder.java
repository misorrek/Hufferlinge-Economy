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
import org.jetbrains.annotations.Nullable;

import huff.economy.EconomyInterface;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.ItemHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.interfaces.Action;
import huff.lib.various.MenuHolder;

public class TradeHolder extends MenuHolder
{
	public static final String MENU_IDENTIFIER = "menu:economy:interaction";
	
	private static final Integer[] TRADERLEFT_SLOTS = {10,11,12,19,20,21,28,29,30,37,38,39};
	private static final Integer[] TRADERRIGHT_SLOTS = {14,15,16,23,24,25,32,33,34,41,42,43};
	private static final String NOTREADY_NAME = "§6Handel ausstehend...";
	private static final Material NOTREADY_MATERIAL = Material.ORANGE_STAINED_GLASS_PANE;
	private static final String READY_NAME = "§aHandel akzeptiert";
	private static final Material READY_MATERIAL = Material.LIME_STAINED_GLASS_PANE;
	
	public TradeHolder(@NotNull EconomyInterface economyInterface, @NotNull UUID leftTrader, @NotNull UUID rightTrader)
	{
		super(MENU_IDENTIFIER, InventoryHelper.INV_SIZE_6, economyInterface.getConfig().getTradeInventoryName());
		
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

	public boolean handleEvent(@NotNull int clickedSlot, @NotNull HumanEntity human)
	{
		Validate.notNull((Object) human, "The human cannot be null.");
		
		final boolean isLeftTrader = human.getUniqueId().equals(leftTrader.getUUID());
		
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
		
		if (leftTrader.isReady() && rightTrader.isReady())
		{
			finishTrade();
		}
		return true;
	}
	
	public void handleClose(@NotNull Player player)
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
			player.sendMessage(MessageHelper.PREFIX_HUFF + "Handel §cabgebrochen§7.");
		}
		otherHuman.closeInventory();
		otherHuman.sendMessage(MessageHelper.PREFIX_HUFF + "Handel §cabgebrochen§7.");
	}

	private void initInventory()
	{
		final ItemStack borderItem = InventoryHelper.getBorderItem();
		final OfflinePlayer traderLeftPlayer = Bukkit.getOfflinePlayer(leftTrader.getUUID());
		final OfflinePlayer traderRightPlayer = Bukkit.getOfflinePlayer(rightTrader.getUUID());
		
		InventoryHelper.setBorder(this.getInventory(), borderItem);
		InventoryHelper.setItem(this.getInventory(), 2, 5, borderItem);
		InventoryHelper.setItem(this.getInventory(), 3, 5, borderItem);
		InventoryHelper.setItem(this.getInventory(), 4, 5, borderItem);
		InventoryHelper.setItem(this.getInventory(), 5, 5, borderItem);
		
		InventoryHelper.setItem(this.getInventory(), 1, 1, ItemHelper.getSkullWithMeta(traderLeftPlayer, MessageHelper.getHighlighted(traderLeftPlayer.getName(), false , false)));	
		
	    InventoryHelper.setItem(this.getInventory(), 1, 3, ItemHelper.getItemWithMeta(economy.getConfig().getValueMaterial(), 
				                                                                      MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(leftTrader.getValue()), false , false))); 
	    InventoryHelper.setItem(this.getInventory(), 1, 7, ItemHelper.getItemWithMeta(economy.getConfig().getValueMaterial(), 
                                                                                      MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(rightTrader.getValue()), false , false)));	
	    InventoryHelper.setItem(this.getInventory(), 1, 9, ItemHelper.getSkullWithMeta(traderRightPlayer, MessageHelper.getHighlighted(traderRightPlayer.getName(), false , false)));
		
		InventoryHelper.setItem(this.getInventory(), 6, 3, ItemHelper.getItemWithMeta(NOTREADY_MATERIAL, NOTREADY_NAME));
		InventoryHelper.setItem(this.getInventory(), 6, 5, InventoryHelper.getCloseItem());
		InventoryHelper.setItem(this.getInventory(), 6, 7, ItemHelper.getItemWithMeta(NOTREADY_MATERIAL, NOTREADY_NAME));
	}
	
	private void updateValues()
	{
		ItemHelper.updateItemWithMeta(InventoryHelper.getItem(this.getInventory(), 1, 3), MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(leftTrader.getValue()), false , false));
		ItemHelper.updateItemWithMeta(InventoryHelper.getItem(this.getInventory(), 1, 7), MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(rightTrader.getValue()), false , false));
	}
	
	private void openInventory()
	{
		final Player leftPlayer = Bukkit.getPlayer(leftTrader.getUUID());
		final Player rightPlayer = Bukkit.getPlayer(rightTrader.getUUID());
		
		if (checkPlayerState(leftPlayer) && checkPlayerState(rightPlayer))
		{
			leftPlayer.closeInventory();
			leftPlayer.openInventory(this.getInventory());
			
			rightPlayer.closeInventory();
			rightPlayer.openInventory(this.getInventory());
		}
	}
	
	private boolean checkPlayerState(@Nullable Player player)
	{
		return player != null && player.isOnline() && 
			   InventoryHelper.isViewer(this.getInventory(), player);
	}
	
	private boolean itemPlaceAllowed(boolean isLeftTrader, int slot)
	{
		final List<Integer> tradeSlots = Arrays.asList(isLeftTrader ? TRADERLEFT_SLOTS : TRADERRIGHT_SLOTS);
		final boolean leftCase = isLeftTrader && !leftTrader.isReady() && tradeSlots.contains(slot);				                 
		final boolean rightCase = !isLeftTrader && !rightTrader.isReady() && tradeSlots.contains(slot);
		
		return leftCase || rightCase;
	}
	
	private boolean isValueSlot(int slot, boolean expectLeft)
	{
		return slot == InventoryHelper.getSlotFromRowColumn(this.getInventory().getSize(), 1, expectLeft ? 3 : 7);
	}
	
	private void openValueChooser(@NotNull HumanEntity human, boolean isLeftTrader) 
	{
		if ((isLeftTrader && !leftTrader.isReady()) || (!isLeftTrader && !rightTrader.isReady()))
		{
			human.closeInventory();
			human.openInventory(new TransactionHolder(economy, getValueFinishAction(isLeftTrader)).getInventory());	
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
			
			if (checkPlayerState(player))
			{
				player.closeInventory();
				player.openInventory(getInventory());	
			}
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
			leftTrader.changeReady();
		}
		else
		{
			rightTrader.changeReady();
		}
	}
	
	private void finishTrade()
	{
		tradeFinished = true;
		
		final Player leftPlayer = Bukkit.getPlayer(leftTrader.getUUID());
		final Player rightPlayer = Bukkit.getPlayer(rightTrader.getUUID());
		
		if (checkPlayerState(leftPlayer) && checkPlayerState(rightPlayer))
		{
			if (leftTrader.getValue() != 0)
			{
				economy.getStorage().updateBalance(leftTrader.getUUID(), leftTrader.getValue(), true, false);
				economy.getStorage().updateBalance(rightTrader.getUUID(), leftTrader.getValue(), false, false);
			}
			
			if (rightTrader.getValue() != 0)
			{
				economy.getStorage().updateBalance(rightTrader.getUUID(), rightTrader.getValue(), true, false);
				economy.getStorage().updateBalance(leftTrader.getUUID(), rightTrader.getValue(), false, false);
			}
			
			leftPlayer.getInventory().addItem(getTradeItems(false).toArray(new ItemStack[0]));
			leftPlayer.sendMessage(getTradeOverview(rightPlayer.getName(), leftTrader, rightTrader));
			leftPlayer.closeInventory();
			
			rightPlayer.getInventory().addItem(getTradeItems(true).toArray(new ItemStack[0]));
			rightPlayer.sendMessage(getTradeOverview(leftPlayer.getName(), rightTrader, leftTrader));
			rightPlayer.closeInventory();
		}
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
		return String.format("§8☰ §7Handel §aabgeschlossen - Übersicht\n\n" +
	                         "§8☷ §7Verhandlungspartner: §9%s\n" +
	                         "§8☷ §7%2$s abgegeben: §9%.0f\n" +
	                         "§8☷ §7%2$s bekommen: §9%.0f\n" +
	                         "§8☷ §7Güter abgegeben: §9%d\n" +
	                         "§8☷ §7Güter bekommen: §9%d", 
	                         partnerName, economy.getConfig().getValueName(), 
	                         traderSelf.getValue(), traderOther.getValue(), 
	                         traderSelf.getItems(), traderOther.getItems());
	}
}
