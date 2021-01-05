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
	private static final int BANKCHECK_PERIOD = 500;
	
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
	private long lastWorldTime = 0;
	
	public void init()
	{
		registerCommands();
		registerListener();
	}
	
	public void handleBankSpawning(long worldTime)
	{		
		if ((lastWorldTime + BANKCHECK_PERIOD) <= worldTime || lastWorldTime > worldTime)
		{
			Bukkit.getConsoleSender().sendMessage("BANKCHECK : " + worldTime);
			
			for (Location bankLocation : economy.getBank().getBankLocations())
			{
				economy.trySpawnBankEntity(bankLocation);
			}
			lastWorldTime = worldTime;
		}
	}
	
	private void registerCommands()
	{
		new EconomyCommand(economy);
	} 
	
	private void registerListener()
	{
		PluginManager pluginManager = Bukkit.getPluginManager();
		
		pluginManager.registerEvents(new JoinListener(economy), plugin);
		pluginManager.registerEvents(new EconomyListener(economy), plugin);
		pluginManager.registerEvents(new InventoryListener(economy), plugin);
	}
}
