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
	public static final int PAY_1 = 1;
	public static final int PAY_2 = 5;
	public static final int PAY_3 = 10;
	public static final int PAY_4 = 100;
	public static final int PAY_5 = 1000;
	
	public static final String ITEM_PERFORMPAY = "§7» §aHerausnehmen";
	
	private WalletUtil() { }
	
	public static @NotNull String getPayItemName(int payValue, boolean negativeValue)
	{
		return negativeValue ? "§c- " + payValue : "§a+ " + payValue;
	}
	
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
	
		final Inventory walletInventory = Bukkit.createInventory(null, 27, getWalletInventoryName(economyConfig.getWalletName()));
		final ItemStack borderItem = InventoryHelper.getBorderItem();
		final ItemStack fillItem = InventoryHelper.getFillItem();
		final String valueName = economyConfig.getValueName();
		
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
	
		final Inventory walletInventory = Bukkit.createInventory(null, 36, getPayInventoryName(economyConfig.getValueName()));
		final ItemStack borderItem = InventoryHelper.getBorderItem();	
		final String valueName = economyConfig.getValueName();
			
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
		walletInventory.setItem(10, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getPayItemName(PAY_5, false)));
		walletInventory.setItem(11, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getPayItemName(PAY_4, false)));
		walletInventory.setItem(12, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getPayItemName(PAY_3, false)));
		walletInventory.setItem(13, InventoryHelper.getItemWithMeta(economyConfig.getValueMaterial(), getValueAmountName(valueName, 0)));
		walletInventory.setItem(14, InventoryHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, getPayItemName(PAY_3, true)));
		walletInventory.setItem(15, InventoryHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, getPayItemName(PAY_4, true)));
		walletInventory.setItem(16, InventoryHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, getPayItemName(PAY_5, true)));
		walletInventory.setItem(17, borderItem);
		//THIRD
		walletInventory.setItem(18, borderItem);
		walletInventory.setItem(19, borderItem);
		walletInventory.setItem(20, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getPayItemName(PAY_2, false)));
		walletInventory.setItem(21, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getPayItemName(PAY_1, false)));
		if (StringHelper.isNotNullOrEmpty(playerName))
		{
			walletInventory.setItem(22, InventoryHelper.getItemWithMeta(Material.BLUE_STAINED_GLASS_PANE, "§7Empfänger: §9" + playerName));
		}
		else
		{
			walletInventory.setItem(22, borderItem);
		}
		walletInventory.setItem(23, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getPayItemName(PAY_1, true)));
		walletInventory.setItem(24, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getPayItemName(PAY_2, true)));
		walletInventory.setItem(25, borderItem);
		walletInventory.setItem(26, borderItem);
		//FOURTH
		walletInventory.setItem(27, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, ITEM_PERFORMPAY));
		walletInventory.setItem(28, borderItem);
		walletInventory.setItem(29, borderItem);
		walletInventory.setItem(30, borderItem);
		walletInventory.setItem(31, borderItem);
		walletInventory.setItem(32, borderItem);
		walletInventory.setItem(33, borderItem);
		walletInventory.setItem(34, borderItem);
		walletInventory.setItem(35, InventoryHelper.getAbortItem());		
		
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
		
		final ItemStack targetItem = payInventory.getItem(22);
		
		if (targetItem == null)
		{
			return null;
		}	
		final Pattern valuePattern = Pattern.compile("§.*: §.(.*)");
		final Matcher matcher = valuePattern.matcher(targetItem.getItemMeta().getDisplayName());
		
		while (matcher.find())
		{
			return Bukkit.getPlayer(matcher.group(1));
		}
		return null;
	}
	
	public static @Nullable double getPayValueAmount(@NotNull Inventory payInventory)
	{
		Validate.notNull((Object) payInventory, "The pay-inventory cannot be null.");
		
		final ItemStack targetItem = payInventory.getItem(13);
		
		if (targetItem != null)
		{
			try
			{
				final Pattern valuePattern = Pattern.compile("§.([0-9.]*).*");
				final Matcher matcher = valuePattern.matcher(targetItem.getItemMeta().getDisplayName());
				
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
		
		final ItemStack valueAmountItem = payInventory.getItem(13);
		final ItemMeta valueAmountMeta = valueAmountItem.getItemMeta();
		
		valueAmountMeta.setDisplayName(getValueAmountName(valueName, updatedValue));
		valueAmountItem.setItemMeta(valueAmountMeta);	
	}
}