package org.stormdev.SMPlugin.api.messages;

import org.stormdev.servermanager.api.APIProvider;
import org.stormdev.servermanager.api.messaging.MessageRecipient;

public class ReceivedMessage implements org.stormdev.servermanager.api.events.ReceivedMessage {

	private String title;
	private String msg;
	private String from;
	
	public ReceivedMessage(String title, String msg, String from){
		this.title = title;
		this.msg = msg;
		this.from = from;
	}
	
	@Override
	public MessageRecipient getSender() {
		if(from.equalsIgnoreCase("host")){
			return MessageRecipient.HOST;
		}
		if(APIProvider.getAPI().getServers().isServerConnected(from)){
			return APIProvider.getAPI().getServers().getServer(from).getAsMessageRecipient();
		}
		return MessageRecipient.create(from);
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getMessage() {
		return msg;
	}

}
