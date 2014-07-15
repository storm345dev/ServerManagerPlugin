package net.stormdev.MTA.SMPlugin.core;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.stormdev.MTA.SMPlugin.connections.Message;
import net.stormdev.MTA.SMPlugin.connections.TransitMessage;
import net.stormdev.MTA.SMPlugin.messaging.MessageEvent;
import net.stormdev.MTA.SMPlugin.messaging.MessageRecipient;
import net.stormdev.MTA.SMPlugin.requests.UpdateRequest;
import net.stormdev.MTA.SMPlugin.servers.ServerConnectToHostEvent;

import org.bukkit.Bukkit;

public class HostConnection implements Runnable {
	
	private volatile SocketAddress address;
	private volatile String connectionId;
	private volatile Socket socket;
	private volatile boolean connected = false;
	
	private volatile PrintWriter out = null;
	private volatile BufferedReader in = null;
	private volatile List<TransitMessage> inbound = new ArrayList<TransitMessage>();
	
	private volatile boolean identified = false;
	private volatile long connectedTime = 0;
	private volatile long aliveTime = 0;
	
	private volatile boolean keepAliveRunning = false;
	
	private volatile boolean connect = true;
	
	private static HostConnection instance;
	
	public HostConnection(String ip, int port, String connectionId){
		instance = this;
		this.connectionId = connectionId;
		this.address = new InetSocketAddress(ip, port);
		connected = false;
	}
	
	public String getConnectionID(){
		return connectionId;
	}
	
	public boolean isIdentified(){
		return identified;
	}
	
	public void setShouldConnect(boolean con) {
		connect = con;
	}
	
	public boolean isConnected() throws IOException{
		return connected && socket != null && in != null;
	}
	
