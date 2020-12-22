package huff.economy;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import huff.economy.listener.EconomyListener;
import huff.economy.listener.InventoryListener;
import huff.economy.listener.JoinListener;
import huff.economy.storage.Bank;
import huff.economy.storage.Signature;
import huff.economy.storage.Storage;
import huff.lib.helper.CommandHelper;
import huff.lib.listener.MenuInventoryListener;
import huff.lib.manager.RedisManager;
import huff.lib.manager.delayedmessage.DelayedMessagesManager;

public class EconomyModule
{
	public EconomyModule(@NotNull JavaPlugin plugin, @NotNull RedisManager redisManager, @NotNull DelayedMessagesManager delayedMessageManager)
	{
		Validate.notNull((Object) plugin, "The plugin-instance cannot be null.");
		Validate.notNull((Object) redisManager, "The redis-manager cannot be null.");
		
		this.plugin = plugin;	
		this.economyInterface = new EconomyInterface(plugin,
				                                     new Config(plugin.getDataFolder().getAbsolutePath()), 
				                                     new Storage(redisManager), 
				                                     new Signature(redisManager), 
				                                     new Bank(redisManager),
				                                     delayedMessageManager);
	}
	
	private final JavaPlugin plugin;	
	private final EconomyInterface economyInterface;
	
	public void registerCommands()
	{
		EconomyCommand economyCommand = new EconomyCommand(economyInterface);
		PluginCommand pluginEconomyCommand = plugin.getCommand("huffeconomy");
		
		pluginEconomyCommand.setExecutor(economyCommand);
		pluginEconomyCommand.setTabCompleter(economyCommand);
		pluginEconomyCommand.setDescription("Hufferlinge Economy Command");
		pluginEconomyCommand.setPermission(EconomyCommand.PERM_ECONOMY);
		CommandHelper.addAliases(pluginEconomyCommand, "huffconomy", "economy", "money");
	} 
	
	public void registerListener()
	{
		PluginManager pluginManager = Bukkit.getPluginManager();
		
		pluginManager.registerEvents(new JoinListener(economyInterface), plugin);
		pluginManager.registerEvents(new EconomyListener(economyInterface), plugin);
		pluginManager.registerEvents(new InventoryListener(economyInterface), plugin);
		pluginManager.registerEvents(new MenuInventoryListener(plugin), plugin);
	}
	
	public void handleBankSpawning(long worldTime)
	{		
		if (worldTime % 1000 == 0)
		{
			for (Location bankLocation : economyInterface.getBank().getBankLocations())
			{
				economyInterface.trySpawnBankEntity(bankLocation);
			}
		}
	}
}
