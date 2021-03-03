package huff.economy;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
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
import huff.economy.storage.EconomyUser;
import huff.lib.helper.DataHelper;
import huff.lib.helper.EntityHelper;
import huff.lib.manager.delaymessage.DelayMessageManager;

/**
 * The dependency injection class for the economy module.
 */
public class EconomyInterface
{
	public static final String ENTITYKEY_BANK = "bank";
	public static final String ENTITYKEY_BANKCLOSED = "bank_closed";
	
	public EconomyInterface(@NotNull JavaPlugin plugin, @NotNull EconomyUser storage, @NotNull Signature signature, 
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
	private final EconomyUser storage;
	private final Signature signature;
	private final Bank bank;
	private final DelayMessageManager delayMessageManager;
	
	@NotNull
	public JavaPlugin getPlugin()
	{
		return plugin;
	}
	
	@NotNull
	public EconomyUser getStorage()
	{
		return storage;
	}
	
	@NotNull
	public Signature getSignature()
	{
		return signature;
	}
	
	@NotNull
	public Bank getBank()
	{
		return bank;
	}
	
	@NotNull
	public DelayMessageManager getDelayMessageManager()
	{
		return delayMessageManager;
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
			checkBankEnties(location, ENTITYKEY_BANKCLOSED, true);
			spawnBankEntity(location);
		}
		else
		{
			checkBankEnties(location, ENTITYKEY_BANK, true);
			spawnClosedEntity(location);	
		}
	}
	
	public void tryRemoveBankEntity(@NotNull Location location)
	{
		checkBankEnties(location, ENTITYKEY_BANK, true);
		checkBankEnties(location, ENTITYKEY_BANKCLOSED, true);
	}
	
	private void spawnBankEntity(@Nullable Location location)
	{
		if (location == null || checkBankEnties(location, ENTITYKEY_BANK, false))
		{
			return;
		}
		final Villager bankEntity = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
		
		bankEntity.setAI(false);
		bankEntity.setSilent(true);
		bankEntity.setInvulnerable(true);
		bankEntity.setCollidable(false);
		bankEntity.setVillagerType(Type.TAIGA);
		bankEntity.setProfession(Profession.CARTOGRAPHER);
		bankEntity.setCustomName(EconomyConfig.BANK_ENTITYNAME.getValue());
		EntityHelper.setTag(bankEntity, plugin, ENTITYKEY_BANK);
		EntityHelper.setTag(bankEntity, plugin, EntityHelper.ENTITYKEY_FOLLOWLOOK, DataHelper.convertLocationToString(location));
	}
	
	private void spawnClosedEntity(@Nullable Location location)
	{
		if (location == null || checkBankEnties(location, ENTITYKEY_BANKCLOSED, false))
		{
			return;
		}
		final Entity closedEntity = EntityHelper.createHovertextEntity(location, EconomyConfig.BANK_ENTITYNAME.getValue(), 1);
		final Entity closedEntity2 = EntityHelper.createHovertextEntity(location, EconomyConfig.BANK_CLOSEDNAME.getValue(), 2);
		
		EntityHelper.setTag(closedEntity, plugin, ENTITYKEY_BANKCLOSED);
		EntityHelper.setTag(closedEntity2, plugin, ENTITYKEY_BANKCLOSED);
	}
	
	private boolean checkBankEnties(@NotNull Location location, @NotNull String entityKey, boolean withRemove)
	{
		boolean entityFound = false;
		
		for (Entity entity : location.getWorld().getNearbyEntities(location, Bank.BANK_TOLERANCE, Bank.BANK_TOLERANCE, Bank.BANK_TOLERANCE))
		{
			if (EntityHelper.hasTag(entity, plugin, entityKey))
			{
				if (!entityFound)
				{
					entityFound = true;
				}
				
				if (withRemove)
				{
					entity.remove();
				}
				else
				{
					return entityFound;
				}
			}
		}
		return entityFound;
	}
}
