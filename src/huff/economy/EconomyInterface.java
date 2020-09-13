package huff.economy;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import huff.economy.listener.JoinListener;
import huff.economy.listener.WalletListener;
import huff.economy.storage.EconomyBank;
import huff.economy.storage.EconomySignature;
import huff.economy.storage.EconomyStorage;
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
		this.economyBank = new EconomyBank(redisManager);
		this.economyConfig = new EconomyConfig(plugin.getDataFolder().getAbsolutePath());
	}
	
	private final JavaPlugin plugin;
	private final EconomyStorage economyStorage;
	private final EconomySignature economySignature;
	private final EconomyBank economyBank;
	private final EconomyConfig economyConfig;
	
	private boolean bankOpen = false;
	
	public void registerCommands()
	{
		EconomyCommand economyCommand = new EconomyCommand(economyStorage, economyBank, economyConfig);
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
	
	public void handleBankSpawning(long worldTime)
	{	
		if (worldTime >= economyConfig.getBankOpen() && worldTime < economyConfig.getBankClose())
		{
			if (!bankOpen)
			{
				spawnBank();
				bankOpen = true;	
			}			
		}
		else
		{
			if (bankOpen)
			{
				removeBank();
				bankOpen = false;
			}		
		}
	}
	
	public void spawnBank()
	{
		for (Location bankLocation : economyBank.getBankLocations())
		{
			final Villager bankEntity = (Villager) bankLocation.getWorld().spawnEntity(bankLocation, EntityType.VILLAGER);
			
			bankEntity.setAI(false);
			bankEntity.setInvulnerable(true);
			bankEntity.setCollidable(false);
			bankEntity.setVillagerType(Type.DESERT);
			bankEntity.setCustomName(economyConfig.getBankEntityName());
		}
	}
	
	public void removeBank()
	{
		for (Location bankLocation : economyBank.getBankLocations())
		{
			for (Entity entity : bankLocation.getWorld().getNearbyEntities(bankLocation, 5, 5, 5))
			{
				if (entity instanceof Villager && entity.getCustomName().equals(economyConfig.getBankEntityName()))
				{
					entity.remove();
				}
			}
		}
	}
}
