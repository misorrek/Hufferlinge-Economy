package huff.economy.inventories;

import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.economy.EconomyInterface;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.ItemHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.helper.StringHelper;
import huff.lib.various.MenuInventoryHolder;

public class BankInventory extends MenuInventoryHolder
{
	public static final String MENU_IDENTIFIER = "menu:economy:bank";
	
	public BankInventory(@NotNull EconomyInterface economyInterface, @NotNull UUID menuViewer, @NotNull Location bankLocation)
	{
		super(MENU_IDENTIFIER, InventoryHelper.INV_SIZE_4, economyInterface.getConfig().getBankInventoryName());
		
		Validate.notNull((Object) economyInterface, "The economy-interface cannot be null.");
		Validate.notNull((Object) menuViewer, "The menu-viewer cannot be null.");
		Validate.notNull((Object) bankLocation, "The bank-location cannot be null.");

		this.economy = economyInterface;
		this.menuViewer = menuViewer;
		this.bankLocation = bankLocation;
		
		initInventory();
	}
	
	private final EconomyInterface economy;
	private final UUID menuViewer;
	private final Location bankLocation;
	
	public void handleEvent(@Nullable ItemStack currentItem, @NotNull HumanEntity human)
	{
		Validate.notNull((Object) human, "The human cannot be null.");
		
		if (currentItem == null || currentItem.getItemMeta() == null)
		{
			return;
		}	
		final String currentItemName = currentItem.getItemMeta().getDisplayName();
		
		if (economy.getConfig().getBankRemoveName().equals(currentItemName))
		{
			economy.getBank().removeBank(bankLocation);			
			economy.removeBankEntity(bankLocation, true);
			
			human.closeInventory();
			human.getInventory().addItem(economy.getConfig().getBankSpawnItem());
			human.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, economy.getConfig().getBankName(), " entfernt. Item zum Neuerstellen in das Inventar gelegt."));
		}
		else
		{
			TransactionInventory.handleTransactionOpen(economy, human, currentItemName);
		}
	}
	
	private void initInventory()
	{	
		InventoryHelper.setFill(this.getInventory(), InventoryHelper.getBorderItem(), true);		
		
		InventoryHelper.setItem(this.getInventory(), 2, 2, ItemHelper.getItemWithMeta(economy.getConfig().getValueMaterial(), "§7" + economy.getConfig().getBankName() + " : " +
																                MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(economy.getStorage().getBalance(menuViewer)), 
																                false , false)));
		InventoryHelper.setItem(this.getInventory(), 2, 3, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
																				economy.getConfig().getTransactionInventoryName(TransactionKind.BANK_IN)));
		InventoryHelper.setItem(this.getInventory(), 2, 5, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
																				economy.getConfig().getTransactionInventoryName(TransactionKind.BANK_OTHER)));
		InventoryHelper.setItem(this.getInventory(), 2, 7, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
																				economy.getConfig().getTransactionInventoryName(TransactionKind.BANK_OUT)));			
		InventoryHelper.setItem(this.getInventory(), 2, 8, ItemHelper.getItemWithMeta(economy.getConfig().getValueMaterial(), "§7" + economy.getConfig().getWalletName() + " : " + 
																                MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(economy.getStorage().getWallet(menuViewer)), 
																                false , false)));
		InventoryHelper.setItem(this.getInventory(), 4, 5, InventoryHelper.getCloseItem());
					
		if (economy.getBank().isOwner(menuViewer, bankLocation))
		{
			InventoryHelper.setItem(this.getInventory(), 4, 9, ItemHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, economy.getConfig().getBankRemoveName()));
		}
	}
}
