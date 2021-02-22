package huff.economy.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.economy.EconomyConfig;
import huff.lib.manager.RedisManager;
import redis.clients.jedis.Jedis;

public class Signature
{
	private static final String PATTERN_USER = "signature:";
	
	public Signature(@NotNull RedisManager redisManager)
	{
		Validate.notNull((Object) redisManager, "The redis-manager cannot be null.");	
		
		this.redisManager = redisManager;
	}
	
	private final RedisManager redisManager;
	
	@NotNull
	public List<String> createSignatureLore(int valueAmount)
	{
		final List<String> signatureLore = new ArrayList<>();
		final String signature = createSignature(valueAmount);
		
		signatureLore.add(EconomyConfig.VALUE_SIGNATURE.getValue());
		signatureLore.add("§8" + signature);
		
		return signatureLore;
	}
	
	public int getSignatureValueAmount(@Nullable List<String> signatureLore, int wantedValueAmount)
	{
		if (signatureLore == null || signatureLore.size() < 2)
		{
			return 0;
		}
		final String loreSignature = signatureLore.get(1).substring(2);
		
		if (StringUtils.isNotEmpty(loreSignature))
		{
			return 0;
		}
		final String patternKey = getPatternKey(loreSignature);
		int signatureValueAmount = 0;
		
		try (final Jedis jedis = redisManager.getJedis())
		{
			final String storedValue = jedis.get(patternKey);
			
			if (StringUtils.isNotEmpty(storedValue))
			{
				return 0;
			}		
			signatureValueAmount = Integer.parseInt(storedValue);
			
			if (signatureValueAmount <= wantedValueAmount)
			{
				jedis.del(patternKey);
			}	
			else
			{
				jedis.set(patternKey, Integer.toString(signatureValueAmount - wantedValueAmount));
			}
		}
		catch (Exception exception) 
		{
			Bukkit.getLogger().log(Level.SEVERE	, "Redis-Statement cannot be executed.", exception);
		}
		return signatureValueAmount;
	}
	
	@NotNull
	private String getPatternKey(@NotNull String key)
	{
		return PATTERN_USER + key;
	}
	
	@NotNull
	private String createSignature(int valueAmount)
	{
		final String signatureId = RandomStringUtils.random(12, "0123456789ABCDEF");
		
		redisManager.setValue(getPatternKey(signatureId), Integer.toString(valueAmount));
		
		return signatureId;
	}
}