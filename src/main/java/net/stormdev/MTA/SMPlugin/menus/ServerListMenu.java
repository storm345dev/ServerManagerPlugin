package net.stormdev.MTA.SMPlugin.menus;

import java.util.ArrayList;
import java.util.List;

import net.stormdev.MTA.SMPlugin.core.Core;
import net.stormdev.MTA.SMPlugin.events.Listener;
import net.stormdev.MTA.SMPlugin.gui.IconMenu;
import net.stormdev.MTA.SMPlugin.gui.IconMenu.OptionClickEvent;
import net.stormdev.MTA.SMPlugin.gui.IconMenu.OptionClickEventHandler;
import net.stormdev.MTA.SMPlugin.servers.Server;
import net.stormdev.MTA.SMPlugin.servers.ServerListUpdateEvent;
import net.stormdev.MTA.SMPlugin.utils.MetaValue;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;

public class ServerListMenu implements OptionClickEventHandler,Listener<ServerListUpdateEvent>,org.bukkit.event.Listener { //One menu per player accessing it
	private IconMenu menu;
	private String playerName;
	private String title;
	
	public ServerListMenu(Player player){
		playerName = player.getName();
		Core.plugin.eventManager.registerListener(ServerListUpdateEvent.class, this);
		
		List<Server> servers = Core.plugin.servers.getConnectedServers();
		double quant = ((double)servers.size()) / 9.0;
		int icons = ((int) Math.ceil(quant))  *  9; //A multiple of 9 that fits all servers
		title = Core.colors.getTitle()+"Server List";
		
		menu = new IconMenu(title, icons, this, Core.plugin);
		
		//Load menu icons
		open(player);
		refresh();
	}
	
	private Inventory getInventory(){
		Player player = Bukkit.getPlayer(playerName);
		if(player == null || !player.isOnline()){
			return null;
		}
		Inventory inv = player.getOpenInventory().getTopInventory();
		if(inv == null || !inv.getTitle().equals(title)){
			return null;
		}
		return inv;
	}
	
	private void destroy(){
		Core.plugin.eventManager.unregisterListener(this);
		menu.destroy();
	}
	
	private void open(Player player){
		menu.open(player);
	}
	
	public void refresh(){
		Core.plugin.servers.updateServers(); //Get them to refresh for it to update on the menu
	}

	@Override
	public void onOptionClick(OptionClickEvent event) {
		event.setWillClose(true);
		event.setWillDestroy(true);
		return;
	}

	@Override
	public void onCall(ServerListUpdateEvent event) {
		Inventory inv = getInventory();
		if(inv == null){
			return;
		}
		
		final Player player = Bukkit.getPlayer(playerName);
		if(player == null){
			return;
		}
		InventoryView view = player.getOpenInventory();
		
		final List<Server> list = Core.plugin.servers.getConnectedServers();
		double quant = ((double)list.size()) / 9.0;
		int icons = ((int) Math.ceil(quant))  *  9; //A multiple of 9 that fits all servers
		if(inv.getSize() < icons){ //Need a bigger inventory :(
			//Make the inventory bigger
			player.setMetadata("ignoreInvClose", new MetaValue(true, Core.plugin)); //Effective cancel destroying this because of inv close
			view.close();
			menu = new IconMenu(title, icons, this, Core.plugin);
			
			Bukkit.getScheduler().runTaskLaterAsynchronously(Core.plugin, new Runnable(){

				@Override
				public void run() {
					player.removeMetadata("ignoreInvClose", Core.plugin); //Make SURE that if they exit it, it'll fire correctly; else it's a memory leak
					menu.open(player);
					
					Inventory inv = getInventory();
					if(inv == null){
						return;
					}
					
					renderServers(inv, list);
					
					return;
				}}, 2l);
			return;
		}
		
		renderServers(inv, list);
		
	}
	
	private void renderServers(Inventory inv, List<Server> list){
		int slot = 0;
		for(Server server:list){
			ItemStack item = getItem(server);
			inv.setItem(slot, item);
			menu.setOption(slot, item, item.getItemMeta().getDisplayName(), item.getItemMeta().getLore());
			slot++;
		}
	}
	
	private ItemStack getItem(Server server){
		ItemStack item = getShow(server.getResourceScore(), server.isOpen());
		ItemMeta im = item.getItemMeta();
		
		im.setDisplayName(Core.colors.getTitle()+server.getTitle());
		List<String> info = new ArrayList<String>();
		
		String openStatus;
		if(server.isOpen()){
			openStatus = ChatColor.GREEN+"Yes";
		}
		else {
			openStatus = ChatColor.RED+"No";
		}
		
		info.add(Core.colors.getInfo()+"("+server.getDescription()+")");
		info.add(Core.colors.getTitle()+"Players: "+Core.colors.getInfo()+"["+server.getPlayerCount()+"/"+server.getMaxPlayers()+"]");
		info.add(Core.colors.getTitle()+"Resource Score: "+Core.colors.getInfo()+server.getResourceScore());
		info.add(Core.colors.getTitle()+"TPS: "+Core.colors.getInfo()+server.getTPS());
		info.add(Core.colors.getTitle()+"Open: "+openStatus);
		im.setLore(info);
		
		item.setItemMeta(im);
		return item;
	}
	
	private ItemStack getShow(int resourceScore, boolean open){
		DyeColor color = DyeColor.YELLOW;
		
		if(resourceScore > 85){
			color = DyeColor.GREEN;
		}
		else if(resourceScore > 75){
			color = DyeColor.LIME;
		}
		else if(resourceScore > 60){
			color = DyeColor.YELLOW;
		}
		else if(resourceScore > 50){
			color = DyeColor.ORANGE;
		}
		else if(resourceScore > 40){
			color = DyeColor.MAGENTA;
		}
		else {
			color = DyeColor.RED;
		}
		
		if(open){	//Open
			return new Wool(color).toItemStack();
		}
		else{ //Not open
			//Get correct colours
			short data = 0; //White
			switch(color){
			case BLACK: data = 15;
				break;
			case BLUE: data = 11;
				break;
			case BROWN: data = 12;
				break;
			case CYAN: data = 9;
				break;
			case GRAY: data = 7;
				break;
			case GREEN: data = 13;
				break;
			case LIGHT_BLUE: data = 3;
				break;
			case LIME: data = 5;
				break;
			case MAGENTA: data = 2;
				break;
			case ORANGE: data = 1;
				break;
			case PINK: data = 6;
				break;
			case PURPLE: data = 10;
				break;
			case RED: data = 14;
				break;
			case SILVER: data = 8;
				break;
			case WHITE: data = 0;
				break;
			case YELLOW: data = 4;
				break;
			default:
				break; //Get the right data
			}
			
			return new ItemStack(Material.STAINED_CLAY, 1, data); //Until there is a better say to get Stained Clay colours...
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	void invClose(InventoryCloseEvent event){
		if(event.getPlayer().hasMetadata("ignoreInvClose")){
			event.getPlayer().removeMetadata("ignoreInvClose", Core.plugin);
			return;
		}
		if(event.getPlayer().getName().equals(playerName)){
			destroy();
			return;
		}
	}
}
