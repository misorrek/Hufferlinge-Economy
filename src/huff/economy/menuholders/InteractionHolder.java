package huff.economy.menuholders;

import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.economy.EconomyInterface;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.ItemHelper;
import huff.lib.various.MenuHolder;

public class InteractionHolder extends MenuHolder
{
public static final String MENU_IDENTIFIER = "menu:economy:interaction";
	
	public InteractionHolder(@NotNull EconomyInterface economyInterface, @NotNull UUID menuViewer, @NotNull UUID interactionTarget)
	{
		super(MENU_IDENTIFIER, InventoryHelper.INV_SIZE_3, "§8Interaktion wählen");
		
		Validate.notNull((Object) economyInterface, "The economy-interface cannot be null.");
		Validate.notNull((Object) menuViewer, "The menu-viewer cannot be null.");
		Validate.notNull((Object) interactionTarget, "The interaction target cannot be null.");
		
		this.economy = economyInterface;
		this.menuViewer = menuViewer;
		this.interactionTarget = interactionTarget;
		
		initInventory();
	}

	private final EconomyInterface economy;
	private final UUID menuViewer;
	private final UUID interactionTarget;

	public void handleEvent(@Nullable ItemStack currentItem, @NotNull HumanEntity human)
	{
		Validate.notNull((Object) human, "The human cannot be null.");
		
		if (currentItem == null || currentItem.getItemMeta() == null)
		{
			return;
		}			
		
		if (currentItem.getType() == economy.getConfig().getValueMaterial())
		{
			human.closeInventory();
			human.openInventory(new TransactionHolder(economy, TransactionKind.WALLET_OTHER, interactionTarget).getInventory());
		}
		else if (economy.getConfig().equalsTradeItem(currentItem))
		{
			new TradeHolder(economy, menuViewer, interactionTarget);
		}
	}
	
	private void initInventory()
	{
		final OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(interactionTarget);
		
		InventoryHelper.setBorder(this.getInventory(), InventoryHelper.getBorderItem());
		InventoryHelper.setFill(this.getInventory(), InventoryHelper.getFillItem(), false);
		
		InventoryHelper.setItem(this.getInventory(), 2, 2, ItemHelper.getItemWithMeta(economy.getConfig().getValueMaterial(), 
				                                                                      economy.getConfig().getTransactionInventoryName(TransactionKind.WALLET_OTHER))); 
		
		InventoryHelper.setItem(this.getInventory(), 3, 5, ItemHelper.getSkullWithMeta(targetPlayer, "§7Empfänger: §9" + targetPlayer.getName()));
	
		InventoryHelper.setItem(this.getInventory(), 2, 8, ItemHelper.getItemWithMeta(economy.getConfig().getTradeMaterial(),
				                                                                      economy.getConfig().getTradeInventoryName()));
		InventoryHelper.setItem(this.getInventory(), 3, 5, InventoryHelper.getCloseItem());
	}
}
