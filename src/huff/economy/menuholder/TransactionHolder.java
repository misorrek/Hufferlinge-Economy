package huff.economy.menuholder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.economy.EconomyConfig;
import huff.economy.EconomyInterface;
import huff.economy.EconomyMessage;
import huff.economy.storage.Storage;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.ItemHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.helper.SignHelper;
import huff.lib.events.PlayerSignInputEvent;
import huff.lib.helper.IndependencyHelper;
import huff.lib.manager.delaymessage.DelayType;
import huff.lib.menuholder.MenuExitType;
import huff.lib.menuholder.MenuHolder;
import huff.lib.menuholder.PlayerChooserHolder;
import huff.lib.various.Action;
import huff.lib.various.LibMessage;
import huff.lib.various.structures.StringPair;

public class TransactionHolder extends MenuHolder
{
	private static final String NBT_KEY = "changeamount";
	private static final int AMOUNT_1 = 1;
	private static final int AMOUNT_2 = 5;
	private static final int AMOUNT_3 = 10;
	private static final int AMOUNT_4 = 100;
	private static final int AMOUNT_5 = 1000;
	
	private TransactionHolder(@NotNull EconomyInterface economyInterface, TransactionKind transactionKind, @NotNull UUID viewer, @Nullable UUID target, double transactionValue)
	{
		super("economy:transaction", InventoryHelper.INV_SIZE_4, transactionKind.getLabel(), MenuExitType.ABORT);
		
		Validate.notNull((Object) economyInterface, "The economy-interface cannot be null.");
		
		this.economy = economyInterface;
		this.transactionKind = transactionKind;
		this.transactionValue = transactionValue;
		this.target = target;
		
		initInventory(viewer);
	}
	
	public TransactionHolder(@NotNull EconomyInterface economyInterface, TransactionKind transactionKind, @NotNull UUID viewer)
	{
		this(economyInterface, transactionKind, viewer, null, 0);
	}
	
	public TransactionHolder(@NotNull EconomyInterface economyInterface, TransactionKind transactionKind, @NotNull UUID viewer, @Nullable UUID target)
	{
		this(economyInterface, transactionKind, viewer, target, 0);
		
		Validate.isTrue(target != null || !transactionKind.isHumanTransaction(), "The target-uuid cannot be null in a human transaction.");
	}

	public TransactionHolder(@NotNull EconomyInterface economyInterface, TransactionKind transactionKind, @NotNull UUID viewer, double transactionValue, @NotNull Action finishAction)
	{
		this(economyInterface, transactionKind, viewer, null, transactionValue);
		
		Validate.isTrue(transactionKind.isChooseTransaction(), "The transaction kind must be a choose transaction.");
		Validate.notNull((Object) finishAction, "The transaction finish action cannot be null.");
		
		this.finishAction = finishAction;
	}
	
	private final EconomyInterface economy;
	private final TransactionKind transactionKind;
	
	private double transactionValue;
	private UUID target;
	private Action finishAction;	
	
	public static void handleTransactionOpen(@NotNull EconomyInterface economy, @NotNull HumanEntity human, @NotNull String currentItemName)
	{
		Validate.notNull((Object) economy, "The economy-interface cannot be null.");
		Validate.notNull((Object) human, "The human who clicked cannot be null.");
		Validate.notNull((Object) currentItemName, "The current item name cannot be null.");
		
		if (TransactionKind.isTransaction(currentItemName))
		{
			final TransactionKind transactionKind = TransactionKind.getTransaction(currentItemName);
			
			if (transactionKind == TransactionKind.BANK_OTHER)
			{
				new PlayerChooserHolder(economy.getPlugin(), economy.getStorage().getUsers(human.getUniqueId()), InventoryHelper.INV_SIZE_6, null, MenuExitType.BACK, params -> 
				{
					final Object object = params != null && params.length > 0 ? params[0] : null;
					
					if (object instanceof UUID)
					{
						new TransactionHolder(economy, TransactionKind.BANK_OTHER, human.getUniqueId(), (UUID) object).open(human);
					}
				}).open(human);
			}
			else
			{	
				 new TransactionHolder(economy, transactionKind, human.getUniqueId()).open(human);
			}
		}
	}
	