	public boolean connect() {
		if(connected){
			return true;
		}
		try {
			socket = new Socket();
			socket.setKeepAlive(true);
			socket.connect(address, 3000); //Give the server 3s to check if available
			out = new PrintWriter(new DataOutputStream(socket.getOutputStream()), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			startListening();
			startKeepAlive();
			connected = true;
		} catch (Exception e) { //Connection attempt failed
			Core.logger.debug("Connection attempt failed!");
			return false;
		}
		return true;
	}
	
	private void startKeepAlive(){
		if(keepAliveRunning){
			return;
		}
		keepAliveRunning = true;
		Bukkit.getScheduler().runTaskAsynchronously(Core.plugin, new Runnable(){

			@Override
			public void run() {
				int i=0;
				while(connected){
					if(i<1){ //Every 15s
						try {
							UpdateRequest.reply();
						} catch (IOException e) {
							// An error?!? OH DEAR!
							e.printStackTrace();
						} //Tell the host service how we are!
					}
					rawMsg("alive");
					try {
						Thread.sleep(1500); //1.5s
					} catch (InterruptedException e) {
						//Oh well
						keepAliveRunning = false;
						return;
					}
					if((!identified && connectedTime > 0 && (System.currentTimeMillis() - connectedTime) > 10000) //10s timeout
							|| (aliveTime > 0 && (System.currentTimeMillis() - aliveTime) > 10000)){ //10s timeout
						Core.logger.info("Connection timed out...");
						close(false);
					}
					i++;
					if(i>10){
						i = 0;
					}
 				}
				keepAliveRunning = false;
				return;
			}});
	}
	
	private void startListening(){
		Bukkit.getScheduler().runTaskAsynchronously(Core.plugin, this);
	}
	
	
	private void msg(Message msg){
		List<String> lines = msg.getRaw();
		for(String line:lines){
			rawMsg(line);
		}
	}
	
	private void rawMsg(String msg){
		if(!connected){
			return;
		}
		out.println(msg);
		out.flush();
	}
	
	public void close(boolean terminate){
		if(terminate){
			connect = false; //Should stop reconnecting also
			connected = false; //Should stop reading current connection
			return;
		}
		connected = false;
		try {
			if(in != null){
				in.close();
			}
			if(out != null){
				if(!socket.isOutputShutdown()){
					rawMsg("close");
				}
				out.close();
			}
		} catch (IOException e) {
			// Whatever
		}
	}
	
	public void connectIt(){
		//Connect and reconnect when needed!
		final HostConnection con = this;
		Bukkit.getScheduler().runTaskAsynchronously(Core.plugin, new Runnable(){

			@Override
			public void run() {
				while(connect
						&& instance.equals(con)){ //Check it's ourselves, and not reloaded self
					try {
						if(!isConnected()){
							Core.logger.debug("Attempting connection...");
							//Reconnect
							if(!connect()){ //Attempt to connect again
								//It failed to connect
								Thread.sleep(10000); //Wait 10s to try again
							}
						}
					} catch (Exception e) {
						// Ignore and move on
						e.printStackTrace();
					}
				}
				return;
			}});
	}

	@Override
	public void run() {
		connectedTime = System.currentTimeMillis();
		
		try {
			String line;
			while(((line = in.readLine()) != null) && connected && connect){ //Make sure it terminates correctly
				try {
					if(line.equalsIgnoreCase("alive")){
						aliveTime = System.currentTimeMillis();
						continue;
					}
					else if(line.equalsIgnoreCase("identify")){
						Message toSend = new Message(MessageRecipient.HOST.getConnectionID(), connectionId, "indentify", "server"); //Tell them we're a server
						msg(toSend);
						continue;
					}
					else if(!identified && line.equalsIgnoreCase("authenticated")){
						identified = true;
						Core.logger.info("Successfully connected to host service!");
						Bukkit.getScheduler().runTaskAsynchronously(Core.plugin, new Runnable(){

							@Override
							public void run() {
								Core.plugin.eventManager.callEvent(new ServerConnectToHostEvent());
								return;
							}});
						continue;
					}
					else if(line.equalsIgnoreCase("close")){
						close(false);
						return;
					}
					else if(line.equalsIgnoreCase("ping")){
						rawMsg("pong");
						continue;
					}
					else if(line.equalsIgnoreCase("unsupportedOperation")){
						Core.logger.info(Core.colors.getError()+"Unsupported operation attempted and rejected! Is ServerManager up-to-date on all services?");
						continue;
					}
					else if(line.equalsIgnoreCase("alreadyConnected")){
						Core.logger.info(Core.colors.getError()+"Server with same ID already connected! Duplicate servers, or has the server just restarted?");
						return;
					}
					else if(line.equalsIgnoreCase("badSecurityCode")){
						Core.logger.info(Core.colors.getError()+"Security code in config doesn't match that the Host Service was initialized with and therefore"
								+ " the connection attempt was blocked! Please change your security code to match!");
						return;
					}
					//Message handling
					try {
						Message received = processMsg(line);
						if(received != null){ //Message recieved!
							Core.plugin.eventManager.callEvent(new MessageEvent(received)); //Tell everybody it's been received
						}
					} catch (Exception e) {
						//Error in msg format
					}
				} catch (Exception e) {
					//General error, but KEEP LISTENING; just print the problem.
					e.printStackTrace();
				}
			}
		} catch (Exception e){ //Connection forcefully closed
			//Connection cut
			Core.logger.info("Lost connection!");
			close(false);
			return;
		}
		
		System.out.println("Terminating connection...");
		if(!socket.isOutputShutdown() && socket.isConnected()){
			close(false);
		}
		System.out.println("Connection terminated!");
		return;
	}
	
	private synchronized Message processMsg(String in){
		for(TransitMessage msg:inbound){
			if(msg.onRecieve(in)){
				if(msg.hasRecievedAll()){
					return msg.getMessage();
				}
				return null; //It matches and has been recieved
			}
		}
		//Didn't match one already being parsed, so create new
		TransitMessage msg = new TransitMessage(in);
		msg.onRecieve(in); //Make sure it knows
		
		if(msg.hasRecievedAll()){ //Message was only one line, we're done
			return msg.getMessage();
		}
		
		//Message is more than one line, add to the list of being parsed
		inbound.add(msg);
		return null;
	}
	
	public void sendMsg(Message msg){
		sendRawMsg(msg.getRaw());
	}
	
	public synchronized void sendRawMsg(Collection<? extends String> msg){
		//Just do it
		for(String m:msg){
			rawMsg(m);
		}
	}
	
	public synchronized void sendRawMsg(String msg){
		//Just do it
		rawMsg(msg);
	}
}
