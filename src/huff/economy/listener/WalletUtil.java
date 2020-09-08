package huff.economy.listener;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.economy.EconomyConfig;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.StringHelper;

public class WalletUtil
{
	public static final int PAY_ADD_1 = 1;
	public static final int PAY_ADD_2 = 10;
	public static final int PAY_ADD_3 = 100;
	public static final int PAY_REMOVE_1 = 1;
	public static final int PAY_REMOVE_2 = 10;
	public static final int PAY_REMOVE_3 = 100;
	
	public static final String ITEM_PAY_ADD_1 = "§a+ " + PAY_ADD_1;
	public static final String ITEM_PAY_ADD_2 = "§a+ " + PAY_ADD_2;
	public static final String ITEM_PAY_ADD_3 = "§a+ " + PAY_ADD_3;
	public static final String ITEM_PAY_REMOVE_1 = "§c- " + PAY_REMOVE_1;
	public static final String ITEM_PAY_REMOVE_2 = "§c- " + PAY_REMOVE_2;
	public static final String ITEM_PAY_REMOVE_3 = "§c- " + PAY_REMOVE_3;
	public static final String ITEM_PERFORMPAY = "§7» §aHerausnehmen";
	
	private WalletUtil() { }
	
	public static @NotNull ItemStack getValueItem(@NotNull EconomyConfig economyConfig)
	{
		Validate.notNull((Object) economyConfig, "The economy-config cannot be null.");
		
		return InventoryHelper.getItemWithMeta(economyConfig.getValueMaterial(), "§e§l" + economyConfig.getValueName());
	}
	
	public static@NotNull ItemStack getWalletItem(@NotNull EconomyConfig economyConfig)
	{
		Validate.notNull((Object) economyConfig, "The economy-config cannot be null.");
		
		return InventoryHelper.getItemWithMeta(economyConfig.getWalletMaterial(), "§6§l" + economyConfig.getWalletName());
	}
	
	public static @NotNull String getWalletInventoryName(@NotNull String walletName)
	{
		Validate.notNull((Object) walletName, "The wallet-name cannot be null.");
		
		return "§7» §6" + walletName;
	}
	
	public static @NotNull String getPayInventoryName(@NotNull String valueName)
	{
		Validate.notNull((Object) valueName, "The value-name cannot be null.");
		
		return "§7» §e" + valueName + " herausnehmen";
	}
	
	public static @NotNull Inventory getWalletInventory(@NotNull EconomyConfig economyConfig, double currentWallet)
	{
		Validate.notNull((Object) economyConfig, "The economy-config cannot be null.");
	
		Inventory walletInventory = Bukkit.createInventory(null, 27, getWalletInventoryName(economyConfig.getWalletName()));
		ItemStack borderItem = InventoryHelper.getBorderItem();
		ItemStack fillItem = InventoryHelper.getFillItem();
		String valueName = economyConfig.getValueName();
		
		//FIRST
		walletInventory.setItem(0, borderItem);
		walletInventory.setItem(1, borderItem);
		walletInventory.setItem(2, borderItem);
		walletInventory.setItem(3, borderItem);
		walletInventory.setItem(4, borderItem);
		walletInventory.setItem(5, borderItem);
		walletInventory.setItem(6, borderItem);
		walletInventory.setItem(7, borderItem);
		walletInventory.setItem(8, borderItem);
		//SECOND
		walletInventory.setItem(9, borderItem);
		walletInventory.setItem(10, InventoryHelper.getItemWithMeta(economyConfig.getValueMaterial(), getValueAmountName(valueName, currentWallet)));
		walletInventory.setItem(11, borderItem);
		walletInventory.setItem(12, fillItem);
		walletInventory.setItem(13, fillItem);
		walletInventory.setItem(14, fillItem);
		walletInventory.setItem(15, borderItem);
		walletInventory.setItem(16, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getPayInventoryName(valueName)));
		walletInventory.setItem(17, borderItem);
		//THIRD
		walletInventory.setItem(18, borderItem);
		walletInventory.setItem(19, borderItem);
		walletInventory.setItem(20, borderItem);
		walletInventory.setItem(21, borderItem);
		walletInventory.setItem(22, InventoryHelper.getCloseItem());
		walletInventory.setItem(23, borderItem);
		walletInventory.setItem(24, borderItem);
		walletInventory.setItem(25, borderItem);
		walletInventory.setItem(26, borderItem);
		