	@Override
	public boolean handleClick(@NotNull InventoryClickEvent event)
	{
		Validate.notNull((Object) event, "The inventory click event cannot be null.");
		
		if (event.getClickedInventory().getType() == InventoryType.PLAYER)
		{
			return false;
		}
		final HumanEntity human = event.getWhoClicked();
		final ItemStack currentItem = event.getCurrentItem();
		
		if (!ItemHelper.hasMeta(currentItem))
		{
			return true;
		}			
		final String currentItemName = currentItem.getItemMeta().getDisplayName();
		final String amountValue = IndependencyHelper.getTagFromItemStack(currentItem, NBT_KEY);
		
		if (StringUtils.isNotEmpty(amountValue))
		{					
			handleTransactionValueChange(Integer.parseInt(amountValue), (Player) human);
		}
		else if (currentItemName.equals(EconomyConfig.TRANSACTION_SIGNINPUTNAME.getValue()))
		{
			super.openSign(human, SignHelper.getInputLines("Betrag eingeben", "---"));
		}
		else if (currentItemName.equals(getPerformItemName()))
		{
			if (transactionValue == 0)
			{
				((Player) human).playSound(human.getLocation(), Sound.ENTITY_EGG_THROW, 1, 2);
			}
			else if (transactionKind == TransactionKind.BANK_OTHER || transactionKind == TransactionKind.WALLET_OTHER)
			{			
				handleTransactionHuman(human);			
			}
			else if (transactionKind == TransactionKind.BANK_IN)
			{				
				handleTransactionBank(human, false);
			}
			else if (transactionKind == TransactionKind.BANK_OUT)
			{
				handleTransactionBank(human, true);
			}
			else if (transactionKind == TransactionKind.WALLET_OUT)
			{
				handleTransactionWalletOut(human);			
			}
			else if (transactionKind.isChooseTransaction() && finishAction != null)
			{
				finishAction.execute(transactionValue);	
			}
			else
			{
				MenuHolder.close(human);
				human.sendMessage(EconomyMessage.TRANSACTION_FAIL.getMessage());	
			}	
		}
		return true;
	}
	
	@Override
	public void handleSignInput(PlayerSignInputEvent event)
	{
		final String inputValue = event.getLines()[0];
		
		try
		{
			handleTransactionValueChange(Double.parseDouble(inputValue), event.getPlayer());
		}
		catch (NumberFormatException exception)
		{
			EconomyMessage.INVALIDAMOUNT.getMessage(new StringPair("text", inputValue));
		}
	}
	
	private void initInventory(@NotNull UUID uuid)
	{	
		InventoryHelper.setFill(super.getInventory(), InventoryHelper.getBorderItem(), true);
		
		InventoryHelper.setItem(super.getInventory(), 2, 2, getAmountItem(AMOUNT_5, false));		
		InventoryHelper.setItem(super.getInventory(), 2, 3, getAmountItem(AMOUNT_4, false));
		InventoryHelper.setItem(super.getInventory(), 2, 4, getAmountItem(AMOUNT_3, false));
		InventoryHelper.setItem(super.getInventory(), 2, 5, ItemHelper.getItemWithMeta(EconomyConfig.VALUE_MATERIAL.getValue(),
				                                                                       MessageHelper.getHighlighted(EconomyConfig.getValueFormatted(0), false, false),
				                                                                       getMaxValueLore(getMaxTransactionValue(uuid), false)));	
		InventoryHelper.setItem(super.getInventory(), 2, 6, getAmountItem(AMOUNT_3, true));
		InventoryHelper.setItem(super.getInventory(), 2, 7, getAmountItem(AMOUNT_4, true));
		InventoryHelper.setItem(super.getInventory(), 2, 8, getAmountItem(AMOUNT_5, true));
		
		InventoryHelper.setItem(super.getInventory(), 3, 3, getAmountItem(AMOUNT_2, false));
		InventoryHelper.setItem(super.getInventory(), 3, 4, getAmountItem(AMOUNT_1, false));
		
		if (target != null)
		{		
			final OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(target);
			
			InventoryHelper.setItem(super.getInventory(), 3, 5, 
					ItemHelper.getSkullWithMeta(targetPlayer, EconomyConfig.TRANSACTION_RECEIVER.getMessage(new StringPair("user", targetPlayer.getName()))));
		}		
		InventoryHelper.setItem(super.getInventory(), 3, 6, getAmountItem(AMOUNT_1, true));
		InventoryHelper.setItem(super.getInventory(), 3, 7, getAmountItem(AMOUNT_2, true));
		
		InventoryHelper.setItem(super.getInventory(), 4, 1, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getPerformItemName()));
		
