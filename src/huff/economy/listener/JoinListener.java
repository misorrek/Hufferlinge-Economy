package huff.economy.listener;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import huff.economy.EconomyInterface;
import huff.lib.helper.InventoryHelper;
import huff.lib.manager.delaymessage.DelayType;

public class JoinListener implements Listener
{
	public JoinListener(@NotNull EconomyInterface economyInterface)
	{
		Validate.notNull((Object) economyInterface, "The economy-interface cannot be null.");
		
		this.economy = economyInterface;
	}
	private final EconomyInterface economy;
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		final Player player = event.getPlayer();
		final Inventory playerInventory = player.getInventory();
		final ItemStack walletItem = economy.getConfig().getWalletItem();
		
		if (!playerInventory.contains(walletItem))
		{
			if (InventoryHelper.getFreeSlotCount(playerInventory) > 0)
			{
				playerInventory.addItem(walletItem);
			}
			else
			{
				playerInventory.setItem(economy.getConfig().getWalletDefaultSlot(), walletItem);
			}
		}
		
		if (!economy.getStorage().existUser(player.getUniqueId()))
		{
			economy.getStorage().addUser(player.getUniqueId(), economy.getConfig().getStartBalance());
		}
		economy.getDelayMessageManager().sendDelayMessages(player, DelayType.NEXTJOIN);
	}
}
