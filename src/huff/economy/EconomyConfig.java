package huff.economy;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import huff.lib.helper.FileHelper;
import huff.lib.interfaces.DatabaseProperties;

public class EconomyConfig implements DatabaseProperties
{
	private static final String CFG_ROOT = "huffeconomy.";
	private static final String CFG_DATABASE = CFG_ROOT + "database.";
	
	private static final String CFG_HOST = CFG_DATABASE + "host";
	private static final String CFG_PORT = CFG_DATABASE + "port";
	private static final String CFG_DATABASENAME = CFG_DATABASE + "databasename";
	private static final String CFG_USERNAME = CFG_DATABASE + "username";
	private static final String CFG_PASSWORD = CFG_DATABASE + "password";
	
	private static final String CFG_VALUENAME = CFG_ROOT + "value_name";
	private static final String CFG_VALUEMATERIAL = CFG_ROOT + "value_material";
	private static final String CFG_STARTBALANCE = CFG_ROOT + "start_balance";
	private static final String CFG_TRANSACTION_FEEDBACK = CFG_ROOT + "transaction_feedback";
	
	public EconomyConfig(@NotNull String pluginFolderPath)
	{
		Validate.notNull((Object) pluginFolderPath, "The plugin-folder-path cannot be null.");
		
		this.config = FileHelper.loadYamlConfigurationFromFile(Paths.get(pluginFolderPath, "Economy", "config.yml").toString(), "Hufferlinge Economy Config File", createDefaults());
		
		loadValues();
	}
	
	private final YamlConfiguration config;
	
	private String host;
	private String port;
	private String databasename;
	private String username;
	private String password;
	
	private String valueName;
	private Material valueMaterial;
	private double startBalance;
	private boolean transactionFeedback;

	public void loadValues()
	{
		host = (String) FileHelper.readConfigValue(config, CFG_HOST);
		port = (String) FileHelper.readConfigValue(config, CFG_PORT);
		databasename = (String) FileHelper.readConfigValue(config, CFG_DATABASENAME);
		username = (String) FileHelper.readConfigValue(config, CFG_USERNAME);
		password = (String) FileHelper.readConfigValue(config, CFG_PASSWORD);
		valueName = (String) FileHelper.readConfigValue(config, CFG_VALUENAME);
		valueMaterial = Material.getMaterial((String) FileHelper.readConfigValue(config, CFG_VALUEMATERIAL));
		startBalance = (double) FileHelper.readConfigValue(config, CFG_STARTBALANCE);
		transactionFeedback = (boolean) FileHelper.readConfigValue(config, CFG_TRANSACTION_FEEDBACK);
	}
	
	@Override
	public @NotNull String getHost()
	{
		return host;
	}

	@Override
	public @NotNull String getPort()
	{
		return port;
	}

	@Override
	public @NotNull String getDatabasename()
	{
		return databasename;
	}
	
	@Override
	public @NotNull String getUsername()
	{
		return username;
	}
	
	@Override
	public @NotNull String getPassword()
	{
		return password;
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
		
		defaults.put(CFG_HOST, "localhost");
		defaults.put(CFG_PORT, "3306");
		defaults.put(CFG_DATABASENAME, "huffeconomy");
		defaults.put(CFG_USERNAME, "huffuser");
		defaults.put(CFG_PASSWORD, "0000");
		defaults.put(CFG_VALUENAME, "Goldlinge");
		defaults.put(CFG_VALUEMATERIAL, Material.GOLD_NUGGET.toString());
		defaults.put(CFG_STARTBALANCE, 10);
		defaults.put(CFG_TRANSACTION_FEEDBACK, true);
		
		return defaults;
	}
}
