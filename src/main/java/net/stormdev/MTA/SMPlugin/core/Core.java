package net.stormdev.MTA.SMPlugin.core;

import net.stormdev.MTA.SMPlugin.utils.Colors;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin {
	
	public static FileConfiguration config;
	public static Colors colors;
	public static Core plugin;
	public static CustomLogger logger;
	
	@Override
	public void onEnable(){
		plugin = this;
		
		config = getConfig();
		config = Configurator.configure(config);
		saveConfig();
		
		logger = new CustomLogger(Bukkit.getConsoleSender(), getLogger());
		
		// Load the colour scheme
		colors = new Colors(config.getString("colorScheme.success"),
				config.getString("colorScheme.error"),
				config.getString("colorScheme.info"),
				config.getString("colorScheme.title"),
				config.getString("colorScheme.title"));
		
		getServer().getLogger().info("ServerManagerPlugin has been enabled!");
	}
	
	@Override
	public void onDisable(){
		
		getServer().getLogger().info("ServerManagerPlugin has been disabled!");
	}
}
