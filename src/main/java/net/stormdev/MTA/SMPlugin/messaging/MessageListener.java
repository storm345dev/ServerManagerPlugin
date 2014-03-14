package net.stormdev.MTA.SMPlugin.messaging;

import java.io.IOException;

import net.stormdev.MTA.SMPlugin.connections.Message;
import net.stormdev.MTA.SMPlugin.core.Core;
import net.stormdev.MTA.SMPlugin.events.Listener;
import net.stormdev.MTA.SMPlugin.requests.UpdateRequest;

public class MessageListener implements Listener<MessageEvent> {

	private Core main;
	public MessageListener(){
		main = Core.plugin;
		Core.plugin.eventManager.registerListener(new MessageEvent(null), this); //Registers the event to us
	}
	
	public void onCall(MessageEvent event) {
		Message message = event.getMessage();
		
		//TODO Handle message receiving
		Core.logger.info("Received: "+message.getMsg());
		
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
		}
		
		return;
	}
	
}
