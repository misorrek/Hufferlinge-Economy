package huff.economy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import huff.lib.helper.MessageHelper;
import huff.lib.helper.PermissionHelper;
import huff.lib.various.AlphanumericComparator;

public class EconomyCommand implements CommandExecutor, TabCompleter
{
	private static final String PERM_ECONOMY = PermissionHelper.PERM_ROOT_HUFF + "economy";
	
	public EconomyCommand(@NotNull EconomyTable economyTable, @NotNull EconomyConfig economyConfig)
	{
		Validate.notNull((Object) economyTable, "The economy-table cannot be null.");
		Validate.notNull((Object) economyConfig, "The economy-config cannot be null.");
		
		this.economyTable = economyTable;
		this.economyConfig = economyConfig;
	}
	
	private final EconomyTable economyTable;
	private final EconomyConfig economyConfig;
	
	// C O M M A N D

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (sender instanceof Player && !PermissionHelper.hasPlayerPermissionFeedbacked((Player) sender, PERM_ECONOMY))
		{
			return false;
		}
		
		if (args.length >= 1)
		{
			if (args[0].equalsIgnoreCase("list"))
			{
				return executeList(sender);
			}
			else if (args[0].equalsIgnoreCase("balance"))
			{
				if (args.length >= 2)
				{
					executeValueAction(sender, args, true);
				}
			}
			else if (args[0].equalsIgnoreCase("wallet"))
			{
				if (args.length >= 2)
				{
					executeValueAction(sender, args, false);
				}
			}
		}		
		sender.sendMessage(MessageHelper.getWrongInput("/"+ cmd.getName() + " [list|balance [show|set|add|remove] <value> (<player>)|wallet [show|set|add|remove] <value> (<player>)"));
		return false;
	}
	
	private boolean executeList(CommandSender sender)
	{
		sender.sendMessage("§8☰ §7Übersicht über die Kontostände aller Spieler");
		sender.sendMessage("");
		
		for (String economyEntry : economyTable.getEconomyOverview())
		{
			sender.sendMessage(economyEntry);
		}	
		return true;
	}
	
	private boolean executeValueAction(CommandSender sender, String[] args, boolean isBalance)
	{
		final String action = args[1];
		
		if (action.equalsIgnoreCase("show"))
		{
			return executeShow(sender, args, isBalance);
		}
		else if (action.equalsIgnoreCase("set"))
		{
			return executeSet(sender, args, isBalance);
		}
		else if (action.equalsIgnoreCase("add"))
		{
			return executeAdd(sender, args, isBalance);
		}
		else if (action.equalsIgnoreCase("remove"))
		{
			return executeRemove(sender, args, isBalance);
		}
		return false;
	}
	
	private boolean executeShow(CommandSender sender, String[] args, boolean isBalance)
	{
		if (args.length >= 3)
		{			
			final String targetPlayerName = args[3]; 
			Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
			
			if (targetPlayer == null)
			{
				sender.sendMessage(MessageHelper.getPlayerNotFound(targetPlayerName));
			}
			double value = isBalance ? economyTable.getBalance(((Player) sender).getUniqueId()) : economyTable.getWallet(((Player) sender).getUniqueId());
			
			if (value == EconomyTable.CODE_USERNOTEXIST)
			{
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Der Spieler" + MessageHelper.getQuoted(targetPlayerName) + "ist nicht in der Economy-Datenbank vorhanden.");
			}	
			else
			{
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Der Spieler" + MessageHelper.getQuoted(targetPlayerName) + "hat" + MessageHelper.getHighlighted(economyConfig.getValueFormatted(value)) + 
						           (isBalance ? "auf der Bank." : "im Geldbeutel"));
			}
		}
		else if (sender instanceof Player)
		{
			double value = isBalance ? economyTable.getBalance(((Player) sender).getUniqueId()) : economyTable.getWallet(((Player) sender).getUniqueId());
			
			if (value == EconomyTable.CODE_USERNOTEXIST)
			{
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du bist nicht in der Economy-Datenbank vorhanden.");
			}	
			else
			{
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du hast" + MessageHelper.getHighlighted(economyConfig.getValueFormatted(value)) + (isBalance ? "auf der Bank." : "im Geldbeutel"));
			}
		}
		sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du kannst diesen Befehl nicht auf dich selbst aufrufen.");
		return false;
	}
	
	private boolean executeSet(CommandSender sender, String[] args, boolean isBalance)
	{
		final double value = parseDoubleInput(sender, args[2]);			
		
		if (value == -1)
		{
			return false;
		}	
		
		if (args.length >= 4)
		{			
			final String targetPlayerName = args[3]; 
			Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
			
			if (targetPlayer == null)
			{
				sender.sendMessage(MessageHelper.getPlayerNotFound(targetPlayerName));
			}
			
			if (isBalance ? economyTable.setBalance(((Player) sender).getUniqueId(), value) : economyTable.setWallet(((Player) sender).getUniqueId(), value))
			{
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Der Spieler" + MessageHelper.getQuoted(targetPlayerName) + "hat nun" + MessageHelper.getHighlighted(economyConfig.getValueFormatted(value)) + 
				           (isBalance ? "auf der Bank." : "im Geldbeutel"));
			}
			else
			{
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Der Spieler" + MessageHelper.getQuoted(targetPlayerName) + "ist nicht in der Economy-Datenbank vorhanden.");
			}
		}
		else if (sender instanceof Player)
		{
			if (isBalance ? economyTable.setBalance(((Player) sender).getUniqueId(), value) : economyTable.setWallet(((Player) sender).getUniqueId(), value))
			{
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du hast nun" + MessageHelper.getHighlighted(economyConfig.getValueFormatted(value)) + 
				           (isBalance ? "auf der Bank." : "im Geldbeutel"));
			}
			else
			{
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du bist nicht in der Economy-Datenbank vorhanden.");
			}
		}
		sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du kannst diesen Befehl nicht auf dich selbst aufrufen.");
		return false;
	}
	
	private boolean executeAdd(CommandSender sender, String[] args, boolean isBalance) //Redice
	{
		final double value = parseDoubleInput(sender, args[2]);			
		
		if (value == -1)
		{
			return false;
		}	
		
		if (args.length >= 4)
		{			
			final String targetPlayerName = args[3]; 
			Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
			
			if (targetPlayer == null)
			{
				sender.sendMessage(MessageHelper.getPlayerNotFound(targetPlayerName));
			}
			final UUID uuid = targetPlayer.getUniqueId();
			final int feedbackCode = (isBalance ? economyTable.updateBalance(uuid, value, false, false) : economyTable.updateWallet(uuid, value, false));
			
			if (feedbackCode == EconomyTable.CODE_USERNOTEXIST)
			{
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Der Spieler" + MessageHelper.getQuoted(targetPlayerName) + "ist nicht in der Economy-Datenbank vorhanden.");
			}
			else
			{
				double updatedValue = economyTable.getBalance(((Player) sender).getUniqueId());
				
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du hast dem Spieler" + MessageHelper.getQuoted(targetPlayerName) + 
						           MessageHelper.getHighlighted(economyConfig.getValueFormatted(value)) + "hinzugefügt.");
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Er hat nun" + MessageHelper.getHighlighted(economyConfig.getValueFormatted(updatedValue)) + 
				                   (isBalance ? "auf der Bank." : "im Geldbeutel"));
			}
		}
		else if (sender instanceof Player)
		{
			final UUID uuid = ((Player) sender).getUniqueId();
			final int feedbackCode = (isBalance ? economyTable.updateBalance(uuid, value, false, false) : economyTable.updateWallet(uuid, value, false));
			
			if (feedbackCode == EconomyTable.CODE_USERNOTEXIST)
			{
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du bist nicht in der Economy-Datenbank vorhanden.");
			}
			else
			{
				double updatedValue = economyTable.getBalance(((Player) sender).getUniqueId());
				
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du hast dir" + MessageHelper.getHighlighted(economyConfig.getValueFormatted(value)) + "hinzugefügt.");
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du hast nun" + MessageHelper.getHighlighted(economyConfig.getValueFormatted(updatedValue)) + 
				                   (isBalance ? "auf der Bank." : "im Geldbeutel"));
			}
		}
		sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du kannst diesen Befehl nicht auf dich selbst aufrufen.");
		return false;
	}
	
	private boolean executeRemove(CommandSender sender, String[] args, boolean isBalance) //Redice
	{
		final double value = parseDoubleInput(sender, args[2]);			
		
		if (value == -1)
		{
			return false;
		}	
		
		if (args.length >= 4)
		{			
			final String targetPlayerName = args[3]; 
			Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
			
			if (targetPlayer == null)
			{
				sender.sendMessage(MessageHelper.getPlayerNotFound(targetPlayerName));
			}
			final UUID uuid = targetPlayer.getUniqueId();
			final int feedbackCode = (isBalance ? economyTable.updateBalance(uuid, value, true, false) : economyTable.updateWallet(uuid, value, true));
			
			if (feedbackCode == EconomyTable.CODE_USERNOTEXIST)
			{
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Der Spieler" + MessageHelper.getQuoted(targetPlayerName) + "ist nicht in der Economy-Datenbank vorhanden.");
			}
			if (feedbackCode == EconomyTable.CODE_NOTENOUGHVALUE)
			{
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du hast dazu nicht genug " + (isBalance ? "auf der Bank." : "im Geldbeutel"));
			}
			else
			{
				double updatedValue = economyTable.getBalance(((Player) sender).getUniqueId());
				
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du hast dem Spieler" + MessageHelper.getQuoted(targetPlayerName) + 
						           MessageHelper.getHighlighted(economyConfig.getValueFormatted(value)) + "entfernt.");
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Er hat nun" + MessageHelper.getHighlighted(economyConfig.getValueFormatted(updatedValue)) + 
				                   (isBalance ? "auf der Bank." : "im Geldbeutel"));
			}
		}
		else if (sender instanceof Player)
		{
			final UUID uuid = ((Player) sender).getUniqueId();
			final int feedbackCode = (isBalance ? economyTable.updateBalance(uuid, value, true, false) : economyTable.updateWallet(uuid, value, true));
			
			if (feedbackCode == EconomyTable.CODE_USERNOTEXIST)
			{
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du bist nicht in der Economy-Datenbank vorhanden.");
			}
			if (feedbackCode == EconomyTable.CODE_NOTENOUGHVALUE)
			{
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du hast dazu nicht genug " + (isBalance ? "auf der Bank." : "im Geldbeutel"));
			}
			else
			{
				double updatedValue = economyTable.getBalance(((Player) sender).getUniqueId());
				
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du hast dir" + MessageHelper.getHighlighted(economyConfig.getValueFormatted(value)) + "entfernt.");
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du hast nun" + MessageHelper.getHighlighted(economyConfig.getValueFormatted(updatedValue)) + 
				                   (isBalance ? "auf der Bank." : "im Geldbeutel"));
			}
		}
		sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du kannst diesen Befehl nicht auf dich selbst aufrufen.");
		return false;
	}
	
	private double parseDoubleInput(CommandSender sender, String input)
	{
		try 
		{
			return Double.parseDouble(input);
		}
		catch (NumberFormatException execption)
		{
			sender.sendMessage(MessageHelper.PREFIX_HUFF + "Der eingegebene Wert ist ungültig.");
		}
		return -1;
	}

	// T A B C O M P L E T E
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		List<String> paramSuggestions = new ArrayList<>();
		
		if (!(sender instanceof Player) || !PermissionHelper.hasPlayerPermission((Player) sender, PERM_ECONOMY)) 
		{
			return paramSuggestions;
		}	
		
		if (args.length == 1)
		{
			paramSuggestions.add("list");
			paramSuggestions.add("balance");
			paramSuggestions.add("wallet");
		}
		else if (args.length == 2)
		{
			if (args[0] == "list")
			{
				fillParamSuggestionsWithPlayers(paramSuggestions);			
			}
			else if (args[0] == "balance" || args[0] == "wallet")
			{
				paramSuggestions.add("show");
				paramSuggestions.add("set");
				paramSuggestions.add("add");
				paramSuggestions.add("remove");
			}
		}
		else if (args.length == 4)
		{
			if (args[0] == "balance" || args[0] == "wallet")
			{
				fillParamSuggestionsWithPlayers(paramSuggestions);
			}
		}
		return paramSuggestions;
	}
	
	private void fillParamSuggestionsWithPlayers(List<String> paramSuggestions)
	{
		for (Player publicPlayer : Bukkit.getOnlinePlayers())
		{
			paramSuggestions.add(publicPlayer.getName());
		}
		paramSuggestions.sort(new AlphanumericComparator());
	}
}
