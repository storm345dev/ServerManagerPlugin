package net.stormdev.MTA.SMPlugin.core;

import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;

public class Configurator {
	public static FileConfiguration configure(FileConfiguration config){
		// Setup the general plugin settings
		if(!config.contains("general.logger.colour")){
			config.set("general.logger.colour", true);
		}
		// Setup the core plugin settings
		if(!config.contains("core.host.ip")){
			config.set("core.host.ip", "localhost");
		}
		if(!config.contains("core.host.port")){
			config.set("core.host.port", 50000);
		}
		if(!config.contains("core.host.securityKey")){
			config.set("core.host.securityKey", "pass");
		}
		if(!config.contains("core.host.serverName")){
			config.set("core.host.serverName", UUID.randomUUID().toString());
		}
		if(!config.contains("core.host.serverDescription")){
			config.set("core.host.serverDescription", "A minecraft server");
		}
		// Setup the SM settings
		if(!config.contains("server.settings.dynamicOpenAndCloseWithLag")){
			config.set("server.settings.dynamicOpenAndCloseWithLag", true);
		}
		// Setup the colour scheme
		if (!config.contains("colorScheme.success")) {
			config.set("colorScheme.success", "&c");
		}
		if (!config.contains("colorScheme.error")) {
			config.set("colorScheme.error", "&7");
		}
		if (!config.contains("colorScheme.info")) {
			config.set("colorScheme.info", "&f");
		}
		if (!config.contains("colorScheme.title")) {
			config.set("colorScheme.title", "&6");
		}
		if (!config.contains("colorScheme.tp")) {
			config.set("colorScheme.tp", "&1");
		}
		return config;
	}
}
