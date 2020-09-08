package huff.economy;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import huff.lib.helper.FileHelper;

public class EconomyConfig
{
	private static final String CFG_ROOT = "huffeconomy.";
	
	private static final String CFG_VALUENAME = CFG_ROOT + "value_name";
	private static final String CFG_VALUEMATERIAL = CFG_ROOT + "value_material";
	private static final String CFG_WALLETNAME = CFG_ROOT + "wallet_name";
	private static final String CFG_WALLETMATERIAL = CFG_ROOT + "wallet_material";
	private static final String CFG_STARTBALANCE = CFG_ROOT + "start_balance";
	private static final String CFG_TRANSACTION_FEEDBACK = CFG_ROOT + "transaction_feedback";
	
	public EconomyConfig(@NotNull String pluginFolderPath)
	{
		Validate.notNull((Object) pluginFolderPath, "The plugin-folder-path cannot be null.");
		
		this.config = FileHelper.loadYamlConfigurationFromFile(Paths.get(pluginFolderPath, "Economy", "config.yml").toString(), "Hufferlinge Economy Config File", createDefaults());
		
		loadValues();
	}
	
	private final YamlConfiguration config;
	
	private String valueName;
	private Material valueMaterial;
	private String walletName;
	private Material walletMaterial;
	private double startBalance;
	private boolean transactionFeedback;

	public void loadValues()
	{
		valueName = (String) FileHelper.readConfigValue(config, CFG_VALUENAME);
		valueMaterial = Material.getMaterial((String) FileHelper.readConfigValue(config, CFG_VALUEMATERIAL));
		walletName = (String) FileHelper.readConfigValue(config, CFG_WALLETNAME);
		walletMaterial = Material.getMaterial((String) FileHelper.readConfigValue(config, CFG_WALLETMATERIAL));
		startBalance = (int) FileHelper.readConfigValue(config, CFG_STARTBALANCE);
		transactionFeedback = (boolean) FileHelper.readConfigValue(config, CFG_TRANSACTION_FEEDBACK);
	}
	
	public @NotNull String getValueName()
	{
		return valueName;
	}
	
	public @NotNull String getValueFormatted(double value)
	{
		return String.format("%.0f %s", value, getValueName());
	}
	
	public @NotNull Material getValueMaterial()
	{
		return valueMaterial;
	}
	
	public @NotNull String getWalletName()
	{
		return walletName;
	}
	
	public @NotNull Material getWalletMaterial()
	{
		return walletMaterial;
	}
	
	public @NotNull double getStartBalance()
	{
		return startBalance;
	}
	
	public @NotNull boolean hasTransactionFeedback()
	{
		return transactionFeedback;
	} 
	
	private Map<String, Object> createDefaults()
	{
		Map<String, Object> defaults = new HashMap<>();
		
		defaults.put(CFG_VALUENAME, "Goldlinge");
		defaults.put(CFG_VALUEMATERIAL, Material.GOLD_NUGGET.toString());
		defaults.put(CFG_WALLETNAME, "Geldbeutel");
		defaults.put(CFG_WALLETMATERIAL, Material.BROWN_SHULKER_BOX.toString());
		defaults.put(CFG_STARTBALANCE, 10);
		defaults.put(CFG_TRANSACTION_FEEDBACK, true);
		
		return defaults;
	}
}
