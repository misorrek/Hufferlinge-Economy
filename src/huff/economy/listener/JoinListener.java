package huff.economy.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import huff.economy.EconomyTable;

public class JoinListener implements Listener
{
	public JoinListener(@NotNull EconomyTable economyTable)
	{
		this.economyTable = economyTable;
	}
	private EconomyTable economyTable;
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		if (!economyTable.userExist(event.getPlayer().getUniqueId()))
		{
			economyTable.addUser(event.getPlayer().getUniqueId(), 100);
		}
	}
}
