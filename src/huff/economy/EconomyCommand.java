package huff.economy;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;

import huff.economy.storage.Bank;
import huff.economy.storage.Storage;
import huff.lib.helper.PermissionHelper;
import huff.lib.helper.StringHelper;
import huff.lib.helper.UserHelper;
import huff.lib.various.HuffCommand;
import huff.lib.various.LibMessage;
import huff.lib.various.structures.StringPair;

public class EconomyCommand extends HuffCommand
{
	private static final int LISTENTRIES_PER_PAGE = 10;
	
	public EconomyCommand(@NotNull EconomyInterface economy)
	{
		super(economy.getPlugin(), "economy");
		
		Validate.notNull((Object) economy, "The economy-interface cannot be null.");
		
		this.economy = economy;
		super.setDescription("Hufferlinge Economy Verwaltung");
		super.setUsage(StringHelper.build("\n \n§8☷ §7/economy\n",
						                 "§8☷ §7list <Seite>\n",
						                 "§8☷ §7balance [show|set|add|remove] (<Wert>) (<Spieler>)\n", 
						                 "§8☷ §7wallet [show|set|add|remove] (<Wert>) (<Spieler>)\n",
						                 "§8☷ §7bank [show|item|add|remove] (<Seite>)"));
		super.setAliases("huffeconomy", "huffconomy", "money");
		super.setPermission(PermissionHelper.PERM_ROOT_HUFF + "economy");
		addTabCompletion();
		super.registerCommand();
	}

	private final EconomyInterface economy;
	
