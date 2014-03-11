package net.stormdev.MTA.SMPlugin.core;

import org.bukkit.configuration.file.FileConfiguration;

public class Configurator {
	public static FileConfiguration configure(FileConfiguration config){
		// Setup the core plugin settings
		if(!config.contains("general.logger.colour")){
			config.set("general.logger.colour", true);
		}
		// Setup the colour scheme
		if (!config.contains("colorScheme.success")) {
			config.set("colorScheme.success", "&c");
		}
		if (!config.contains("colorScheme.error")) {
			config.set("colorScheme.error", "&7");
		}
		if (!config.contains("colorScheme.info")) {
			config.set("colorScheme.info", "&6");
		}
		if (!config.contains("colorScheme.title")) {
			config.set("colorScheme.title", "&4");
		}
		if (!config.contains("colorScheme.tp")) {
			config.set("colorScheme.tp", "&1");
		}
		return config;
	}
}
