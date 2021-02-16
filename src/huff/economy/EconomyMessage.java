package huff.economy;

import huff.lib.manager.MessageManager;
import huff.lib.various.HuffConfiguration;
import huff.lib.various.LibMessage;
import huff.lib.various.structures.MessagePair;

public class EconomyMessage
{
	private EconomyMessage() { }
	
	public static final MessagePair NEWVALUE = new MessagePair("economy.new_value", LibMessage.PREFIX_GENERAL.getKeyLink() + "Der neue Stand beträgt §9%amount%§7.");
	public static final MessagePair NOSELFEXECUTE = new MessagePair("economy.no_self_execute", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du kannst diesen Befehl nicht auf dich selbst aufrufen.");
	public static final MessagePair INVALIDPAGE = new MessagePair("economy.invalid_page", LibMessage.PREFIX_GENERAL.getKeyLink() + "Ungültige Seite. Es gibt §9%maxpage% Seiten§7.");
	public static final MessagePair INVALIDNUMBER = new MessagePair("economy.invalid_number", LibMessage.PREFIX_GENERAL.getKeyLink() + "§9\"%text%\"%7 ist keine gültige Nummer.");
	public static final MessagePair INVALIDFEEDBACK = new MessagePair("economy.invalid_feedback", LibMessage.PREFIX_GENERAL.getKeyLink() + "Ungültiger Datenbank-Rückgabecode §9\"%text%\"§7.");

	public static final MessagePair LIST_HEADER = new MessagePair("economy.list.header", "§8☰ §7Übersicht über die Kontostände - Seite (%page%/%maxPage%)");
	public static final MessagePair LIST_NODATA = new MessagePair("economy.list.no_data", "§8☰ §7Keine Spieler zur Übersicht vorhanden");

	public static final MessagePair VALUE_NONEGATIVE = new MessagePair("economy.value.no_negative", LibMessage.PREFIX_GENERAL.getKeyLink() + "Der eingegebene Wert darf nicht negativ sein.");
	public static final MessagePair VALUE_INVALIDVALUE = new MessagePair("economy.value.invalid_value", LibMessage.PREFIX_GENERAL.getKeyLink() + "§9\"%text%\"%7 ist kein gültiger Zahlenwert.");
	
	public static final MessagePair BANK_HEADER = new MessagePair("economy.bank.header", "§8☰ §7Übersicht aller %bankname% - Seite (%page%/%maxPage%)");
	public static final MessagePair BANK_NODATA = new MessagePair("economy.bank.no_data", "§8☰ §7Keine %bankname% zur Übersicht vorhanden");
	public static final MessagePair BANK_NOTALLOWED = new MessagePair("economy.bank.not_allowed", LibMessage.PREFIX_GENERAL.getKeyLink() + "Das erlaubt der %bankname% nicht.");
	public static final MessagePair BANK_PLACE = new MessagePair("economy.bank.place", LibMessage.PREFIX_GENERAL.getKeyLink() + "%bankname% platziert.");
	public static final MessagePair BANK_OPENINGHOURS = new MessagePair("economy.bank.opening_hours", LibMessage.PREFIX_GENERAL.getKeyLink() + "Denke an die Öffnungszeiten von §9%open%§7 bis §9%close%§7.");
	public static final MessagePair BANK_TOCLOSE = new MessagePair("economy.bank.to_close", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du bist zu nah an einem anderen %bankname%.");
	public static final MessagePair BANK_REMOVE = new MessagePair("economy.bank.remove", LibMessage.PREFIX_GENERAL.getKeyLink() + "%bankname% entfernt.");
	public static final MessagePair BANK_NOTHINGNEAR = new MessagePair("economy.bank.nothing_near", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du bist nicht in der Nähe von einem %bankname%.");
	public static final MessagePair BANK_ITEM = new MessagePair("economy.bank.item", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du hast den Gegenstand zum Erstellen eines %bankname% bekommen.");
	
	public static final MessagePair DONTEXIST_SELF = new MessagePair("economy.dont_exist.self", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du bist nicht in der Economy-Datenbank vorhanden.");
	public static final MessagePair DONTEXIST_OTHER = new MessagePair("economy.dont_exist.other", LibMessage.PREFIX_GENERAL.getKeyLink() + "Der Spieler §9%user%§7 ist nicht in der Economy-Datenbank vorhanden");
	
	public static final MessagePair BALANCE_SELF_SHOW = new MessagePair("economy.balance.self.show", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du hast §9%amount%§7 auf der Bank.");
	public static final MessagePair BALANCE_SELF_SET = new MessagePair("economy.balance.self.set", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du hast nun §9%amount%§7 auf der Bank.");
	public static final MessagePair BALANCE_SELF_LESS = new MessagePair("economy.balance.self.less", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du hast nun §9%amount%§7 weniger auf der Bank.");
	public static final MessagePair BALANCE_SELF_MORE = new MessagePair("economy.balance.self.more", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du hast nun §9%amount%§7 mehr auf der Bank.");
	public static final MessagePair BALANCE_SELF_NOTENOUGH = new MessagePair("economy.balance.self.not_enough", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du hast dazu nicht genung auf der Bank.");
	public static final MessagePair BALANCE_OTHER_SHOW = new MessagePair("economy.balance.other.show", LibMessage.PREFIX_GENERAL.getKeyLink() + "Der Spieler §9%user%§7 hat §9%amount%§7 auf der Bank.");
	public static final MessagePair BALANCE_OTHER_SET = new MessagePair("economy.balance.other.set", LibMessage.PREFIX_GENERAL.getKeyLink() + "Der Spieler §9%user%§7 hat nun §9%amount%§7 auf der Bank.");
	public static final MessagePair BALANCE_OTHER_LESS = new MessagePair("economy.balance.other.less", LibMessage.PREFIX_GENERAL.getKeyLink() + "Der Spieler §9%user%§7 hat nun §9%amount%§7 weniger auf der Bank.");
	public static final MessagePair BALANCE_OTHER_MORE = new MessagePair("economy.balance.other.more", LibMessage.PREFIX_GENERAL.getKeyLink() + "Der Spieler §9%user%§7 hat nun §9%amount%§7 mehr auf der Bank.");
	public static final MessagePair BALANCE_OTHER_NOTENOUGH = new MessagePair("economy.balance.other.not_enough", LibMessage.PREFIX_GENERAL.getKeyLink() + "Der Spieler §9%user%§7 hat dazu nicht genung auf der Bank.");
	
	public static final MessagePair WALLET_SELF_SHOW = new MessagePair("economy.wallet.self.show", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du hast §9%amount%§7 im Geldbeutel.");
	public static final MessagePair WALLET_SELF_SET = new MessagePair("economy.wallet.self.set", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du hast nun §9%amount%§7 im Geldbeutel.");
	public static final MessagePair WALLET_SELF_LESS = new MessagePair("economy.wallet.self.less", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du hast nun §9%amount%§7 weniger im Geldbeutel.");
	public static final MessagePair WALLET_SELF_MORE = new MessagePair("economy.wallet.self.more", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du hast nun §9%amount%§7 mehr im Geldbeutel.");
	public static final MessagePair WALLET_SELF_NOTENOUGH = new MessagePair("economy.wallet.self.not_enough", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du hast dazu nicht genung im Geldbeutel.");
	public static final MessagePair WALLET_OTHER_SHOW = new MessagePair("economy.wallet.other.show", LibMessage.PREFIX_GENERAL.getKeyLink() + "Der Spieler §9%user%§7 hat §9%amount%§7 im Geldbeutel.");
	public static final MessagePair WALLET_OTHER_SET = new MessagePair("economy.wallet.other.set", LibMessage.PREFIX_GENERAL.getKeyLink() + "Der Spieler §9%user%§7 hat nun §9%amount%§7 im Geldbeutel.");
	public static final MessagePair WALLET_OTHER_LESS = new MessagePair("economy.wallet.other.less", LibMessage.PREFIX_GENERAL.getKeyLink() + "Der Spieler §9%user%§7 hat nun §9%amount%§7 weniger im Geldbeutel.");
	public static final MessagePair WALLET_OTHER_MORE = new MessagePair("economy.wallet.other.more", LibMessage.PREFIX_GENERAL.getKeyLink() + "Der Spieler §9%user%§7 hat nun §9%amount%§7 mehr im Geldbeutel.");
	public static final MessagePair WALLET_OTHER_NOTENOUGH = new MessagePair("economy.wallet.other.not_enough", LibMessage.PREFIX_GENERAL.getKeyLink() + "Der Spieler §9%user%§7 hat dazu nicht genung im Geldbeutel.");
	public static final MessagePair WALLET_NOPICKUP = new MessagePair("economy.wallet.other.no_pickup", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du kannst in deinem Spielmodus nicht in den %walletname% einlagern.");
	
	public static final MessagePair TRANSACTION_SENT = new MessagePair("economy.transaction.sent", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du hast §9%amount%§7 an §9%user%§7 übertragen.");
	public static final MessagePair TRANSACTION_RECEIVED = new MessagePair("economy.transaction.received", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du hast §9%amount%§7 von §9%user%§7 erhalten.");
	public static final MessagePair TRANSACTION_DEPOSITED = new MessagePair("economy.transaction.deposited", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du hast §9%amount%§7 auf die Bank eingezahlt.");
	public static final MessagePair TRANSACTION_WITHDRAWED = new MessagePair("economy.transaction.withdrawed", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du hast §9%amount%§7 von der Bank ausgezahlt.");
	public static final MessagePair TRANSACTION_TAKEN = new MessagePair("economy.transaction.taken", LibMessage.PREFIX_GENERAL.getKeyLink() + "Du hast §9%amount%§7 aus deinem %walletname% herausgenommen.");
	public static final MessagePair TRANSACTION_FAIL = new MessagePair("economy.transaction.fail", LibMessage.PREFIX_GENERAL.getKeyLink() + "Die Transaktion konnte nicht abgeschlossen werden.");
	public static final MessagePair TRANSACTION_TARGETDISCONNECTED = new MessagePair("economy.transaction.target_disconnected", LibMessage.PREFIX_GENERAL.getKeyLink() + "§9%user%§7 ist nicht mehr da.");
	
	public static final MessagePair TRADE_NOTALLOWED = new MessagePair("economy.wallet.other.not_allowed", LibMessage.PREFIX_GENERAL.getKeyLink() + "Mit §9%user%§7 kann gerade nicht gehandelt werden.");
	public static final MessagePair TRADE_ABORT = new MessagePair("economy.wallet.other.abort", LibMessage.PREFIX_GENERAL.getKeyLink() + "Der Handel wurde §cabgebrochen§7.");
	
	public static void init()
	{
		final HuffConfiguration config = new HuffConfiguration();
		
		config.addEmptyLine("economy");
		config.addCommentLine("economy", "+--------------------------------------+ #", false);
		config.addCommentLine("economy", "         // E C O N O M Y //             #", false);
		config.addCommentLine("economy", "+--------------------------------------+ #", false);
		config.addEmptyLine("economy");
		
		config.addContextLine(NEWVALUE.getKey(), "amount");
		config.set(NEWVALUE);
		config.set(NOSELFEXECUTE);
		config.addContextLine(INVALIDPAGE.getKey(), "maxpage");
		config.set(INVALIDPAGE);
		config.addContextLine(INVALIDNUMBER.getKey(), "text");
		config.set(INVALIDNUMBER);
		config.addContextLine(INVALIDFEEDBACK.getKey(), "text");
		config.set(INVALIDFEEDBACK);
		
		config.addContextLine(LIST_HEADER.getKey(), "page", "maxpage");
		config.set(LIST_HEADER);
		config.set(LIST_NODATA);
		
		config.set(VALUE_NONEGATIVE);
		config.addContextLine(VALUE_INVALIDVALUE.getKey(), "text");
		config.set(VALUE_INVALIDVALUE);
		
		config.addContextLine(BANK_HEADER.getKey(), "bankname", "page", "maxpage");
		config.set(BANK_HEADER);
		config.addContextLine(BANK_NODATA.getKey(), "bankname");
		config.set(BANK_NODATA);
		config.addContextLine(BANK_NOTALLOWED.getKey(), "bankname");
		config.set(BANK_NOTALLOWED);
		config.addContextLine(BANK_PLACE.getKey(), "bankname");
		config.set(BANK_PLACE);
		config.addContextLine(BANK_OPENINGHOURS.getKey(), "open", "close");
		config.set(BANK_OPENINGHOURS);
		config.addContextLine(BANK_TOCLOSE.getKey(), "bankname");
		config.set(BANK_TOCLOSE);
		config.addContextLine(BANK_REMOVE.getKey(), "bankname");
		config.set(BANK_REMOVE);
		config.addContextLine(BANK_NOTHINGNEAR.getKey(), "bankname");
		config.set(BANK_NOTHINGNEAR);
		config.addContextLine(BANK_ITEM.getKey(), "bankname");
		config.set(BANK_ITEM);
		
		config.set(DONTEXIST_SELF);
		config.addContextLine(DONTEXIST_OTHER.getKey(), "user");
		config.set(DONTEXIST_OTHER);
		
		config.addContextLine(BALANCE_SELF_SHOW.getKey(), "amount");
		config.set(BALANCE_SELF_SHOW);
		config.addContextLine(BALANCE_SELF_SET.getKey(), "amount");
		config.set(BALANCE_SELF_SET);
		config.addContextLine(BALANCE_SELF_LESS.getKey(), "amount");
		config.set(BALANCE_SELF_LESS);
		config.addContextLine(BALANCE_SELF_MORE.getKey(), "amount");
		config.set(BALANCE_SELF_MORE);
		config.set(BALANCE_SELF_NOTENOUGH);
		config.addContextLine(BALANCE_OTHER_SHOW.getKey(), "user", "amount");
		config.set(BALANCE_OTHER_SHOW);
		config.addContextLine(BALANCE_OTHER_SET.getKey(), "user", "amount");
		config.set(BALANCE_OTHER_SET);
		config.addContextLine(BALANCE_OTHER_LESS.getKey(), "user", "amount");
		config.set(BALANCE_OTHER_LESS);
		config.addContextLine(BALANCE_OTHER_MORE.getKey(), "user", "amount");
		config.set(BALANCE_OTHER_MORE);
		config.set(BALANCE_OTHER_NOTENOUGH);
		
		config.addContextLine(WALLET_SELF_SHOW.getKey(), "amount");
		config.set(WALLET_SELF_SHOW);
		config.addContextLine(WALLET_SELF_SET.getKey(), "amount");
		config.set(WALLET_SELF_SET);
		config.addContextLine(WALLET_SELF_LESS.getKey(), "amount");
		config.set(WALLET_SELF_LESS);
		config.addContextLine(WALLET_SELF_MORE.getKey(), "amount");
		config.set(WALLET_SELF_MORE);
		config.set(WALLET_SELF_NOTENOUGH);
		config.addContextLine(WALLET_OTHER_SHOW.getKey(), "user", "amount");
		config.set(WALLET_OTHER_SHOW);
		config.addContextLine(WALLET_OTHER_SET.getKey(), "user", "amount");
		config.set(WALLET_OTHER_SET);
		config.addContextLine(WALLET_OTHER_LESS.getKey(), "user", "amount");
		config.set(WALLET_OTHER_LESS);
		config.addContextLine(WALLET_OTHER_MORE.getKey(), "user", "amount");
		config.set(WALLET_OTHER_MORE);
		config.set(WALLET_OTHER_NOTENOUGH);
		config.addContextLine(WALLET_NOPICKUP.getKey(), "walletname");
		config.set(WALLET_NOPICKUP);
		
		config.addContextLine(TRANSACTION_SENT.getKey(), "user", "amount");
		config.set(TRANSACTION_SENT);
		config.addContextLine(TRANSACTION_RECEIVED.getKey(), "user", "amount");
		config.set(TRANSACTION_RECEIVED);
		config.addContextLine(TRANSACTION_DEPOSITED.getKey(), "amount");
		config.set(TRANSACTION_DEPOSITED);
		config.addContextLine(TRANSACTION_WITHDRAWED.getKey(), "amount");
		config.set(TRANSACTION_WITHDRAWED);
		config.addContextLine(TRANSACTION_TAKEN.getKey(), "amount", "walletname");
		config.set(TRANSACTION_TAKEN);
		config.set(TRANSACTION_FAIL);
		config.addContextLine(TRANSACTION_TARGETDISCONNECTED.getKey(), "user");
		config.set(TRANSACTION_TARGETDISCONNECTED);
		
		config.addContextLine(TRADE_NOTALLOWED.getKey(), "user");
		config.set(TRADE_NOTALLOWED);
		config.set(TRADE_ABORT);
		
		MessageManager.addDefaults(config);
	}
}
