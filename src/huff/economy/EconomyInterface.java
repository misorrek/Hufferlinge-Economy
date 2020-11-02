package huff.economy;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Type;
import org.jetbrains.annotations.NotNull;

import huff.economy.storage.EconomyBank;
import huff.economy.storage.EconomySignature;
import huff.economy.storage.EconomyStorage;
import huff.lib.manager.delayedmessage.DelayedMessageManager;

public class EconomyInterface
{
	public EconomyInterface(@NotNull EconomyConfig economyConfig, @NotNull EconomyStorage economyStorage, @NotNull EconomySignature economySignature, 
			                @NotNull EconomyBank economyBank, @NotNull DelayedMessageManager delayedMessageManager)
	{
		Validate.notNull((Object) economyConfig, "The economy-config cannot be null.");
		Validate.notNull((Object) economyStorage, "The economy-storage cannot be null.");
		Validate.notNull((Object) economySignature, "The economy-signature cannot be null");
		Validate.notNull((Object) economyBank, "The economy-bank cannot be null");
		//Validate.notNull((Object) delayedMessageManager, "The delayed-message-manager cannot be null.");
		
		this.economyConfig = economyConfig;
		this.economyStorage = economyStorage;
		this.economySignature = economySignature;
		this.economyBank = economyBank;
		this.delayedMessageManager = delayedMessageManager;
	}
	private final EconomyConfig economyConfig;
	private final EconomyStorage economyStorage;
	private final EconomySignature economySignature;
	private final EconomyBank economyBank;
	private final DelayedMessageManager delayedMessageManager;
	
	public @NotNull EconomyConfig getConfig()
	{
		return economyConfig;
	}
	
	public @NotNull EconomyStorage getStorage()
	{
		return economyStorage;
	}
	
	public @NotNull EconomySignature getSignature()
	{
		return economySignature;
	}
	
	public @NotNull EconomyBank getBank()
	{
		return economyBank;
	}
	
	public @NotNull DelayedMessageManager getDelayedMessageManager()
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
		bankEntity.setCustomName(economyConfig.getBankEntityName());
		
		return bankEntity;
	}
	
	public void removeBankEntity(@NotNull Location location)
	{
		Validate.notNull((Object) location, "The bank-entity-location cannot be null");
		
		for (Entity entity : location.getWorld().getNearbyEntities(location, 2, 2, 2))
		{
			if (entity instanceof Villager && entity.getCustomName().equals(economyConfig.getBankEntityName()))
			{
				entity.remove();
			}
		}
	}
}