	// C O M M A N D

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (args.length > 0)
		{
			final String firstArgument = args[0];
			
			if (args.length >= 2)
			{
				if (firstArgument.equalsIgnoreCase("list"))
				{
					executeList(sender, args);
					return true;
				}
				else if (firstArgument.equalsIgnoreCase("balance"))
				{
					return executeValueAction(sender, args, true);
				}
				else if (firstArgument.equalsIgnoreCase("wallet"))
				{
					return executeValueAction(sender, args, false);
				}
				else if (firstArgument.equalsIgnoreCase("bank"))
				{
					return executeBankAction(sender, args);
				}
			}	
		}	
		return false;
	}
	
	// L I S T
	
	private void executeList(CommandSender sender, String[] args)
	{
		final int maxPage = getListPageCount(economy.getStorage().getUserCount());
		final int page = getListPage(sender, args[1], maxPage);
		
		if (page == -1)
		{
			return;
		}		
		final List<String> economyOverview = economy.getStorage().getEconomyOverview((page - 1) * LISTENTRIES_PER_PAGE, page * LISTENTRIES_PER_PAGE);
		
		if (!economyOverview.isEmpty())
		{
			sender.sendMessage(EconomyMessage.LIST_HEADER.getMessage(new StringPair("page", Integer.toString(page)), new StringPair("maxpage", Integer.toString(maxPage))));
			sender.sendMessage("");
			
			for (String economyEntry : economyOverview)
			{
			
			
				sender.sendMessage(economyEntry);
			}	
		}
		else
		{
			sender.sendMessage(EconomyMessage.LIST_NODATA.getMessage());
		}
	}
	
	private int getListPage(CommandSender sender, String input, int maxPage)
	{
		try 
		{
			final int page = Integer.parseInt(input);
			
			if (page < 1 || page > maxPage)
			{
				sender.sendMessage(EconomyMessage.INVALIDPAGE.getMessage(new StringPair("maxpage", Integer.toString(maxPage))));
				return -1;
			}
			return page;
		}
		catch (NumberFormatException exception)
		{
			sender.sendMessage(EconomyMessage.INVALIDNUMBER.getMessage(new StringPair("text", input)));
		}	
		return -1;
	}
	
	private int getListPageCount(int count)
	{
		return (int) Math.ceil((double) count / LISTENTRIES_PER_PAGE);
	}
	
	// V A L U E
	
	private boolean executeValueAction(CommandSender sender, String[] args, boolean isBalance)
	{
		switch (args[1].toLowerCase())
		{
		case "show":
			executeValueShow(sender, args, isBalance);	
			return true;
		case "set":
			executeValueSet(sender, args, isBalance);
			return true;
		case "add":
			executeValueUpdate(sender, args, isBalance, false);
			return true;
		case "remove":
			executeValueUpdate(sender, args, isBalance, true);
			return true;
		default:
			return false;
		}
	}
	
	private void executeValueShow(CommandSender sender, String[] args, boolean isBalance)
	{
		if (args.length >= 3)
		{			
			final String targetPlayerName = args[2]; 
			final UUID targetPlayer = UserHelper.getUniqueId(targetPlayerName);
			
			if (targetPlayer == null)
			{
				sender.sendMessage(LibMessage.NOTFOUND.getMessage(new StringPair("user", targetPlayerName)));
				return;
			}
			sender.sendMessage(processGetValue(isBalance, targetPlayer, targetPlayerName));
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
		final double value = isBalance ? economy.getStorage().getBalance(targetUUID) : economy.getStorage().getWallet(targetUUID);
		final int feedbackCode = value >= 0 ? Storage.CODE_SUCCESS : Storage.CODE_NOUSER;
		
		return processFeedbackCode(feedbackCode, value, isBalance, false, false, targetName, null);
	}
	
	private void executeValueSet(CommandSender sender, String[] args, boolean isBalance)
	{
		final double value = parseDoubleInput(sender, args[2]);			
		
		if (value == -1)
		{
			return;
		}	
		
		if (args.length >= 4)
		{			
			final String targetPlayerName = args[3]; 
			final UUID targetPlayer = UserHelper.getUniqueId(targetPlayerName);
			
			if (targetPlayer == null)
			{
				sender.sendMessage(LibMessage.NOTFOUND.getMessage(new StringPair("user", targetPlayerName)));
				return;
			}
			sender.sendMessage(procesSetValue(isBalance, value, targetPlayer, targetPlayerName));
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
		final int feedbackCode = isBalance ? economy.getStorage().setBalance(targetUUID, value) : economy.getStorage().setWallet(targetUUID, value);
		
		return processFeedbackCode(feedbackCode, value, isBalance, false, true, targetName, targetUUID); 
	}
	
	private void executeValueUpdate(CommandSender sender, String[] args, boolean isBalance, boolean isRemove) 
	{
		final double value = parseDoubleInput(sender, args[2]);			
		
		if (value == -1)
		{
			return;
		}	
		
		if (args.length >= 4)
		{			
			final String targetPlayerName = args[3]; 
			final UUID targetPlayer = UserHelper.getUniqueId(targetPlayerName);
			
			if (targetPlayer == null)
			{
				sender.sendMessage(LibMessage.NOTFOUND.getMessage(new StringPair("user", targetPlayerName)));
				return;
			}
			sender.sendMessage(processUpdateValue(isBalance, isRemove, value, targetPlayer, targetPlayerName));
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
		final int feedbackCode = (economy.getStorage().updateValue(targetUUID, value, isRemove, isBalance));
		
		return processFeedbackCode(feedbackCode, value, isBalance, isRemove, false, targetName, targetUUID);
	}
	
	private double parseDoubleInput(CommandSender sender, String input)
	{
		try 
		{
			final double parsedValue =  Double.parseDouble(input);
			
			if (parsedValue < 0)
			{
				sender.sendMessage(EconomyMessage.VALUE_NONEGATIVE.getMessage());
				return -1;
			}		
			return parsedValue;
		}
		catch (NumberFormatException execption)
		{
			sender.sendMessage(EconomyMessage.VALUE_INVALIDVALUE.getMessage(new StringPair("text", input)));
		}
		return -1;
	}
	
	private String processFeedbackCode(int code, double value, boolean isBalance, boolean withRemove, boolean override, String playerName, UUID playerUUID)
	{
		final boolean selfPerform = StringUtils.isEmpty(playerName);
		final boolean updatedPerform = playerUUID != null;
		
		switch (code)
		{
		case Storage.CODE_NOUSER:
			return selfPerform ? EconomyMessage.DONTEXIST_SELF.getMessage() : EconomyMessage.DONTEXIST_OTHER.getMessage(new StringPair("user", playerName));
		
		case Storage.CODE_NOTENOUGHVALUE:
			if (isBalance)
			{
				return selfPerform ? EconomyMessage.BALANCE_SELF_NOTENOUGH.getMessage() : EconomyMessage.BALANCE_OTHER_NOTENOUGH.getMessage(new StringPair("user", playerName));
			}
			return selfPerform ? EconomyMessage.WALLET_SELF_NOTENOUGH.getMessage() : EconomyMessage.WALLET_OTHER_NOTENOUGH.getMessage(new StringPair("user", playerName));
		
		case Storage.CODE_SUCCESS:
			// B A L A N C E
			
			if (isBalance)
			{
				if (override)
				{
					return selfPerform ? EconomyMessage.BALANCE_SELF_SET.getMessage(new StringPair("amount", EconomyConfig.getValueFormatted(value))) 
					                   : EconomyMessage.BALANCE_OTHER_SET.getMessage(new StringPair("user", playerName), new StringPair("amount", EconomyConfig.getValueFormatted(value))); 
				}
				
				if (updatedPerform)
				{
					final StringBuilder messageBuilder = new StringBuilder();
					
					if (withRemove)
					{
						messageBuilder.append(selfPerform ? EconomyMessage.BALANCE_SELF_LESS.getMessage(new StringPair("amount", EconomyConfig.getValueFormatted(value))) 
						                                  : EconomyMessage.BALANCE_OTHER_LESS.getMessage(new StringPair("user", playerName), new StringPair("amount", EconomyConfig.getValueFormatted(value))));
					}
					else
					{
						messageBuilder.append(selfPerform ? EconomyMessage.BALANCE_SELF_MORE.getMessage(new StringPair("amount", EconomyConfig.getValueFormatted(value))) 
                                                          : EconomyMessage.BALANCE_OTHER_MORE.getMessage(new StringPair("user", playerName), new StringPair("amount", EconomyConfig.getValueFormatted(value))));
					}
					messageBuilder.append("\n");
					messageBuilder.append(EconomyMessage.NEWVALUE.getMessage(new StringPair("amount", EconomyConfig.getValueFormatted(economy.getStorage().getBalance(playerUUID)))));
					return messageBuilder.toString();
				}
				return selfPerform ? EconomyMessage.BALANCE_SELF_SHOW.getMessage(new StringPair("amount", EconomyConfig.getValueFormatted(value))) 
						           : EconomyMessage.BALANCE_OTHER_SHOW.getMessage(new StringPair("user", playerName), new StringPair("amount", EconomyConfig.getValueFormatted(value)));
			}
			
			// W A L L E T
			
			if (override)
			{
				return selfPerform ? EconomyMessage.WALLET_SELF_SET.getMessage(new StringPair("amount", EconomyConfig.getValueFormatted(value))) 
				                   : EconomyMessage.WALLET_OTHER_SET.getMessage(new StringPair("user", playerName), new StringPair("amount", EconomyConfig.getValueFormatted(value))); 
			}
			
			if (updatedPerform)
			{
				final StringBuilder messageBuilder = new StringBuilder();
				
				if (withRemove)
				{
					messageBuilder.append(selfPerform ? EconomyMessage.WALLET_SELF_LESS.getMessage(new StringPair("amount", EconomyConfig.getValueFormatted(value))) 
					                                  : EconomyMessage.WALLET_OTHER_LESS.getMessage(new StringPair("user", playerName), new StringPair("amount", EconomyConfig.getValueFormatted(value))));
				}
				else
				{
					messageBuilder.append(selfPerform ? EconomyMessage.WALLET_SELF_MORE.getMessage(new StringPair("amount", EconomyConfig.getValueFormatted(value))) 
                                                      : EconomyMessage.WALLET_OTHER_MORE.getMessage(new StringPair("user", playerName), new StringPair("amount", EconomyConfig.getValueFormatted(value))));
				}
				messageBuilder.append("\n");
				messageBuilder.append(EconomyMessage.NEWVALUE.getMessage(new StringPair("amount", EconomyConfig.getValueFormatted(economy.getStorage().getWallet(playerUUID)))));
				return messageBuilder.toString();
			}
			return selfPerform ? EconomyMessage.WALLET_SELF_SHOW.getMessage(new StringPair("amount", EconomyConfig.getValueFormatted(value))) 
					           : EconomyMessage.WALLET_OTHER_SHOW.getMessage(new StringPair("user", playerName), new StringPair("amount", EconomyConfig.getValueFormatted(value)));
		
		default:
			return EconomyMessage.INVALIDFEEDBACK.getMessage(new StringPair("text", Integer.toString(code)));
		}
	}

	private @NotNull String getInvalidSenderMessage()
	{
		return StringHelper.build(EconomyMessage.NOSELFEXECUTE.getMessage());
	}
	
	// B A N K
	
	private boolean executeBankAction(CommandSender sender, String[] args)
	{
		final String action = args[1].toLowerCase();
		
		if (!action.equals("show") && !(sender instanceof Player))
		{
			sender.sendMessage(LibMessage.NOTINCONSOLE.getMessage());
			return true;
		}
		
		switch (action)
		{
		case "show":
			if (args.length >= 3)
			{
				executeBankShow(sender, args);	
				return true;
			}
			return false;
		case "add":
			executeBankAdd((Player) sender);
			return true;
		case "remove":
			executeBankRemove((Player) sender);
			return true;
		case "item":
			executeBankItem((Player) sender);
			return true;
		default:
			return false;
		}
	}
	
	private void executeBankShow(CommandSender sender, String[] args)
	{	
		final int maxPage = getListPageCount(economy.getBank().getBankCount());
		final int page = getListPage(sender, args[2], maxPage);
		
		if (page == -1)
		{
			return;
		}		
		final List<Location> bankLocations = economy.getBank().getBankLocations();
		
		if (!bankLocations.isEmpty())
		{	
			int position = 1;
			
			sender.sendMessage(EconomyMessage.BANK_HEADER.getMessage(new StringPair("bankname", EconomyConfig.BANK_NAME.getValue()), 
					                                                 new StringPair("page", Integer.toString(page)), 
					                                                 new StringPair("maxpage", Integer.toString(maxPage))));
			sender.sendMessage("");		
			
			for (int i = (page - 1) * LISTENTRIES_PER_PAGE; i < (page * LISTENTRIES_PER_PAGE); i++)
			{
				final Location bankLocation = bankLocations.get(i);
				final World bankLocationWorld = bankLocation.getWorld();
				
				if (bankLocationWorld == null)
				{
					continue;
				}
				final boolean sameWorld = sender instanceof Player && ((Player) sender).getWorld().equals(bankLocationWorld);
				
				sender.sendMessage(String.format("§8☰  §a%d §8- §7Welt: §9%s\n" + 
			                                     "§8☷ §7Koordinaten: §9%.0f %.0f %.0f §8× §7Distanz: §9%.2f", 
			                                     position, bankLocationWorld.getName(), bankLocation.getX(), 
			                                     bankLocation.getY(), bankLocation.getZ(),
			                                     sameWorld ? ((Player) sender).getLocation().distance(bankLocation) : -1));
				position++;
			}	
		}
		else
		{
			sender.sendMessage(EconomyMessage.BANK_NODATA.getMessage(new StringPair("bankname", EconomyConfig.BANK_NAME.getValue())));
		}
	}
	
	private void executeBankAdd(Player player)
	{
		final Location playerLocation = player.getLocation();
		final Location bankLocation = new Location(playerLocation.getWorld(), playerLocation.getBlockX() + 0.5, playerLocation.getBlockY(), playerLocation.getBlockZ() + 0.5,
				                                   playerLocation.getYaw(), 0);		
		
		if (economy.getBank().addBank(bankLocation, player.getUniqueId()) == Bank.CODE_SUCCESS)
		{
			economy.trySpawnBankEntity(bankLocation);
			player.sendMessage(EconomyMessage.BANK_PLACE.getMessage(new StringPair("bankname", EconomyConfig.BANK_NAME.getValue())));
		}
		else
		{
			player.sendMessage(EconomyMessage.BANK_TOCLOSE.getMessage(new StringPair("bankname", EconomyConfig.BANK_NAME.getValue())));
		}
	}
	
	private void executeBankRemove(Player player)
	{
		if (economy.getBank().removeBank(player.getLocation()) == Bank.CODE_SUCCESS)
		{
			economy.tryRemoveBankEntity(player.getLocation());
			player.sendMessage(EconomyMessage.BANK_REMOVE.getMessage(new StringPair("bankname", EconomyConfig.BANK_NAME.getValue())));
		}
		else
		{
			player.sendMessage(EconomyMessage.BANK_NOTHINGNEAR.getMessage(new StringPair("bankname", EconomyConfig.BANK_NAME.getValue())));
		}	
	}
	
	private void executeBankItem(Player player)
	{
		player.getInventory().addItem(EconomyConfig.getBankItem());
		player.sendMessage(EconomyMessage.BANK_ITEM.getMessage(new StringPair("bankname", EconomyConfig.BANK_NAME.getValue())));		
	}

	// T A B C O M P L E T E
	
	private void addTabCompletion()
	{
		final Map<Integer, List<String>> showBeforeText = ImmutableMap.of(
				0, Stream.of("balance", "wallet").collect(Collectors.toList()), 
				1, Stream.of("show").collect(Collectors.toList()));
		final Map<Integer, List<String>> valueBeforeText = ImmutableMap.of(
				0, Stream.of("balance", "wallet").collect(Collectors.toList()), 
				1, Stream.of("set", "add", "remove").collect(Collectors.toList()));
		final Map<Integer, List<String>> bankBeforeText = ImmutableMap.of(
				0, Stream.of("bank").collect(Collectors.toList()), 
				1, Stream.of("show").collect(Collectors.toList()));
		final String[] listPages = Stream.iterate(1, x -> x + 1)
				.limit(getListPageCount(economy.getStorage().getUserCount()))
				.map(x -> Integer.toString(x))
				.toArray(String[]::new);
		final String[] bankPages = Stream.iterate(1, x -> x + 1)
				.limit(getListPageCount(economy.getBank().getBankCount()))
				.map(x -> Integer.toString(x))
				.toArray(String[]::new);
		final String[] players = Stream.of(Bukkit.getOfflinePlayers())
				.map(OfflinePlayer::getName)
				.toArray(String[]::new); 
		
		super.addTabCompletion(0, "list", "balance", "wallet", "bank");
		super.addTabCompletion(1, null, Stream.of("list").toArray(String[]::new), listPages);
		super.addTabCompletion(1, null, Stream.of("balance", "wallet").toArray(String[]::new), "show", "set", "add", "remove");
		super.addTabCompletion(1, null, Stream.of("bank").toArray(String[]::new), "show", "item", "add", "remove");
		super.addTabCompletion(2, null, showBeforeText, players);
		super.addTabCompletion(2, null, valueBeforeText, "<Wert>");
		super.addTabCompletion(3, null, valueBeforeText, players);
		super.addTabCompletion(3, null, bankBeforeText, bankPages);
	}
}
