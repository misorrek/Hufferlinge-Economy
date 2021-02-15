package huff.economy.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.lib.helper.DataHelper;
import huff.lib.helper.StringHelper;
import huff.lib.manager.RedisManager;
import redis.clients.jedis.Jedis;

public class Bank
{
	public static final int CODE_SUCCESS = 0;
	public static final int CODE_NOBANK = -1;
	public static final int CODE_DUPLICATE = -2;
	public static final int CODE_NOSPACE = -3;
	
	private static final String PATTERN_BANK = "bank:";	
	private static final String FIELD_LOCATION = "location";
	private static final String FIELD_OWNER = "owner";
	private static final double REMOVE_DISTANCE = 10;
	
	public Bank(@NotNull RedisManager redisManager)
	{
		Validate.notNull((Object) redisManager, "The redis-manager cannot be null.");	
		
		this.redisManager = redisManager;
	}
	
	private final RedisManager redisManager;
		
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
	
	public int getBankCount()
	{
		return getKeys().size();
	}
	
	public boolean isOwner(@NotNull UUID uuid, @NotNull Location location)
	{
		final String bankKey = getBankAtLocation(location, 2);
		
		if (bankKey != null)
		{
			final String fieldValue = redisManager.getFieldValue(bankKey, FIELD_OWNER);
			
			return fieldValue != null && fieldValue.equals(uuid.toString());		
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
		Validate.notNull((Object) location, "The bank location cannot be null.");	
		
		if (isBankAtLocation(location, 1.5))
		{
			return CODE_DUPLICATE;
		}
		redisManager.addMap(getNewPatternKey(), getFieldValuePairs(location, ownerUUID));
		
		return CODE_SUCCESS;
	}
	
	public int removeBank(@NotNull Location location)
	{
		Validate.notNull((Object) location, "The bank location cannot be null.");	
		
		final String bankKey = getBankAtLocation(location, REMOVE_DISTANCE);
		
		if (bankKey != null)
		{
			redisManager.deleteKey(bankKey);
	
			return CODE_SUCCESS;
		}
		return CODE_NOBANK;
	}
	
	private @NotNull Set<String> getKeys()
	{
		try (final Jedis jedis = redisManager.getJedis())
		{
			return jedis.keys(StringHelper.build('*', PATTERN_BANK, '*'));
		}
		catch (Exception exception) 
		{
			Bukkit.getLogger().log(Level.SEVERE	, "Redis statement cannot be executed.", exception);
		}
		return new HashSet<>();
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
