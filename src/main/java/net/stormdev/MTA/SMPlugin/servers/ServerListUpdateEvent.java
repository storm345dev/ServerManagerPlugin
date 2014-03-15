package net.stormdev.MTA.SMPlugin.servers;

import net.stormdev.MTA.SMPlugin.events.Event;

public class ServerListUpdateEvent implements Event {
	private volatile Servers list;
	
	public ServerListUpdateEvent(Servers serverList){
		this.list = serverList;
	}
	
	public Servers getServers(){
		return list;
	}
}
