package net.stormdev.MTA.SMPlugin.messaging;

import java.io.IOException;
import java.util.regex.Pattern;

import net.stormdev.MTA.SMPlugin.connections.Message;
import net.stormdev.MTA.SMPlugin.core.AntiCrash;
import net.stormdev.MTA.SMPlugin.core.Core;
import net.stormdev.MTA.SMPlugin.events.Listener;
import net.stormdev.MTA.SMPlugin.requests.UpdateRequest;
import net.stormdev.MTA.SMPlugin.servers.Server;
import net.stormdev.MTA.SMPlugin.utils.Colors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MessageListener implements Listener<MessageEvent> {

	private Core main;
	public MessageListener(){
		main = Core.plugin;
		Core.plugin.eventManager.registerListener(MessageEvent.class, this); //Registers the event to us
	}
	
	public void onCall(MessageEvent event) {
		Message message = event.getMessage();
		
		//TODO Handle message receiving
		Core.logger.debug("Received: "+message.getMsg());
		
		String title = message.getMsgTitle();
		
		if(message.getFrom().equals(Core.plugin.connection.getConnectionID())){ //From ourself
			return;
		}
		else if(title.equals("executeCommand")){
			String command = message.getMsg();
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
			return;
		}
		else if(title.equals("printMsg")){
			String msg = message.getMsg();
			String coloured = Colors.colorise(msg);
			Core.logger.info(coloured);
			return;
		}
		else if(title.equals("alert")){
			String msg = message.getMsg();
			String coloured = Colors.colorise(msg);
			Bukkit.broadcastMessage(coloured);
			return;
		}
		else if(title.equals("reload")){
			Bukkit.getServer().reload();
		}
		else if(title.equals("restart")){
			AntiCrash.getInstance().restart();
			return;
		}
		else if(title.equals("stop")){
			Core.logger.info("Web connection is stopping server...");
			Bukkit.getServer().shutdown();
		}
		else if(title.equals("setTime")){
			String command = "time set "+message.getMsg();
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
		}
		else if(title.equals("kickPlayer")){
			String args[] = message.getMsg().split(" ");
			if(args.length < 1){
				return;
			}
			String name = args[0];
			Player player = Bukkit.getPlayer(name);
			if(player != null){
				StringBuilder msgBuilder = new StringBuilder();
				boolean first = true;
				for(int i=1;i<args.length;i++){
					if(!first){
						msgBuilder.append(" ");
					}
					else {
						first = false;
					}
					msgBuilder.append(args[i]);
				}
				player.kickPlayer(msgBuilder.toString());
			}
			Core.logger.info("Web connection kicked user: "+name);
			return;
		}
		else if(message.getFrom().equals(MessageRecipient.HOST.getConnectionID())){
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
			else if(title.equalsIgnoreCase("servers")){
				//We've been sent a list of all servers, process it...
				String list = message.getMsg();
				String[] raws = list.split(Pattern.quote(","));
				Server[] srvs = new Server[raws.length];
				for(int i=0;i<raws.length;i++){
					Server s;
					try {
						s = Server.fromRawString(raws[i]);
					} catch (Exception e) {
						// An error???
						continue;
					}
					srvs[i] = s;
				}
				Core.plugin.servers.setServers(srvs);
			}
		}
		
		return;
	}
	
}
