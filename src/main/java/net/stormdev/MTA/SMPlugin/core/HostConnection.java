package net.stormdev.MTA.SMPlugin.core;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.List;

import net.stormdev.MTA.SMPlugin.connections.Message;
import net.stormdev.MTA.SMPlugin.messaging.MessageRecipient;

import org.bukkit.Bukkit;

public class HostConnection implements Runnable {
	
	private String ip;
	private int port;
	private SocketAddress address;
	private String connectionId;
	private Socket socket;
	private boolean connected = false;
	
	private PrintWriter out = null;
	private BufferedReader in = null;
	
	private boolean connect = true;
	
	public HostConnection(String ip, int port, String connectionId){
		this.ip = ip;
		this.port = port;
		this.connectionId = connectionId;
		this.address = new InetSocketAddress(ip, port);
		connected = false;
	}
	
	public void setShouldConnect(boolean con) {
		connect = con;
	}
	
	public boolean connect() throws IOException {
		if(connected){
			return true;
		}
		socket = new Socket();
		socket.setKeepAlive(true);
		socket.connect(address, 3000); //Give the server 3s to check if available
		out = new PrintWriter(new DataOutputStream(socket.getOutputStream()), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		startListening();
		startKeepAlive();
		connected = true;
		return true;
	}
	
	private void startKeepAlive(){
		Bukkit.getScheduler().runTaskAsynchronously(Core.plugin, new Runnable(){

			@Override
			public void run() {
				while(connected){
					rawMsg("alive");
					try {
						Thread.sleep(3000); //3s
					} catch (InterruptedException e) {
						//Oh well
					}
 				}
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
		}
		try {
			if(in != null){
				in.close();
			}
			if(out != null){
				out.close();
			}
		} catch (IOException e) {
			// Whatever
		}
		connected = false;
	}
	
	public void connectIt(){
		//TODO Connect and reconnect when needed!
	}

	@Override
	public void run() {
		while(connected){
			try {
				String line;
				while((line = in.readLine()) != null){
					if(line.equalsIgnoreCase("identify")){
						Message toSend = new Message(MessageRecipient.HOST.getConnectionID(), connectionId, "indentify", "server"); //Tell them we're a server
						msg(toSend);
						continue;
					}
					else if(line.equalsIgnoreCase("close")){
						close(false);
						continue;
					}
					else if(line.equalsIgnoreCase("ping")){
						rawMsg("pong");
						continue;
					}
					//TODO Message handling
				}
			} catch (SocketException e){
				//Connection cut
				close(false);
			} catch (Exception e) {
				// AN ERROR???
				continue;
			}
		}
		if(connect){
			//Reconnect
			try {
				connect();
			} catch (IOException e) {
				//TODO Recursive reconnect until it connects again
			}
			return;
		}
		return;
	}
}
