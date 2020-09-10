package huff.economy;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import huff.lib.manager.DatabaseManager;

public class EconomyTable
{
	public static final int CODE_SUCCESS = 0;
	public static final int CODE_NOUSER = -1;
	public static final int CODE_NOTENOUGHVALUE = -2;
	
	private static final String TABLE = "HuffEconomy";
	
	private static final String FIELD_UUID = "UUID";
	private static final String FIELD_BALANCE = "Balance";
	private static final String FIELD_WALLET = "Wallet";
	private static final String FIELD_SUM = "sum";
	
	public EconomyTable(@NotNull  DatabaseManager databaseManager)
	{
		Validate.notNull((Object) databaseManager, "The database-manager cannot be null.");
		
		this.databaseManager = databaseManager;
		
		checkTable();
	}
	
	private DatabaseManager databaseManager;
	
	private void checkTable() //Umstieg auf Redice?
	{
		try 
		{
			PreparedStatement ps = databaseManager.prepareStatement("CREATE TABLE IF NOT EXISTS %1$s(%2$s NVARCHAR(100), %3$s DOUBLE, %4$s DOUBLE, PRIMARY KEY(%2$s))", 
					                                                TABLE, FIELD_UUID, FIELD_BALANCE, FIELD_WALLET);
			ps.executeUpdate();
		} 
		catch (SQLException exception) 
		{
			Bukkit.getLogger().log(Level.SEVERE	, "Table " + TABLE + " cannot be created.", exception);
		}
	}
	
	public boolean userExist(@NotNull UUID uuid) 
	{
		Validate.notNull((Object) uuid, "The uuid cannot be null.");
		
		try 
		{
			PreparedStatement ps = databaseManager.prepareStatement("SELECT * FROM %s WHERE %s = ?", TABLE, FIELD_UUID);
			ps.setString(1, uuid.toString());
			
			try (ResultSet rs = ps.executeQuery())
			{
				return rs.next();
			}
		} 
		catch (SQLException exception) 
		{
			Bukkit.getLogger().log(Level.SEVERE	, "Statement cannot be executed.", exception);
		}
		return false;
	}
	
	public boolean addUser(@NotNull UUID uuid, double startBalance) 
	{
		if (!userExist(uuid))
		{
			try 
			{
				PreparedStatement ps = databaseManager.prepareStatement("INSERT INTO %s(%s,%s,%s) VALUES (?,?,?)", TABLE, FIELD_UUID, FIELD_BALANCE, FIELD_WALLET);
				ps.setString(1, uuid.toString());
				ps.setDouble(2, startBalance);
				ps.setDouble(3, 0.0);
				ps.executeUpdate();
				return true;
			} 
			catch (SQLException exception) 
			{
				Bukkit.getLogger().log(Level.SEVERE	, "Statement cannot be executed.", exception);
			}
		}
		return false;
	}
	
	public double getBalance(@NotNull UUID uuid)
	{
		if (userExist(uuid))
		{
			try
			{
				PreparedStatement ps = databaseManager.prepareStatement("SELECT %s FROM %s WHERE %s = ?", FIELD_BALANCE, TABLE, FIELD_UUID);
				ps.setString(1, uuid.toString());
				
				try (ResultSet rs = ps.executeQuery())
				{
					while (rs.next()) 
					{
						return rs.getDouble(FIELD_BALANCE);
					}
				}
			}
			catch (SQLException exception) 
			{
				Bukkit.getLogger().log(Level.SEVERE	, "Statement cannot be executed.", exception);
			}
		}
		return CODE_NOUSER;
	}
	
	private void updateBalance(@NotNull UUID uuid, double value)
	{
		try
		{
			PreparedStatement ps = databaseManager.prepareStatement("UPDATE %s SET %s = ? WHERE %s = ?", TABLE, FIELD_BALANCE, FIELD_UUID);
			ps.setDouble(1, value);
			ps.setString(2, uuid.toString());
			ps.executeUpdate();
		}
		catch (SQLException exception) 
		{
			Bukkit.getLogger().log(Level.SEVERE	, "Statement cannot be executed.", exception);
		}
	}
	