		if (EconomyConfig.TRANSACTION_SIGNINPUT.getValue())
		{
			InventoryHelper.setItem(super.getInventory(), 4, 5, ItemHelper.getItemWithMeta(Material.JUNGLE_SIGN, EconomyConfig.TRANSACTION_SIGNINPUTNAME.getValue()));	
		}
		super.setMenuExitItem();
	}
	
	private void handleTransactionValueChange(double changeValue, @NotNull Player player)
	{
		final double maxValue = getMaxTransactionValue(player.getUniqueId());
		double updatedTransactionValue = transactionValue + changeValue;
		boolean isInventoryMax = false;
	
		if (updatedTransactionValue < 0)
		{
			updatedTransactionValue = 0;
		}
		else if (updatedTransactionValue > maxValue)
		{
			updatedTransactionValue = maxValue;
		}	
		
		if (transactionKind.isItemTransaction())
		{
			final int maxInventoryValue = InventoryHelper.getFreeItemStackAmount(player.getInventory(), EconomyConfig.getValueItem());
			
			if (updatedTransactionValue > maxInventoryValue)
			{
				updatedTransactionValue = maxInventoryValue;
				isInventoryMax = true;			
			}
		}

		if (transactionValue != updatedTransactionValue)
		{	
			updateTransactionValue(updatedTransactionValue, maxValue, isInventoryMax);
			player.playSound(player.getLocation(), (changeValue < 0 ? Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF : Sound.ENTITY_EXPERIENCE_ORB_PICKUP), 1, 2);
		}
		else
		{
			updateTransactionValue(updatedTransactionValue, maxValue, isInventoryMax);
			player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1, 2);
		}
		
	}
	
	private void handleTransactionHuman(@NotNull HumanEntity human)
	{
		final String formattedValueAmount = EconomyConfig.getValueFormatted(transactionValue);
		final OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(target);
		
		if (!transactionKind.isBankTransaction() && !targetPlayer.isOnline())
		{
			MenuHolder.close(human);
			human.sendMessage(EconomyMessage.TRANSACTION_TARGETDISCONNECTED.getMessage(new StringPair("user", targetPlayer.getName())));
		}
		else if (economy.getStorage().runTransaction(human.getUniqueId(), target, transactionValue, transactionKind.isBankTransaction()))
		{
			MenuHolder.close(human);
			human.sendMessage(EconomyMessage.TRANSACTION_SENT.getMessage(new StringPair("amount", formattedValueAmount), new StringPair("user", targetPlayer.getName())));
			
			final String otherMessage = EconomyMessage.TRANSACTION_SENT.getMessage(new StringPair("amount", formattedValueAmount), new StringPair("user", human.getName()));
			
			if (targetPlayer.isOnline())
			{
				((Player) targetPlayer).sendMessage(otherMessage);
			}
			else
			{
				economy.getDelayMessageManager().addDelayMessage(target, DelayType.NEXTJOIN, otherMessage);
			}		
		}
		else
		{
			human.sendMessage(EconomyMessage.TRANSACTION_FAIL.getMessage());
		}
	}
	
	private void handleTransactionBank(@NotNull HumanEntity human, boolean fromBalanceTransaction)
	{		
		if (economy.getStorage().runTransaction(human.getUniqueId(), transactionValue, fromBalanceTransaction))
		{		
			MenuHolder.close(human);
			human.sendMessage(fromBalanceTransaction ? EconomyMessage.TRANSACTION_WITHDRAWED.getMessage(new StringPair("amount", EconomyConfig.getValueFormatted(transactionValue)))
					                                 : EconomyMessage.TRANSACTION_DEPOSITED.getMessage(new StringPair("amount", EconomyConfig.getValueFormatted(transactionValue))));
		}	
	}
	
	private void handleTransactionWalletOut(@NotNull HumanEntity human)
	{
		final ItemStack valueItem = EconomyConfig.getValueItem();
		final int maxInventoryValue = InventoryHelper.getFreeItemStackAmount(human.getInventory(), valueItem);
		
		if (transactionValue > maxInventoryValue)
		{
			((Player) human).playSound(human.getLocation(), Sound.ENTITY_EGG_THROW, 1, 2);
			human.sendMessage(LibMessage.PREFIX_GENERAL.getMessage() + "Unzureichender Platz.");	
			return;
		}
		
		if (economy.getStorage().updateWallet(human.getUniqueId(), transactionValue, true) == Storage.CODE_SUCCESS)
		{
			int maxStackSize = valueItem.getMaxStackSize();
			int openTransactionValue = (int) transactionValue;
			
			while (openTransactionValue > 0)
			{
				if (openTransactionValue >= maxStackSize)
				{
					valueItem.setAmount(maxStackSize);
					ItemHelper.applyLore(valueItem, economy.getSignature().createSignatureLore(maxStackSize));
					openTransactionValue -= maxStackSize;
				}
				else
				{
					valueItem.setAmount(openTransactionValue);
					ItemHelper.applyLore(valueItem, economy.getSignature().createSignatureLore(openTransactionValue));
					openTransactionValue = 0;
				}						
				human.getInventory().addItem(valueItem);
			}
			MenuHolder.close(human);
			((Player) human).playSound(human.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1, 2);
			human.sendMessage(EconomyMessage.TRANSACTION_TAKEN.getMessage(new StringPair("amount", EconomyConfig.getValueFormatted(transactionValue)), 
					                                                      new StringPair("walletname", EconomyConfig.WALLET_NAME.getValue())));
		}
	}
	
	private @NotNull ItemStack getAmountItem(int amount, boolean negativeValue)
	{
		final ItemStack amountItem = ItemHelper.getItemWithMeta(negativeValue ? Material.RED_STAINED_GLASS_PANE : Material.LIME_STAINED_GLASS_PANE, 
				                                                negativeValue ? "§c- " + amount : "§a+ " + amount);
		
		return IndependencyHelper.getTaggedItemStack(amountItem, NBT_KEY, Integer.toString(negativeValue ? Math.negateExact(amount) : amount)); 
	}
	
	private @NotNull String getPerformItemName()
	{		
		return "§7» §a" + transactionKind.getLabel();
	}
	
	private void updateTransactionValue(double updatedTransactionValue, double maxTransactionValue, boolean isInventoryMax)
	{	
		transactionValue = updatedTransactionValue;
		
		ItemHelper.updateItemWithMeta(InventoryHelper.getItem(super.getInventory(), 2, 5), 
				                      MessageHelper.getHighlighted(EconomyConfig.getValueFormatted(transactionValue), false, false), 
				                      getMaxValueLore(maxTransactionValue, isInventoryMax));
	}
	
	@NotNull
	private List<String> getMaxValueLore(double maxTransactionValue, boolean isInventoryMax)
	{
		final List<String> valueItemLore = new ArrayList<>();
		
		valueItemLore.add(String.format("§7%s: %.0f", transactionKind.isBankTransaction() ? 
				                                      EconomyConfig.BANK_NAME.getValue() : 
				                                      EconomyConfig.WALLET_NAME.getValue(),
				                                      maxTransactionValue));
		if (isInventoryMax)
		{
			valueItemLore.add(" ");
			valueItemLore.add("§cUnzureichender Platz.");
		}		
		return valueItemLore;
	}
	
	private double getMaxTransactionValue(@NotNull UUID uuid)
	{
		return transactionKind.isBankTransaction() ? economy.getStorage().getBalance(uuid) : economy.getStorage().getWallet(uuid);
	}
}