package net.stormdev.MTA.SMPlugin.messaging;

import net.stormdev.MTA.SMPlugin.connections.Message;
import net.stormdev.MTA.SMPlugin.events.Event;

public class MessageEvent implements Event {
	private Message msg;
	public MessageEvent(Message message){
		this.msg = message;
	}
	public Message getMessage(){
		return this.msg;
	}
}
