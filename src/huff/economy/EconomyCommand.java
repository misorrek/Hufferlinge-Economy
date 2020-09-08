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
import huff.lib.helper.StringHelper;
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
		
		if (args.length == 1)
		{
			if (args[0].equalsIgnoreCase("list"))
			{
				executeList(sender);
				return true;
			}
		}
		else if (args.length >= 2)
		{
			if (args[0].equalsIgnoreCase("balance"))
			{
				return executeValueAction(sender, args, true);
			}
			else if (args[0].equalsIgnoreCase("wallet"))
			{
				return executeValueAction(sender, args, false);
			}
		}		
		sender.sendMessage(MessageHelper.getWrongInput("/" + cmd.getName() + "\n" +
				                                       "§8☰ list\n" + 
				                                       "§8☰ balance [show|set|add|remove] <value> (<player>)\n" + 
				                                       "§8☰ wallet [show|set|add|remove] <value> (<player>)"));
		return false;
	}
	
	private void executeList(CommandSender sender)
	{
		List<String> economyOverview = economyTable.getEconomyOverview();
		
		if (!economyOverview.isEmpty())
		{
			sender.sendMessage("§8☰ §7Übersicht über die Kontostände aller Spieler");
			sender.sendMessage("");
			
			for (String economyEntry : economyOverview)
			{
				sender.sendMessage(economyEntry);
			}	
		}
		else
		{
			sender.sendMessage("§8☰ §7Keine Spieler zur Übersicht vorhanden");
		}
	}
	
	private boolean executeValueAction(CommandSender sender, String[] args, boolean isBalance)
	{
		switch (args[1].toLowerCase())
		{
		case "show":
			executeShow(sender, args, isBalance);	
			return true;
		case "set":
			executeSet(sender, args, isBalance);
			return true;
		case "add":
			executeAdd(sender, args, isBalance);
			return true;
		case "remove":
			executeRemove(sender, args, isBalance);
			return true;
		default:
			return false;
		}
	}
	
	private void executeShow(CommandSender sender, String[] args, boolean isBalance)
	{
		if (args.length >= 3)
		{			
			final String targetPlayerName = args[2]; 
			final Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
			
			if (targetPlayer == null)
			{
				sender.sendMessage(MessageHelper.getPlayerNotFound(targetPlayerName));
				return;
			}
			double value = isBalance ? economyTable.getBalance(targetPlayer.getUniqueId()) : economyTable.getWallet(targetPlayer.getUniqueId());
			int feedbackCode = value >= 0 ? EconomyTable.CODE_SUCCESS : EconomyTable.CODE_USERNOTEXIST;
			
			sender.sendMessage(processFeedbackCode(feedbackCode, value, isBalance, false, targetPlayerName, null));
		}
		else if (sender instanceof Player)
		{
			double value = isBalance ? economyTable.getBalance(((Player) sender).getUniqueId()) : economyTable.getWallet(((Player) sender).getUniqueId());
			int feedbackCode = value >= 0 ? EconomyTable.CODE_SUCCESS : EconomyTable.CODE_USERNOTEXIST;
			
			sender.sendMessage(processFeedbackCode(feedbackCode, value, isBalance, false, null, null));
		}
		else
		{
			sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du kannst diesen Befehl nicht auf dich selbst aufrufen.");
		}
	}
	
	private void executeSet(CommandSender sender, String[] args, boolean isBalance)
	{
		final double value = parseDoubleInput(sender, args[2]);			
		
		if (value == -1)
		{
			return;
		}	
		
		if (args.length >= 4)
		{			
			final String targetPlayerName = args[3]; 
			final Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
			
			if (targetPlayer == null)
			{
				sender.sendMessage(MessageHelper.getPlayerNotFound(targetPlayerName));
				return;
			}
			int feedbackCode = isBalance ? economyTable.setBalance(targetPlayer.getUniqueId(), value) : economyTable.setWallet(targetPlayer.getUniqueId(), value);
			
			sender.sendMessage(processFeedbackCode(feedbackCode, value, isBalance, false, targetPlayerName, null));
		}
		else if (sender instanceof Player)
		{
			int feedbackCode = isBalance ? economyTable.setBalance(((Player) sender).getUniqueId(), value) : economyTable.setWallet(((Player) sender).getUniqueId(), value);
			
			sender.sendMessage(processFeedbackCode(feedbackCode, value, isBalance, false, null, null));
		}
		else
		{
			sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du kannst diesen Befehl nicht auf dich selbst aufrufen.");
		}
	}
	
	private void executeAdd(CommandSender sender, String[] args, boolean isBalance) //Redice
	{
		final double value = parseDoubleInput(sender, args[2]);			
		
		if (value == -1)
		{
			return;
		}	
		
		if (args.length >= 4)
		{			
			final String targetPlayerName = args[3]; 
			final Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
			
			if (targetPlayer == null)
			{
				sender.sendMessage(MessageHelper.getPlayerNotFound(targetPlayerName));
				return;
			}
			final UUID uuid = targetPlayer.getUniqueId();
			final int feedbackCode = (isBalance ? economyTable.updateBalance(uuid, value, false, false) : economyTable.updateWallet(uuid, value, false));
			
			sender.sendMessage(processFeedbackCode(feedbackCode, value, isBalance, false, targetPlayerName, uuid));
		}
		else if (sender instanceof Player)
		{
			final UUID uuid = ((Player) sender).getUniqueId();
			final int feedbackCode = (isBalance ? economyTable.updateBalance(uuid, value, false, false) : economyTable.updateWallet(uuid, value, false));
			
			sender.sendMessage(processFeedbackCode(feedbackCode, value, isBalance, false, null, uuid));
		}
		else
		{
			sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du kannst diesen Befehl nicht auf dich selbst aufrufen.");
		}
	}
	
	private void executeRemove(CommandSender sender, String[] args, boolean isBalance) 
	{
		final double value = parseDoubleInput(sender, args[2]);			
		
		if (value == -1)
		{
			return;
		}	
		
		if (args.length >= 4)
		{			
			final String targetPlayerName = args[3]; 
			Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
			
			if (targetPlayer == null)
			{
				sender.sendMessage(MessageHelper.getPlayerNotFound(targetPlayerName));
				return;
			}
			final UUID uuid = targetPlayer.getUniqueId();
			final int feedbackCode = (isBalance ? economyTable.updateBalance(uuid, value, true, false) : economyTable.updateWallet(uuid, value, true));
			
			sender.sendMessage(processFeedbackCode(feedbackCode, value, isBalance, true, targetPlayerName, uuid));
		}
		else if (sender instanceof Player)
		{
			final UUID uuid = ((Player) sender).getUniqueId();
			final int feedbackCode = (isBalance ? economyTable.updateBalance(uuid, value, true, false) : economyTable.updateWallet(uuid, value, true));
			
			sender.sendMessage(processFeedbackCode(feedbackCode, value, isBalance, true, null, uuid));
		}
		else
		{
			sender.sendMessage(MessageHelper.PREFIX_HUFF + "Du kannst diesen Befehl nicht auf dich selbst aufrufen.");
		}
	}
	
	private double parseDoubleInput(CommandSender sender, String input)
	{
		try 
		{
			double parsedValue =  Double.parseDouble(input);
			
			if (parsedValue < 0)
			{
				sender.sendMessage(MessageHelper.PREFIX_HUFF + "Der eingegebene Wert darf nicht negativ sein.");
				return -1;
			}		
			return parsedValue;
		}
		catch (NumberFormatException execption)
		{
			sender.sendMessage(MessageHelper.PREFIX_HUFF + "Der eingegebene Wert ist ungültig.");
		}
		return -1;
	}
	
	private String processFeedbackCode(int code, double value, boolean isBalance, boolean withRemove, String playerName, UUID playerUUID)
	{
		StringBuilder messageBuilder = new StringBuilder();
		boolean selfPerform = StringHelper.isNullOrEmpty(playerName);
		boolean updatedPerform = playerUUID != null;
		
		messageBuilder.append(MessageHelper.PREFIX_HUFF);
		
		if (selfPerform)
		{
			messageBuilder.append("Du ");
		}
		else
		{
			messageBuilder.append("Der Spieler");
			messageBuilder.append(MessageHelper.getHighlighted(playerName));
		}
		
		switch (code)
		{
		case EconomyTable.CODE_USERNOTEXIST:
			messageBuilder.append(selfPerform ? "bist " : "ist ");
			messageBuilder.append("nicht in der Economy-Datenbank vorhanden.");
			break;
		case EconomyTable.CODE_NOTENOUGHVALUE:
			messageBuilder.append(selfPerform ? "hast " : "hat ");
			messageBuilder.append("dazu nicht genug ");
			messageBuilder.append(isBalance ? "auf der Bank." : "im Geldbeutel.");
			break;
		case EconomyTable.CODE_SUCCESS:
			messageBuilder.append(selfPerform ? "hast " : "hat ");
			messageBuilder.append(updatedPerform ? "nun " : "");
			messageBuilder.append(MessageHelper.getHighlighted(economyConfig.getValueFormatted(value), false, true));
			if (updatedPerform) messageBuilder.append(withRemove ? "weniger " : "mehr ");
			messageBuilder.append(isBalance ? "auf der Bank." : "im Geldbeutel.");
			if (updatedPerform)
			{
				messageBuilder.append("\n");
				messageBuilder.append(MessageHelper.PREFIX_HUFF);
				messageBuilder.append("Der neue Stand beträgt");
				messageBuilder.append(MessageHelper.getHighlighted(economyConfig.getValueFormatted(isBalance ? economyTable.getBalance(playerUUID) : economyTable.getWallet(playerUUID)), true, false));
				messageBuilder.append(".");
			}
			break;
		default:
			return MessageHelper.PREFIX_HUFF + "Ungültiger Datenbank-Rückgabecode" + MessageHelper.getQuoted(Integer.toString(code), true, false) + ".";
		}		
		return messageBuilder.toString();
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
		else if (StringHelper.isIn(true, args[0], "balance", "wallet"))
		{
			if (args.length == 2)
			{
				paramSuggestions.add("show");
				paramSuggestions.add("set");
				paramSuggestions.add("add");
				paramSuggestions.add("remove");
			}
			else if ((args.length == 3 && args[1].equalsIgnoreCase("show")) ||
					 (args.length == 4 && StringHelper.isIn(true, args[1], "set", "add", "remove")))
			{
				for (Player publicPlayer : Bukkit.getOnlinePlayers())
				{
					paramSuggestions.add(publicPlayer.getName());
				}
				paramSuggestions.sort(new AlphanumericComparator());
			}
		}
		return paramSuggestions;
	}
}
