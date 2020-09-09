package huff.economy;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import huff.lib.helper.StringHelper;
import redis.clients.jedis.Jedis;

public class EconomySignature
{
	private static final String OFFLINE_SIGNATURE = "#0000#";
	
	public EconomySignature()
	{
		try
		{
			this.jedis = new Jedis("127.0.0.1", 6379);
		}
		catch (Exception exception)
		{
			Bukkit.getLogger().log(Level.SEVERE, "Cant init jedis.", exception);
		}
		Bukkit.getConsoleSender().sendMessage(jedis.ping());
		Bukkit.getConsoleSender().sendMessage(Boolean.toString(isConnected()));
	}
	
	private Jedis jedis;
	
	private boolean isConnected()
	{
		return jedis != null && jedis.isConnected();
	}
	
	private @NotNull String createSignature(int valueAmount)
	{
		final String signatureId = RandomStringUtils.random(12, "0123456789ABCDEF");
		
		jedis.set(signatureId, Integer.toString(valueAmount));
		
		return signatureId;
	}
	
	public @NotNull List<String> createSignatureLore(int valueAmount)
	{
		final List<String> signatureLore = new ArrayList<>();
		final String signature = isConnected() ? createSignature(valueAmount) : OFFLINE_SIGNATURE;
		
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
		final String storedValue = jedis.get(loreSignature);
		
		if (StringHelper.isNullOrEmpty(storedValue))
		{
			return -1;
		}		
		final int signatureValueAmount = Integer.parseInt(storedValue);
		
		if (signatureValueAmount <= wantedValueAmount)
		{
			jedis.del(loreSignature);
		}	
		else
		{
			jedis.set(loreSignature, Integer.toString(signatureValueAmount - wantedValueAmount));
		}
		return signatureValueAmount;
	}
}
