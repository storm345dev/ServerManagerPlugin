package net.stormdev.MTA.SMPlugin.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.stormdev.MTA.SMPlugin.utils.Scheduler;
import net.stormdev.MTA.SMPlugin.utils.TaskTimeoutException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class AntiCrash extends Thread {
	
	private static AntiCrash instance;
	
	private UUID instanceId;
	private String command;
	private boolean running = true;
	private String[] args;
	private int port;
	
	public AntiCrash(UUID instance, String startCommand){
		this.instanceId = instance;
		AntiCrash.instance = this;
		this.command = startCommand;
		String prefix = Core.config.getString("server.settings.restartScriptPrefix");
		prefix = prefix.trim(); //Remove whitespace
		args = prefix.split(" "); //Each cmd arg
		port = Bukkit.getPort();
	}
	
	public static AntiCrash getInstance(){
		return instance;
	}
	
	public UUID getAssociatedInstanceId(){
		return instanceId;
	}
	
	public void end(){
		this.running = false;
	}
	
	@Override
	public void run(){
		Core.logger.info("Auto-restart is "+ChatColor.GREEN+"enabled!");
		//Main script
		while(running){
			//Check if it's crashed
			try {
				Scheduler.runBlockingSyncTask(new Runnable(){

					@Override
					public void run() {
						Bukkit.getServer().getMotd(); //Do a meaningless task
						return;
					}}, 27);
			} catch (TaskTimeoutException e) {
				//The task timed out!
				try {
					Scheduler.runBlockingSyncTask(new Runnable(){

						@Override
						public void run() {
							Bukkit.getServer().getMotd(); //Do a meaningless task
							return;
						}}, 27);
				} catch (TaskTimeoutException e1) {
					onTimeout(false);
				} catch (Exception e1) {
					running = false; //Server is stopping/reloading
				}
				
			} catch(Exception e){
				running = false; //Server is stopping/reloading
			}
			try {
				Thread.sleep(5000); //5s
			} catch (InterruptedException e) {
			}
		}
		Core.logger.info("Auto-restart script exited!");
		return;
	}
	
	public void testFreeze(){
		try {
			Scheduler.runBlockingSyncTaskNoTimeout(new Runnable(){

				@Override
				public void run() {
					try {
						Thread.sleep(Integer.MAX_VALUE);
					} catch (InterruptedException e) {
						//Lol
					}
					return;
				}});
		} catch (Exception e) {
			// IDK
			e.printStackTrace();
		}
	}
	
	public void restart(){
		onTimeout(true);
	}
	
	private void onTimeout(boolean force){
		if(!force && (!running || instanceId != Core.instanceId)){
			return;
		}
		System.out.println("Restarting...!");
		try {
			// "cmd", "/c", "start"
			List<String> argss = new ArrayList<String>();
			for(int i=0;i<args.length;i++){
				String s = args[i];
				if(s == null || s.length() < 1){
					continue;
				}
				argss.add(s);
			}
			args = argss.toArray(new String[]{});
			final String[] cmds = new String[args.length+1];
			for(int i=0;i<args.length;i++){
				cmds[i] = args[i];
			}
			cmds[cmds.length-1] = command;
			
			Thread onShutOff = new Thread(){
				@Override
				public void run(){
					if(instanceId != Core.instanceId){
						return;
					}
					File outFile = new File("SMRestarts"+File.separator+"latest.txt");
					try {
						outFile.getParentFile().mkdirs();
						outFile.createNewFile();
					} catch (IOException e1) {
						// oh well
					}
					PrintStream ps = null;
					try {
						ps = new PrintStream(outFile);
					} catch (FileNotFoundException e2) {
						//whatever
					}
					if(ps != null){
						ps.println("Start restart log:");
						ps.println("Previous server shutdown!");
					}
					try {
						ps.println("Starting new server...");
						Thread daemon = new Thread(){
							
							@Override
							public void run(){
								File outFile = new File("SMRestarts"+File.separator+"startScriptLatest.txt");
								try {
									outFile.getParentFile().mkdirs();
									outFile.createNewFile();
								} catch (IOException e1) {
									// oh well
								}
								PrintStream ps = null;
								try {
									ps = new PrintStream(outFile);
								} catch (FileNotFoundException e2) {
									//whatever
								}
								
								ps.println("Successfully launched startup thread!");
								
								long startMS = System.currentTimeMillis();
								ps.println("Waiting on old server...");
								boolean oldRunning = true;
								while(oldRunning){
									try {
										ServerSocket ss = new ServerSocket(port);
										ss.setReuseAddress(true);
										ss.close();
										oldRunning = false;
									} catch (IOException e1) {
										//Old is running, port cannot bind
									}
								}
								
								ps.println("Old server FULLY shutdown after "+(System.currentTimeMillis()-startMS)+" milliseconds!");
								
								ps.println("Launching server...");
								ps.println("Executing system command(s):");
								StringBuilder sb = new StringBuilder();
								for(String cmd:cmds){
									if(sb.length() > 0){
										sb.append(", ");
									}
									sb.append(cmd);
								}
								ps.println("["+sb.toString()+"]");
								if(cmds.length < 2){
									ps.println("(Single)");
								}
								
								String current;
								try {
									current = new java.io.File( "." ).getCanonicalPath();
									 ps.println("In directory: "+current);
								} catch (IOException e1) {
									ps.println("(Unable to determine currest directory path)");
								}
						       
								
								Process newServer = null;
								try {
									if(cmds.length > 1){
										newServer = Runtime.getRuntime().exec(cmds); //Actually restart the server
									}
									else {
										newServer = Runtime.getRuntime().exec(cmds[0]); //Start the server
									}
									if(newServer != null){
										ps.println("New server successfully launched!");
									}
									else {
										ps.println("Failed to launch!");
									}
								} catch (IOException e) {
									e.printStackTrace(ps);
								}
								
								if(ps != null){
									ps.close();
								}
								return;
							}
						};
						daemon.setDaemon(true);
						daemon.start();
						
						ps.println("New server starting!");
					} catch (Exception e) {
						if(ps != null){
							e.printStackTrace(ps);
						}
					}
					if(ps != null){
						ps.println("Terminating restart handler...");
						ps.close();
					}
					return;
				}
			};
			onShutOff.setDaemon(true);
			Runtime.getRuntime().addShutdownHook(onShutOff);
			
			System.exit(0); //Exit the current server
			
		} catch (Exception e) {
			//Oh well...
			e.printStackTrace();
		}
		return;
	}
}
