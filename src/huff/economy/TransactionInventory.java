package huff.economy;

import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.lib.helper.InventoryHelper;
import huff.lib.helper.ItemHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.helper.StringHelper;
import huff.lib.various.ExpandableInventory;

public class TransactionInventory extends ExpandableInventory
{
	public static final int AMOUNT_1 = 1;
	public static final int AMOUNT_2 = 5;
	public static final int AMOUNT_3 = 10;
	public static final int AMOUNT_4 = 100;
	public static final int AMOUNT_5 = 1000;
	
	public TransactionInventory(TransactionKind transactionKind, @Nullable UUID targetUUID)
	{
		super(null, InventoryHelper.INV_SIZE_4, transactionKind.getLabel());
		
		this.transactionKind = transactionKind;
		this.targetUUID = targetUUID;
		this.transactionValue = 0;
		
		initInventory();
	}
	
	private final TransactionKind transactionKind;
	private final UUID targetUUID;
	
	private double transactionValue;
	
	public void handleEvent(@NotNull EconomyInterface economy, @Nullable ItemStack currentItem, @NotNull HumanEntity human)
	{
		Validate.notNull((Object) economy, "The economy-interface cannot be null.");
		Validate.notNull((Object) human, "The human who clicked cannot be null.");
		
		final String currentItemName = currentItem.getItemMeta().getDisplayName();
		
		if (StringHelper.contains(false, currentItemName, "+", "-"))
		{		
			final Player player = (Player) human;
			final double storageValue = transactionKind.isBankTransaction ? economy.getStorage().getBalance() : economy.getStorage().getWallet);
			final int changeValue = getAmountFromItemName(currentItemName);
			
			double updatedTransactionValue = transactionValue + changeValue;
			
			if (updatedTransactionValue < 0)
			{
				if (transactionValue > 0)
				{
					updatedTransactionValue = 0;
				}
				else
				{
					player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1, 2);
					return;
				}
			}	
			else if (updatedTransactionValue > storageValue)
			{
				if (transactionValue < storageValue)
				{
					updatedTransactionValue = storageValue;
				}
				else
				{
					player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1, 2);
					return;
				}
			}
			updateTransactionValue(economy.getConfig());
			player.playSound(player.getLocation(), (changeValue < 0 ? Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF : Sound.ENTITY_EXPERIENCE_ORB_PICKUP), 1, 2);
			player.closeInventory();
		}
		else if (currentItemName.equals(getPerformItemName())
		{
			final String formattedValueAmount = MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(transactionValue));
			
			if (targetUUID != null)
			{			
				final int selfFeedbackcode = transactionKind.isBankTransaction() ? economy.getStorage().updateBalance(human.getUniqueId(), transactionValue, true, false) : economy.getStorage().updateWallet(human.getUniqueId(), transactionValue, true);
				final int otherFeedbackcode = transactionKind.isBankTransaction() ? economy.getStorage().updateBalance(targetUUID, transactionValue, false, false) : economy.getStorage().updateWallet(targetUUID, transactionValue, false);
				
				if (selfFeedbackcode == EconomyStorage.CODE_SUCCESS && otherFeedbackcode == EconomyStorage.CODE_SUCCESS)
				{
					human.closeInventory();
					human.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du hast", formattedValueAmount, "an",
														 MessageHelper.getHighlighted(targetPlayer.getName()), "übergeben."));
					
					if (Bukkit.getOfflinePlayer(targetUUID).isOnline())
					{
						targetPlayer.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du hast", formattedValueAmount, "von",
											                        MessageHelper.getHighlighted(human.getName()), "erhalten."));
					}
					else
					{
						//TODO DelayedMessage
					}				
					return;
				}				
			}
			else if (transactionKind == TransactionKind.WALLET_OUT_BANK)
			{				
				if (economy.getStorage().updateBalance(human.getUniqueId(), transactionValue, false, true) == EconomyStorage.CODE_SUCCESS)
				{		
					human.closeInventory();
					human.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du hast", formattedValueAmount, "auf die",
														 economy.getConfig().getBankName(), " eingezahlt."));
					return;
				}	
			}
			else if (transactionKind == TransactionKind.BANK_OUT)
			{
				if (economy.getStorage().updateBalance(human.getUniqueId(), transactionValue, true, true) == EconomyStorage.CODE_SUCCESS)
				{		
					human.closeInventory();
					human.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du hast", formattedValueAmount, "von der",
														 economy.getConfig().getBankName(), " abgehoben."));
					return;
				}	
			}
			else if (transactionKind == TransactionKind.WALLET_OUT)
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
							applyLore(valueItem, economy.getSignature().createSignatureLore(maxStackSize));
							transactionValue -= maxStackSize;
						}
						else
						{
							valueItem.setAmount((int) transactionValue);
							applyLore(valueItem, economy.getSignature().createSignatureLore((int) transactionValue));
							transactionValue = 0;
						}						
						human.getInventory().addItem(valueItem);
					}
					human.closeInventory();
					human.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du hast", formattedValueAmount, "aus deinem ",
														             economy.getConfig().getWalletName(), " herausgenommen."));
					return;
				}					
			}
			human.closeInventory();
			human.sendMessage(MessageHelper.PREFIX_HUFF + "Die Transaktion konnte nicht abgeschlossen werden.");	
		}
	}
	
	private void initInventory()
	{	
		InventoryHelper.setFill(this, InventoryHelper.getBorderItem(), true);
		

		InventoryHelper.setItem(this, 2, 2, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_5, false)));
		InventoryHelper.setItem(this, 2, 3, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_4, false)));
		InventoryHelper.setItem(this, 2, 4, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_3, false)));
		
		InventoryHelper.setItem(this, 2, 6, ItemHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_3, true)));
		InventoryHelper.setItem(this, 2, 7, ItemHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_4, true)));
		InventoryHelper.setItem(this, 2, 8, ItemHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_5, true)));
		
		InventoryHelper.setItem(this, 3, 3, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_2, false)));
		InventoryHelper.setItem(this, 3, 4, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_1, false)));
		
		if (targetUUID != null)
		{		
			final OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetUUID);
			
			InventoryHelper.setItem(this, 3, 5, ItemHelper.getSkullWithMeta(targetPlayer, "§7Empfänger: §9" + targetPlayer.getName()));
		}
		
		InventoryHelper.setItem(this, 3, 6, ItemHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_1, true)));
		InventoryHelper.setItem(this, 3, 7, ItemHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, getAmountItemName(AMOUNT_2, true)));
		
		InventoryHelper.setItem(this, 4, 1,ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, getPerformItemName()));
		InventoryHelper.setItem(this, 4, 9, InventoryHelper.getAbortItem());
	}
	
	private @Nullable Player getPayTargetPlayer(@NotNull Inventory payInventory)
	{
		Validate.notNull((Object) payInventory, "The pay-inventory cannot be null.");
		
		final ItemStack targetItem = payInventory.getItem(22);
		
		if (targetItem == null)
		{
			return null;
		}	
		final Pattern valuePattern = Pattern.compile("§.*: §.(.*)");
		final Matcher matcher = valuePattern.matcher(targetItem.getItemMeta().getDisplayName());
		
		if (matcher.find())
		{
			return Bukkit.getPlayer(matcher.group(1));
		}
		return null;
	}
	
	private @Nullable double getPayValueAmount(@NotNull Inventory payInventory)
	{
		Validate.notNull((Object) payInventory, "The pay-inventory cannot be null.");
		
		final ItemStack targetItem = payInventory.getItem(13);
		
		if (targetItem != null)
		{
			try
			{
				final Pattern valuePattern = Pattern.compile("§.([0-9.]*).*");
				final Matcher matcher = valuePattern.matcher(targetItem.getItemMeta().getDisplayName());
				
				if (matcher.find())
				{
					return Double.parseDouble(matcher.group(1));
				}
			}
			catch (NumberFormatException exception)
			{
				Bukkit.getLogger().log(Level.WARNING, "The pay-value-amount is invalid.", exception);
			}
		}
		return 0;
	} 
	
	private void updateTransactionValue(@NotNull EconomyConfig economyConfig)
	{	
		final ItemStack transactionValueItem = InventoryHelper.getItem(this, 2, 5);
		final ItemMeta transactionValueMeta = transactionValueItem.getItemMeta();
		
		transactionValueMeta.setDisplayName(MessageHelper.getHighlighted(economyConfig.getValueFormatted(transactionValue)));
		transactionValueItem.setItemMeta(transactionValueMeta);	
	}
	
	private void applyLore(@NotNull ItemStack loreItem, @NotNull List<String> lore) //TODO Move to ItemHelper
	{
		ItemMeta loreMeta = loreItem.getItemMeta();
		loreMeta.setLore(lore);
		loreItem.setItemMeta(loreMeta);
	}
	
	private @NotNull String getAmountItemName(int amount, boolean negativeValue)
	{
		return negativeValue ? "§c- " + amount : "§a+ " + amount;
	}
	
	private int getAmountFromItemName(@NotNull String amountItemName)
	{
		
	}
	
	private @NotNull String getPerformItemName()
	{		
		return StringHelper.build("§7» §a", transactionKind.getLabel());
	}
}
