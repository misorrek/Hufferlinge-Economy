package huff.economy;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import huff.lib.helper.StringHelper;
import huff.lib.manager.RedisManager;

public class EconomySignature
{
	private static final String OFFLINE_SIGNATURE = "#0000#";
	
	private static final String PATTERN_USER = "signature:";
	
	public EconomySignature(@NotNull RedisManager redisManager)
	{
		Validate.notNull((Object) redisManager, "The redis-manager cannot be null.");	
		
		this.redisManager = redisManager;
	}
	
	private RedisManager redisManager;
	
	private @NotNull String createSignature(int valueAmount)
	{
		final String signatureId = RandomStringUtils.random(12, "0123456789ABCDEF");
		
		redisManager.getJedis().set(getPatternKey(signatureId), Integer.toString(valueAmount));
		
		return signatureId;
	}
	
	public @NotNull List<String> createSignatureLore(int valueAmount)
	{
		final List<String> signatureLore = new ArrayList<>();
		final String signature = redisManager.isConnected() ? createSignature(valueAmount) : OFFLINE_SIGNATURE;
		
		signatureLore.add("§8Prägung");
		signatureLore.add("§8" + signature);
		
		return signatureLore;
	}
	
	public int getSignatureValueAmount(List<String> signatureLore, int wantedValueAmount)
	{
		final String loreSignature = signatureLore.get(1).substring(2);
		
		if (StringHelper.isNullOrEmpty(loreSignature))
		{
			return -1;
		}
		if (loreSignature.equals(OFFLINE_SIGNATURE))
		{
			return wantedValueAmount;
		}
		final String patternKey = getPatternKey(loreSignature);
		final String storedValue = redisManager.getJedis().get(patternKey);
		
		if (StringHelper.isNullOrEmpty(storedValue))
		{
			return -1;
		}		
		final int signatureValueAmount = Integer.parseInt(storedValue);
		
		if (signatureValueAmount <= wantedValueAmount)
		{
			 redisManager.getJedis().del(patternKey);
		}	
		else
		{
			 redisManager.getJedis().set(patternKey, Integer.toString(signatureValueAmount - wantedValueAmount));
		}
		return signatureValueAmount;
	}
	
	private @NotNull String getPatternKey(@NotNull String key)
	{
		return PATTERN_USER + key;
	}
}