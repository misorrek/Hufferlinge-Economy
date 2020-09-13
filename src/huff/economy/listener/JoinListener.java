package huff.economy.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import huff.economy.EconomyConfig;
import huff.economy.storage.EconomyStorage;

public class JoinListener implements Listener
{
	public JoinListener(@NotNull EconomyConfig economyConfig, @NotNull EconomyStorage economyStorage)
	{
		this.economyConfig = economyConfig;
		this.economyStorage = economyStorage;
	}
	private EconomyConfig economyConfig;
	private EconomyStorage economyStorage;
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		final Player player = event.getPlayer();
		final Inventory playerInventory = player.getInventory();
		final ItemStack walletItem = economyConfig.getWalletItem();
		
		if (!playerInventory.contains(walletItem))
		{
			playerInventory.setItem(8, walletItem);
		}
		
		if (!economyStorage.existUser(player.getUniqueId()))
		{
			economyStorage.addUser(player.getUniqueId(), economyConfig.getStartBalance());
		}
	}
}
