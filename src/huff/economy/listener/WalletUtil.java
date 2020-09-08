package huff.economy.listener;

import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
		
		ItemStack valueItem = new ItemStack(economyConfig.getValueMaterial());
		valueItem.getItemMeta().setDisplayName("§e§l" + economyConfig.getValueName());
		
		return valueItem;
	}
	
	public static@NotNull  ItemStack getWalletItem(@NotNull EconomyConfig economyConfig)
	{
		Validate.notNull((Object) economyConfig, "The economy-config cannot be null.");
		
		ItemStack walletItem = new ItemStack(economyConfig.getWalletMaterial());
		walletItem.getItemMeta().setDisplayName("§6§l" + economyConfig.getWalletName());
		
		return walletItem;
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
		
		String valueName = economyConfig.getValueName();
		
		ItemStack borderItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemStack fillItem = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		
		ItemStack valueItem = new ItemStack(economyConfig.getValueMaterial());
		valueItem.getItemMeta().setDisplayName(getValueAmountName(valueName, currentWallet));
		
		ItemStack payItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
		valueItem.getItemMeta().setDisplayName(getPayInventoryName(valueName));
			
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
		walletInventory.setItem(10, valueItem);
		walletInventory.setItem(11, borderItem);
		walletInventory.setItem(12, fillItem);
		walletInventory.setItem(13, fillItem);
		walletInventory.setItem(14, fillItem);
		walletInventory.setItem(15, borderItem);
		walletInventory.setItem(16, payItem);
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
		
		String valueName = economyConfig.getValueName();
		
		ItemStack borderItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		
		ItemStack valueItem = new ItemStack(economyConfig.getValueMaterial());
		valueItem.getItemMeta().setDisplayName(getValueAmountName(valueName, 0));
		
		ItemStack performPayItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
		valueItem.getItemMeta().setDisplayName(ITEM_PERFORMPAY);
		
		ItemStack payAddItem1 = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
		payAddItem1.getItemMeta().setDisplayName(ITEM_PAY_ADD_1);
		
		ItemStack payAddItem2 = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
		payAddItem1.getItemMeta().setDisplayName(ITEM_PAY_ADD_2);
		
		ItemStack payAddItem3 = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
		payAddItem1.getItemMeta().setDisplayName(ITEM_PAY_ADD_3);
		
		ItemStack payRemoveItem1 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
		payAddItem1.getItemMeta().setDisplayName(ITEM_PAY_REMOVE_1);
		
		ItemStack payRemoveItem2 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
		payAddItem1.getItemMeta().setDisplayName(ITEM_PAY_REMOVE_2);
		
		ItemStack payRemoveItem3 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
		payAddItem1.getItemMeta().setDisplayName(ITEM_PAY_REMOVE_3);
			
		//FIRST
		walletInventory.setItem(0, borderItem);
		walletInventory.setItem(1, borderItem);
		walletInventory.setItem(2, borderItem);
		walletInventory.setItem(3, borderItem);
		if (StringHelper.isNotNullOrEmpty(playerName))
		{
			ItemStack targetItem = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
			targetItem.getItemMeta().setDisplayName("§7Ziel: §9" + playerName);
			walletInventory.setItem(4, targetItem);
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
		walletInventory.setItem(10, payAddItem1);
		walletInventory.setItem(11, payAddItem2);
		walletInventory.setItem(12, payAddItem3);
		walletInventory.setItem(13, valueItem);
		walletInventory.setItem(14, payRemoveItem1);
		walletInventory.setItem(15, payRemoveItem2);
		walletInventory.setItem(16, payRemoveItem3);
		walletInventory.setItem(17, borderItem);
		//THIRD
		walletInventory.setItem(18, performPayItem);
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
			String valueAmountName = targetItem.getItemMeta().getDisplayName();
			try
			{
				double valueAmount = Double.parseDouble(valueAmountName.substring(2, valueAmountName.length()-1));
				return valueAmount;
			}
			catch (NumberFormatException exception)
			{
				Bukkit.getLogger().log(Level.WARNING, "Der pay-value-amount ist ungültig.", exception);
			}
		}
		return 0;
	} 
	
	public static void setPayValueAmount(@NotNull Inventory payInventory, @NotNull String valueName, @NotNull double updatedValue)
	{
		Validate.notNull((Object) payInventory, "The pay-inventory cannot be null.");
		
		payInventory.getItem(13).getItemMeta().setDisplayName(getValueAmountName(valueName, updatedValue));;
	}
}