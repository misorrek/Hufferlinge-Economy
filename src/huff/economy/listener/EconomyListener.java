package huff.economy.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.economy.EconomyInterface;
import huff.economy.menuholders.BankHolder;
import huff.economy.menuholders.InteractionHolder;
import huff.economy.menuholders.WalletHolder;
import huff.economy.storage.Bank;
import huff.economy.storage.Storage;
import huff.lib.helper.ItemHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.helper.StringHelper;

public class EconomyListener implements Listener
{
	public EconomyListener(@NotNull EconomyInterface economyInterface)
	{
		Validate.notNull((Object) economyInterface, "The economy-interface cannot be null.");
		
		this.economy = economyInterface;
	}
	private final EconomyInterface economy;
	private final Map<UUID, Integer> lastWalletSlot = new HashMap<>();
	
	// B L O C K S
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		final ItemStack placedItem = event.getItemInHand();
		
		if (economy.getConfig().equalsWalletItem(placedItem) || 
			economy.getConfig().equalsValueItem(placedItem))
		{
			event.setCancelled(true);
			return;
		}
		event.setCancelled(handleBankBlockInteract(event.getPlayer(), event.getBlock().getLocation(), null));
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event)
	{
		event.setCancelled(handleBankBlockInteract(event.getPlayer(), event.getBlock().getLocation(), event.getBlock()));
	}
	
	private boolean handleBankBlockInteract(@NotNull Player player, @NotNull Location location, @Nullable Block block)
	{
		if (block == null || block.getType() == economy.getConfig().getBankMaterial())
		{			
			for (Location bankLocation : economy.getBank().getBankLocations())
			{
				if (bankLocation.distance(location) <= 1)
				{
					player.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Das erlaubt der ", economy.getConfig().getBankName(), " nicht."));
					return true;
				}
			}
		}
		return false;
	}
	
	// E N T I T Y
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		final List<ItemStack> playerDrops = event.getDrops();
		ItemStack walletItem = null;
		
		for (ItemStack playerDrop : playerDrops)
		{
			if (economy.getConfig().equalsWalletItem(playerDrop))
			{
				walletItem = playerDrop;
				lastWalletSlot.put(event.getEntity().getUniqueId(), event.getEntity().getInventory().first(playerDrop));
			}
		}
		
		if (walletItem != null)
		{
			int dropValueAmount = (int) (economy.getStorage().getWallet(event.getEntity().getUniqueId()) * 0.01);
			
			if (economy.getStorage().updateWallet(event.getEntity().getUniqueId(), dropValueAmount, true) == Storage.CODE_SUCCESS)
			{
				while (dropValueAmount > 0)
				{
					final ItemStack dropValueItem = economy.getConfig().getValueItem();
					final int maxValueItemStackSize = dropValueItem.getMaxStackSize();
					
					if (dropValueAmount >= maxValueItemStackSize)
					{
						dropValueItem.setAmount(maxValueItemStackSize);
						ItemHelper.applyLore(dropValueItem, economy.getSignature().createSignatureLore(maxValueItemStackSize));
						dropValueAmount -= maxValueItemStackSize;
					}
					else
					{
						dropValueItem.setAmount(dropValueAmount);
						ItemHelper.applyLore(dropValueItem, economy.getSignature().createSignatureLore(dropValueAmount));
						dropValueAmount = 0;
					}
					playerDrops.add(dropValueItem);
				}
			}	
			playerDrops.remove(walletItem);
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		final Inventory playerInventory = event.getPlayer().getInventory();
		final ItemStack walletItem = economy.getConfig().getWalletItem();
		
		if (!playerInventory.contains(walletItem))
		{
			final UUID uuid = event.getPlayer().getUniqueId();
			
			if (lastWalletSlot.containsKey(uuid))
			{
				playerInventory.setItem(lastWalletSlot.get(uuid), walletItem);
				lastWalletSlot.remove(uuid);
			}
			else
			{
				playerInventory.setItem(economy.getConfig().getWalletDefaultSlot(), walletItem);
			}						
		}		
	}
	
	// E N T I T Y - I N T E R A C T
	
	@EventHandler 
	public void onInteractAtEntity(PlayerInteractAtEntityEvent event)
	{		
		event.setCancelled(handleEntityInteract(event.getPlayer(), event.getRightClicked()));
	}
	
	@EventHandler
	public void onHitAtEntity(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() instanceof Player)
		{
			event.setCancelled(handleEntityInteract((Player) event.getDamager(), event.getEntity()));
		}
	}
	
	private boolean handleEntityInteract(@NotNull Player player, @NotNull Entity entity)
	{
		if (economy.getConfig().equalsWalletItem(player.getInventory().getItemInMainHand()) ||
		    economy.getConfig().equalsWalletItem(player.getInventory().getItemInOffHand()))
		{
			if (entity instanceof Player)
			{
				player.closeInventory();
				player.openInventory(new InteractionHolder(economy, player.getUniqueId(), entity.getUniqueId()).getInventory());
			}	
			return true;
		}
		
		if (entity instanceof Villager &&
			entity.getCustomName().equals(economy.getConfig().getBankEntityName()))
		{
			final UUID playerUUID = player.getUniqueId();
			
			player.openInventory(new BankHolder(economy, playerUUID, entity.getLocation()).getInventory());
			return true;
		}	
		return false;
	}
	
	// I T E M -  I N T E R A C T
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onDrop(PlayerDropItemEvent event)
	{
		if (economy.getConfig().equalsWalletItem(event.getItemDrop().getItemStack()))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onInteract(PlayerInteractEvent event)
	{
		final Action action = event.getAction();
		final Player player = event.getPlayer();
		final ItemStack playerMainItem = player.getInventory().getItemInMainHand();
		
		if ((action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) &&
			economy.getConfig().equalsWalletItem(playerMainItem))
		{
			player.openInventory(new WalletHolder(economy, player.getUniqueId()).getInventory());
			player.playSound(player.getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 2);	
			event.setCancelled(true);
		}
		else if (action == Action.RIGHT_CLICK_BLOCK && 
				 economy.getConfig().equalsBankSpawnItem(playerMainItem))
		{
			if (economy.getBank().handleBankAdd(event.getClickedBlock().getLocation(), player.getUniqueId(), economy.getConfig()) == Bank.CODE_SUCCESS)
			{
				player.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, economy.getConfig().getBankName(), " platziert.\n",
                                                      MessageHelper.PREFIX_HUFF, "Denke an die Öffnungszeiten von ", MessageHelper.getTimeLabel(economy.getConfig().getBankOpen()),
                                                                                 " bis ", MessageHelper.getTimeLabel(economy.getConfig().getBankClose()), "."));
							
				player.getInventory().getItemInMainHand().setAmount(playerMainItem.getAmount() -1);
			}
			else
			{
				player.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du bist zu nah an einem anderen ", economy.getConfig().getBankName(), "."));
			}
			event.setCancelled(true);
		}
	}
}
