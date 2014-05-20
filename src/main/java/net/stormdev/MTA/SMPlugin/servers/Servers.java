package net.stormdev.MTA.SMPlugin.servers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.stormdev.MTA.SMPlugin.connections.Message;
import net.stormdev.MTA.SMPlugin.core.Core;
import net.stormdev.MTA.SMPlugin.messaging.MessageRecipient;

import org.bukkit.Bukkit;

public class Servers implements org.stormdev.servermanager.api.messaging.Servers{
	private volatile Map<String, Server> servers = new HashMap<String, Server>();
	
	public synchronized int getConnectedCount(){
		return servers.size();
	}
	
	public synchronized void registerServer(String con){
		servers.put(con, Server.createBlank(con));
	}
	
	public synchronized void registerServer(Server server){
		servers.put(server.getConnectionId(), server);
	}
	
	public synchronized Server getServer(String conId){
		return servers.get(conId);
	}
	
	public synchronized boolean serverExists(String conId){
		return servers.containsKey(conId);
	}
	
	public synchronized void disconnectServer(String conId){
		servers.remove(conId);
	}
	
	public synchronized List<Server> getConnectedServers(){
		return new ArrayList<Server>(servers.values());
	}
	
	public synchronized void setServers(Server... srvs){
		try {
			servers.clear();
			for(Server s:srvs){
				servers.put(s.getConnectionId(), s);
			}
		} catch (Exception e) {
			//Errors happen when passcode is wrong
			return;
		}
		
		//Fire a server list update event
		final Servers list = this;
		Bukkit.getScheduler().runTaskAsynchronously(Core.plugin, new Runnable(){

			@Override
			public void run() {
				Core.plugin.eventManager.callEvent(new ServerListUpdateEvent(list));
				return;
			}});
	}
	
	public void updateServers(){
		Bukkit.getScheduler().runTaskAsynchronously(Core.plugin, new Runnable(){

			@Override
			public void run() { //Ask for the server list from the host, the listener will automatically update above for us
				Core.plugin.connection.sendMsg(new Message(MessageRecipient.HOST.getConnectionID(), Core.plugin.connection.getConnectionID(), "getServers", "getServers"));
				return;
			}});
	}

	@Override
	public Map<String, org.stormdev.servermanager.api.messaging.Server> getServers() {
		Map<String, org.stormdev.servermanager.api.messaging.Server> srvs = new HashMap<String, org.stormdev.servermanager.api.messaging.Server>();
		for(String id:new ArrayList<String>(servers.keySet())){
			srvs.put(id, servers.get(id));
		}
		return srvs;
	}

	@Override
	public boolean isServerConnected(String serverID) {
		return servers.containsKey(serverID);
	}

}
