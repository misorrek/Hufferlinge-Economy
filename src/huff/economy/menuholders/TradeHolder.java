package huff.economy.menuholders;

import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.economy.EconomyInterface;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.ItemHelper;
import huff.lib.various.MenuHolder;

public class TradeHolder extends MenuHolder
{
	public static final String MENU_IDENTIFIER = "menu:economy:interaction";
	
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
	}

	private final EconomyInterface economy;
	private final UUID traderLeft;
	private final UUID traderRight;

	public void handleEvent(@Nullable ItemStack currentItem, @NotNull HumanEntity human)
	{
		Validate.notNull((Object) human, "The human cannot be null.");
		
		if (currentItem == null || currentItem.getItemMeta() == null)
		{
			return;
		}
	}
	
	private void initInventory()
	{
		final OfflinePlayer traderLeftPlayer = Bukkit.getOfflinePlayer(traderLeft);
		final OfflinePlayer traderRightPlayer = Bukkit.getOfflinePlayer(traderRight);
		
		InventoryHelper.setBorder(this.getInventory(), InventoryHelper.getBorderItem());
		InventoryHelper.setFill(this.getInventory(), InventoryHelper.getFillItem(), false);
		
		InventoryHelper.setItem(this.getInventory(), 2, 2, ItemHelper.getItemWithMeta(economy.getConfig().getValueMaterial(), 
				                                                                      economy.getConfig().getTransactionInventoryName(TransactionKind.WALLET_OTHER))); 
		
		//InventoryHelper.setItem(this.getInventory(), 3, 5, ItemHelper.getSkullWithMeta(targetPlayer, "§7Empfänger: §9" + targetPlayer.getName()));
	
		InventoryHelper.setItem(this.getInventory(), 2, 8, ItemHelper.getItemWithMeta(economy.getConfig().getTradeMaterial(),
				                                                                      economy.getConfig().getTradeInventoryName()));
		InventoryHelper.setItem(this.getInventory(), 3, 5, InventoryHelper.getCloseItem());
	}
}
