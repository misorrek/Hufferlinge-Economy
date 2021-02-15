package huff.economy.menuholder;

import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import huff.economy.EconomyConfig;
import huff.economy.EconomyInterface;
import huff.economy.EconomyMessage;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.ItemHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.menuholder.MenuExitType;
import huff.lib.menuholder.MenuHolder;

public class BankHolder extends MenuHolder
{
	public BankHolder(@NotNull EconomyInterface economyInterface, @NotNull UUID menuViewer, @NotNull Location bankLocation)
	{
		super("economy:bank", InventoryHelper.INV_SIZE_4, EconomyConfig.BANK_INVNAME.getValue(), MenuExitType.CLOSE);
		
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
			
			if (EconomyConfig.BANK_REMOVENAME.getValue().equals(currentItemName))
			{
				economy.getBank().removeBank(bankLocation);			
				economy.tryRemoveBankEntity(bankLocation);
				
				MenuHolder.close(human);
				human.getInventory().addItem(EconomyConfig.getBankItem());
				human.sendMessage(EconomyMessage.BANK_REMOVE.getMessage());
				human.sendMessage(EconomyMessage.BANK_ITEM.getMessage());
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
		
		InventoryHelper.setItem(super.getInventory(), 2, 2, ItemHelper.getItemWithMeta(EconomyConfig.VALUE_MATERIAL.getValue(), "§7" + EconomyConfig.BANK_NAME.getValue() + " : " +
																                MessageHelper.getHighlighted(EconomyConfig.getValueFormatted(economy.getStorage().getBalance(menuViewer)), 
																                false , false)));
		InventoryHelper.setItem(super.getInventory(), 2, 3, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
																				EconomyConfig.getTransactionInventoryName(TransactionKind.BANK_IN)));
		InventoryHelper.setItem(super.getInventory(), 2, 5, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
																				EconomyConfig.getTransactionInventoryName(TransactionKind.BANK_OTHER)));
		InventoryHelper.setItem(super.getInventory(), 2, 7, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
																				EconomyConfig.getTransactionInventoryName(TransactionKind.BANK_OUT)));			
		InventoryHelper.setItem(super.getInventory(), 2, 8, ItemHelper.getItemWithMeta(EconomyConfig.VALUE_MATERIAL.getValue(), "§7" + EconomyConfig.WALLET_NAME.getValue() + " : " + 
																                MessageHelper.getHighlighted(EconomyConfig.getValueFormatted(economy.getStorage().getWallet(menuViewer)), 
																                false , false)));
		
		if (economy.getBank().isOwner(menuViewer, bankLocation))
		{
			InventoryHelper.setItem(super.getInventory(), 4, 9, ItemHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, EconomyConfig.BANK_REMOVENAME.getValue()));
		}
		super.setMenuExitItem();
	}
}
