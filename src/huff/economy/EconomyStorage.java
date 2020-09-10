package huff.economy;

import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import huff.lib.manager.RedisManager;

public class EconomyStorage
{
	public EconomyStorage(@NotNull RedisManager redisManager)
	{
		Validate.notNull((Object) redisManager, "The database-manager cannot be null.");		
		this.redisManager = redisManager;
	}
	
	private RedisManager redisManager;
	
	public boolean userExist(@NotNull UUID uuid)
	{
		Validate.notNull((Object) uuid, "The uuid cannot be null.");
		
		try 
		{
			return redisManager.getJedis().exists(uuid.toString());
		} 
		catch (Exception exception) 
		{
			Bukkit.getLogger().log(Level.SEVERE	, "Redis-Statement cannot be executed.", exception);
		}
		return false;
	}
	
	public boolean addUser(@NotNull UUID uuid, double startBalance) 
	{
		if (!userExist(uuid))
		{
			try 
			{
				//TODO
				return true;
			} 
			catch (Exception exception) 
			{
				Bukkit.getLogger().log(Level.SEVERE	, "Redis-Statement cannot be executed.", exception);
			}
		}
		return false;
	}
}
