package huff.economy.menuholder;

import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import huff.economy.EconomyConfig;
import huff.economy.EconomyInterface;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.ItemHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.menuholder.MenuExitType;
import huff.lib.menuholder.MenuHolder;

/**
 * A menu class that contains the wallet main menu.
 */
public class WalletHolder extends MenuHolder
{
	public WalletHolder(@NotNull EconomyInterface economyInterface, @NotNull UUID menuViewer)
	{
		super("economy:wallet", InventoryHelper.INV_SIZE_3, EconomyConfig.WALLET_INVNAME.getValue(), MenuExitType.CLOSE);
		
		Validate.notNull((Object) economyInterface, "The economy-interface cannot be null.");
		Validate.notNull((Object) menuViewer, "The menu-viewer cannot be null.");
		
		this.economy = economyInterface;
		this.menuViewer = menuViewer;
		
		initInventory();
	}
	
	private final EconomyInterface economy;
	private final UUID menuViewer;
	
	@Override
	public boolean handleClick(@NotNull InventoryClickEvent event)
	{
		Validate.notNull((Object) event, "The inventory click event cannot be null.");
		
		final HumanEntity human = event.getWhoClicked();
		final ItemStack currentItem = event.getCurrentItem();
		
		if (ItemHelper.hasMeta(currentItem))
		{
			TransactionHolder.handleTransactionOpen(economy, human,  currentItem.getItemMeta().getDisplayName());
		}			
		return true;
	}
	
	private void initInventory()
	{	
		InventoryHelper.setBorder(super.getInventory(), InventoryHelper.getBorderItem());
		InventoryHelper.setFill(super.getInventory(), InventoryHelper.getFillItem(), false);
		
		InventoryHelper.setItem(super.getInventory(), 2, 2, ItemHelper.getItemWithMeta(EconomyConfig.VALUE_MATERIAL.getValue(),
				                                                                       MessageHelper.getHighlighted(EconomyConfig.getValueFormatted(economy.getStorage().getWallet(menuViewer)), 
						                                                               false , false)));
		InventoryHelper.setItem(super.getInventory(), 2, 8, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
																				       EconomyConfig.getTransactionInventoryName(TransactionKind.WALLET_OUT)));
		super.setMenuExitItem();
	}
}
