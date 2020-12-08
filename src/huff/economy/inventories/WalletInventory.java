package huff.economy.inventories;

import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.economy.EconomyInterface;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.ItemHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.various.MenuInventoryHolder;

public class WalletInventory extends MenuInventoryHolder
{
	public static final String MENU_IDENTIFIER = "menu:economy:wallet";
	
	public WalletInventory(@NotNull EconomyInterface economyInterface, @NotNull UUID menuViewer)
	{
		super(MENU_IDENTIFIER, InventoryHelper.INV_SIZE_3, economyInterface.getConfig().getWalletInventoryName());
		
		Validate.notNull((Object) economyInterface, "The economy-interface cannot be null.");
		Validate.notNull((Object) menuViewer, "The menu-viewer cannot be null.");
		
		this.economy = economyInterface;
		this.menuViewer = menuViewer;
		
		initInventory();
	}
	
	private final EconomyInterface economy;
	private final UUID menuViewer;
	
	public void handleEvent(@Nullable ItemStack currentItem, @NotNull HumanEntity human)
	{
		Validate.notNull((Object) human, "The human cannot be null.");
		
		if (currentItem == null || currentItem.getItemMeta() == null)
		{
			return;
		}			
		TransactionInventory.handleTransactionOpen(economy, human,  currentItem.getItemMeta().getDisplayName());
	}
	
	private void initInventory()
	{	
		InventoryHelper.setBorder(this.getInventory(), InventoryHelper.getBorderItem());
		InventoryHelper.setFill(this.getInventory(), InventoryHelper.getFillItem(), false);
		
		InventoryHelper.setItem(this.getInventory(), 2, 2, ItemHelper.getItemWithMeta(economy.getConfig().getValueMaterial(), 
				                                                                  MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(economy.getStorage().getWallet(menuViewer)), 
						                                                          false , false)));
		InventoryHelper.setItem(this.getInventory(), 2, 8, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
																				  economy.getConfig().getTransactionInventoryName(TransactionKind.WALLET_OUT)));
		InventoryHelper.setItem(this.getInventory(), 3, 5, InventoryHelper.getCloseItem());
	}
}
