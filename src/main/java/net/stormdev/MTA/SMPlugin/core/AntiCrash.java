package net.stormdev.MTA.SMPlugin.core;

import java.io.IOException;
import java.util.UUID;

import net.stormdev.MTA.SMPlugin.utils.Scheduler;
import net.stormdev.MTA.SMPlugin.utils.TaskTimeoutException;

import org.bukkit.Bukkit;

public class AntiCrash extends Thread {
	
	private static AntiCrash instance;
	
	private UUID instanceId;
	private String command;
	private boolean running = true;
	private String[] args;
	
	public AntiCrash(UUID instance, String startCommand){
		this.instanceId = instance;
		AntiCrash.instance = this;
		this.command = startCommand;
		String prefix = Core.config.getString("server.settings.restartScriptPrefix");
		prefix = prefix.trim(); //Remove whitespace
		args = prefix.split(" "); //Each cmd arg
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
						Thread.sleep(120000);
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
			String[] cmds = new String[args.length+1];
			for(int i=0;i<args.length;i++){
				cmds[i] = args[i];
			}
			cmds[cmds.length-1] = command;
			Runtime.getRuntime().exec(cmds); //Actually restart the server
			System.exit(1); //Exit the current server
			
		} catch (IOException e) {
			//Oh well...
		}
		return;
	}
}
