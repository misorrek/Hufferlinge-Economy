package huff.economy;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Type;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.economy.storage.Bank;
import huff.economy.storage.Signature;
import huff.economy.storage.Storage;
import huff.lib.manager.delayedmessage.DelayedMessagesManager;

public class EconomyInterface
{
	public EconomyInterface(@NotNull JavaPlugin plugin, @NotNull Config config, @NotNull Storage storage, @NotNull Signature signature, 
			                @NotNull Bank bank, @NotNull DelayedMessagesManager delayedMessageManager)
	{
		Validate.notNull((Object) plugin, "The plugin instance cannot be null.");
		Validate.notNull((Object) config, "The economy-config cannot be null.");
		Validate.notNull((Object) storage, "The economy-storage cannot be null.");
		Validate.notNull((Object) signature, "The economy-signature cannot be null");
		Validate.notNull((Object) bank, "The economy-bank cannot be null");
		Validate.notNull((Object) delayedMessageManager, "The delayed-message-manager cannot be null.");
		
		this.plugin = plugin;
		this.config = config;
		this.storage = storage;
		this.signature = signature;
		this.bank = bank;
		this.delayedMessageManager = delayedMessageManager;
	}
	
	private final JavaPlugin plugin;
	private final Config config;
	private final Storage storage;
	private final Signature signature;
	private final Bank bank;
	private final DelayedMessagesManager delayedMessageManager;
	
	public @NotNull JavaPlugin getPlugin()
	{
		return plugin;
	}
	
	public @NotNull Config getConfig()
	{
		return config;
	}
	
	public @NotNull Storage getStorage()
	{
		return storage;
	}
	
	public @NotNull Signature getSignature()
	{
		return signature;
	}
	
	public @NotNull Bank getBank()
	{
		return bank;
	}
	
	public @NotNull DelayedMessagesManager getDelayedMessageManager()
	{
		return delayedMessageManager;
	}
	
	public void trySpawnBankEntity(@NotNull Location location)
	{
		World world = location.getWorld();
		
		if (world == null)
		{
			return;
		}
		
		if (world.getTime() >= getConfig().getBankOpen() && world.getTime() < getConfig().getBankClose())
		{
			spawnBankEntity(location);
			removeClosedEntity(location);
		}
		else
		{
			spawnClosedEntity(location);
			removeBankEntity(location);
		}
	}
	
	public void tryRemoveBankEntity(@NotNull Location location)
	{
		removeBankEntity(location);
		removeClosedEntity(location);
	}
	
	private void spawnBankEntity(@Nullable Location location)
	{
		if (location == null || alreadyBankEntitySpawned(location))
		{
			return;
		}
		final Villager bankEntity = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
		
		bankEntity.setAI(false);
		bankEntity.setSilent(true);
		bankEntity.setInvulnerable(true);
		bankEntity.setCollidable(false);
		bankEntity.setVillagerType(Type.SAVANNA);
		//bankEntity.setProfession(Profession.CARTOGRAPHER);
		bankEntity.setCustomName(config.getBankEntityName());
	}
	
	private void spawnClosedEntity(@Nullable Location location)
	{
		if (location == null || alreadyClosedEntitySpawned(location))
		{
			return;
		}
		final ArmorStand closedEntity = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0, 0.5, 0), EntityType.ARMOR_STAND);
		final ArmorStand closedEntity2 = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0, 0.2, 0), EntityType.ARMOR_STAND);
		
		closedEntity.setSmall(true);
		closedEntity.setVisible(false);
		closedEntity.setGravity(false);
		closedEntity.setInvulnerable(true);
		closedEntity.setCollidable(false);
		closedEntity.setCustomNameVisible(true);
		closedEntity.setCustomName(config.getBankEntityName());
		
		closedEntity2.setSmall(true);
		closedEntity2.setVisible(false);
		closedEntity2.setGravity(false);
		closedEntity2.setInvulnerable(true);
		closedEntity2.setCollidable(false);
		closedEntity2.setCustomNameVisible(true);
		closedEntity2.setCustomName("§7- Geschlossen -");
	}
	
	private boolean alreadyBankEntitySpawned(@NotNull Location location)
	{
		Validate.notNull((Object) location, "The bank entity location cannot be null");
		
		for (Entity entity : location.getWorld().getNearbyEntities(location, 1.5, 1.5, 1.5))
		{
			if (entity instanceof Villager && entity.getCustomName().equals(config.getBankEntityName()))
			{
				return true;	
			}
		}
		return false;
	}
	
	private boolean alreadyClosedEntitySpawned(@NotNull Location location)
	{
		Validate.notNull((Object) location, "The bank entity location cannot be null");
		
		for (Entity entity : location.getWorld().getNearbyEntities(location.clone().add(0, 0.2, 0), 1.5, 1.5, 1.5))
		{
			if (entity instanceof ArmorStand && entity.getCustomName().equals("§7- Geschlossen -"))
			{
				return true;	
			}
		}
		return false;
	}
	
	private void removeBankEntity(@NotNull Location location)
	{
		Validate.notNull((Object) location, "The bank entity location cannot be null");
		
		for (Entity entity : location.getWorld().getNearbyEntities(location, 1.5, 1.5, 1.5))
		{
			if (entity instanceof Villager && entity.getCustomName().equals(config.getBankEntityName()))
			{
				entity.remove();		
			}
		}
	}
	
	private void removeClosedEntity(@NotNull Location location)
	{
		Validate.notNull((Object) location, "The bank closed location cannot be null");
		
		for (Entity entity : location.getWorld().getNearbyEntities(location.clone().add(0, 0.35, 0), 1.5, 1.5, 1.5))
		{
			if (entity instanceof ArmorStand && (entity.getCustomName().equals(config.getBankEntityName()) || 
					                             entity.getCustomName().equals("§7- Geschlossen -")))
			{
				entity.remove();		
			}
		}
	}
}
