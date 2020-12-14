package huff.economy;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.economy.menuholders.TransactionKind;
import huff.lib.helper.FileHelper;
import huff.lib.helper.ItemHelper;
import huff.lib.helper.StringHelper;

public class Config
{
	private static final String CFG_ROOT = "huffeconomy.";
	private static final String CFG_VALUE = CFG_ROOT + "value.";
	private static final String CFG_WALLET = CFG_ROOT + "wallet.";
	private static final String CFG_BANK = CFG_ROOT + "bank.";
	private static final String CFG_TRADE = CFG_ROOT + "trade.";
	
	private static final String CFG_STARTBALANCE = CFG_ROOT + "start_balance";
	private static final String CFG_TRANSACTION_FEEDBACK = CFG_ROOT + "transaction_feedback";
	private static final String CFG_VALUE_NAME = CFG_VALUE + "name";
	private static final String CFG_VALUE_MATERIAL = CFG_VALUE + "material";
	private static final String CFG_WALLET_NAME = CFG_WALLET + "name";
	private static final String CFG_WALLET_MATERIAL = CFG_WALLET + "material";
	private static final String CFG_WALLET_DEFAULTSLOT = CFG_WALLET + "default_slot";
	private static final String CFG_BANK_NAME = CFG_BANK + "name";
	private static final String CFG_BANK_MATERIAL = CFG_BANK + "material";
	private static final String CFG_BANK_SPAWN_MATERIAL = CFG_BANK + "spawn_material";
	private static final String CFG_BANK_OPEN = CFG_BANK + "open";
	private static final String CFG_BANK_CLOSE = CFG_BANK + "close";
	private static final String CFG_TRADE_NAME= CFG_TRADE + "name";
	private static final String CFG_TRADE_MATERIAL= CFG_TRADE + "material";
	
	public Config(@NotNull String pluginFolderPath)
	{
		Validate.notNull((Object) pluginFolderPath, "The plugin-folder-path cannot be null.");
		
		this.configuration = FileHelper.loadYamlConfigurationFromFile(Paths.get(pluginFolderPath, "Economy", "config.yml").toString(), "Hufferlinge Economy Config File", createDefaults());
	
		loadValues();
	}
	
	private final YamlConfiguration configuration;
	
	private double startBalance;
	private boolean transactionFeedback;
	private String valueName;
	private Material valueMaterial; 
	private String walletName;
	private Material walletMaterial;
	private int walletDefaultSlot;
	private String bankName;
	private Material bankMaterial;
	private Material bankSpawnMaterial;
	private int bankOpen;
	private int bankClose;
	private String tradeName;
	private Material tradeMaterial;

	public void loadValues()
	{
		startBalance = (int) FileHelper.readConfigValue(configuration, CFG_STARTBALANCE);
		transactionFeedback = (boolean) FileHelper.readConfigValue(configuration, CFG_TRANSACTION_FEEDBACK);
		valueName = (String) FileHelper.readConfigValue(configuration, CFG_VALUE_NAME);
		valueMaterial = Material.getMaterial((String) FileHelper.readConfigValue(configuration, CFG_VALUE_MATERIAL));
		walletName = (String) FileHelper.readConfigValue(configuration, CFG_WALLET_NAME);
		walletMaterial = Material.getMaterial((String) FileHelper.readConfigValue(configuration, CFG_WALLET_MATERIAL));
		walletDefaultSlot = (int) FileHelper.readConfigValue(configuration, CFG_WALLET_DEFAULTSLOT);
		bankName = (String) FileHelper.readConfigValue(configuration, CFG_BANK_NAME);
		bankMaterial = Material.getMaterial((String) FileHelper.readConfigValue(configuration, CFG_BANK_MATERIAL));
		bankSpawnMaterial = Material.getMaterial((String) FileHelper.readConfigValue(configuration, CFG_BANK_SPAWN_MATERIAL));
		bankOpen = (int) FileHelper.readConfigValue(configuration, CFG_BANK_OPEN);
		bankClose = (int) FileHelper.readConfigValue(configuration, CFG_BANK_CLOSE);
		tradeName = (String) FileHelper.readConfigValue(configuration, CFG_TRADE_NAME);
		tradeMaterial = Material.getMaterial((String) FileHelper.readConfigValue(configuration, CFG_TRADE_MATERIAL));
	}
	
	public @NotNull double getStartBalance()
	{
		return startBalance;
	}
	
	public boolean hasTransactionFeedback()
	{
		return transactionFeedback;
	} 
	
	// V A L U E
	
	public @NotNull String getValueName()
	{
		return valueName;
	}	
	
	public @NotNull String getValueFormatted(double value)
	{
		return String.format("%.0f %s", value, getValueName());
	}
	
	public @NotNull String getValueItemName()
	{
		return "§e§l" + getValueName();
	}	
	
	public @NotNull Material getValueMaterial()
	{
		return valueMaterial;
	}
	
	public @NotNull ItemStack getValueItem()
	{		
		return ItemHelper.getItemWithMeta(getValueMaterial(), getValueItemName());
	}
	
