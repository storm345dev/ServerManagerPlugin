package net.stormdev.MTA.SMPlugin.servers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Servers {
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
		servers.clear();
		for(Server s:srvs){
			servers.put(s.getConnectionId(), s);
		}
	}

}
