package huff.economy.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.economy.EconomyConfig;
import huff.lib.helper.DataHelper;
import huff.lib.helper.StringHelper;
import huff.lib.manager.RedisManager;

public class EconomyBank
{
	public static final int CODE_SUCCESS = 0;
	public static final int CODE_NOBANK = -1;
	public static final int CODE_DUPLICATE = -2;
	public static final int CODE_NOSPACE = -3;
	
	private static final String PATTERN_BANK = "bank:";	
	private static final String FIELD_LOCATION = "location";
	private static final String FIELD_OWNER = "owner";
	private static final double REMOVE_DISTANCE = 10;
	
	public EconomyBank(@NotNull RedisManager redisManager)
	{
		Validate.notNull((Object) redisManager, "The redis-manager cannot be null.");	
		
		this.redisManager = redisManager;
	}
	
	private RedisManager redisManager;
		
	public boolean isBankAtLocation(@NotNull Location location, double tolerance)
	{
		Validate.notNull((Object) location, "The location cannot be null.");	
		
		return getBankAtLocation(location, tolerance) != null;
	}
	
	public @Nullable String getBankAtLocation(@NotNull Location location, double tolerance)
	{
		Validate.notNull((Object) location, "The location cannot be null.");	
		
		String nearestBankKey = null;
		double nearestBankDistance = tolerance;	
		
		for (String key : getKeys())
		{
			final Location bankLocaction = DataHelper.convertStringtoLocation(redisManager.getFieldValue(key, FIELD_LOCATION));	
			
			if (bankLocaction != null)
			{
				final double currentBankDistance = bankLocaction.distance(location);
				
				if (currentBankDistance <= nearestBankDistance)
				{
					nearestBankKey = key;
					nearestBankDistance = currentBankDistance;
				}
			}
		}
		return nearestBankKey;
	} 
	
	public boolean isOwner(@NotNull UUID uuid, @NotNull Location location)
	{
		final String bankKey = getBankAtLocation(location, 2);
		
		if (bankKey != null)
		{
			return redisManager.getFieldValue(bankKey, FIELD_OWNER).equals(uuid.toString());
		}
		return false;
	}
	
	public @NotNull List<Location> getBankLocations()
	{
		List<Location> bankLocations = new ArrayList<>();
		
		for (String key : getKeys())
		{
			final Location bankLocaction = DataHelper.convertStringtoLocation(redisManager.getFieldValue(key, FIELD_LOCATION));
			
			if (bankLocaction != null)
			{
				bankLocations.add(bankLocaction);
			}
		}
		return bankLocations;
	}
	
	public int addBank(@NotNull Location location, @Nullable UUID ownerUUID)
	{
		Validate.notNull((Object) location, "The bank-location cannot be null.");	
		
		if (isBankAtLocation(location, 2))
		{
			return CODE_DUPLICATE;
		}
		redisManager.addMap(getNewPatternKey(), getFieldValuePairs(location, ownerUUID));
		return CODE_SUCCESS;
	}	
	
	public int handleBankAdd(@NotNull Player player, @NotNull EconomyConfig economyConfig)
	{
		Validate.notNull((Object) player, "The bank-location cannot be null.");
		
		final Location playerLocation = player.getLocation();
		final Location bankLocation = new Location(playerLocation.getWorld(), playerLocation.getBlockX() + 0.5, playerLocation.getBlockY(), playerLocation.getBlockZ() + 0.5);
		
		if (addBank(bankLocation, player.getUniqueId()) == EconomyBank.CODE_SUCCESS)
		{
			bankLocation.subtract(0, 1, 0).getBlock().setType(economyConfig.getBankMaterial());
			return CODE_SUCCESS;
		}
		else
		{
			return CODE_DUPLICATE;
		}	
	}
	
	public int removeBank(@NotNull Location location)
	{
		Validate.notNull((Object) location, "The bank-location cannot be null.");	
		
		final String bankKey = getBankAtLocation(location, REMOVE_DISTANCE);
		
		if (bankKey != null)
		{
			redisManager.getJedis().del(bankKey);
			return CODE_SUCCESS;
		}
		return CODE_NOBANK;
	}
	
	private @NotNull Set<String> getKeys()
	{
		return redisManager.getJedis().keys(StringHelper.build('*', PATTERN_BANK, '*'));
	}
	
	private @NotNull String getPatternKey(@NotNull String key)
	{
		return PATTERN_BANK + key;
	}
	
	private @NotNull String getNewPatternKey()
	{
		return PATTERN_BANK + getKeys().size();
	}
	
	private Map<String, String> getFieldValuePairs(@NotNull Location location, @Nullable UUID owenerUUID)
	{
		final Map<String, String> resultMap = new HashMap<>();
		
		resultMap.put(FIELD_LOCATION, DataHelper.convertLocationToString(location));
		resultMap.put(FIELD_OWNER, owenerUUID == null ? "" : owenerUUID.toString());
		
		return resultMap; 
	}
}
