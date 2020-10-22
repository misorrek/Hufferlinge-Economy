package huff.economy;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import huff.economy.listener.JoinListener;
import huff.economy.listener.EconomyListener;
import huff.economy.storage.EconomyBank;
import huff.economy.storage.EconomySignature;
import huff.economy.storage.EconomyStorage;
import huff.lib.manager.RedisManager;
import huff.lib.manager.delayedmessage.DelayedMessageManager;

public class EconomyModule
{
	public EconomyModule(@NotNull JavaPlugin plugin, @NotNull RedisManager redisManager, @NotNull DelayedMessageManager delayedMessageManager)
	{
		Validate.notNull((Object) plugin, "The plugin-instance cannot be null.");
		Validate.notNull((Object) redisManager, "The redis-manager cannot be null.");
		Validate.notNull((Object) delayedMessageManager, "The delayed-message-manager cannot be null.");
		
		this.plugin = plugin;	
		this.economyInterface = new EconomyInterface(new EconomyConfig(plugin.getDataFolder().getAbsolutePath()), 
				                                     new EconomyStorage(redisManager), 
				                                     new EconomySignature(redisManager), 
				                                     new EconomyBank(redisManager),
				                                     delayedMessageManager);
	}
	
	private final JavaPlugin plugin;	
	private final EconomyInterface economyInterface;
	
	private boolean bankOpen = false;
	
	public void registerCommands()
	{
		EconomyCommand economyCommand = new EconomyCommand(economyInterface);
		PluginCommand pluginEconomyCommand = plugin.getCommand("huffeconomy");
		
		pluginEconomyCommand.setExecutor(economyCommand);
		pluginEconomyCommand.setTabCompleter(economyCommand);
		pluginEconomyCommand.setDescription("Hufferlinge Economy Command");
		addAliases(pluginEconomyCommand, "huffconomy", "economy", "money");
	}
	
	private void addAliases(Command command, String... aliases) //TODO Move to Lib
	{
		Map<String, Command> internalCommandMap = getInternalCommandMap();
		
		for (String alias : aliases)
		{
			map.put(alias.toLowerCase(), command);
		}
	}
	
	private @Nullable HashMap<String, Command> getInternalCommandMap() //TODO Move to Lib
	{
		try
		{
			Method getCommandMap = plugin.getServer().getClass().getMethod("getCommandMap");
			SimpleCommandMap commandMap = (SimpleCommandMap) getCommandMap.invoke(plugin.getServer());
			
			if (commandMap == null)
			{
				return null;
			}
			Field knownCommands = commandMap.getClass().getDeclaredField("knownCommands");
			knownCommands.setAccessible(true);
			
			return (HashMap<String, Command>) knownCommands.get(commandMap);
		}
		catch (Exception exception)
		{
			//TODO
			return null;
		}
	}
	
	public void registerListener()
	{
		PluginManager pluginManager = Bukkit.getPluginManager();
		
		pluginManager.registerEvents(new JoinListener(economyInterface), plugin);
		pluginManager.registerEvents(new EconomyListener(economyInterface), plugin);
		pluginManager.registerEvents(new InventoryListener(economyInterface), plugin);
	}
	
	public void handleBankSpawning(long worldTime)
	{	
		if (worldTime >= economyInterface.getConfig().getBankOpen() && worldTime < economyInterface.getConfig().getBankClose())
		{
			if (!bankOpen)
			{
				for (Location bankLocation : economyInterface.getBank().getBankLocations())
				{
					economyInterface.removeBankEntity(bankLocation);
				}	
				bankOpen = true;	
			}			
		}
		else
		{
			if (bankOpen)
			{
				for (Location bankLocation : economyInterface.getBank().getBankLocations())
				{
					economyInterface.removeBankEntity(bankLocation);
				}			
				bankOpen = false;
			}		
		}
	}
}
