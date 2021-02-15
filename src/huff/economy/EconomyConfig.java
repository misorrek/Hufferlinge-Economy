package huff.economy;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.economy.menuholder.TransactionKind;
import huff.lib.helper.ItemHelper;
import huff.lib.helper.StringHelper;
import huff.lib.manager.ConfigManager;
import huff.lib.various.HuffConfiguration;
import huff.lib.various.structures.ConfigEnumPair;
import huff.lib.various.structures.ConfigPair;
import huff.lib.various.structures.MessagePair;

public class EconomyConfig
{
	private EconomyConfig() { }

	public static final ConfigPair<String> VALUE_NAME = new ConfigPair<>("economy.value.name", "Goldlinge", String.class);
	public static final ConfigPair<String> VALUE_ITEMNAME = new ConfigPair<>("economy.value.item_name", "§e§l" + VALUE_NAME.getKeyLink(), String.class);
	public static final ConfigEnumPair<Material> VALUE_MATERIAL = new ConfigEnumPair<>("economy.value.material", Material.GOLD_NUGGET, Material.class);
	public static final ConfigPair<String> VALUE_SIGNATURE = new ConfigPair<>("economy.value.signature", "§8Prägung", String.class);

	public static final ConfigPair<String> WALLET_NAME = new ConfigPair<>("economy.wallet.name", "Geldbeutel", String.class);
	public static final ConfigPair<String> WALLET_ITEMNAME = new ConfigPair<>("economy.wallet.item_name", "§6§l" + WALLET_NAME.getKeyLink(), String.class);
	public static final ConfigPair<String> WALLET_INVNAME = new ConfigPair<>("economy.wallet.inventory_name", "§7» §6" + WALLET_NAME.getKeyLink(), String.class);
	public static final ConfigEnumPair<Material> WALLET_MATERIAL = new ConfigEnumPair<>("economy.wallet.material", Material.BROWN_SHULKER_BOX, Material.class);
	public static final ConfigPair<Integer> WALLET_DEFAULTSLOT = new ConfigPair<>("economy.wallet.default_slot", 8, Integer.class);
	
	public static final ConfigPair<String> BANK_NAME = new ConfigPair<>("economy.bank.name", "Bänker", String.class);
	public static final ConfigPair<String> BANK_ITEMNAME = new ConfigPair<>("economy.bank.item_name", "§e§l" + BANK_NAME.getKeyLink(), String.class);
	public static final ConfigPair<String> BANK_INVNAME = new ConfigPair<>("economy.bank.inventory_name", "§7» §e" + BANK_NAME.getKeyLink(), String.class);
	public static final ConfigPair<String> BANK_ENTITYNAME = new ConfigPair<>("economy.bank.entity_name", "§e§l" + BANK_NAME.getKeyLink(), String.class);
	public static final ConfigPair<String> BANK_CLOSEDNAME = new ConfigPair<>("economy.bank.closed_name", "§7- Geschlossen -", String.class);
	public static final ConfigPair<String> BANK_REMOVENAME = new ConfigPair<>("economy.bank.inventory_remove_name", "§7» §c" + BANK_NAME.getKeyLink() + " entfernen", String.class);
	public static final ConfigEnumPair<Material> BANK_MATERIAL = new ConfigEnumPair<>("economy.bank.material", Material.OCELOT_SPAWN_EGG, Material.class);
	public static final ConfigPair<Integer> BANK_OPEN = new ConfigPair<>("economy.bank.open", 1000, Integer.class);
	public static final ConfigPair<Integer> BANK_CLOSE = new ConfigPair<>("economy.bank.close", 13000, Integer.class);
	public static final ConfigPair<Integer> BANK_STARTBALANCE = new ConfigPair<>("economy.bank.start_balance", 100, Integer.class);
	
	public static final ConfigPair<Boolean> TRANSACTION_FEEDBACK = new ConfigPair<>("economy.transaction.feedback", true, Boolean.class);
	public static final MessagePair TRANSACTION_RECEIVER = new MessagePair("economy.trade.name", "§7Empfänger: §9%user%");
	
	public static final ConfigPair<String> TRADE_NAME = new ConfigPair<>("economy.trade.name", "Handel", String.class);
	public static final ConfigPair<String> TRADE_INVNAME = new ConfigPair<>("economy.trade.inventory_name", "§7» §e" + TRADE_NAME.getKeyLink(), String.class);
	public static final ConfigEnumPair<Material> TRADE_MATERIAL = new ConfigEnumPair<>("economy.trade.material", Material.BARREL, Material.class);
	public static final ConfigPair<String> TRADE_PENDINGNAME = new ConfigPair<>("economy.trade.pending_name", "§6Handel ausstehend...", String.class);
	public static final ConfigEnumPair<Material> TRADE_PENDINGMATERIAL = new ConfigEnumPair<>("economy.trade.pending_material", Material.ORANGE_STAINED_GLASS_PANE, Material.class);
	public static final ConfigPair<String> TRADE_ACCEPTEDNAME = new ConfigPair<>("economy.trade.accepted_name", "§aHandel akzeptiert", String.class);
	public static final ConfigEnumPair<Material> TRADE_ACCEPTEDMATERIAL = new ConfigEnumPair<>("economy.trade.accepted_material", Material.LIME_STAINED_GLASS_PANE, Material.class);
	
