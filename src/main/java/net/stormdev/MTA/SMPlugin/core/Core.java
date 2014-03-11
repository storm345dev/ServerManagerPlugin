package net.stormdev.MTA.SMPlugin.core;

import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin {
	@Override
	public void onEnable(){
		
		getServer().getLogger().info("ServerManagerPlugin has been enabled!");
	}
	
	@Override
	public void onDisable(){
		
		getServer().getLogger().info("ServerManagerPlugin has been disabled!");
	}
}
