package org.stormdev.SMPlugin.api;

import java.io.IOException;
import java.lang.reflect.Method;

import net.stormdev.MTA.SMPlugin.core.Core;

import org.stormdev.servermanager.api.APIProvider;
import org.stormdev.servermanager.api.APIProviderType;
import org.stormdev.servermanager.api.ServerManagerAPI;

public class API implements ServerManagerAPI {
	
	private Core plugin;
	
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
}
