package huff.economy;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import huff.economy.listener.JoinListener;
import huff.economy.listener.WalletListener;
import huff.lib.manager.DatabaseManager;

public class EconomyInterface
{
	public EconomyInterface(@NotNull JavaPlugin plugin, @NotNull DatabaseManager databaseManager)
	{
		Validate.notNull((Object) plugin, "The plugin-instance cannot be null.");
		Validate.notNull((Object) databaseManager, "The database-manager cannot be null.");
		
		this.plugin = plugin;
		this.economyTable = new EconomyTable(databaseManager);
		this.economyConfig = new EconomyConfig(plugin.getDataFolder().getAbsolutePath());
	}
	
	private final JavaPlugin plugin;
	private final EconomyTable economyTable;
	private final EconomyConfig economyConfig;
	
	public void registerCommands()
	{
		EconomyCommand economyCommand = new EconomyCommand(economyTable, economyConfig);
		PluginCommand pluginEconomyCommand = plugin.getCommand("huffeconomy");
		List<String> economyCommandAliases = new ArrayList<>();
		
		economyCommandAliases.add("huffconomy");
		economyCommandAliases.add("economy");
		economyCommandAliases.add("money");
		
		pluginEconomyCommand.setExecutor(economyCommand);
		pluginEconomyCommand.setTabCompleter(economyCommand);
		pluginEconomyCommand.setAliases(economyCommandAliases);
		pluginEconomyCommand.setDescription("Hufferlinge Economy Command");
	}
	
	public void registerListener()
	{
		PluginManager pluginManager = Bukkit.getPluginManager();
		
		pluginManager.registerEvents(new JoinListener(economyConfig, economyTable), plugin);
		pluginManager.registerEvents(new WalletListener(economyConfig, economyTable), plugin);
	}
}
