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
			Method method = APIProvider.class.getMethod("setAPI", ServerManagerAPI.class);
		} catch (SecurityException e) {
			Core.logger.info("OOPS: Security exception trying to initialize api! Any projects depending on it will be broken!");
		} catch (NoSuchMethodException e) {
			Core.logger.info("OOPS: Error when trying to initialize api! Maybe you're JAR is corrupt? Any projects depending on it will be broken!");
		}
	}

	@Override
	public double getAPIVersion() {
		return 1.0;
	}

	@Override
	public APIProviderType getProvider() {
		return APIProviderType.CORE;
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
