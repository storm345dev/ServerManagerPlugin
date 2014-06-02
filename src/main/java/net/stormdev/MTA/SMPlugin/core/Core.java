package net.stormdev.MTA.SMPlugin.core;

import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.UUID;

import net.stormdev.MTA.SMPlugin.commands.SMRestartCommandExecutor;
import net.stormdev.MTA.SMPlugin.commands.ServerListCommandExecutor;
import net.stormdev.MTA.SMPlugin.commands.ServerManagerCommandExecutor;
import net.stormdev.MTA.SMPlugin.connections.Message;
import net.stormdev.MTA.SMPlugin.events.ConnectEventListener;
import net.stormdev.MTA.SMPlugin.events.EventManager;
import net.stormdev.MTA.SMPlugin.events.ServerEventListener;
import net.stormdev.MTA.SMPlugin.messaging.Encrypter;
import net.stormdev.MTA.SMPlugin.messaging.MessageListener;
import net.stormdev.MTA.SMPlugin.servers.Servers;
import net.stormdev.MTA.SMPlugin.utils.Colors;
import net.stormdev.MTA.SMPlugin.utils.CountDown;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.stormdev.SMPlugin.api.API;

public class Core extends JavaPlugin {
	
	public static FileConfiguration config;
	public static Colors colors;
	public static Core plugin;
	public static CustomLogger logger;
	public static String verString;
	public static Random random = new Random();
	public static UUID instanceId;
	public static API API;
	
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
	private boolean restartOnCrash = true;
	private String restartScript;
	private ServerOutput outputReader;
	private BukkitTask serverUpdate = null;
	
	private BukkitTask idle;
	
	public String testString;
	
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
		instanceId = UUID.randomUUID();
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
		if(!encrypter.test()){
			Core.logger.info("BAD security key!");
		}
		eventManager = new EventManager();
		
		new MessageListener(); //Listen to message events in the listener
		
		new ServerEventListener();
		new ConnectEventListener();
		
		//Load the connection stuff
		connection = new HostConnection(ip, port, serverName);
		connection.connectIt();
		
		setupCMDExecutors();
		
		logger.info("Starting up...");
		idle();
		
		new AntiCrash(instanceId, restartScript);
		if(restartOnCrash){
			Bukkit.getScheduler().runTask(Core.plugin, new Runnable(){ //Run it post-server startup

				@Override
				public void run() {
					logger.info("Starting automatic crash recovery...");
					AntiCrash.getInstance().start();
					return;
				}});
		}
		
		if(config.getBoolean("server.settings.shareConsole")){
			Bukkit.getScheduler().runTaskAsynchronously(Core.plugin, new Runnable(){

				@Override
				public void run() {
					logger.info("Starting console sharing...");
					new ServerOutput();
					return;
				}});
		}
		logger.info("Started!");
		
		API = new API(this);
		
		if(Core.logger.isDebug()){
		Bukkit.getScheduler().runTaskLaterAsynchronously(Core.plugin, new Runnable(){

			@Override
			public void run() {
				StringBuilder longString = new StringBuilder();
				while(longString.length() < 30000){
					longString.append(UUID.randomUUID().toString());
				}
				
				testString = longString.toString();
				
				connection.sendMsg(new Message(connection.getConnectionID(), connection.getConnectionID(), "testString", longString.toString()));
				return;
			}}, 100l);
		}
		
		serverUpdate = Bukkit.getScheduler().runTaskTimer(this, new Runnable(){

			@Override
			public void run() {
				servers.updateServers();
				return;
			}}, 15*20l, 10*20l);
		
		logger.info("ServerManagerPlugin v"+verString+" has been enabled!");
	}
	
	@Override
	public void onDisable(){
		serverMonitor.cancel(); //Terminate it
		serverUpdate.cancel();
		idle.cancel();
		Bukkit.getScheduler().cancelTasks(this);
		if(outputReader != null){
			outputReader.stop();
		}
		connection.close(true); //Fully shutdown connection
		logger.info("ServerManagerPlugin v"+verString+" has been disabled!");
	}
	
	private void idle(){
		final CountDown loop = new CountDown(6); //Each 'tick' is 10s apart
		idle = Bukkit.getScheduler().runTaskTimerAsynchronously(Core.plugin, new Runnable(){

			@Override
			public void run() {
				int i = loop.get();
				
				if(i == 6 || i == 3){ //Every 30s
					servers.updateServers();
				}
				
				loop.increment();
				return;
			}}, 200l, 200l); //Every 10s
	}
	
	private void setupCMDExecutors(){
		getCommand("servermanager").setExecutor(new ServerManagerCommandExecutor());
		getCommand("serverlist").setExecutor(new ServerListCommandExecutor());
		getCommand("smrestart").setExecutor(new SMRestartCommandExecutor());
	}
	
	public void loadConfigSettings(){
		colors = new Colors(config.getString("colorScheme.success"),
				config.getString("colorScheme.error"),
				config.getString("colorScheme.info"),
				config.getString("colorScheme.title"),
				config.getString("colorScheme.title"));
		
		port = config.getInt("core.host.port");
		try {
			securityKey = new String(config.getString("core.host.securityKey").getBytes(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// Unsupported
			e.printStackTrace();
		}
		securityKey = securityKey.trim();
		Core.logger.info("Using securityKey: '"+securityKey+"'");
		ip = config.getString("core.host.ip");
		serverName = config.getString("core.host.serverName");
		serverDescription = config.getString("core.host.serverDescription");
		dynamicOpenClose = config.getBoolean("server.settings.dynamicOpenAndCloseWithLag");
		restartOnCrash = config.getBoolean("server.settings.restartOnCrash");
		restartScript = config.getString("server.settings.restartScript");
	}
}
