package huff.economy.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.lib.helper.DataHelper;
import huff.lib.manager.RedisManager;
import huff.lib.storage.RedisFeedback;
import huff.lib.storage.RedisStorage;

/**
 * A redis storage class that stores the locations of banks.
 */
public class Bank extends RedisStorage
{
	public static final double BANK_TOLERANCE = 2;
	
	private static final String PATTERN= "bank:";	
	private static final String FIELD_LOCATION = "location";
	private static final String FIELD_OWNER = "owner";
	
	private static final double REMOVE_DISTANCE = 10;
	
	public Bank(@NotNull RedisManager redisManager)
	{
		super(redisManager, false);
	}
	
	@Override
	protected @NotNull String getKeyPattern() 
	{
		return PATTERN;
	}
	
	// B A N K
		
	public boolean isBankAtLocation(@NotNull Location location, double tolerance)
	{
		Validate.notNull((Object) location, "The location cannot be null.");	
		
		return getBankAtLocation(location, tolerance) != null;
	}
	
	@Nullable
	public String getBankAtLocation(@NotNull Location location, double tolerance)
	{
		Validate.notNull((Object) location, "The location cannot be null.");	
		
		String nearestBankKey = null;
		double nearestBankDistance = tolerance;	
		
		for (String key : getCombinedKeys())
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
		final String bankKey = getBankAtLocation(location, BANK_TOLERANCE);
		
		if (bankKey != null)
		{
			final String fieldValue = redisManager.getFieldValue(bankKey, FIELD_OWNER);
			
			return fieldValue != null && fieldValue.equals(uuid.toString());		
		}
		return false;
	}
	
	@NotNull
	public List<Location> getBankLocations()
	{
		List<Location> bankLocations = new ArrayList<>();
		
		for (String key : getCombinedKeys())
		{
			final Location bankLocaction = DataHelper.convertStringtoLocation(redisManager.getFieldValue(key, FIELD_LOCATION));
			
			if (bankLocaction != null)
			{
				bankLocations.add(bankLocaction);
			}
		}
		return bankLocations;
	}
	
	public RedisFeedback addBank(@NotNull Location location, @Nullable UUID ownerUuid)
	{
		Validate.notNull((Object) location, "The bank location cannot be null.");	
		
		if (isBankAtLocation(location, BANK_TOLERANCE))
		{
			return RedisFeedback.DUPLICATE;
		}
		final Map<String, String> resultMap = new HashMap<>();
		
		resultMap.put(FIELD_LOCATION, DataHelper.convertLocationToString(location));
		resultMap.put(FIELD_OWNER, ownerUuid == null ? "" : ownerUuid.toString());
		
		redisManager.addMap(getAutoCombinedKey(), resultMap);
		
		return RedisFeedback.SUCCESS;
	}
	
	public RedisFeedback removeBank(@NotNull Location location)
	{
		Validate.notNull((Object) location, "The bank location cannot be null.");	
		
		final String bankKey = getBankAtLocation(location, REMOVE_DISTANCE);
		
		if (bankKey != null)
		{
			redisManager.deleteKey(bankKey);
	
			return RedisFeedback.SUCCESS;
		}
		return RedisFeedback.NOENTRY;
	}
}
