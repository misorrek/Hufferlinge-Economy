package huff.economy.inventories;

import java.util.UUID;

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

import huff.economy.Config;
import huff.economy.EconomyInterface;
import huff.economy.storage.Storage;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.ItemHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.helper.IndependencyHelper;
import huff.lib.helper.StringHelper;
import huff.lib.inventories.PlayerChooserInventory;
import huff.lib.manager.delayedmessage.DelayType;
import huff.lib.various.MenuInventoryHolder;

public class TransactionInventory extends MenuInventoryHolder
{
	public static final String MENU_IDENTIFIER = "menu:economy:transaction";
	public static final String CHOOSER_KEY = "Transaction";
	
	private static final String NBT_KEY = "changeamount";
	
	private static final int AMOUNT_1 = 1;
	private static final int AMOUNT_2 = 5;
	private static final int AMOUNT_3 = 10;
	private static final int AMOUNT_4 = 100;
	private static final int AMOUNT_5 = 1000;
	
	public TransactionInventory(@NotNull EconomyInterface economyInterface, TransactionKind transactionKind, @Nullable UUID targetUUID)
	{
		super(MENU_IDENTIFIER, InventoryHelper.INV_SIZE_4, transactionKind.getLabel());
		
		Validate.notNull((Object) economyInterface, "The economy-interface cannot be null.");
		Validate.isTrue(targetUUID != null || !transactionKind.isHumanTransaction(), "The target-uuid cannot be null in a human transaction.");
		
		this.economy = economyInterface;
		this.transactionKind = transactionKind;
		this.targetUUID = targetUUID;
		this.transactionValue = 0;
		
		initInventory();
	}
	
	public TransactionInventory(@NotNull EconomyInterface economyInterface, TransactionKind transactionKind)
	{
		this(economyInterface, transactionKind, null);
	}
	
	private final EconomyInterface economy;
	private final TransactionKind transactionKind;
	private final UUID targetUUID;
	
	private double transactionValue;
	
	public static void handleTransactionOpen(@NotNull EconomyInterface economy, @NotNull HumanEntity human, @NotNull String currentItemName)
	{
		Validate.notNull((Object) economy, "The economy-interface who clicked cannot be null.");
		
		if (TransactionKind.isTransaction(currentItemName))
		{
			final TransactionKind transactionKind = TransactionKind.getTransaction(currentItemName);
			
			human.closeInventory();
			
			if (transactionKind == TransactionKind.BANK_OTHER)
			{
				human.openInventory(new PlayerChooserInventory(CHOOSER_KEY, economy.getStorage().getUsers(human.getUniqueId()), InventoryHelper.INV_SIZE_6, null, true).getInventory());
			}
			else
			{						
				human.openInventory(new TransactionInventory(economy, transactionKind).getInventory());
			}
		}
	}
	
	public void handleEvent(@Nullable ItemStack currentItem, @NotNull HumanEntity human)
	{
		Validate.notNull((Object) human, "The human who clicked cannot be null.");
		
		if (currentItem == null || currentItem.getItemMeta() == null)
		{
			return;
		}			
		final String currentItemName = currentItem.getItemMeta().getDisplayName();
		final String amountValue = IndependencyHelper.getTagFromItemStack(currentItem, NBT_KEY);
		
		if (StringHelper.isNotNullOrEmpty(amountValue))
		{					
			handleTransactionValueChange(amountValue, human);
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
			else
			{
				human.closeInventory();
				human.sendMessage(MessageHelper.PREFIX_HUFF + "Die Transaktion konnte nicht abgeschlossen werden.");	
			}	
		}
	}
	
	private void initInventory()
	{	
		InventoryHelper.setFill(this.getInventory(), InventoryHelper.getBorderItem(), true);
		
		InventoryHelper.setItem(this.getInventory(), 2, 2, getAmountItem(AMOUNT_5, false));		
		InventoryHelper.setItem(this.getInventory(), 2, 3, getAmountItem(AMOUNT_4, false));
		InventoryHelper.setItem(this.getInventory(), 2, 4, getAmountItem(AMOUNT_3, false));
		InventoryHelper.setItem(this.getInventory(), 2, 5, ItemHelper.getItemWithMeta(economy.getConfig().getValueMaterial(),
				                                                                      MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(0))));	
		InventoryHelper.setItem(this.getInventory(), 2, 6, getAmountItem(AMOUNT_3, true));
		InventoryHelper.setItem(this.getInventory(), 2, 7, getAmountItem(AMOUNT_4, true));
		InventoryHelper.setItem(this.getInventory(), 2, 8, getAmountItem(AMOUNT_5, true));
		
