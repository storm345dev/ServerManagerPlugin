package net.stormdev.MTA.SMPlugin.messaging;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import net.stormdev.MTA.SMPlugin.connections.Message;
import net.stormdev.MTA.SMPlugin.core.AntiCrash;
import net.stormdev.MTA.SMPlugin.core.Core;
import net.stormdev.MTA.SMPlugin.events.Listener;
import net.stormdev.MTA.SMPlugin.files.AlreadyExistsException;
import net.stormdev.MTA.SMPlugin.files.FileLockedException;
import net.stormdev.MTA.SMPlugin.files.FileTools;
import net.stormdev.MTA.SMPlugin.files.MessageFiles;
import net.stormdev.MTA.SMPlugin.files.NotADirectoryException;
import net.stormdev.MTA.SMPlugin.requests.UpdateRequest;
import net.stormdev.MTA.SMPlugin.servers.Server;
import net.stormdev.MTA.SMPlugin.utils.Colors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.stormdev.SMPlugin.api.messages.ReceivedMessage;
import org.stormdev.servermanager.api.APIProvider;
import org.stormdev.servermanager.api.events.MessageReceiveEvent;

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
		
		if(title.startsWith("pluginMsg:")){
			String mTitle = title.replaceFirst(Pattern.quote("pluginMsg:"), "");
			ReceivedMessage rm = new ReceivedMessage(mTitle, message.getMsg(), message.getFrom());
			APIProvider.getAPI().getEventManager().callEvent(new MessageReceiveEvent(rm));
			return;
		}
		else if(title.equals("testString")){
			boolean equal = Core.plugin.testString.equals(message.getMsg());
			System.out.println("Test string success: "+equal);
			return;
		}
		/*
		else if(message.getFrom().equals(Core.plugin.connection.getConnectionID())){ //From ourself
			return;
		}
		*/
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
			new Thread(){ //Use separate thread to stop ANY server freezing
				@Override
				public void run(){
					try {
						String sysPath = FileTools.getPathFromOnlinePath(path, false);
						File onSys = new File(sysPath);
						String name = onSys.getName();
						long length = onSys.length(); //In bytes
						if(length > 1024*1000){//If bigger than 1000KB
							Core.plugin.connection.sendMsg(new Message(from, Core.plugin.connection.getConnectionID(), "fileData", "FileTooBig"));
							return;
						}
						/*
						if(name.toLowerCase().endsWith(".jar") || name.toLowerCase().endsWith(".bat") || name.toLowerCase().endsWith(".exe")){
							Core.plugin.connection.sendMsg(new Message(from, Core.plugin.connection.getConnectionID(), "fileData", "Forbidden"));
							return;
						}
						*/
						Thread.yield();
						byte[] data = MessageFiles.getFileResponse(path, false);
						Thread.yield();
						StringBuilder response = new StringBuilder(name+"|"); // name|data
						String dataStr = new String(data, Charsets.ISO_8859_1);
						Thread.yield();
						response.append(dataStr);
						Thread.yield();
						Core.plugin.connection.sendMsg(new Message(from, Core.plugin.connection.getConnectionID(), "fileData", response.toString()));
						
					} catch (IOException e) {
						Core.plugin.connection.sendMsg(new Message(from, Core.plugin.connection.getConnectionID(), "fileData", "FileNotFound"));
					} catch (FileLockedException e) {
						Core.plugin.connection.sendMsg(new Message(from, Core.plugin.connection.getConnectionID(), "fileData", "FileLocked"));
					} catch (Exception e){
						Core.plugin.connection.sendMsg(new Message(from, Core.plugin.connection.getConnectionID(), "fileData", "FileNotFound"));
					}
					return;
				}
			}.start();
			
			return;
		}
		else if(title.equals("renameFile")){
			String[] parts = message.getMsg().split(Pattern.quote("|"));
			if(parts.length < 2){
				return;
			}
			String path = parts[0];
			String newName = parts[1];
			String sysPath = FileTools.getPathFromOnlinePath(path, false);
			
			try {
				if(!FileTools.renameFile(sysPath, newName, true)){
					return;
				}
			} catch (Exception e) {
				Core.logger.info("File rename failed!");
				return; //Error
			}
			
			try { //Send a file list response
				String dir = path.substring(0, path.lastIndexOf("/"));
				String systemDir = FileTools.getPathFromOnlinePath(dir, false);
				String response = dir+":";
				try {
					response += MessageFiles.getFileListResponse(systemDir);
				} catch (NotADirectoryException e) {
					response += "FileNotFound"; //Not found
				}
				Core.plugin.connection.sendMsg(new Message(message.getFrom(), Core.plugin.connection.getConnectionID(), "fileList", response));
				return;
			} catch (Exception e) {
				//Path invalid
			}
			
			return;
		}
		else if(title.equals("deleteFile")){
			final String path = message.getMsg();
			final String from = message.getFrom();
			final String sysPath = FileTools.getPathFromOnlinePath(path, false);
			
			new Thread(){
				@Override
				public void run(){
					File f = new File(sysPath);
					
					if(!f.isDirectory()){
						try {
							if(!f.delete()){
								f.deleteOnExit();
							}
						} catch (Exception e1) {
							//Access denied etc
							return;
						}
					}
					else {
						try {
							if(!FileTools.deleteDirectory(f)){
								f.deleteOnExit();
							}
						} catch (Exception e) {
							// Access denied etc
						}
					}
					
					try { //Send a file list response
						String dir = path.substring(0, path.lastIndexOf("/"));
						String systemDir = FileTools.getPathFromOnlinePath(dir, false);
						String response = dir+":";
						try {
							response += MessageFiles.getFileListResponse(systemDir);
						} catch (NotADirectoryException e) {
							response += "FileNotFound"; //Not found
						}
						Core.plugin.connection.sendMsg(new Message(from, Core.plugin.connection.getConnectionID(), "fileList", response));
						return;
					} catch (Exception e) {
						//Path invalid
					}
					return;
				}
				
			}.run();
			return;
		}
		else if(title.equals("newFolder")){
			final String path = message.getMsg();
			final String from = message.getFrom();
			final String sysPath = FileTools.getPathFromOnlinePath(path, false);
			Core.logger.info("Attempting to create a new folder...");
			if(path.contains(Pattern.quote("."))){
				Core.logger.info("Path cannot contain '.'!");
				return;
			}
			
			new Thread(){
				@Override
				public void run(){
					File f = new File(sysPath);
					
					if(f.exists()){
						Core.logger.info("Folder already exists!");
						return;
					}
					
					f.mkdirs(); //Create a new folder
					
					try { //Send a file list response
						String dir = path.substring(0, path.lastIndexOf("/"));
						String systemDir = FileTools.getPathFromOnlinePath(dir, false);
						String response = dir+":";
						try {
							response += MessageFiles.getFileListResponse(systemDir);
						} catch (NotADirectoryException e) {
							response += "FileNotFound"; //Not found
						}
						Core.plugin.connection.sendMsg(new Message(from, Core.plugin.connection.getConnectionID(), "fileList", response));
						return;
					} catch (Exception e) {
						//Path invalid
					}
					return;
				}
				
			}.run();
			return;
		}
		else if(title.equals("uploadFile")){
			System.out.println("Received a file...");
			String out = message.getMsg();
			
			int splitIndex = out.indexOf("|"); //First index of
			if(splitIndex <= 0){ //Not found, or no path
				return;
			}
			
			final String path = out.substring(0, splitIndex);
			final String from = message.getFrom();
			String dataStr = out.substring(splitIndex+1);
			
			final byte[] fileContents = dataStr.getBytes(Charsets.ISO_8859_1);
			
			Core.logger.info("Received File: "+path+" Size: "+fileContents.length);
			
			new Thread(){
				@Override
				public void run(){
					String sysPath = FileTools.getPathFromOnlinePath(path, false);
					try {
						FileTools.saveFile(sysPath, fileContents, false);
					} catch (AlreadyExistsException e) {
						//NVM, we've set to override
					} catch (IOException e) {
						Core.logger.info("Error saving file! Do you have access to that directory?");
						return;
					} catch (FileLockedException e) {
						Core.logger.info("Error saving file! A file was locked?");
						return;
					}
					
					try { //Send a file list response
						String dir = path.substring(0, path.lastIndexOf("/"));
						String systemDir = FileTools.getPathFromOnlinePath(dir, false);
						String response = dir+":";
						try {
							response += MessageFiles.getFileListResponse(systemDir);
						} catch (NotADirectoryException e) {
							response += "FileNotFound"; //Not found
						}
						Core.plugin.connection.sendMsg(new Message(from, Core.plugin.connection.getConnectionID(), "fileList", response));
						return;
					} catch (Exception e) {
						//Path invalid
					}
					return;
				}
			}.start();
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
