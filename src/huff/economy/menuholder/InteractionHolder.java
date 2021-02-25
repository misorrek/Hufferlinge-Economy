package huff.economy.menuholder;

import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import huff.economy.EconomyConfig;
import huff.economy.EconomyInterface;
import huff.economy.EconomyMessage;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.ItemHelper;
import huff.lib.menuholder.MenuExitType;
import huff.lib.menuholder.MenuHolder;
import huff.lib.various.structures.StringPair;

/**
 * A menu class that contains the menu that shows up when a player interact with the wallet to another player.
 */
public class InteractionHolder extends MenuHolder
{
	public InteractionHolder(@NotNull EconomyInterface economyInterface, @NotNull UUID menuViewer, @NotNull UUID interactionTarget)
	{
		super("economy:interaction", InventoryHelper.INV_SIZE_3, "§8Interaktion wählen", MenuExitType.CLOSE);
		
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

	@Override
	public boolean handleClick(@NotNull InventoryClickEvent event)
	{
		Validate.notNull((Object) event, "The inventory click event cannot be null.");
		
		final HumanEntity human = event.getWhoClicked();
		final ItemStack currentItem = event.getCurrentItem();
		
		if (ItemHelper.hasMeta(currentItem))
		{
			if (currentItem.getType() == EconomyConfig.VALUE_MATERIAL.getValue())
			{
				new TransactionHolder(economy, TransactionKind.WALLET_OTHER, human.getUniqueId(), interactionTarget).open(human);
			}
			else if (currentItem.getType() == EconomyConfig.TRADE_MATERIAL.getValue())
			{
				final Player targetPlayer = Bukkit.getPlayer(interactionTarget);
				
				if (targetPlayer != null)
				{
					if (InventoryHelper.isInternalCraftView(targetPlayer.getOpenInventory()))
					{
						new TradeHolder(economy, menuViewer, interactionTarget);
					}
					else
					{
						human.sendMessage(EconomyMessage.TRADE_NOTALLOWED.getValue(new StringPair("user", targetPlayer.getName())));
					}
				}
				else
				{
					MenuHolder.close(human);
				}
			}
		}			
		return true;
	}
	
	private void initInventory()
	{
		final OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(interactionTarget);
		
		InventoryHelper.setBorder(super.getInventory(), InventoryHelper.getBorderItem());
		InventoryHelper.setFill(super.getInventory(), InventoryHelper.getFillItem(), false);
		
		InventoryHelper.setItem(super.getInventory(), 2, 2, ItemHelper.getItemWithMeta(EconomyConfig.VALUE_MATERIAL.getValue(), 
				                                                                       EconomyConfig.getTransactionInventoryName(TransactionKind.WALLET_OTHER))); 
		
		InventoryHelper.setItem(super.getInventory(), 3, 5, ItemHelper.getSkullWithMeta(targetPlayer, EconomyConfig.TRANSACTION_RECEIVER.getValue(new StringPair("user", targetPlayer.getName()))));
	
		InventoryHelper.setItem(super.getInventory(), 2, 8, ItemHelper.getItemWithMeta(EconomyConfig.TRADE_MATERIAL.getValue(),
				                                                                       EconomyConfig.TRADE_INVNAME.getValue()));
		super.setMenuExitItem();
	}
}
