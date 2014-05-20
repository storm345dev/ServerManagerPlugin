package org.stormdev.SMPlugin.api.messages;

import java.io.IOException;

import net.stormdev.MTA.SMPlugin.connections.Message;
import net.stormdev.MTA.SMPlugin.core.Core;

import org.stormdev.servermanager.api.messaging.InvalidRecipientException;
import org.stormdev.servermanager.api.messaging.MessageRecipient;
import org.stormdev.servermanager.api.messaging.MessageSendFailedException;
import org.stormdev.servermanager.api.messaging.Messager;
import org.stormdev.servermanager.api.messaging.MessagingUnavailableException;

public class Messenger implements Messager {

	@Override
	public boolean isMessagingAvailable() {
		try {
			return Core.plugin.connection.isConnected() && Core.plugin.connection.isIdentified();
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public void sendMessage(MessageRecipient recipient, String title,
			String message) throws InvalidRecipientException,
			MessagingUnavailableException, MessageSendFailedException {
		if(!isMessagingAvailable()){
			throw new MessagingUnavailableException();
		}
		
		String mTitle = "pluginMsg:"+title;
		
		if(recipient.getConnectionID() == null || Core.plugin.connection.getConnectionID() == null){
			throw new MessageSendFailedException();
		}
		
		Core.plugin.connection.sendMsg(new Message(recipient.getConnectionID(), Core.plugin.connection.getConnectionID(), mTitle, message));
	}
	
}
