package huff.economy;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import huff.lib.helper.InventoryHelper;
import huff.lib.helper.ItemHelper;
import huff.lib.helper.MessageHelper;

public class EconomyUtil
{	
	private EconomyUtil() { }
	
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
		walletInventory.setItem(10, ItemHelper.getItemWithMeta(economyConfig.getValueMaterial(), 
				                                                    MessageHelper.getHighlighted(economyConfig.getValueFormatted(currentWallet), false , false)));
		walletInventory.setItem(11, borderItem);
		walletInventory.setItem(12, fillItem);
		walletInventory.setItem(13, fillItem);
		walletInventory.setItem(14, fillItem);
		walletInventory.setItem(15, borderItem);
		walletInventory.setItem(16, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
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
		walletInventory.setItem(10, ItemHelper.getItemWithMeta(economyConfig.getValueMaterial(), 
				                                                    MessageHelper.getHighlighted(economyConfig.getValueFormatted(currentBalance), false, false)));
		walletInventory.setItem(11, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
				                                                    economyConfig.getTransactionInventoryName(TransactionKind.WALLET_OUT)));
		walletInventory.setItem(12, borderItem);
		walletInventory.setItem(13, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
				                                                    economyConfig.getTransactionInventoryName(TransactionKind.BANK_OTHER)));
		walletInventory.setItem(14, borderItem);
		walletInventory.setItem(15, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
				                                                    economyConfig.getTransactionInventoryName(TransactionKind.BANK_OUT)));
		walletInventory.setItem(16, ItemHelper.getItemWithMeta(economyConfig.getWalletMaterial(),
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
			walletInventory.setItem(26, ItemHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, economyConfig.getBankRemoveName()));
		}
		else
		{
			walletInventory.setItem(26, borderItem);
		}
		
		
		return walletInventory;
	}
}
