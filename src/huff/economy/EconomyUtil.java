package huff.economy;

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

import huff.lib.helper.InventoryHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.helper.StringHelper;

public class EconomyUtil
{
	public static final int AMOUNT_1 = 1;
	public static final int AMOUNT_2 = 5;
	public static final int AMOUNT_3 = 10;
	public static final int AMOUNT_4 = 100;
	public static final int AMOUNT_5 = 1000;
	
	private EconomyUtil() { }
	
	// I T E M
	
	public static @NotNull String getAmountItemName(int amount, boolean negativeValue)
	{
		return negativeValue ? "§c- " + amount : "§a+ " + amount;
	}
	
	public static @NotNull String getPerformItemName(TransactionKind transactionKind)
	{		
		return StringHelper.build("§7» §a", transactionKind.getLabel());
	}
	
	// I N V E N T O R Y
	
	public static @NotNull Inventory getWalletInventory(@NotNull EconomyConfig economyConfig, double currentWallet)
	{
		Validate.notNull((Object) economyConfig, "The economy-config cannot be null.");
	
		final Inventory walletInventory = Bukkit.createInventory(null, 27, economyConfig.getWalletInventoryName());
		final ItemStack borderItem = InventoryHelper.getBorderItem();
		final ItemStack fillItem = InventoryHelper.getFillItem();
		
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
		walletInventory.setItem(10, InventoryHelper.getItemWithMeta(economyConfig.getValueMaterial(), 
				                                                    MessageHelper.getHighlighted(economyConfig.getValueFormatted(currentWallet), false , false)));
		walletInventory.setItem(11, borderItem);
		walletInventory.setItem(12, fillItem);
		walletInventory.setItem(13, fillItem);
		walletInventory.setItem(14, fillItem);
		walletInventory.setItem(15, borderItem);
		walletInventory.setItem(16, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
				                                                    economyConfig.getTransactionInventoryName(TransactionKind.WALLET_OUT)));
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
	
	public static @NotNull Inventory getBankInventory(@NotNull EconomyConfig economyConfig, double currentBalance, double currentWallet, boolean withRemove)
	{
		Validate.notNull((Object) economyConfig, "The economy-config cannot be null.");
	
		final Inventory walletInventory = Bukkit.createInventory(null, 36, economyConfig.getBankInventoryName());
		final ItemStack borderItem = InventoryHelper.getBorderItem();
		
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
		walletInventory.setItem(10, InventoryHelper.getItemWithMeta(economyConfig.getValueMaterial(), 
				                                                    MessageHelper.getHighlighted(economyConfig.getValueFormatted(currentBalance), false, false)));
		walletInventory.setItem(11, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
				                                                    economyConfig.getTransactionInventoryName(TransactionKind.WALLET_OUT)));
		walletInventory.setItem(12, borderItem);
		walletInventory.setItem(13, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
				                                                    economyConfig.getTransactionInventoryName(TransactionKind.BANK_OTHER)));
		walletInventory.setItem(14, borderItem);
		walletInventory.setItem(15, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
				                                                    economyConfig.getTransactionInventoryName(TransactionKind.BANK_OUT)));
		walletInventory.setItem(16, InventoryHelper.getItemWithMeta(economyConfig.getWalletMaterial(),
				                                                    MessageHelper.getHighlighted(economyConfig.getValueFormatted(currentWallet), false, false)));
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
		if (withRemove)
		{
			walletInventory.setItem(26, InventoryHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, economyConfig.getBankRemoveName()));
		}
		else
		{
			walletInventory.setItem(26, borderItem);
		}
		
		
		return walletInventory;
	}
	
	public static @NotNull Inventory getTransactionInventory(@NotNull EconomyConfig economyConfig, TransactionKind transactionKind, double currentValue, @Nullable String playerName)
	{
		Validate.notNull((Object) economyConfig, "The economy-config cannot be null.");
	
		final Inventory walletInventory = Bukkit.createInventory(null, 36, economyConfig.getTransactionInventoryName(transactionKind));
		final ItemStack borderItem = InventoryHelper.getBorderItem();
			
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
		walletInventory.setItem(10, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_5, false)));
		walletInventory.setItem(11, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_4, false)));
		walletInventory.setItem(12, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_3, false)));
		walletInventory.setItem(13, InventoryHelper.getItemWithMeta(economyConfig.getValueMaterial(), MessageHelper.getHighlighted(economyConfig.getValueFormatted(0), false, false), 
				                                                    economyConfig.getCurrentValueLore(transactionKind, currentValue)));
		walletInventory.setItem(14, InventoryHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_3, true)));
		walletInventory.setItem(15, InventoryHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_4, true)));
		walletInventory.setItem(16, InventoryHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_5, true)));
		walletInventory.setItem(17, borderItem);
		//THIRD
		walletInventory.setItem(18, borderItem);
		walletInventory.setItem(19, borderItem);
		walletInventory.setItem(20, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_2, false)));
		walletInventory.setItem(21, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_1, false)));
		if (StringHelper.isNotNullOrEmpty(playerName))
		{
			final ItemStack targetSkull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3); //TODO In Lib auslagern
			final SkullMeta targetSkullMeta = (SkullMeta) targetSkull.getItemMeta();
			targetSkullMeta.setOwner(playerName);
			targetSkullMeta.setDisplayName("§7Empfänger: §9" + playerName);
			targetSkull.setItemMeta(targetSkullMeta);
			
			walletInventory.setItem(22, targetSkull);
		}
		else
		{
			walletInventory.setItem(22, borderItem);
		}
		walletInventory.setItem(23, InventoryHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_1, true)));
		walletInventory.setItem(24, InventoryHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_2, true)));
		walletInventory.setItem(25, borderItem);
		walletInventory.setItem(26, borderItem);
		//FOURTH
		walletInventory.setItem(27, InventoryHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getPerformItemName(transactionKind)));
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
	
	// I N V E N T O R Y - A C T I O N S
	
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
		
		if (matcher.find())
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
				
				if (matcher.find())
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
	
	public static void setPayValueAmount(@NotNull Inventory payInventory, @NotNull String formattedValue)
	{
		Validate.notNull((Object) payInventory, "The pay-inventory cannot be null.");
		Validate.notNull((Object) payInventory, "The formatted value cannot be null.");
		
		final ItemStack valueAmountItem = payInventory.getItem(13);
		final ItemMeta valueAmountMeta = valueAmountItem.getItemMeta();
		
		valueAmountMeta.setDisplayName(MessageHelper.getHighlighted(formattedValue));
		valueAmountItem.setItemMeta(valueAmountMeta);	
	}
}
