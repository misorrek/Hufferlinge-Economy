package huff.economy.listener;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import huff.economy.EconomyConfig;
import huff.economy.EconomyTable;
import huff.lib.helper.InventoryHelper;
import huff.lib.helper.MessageHelper;
import huff.lib.helper.StringHelper;

public class WalletListener implements Listener
{
	public WalletListener(@NotNull EconomyConfig economyConfig, @NotNull EconomyTable economyTable)
	{
		Validate.notNull((Object) economyConfig, "The economy-config cannot be null.");
		Validate.notNull((Object) economyTable, "The economy-table cannot be null.");
		
		this.economyConfig = economyConfig;
		this.economyTable = economyTable;
	}
	private final EconomyConfig economyConfig;
	private final EconomyTable economyTable;
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		ItemStack placedItem = event.getItemInHand();
		
		if (equalsWalletItem(placedItem) || equalsValueItem(placedItem))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onDrop(PlayerDropItemEvent event)
	{
		if (equalsWalletItem(event.getItemDrop().getItemStack()))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onCraft(PrepareItemCraftEvent event)
	{
		if (event.getRecipe() != null)
		{	
			for (ItemStack contentItem : event.getInventory().getContents())
			{
				if (equalsWalletItem(contentItem) || equalsValueItem(contentItem)) 
				{
					event.getInventory().setResult(new ItemStack(Material.AIR));
				}
			}
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onAnvil(PrepareAnvilEvent event)
	{
		for (ItemStack contentItem : event.getInventory().getContents())
		{
			if (equalsWalletItem(contentItem) || equalsValueItem(contentItem)) 
			{
				event.setResult(new ItemStack(Material.AIR));
			}
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onSmithing(PrepareSmithingEvent event)
	{
		for (ItemStack contentItem : event.getInventory().getContents())
		{
			if (equalsWalletItem(contentItem) || equalsValueItem(contentItem)) 
			{
				event.setResult(new ItemStack(Material.AIR));
			}
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onEnchant(PrepareItemEnchantEvent event)
	{
		ItemStack enchantItem = event.getItem();
		
		if (equalsWalletItem(enchantItem) || equalsValueItem(enchantItem))
		{	
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		
		if (!event.getAction().equals(Action.LEFT_CLICK_AIR) || !event.getAction().equals(Action.LEFT_CLICK_BLOCK) ||
			!equalsWalletItem(player.getInventory().getItemInMainHand()))
		{
			return;
		}
		player.openInventory(WalletUtil.getWalletInventory(economyConfig, economyTable.getWallet(player.getUniqueId())));
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onInteractAtEntity(PlayerInteractAtEntityEvent event)
	{
		Player player = event.getPlayer();
		
		if (!(event.getRightClicked() instanceof Player) ||
			!equalsWalletItem(event.getPlayer().getInventory().getItemInMainHand()) ||
			!equalsWalletItem(event.getPlayer().getInventory().getItemInOffHand()))
		{
			return;
		}
		player.openInventory(WalletUtil.getPayInventory(economyConfig, ((Player) event.getRightClicked()).getName()));
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onHumanInverntoryClick(InventoryClickEvent event)
	{
		if (event.getCurrentItem() == null)
		{
			return;
		}
		
		if (equalsWalletItem(event.getCurrentItem()) && (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) || 
				                                         event.getAction().equals(InventoryAction.CLONE_STACK)))
		{
			event.setCancelled(true);
			return;
		}
		
		if (equalsValueItem(event.getCurrentItem()) && (event.getAction().equals(InventoryAction.DROP_ALL_CURSOR) || 
                										event.getAction().equals(InventoryAction.DROP_ONE_CURSOR)))
		{
			ItemStack slotItem = event.getInventory().getItem(event.getSlot());
			
			if (slotItem != null && equalsWalletItem(slotItem))
			{
				final HumanEntity human = event.getWhoClicked();
				final int feedbackCode = economyTable.updateWallet(human.getUniqueId(), event.getCurrentItem().getAmount(), false);
				
				human.sendMessage("Wallet Drop!");
				
				if (feedbackCode == EconomyTable.CODE_SUCCESS)
				{
					event.setCurrentItem(new ItemStack(Material.AIR));
				}
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onWalletInventoryClick(InventoryClickEvent event)
	{
		if (event.getView().getTitle().equals(WalletUtil.getWalletInventoryName(economyConfig.getWalletName())))
		{
			event.setCancelled(true);
			
			ItemStack currentItem = event.getCurrentItem();
			
			if (currentItem == null)
			{
				return;
			}
			String currentItemName = currentItem.getItemMeta().getDisplayName();
			
			if (currentItemName.equals(InventoryHelper.ITEM_CLOSE))
			{
				event.getView().close();
			}
			else if (currentItemName.equals(WalletUtil.getPayInventoryName(economyConfig.getValueName())))
			{
				for (HumanEntity human : event.getViewers())
				{
					human.openInventory(WalletUtil.getPayInventory(economyConfig, null));
				}
			}
		}
		else if (event.getView().getTitle().equals(WalletUtil.getPayInventoryName(economyConfig.getValueName())))
		{		
			event.setCancelled(true);
			
			ItemStack currentItem = event.getCurrentItem();
			
			if (currentItem == null)
			{
				return;
			}
			String currentItemName = currentItem.getItemMeta().getDisplayName();
			
			if (currentItemName.equals(InventoryHelper.ITEM_BACK))
			{
				for (HumanEntity human : event.getViewers())
				{
					human.openInventory(WalletUtil.getWalletInventory(economyConfig, economyTable.getWallet(human.getUniqueId())));
				}
			}
			else if (currentItemName.equals(WalletUtil.ITEM_PERFORMPAY))
			{
				Player targetPlayer = WalletUtil.getPayTargetPlayer(event.getInventory());
				
				if (targetPlayer != null)
				{
					for (HumanEntity human : event.getViewers())
					{
						if (targetPlayer.isOnline())
						{
							double valueAmount = WalletUtil.getPayValueAmount(event.getInventory());
							String formattedValueAmount = MessageHelper.getHighlighted(economyConfig.getValueFormatted(valueAmount));
							
							if (economyTable.updateWallet(human.getUniqueId(), valueAmount, true) != EconomyTable.CODE_SUCCESS)
							{
								human.sendMessage(MessageHelper.PREFIX_HUFF + "Das Herausnehmen des Geldes konnte nicht abgeschlossen werden.");
								event.getView().close();
								return;
							}
							if (economyTable.updateWallet(targetPlayer.getUniqueId(), valueAmount, false) != EconomyTable.CODE_SUCCESS)
							{
								human.sendMessage(MessageHelper.PREFIX_HUFF + "Das Herausnehmen des Geldes konnte nicht abgeschlossen werden.");
								event.getView().close();
								return;
							}
							human.sendMessage(MessageHelper.PREFIX_HUFF + "Du hast" + formattedValueAmount + "an" +
							                  MessageHelper.getHighlighted(targetPlayer.getName() + "übergeben."));
							targetPlayer.sendMessage(MessageHelper.PREFIX_HUFF + "Du hast" + formattedValueAmount + "von" +
							                         MessageHelper.getHighlighted(human.getName()) + "erhalten.");
						}
						else
						{
							human.sendMessage(MessageHelper.PREFIX_HUFF + "Das Herausnehmen des Geldes konnte nicht abgeschlossen werden.");
						}
						event.getView().close();
					}
				}
				else
				{
					for (HumanEntity human : event.getViewers())
					{
						double valueAmount = WalletUtil.getPayValueAmount(event.getInventory());
						String formattedValueAmount = MessageHelper.getHighlighted(economyConfig.getValueFormatted(valueAmount));
						ItemStack valueItem = WalletUtil.getValueItem(economyConfig);
						
						if (economyTable.updateWallet(human.getUniqueId(), valueAmount, true) != EconomyTable.CODE_SUCCESS)
						{
							human.sendMessage(MessageHelper.PREFIX_HUFF + "Das Herausnehmen des Geldes konnte nicht abgeschlossen werden.");
							event.getView().close();
							return;
						}
						while ((int) valueAmount > 0)
						{
							if ((int) valueAmount >= 64)
							{
								valueItem.setAmount(64);
							}
							else
							{
								valueItem.setAmount((int) valueAmount);
							}						
							human.getInventory().addItem(valueItem);
						}
						
						human.sendMessage(MessageHelper.PREFIX_HUFF + "Du hast" + formattedValueAmount + "aus deinem " + economyConfig.getWalletName() + " herausgenommen.");
						event.getView().close();
					}
				}
			}
			else if (StringHelper.isIn(false, currentItemName, WalletUtil.ITEM_PAY_ADD_1,
															   WalletUtil.ITEM_PAY_ADD_2,
															   WalletUtil.ITEM_PAY_ADD_3,
															   WalletUtil.ITEM_PAY_REMOVE_1,
															   WalletUtil.ITEM_PAY_REMOVE_2,
															   WalletUtil.ITEM_PAY_REMOVE_3))
			{
				String changeItem = event.getCurrentItem().getItemMeta().getDisplayName();
				try
				{
					double valueChange = Double.parseDouble(changeItem.substring(2, changeItem.length()-1).replace(" ", ""));
					double valueAmount = WalletUtil.getPayValueAmount(event.getInventory());
					
					WalletUtil.setPayValueAmount(event.getInventory(), economyConfig.getValueName(), valueAmount + valueChange); 
				}
				catch (NumberFormatException exception)
				{
					
				}
			}
		}
	}

	private boolean equalsWalletItem(ItemStack item)
	{
		return item.getType().equals(economyConfig.getWalletMaterial()) && item.getItemMeta().getDisplayName().equals(economyConfig.getWalletName());
	}
	
	private boolean equalsValueItem(ItemStack item)
	{
		return item.getType().equals(economyConfig.getValueMaterial()) && item.getItemMeta().getDisplayName().equals(economyConfig.getValueName());
	}
}