		InventoryHelper.setItem(this.getInventory(), 3, 3, getAmountItem(AMOUNT_2, false));
		InventoryHelper.setItem(this.getInventory(), 3, 4, getAmountItem(AMOUNT_1, false));
		
		if (targetUUID != null)
		{		
			final OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetUUID);
			
			InventoryHelper.setItem(this.getInventory(), 3, 5, ItemHelper.getSkullWithMeta(targetPlayer, "§7Empfänger: §9" + targetPlayer.getName()));
		}		
		InventoryHelper.setItem(this.getInventory(), 3, 6, getAmountItem(AMOUNT_1, true));
		InventoryHelper.setItem(this.getInventory(), 3, 7, getAmountItem(AMOUNT_2, true));
		
		InventoryHelper.setItem(this.getInventory(), 4, 1,ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getPerformItemName()));
		InventoryHelper.setItem(this.getInventory(), 4, 9, InventoryHelper.getAbortItem());
	}
	
	private void updateTransactionValue(@NotNull Config economyConfig, double updatedTransactionValue)
	{	
		final ItemStack transactionValueItem = InventoryHelper.getItem(this.getInventory(), 2, 5);
		final ItemMeta transactionValueMeta = transactionValueItem.getItemMeta();
		
		transactionValue = updatedTransactionValue;
		
		transactionValueMeta.setDisplayName(MessageHelper.getHighlighted(economyConfig.getValueFormatted(transactionValue)));
		transactionValueItem.setItemMeta(transactionValueMeta);	
	}
	
	private void handleTransactionValueChange(@NotNull String changeValueString, @NotNull HumanEntity human)
	{
		final Player player = (Player) human;
		final int maxInventoryValue = InventoryHelper.getFreeItemStackAmount(human.getInventory(), economy.getConfig().getValueItem());
		final double storageValue = transactionKind.isBankTransaction() ? economy.getStorage().getBalance(player.getUniqueId()) : economy.getStorage().getWallet(player.getUniqueId());
		final double changeValue = Integer.parseInt(changeValueString);
		double updatedTransactionValue = transactionValue + changeValue;
	
		if (updatedTransactionValue < 0)
		{
			updatedTransactionValue = 0;
		}
		else if (transactionKind == TransactionKind.WALLET_OUT && updatedTransactionValue > maxInventoryValue)
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
		else if (updatedTransactionValue > storageValue)
		{
			updatedTransactionValue = storageValue;
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
	
	private void handleTransactionHuman(@NotNull HumanEntity human)
	{
		final String formattedValueAmount = MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(transactionValue));
		final int selfFeedbackcode = transactionKind.isBankTransaction() ? economy.getStorage().updateBalance(human.getUniqueId(), transactionValue, true, false) : economy.getStorage().updateWallet(human.getUniqueId(), transactionValue, true);
		final int otherFeedbackcode = transactionKind.isBankTransaction() ? economy.getStorage().updateBalance(targetUUID, transactionValue, false, false) : economy.getStorage().updateWallet(targetUUID, transactionValue, false);
		
		if (selfFeedbackcode == Storage.CODE_SUCCESS && otherFeedbackcode == Storage.CODE_SUCCESS)
		{
			final OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetUUID);
			
			human.closeInventory();
			human.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du hast", formattedValueAmount, "an",
												 MessageHelper.getHighlighted(targetPlayer.getName()), "übertragen."));
			
			final String otherMessage = StringHelper.build(MessageHelper.PREFIX_HUFF, "Du hast", formattedValueAmount, "von",
                                                           MessageHelper.getHighlighted(human.getName()), "erhalten.");
			
			if (targetPlayer.isOnline())
			{
				((Player) targetPlayer).sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du hast", formattedValueAmount, "von",
									                                   MessageHelper.getHighlighted(human.getName()), "erhalten."));
			}
			else
			{
				economy.getDelayedMessageManager().addDelayedMessage(targetUUID, DelayType.NEXTJOIN, otherMessage);
			}		
		}	
	}
	
	private void handleTransactionBank(@NotNull HumanEntity human, boolean outgoingTransaction)
	{		
		if (economy.getStorage().updateBalance(human.getUniqueId(), transactionValue, outgoingTransaction, true) == Storage.CODE_SUCCESS)
		{		
			human.closeInventory();
			human.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du hast", MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(transactionValue)), 
												 (outgoingTransaction ? "von der Bank ausgezahlt." :"auf die Bank eingezahlt.")));
		}	
	}
	
	private void handleTransactionWalletOut(@NotNull HumanEntity human)
	{
		final ItemStack valueItem = economy.getConfig().getValueItem();
		
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
			human.closeInventory();
			((Player) human).playSound(human.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1, 2);
			human.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du hast", MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(transactionValue)), "aus deinem ",
												 economy.getConfig().getWalletName(), " herausgenommen."));
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
}
