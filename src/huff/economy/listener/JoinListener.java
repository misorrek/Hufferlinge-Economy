package huff.economy.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import huff.economy.EconomyConfig;
import huff.economy.EconomyTable;

public class JoinListener implements Listener
{
	public JoinListener(@NotNull EconomyConfig economyConfig, @NotNull EconomyTable economyTable)
	{
		this.economyConfig = economyConfig;
		this.economyTable = economyTable;
	}
	private EconomyConfig economyConfig;
	private EconomyTable economyTable;
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		event.getPlayer().getInventory().setItem(8, WalletUtil.getWalletItem(economyConfig)); //TODO Hat er schon einen?
		
		if (!economyTable.userExist(event.getPlayer().getUniqueId()))
		{
			economyTable.addUser(event.getPlayer().getUniqueId(), 100);
		}
	}
}
