package net.stormdev.MTA.SMPlugin.gui;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.stormdev.MTA.SMPlugin.core.Core;
import net.stormdev.MTA.SMPlugin.utils.MetaValue;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class IconMenu implements Listener {

	private String name;
	private int size;
	private OptionClickEventHandler handler;
	private Plugin plugin;

	private String[] optionNames;
	private ItemStack[] optionIcons;
	private Boolean enabled = true;
	private String metaData;

	public IconMenu(String name, int size, OptionClickEventHandler handler,
			Plugin plugin) {
		this.name = name;
		this.size = size;
		this.handler = handler;
		this.plugin = plugin;
		this.optionNames = new String[size];
		this.optionIcons = new ItemStack[size];
		this.metaData = "menu." + UUID.randomUUID().toString();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public synchronized IconMenu setOption(int position, ItemStack icon, String name,
			String... info) {
		optionNames[position] = name;
		optionIcons[position] = setItemNameAndLore(icon, name, info);
		return this;
	}

	public synchronized IconMenu setOption(int position, ItemStack icon, String name,
			List<String> info) {
		optionNames[position] = name;
		optionIcons[position] = setItemNameAndLore(icon, name, info);
		return this;
	}
	
	public synchronized void setSize(int size){
		optionIcons = new ItemStack[size];
		this.size = size;
	}

	public synchronized void open(Player player) {
		Inventory inventory = Bukkit.createInventory(player, size, name);
		enabled = true;
		name = inventory.getTitle();
		for (int i = 0; i < optionIcons.length; i++) {
			if (optionIcons[i] != null) {
				inventory.setItem(i, optionIcons[i]);
			}
		}
		player.setMetadata(metaData, new MetaValue(null, Core.plugin));
		player.openInventory(inventory);
	}

	public void destroy() {
		HandlerList.unregisterAll(this);
		handler = null;
		plugin = null;
		optionNames = null;
		optionIcons = null;
		enabled = false;
		metaData = null;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	void invClose(InventoryCloseEvent event){
		if(event.getPlayer().hasMetadata(metaData)){
			event.getPlayer().removeMetadata(metaData, Core.plugin);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void onInventoryClick(InventoryClickEvent event) {
		
		if (event.getInventory().getTitle().equals(name) && enabled
				&& event.getWhoClicked().hasMetadata(metaData)) {
			event.setCancelled(true);
			int slot = event.getRawSlot();
			if (slot >= 0 && slot < size && optionNames[slot] != null) {
				Plugin plugin = this.plugin;
				OptionClickEvent e = new OptionClickEvent(event.getInventory(), this,
						(Player) event.getWhoClicked(), slot, optionNames[slot]);
				handler.onOptionClick(e);
				if (e.willClose()) {
					final Player p = (Player) event.getWhoClicked();
					Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
						@Override
						public void run() {
							p.closeInventory();
							p.removeMetadata(metaData, Core.plugin);
						}
					}, 1);
				}
				if (e.willDestroy()) {
					destroy();
				}
			}
		}
	}

	public interface OptionClickEventHandler {
		public void onOptionClick(OptionClickEvent event);
	}

	public class OptionClickEvent {
		private Player player;
		private int position;
		private String name;
		private boolean close;
		private boolean destroy;
		private Inventory inv;
		private IconMenu menu;

		public OptionClickEvent(Inventory inv, IconMenu menu, Player player, int position, String name) {
			this.player = player;
			this.position = position;
			this.name = name;
			this.close = true;
			this.destroy = false;
			this.inv = inv;
			this.menu = menu;
		}
		
		public Inventory getInventory(){
			return inv;
		}
		
		public IconMenu getMenu(){
			return menu;
		}

		public Player getPlayer() {
			return player;
		}

		public int getPosition() {
			return position;
		}

		public String getName() {
			return name;
		}

		public boolean willClose() {
			return close;
		}

		public boolean willDestroy() {
			return destroy;
		}

		public void setWillClose(boolean close) {
			this.close = close;
		}

		public void setWillDestroy(boolean destroy) {
			this.destroy = destroy;
		}
	}

	public ItemStack setItemNameAndLore(ItemStack item, String name,
			String... lore) {
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		im.setLore(Arrays.asList(lore));
		item.setItemMeta(im);
		return item;
	}

	public ItemStack setItemNameAndLore(ItemStack item, String name,
			List<String> lore) {
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		im.setLore(lore);
		item.setItemMeta(im);
		return item;
	}

}
