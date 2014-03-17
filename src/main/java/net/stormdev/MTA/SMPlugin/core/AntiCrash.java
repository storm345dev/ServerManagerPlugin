package net.stormdev.MTA.SMPlugin.core;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;

import net.stormdev.MTA.SMPlugin.utils.Scheduler;

public class AntiCrash extends Thread {
	
	private static AntiCrash instance;
	
	private UUID instanceId;
	private String command;
	private boolean running = true;
	
	public AntiCrash(UUID instance, String startCommand){
		this.instanceId = instance;
		AntiCrash.instance = this;
		this.command = startCommand;
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
					}}, 60); //60s
			} catch (Exception e) {
				//The task timed out!
				onTimeout();
			}
			
		}
		return;
	}
	
	private void onTimeout(){
		if(!running || instanceId != Core.instanceId){
			return;
		}
		try {
			Runtime.getRuntime().exec(command);
			Runtime.getRuntime().exit(0);
		} catch (IOException e) {
			//Oh well...
		}
		return;
	}
}
