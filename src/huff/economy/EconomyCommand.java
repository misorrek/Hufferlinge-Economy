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
import org.jetbrains.annotations.Nullable;

import huff.lib.helper.MessageHelper;
import huff.lib.helper.PermissionHelper;
import huff.lib.helper.StringHelper;
import huff.lib.various.AlphanumericComparator;

public class EconomyCommand implements CommandExecutor, TabCompleter
{
	private static final String PERM_ECONOMY = PermissionHelper.PERM_ROOT_HUFF + "economy";
	
	public EconomyCommand(@NotNull EconomyStorage economyStorage, @NotNull EconomyConfig economyConfig)
	{
		Validate.notNull((Object) economyStorage, "The economy-table cannot be null.");
		Validate.notNull((Object) economyConfig, "The economy-config cannot be null.");
		
		this.economyStorage = economyStorage;
		this.economyConfig = economyConfig;
	}
	private final EconomyStorage economyStorage;
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
		sender.sendMessage(MessageHelper.getWrongInput(StringHelper.build("/", cmd.getName(), "\n",
				                                       "§8☰ list\n",
				                                       "§8☰ balance [show|set|add|remove] <value> (<player>)\n", 
				                                       "§8☰ wallet [show|set|add|remove] <value> (<player>)")));
		return false;
	}
	
	private void executeList(CommandSender sender)
	{
		final List<String> economyOverview = economyStorage.getEconomyOverview();
		
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
			executeUpdate(sender, args, isBalance, false);
			return true;
		case "remove":
			executeUpdate(sender, args, isBalance, true);
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
			sender.sendMessage(processGetValue(isBalance, targetPlayer.getUniqueId(), targetPlayerName));
		}
		else if (sender instanceof Player)
		{
			sender.sendMessage(processGetValue(isBalance, ((Player) sender).getUniqueId(), null));
		}
		else
		{
			sender.sendMessage(getInvalidSenderMessage());
		}
	}
	
	private @NotNull String processGetValue(boolean isBalance, @NotNull UUID targetUUID, @Nullable String targetName)
	{
		final double value = isBalance ? economyStorage.getBalance(targetUUID) : economyStorage.getWallet(targetUUID);
		final int feedbackCode = value >= 0 ? EconomyStorage.CODE_SUCCESS : EconomyStorage.CODE_NOUSER;
		
		return processFeedbackCode(feedbackCode, value, isBalance, false, targetName, null);
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
			sender.sendMessage(procesSetValue(isBalance, value, targetPlayer.getUniqueId(), targetPlayerName));
		}
		else if (sender instanceof Player)
		{
			sender.sendMessage(procesSetValue(isBalance, value, ((Player) sender).getUniqueId(), null));
		}
		else
		{
			sender.sendMessage(getInvalidSenderMessage());
		}
	}
	
	private @NotNull String procesSetValue(boolean isBalance, double value, @NotNull UUID targetUUID, @Nullable String targetName)
	{
		final int feedbackCode = isBalance ? economyStorage.setBalance(targetUUID, value) : economyStorage.setWallet(targetUUID, value);
		
		return processFeedbackCode(feedbackCode, value, isBalance, false, targetName, null); 
	}
	
	private void executeUpdate(CommandSender sender, String[] args, boolean isBalance, boolean isRemove) 
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
			sender.sendMessage(processUpdateValue(isBalance, isRemove, value, targetPlayer.getUniqueId(), targetPlayerName));
		}
		else if (sender instanceof Player)
		{
			sender.sendMessage(processUpdateValue(isBalance, isRemove, value, ((Player) sender).getUniqueId(), null));
		}
		else
		{
			sender.sendMessage(getInvalidSenderMessage());
		}
	}
	
	private @NotNull String processUpdateValue(boolean isBalance, boolean isRemove, double value, @NotNull UUID targetUUID, @Nullable String targetName)
	{
		final int feedbackCode = (isBalance ? economyStorage.updateBalance(targetUUID, value, isRemove, false) : economyStorage.updateWallet(targetUUID, value, isRemove));
		
		return processFeedbackCode(feedbackCode, value, isBalance, isRemove, targetName, targetUUID);
	}
	
	private double parseDoubleInput(CommandSender sender, String input)
	{
		try 
		{
			final double parsedValue =  Double.parseDouble(input);
			
			if (parsedValue < 0)
			{
				sender.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Der eingegebene Wert darf nicht negativ sein."));
				return -1;
			}		
			return parsedValue;
		}
		catch (NumberFormatException execption)
		{
			sender.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Der eingegebene Wert ist ungültig."));
		}
		return -1;
	}
	
	private String processFeedbackCode(int code, double value, boolean isBalance, boolean withRemove, String playerName, UUID playerUUID)
	{
		final StringBuilder messageBuilder = new StringBuilder();
		final boolean selfPerform = StringHelper.isNullOrEmpty(playerName);
		final boolean updatedPerform = playerUUID != null;
		
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
		case EconomyStorage.CODE_NOUSER:
			messageBuilder.append(selfPerform ? "bist " : "ist ");
			messageBuilder.append("nicht in der Economy-Datenbank vorhanden.");
			break;
		case EconomyStorage.CODE_NOTENOUGHVALUE:
			messageBuilder.append(selfPerform ? "hast " : "hat ");
			messageBuilder.append("dazu nicht genug ");
			messageBuilder.append(isBalance ? "auf der Bank." : "im Geldbeutel.");
			break;
		case EconomyStorage.CODE_SUCCESS:
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
				messageBuilder.append(MessageHelper.getHighlighted(economyConfig.getValueFormatted(isBalance ? economyStorage.getBalance(playerUUID) : economyStorage.getWallet(playerUUID)), true, false));
				messageBuilder.append(".");
			}
			break;
		default:
			return MessageHelper.PREFIX_HUFF + "Ungültiger Datenbank-Rückgabecode" + MessageHelper.getQuoted(Integer.toString(code), true, false) + ".";
		}		
		return messageBuilder.toString();
	}

	private @NotNull String getInvalidSenderMessage()
	{
		return StringHelper.build(MessageHelper.PREFIX_HUFF, "Du kannst diesen Befehl nicht auf dich selbst aufrufen.");
	}
	
	// T A B C O M P L E T E
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		final List<String> paramSuggestions = new ArrayList<>();
		
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