	public boolean equalsValueItem(@Nullable ItemStack item)
	{
		return item != null && item.getType().equals(getValueMaterial()) && item.getItemMeta().getDisplayName().equals(getValueItemName());
	}
	
	public @NotNull List<String> getCurrentValueLore(TransactionKind transactionKind, double currentValue)
	{
		Validate.notNull((Object) valueName, "The value-name cannot be null.");
		Validate.notNull((Object) transactionKind, "The transaction-kind cannot be null.");
		
		List<String> valueLore = new ArrayList<>();
		
		valueLore.add(String.format("§7%s: %.0f %s", 
				                    transactionKind.isBankTransaction() ? getBankName() : getWalletName(),
				                    currentValue, getValueName()));
		
		return valueLore;
	}
	
	// W A L L E T 
	
	public @NotNull String getWalletName()
	{
		return walletName;
	}
	
	public @NotNull String getWalletItemName()
	{
		return "§6§l" + getWalletName();
	}
	
	public @NotNull String getWalletInventoryName()
	{		
		return "§7» §6" + getWalletName();
	}
	
	public @NotNull Material getWalletMaterial()
	{
		return walletMaterial;
	}
	
	public int getWalletDefaultSlot() 
	{
		return walletDefaultSlot;
	}
	
	public @NotNull ItemStack getWalletItem()
	{		
		return ItemHelper.getItemWithMeta(getWalletMaterial(), getWalletItemName());
	}
	
	public boolean equalsWalletItem(@Nullable ItemStack item)
	{
		return item != null && item.getType().equals(getWalletMaterial()) && item.getItemMeta().getDisplayName().equals(getWalletItemName());
	}
	
	// B A N K
	
	public @NotNull String getBankName()
	{
		return bankName;
	}
	
	public @NotNull String getBankItemName()
	{
		return "§e§l" + getBankName();
	}
	
	public @NotNull String getBankEntityName()
	{
		return "§e" + getBankName();
	}
	
	public @NotNull String getBankInventoryName()
	{		
		return "§7» §e" + getBankName();
	}
	  
	public @NotNull String getBankRemoveName()
	{
		return "§7» §c" + getBankName() + " entfernen";
	}
	
	public @NotNull Material getBankMaterial()
	{
		return bankMaterial;
	}
	
	public @NotNull Material getBankSpawnMaterial()
	{
		return bankSpawnMaterial;
	}
	
	public int getBankOpen()
	{
		return bankOpen;
	}
	
	public int getBankClose()
	{
		return bankClose;
	}
	
	public @NotNull ItemStack getBankSpawnItem()
	{		
		return ItemHelper.getItemWithMeta(getBankSpawnMaterial(), getBankItemName());
	}
	
	public boolean equalsBankSpawnItem(@Nullable ItemStack item)
	{
		return item != null && item.getType().equals(getBankSpawnMaterial()) && item.getItemMeta().getDisplayName().equals(getBankItemName());
	}
	
	// T R A D E
	
	public String getTradeName()
	{
		return tradeName;
	}
	
	public @NotNull String getTradeInventoryName()
	{
		return "§7» §e" + getTradeName();
	}
	
	public Material getTradeMaterial()
	{
		return tradeMaterial;
	}
	
	public boolean equalsTradeItem(@Nullable ItemStack item)
	{
		return item != null && item.getType().equals(getTradeMaterial()) && item.getItemMeta().getDisplayName().equals(getTradeInventoryName());
	}
	
	// O T H E R
	
	public String getTransactionInventoryName(TransactionKind transactionKind)
	{		
		return StringHelper.build("§7» §e", getValueName(), " ", transactionKind.getLowerLabel());
	}
	
	private Map<String, Object> createDefaults()
	{
		Map<String, Object> defaults = new HashMap<>();
		
		defaults.put(CFG_STARTBALANCE, 10);
		defaults.put(CFG_TRANSACTION_FEEDBACK, true);
		defaults.put(CFG_VALUE_NAME, "Goldlinge");
		defaults.put(CFG_VALUE_MATERIAL, Material.GOLD_NUGGET.toString());
		defaults.put(CFG_WALLET_NAME, "Geldbeutel");
		defaults.put(CFG_WALLET_MATERIAL, Material.BROWN_SHULKER_BOX.toString());
		defaults.put(CFG_WALLET_DEFAULTSLOT, 8);
		defaults.put(CFG_BANK_NAME, "Bänker");
		defaults.put(CFG_BANK_MATERIAL, Material.GOLD_BLOCK.toString());
		defaults.put(CFG_BANK_SPAWN_MATERIAL, Material.OCELOT_SPAWN_EGG.toString());
		defaults.put(CFG_BANK_OPEN, 1000);
		defaults.put(CFG_BANK_CLOSE, 13000);
		defaults.put(CFG_TRADE_NAME, "Handel");
		defaults.put(CFG_TRADE_MATERIAL, Material.BARREL.toString());
		
		return defaults;
	}
}
