package org.stormdev.SMPlugin.api;

import java.io.IOException;
import java.lang.reflect.Method;

import net.stormdev.MTA.SMPlugin.core.Core;

import org.stormdev.SMPlugin.api.messages.Messenger;
import org.stormdev.servermanager.api.APIProvider;
import org.stormdev.servermanager.api.APIProviderType;
import org.stormdev.servermanager.api.ServerManagerAPI;
import org.stormdev.servermanager.api.listeners.ListenerManager;
import org.stormdev.servermanager.api.messaging.Messager;
import org.stormdev.servermanager.api.messaging.Servers;

public class API implements ServerManagerAPI {
	
	private Core plugin;
	private ListenerManager listenerManager;
	private Messenger messenger;
	
	public API(Core core){
		this.plugin = core;
		
		try {
		
			Method method = APIProvider.class.getDeclaredMethod("setAPI", ServerManagerAPI.class);
			method.setAccessible(true);
			method.invoke(null, this); //Make ourselves the provided API
		
		} catch (SecurityException e) {
			Core.logger.info("OOPS: Security exception trying to initialize api! Any projects depending on it will be broken!");
			return;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			Core.logger.info("OOPS: Error when trying to initialize api! Maybe you're JAR is corrupt? Any projects depending on it will be broken!");
			return;
		} catch (IllegalArgumentException e) {
			Core.logger.info("OOPS: IllegalArgument exception trying to initialize api! Any projects depending on it will be broken!");
			return;
		} catch (IllegalAccessException e) {
			Core.logger.info("OOPS: Security exception trying to initialize api! Any projects depending on it will be broken!");
			return;
		} catch (Exception e) {
			Core.logger.info("OOPS: Exception trying to initialize api! Any projects depending on it will be broken!");
			return;
		}
		
		if(APIProvider.getAPI() == null || !APIProvider.getAPI().equals(this)){
			Core.logger.info("OOPS: API didn't quite setup right...");
			return;
		}
		
		this.listenerManager = new org.stormdev.SMPlugin.api.listeners.ListenerManager();
		this.messenger = new org.stormdev.SMPlugin.api.messages.Messenger();
		
		//TODO Actually do useful stuff
		
		Core.logger.info("API loaded!");
	}

	@Override
	public double getAPIVersion() {
		return APIMeta.VERSION;
	}

	@Override
	public APIProviderType getProvider() {
		return APIMeta.TYPE;
	}

	@Override
	public boolean isConnected() {
		try {
			return plugin.connection.isConnected();
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public ListenerManager getEventManager() {
		return listenerManager;
	}

	@Override
	public Servers getServers() {
		return Core.plugin.servers;
	}

	@Override
	public Messager getMessenger() {
		return messenger;
	}
}
