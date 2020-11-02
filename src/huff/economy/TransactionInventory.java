package huff.economy;

import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.economy.storage.EconomyStorage;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.ItemHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.helper.StringHelper;
import huff.lib.manager.delayedmessage.DelayType;
import huff.lib.manager.delayedmessage.MessageType;
import huff.lib.various.ExpandableInventory;

public class TransactionInventory extends ExpandableInventory
{
	private static final int AMOUNT_1 = 1;
	private static final int AMOUNT_2 = 5;
	private static final int AMOUNT_3 = 10;
	private static final int AMOUNT_4 = 100;
	private static final int AMOUNT_5 = 1000;
	
	public TransactionInventory(@NotNull EconomyConfig economyConfig, TransactionKind transactionKind, @Nullable UUID targetUUID)
	{
		super(InventoryHelper.INV_SIZE_4, transactionKind.getLabel());
		
		Validate.isTrue(targetUUID != null || !transactionKind.isHumanTransaction(), "The target-uuid cannot be null in a human transaction.");
		
		this.transactionKind = transactionKind;
		this.targetUUID = targetUUID;
		this.transactionValue = 0;
		
		initInventory(economyConfig);
	}
	
	public TransactionInventory(@NotNull EconomyConfig economyConfig, TransactionKind transactionKind)
	{
		this(economyConfig, transactionKind, null);
	}
	
	private final TransactionKind transactionKind;
	private final UUID targetUUID;
	
	private double transactionValue;
	
	public void handleEvent(@NotNull EconomyInterface economy, @Nullable ItemStack currentItem, @NotNull HumanEntity human)
	{
		Validate.notNull((Object) economy, "The economy-interface cannot be null.");
		Validate.notNull((Object) human, "The human who clicked cannot be null.");
		
		if (currentItem == null || currentItem.getItemMeta() == null)
		{
			return;
		}	
		final String currentItemName = currentItem.getItemMeta().getDisplayName();
		
		if (StringHelper.contains(false, currentItemName, "+", "-"))
		{		
			handleTransactionValueChange(economy, currentItemName, human);
		}
		else if (currentItemName.equals(getPerformItemName()))
		{
			if (transactionKind == TransactionKind.BANK_OTHER || transactionKind == TransactionKind.WALLET_OTHER)
			{			
				handleTransactionHuman(economy, human);			
			}
			else if (transactionKind == TransactionKind.BANK_IN)
			{				
				handleTransactionBank(economy, human, false);
			}
			else if (transactionKind == TransactionKind.BANK_OUT)
			{
				handleTransactionBank(economy, human, true);
			}
			else if (transactionKind == TransactionKind.WALLET_OUT)
			{
				handleTransactionWalletOut(economy, human);			
			}
			human.closeInventory();
			human.sendMessage(MessageHelper.PREFIX_HUFF + "Die Transaktion konnte nicht abgeschlossen werden.");	
		}
	}
	
