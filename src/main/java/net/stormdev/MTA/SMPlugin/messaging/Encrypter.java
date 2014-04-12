package net.stormdev.MTA.SMPlugin.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.stormdev.MTA.SMPlugin.connections.Message;
import net.stormdev.MTA.SMPlugin.connections.TransitMessage;
import net.stormdev.MTA.SMPlugin.core.Core;

public class Encrypter {
	
	private volatile char[] pass;
	public static String networkTest;
	
	public Encrypter(String phrase){
		pass = phrase.toCharArray();
	}
	
	public String change(String msg){ //Encrypts AND decrypts
		boolean decrypt = msg.startsWith("`");
		if(decrypt){
			msg = fromChanged(msg);
		}
		
		List<Integer> chs = new ArrayList<Integer>();
		char[] chars = msg.toCharArray();
		
		if(pass.length < 1){
			throw new RuntimeException("Invalid security passphrase for the encryption!");
		}
		
		for(int i = 0,z = 0; i < chars.length; i++){
			int ref = Character.codePointAt(chars,i); //The unicode char number
			int pRef = Character.codePointAt(pass, z);
			int xor = ref ^ pRef; //XOR them together
			
			chs.add(xor);
			
			z++;
			if(z >= pass.length){
				z = 0;
			}
		}
		
		StringBuilder mb = new StringBuilder();
		
		for(int i:chs){
			if(mb.length() < 1){
				mb.append(i); //Add the number 'x'
				continue;
			}
			mb.append(","+i); //Add the number with command ',x'
		}
		
		return "`"+mb.toString();
	}
	
	public String fromChanged(String changed){ //Only translates, doesn't decrypt
		changed = changed.replaceAll("`", "");
		String[] parts = changed.split(",");
		
		StringBuilder product = new StringBuilder();
		for(String segment:parts){
			int i;
			try {
				i = Integer.parseInt(segment);
			} catch (NumberFormatException e) {
				continue; //Invalid segment
			}
			
			char[] ch = Character.toChars(i);
			product.append(ch);
		}
		
		return product.toString();
	}
	
	public boolean test(){
		String rand = UUID.randomUUID().toString()+UUID.randomUUID().toString()+UUID.randomUUID().toString()+"fsufhsdfhsdkjfhsdjkf|||||11";
		String test = fromChanged(
				change(
						change(rand)  )  );
		return rand.equals(test) && Message.test() && TransitMessage.test();
	}
	
	public void testNetwork(){
		String rand = UUID.randomUUID().toString()+UUID.randomUUID().toString()+UUID.randomUUID().toString()+"fsufhsdfhsdkjfhsdjkf|||||11";
		Message testMsg = new Message(Core.plugin.connection.getConnectionID(), Core.plugin.connection.getConnectionID(), "networkTest", rand);
		networkTest = rand;
		Core.logger.debug("Network test msg: "+rand);
		Core.plugin.connection.sendMsg(testMsg);
	}
}
