package huff.economy.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import huff.lib.helper.DataHelper;
import huff.lib.helper.StringHelper;
import huff.lib.manager.RedisManager;

public class EconomyBank
{
	public static final int CODE_SUCCESS = 0;
	public static final int CODE_NOBANK = -1;
	public static final int CODE_DUPLICATE = -2;
	
	private static final String PATTERN_BANK = "bank:";	
	private static final String FIELD_LOCATION = "location";
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
		
		for (Location otherLoc : getBankLocations())
		{
			if (otherLoc.distance(location) <= tolerance)
			{
				return true;
			}
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
	
	public int addBank(@NotNull Location location)
	{
		Validate.notNull((Object) location, "The bank-location cannot be null.");	
		
		if (isBankAtLocation(location, 2))
		{
			return CODE_DUPLICATE;
		}
		redisManager.addMap(getNewPatternKey(), getFieldValuePair(location));
		return CODE_SUCCESS;
	}	
	
	public int removeBank(@NotNull Location location)
	{
		Validate.notNull((Object) location, "The bank-location cannot be null.");	
		
		String nearestBankKey = "";
		double nearestBankDistance = REMOVE_DISTANCE;	
		
		for (String key : getKeys())
		{
			final Location bankLocaction = DataHelper.convertStringtoLocation(redisManager.getFieldValue(key, FIELD_LOCATION));	
			
			if (bankLocaction != null)
			{
				final double currentBankDistance = bankLocaction.distance(location);
				
				if (currentBankDistance < nearestBankDistance)
				{
					nearestBankKey = key;
					nearestBankDistance = currentBankDistance;
				}
			}
		}
		
		if (!nearestBankKey.isEmpty())
		{
			redisManager.getJedis().del(nearestBankKey);
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
	
	private Map<String, String> getFieldValuePair(@NotNull Location location)
	{
		final Map<String, String> resultMap = new HashMap<>();
		
		resultMap.put(FIELD_LOCATION, DataHelper.convertLocationToString(location));

		return resultMap; 
	}
}
