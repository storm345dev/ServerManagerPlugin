package net.stormdev.MTA.SMPlugin.connections;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import net.stormdev.MTA.SMPlugin.core.Core;

public class TransitMessage {
	
	private String id;
	private volatile String[] msgParts;
	int toRecieve;
	
	public TransitMessage(String firstRecieved){
		if(firstRecieved == null)
			firstRecieved = "";
		String[] segs = firstRecieved.split(Pattern.quote("|"));
		id = segs[6]; //The ID segment
		toRecieve = Integer.parseInt(segs[4]); //The number to recieve
		int self = Integer.parseInt(segs[3]);
		
		if(toRecieve < 1)
			toRecieve = 1;
		if(self < 1)
			self = 1;
		
		msgParts = new String[toRecieve];
		msgParts[(self-1)] = firstRecieved;
	}
	
	public Message getMessage(){
		if(!hasRecievedAll()){
			throw new RuntimeException("Bad access to message in transit! Attempted message read before all recieved!");
		}
		return Message.fromRaw(Arrays.asList(msgParts));
	}
	
	public boolean hasRecievedAll(){
		for(String p:msgParts){
			if(p == null || p.length() < 1){
				return false;
			}
		}
		return true;
	}
	
	public boolean onRecieve(String msg){ //Return true if it's out message that is matched by the ID
		String[] parts = msg.split(Pattern.quote("|"));
		
		String rId = parts[6]; //The ID segment;
		if(!rId.equals(id)){
			return false; //Not us
		}
		
		String rawNo = parts[3];
		int n = 0;
		try {
			n = Integer.parseInt(rawNo);
		} catch (NumberFormatException e) {
			return false;
		}
		
		msgParts[(n-1)] = msg;
		
		return true;
	}
	
	public static boolean test(){
		Message test = new Message("to", "from", "test", UUID.randomUUID().toString());
		List<String> parts = test.getRaw();
		
		TransitMessage recieving = new TransitMessage(parts.get(0));
		
		for(String part:parts){
			recieving.onRecieve(part);
		}
		
		if(!recieving.hasRecievedAll()){
			Core.logger.info("NOT RECIEVED ALL");
			return false;
		}
		
		String original = test.getMsg();
		String back = recieving.getMessage().getMsg();

		return original.equals(back);
	}
}
