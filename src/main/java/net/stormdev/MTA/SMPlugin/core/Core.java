package net.stormdev.MTA.SMPlugin.core;

import java.io.IOException;

import net.stormdev.MTA.SMPlugin.events.EventManager;
import net.stormdev.MTA.SMPlugin.messaging.Encrypter;
import net.stormdev.MTA.SMPlugin.utils.Colors;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin {
	
	public static FileConfiguration config;
	public static Colors colors;
	public static Core plugin;
	public static CustomLogger logger;
	public static String verString;
	
	public Encrypter encrypter;
	public EventManager eventManager;
	public HostConnection connection;
	
	private int port;
	private String ip;
	private String securityKey;
	private String serverName;
	private String serverDescription;
	
	@Override
	public void onEnable(){
		plugin = this;
		verString = getDescription().getVersion();
		
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
		
		port = config.getInt("core.host.port");
		securityKey = config.getString("core.host.securityKey");
		ip = config.getString("core.host.ip");
		serverName = config.getString("core.host.serverName");
		serverDescription = config.getString("core.host.serverDescription");
		
		encrypter = new Encrypter(securityKey);
		eventManager = new EventManager();
		
		//TODO Load the connection stuff
		connection = new HostConnection(ip, port, serverName);
		try {
			connection.connect();
		} catch (IOException e) {
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this); //Disable
			return;
		}
		
		logger.info("ServerManagerPlugin v"+verString+" has been enabled!");
	}
	
	@Override
	public void onDisable(){
		
		logger.info("ServerManagerPlugin v"+verString+" has been disabled!");
	}
}
