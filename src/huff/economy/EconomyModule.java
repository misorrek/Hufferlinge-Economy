package huff.economy;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import huff.economy.listener.EconomyListener;
import huff.economy.listener.InventoryListener;
import huff.economy.listener.JoinListener;
import huff.economy.storage.Bank;
import huff.economy.storage.Signature;
import huff.economy.storage.Storage;
import huff.lib.manager.RedisManager;
import huff.lib.manager.delaymessage.DelayMessageManager;

public class EconomyModule
{
	public EconomyModule(@NotNull JavaPlugin plugin, @NotNull RedisManager redisManager, @NotNull DelayMessageManager delayMessageManager)
	{
		Validate.notNull((Object) plugin, "The plugin-instance cannot be null.");
		Validate.notNull((Object) redisManager, "The redis-manager cannot be null.");
		
		this.plugin = plugin;	
		this.economy = new EconomyInterface(plugin,
				                                     new Config(plugin.getDataFolder().getAbsolutePath()), 
				                                     new Storage(redisManager), 
				                                     new Signature(redisManager), 
				                                     new Bank(redisManager),
				                                     delayMessageManager);
	}
	
	private final JavaPlugin plugin;	
	private final EconomyInterface economy;
	
	public void registerCommands()
	{
		new EconomyCommand(economy);
	} 
	
	public void registerListener()
	{
		PluginManager pluginManager = Bukkit.getPluginManager();
		
		pluginManager.registerEvents(new JoinListener(economy), plugin);
		pluginManager.registerEvents(new EconomyListener(economy), plugin);
		pluginManager.registerEvents(new InventoryListener(economy), plugin);
	}
	
	public void handleBankSpawning(long worldTime)
	{		
		if (worldTime % 1000 == 0)
		{
			for (Location bankLocation : economy.getBank().getBankLocations())
			{
				economy.trySpawnBankEntity(bankLocation);
			}
		}
	}
}
