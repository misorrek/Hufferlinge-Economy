package huff.economy.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import huff.lib.helper.JavaHelper;
import huff.lib.helper.UserHelper;
import huff.lib.manager.RedisManager;
import huff.lib.storage.RedisFeedback;
import huff.lib.storage.User;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * A redis storage class that extends the user storage for economy user data.
 */
public class EconomyUser extends User
{	
	private static final String FIELD_BALANCE = "balance";
	private static final String FIELD_WALLET = "wallet";
	
	public EconomyUser(@NotNull RedisManager redisManager)
	{
		super(redisManager);
	}
	
	public void setDefaultFieldValues(@NotNull UUID uuid, double balance, double wallet)
	{
		final String combinedKey = getCombinedKey(uuid);
		
		redisManager.setFieldValue(combinedKey, FIELD_BALANCE, Double.toString(balance));
		redisManager.setFieldValue(combinedKey, FIELD_WALLET, Double.toString(wallet));
	}
	
	public double getBalance(@NotNull UUID uuid)
	{
		return JavaHelper.tryParseDouble(redisManager.getFieldValue(getCombinedKey(uuid), FIELD_BALANCE));
	}
	
	public double getWallet(@NotNull UUID uuid)
	{
		return JavaHelper.tryParseDouble(redisManager.getFieldValue(getCombinedKey(uuid), FIELD_WALLET));
	}
	
	public void setValue(@NotNull UUID uuid, double value, boolean isBalanceTransaction)
	{
		redisManager.setFieldValue(getCombinedKey(uuid), isBalanceTransaction ? FIELD_BALANCE : FIELD_WALLET, 
				                   Double.toString(value));
	}
	
	public void setBalance(@NotNull UUID uuid, double value)
	{
		setValue(uuid, value, true);
	}
	
	public void setWallet(@NotNull UUID uuid, double value)
	{
		setValue(uuid, value, false);
	}
	
	public RedisFeedback updateValue(@NotNull UUID uuid, double value, boolean remove, boolean isBalanceTransaction)
	{
		double currentValue = isBalanceTransaction ? getBalance(uuid) : getWallet(uuid);
		
		if (currentValue == RedisFeedback.NOENTRY.getCode())
		{
			setDefaultFieldValues(uuid, 0, 0);
			currentValue = 0;
		}
		
		if (remove && currentValue < value)
		{
			return RedisFeedback.NOCORRECTVALUE;
		}
		setValue(uuid, currentValue + (remove ? value * -1 : value), isBalanceTransaction);
		
		return RedisFeedback.SUCCESS;
	}

	public RedisFeedback updateBalance(@NotNull UUID uuid, double value, boolean remove)
	{
		return updateValue(uuid, value, remove, true);
	}
	
	public RedisFeedback updateWallet(@NotNull UUID uuid, double value, boolean remove)
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
			
			transaction.hset(getCombinedKey(uuidFrom), isBalanceTransaction ? FIELD_BALANCE : FIELD_WALLET, 
					         Double.toString(currentFrom + (value * -1)));
			transaction.hset(getCombinedKey(uuidTo), isBalanceTransaction ? FIELD_BALANCE : FIELD_WALLET, 
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
			
			transaction.hset(getCombinedKey(uuid), fromBalanceTransaction ? FIELD_BALANCE : FIELD_WALLET, 
					         Double.toString(currentValue + (value * -1)));
			transaction.hset(getCombinedKey(uuid), fromBalanceTransaction ? FIELD_WALLET : FIELD_BALANCE, 
					         Double.toString(currentValue + value));
			transaction.exec();
		}
		return true;
	}
	
	@NotNull
	public List<String> getEconomyOverview(int startIndex, int limit)
	{
		final List<String> economyOverview = new ArrayList<>();	
		final String rankKey = "rank";
		
		try (final Jedis jedis = redisManager.getJedis())
		{
			for (String key : getCombinedKeys())
			{
				final double balance = JavaHelper.tryParseDouble(jedis.hget(key, FIELD_BALANCE));
				final double wallet = JavaHelper.tryParseDouble(jedis.hget(key, FIELD_WALLET));
				final double sum = balance + wallet;
				
				jedis.zadd(rankKey, sum * -1, key);			
			}
			final List<String> orderdKeys = new ArrayList<>();
			
			orderdKeys.addAll(jedis.zrange(rankKey, 0, -1));
			
			for (int i = startIndex; i < orderdKeys.size() && i < limit; i++)
			{
				final String key = orderdKeys.get(i);
				final String username = UserHelper.getUsername(getKey(key));
				
				if (username != null)
				{
					final double balance = JavaHelper.tryParseDouble(jedis.hget(key, FIELD_BALANCE));
					final double wallet = JavaHelper.tryParseDouble(jedis.hget(key, FIELD_WALLET));
					final double sum = balance + wallet;
					
					economyOverview.add(String.format("§8☰ §a%d §8- §9%s\n" +
	                                                  "§8☷ §7Gesamt: §9%.0f §8× §7Konto: §9%.0f §8× §7Geldbeutel: §9%.0f", i + 1, username, sum, balance, wallet));
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
}