	public int setBalance(@NotNull UUID uuid, double value)
	{
		if (userExist(uuid))
		{
			updateBalance(uuid, value);
			return CODE_SUCCESS;
		}
		return CODE_NOUSER;
	}
	
	public int updateBalance(@NotNull UUID uuid, double value, boolean remove, boolean withWallet)
	{
		double currentBalance = getBalance(uuid);
		
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
		updateBalance(uuid, currentBalance + (remove ? value * -1 : value));	//USER NOT EXISTS, NOT NOUGH VALUE 
		return CODE_SUCCESS;
	}
	
	public double getWallet(@NotNull UUID uuid)
	{
		if (userExist(uuid))
		{
			try
			{
				PreparedStatement ps = databaseManager.prepareStatement("SELECT %s FROM %s WHERE %s = ?", FIELD_WALLET, TABLE, FIELD_UUID);
				ps.setString(1, uuid.toString());
							
				try (ResultSet rs = ps.executeQuery())
				{
					while (rs.next()) 
					{
						return rs.getDouble(FIELD_WALLET);
					}
				}
			}
			catch (SQLException exception) 
			{
				Bukkit.getLogger().log(Level.SEVERE	, "Statement cannot be executed.", exception);
			}
		}
		return -1;
	}
	
	public void updateWallet(@NotNull UUID uuid, double value)
	{
		try
		{
			PreparedStatement ps = databaseManager.prepareStatement("UPDATE %s SET %s = ? WHERE %s = ?", TABLE, FIELD_WALLET, FIELD_UUID);
			ps.setDouble(1, value);
			ps.setString(2, uuid.toString());
			ps.executeUpdate();
		}
		catch (SQLException exception) 
		{
			Bukkit.getLogger().log(Level.SEVERE	, "Statement cannot be executed.", exception);
		}
	}
	
	public int setWallet(@NotNull UUID uuid, double value)
	{
		if (userExist(uuid))
		{
			updateWallet(uuid, value);
			return CODE_SUCCESS;
		}
		return CODE_NOUSER;
	}
	
	public int updateWallet(@NotNull UUID uuid, double value, boolean remove)
	{
		double currentWallet = getWallet(uuid);
		
		if (currentWallet == -1)
		{
			return CODE_NOUSER;
		}
		if (remove && currentWallet < value)
		{
			return CODE_NOTENOUGHVALUE;
		}
		updateWallet(uuid, currentWallet + (remove ? value * -1 : value));	
		return CODE_SUCCESS;
	}
	
	@NotNull
	public List<String> getEconomyOverview()
	{
		List<String> economyOverview = new ArrayList<>();
		
		try
		{
			PreparedStatement ps = databaseManager.prepareStatement("SELECT %1$s,%2$s,%3$s,(%2$s + %3$s) AS %4$s FROM %5$s ORDER BY %4$s DESC", FIELD_UUID, FIELD_BALANCE, FIELD_WALLET, FIELD_SUM, TABLE);			
			
			try (ResultSet rs = ps.executeQuery())
			{		
				int position = 1;
				
				while (rs.next()) 
				{
					Player player = Bukkit.getPlayer(UUID.fromString(rs.getString(FIELD_UUID)));
					
					if (player != null)
					{
						double balance = rs.getDouble(FIELD_BALANCE);
						double wallet = rs.getDouble(FIELD_WALLET);
						double sum = rs.getDouble(FIELD_SUM);
						
						economyOverview.add(String.format("§8☰ §a%d §8- §9%s\n" +
								                          "§8☷ §7Gesamt: §9%.0f §8× §7Konto: §9%.0f §8× §7Geldbeutel: §9%.0f", position, player.getName(), sum, balance, wallet));
						position++;
					}
				}
			}
		}
		catch (SQLException exception) 
		{
			Bukkit.getLogger().log(Level.SEVERE	, "Statement cannot be executed.", exception);
		}	
		return economyOverview;
	}
}
