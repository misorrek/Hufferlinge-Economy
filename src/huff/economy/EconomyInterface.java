package huff.economy;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Type;
import org.jetbrains.annotations.NotNull;

import huff.economy.storage.Bank;
import huff.economy.storage.Signature;
import huff.economy.storage.Storage;
import huff.lib.manager.delayedmessage.DelayedMessagesManager;

public class EconomyInterface
{
	public EconomyInterface(@NotNull Config config, @NotNull Storage storage, @NotNull Signature signature, 
			                @NotNull Bank bank, @NotNull DelayedMessagesManager delayedMessageManager)
	{
		Validate.notNull((Object) config, "The economy-config cannot be null.");
		Validate.notNull((Object) storage, "The economy-storage cannot be null.");
		Validate.notNull((Object) signature, "The economy-signature cannot be null");
		Validate.notNull((Object) bank, "The economy-bank cannot be null");
		Validate.notNull((Object) delayedMessageManager, "The delayed-message-manager cannot be null.");
		
		this.config = config;
		this.storage = storage;
		this.signature = signature;
		this.bank = bank;
		this.delayedMessageManager = delayedMessageManager;
	}
	private final Config config;
	private final Storage storage;
	private final Signature signature;
	private final Bank bank;
	private final DelayedMessagesManager delayedMessageManager;
	
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
	
	public @NotNull Villager spawnBankEntity(@NotNull Location location)
	{
		Validate.notNull((Object) location, "The bank-entity-location cannot be null");
		
		final Villager bankEntity = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
		
		bankEntity.setAI(false);
		bankEntity.setInvulnerable(true);
		bankEntity.setCollidable(false);
		bankEntity.setVillagerType(Type.DESERT);
		bankEntity.setCustomName(config.getBankEntityName());
		
		return bankEntity;
	}
	
	public void removeBankEntity(@NotNull Location location, boolean removeBlock)
	{
		Validate.notNull((Object) location, "The bank-entity-location cannot be null");
		
		for (Entity entity : location.getWorld().getNearbyEntities(location, 2, 2, 2))
		{
			if (entity instanceof Villager && entity.getCustomName().equals(config.getBankEntityName()))
			{
				entity.remove();
				
				if (removeBlock)
				{
					entity.getLocation().subtract(0, 1, 0).getBlock().setType(Material.AIR);
				}			
			}
		}
	}
}
