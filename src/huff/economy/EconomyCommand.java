package huff.economy;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import huff.lib.various.AlphanumericComparator;

public class EconomyCommand implements CommandExecutor, TabCompleter
{
	// C O M M A N D

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		
		
		return false;
	}
	
	// T A B C O M P L E T E
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		List<String> paramSuggestions = new ArrayList<>();
		
		paramSuggestions.sort(new AlphanumericComparator());
		return paramSuggestions;
	}
}
