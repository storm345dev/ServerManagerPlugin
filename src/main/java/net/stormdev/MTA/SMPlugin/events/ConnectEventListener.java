package net.stormdev.MTA.SMPlugin.events;

import net.stormdev.MTA.SMPlugin.core.Core;
import net.stormdev.MTA.SMPlugin.servers.ServerConnectToHostEvent;

public class ConnectEventListener implements Listener<ServerConnectToHostEvent> {
	
	public ConnectEventListener(){
		Core.plugin.eventManager.registerListener(ServerConnectToHostEvent.class, this);
	}

	@Override
	public void onCall(ServerConnectToHostEvent event) {
		// On connect do:...
		Core.plugin.servers.updateServers();
		return;
	}

}
