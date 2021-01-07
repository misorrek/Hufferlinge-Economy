package huff.economy.menuholder;

import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import huff.economy.EconomyInterface;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.ItemHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.menuholder.MenuExitType;
import huff.lib.menuholder.MenuHolder;

public class BankHolder extends MenuHolder
{
	public BankHolder(@NotNull EconomyInterface economyInterface, @NotNull UUID menuViewer, @NotNull Location bankLocation)
	{
		super("economy:bank", InventoryHelper.INV_SIZE_4, economyInterface.getConfig().getBankInventoryName(), MenuExitType.CLOSE);
		
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
	
	@Override
	public boolean handleClick(@NotNull InventoryClickEvent event)
	{
		Validate.notNull((Object) event, "The inventory click event cannot be null.");
		
		final HumanEntity human = event.getWhoClicked();
		final ItemStack currentItem = event.getCurrentItem();
		
		if (ItemHelper.hasMeta(currentItem))
		{
			final String currentItemName = currentItem.getItemMeta().getDisplayName();
			
			if (economy.getConfig().getBankRemoveName().equals(currentItemName))
			{
				economy.getBank().removeBank(bankLocation);			
				economy.tryRemoveBankEntity(bankLocation);
				
				MenuHolder.close(human);
				human.getInventory().addItem(economy.getConfig().getBankSpawnItem());
				human.sendMessage(MessageHelper.PREFIX_HUFF + economy.getConfig().getBankName() + " entfernt.");
				human.sendMessage(MessageHelper.PREFIX_HUFF + "Gegenstand zum Neuerstellen in das Inventar gelegt.");
			}
			else
			{
				TransactionHolder.handleTransactionOpen(economy, human, currentItemName);
			}
		}	
		return true;
	}
	
	private void initInventory()
	{	
		InventoryHelper.setFill(super.getInventory(), InventoryHelper.getBorderItem(), true);		
		
		InventoryHelper.setItem(super.getInventory(), 2, 2, ItemHelper.getItemWithMeta(economy.getConfig().getValueMaterial(), "§7" + economy.getConfig().getBankName() + " : " +
																                MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(economy.getStorage().getBalance(menuViewer)), 
																                false , false)));
		InventoryHelper.setItem(super.getInventory(), 2, 3, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
																				economy.getConfig().getTransactionInventoryName(TransactionKind.BANK_IN)));
		InventoryHelper.setItem(super.getInventory(), 2, 5, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
																				economy.getConfig().getTransactionInventoryName(TransactionKind.BANK_OTHER)));
		InventoryHelper.setItem(super.getInventory(), 2, 7, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
																				economy.getConfig().getTransactionInventoryName(TransactionKind.BANK_OUT)));			
		InventoryHelper.setItem(super.getInventory(), 2, 8, ItemHelper.getItemWithMeta(economy.getConfig().getValueMaterial(), "§7" + economy.getConfig().getWalletName() + " : " + 
																                MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(economy.getStorage().getWallet(menuViewer)), 
																                false , false)));
		
		if (economy.getBank().isOwner(menuViewer, bankLocation))
		{
			InventoryHelper.setItem(super.getInventory(), 4, 9, ItemHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, economy.getConfig().getBankRemoveName()));
		}
		super.setMenuExitItem();
	}
}
