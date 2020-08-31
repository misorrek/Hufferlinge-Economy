package huff.economy;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import huff.lib.manager.DatabaseManager;

public class EconomyTable
{
	public EconomyTable(@NotNull  DatabaseManager databaseManager)
	{
		Validate.notNull((Object) databaseManager, "The database-manager cannot be null.");
		
		this.databaseManager = databaseManager;
	}
	
	private DatabaseManager databaseManager;
	
	private void checkTable()
	{
		try 
		{
			PreparedStatement ps = databaseManager.prepareStatement("", null);
			ps.executeUpdate();
		} 
		catch (SQLException exception) 
		{
			Bukkit.getLogger().log(Level.SEVERE	, "Economy-table cannot be created.", exception);
		}
	}
}
