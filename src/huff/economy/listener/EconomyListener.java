package huff.economy.listener;

import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import huff.economy.EconomyInterface;
import huff.economy.TransactionInventory;
import huff.economy.TransactionKind;
import huff.economy.storage.EconomyBank;
import huff.lib.helper.InventoryHelper;
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
		if (entity instanceof Player && 
		    (economy.getConfig().equalsWalletItem(player.getInventory().getItemInMainHand()) ||
		     economy.getConfig().equalsWalletItem(player.getInventory().getItemInOffHand())))
		{
			player.closeInventory();
			player.openInventory(new TransactionInventory(economy.getConfig(), TransactionKind.WALLET_OTHER, entity.getUniqueId()).getInventory());		
			return true;
		}
		
		if (entity instanceof Villager &&
			entity.getCustomName().equals(economy.getConfig().getBankEntityName()))
		{
			final UUID playerUUID = player.getUniqueId();
			
			player.openInventory(getBankInventory(playerUUID, economy.getBank().isOwner(playerUUID, entity.getLocation())));
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
			player.openInventory(getWalletInventory(player.getUniqueId()));
			player.playSound(player.getLocation(), Sound.BLOCK_WOOL_BREAK, 1, 2);	
			event.setCancelled(true);
		}
		else if (action == Action.RIGHT_CLICK_BLOCK && 
				 economy.getConfig().equalsBankSpawnItem(playerMainItem))
		{
			if (economy.getBank().handleBankAdd(player, economy.getConfig()) == EconomyBank.CODE_SUCCESS)
			{
				player.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, economy.getConfig().getBankName(), " platziert.\n",
                                                      MessageHelper.PREFIX_HUFF, "Denke an die Öffnungszeiten von ", MessageHelper.getTimeLabel(economy.getConfig().getBankOpen()),
                                                                                 "bis ", MessageHelper.getTimeLabel(economy.getConfig().getBankClose()), "."));
							
				player.getInventory().getItemInMainHand().setAmount(playerMainItem.getAmount() -1); //TODO Test!
				event.setCancelled(true);
			}
			else
			{
				player.sendMessage(StringHelper.build(MessageHelper.PREFIX_HUFF, "Du bist zu nah an einem anderen ", economy.getConfig().getBankName(), "."));
			}
		}
	}
	
	// I N V E N T O R I E S
	
	private @NotNull Inventory getWalletInventory(@NotNull UUID uuid)
	{
		final Inventory walletInventory = Bukkit.createInventory(null, InventoryHelper.INV_SIZE_3, economy.getConfig().getWalletInventoryName());
		
		InventoryHelper.setBorder(walletInventory, InventoryHelper.getBorderItem());
		InventoryHelper.setFill(walletInventory, InventoryHelper.getFillItem(), false);
		
		InventoryHelper.setItem(walletInventory, 2, 2, ItemHelper.getItemWithMeta(economy.getConfig().getValueMaterial(), 
				                                                                  MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(economy.getStorage().getWallet(uuid)), 
						                                                          false , false)));
		InventoryHelper.setItem(walletInventory, 2, 8, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
																				  economy.getConfig().getTransactionInventoryName(TransactionKind.WALLET_OUT)));
		InventoryHelper.setItem(walletInventory, 3, 5, InventoryHelper.getCloseItem());
		
		return walletInventory;
	}
	
	private @NotNull Inventory getBankInventory(@NotNull UUID uuid, boolean withRemove)
	{
		final Inventory bankInventory = Bukkit.createInventory(null, InventoryHelper.INV_SIZE_4 , economy.getConfig().getBankInventoryName());

		InventoryHelper.setFill(bankInventory, InventoryHelper.getBorderItem(), true);
		
		InventoryHelper.setItem(bankInventory, 2, 2, ItemHelper.getItemWithMeta(economy.getConfig().getValueMaterial(), 
																                MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(economy.getStorage().getBalance(uuid)), 
																                false , false)));
		InventoryHelper.setItem(bankInventory, 2, 3, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
																				economy.getConfig().getTransactionInventoryName(TransactionKind.BANK_IN)));
		InventoryHelper.setItem(bankInventory, 2, 5, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
																				economy.getConfig().getTransactionInventoryName(TransactionKind.BANK_OTHER)));
		InventoryHelper.setItem(bankInventory, 2, 7, ItemHelper.getItemWithMeta(Material.LIME_STAINED_GLASS_PANE, 
																				economy.getConfig().getTransactionInventoryName(TransactionKind.BANK_OUT)));			
		InventoryHelper.setItem(bankInventory, 2, 8, ItemHelper.getItemWithMeta(economy.getConfig().getValueMaterial(), 
																                MessageHelper.getHighlighted(economy.getConfig().getValueFormatted(economy.getStorage().getWallet(uuid)), 
																                false , false)));
		InventoryHelper.setItem(bankInventory, 4, 5, InventoryHelper.getCloseItem());
					
		if (withRemove)
		{
			InventoryHelper.setItem(bankInventory, 4, 9, ItemHelper.getItemWithMeta(Material.RED_STAINED_GLASS_PANE, economy.getConfig().getBankRemoveName()));
		}
		return bankInventory;
	}
}
