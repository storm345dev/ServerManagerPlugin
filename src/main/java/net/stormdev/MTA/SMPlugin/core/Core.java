package net.stormdev.MTA.SMPlugin.core;

import java.util.Random;

import net.stormdev.MTA.SMPlugin.events.EventManager;
import net.stormdev.MTA.SMPlugin.events.ServerEventListener;
import net.stormdev.MTA.SMPlugin.messaging.Encrypter;
import net.stormdev.MTA.SMPlugin.messaging.MessageListener;
import net.stormdev.MTA.SMPlugin.servers.Servers;
import net.stormdev.MTA.SMPlugin.utils.Colors;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class Core extends JavaPlugin {
	
	public static FileConfiguration config;
	public static Colors colors;
	public static Core plugin;
	public static CustomLogger logger;
	public static String verString;
	public static Random random = new Random();
	
	public Encrypter encrypter;
	public EventManager eventManager;
	public HostConnection connection;
	public BukkitTask serverMonitor;
	public Servers servers;
	
	private int port; //Host service's port
	private String ip; //Host service's IP
	private String securityKey;
	private String serverName;
	private String serverDescription;
	
	private boolean serverOpen = true;
	private boolean dynamicOpenClose;
	
	private ServerEventListener serverListener;
	
	public boolean getServerShouldOpenCloseDynamically(){
		return dynamicOpenClose;
	}
	
	public boolean isServerOpen(){
		return serverOpen;
	}
	
	public void setServerOpen(boolean open){
		this.serverOpen = open;
	}
	
	public String getServerName(){
		return serverName;
	}
	
	public String getServerDescription(){
		return serverDescription;
	}
	
	@Override
	public void onEnable(){
		plugin = this;
		verString = getDescription().getVersion();
		
		config = getConfig();
		config = Configurator.configure(config);
		saveConfig();
		
		logger = new CustomLogger(Bukkit.getConsoleSender(), getLogger());
		
		// Load the config...
		loadConfigSettings();
		//Config loaded!
		
		servers = new Servers();
		
		serverMonitor = Bukkit.getScheduler().runTaskTimer(Core.plugin,
				new ServerMonitor(), 100L, 1L);
		encrypter = new Encrypter(securityKey);
		eventManager = new EventManager();
		
		new MessageListener(); //Listen to message events in the listener
		
		serverListener = new ServerEventListener();
		
		//Load the connection stuff
		connection = new HostConnection(ip, port, serverName);
		connection.connectIt();
		
		logger.info("ServerManagerPlugin v"+verString+" has been enabled!");
	}
	
	@Override
	public void onDisable(){
		serverMonitor.cancel(); //Terminate it
		
		connection.close(true); //Fully shutdown connection
		logger.info("ServerManagerPlugin v"+verString+" has been disabled!");
	}
	
	public void loadConfigSettings(){
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
		dynamicOpenClose = config.getBoolean("server.settings.dynamicOpenAndCloseWithLag");
	}
}