	static 
	{
		final HuffConfiguration config = new HuffConfiguration(); 		
		
		config.addEmptyLine("economy");
		config.addCommentLine("economy", "+--------------------------------------+ #", false);
		config.addCommentLine("economy", "         // E C O N O M Y //             #", false);
		config.addCommentLine("economy", "+--------------------------------------+ #", false);
		config.addEmptyLine("economy");
		
		config.set(VALUE_NAME);
		config.set(VALUE_ITEMNAME);
		config.set(VALUE_MATERIAL);
		config.set(VALUE_SIGNATURE);
		
		config.set(WALLET_NAME);
		config.set(WALLET_ITEMNAME);
		config.set(WALLET_INVNAME);
		config.set(WALLET_MATERIAL);
		config.set(WALLET_DEFAULTSLOT);
		
		config.set(BANK_NAME);
		config.set(BANK_ITEMNAME);
		config.set(BANK_INVNAME);
		config.set(BANK_REMOVENAME);
		config.set(BANK_MATERIAL);
		config.set(BANK_OPEN);
		config.set(BANK_CLOSE);
		config.set(BANK_STARTBALANCE);
		
		config.set(TRANSACTION_FEEDBACK);
		
		config.set(TRADE_NAME);
		config.set(TRADE_INVNAME);
		config.set(TRADE_MATERIAL);
		
		ConfigManager.CONFIG.addDefaults(config);
	}
	
	private static final ItemStack VALUE_ITEM = ItemHelper.getItemWithMeta(VALUE_MATERIAL.getValue(), VALUE_ITEMNAME.getValue());
	private static final ItemStack WALLET_ITEM = ItemHelper.getItemWithMeta(WALLET_MATERIAL.getValue(), WALLET_ITEMNAME.getValue());
	private static final ItemStack BANK_ITEM = ItemHelper.getItemWithMeta(BANK_MATERIAL.getValue(), BANK_ITEMNAME.getValue());
	private static final ItemStack TRADE_PENDINGITEM = ItemHelper.getItemWithMeta(TRADE_PENDINGMATERIAL.getValue(), TRADE_PENDINGNAME.getValue());
	private static final ItemStack TRADE_ACCEPTEDITEM = ItemHelper.getItemWithMeta(TRADE_ACCEPTEDMATERIAL.getValue(), TRADE_ACCEPTEDNAME.getValue());
	
	// V A L U E
	
	public static @NotNull String getValueFormatted(double value)
	{
		return String.format("%.0f %s", value, VALUE_NAME.getValue());
	}
	
	public static @NotNull ItemStack getValueItem()
	{			
		return VALUE_ITEM.clone();
	}
	
	public static boolean equalsValueItem(@Nullable ItemStack item) //TODO CHECK
	{
		return item != null && item.isSimilar(VALUE_ITEM);
	}
	
	public static @NotNull List<String> getCurrentValueLore(TransactionKind transactionKind, double currentValue)
	{	
		List<String> valueLore = new ArrayList<>();
		
		valueLore.add(String.format("§7%s: %.0f %s", 
				                    transactionKind.isBankTransaction() ? BANK_NAME.getValue() : WALLET_NAME.getValue(),
				                    currentValue, VALUE_NAME.getValue()));
		
		return valueLore;
	}
	
	// W A L L E T
	
	public static @NotNull ItemStack getWalletItem() 
	{		
		return WALLET_ITEM.clone();
	}
	
	public static boolean equalsWalletItem(@Nullable ItemStack item) //TODO CHECK
	{
		return item != null && item.isSimilar(WALLET_ITEM);
	}

	// B A N K
	
	public static @NotNull ItemStack getBankItem()
	{		
		return BANK_ITEM.clone();
	}
	
	public static boolean equalsBankSpawnItem(@Nullable ItemStack item)
	{
		return item != null && item.isSimilar(BANK_ITEM);
	}
	
	// T R A N S A C T I O N
	
	public static String getTransactionInventoryName(TransactionKind transactionKind)
	{		
		return StringHelper.build("§7» §e", VALUE_NAME.getValue(), " ", transactionKind.getLowerLabel());
	}
	
	// T R A D E
	
	public static @NotNull ItemStack getTradePendingItem()
	{		
		return TRADE_PENDINGITEM.clone();
	}
	
	public static @NotNull ItemStack getTradeAcceptedItem()
	{		
		return TRADE_ACCEPTEDITEM.clone();
	}
}