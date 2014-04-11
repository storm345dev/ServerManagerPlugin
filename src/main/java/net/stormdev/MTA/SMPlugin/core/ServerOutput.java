package net.stormdev.MTA.SMPlugin.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import net.stormdev.MTA.SMPlugin.connections.Message;
import net.stormdev.MTA.SMPlugin.utils.Colors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;



public class ServerOutput extends AbstractAppender {
	private static final int rateLimit = 10;
	private Logger output;
	private long lastMessage = 0;
	private int msgBuffer;
	private int overMsg;
	
	public ServerOutput(){
		super("ServerManager", null, null);
		//output = (Logger) LogManager.getRootLogger();
		try {
			super.start();
			output = (Logger) LogManager.getRootLogger();
			output.addAppender(this);
		} catch (Exception e) {
			Core.logger.error("Unable to start console sharing!", e);
		}
	}

	@Override
	public void append(LogEvent event) {
		String msg = getPrefix(event.getMillis(), event.getLevel()) + event.getMessage().getFormattedMessage();
		msg = ChatColor.stripColor(Colors.colorise(msg)); //Remove all colour chars
		onReceive(msg);
	}
	
	private void onReceive(String msg){
		long now = System.currentTimeMillis();
		long diff = now-lastMessage;
		boolean skip = false;
		boolean reset = true;
		if(diff < 500){ //1/2s interval in rate limit
			reset = false;
			if(msgBuffer > rateLimit){
				skip = true;
			}
		}
		if(reset && !skip){
			if(overMsg > 0){
				send("(Skipped "+overMsg+" lines due to rate limit!)");
			}
			lastMessage = now;
			msgBuffer = 0;
			overMsg = 0;
		}
		if(!skip){
			send(msg);
			msgBuffer++;
		}
		else{
			overMsg++;
		}
	}
	
	private void send(final String msg){
		//Send the message to the online client
		Bukkit.getScheduler().runTaskAsynchronously(Core.plugin, new Runnable(){

			@Override
			public void run() {
				String out = String.format(msg, Locale.ENGLISH);
				/*
				if(out.contains("<") && out.contains(">")){
					int start = out.indexOf("<");
					int end = out.indexOf(">");
					String name = out.substring(start+1, end);
					name = name.replaceAll("\u001B\\[[;\\d]*m", "").replaceAll(Pattern.quote("\""), Matcher.quoteReplacement("\\\"")).replaceAll(Pattern.quote("'"), Matcher.quoteReplacement("\\\'"));
					String first = out.substring(0, 8);
					String second = out.substring(end+1);
					out = first+" < "+name+" > "+second;
				}
				final String toSend =
					    out.replaceAll("\u001B\\[[;\\d]*m", "").replaceAll(Pattern.quote("\""), Matcher.quoteReplacement("\\\"")).replaceAll(Pattern.quote("'"), Matcher.quoteReplacement("\\\'"));
				*/
				final String toSend = out;
				Core.plugin.connection.sendMsg(new Message("web", Core.plugin.connection.getConnectionID(), "consoleOutput", toSend));
				return;
			}});
	}
	
	private String getTime(long millis){
		return new SimpleDateFormat("hh:mm:ss").format(
				new Date(millis));
	}
	
	private String getPrefix(long millis, Level level){
		return getTime(millis) + " [" + level.toString() + "] ";
	}
	

}
