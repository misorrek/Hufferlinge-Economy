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
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.economy.EconomyConfig;
import huff.economy.EconomyInterface;
import huff.economy.EconomyMessage;
import huff.economy.menuholder.BankHolder;
import huff.economy.menuholder.InteractionHolder;
import huff.economy.menuholder.TradeHolder;
import huff.economy.menuholder.WalletHolder;
import huff.economy.storage.Bank;
import huff.economy.storage.Storage;
import huff.lib.helper.EntityHelper;
import huff.lib.helper.ItemHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.menuholder.MenuHolder;
import huff.lib.various.structures.StringPair;

/**
 * A listener class that handles general economy specific events.
 */
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
		
		if (EconomyConfig.equalsWalletItem(placedItem) || EconomyConfig.equalsValueItem(placedItem))
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
		if (block == null || block.getType() == EconomyConfig.BANK_MATERIAL.getValue())
		{			
			for (Location bankLocation : economy.getBank().getBankLocations())
			{
				if (bankLocation.distance(location) <= 1)
				{
					player.sendMessage(EconomyMessage.BANK_NOTALLOWED.getValue(new StringPair("bankname", EconomyConfig.BANK_NAME.getValue())));
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
			if (EconomyConfig.equalsWalletItem(playerDrop))
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
					final ItemStack dropValueItem = EconomyConfig.getValueItem();
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
		final ItemStack walletItem = EconomyConfig.getWalletItem();
		
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
				playerInventory.setItem(EconomyConfig.WALLET_DEFAULTSLOT.getValue(), walletItem);
			}						
		}		
	}
	
	@EventHandler
	public void onVillagerTrade(VillagerAcquireTradeEvent event)
	{
		if (EntityHelper.hasTag(event.getEntity(), economy.getPlugin(), EconomyInterface.ENTITYKEY_BANK))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onVillagerTrade(VillagerCareerChangeEvent event)
	{
		if (EntityHelper.hasTag(event.getEntity(), economy.getPlugin(), EconomyInterface.ENTITYKEY_BANK))
		{
			event.setCancelled(true);
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
		if (EconomyConfig.equalsWalletItem(player.getInventory().getItemInMainHand()) ||
		    EconomyConfig.equalsWalletItem(player.getInventory().getItemInOffHand()))
		{
			if (entity instanceof Player)
			{
				 new InteractionHolder(economy, player.getUniqueId(), entity.getUniqueId()).open(player);
			}	
			return true;
		}
		
		if (entity instanceof Villager && EntityHelper.hasTag(entity, economy.getPlugin(), EconomyInterface.ENTITYKEY_BANK))
		{
			final UUID playerUUID = player.getUniqueId();
			
			new BankHolder(economy, playerUUID, entity.getLocation()).open(player);
			return true;
		}	
		return false;
	}
	
	// I T E M -  I N T E R A C T
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onDrop(PlayerDropItemEvent event)
	{
		if (EconomyConfig.equalsWalletItem(event.getItemDrop().getItemStack()))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPickup(EntityPickupItemEvent event)
	{
		if (event.getEntity() instanceof HumanEntity && 
			((HumanEntity) event.getEntity()).getOpenInventory().getTopInventory().getHolder() instanceof TradeHolder)
		{
			((TradeHolder) ((HumanEntity) event.getEntity()).getOpenInventory().getTopInventory().getHolder()).handlePickup();
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onInteract(PlayerInteractEvent event)
	{
		final Action action = event.getAction();
		final Player player = event.getPlayer();
		final ItemStack playerMainItem = player.getInventory().getItemInMainHand();
		
		if ((action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) &&
			EconomyConfig.equalsWalletItem(playerMainItem) && !(player.getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder))
		{
			new WalletHolder(economy, player.getUniqueId()).open(player);
			player.playSound(player.getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 2);	
			event.setCancelled(true);
		}
		else if (action == Action.RIGHT_CLICK_BLOCK && 
				 EconomyConfig.equalsBankSpawnItem(playerMainItem))
		{
			final Location blockLocation = event.getClickedBlock().getLocation();
			final float playerYaw = player.getLocation().getYaw();
			final Location bankLocation = new Location(blockLocation.getWorld(), blockLocation.getBlockX() + 0.5, blockLocation.getBlockY() + 1.0, blockLocation.getBlockZ() + 0.5,
					                                   playerYaw <= 0 ? playerYaw + 180 : playerYaw - 180, 0);			
			
			if (economy.getBank().addBank(bankLocation, player.getUniqueId()) == Bank.CODE_SUCCESS)
			{
				economy.trySpawnBankEntity(bankLocation);
				player.getInventory().getItemInMainHand().setAmount(playerMainItem.getAmount() -1);
				player.sendMessage(EconomyMessage.BANK_PLACE.getValue() + "\n"
                                   + EconomyMessage.BANK_OPENINGHOURS.getValue(new StringPair("open", MessageHelper.getTimeLabel(EconomyConfig.BANK_OPEN.getValue())),
                                		                                         new StringPair("close", MessageHelper.getTimeLabel(EconomyConfig.BANK_CLOSE.getValue()))));
			}
			else
			{
				player.sendMessage(EconomyMessage.BANK_TOCLOSE.getValue(new StringPair("bankname", EconomyConfig.BANK_NAME.getValue())));
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
    public void onQuit(PlayerQuitEvent event)
	{
		final Player player = event.getPlayer();
		final InventoryHolder inventoryHolder = player.getOpenInventory().getTopInventory().getHolder();
		
		if (inventoryHolder instanceof TradeHolder)
		{
			((TradeHolder) inventoryHolder).handleAbort(player);
		}
	}
}
