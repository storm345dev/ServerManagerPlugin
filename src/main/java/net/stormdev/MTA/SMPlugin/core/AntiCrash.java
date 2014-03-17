package net.stormdev.MTA.SMPlugin.core;

import java.util.UUID;

import org.bukkit.Bukkit;

import net.stormdev.MTA.SMPlugin.utils.Scheduler;

public class AntiCrash extends Thread {
	
	private static AntiCrash instance;
	
	private UUID instanceId;
	private boolean running = true;
	public AntiCrash(UUID instance){
		this.instanceId = instance;
		AntiCrash.instance = this;
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
		//TODO Main script
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
		
	}
}
