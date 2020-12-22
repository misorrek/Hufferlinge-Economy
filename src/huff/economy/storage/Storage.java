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
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.lib.manager.RedisManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public class Storage
{
	public static final int CODE_SUCCESS = 0;
	public static final int CODE_NOUSER = -1;
	public static final int CODE_NOTENOUGHVALUE = -2;
	
	private static final String PATTERN_USER = "user:";	
	private static final String FIELD_BALANCE = "balance";
	private static final String FIELD_WALLET = "wallet";
	
	public Storage(@NotNull RedisManager redisManager)
	{
		Validate.notNull((Object) redisManager, "The redis-manager cannot be null.");	
		
		this.redisManager = redisManager;
	}
	
	private RedisManager redisManager;
	
	public boolean existUser(@NotNull UUID uuid)
	{
		return redisManager.existKey(getPatternKey(uuid));
	}
	
	public boolean addUser(@NotNull UUID uuid, double startBalance) 
	{
		return redisManager.addMap(getPatternKey(uuid), getFieldValuePairs(startBalance, 0.0));
	}
	
	public List<UUID> getUsers()
	{
		return getUsers(null);
	}
	
	public List<UUID> getUsers(@Nullable UUID filteredUUID)
	{
		List<UUID> users = new ArrayList<>();
		
		for (String key : getKeys())
		{
			final UUID currentUUID = UUID.fromString(key.replace(PATTERN_USER, ""));
			
			if (!currentUUID.equals(filteredUUID))
			{
				users.add(currentUUID);
			}			
		}
		return users;
	}
	
	public double getBalance(@NotNull UUID uuid)
	{
		return parseDouble(redisManager.getFieldValue(getPatternKey(uuid), FIELD_BALANCE));
	}
	
	public double getWallet(@NotNull UUID uuid)
	{
		return parseDouble(redisManager.getFieldValue(getPatternKey(uuid), FIELD_WALLET));
	}
	
	public int setValue(@NotNull UUID uuid, double value, boolean isBalanceTransaction)
	{
		if (existUser(uuid))
		{
			redisManager.setFieldValue(getPatternKey(uuid), isBalanceTransaction ? FIELD_BALANCE : FIELD_WALLET, 
					                   Double.toString(value));
			return CODE_SUCCESS;
		}
		return CODE_NOUSER;
	}
	
	public int setBalance(@NotNull UUID uuid, double value)
	{
		return setValue(uuid, value, true);
	}
	
	public int setWallet(@NotNull UUID uuid, double value)
	{
		return setValue(uuid, value, false);
	}
	
	public int updateValue(@NotNull UUID uuid, double value, boolean remove, boolean isBalanceTransaction)
	{
		final double currentValue = isBalanceTransaction ? getBalance(uuid) : getWallet(uuid);
		
		if (currentValue == CODE_NOUSER)
		{
			return CODE_NOUSER;
		}
		
		if (remove && currentValue < value)
		{
			return CODE_NOTENOUGHVALUE;
		}
		setValue(uuid, currentValue + (remove ? value * -1 : value), isBalanceTransaction);
		return CODE_SUCCESS;
	}

	public int updateBalance(@NotNull UUID uuid, double value, boolean remove)
	{
		return updateValue(uuid, value, remove, true);
	}
	
	public int updateWallet(@NotNull UUID uuid, double value, boolean remove)
	{
		return updateValue(uuid, value, remove, false);
	}
	
	public boolean runTransaction(@NotNull UUID uuidFrom, @NotNull UUID uuidTo, double value, boolean isBalanceTransaction)
	{
		final double currentFrom = isBalanceTransaction ? getBalance(uuidFrom) : getWallet(uuidFrom);
		final double currentTo = isBalanceTransaction ? getBalance(uuidTo) : getWallet(uuidTo);
		
		if (currentFrom == -1 || currentTo == -1 || currentFrom < value)
		{
			return false;
		}
		
		try (final Jedis jedis = redisManager.getJedis())
		{
			final Transaction transaction = jedis.multi();
			
			transaction.hset(getPatternKey(uuidFrom), isBalanceTransaction ? FIELD_BALANCE : FIELD_WALLET, 
					         Double.toString(currentFrom + (value * -1)));
			transaction.hset(getPatternKey(uuidTo), isBalanceTransaction ? FIELD_BALANCE : FIELD_WALLET, 
					         Double.toString(currentTo + value));
			transaction.exec();
		}
		return true;
	}
	
	public boolean runTransaction(@NotNull UUID uuid, double value, boolean fromBalanceTransaction)
	{
		final double currentValue = fromBalanceTransaction ? getBalance(uuid) : getWallet(uuid);
		
		if (currentValue == -1 || currentValue < value)
		{
			return false;
		}
		
		try (final Jedis jedis = redisManager.getJedis())
		{
			final Transaction transaction = jedis.multi();
			
			transaction.hset(getPatternKey(uuid), fromBalanceTransaction ? FIELD_BALANCE : FIELD_WALLET, 
					         Double.toString(currentValue + (value * -1)));
			transaction.hset(getPatternKey(uuid), fromBalanceTransaction ? FIELD_WALLET : FIELD_BALANCE, 
					         Double.toString(currentValue + value));
			transaction.exec();
		}
		return true;
	}
	
	public @NotNull List<String> getEconomyOverview()
	{
		final List<String> economyOverview = new ArrayList<>();	
		final String rankKey = "rank";
		
		try (final Jedis jedis = redisManager.getJedis())
		{
			for (String key : getKeys())
			{
				final double balance = parseDouble(jedis.hget(key, FIELD_BALANCE));
				final double wallet = parseDouble(jedis.hget(key, FIELD_WALLET));
				final double sum = balance + wallet;
				
				redisManager.getJedis().zadd(rankKey, sum * -1, key);			
			}
			int position = 1;
			
			for (String key : jedis.zrange(rankKey, 0, -1))
			{
				final OfflinePlayer player = Bukkit.getOfflinePlayer(getUUIDFromKey(key));
				
				if (player != null)
				{
					final double balance = parseDouble(jedis.hget(key, FIELD_BALANCE));
					final double wallet = parseDouble(jedis.hget(key, FIELD_WALLET));
					final double sum = balance + wallet;
					
					economyOverview.add(String.format("§8☰ §a%d §8- §9%s\n" +
	                                                  "§8☷ §7Gesamt: §9%.0f §8× §7Konto: §9%.0f §8× §7Geldbeutel: §9%.0f", position, player.getName(), sum, balance, wallet));
					position++;
				}		
			}			
			jedis.del(rankKey);			
		}
		catch (Exception exception) 
		{
			Bukkit.getLogger().log(Level.SEVERE	, "Redis-Statement cannot be executed.", exception);
		}	
		return economyOverview;
	}
	
	private @NotNull Set<String> getKeys()
	{
		try (final Jedis jedis = redisManager.getJedis())
		{
			return jedis.keys('*' + PATTERN_USER + '*');
		}
		catch (Exception exception) 
		{
			Bukkit.getLogger().log(Level.SEVERE	, "Redis-Statement cannot be executed.", exception);
		}
		return new HashSet<>();
	}
	
	private @NotNull String getPatternKey(@NotNull UUID uuid)
	{
		return PATTERN_USER + uuid.toString();
	}
	
	private @Nullable UUID getUUIDFromKey(@NotNull String key)
	{
		return UUID.fromString(key.replace(PATTERN_USER, ""));
	}
	
	private Map<String, String> getFieldValuePairs(double balance, double wallet)
	{
		final Map<String, String> resultMap = new HashMap<>();
		
		resultMap.put(FIELD_BALANCE, Double.toString(balance));
		resultMap.put(FIELD_WALLET, Double.toString(wallet));

		return resultMap; 
	}
	
	private double parseDouble(String doubleString)
	{		
		if (doubleString == null)
		{
			return -1;
		}	
		
		try
		{
			return Double.parseDouble(doubleString);
		}
		catch (NumberFormatException exception)
		{
			Bukkit.getLogger().log(Level.WARNING, "The double-value is invalid.", exception);
		}
		return -1;
	}
}
