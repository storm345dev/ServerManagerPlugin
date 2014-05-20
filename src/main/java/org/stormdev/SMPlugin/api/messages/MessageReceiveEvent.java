package org.stormdev.SMPlugin.api.messages;

import org.stormdev.servermanager.api.events.ReceivedMessage;
import org.stormdev.servermanager.api.listeners.SMEvent;

public class MessageReceiveEvent implements org.stormdev.servermanager.api.events.MessageReceiveEvent{

	private ReceivedMessage msg;
	public MessageReceiveEvent(ReceivedMessage msg){
		this.msg = msg;
	}
	
	@Override
	public String getEventName() {
		return "Message Receive Event";
	}

	@Override
	public Class<? extends SMEvent> getEventClass() {
		return MessageReceiveEvent.class;
	}

	@Override
	public ReceivedMessage getMessage() {
		return msg;
	}
	
}