	private void initInventory(@NotNull EconomyConfig economyConfig)
	{	
		InventoryHelper.setFill(this.getInventory(), InventoryHelper.getBorderItem(), true);
	
		InventoryHelper.setItem(this.getInventory(), 2, 2, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_5, false)));
		InventoryHelper.setItem(this.getInventory(), 2, 3, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_4, false)));
		InventoryHelper.setItem(this.getInventory(), 2, 4, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_3, false)));
		InventoryHelper.setItem(this.getInventory(), 2, 5, ItemHelper.getItemWithMeta(economyConfig.getValueMaterial(), MessageHelper.getHighlighted(economyConfig.getValueFormatted(0))));
		InventoryHelper.setItem(this.getInventory(), 2, 6, ItemHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_3, true)));
		InventoryHelper.setItem(this.getInventory(), 2, 7, ItemHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_4, true)));
		InventoryHelper.setItem(this.getInventory(), 2, 8, ItemHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_5, true)));
		
		InventoryHelper.setItem(this.getInventory(), 3, 3, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_2, false)));
		InventoryHelper.setItem(this.getInventory(), 3, 4, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_1, false)));
		
		if (targetUUID != null)
		{		
			final OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetUUID);
			
			InventoryHelper.setItem(this.getInventory(), 3, 5, ItemHelper.getSkullWithMeta(targetPlayer, "§7Empfänger: §9" + targetPlayer.getName()));
		}		
		InventoryHelper.setItem(this.getInventory(), 3, 6, ItemHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_1, true)));
		InventoryHelper.setItem(this.getInventory(), 3, 7, ItemHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_2, true)));
		
		InventoryHelper.setItem(this.getInventory(), 4, 1,ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getPerformItemName()));
		InventoryHelper.setItem(this.getInventory(), 4, 9, InventoryHelper.getAbortItem());
	}
	
	private void updateTransactionValue(@NotNull EconomyConfig economyConfig, double updatedTransactionValue)
	{	
		final ItemStack transactionValueItem = InventoryHelper.getItem(this.getInventory(), 2, 5);
		final ItemMeta transactionValueMeta = transactionValueItem.getItemMeta();
		
		transactionValue = updatedTransactionValue;
		
		transactionValueMeta.setDisplayName(MessageHelper.getHighlighted(economyConfig.getValueFormatted(transactionValue)));
		transactionValueItem.setItemMeta(transactionValueMeta);	
	}
	
	private void handleTransactionValueChange(@NotNull EconomyInterface economy, @NotNull String currentItemName, @NotNull HumanEntity human)
	{
		final Player player = (Player) human;
		final int maxInventoryValue = InventoryHelper.getFreeItemStackAmount(this.getInventory(), economy.getConfig().getValueItem());
		final double storageValue = transactionKind.isBankTransaction() ? economy.getStorage().getBalance(player.getUniqueId()) : economy.getStorage().getWallet(player.getUniqueId());
		final double changeValue = getAmountFromItemName(currentItemName);
		
		double updatedTransactionValue = transactionValue + changeValue;
		
		if (updatedTransactionValue < 0)
		{
			if (transactionValue > 0)
			{
				updatedTransactionValue = 0;
			}
		}
		else if (transactionKind == TransactionKind.WALLET_OUT && updatedTransactionValue > maxInventoryValue)
		{
			if (transactionValue < maxInventoryValue)
			{
				if (maxInventoryValue > storageValue)
				{
					updatedTransactionValue = storageValue;
				}
				else
				{
					updatedTransactionValue = maxInventoryValue;
				}				
			}
		}
		else if (updatedTransactionValue > storageValue)
		{
			if (transactionValue < storageValue)
			{
				updatedTransactionValue = storageValue;
			}
		}
		else
		{
			transactionValue = updatedTransactionValue;
		}		
		
		if (transactionValue != updatedTransactionValue)
		{
			updateTransactionValue(economy.getConfig(), updatedTransactionValue);
			player.playSound(player.getLocation(), (changeValue < 0 ? Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF : Sound.ENTITY_EXPERIENCE_ORB_PICKUP), 1, 2);
		}
		else
		{
			player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1, 2);
		}
	}
	
	private void handleTransactionHuman(@NotNull EconomyInterface economy, @NotNull HumanEntity human)
	{
		final String formattedValueAmount = MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(transactionValue));
		final int selfFeedbackcode = transactionKind.isBankTransaction() ? economy.getStorage().updateBalance(human.getUniqueId(), transactionValue, true, false) : economy.getStorage().updateWallet(human.getUniqueId(), transactionValue, true);
		final int otherFeedbackcode = transactionKind.isBankTransaction() ? economy.getStorage().updateBalance(targetUUID, transactionValue, false, false) : economy.getStorage().updateWallet(targetUUID, transactionValue, false);
		
		if (selfFeedbackcode == EconomyStorage.CODE_SUCCESS && otherFeedbackcode == EconomyStorage.CODE_SUCCESS)
		{
			final OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetUUID);
			
			human.closeInventory();
			human.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du hast", formattedValueAmount, "an",
												 MessageHelper.getHighlighted(targetPlayer.getName()), "übergeben."));
			
			final String otherMessage = StringHelper.build(MessageHelper.PREFIX_HUFF, "Du hast", formattedValueAmount, "von",
                                                           MessageHelper.getHighlighted(human.getName()), "erhalten.");
			
			if (targetPlayer.isOnline())
			{
				((Player) targetPlayer).sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du hast", formattedValueAmount, "von",
									                                   MessageHelper.getHighlighted(human.getName()), "erhalten."));
			}
			else
			{
				economy.getDelayedMessageManager().addDelayedMessage(targetUUID, DelayType.NEXTJOIN, MessageType.INFO, otherMessage);
			}		
		}	
	}
	
	private void handleTransactionBank(@NotNull EconomyInterface economy, @NotNull HumanEntity human, boolean outgoingTransaction)
	{		
		if (economy.getStorage().updateBalance(human.getUniqueId(), transactionValue, outgoingTransaction, true) == EconomyStorage.CODE_SUCCESS)
		{		
			human.closeInventory();
			human.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du hast", MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(transactionValue)), "auf die",
												 economy.getConfig().getBankName(), " eingezahlt."));
		}	
	}
	
	private void handleTransactionWalletOut(@NotNull EconomyInterface economy, @NotNull HumanEntity human)
	{
		final ItemStack valueItem = economy.getConfig().getValueItem();
		
		if (economy.getStorage().updateWallet(human.getUniqueId(), transactionValue, true) == EconomyStorage.CODE_SUCCESS)
		{
			int maxStackSize = valueItem.getMaxStackSize();
			
			while ((int) transactionValue > 0)
			{
				if ((int) transactionValue >= maxStackSize)
				{
					valueItem.setAmount(maxStackSize);
					ItemHelper.applyLore(valueItem, economy.getSignature().createSignatureLore(maxStackSize));
					transactionValue -= maxStackSize;
				}
				else
				{
					valueItem.setAmount((int) transactionValue);
					ItemHelper.applyLore(valueItem, economy.getSignature().createSignatureLore((int) transactionValue));
					transactionValue = 0;
				}						
				human.getInventory().addItem(valueItem);
			}
			human.closeInventory();
			human.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du hast", MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(transactionValue)), "aus deinem ",
												 economy.getConfig().getWalletName(), " herausgenommen."));
		}
	}
	
	private @NotNull String getAmountItemName(int amount, boolean negativeValue)
	{
		return negativeValue ? "§c- " + amount : "§a+ " + amount;
	}
	
	private double getAmountFromItemName(@NotNull String amountItemName)
	{
		try
		{
			final Pattern valuePattern = Pattern.compile("§.([+-]) ([0-9]*)");
			final Matcher matcher = valuePattern.matcher(amountItemName);				
			
			if (matcher.find())
			{					
				final double changeValue = Double.parseDouble(matcher.group(2));
				
				return matcher.group(1).equals("-") ? changeValue * -1 : changeValue;
			}
		}
		catch (NumberFormatException exception)
		{
			Bukkit.getLogger().log(Level.WARNING, "The transaction-change-value is invalid.", exception);
			return 0;
		}
		return 0;
	}
	
	private @NotNull String getPerformItemName()
	{		
		return StringHelper.build("§7» §a", transactionKind.getLabel());
	}
}
