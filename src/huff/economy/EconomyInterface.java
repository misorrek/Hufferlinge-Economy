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
import huff.lib.manager.RedisManager;

public class EconomyInterface
{
	public EconomyInterface(@NotNull JavaPlugin plugin, @NotNull RedisManager redisManager)
	{
		Validate.notNull((Object) plugin, "The plugin-instance cannot be null.");
		Validate.notNull((Object) redisManager, "The redis-manager cannot be null.");
		
		this.plugin = plugin;
		this.economyStorage = new EconomyStorage(redisManager);
		this.economySignature = new EconomySignature(redisManager);
		this.economyConfig = new EconomyConfig(plugin.getDataFolder().getAbsolutePath());
	}
	
	private final JavaPlugin plugin;
	private final EconomyStorage economyStorage;
	private final EconomySignature economySignature;
	private final EconomyConfig economyConfig;
	
	public void registerCommands()
	{
		EconomyCommand economyCommand = new EconomyCommand(economyStorage, economyConfig);
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
		
		pluginManager.registerEvents(new JoinListener(economyConfig, economyStorage), plugin);
		pluginManager.registerEvents(new WalletListener(economyConfig, economyStorage, economySignature), plugin);
	}
}