		return walletInventory;
	}
	
	public static @NotNull Inventory getPayInventory(@NotNull EconomyConfig economyConfig, String playerName)
	{
		Validate.notNull((Object) economyConfig, "The economy-config cannot be null.");
	
		Inventory walletInventory = Bukkit.createInventory(null, 27, getPayInventoryName(economyConfig.getValueName()));
		ItemStack borderItem = InventoryHelper.getBorderItem();	
		String valueName = economyConfig.getValueName();
			
		//FIRST
		walletInventory.setItem(0, borderItem);
		walletInventory.setItem(1, borderItem);
		walletInventory.setItem(2, borderItem);
		walletInventory.setItem(3, borderItem);
		if (StringHelper.isNotNullOrEmpty(playerName))
		{
			walletInventory.setItem(4, InventoryHelper.getItemWithMeta(Material.BLUE_STAINED_GLASS_PANE, "§7Ziel: §9" + playerName));
		}
		else
		{
			walletInventory.setItem(4, borderItem);
		}
		walletInventory.setItem(5, borderItem);
		walletInventory.setItem(6, borderItem);
		walletInventory.setItem(7, borderItem);
		walletInventory.setItem(8, borderItem);
		//SECOND
		walletInventory.setItem(9, borderItem);
		walletInventory.setItem(10, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, ITEM_PAY_ADD_3));
		walletInventory.setItem(11, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, ITEM_PAY_ADD_2));
		walletInventory.setItem(12, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, ITEM_PAY_ADD_1));
		walletInventory.setItem(13, InventoryHelper.getItemWithMeta(economyConfig.getValueMaterial(), getValueAmountName(valueName, 0)));
		walletInventory.setItem(14, InventoryHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, ITEM_PAY_REMOVE_1));
		walletInventory.setItem(15, InventoryHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, ITEM_PAY_REMOVE_2));
		walletInventory.setItem(16, InventoryHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, ITEM_PAY_REMOVE_3));
		walletInventory.setItem(17, borderItem);
		//THIRD
		walletInventory.setItem(18, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, ITEM_PERFORMPAY));
		walletInventory.setItem(19, borderItem);
		walletInventory.setItem(20, borderItem);
		walletInventory.setItem(21, borderItem);
		walletInventory.setItem(22, borderItem);
		walletInventory.setItem(23, borderItem);
		walletInventory.setItem(24, borderItem);
		walletInventory.setItem(25, borderItem);
		walletInventory.setItem(26, InventoryHelper.getAbortItem());
		
		return walletInventory;
	}
		
	public static @NotNull String getValueAmountName(@NotNull String valueName, double currentValue)
	{
		Validate.notNull((Object) valueName, "The value-name cannot be null.");
		
		return String.format("§9%.0f %s", currentValue, valueName);
	}
	
	public static @Nullable Player getPayTargetPlayer(@NotNull Inventory payInventory)
	{
		Validate.notNull((Object) payInventory, "The pay-inventory cannot be null.");
		
		ItemStack targetItem = payInventory.getItem(4);
		
		if (targetItem == null)
		{
			return null;
		}	
		Player targetPlayer = Bukkit.getPlayer(targetItem.getItemMeta().getDisplayName());
		
		return (targetPlayer != null) ? targetPlayer : null;
	}
	
	public static @Nullable double getPayValueAmount(@NotNull Inventory payInventory)
	{
		Validate.notNull((Object) payInventory, "The pay-inventory cannot be null.");
		
		ItemStack targetItem = payInventory.getItem(13);
		
		if (targetItem != null)
		{
			try
			{
				Pattern valuePattern = Pattern.compile("§.([0-9.]*).*");
				Matcher matcher = valuePattern.matcher(targetItem.getItemMeta().getDisplayName());
				
				while (matcher.find())
				{
					return Double.parseDouble(matcher.group(1));
				}
			}
			catch (NumberFormatException exception)
			{
				Bukkit.getLogger().log(Level.WARNING, "The pay-value-amount is invalid.", exception);
			}
		}
		return 0;
	} 
	
	public static void setPayValueAmount(@NotNull Inventory payInventory, @NotNull String valueName, @NotNull double updatedValue)
	{
		Validate.notNull((Object) payInventory, "The pay-inventory cannot be null.");
		
		ItemStack valueAmountItem = payInventory.getItem(13);
		ItemMeta valueAmountMeta = valueAmountItem.getItemMeta();
		valueAmountMeta.setDisplayName(getValueAmountName(valueName, updatedValue));
		valueAmountItem.setItemMeta(valueAmountMeta);	
	}
}