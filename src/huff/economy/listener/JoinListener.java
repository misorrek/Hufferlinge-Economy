package huff.economy.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import huff.economy.EconomyConfig;
import huff.economy.EconomyStorage;

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
		event.getPlayer().getInventory().setItem(8, WalletUtil.getWalletItem(economyConfig)); //TODO Hat er schon einen?
		
		if (!economyStorage.existUser(event.getPlayer().getUniqueId()))
		{
			economyStorage.addUser(event.getPlayer().getUniqueId(), 100);
		}
	}
}
