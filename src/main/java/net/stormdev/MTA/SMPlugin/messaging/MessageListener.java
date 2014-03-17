package net.stormdev.MTA.SMPlugin.messaging;

import java.io.IOException;
import java.util.regex.Pattern;

import net.stormdev.MTA.SMPlugin.connections.Message;
import net.stormdev.MTA.SMPlugin.core.Core;
import net.stormdev.MTA.SMPlugin.events.Listener;
import net.stormdev.MTA.SMPlugin.requests.UpdateRequest;
import net.stormdev.MTA.SMPlugin.servers.Server;

public class MessageListener implements Listener<MessageEvent> {

	private Core main;
	public MessageListener(){
		main = Core.plugin;
		Core.plugin.eventManager.registerListener(MessageEvent.class, this); //Registers the event to us
	}
	
	public void onCall(MessageEvent event) {
		Message message = event.getMessage();
		
		//TODO Handle message receiving
		Core.logger.debug("Received: "+message.getMsg());
		
		String title = message.getMsgTitle();
		
		if(message.getFrom().equals(MessageRecipient.HOST.getConnectionID())){
			if(title.equals("requestCommand")){
				String cmd = message.getMsg();
				if(cmd.equals("serverUpdate")){
					try {
						UpdateRequest.reply(); //Tell them how we are! :)
					} catch (IOException e) {
						// An error occured :(
						e.printStackTrace(); //Show it
					}
					return;
				}
			}
			else if(title.equalsIgnoreCase("servers")){
				//We've been sent a list of all servers, process it...
				String list = message.getMsg();
				String[] raws = list.split(Pattern.quote(","));
				Server[] srvs = new Server[raws.length];
				for(int i=0;i<raws.length;i++){
					Server s;
					try {
						s = Server.fromRawString(raws[i]);
					} catch (Exception e) {
						// An error???
						continue;
					}
					srvs[i] = s;
				}
				Core.plugin.servers.setServers(srvs);
			}
		}
		
		return;
	}
	
}
