package huff.economy;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Villager.Type;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.economy.storage.Bank;
import huff.economy.storage.Signature;
import huff.economy.storage.Storage;
import huff.lib.helper.EntityHelper;
import huff.lib.manager.delaymessage.DelayMessageManager;

public class EconomyInterface
{
	public static final String ENTITYKEY_BANK = "bank";
	public static final String ENTITYKEY_BANKCLOSED = "bank_closed";
	
	public EconomyInterface(@NotNull JavaPlugin plugin, @NotNull Storage storage, @NotNull Signature signature, 
			                @NotNull Bank bank, @NotNull DelayMessageManager delayMessageManager)
	{
		Validate.notNull((Object) plugin, "The plugin instance cannot be null.");
		Validate.notNull((Object) storage, "The economy storage cannot be null.");
		Validate.notNull((Object) signature, "The economy signature cannot be null");
		Validate.notNull((Object) bank, "The economy bank cannot be null");
		Validate.notNull((Object) delayMessageManager, "The delay message manager cannot be null.");
		
		this.plugin = plugin;
		this.storage = storage;
		this.signature = signature;
		this.bank = bank;
		this.delayMessageManager = delayMessageManager;
	}
	
	private final JavaPlugin plugin;
	private final Storage storage;
	private final Signature signature;
	private final Bank bank;
	private final DelayMessageManager delayMessageManager;
	
	public @NotNull JavaPlugin getPlugin()
	{
		return plugin;
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
	
	public @NotNull DelayMessageManager getDelayMessageManager()
	{
		return delayMessageManager;
	}
	
	public @NotNull NamespacedKey getNamespacedKey(@NotNull String key)
	{
		Validate.notNull((Object) key, "The namespaced key cannot be null.");
		
		return new NamespacedKey(plugin, key);
	}
	
	public void trySpawnBankEntity(@NotNull Location location)
	{
		World world = location.getWorld();
		
		if (world == null)
		{
			return;
		}
		
		if (world.getTime() >= EconomyConfig.BANK_OPEN.getValue() && world.getTime() < EconomyConfig.BANK_CLOSE.getValue())
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
		bankEntity.setProfession(Profession.CARTOGRAPHER);
		bankEntity.setCustomName(EconomyConfig.BANK_ENTITYNAME.getValue());
		EntityHelper.setTag(bankEntity, plugin, ENTITYKEY_BANK);
		EntityHelper.setTag(bankEntity, plugin, EntityHelper.ENTITYKEY_FOLLOWLOOK);
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
		closedEntity.setCustomName(EconomyConfig.BANK_ENTITYNAME.getValue());
		EntityHelper.setTag(closedEntity, plugin, ENTITYKEY_BANKCLOSED);
		
		closedEntity2.setSmall(true);
		closedEntity2.setVisible(false);
		closedEntity2.setGravity(false);
		closedEntity2.setInvulnerable(true);
		closedEntity2.setCollidable(false);
		closedEntity2.setCustomNameVisible(true);
		closedEntity2.setCustomName(EconomyConfig.BANK_CLOSEDNAME.getValue());
		EntityHelper.setTag(closedEntity2, plugin, ENTITYKEY_BANKCLOSED);
	}
	
	private boolean alreadyBankEntitySpawned(@NotNull Location location)
	{
		Validate.notNull((Object) location, "The bank entity location cannot be null");
		
		for (Entity entity : location.getWorld().getNearbyEntities(location, 1.5, 1.5, 1.5))
		{
			if (entity instanceof Villager && EntityHelper.hasTag(entity, plugin, ENTITYKEY_BANK))
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
			if (entity instanceof ArmorStand && EntityHelper.hasTag(entity, plugin, ENTITYKEY_BANKCLOSED))
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
			if (entity instanceof Villager && EntityHelper.hasTag(entity, plugin, ENTITYKEY_BANK))
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
			if (entity instanceof ArmorStand && EntityHelper.hasTag(entity, plugin, ENTITYKEY_BANKCLOSED))
			{
				entity.remove();		
			}
		}
	}
}
