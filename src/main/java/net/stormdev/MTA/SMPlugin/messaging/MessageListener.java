package net.stormdev.MTA.SMPlugin.messaging;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import net.stormdev.MTA.SMPlugin.connections.Message;
import net.stormdev.MTA.SMPlugin.core.AntiCrash;
import net.stormdev.MTA.SMPlugin.core.Core;
import net.stormdev.MTA.SMPlugin.events.Listener;
import net.stormdev.MTA.SMPlugin.files.FileTools;
import net.stormdev.MTA.SMPlugin.files.MessageFiles;
import net.stormdev.MTA.SMPlugin.files.NotADirectoryException;
import net.stormdev.MTA.SMPlugin.requests.UpdateRequest;
import net.stormdev.MTA.SMPlugin.servers.Server;
import net.stormdev.MTA.SMPlugin.utils.Colors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.base.Charsets;

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
			final Player player = Bukkit.getPlayer(name);
			if(player != null){
				final StringBuilder msgBuilder = new StringBuilder();
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
				Bukkit.getScheduler().runTask(Core.plugin, new Runnable(){

					@Override
					public void run() {
						player.kickPlayer(Colors.colorise(msgBuilder.toString()));
						return;
					}});
			}
			Core.logger.info("Web connection kicked user: "+name);
			return;
		}
		else if(title.equals("warnPlayer")){
			String args[] = message.getMsg().split(" ");
			if(args.length < 2){
				return;
			}
			final String name = args[0];
			final Player player = Bukkit.getPlayer(name);
			String msg = "unspecified";
			if(player != null){
				final StringBuilder msgBuilder = new StringBuilder();
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
				msg = Colors.colorise(msgBuilder.toString());
				final String mesg = msg;
				Bukkit.getScheduler().runTask(Core.plugin, new Runnable(){

					@Override
					public void run() {
						player.sendMessage(ChatColor.RED+"You were warned for "+ChatColor.GRAY+mesg);
						return;
					}});
			}
			Core.logger.info("Web connection warned user: "+name);
			return;
		}
		else if(title.equals("banPlayer")){
			String args[] = message.getMsg().split(" ");
			if(args.length < 2){
				return;
			}
			final String name = args[0];
			final Player player = Bukkit.getPlayer(name);
			String msg = "unspecified";
			if(player != null){
				final StringBuilder msgBuilder = new StringBuilder();
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
				msg = Colors.colorise(msgBuilder.toString());
				final String mesg = "Banned: "+msg;
				Bukkit.getScheduler().runTask(Core.plugin, new Runnable(){

					@Override
					public void run() {
						player.kickPlayer(mesg);
						player.setBanned(true);
						return;
					}});
			}
			Core.logger.info("Web connection banned user: "+name);
			return;
		}
		else if(title.equals("getPlayers")){
			StringBuilder playerList = new StringBuilder(",");
			boolean first = true;
			Player[] online = Bukkit.getOnlinePlayers().clone();
			for(Player player:online){
				if(!first){
					playerList.append(",");
				}
				else{
					first = false;
				}
				playerList.append(player.getName());
			}
			
			String msg = playerList.toString();
			Core.plugin.connection.sendMsg(new Message(message.getFrom(), Core.plugin.connection.getConnectionID(), "playerList", msg));
			return;
		}
		else if(title.equals("getFileList")){
			String dir = message.getMsg();
			String systemDir = FileTools.getPathFromOnlinePath(dir, false);
			String response = dir+":";
			try {
				response += MessageFiles.getFileListResponse(systemDir);
			} catch (NotADirectoryException e) {
				response += "FileNotFound"; //Not found
			}
			Core.plugin.connection.sendMsg(new Message(message.getFrom(), Core.plugin.connection.getConnectionID(), "fileList", response));
			return;
		}
		else if(title.equals("getFile")){
			final String path = message.getMsg();
			final String from = message.getFrom();
			Bukkit.getScheduler().runTaskAsynchronously(Core.plugin, new Runnable(){

				@Override
				public void run() {
					try {
						String sysPath = FileTools.getPathFromOnlinePath(path, false);
						File onSys = new File(sysPath);
						String name = onSys.getName();
						long length = onSys.length(); //In bytes
						if(length > 1024*1024*250){//If bigger than 250MB
							Core.plugin.connection.sendMsg(new Message(from, Core.plugin.connection.getConnectionID(), "fileData", "FileTooBig"));
							return;
						}
						if(name.toLowerCase().endsWith(".jar") || name.toLowerCase().endsWith(".bat") || name.toLowerCase().endsWith(".exe")){
							Core.plugin.connection.sendMsg(new Message(from, Core.plugin.connection.getConnectionID(), "fileData", "Forbidden"));
							return;
						}
						byte[] data = MessageFiles.getFileResponse(path, false);
						StringBuilder response = new StringBuilder(name+"|"); // name|data
						String dataStr = new String(data, Charsets.UTF_8);
						response.append(dataStr);
						Core.plugin.connection.sendMsg(new Message(from, Core.plugin.connection.getConnectionID(), "fileData", response.toString()));
						
					} catch (Exception e) {
						Core.plugin.connection.sendMsg(new Message(from, Core.plugin.connection.getConnectionID(), "fileData", "FileNotFound"));
					}
					return;
				}});
			
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
