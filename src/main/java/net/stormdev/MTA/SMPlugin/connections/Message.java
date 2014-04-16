package net.stormdev.MTA.SMPlugin.connections;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import net.stormdev.MTA.SMPlugin.core.Core;

public class Message {
	
	private static final int msgSplineLength = 256;
	private String to;
	private String from;
	private String msgTitle;
	private String toSend;
	private String id;
	
	public Message(String to, String from, String msgTitle, String toSend){
		this.setTo(to);
		this.setFrom(from);
		this.setMsgTitle(msgTitle);
		this.setMsg(toSend);
		id = UUID.randomUUID().toString();
	}
	
	private Message(String to, String from, String msgTitle, String toSend, String id){
		this.setTo(to);
		this.setFrom(from);
		this.setMsgTitle(msgTitle);
		this.setMsg(toSend);
		this.id = id;
	}
	
	public String getMsg() {
		return toSend;
	}
	
	public void setMsg(String toSend) {
		this.toSend = toSend;
	}
	
	public String getMsgTitle() {
		return msgTitle;
	}
	
	public void setMsgTitle(String msgTitle) {
		this.msgTitle = msgTitle;
	}
	
	public String getFrom() {
		return from;
	}
	
	public void setFrom(String from) {
		this.from = from;
	}
	
	public String getTo() {
		return to;
	}
	
	public void setTo(String to) {
		this.to = to;
	}
	
	public List<String> getRaw(){ // To|From|Title|i|iMax|Send|ID| And is automatically split for long msgs
		String enc = Core.plugin.encrypter.encrypt(getMsg());
		List<String> parts = new ArrayList<String>();
		
		for(int p=0;p<enc.length();p+=msgSplineLength){
			int max = p+msgSplineLength;
			if(max > enc.length()){
				max = enc.length();
			}
			String part = enc.substring(p, max);
			parts.add(part);
		}
		
		List<String> segments = new ArrayList<String>();
		for(int p=0;p<parts.size();p++){
			String part = parts.get(p);
			String segment = getTo()+"|"+getFrom()+"|"+getMsgTitle()+"|"+(p+1)+"|"+parts.size()+"|"+part+"|"+id+"|";
			segments.add(segment);
		}
		
		return segments;
	}
	
	public static Message fromRaw(List<String> raw){
		if(raw.size() < 1){
			return null;
		}
		StringBuilder product = new StringBuilder();
		String first = raw.get(0);
		String[] msgParts = first.split(Pattern.quote("|"));
		String to = msgParts[0];
		String from = msgParts[1];
		String title = msgParts[2];
		String id = msgParts[6];
		
		for(String part:raw){
			String[] parts = part.split(Pattern.quote("|"));
			String p = parts[5];
			product.append(p);
		}
		
		String enc = product.toString();
		String msg = Core.plugin.encrypter.decrypt(enc);
		return new Message(to, from, title, msg, id);
	}
	
	public static boolean test(){
		String testStr = UUID.randomUUID().toString();
		Message testMsg = new Message("testRecipient", "testSender", "testTitle", testStr);
		List<String> parts = testMsg.getRaw();
		Message translation = Message.fromRaw(parts);
		return translation.getMsg().equals(testStr);
	}
	
}
