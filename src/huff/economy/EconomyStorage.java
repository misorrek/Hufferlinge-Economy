package huff.economy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import huff.lib.helper.StringHelper;
import huff.lib.manager.RedisManager;

public class EconomyStorage
{
	public static final int CODE_SUCCESS = 0;
	public static final int CODE_NOUSER = -1;
	public static final int CODE_NOTENOUGHVALUE = -2;
	
	private static final String PATTERN_USER = "user:";
	
	private static final String FIELD_BALANCE = "balance";
	private static final String FIELD_WALLET = "wallet";
	
	public EconomyStorage(@NotNull RedisManager redisManager)
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
		return redisManager.addMap(getPatternKey(uuid), getFieldValuePair(startBalance, 0.0));
	}
	
	public double getBalance(@NotNull UUID uuid)
	{
		return parseDouble(redisManager.getFieldValue(getPatternKey(uuid), FIELD_BALANCE));
	}
	
	public int setBalance(@NotNull UUID uuid, double value)
	{
		if (existUser(uuid))
		{
			redisManager.updateFieldValue(getPatternKey(uuid), FIELD_BALANCE, Double.toString(value));
			return CODE_SUCCESS;
		}
		return CODE_NOUSER;
	}
	
	public double getWallet(@NotNull UUID uuid)
	{
		return parseDouble(redisManager.getFieldValue(getPatternKey(uuid), FIELD_WALLET));
	}
	
	public int setWallet(@NotNull UUID uuid, double value)
	{
		if (existUser(uuid))
		{
			redisManager.updateFieldValue(getPatternKey(uuid), FIELD_WALLET, Double.toString(value));
			return CODE_SUCCESS;
		}
		return CODE_NOUSER;
	}
	
	public int updateBalance(@NotNull UUID uuid, double value, boolean remove, boolean withWallet)
	{
		final double currentBalance = getBalance(uuid);
		
		if (currentBalance == CODE_NOUSER)
		{
			return CODE_NOUSER;
		}
		
		if (remove && currentBalance < value)
		{
			return CODE_NOTENOUGHVALUE;
		}
		
		if (withWallet)
		{
			updateWallet(uuid, value, !remove);
		}
		setBalance(uuid, currentBalance + (remove ? value * -1 : value));
		return CODE_SUCCESS;
	}
	
	public int updateWallet(@NotNull UUID uuid, double value, boolean remove)
	{
		final double currentWallet = getWallet(uuid);
		
		if (currentWallet == -1)
		{
			return CODE_NOUSER;
		}
		
		if (remove && currentWallet < value)
		{
			return CODE_NOTENOUGHVALUE;
		}
		setWallet(uuid, currentWallet + (remove ? value * -1 : value));	
		return CODE_SUCCESS;
	}
	
	public @NotNull List<String> getEconomyOverview()
	{
		final List<String> economyOverview = new ArrayList<>();	
		final String rankKey = "rank";
		
		try
		{
			for (String key : redisManager.getJedis().keys(StringHelper.build('*', PATTERN_USER, '*')))
			{
				final double balance = parseDouble(redisManager.getFieldValue(key, FIELD_BALANCE));
				final double wallet = parseDouble(redisManager.getFieldValue(key, FIELD_WALLET));
				final double sum = balance + wallet;
				
				redisManager.getJedis().zadd(rankKey, sum, key);			
			}
			int position = 1;
			
			for (String key : redisManager.getJedis().zrange(rankKey, 0, -1))
			{
				final Player player = Bukkit.getPlayer(UUID.fromString(key));
				
				if (player != null)
				{
					final double balance = parseDouble(redisManager.getFieldValue(key, FIELD_BALANCE));
					final double wallet = parseDouble(redisManager.getFieldValue(key, FIELD_WALLET));
					final double sum = balance + wallet;
					
					economyOverview.add(String.format("§8☰ §a%d §8- §9%s\n" +
	                        "§8☷ §7Gesamt: §9%.0f §8× §7Konto: §9%.0f §8× §7Geldbeutel: §9%.0f", position, player.getName(), sum, balance, wallet));
					position++;
				}		
			}			
			redisManager.getJedis().del(rankKey);			
		}
		catch (Exception exception) 
		{
			Bukkit.getLogger().log(Level.SEVERE	, "Statement cannot be executed.", exception);
		}	
		return economyOverview;
	}
	
	private @NotNull String getPatternKey(@NotNull UUID uuid)
	{
		return PATTERN_USER + uuid.toString();
	}
	
	private Map<String, String> getFieldValuePair(double balance, double wallet)
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
