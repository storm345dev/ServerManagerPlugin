package net.stormdev.MTA.SMPlugin.events;

import net.stormdev.MTA.SMPlugin.core.Core;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

public class ServerEventListener implements org.bukkit.event.Listener{
	
	private Core main;
	
	public ServerEventListener(){
		Bukkit.getPluginManager().registerEvents(this, Core.plugin);
		this.main = Core.plugin;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	void postPlayerJoin(PlayerJoinEvent event){
		if(!main.isServerOpen() && !event.getPlayer().hasPermission("servermanager.joinClosedServers")){
			event.getPlayer().kickPlayer("Server full!");
		}
	}
}
