package net.stormdev.MTA.SMPlugin.messaging;

import net.stormdev.MTA.SMPlugin.connections.Message;
import net.stormdev.MTA.SMPlugin.core.Core;
import net.stormdev.MTA.SMPlugin.events.Listener;

public class MessageListener implements Listener<MessageEvent> {

	private Core main;
	public MessageListener(){
		main = Core.plugin;
		Core.plugin.eventManager.registerListener(new MessageEvent(null), this); //Registers the event to us
	}
	
	public void onCall(MessageEvent event) {
		Message message = event.getMessage();
		
		//TODO Handle message receiving
		Core.plugin.logger.info("Received: "+message.getMsg());
		
		return;
	}
	
}
