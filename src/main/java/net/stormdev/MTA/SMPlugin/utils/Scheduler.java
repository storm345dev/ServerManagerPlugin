package net.stormdev.MTA.SMPlugin.utils;

import net.stormdev.ucars.trade.main;

import org.bukkit.Bukkit;

public class Scheduler {
	public static void runBlockingSyncTask(final Runnable run) throws Exception{
		final ToggleLatch latch = new ToggleLatch().lock(); //Create a new latch, and lock it
		
		Bukkit.getScheduler().runTask(main.plugin, new Runnable(){

			@Override
			public void run() {
				try {
					run.run();
				}
				finally {
					//It's finished
					latch.unlock();
				}
				return;
			}});
		
		int timeout = 50;
		while(latch.isLocked() && timeout > 0){
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			}
			timeout--;
		}
		
		if(timeout < 1){
			//It timed out
			throw new TaskTimeoutException();
		}
	}
	
	public static void runBlockingSyncTask(final Runnable run, int timeOut) throws Exception{
		final ToggleLatch latch = new ToggleLatch().lock(); //Create a new latch, and lock it
		
		Bukkit.getScheduler().runTask(main.plugin, new Runnable(){

			@Override
			public void run() {
				try {
					run.run();
				}
				finally {
					//It's finished
					latch.unlock();
				}
				return;
			}});
		
		int timeout = timeOut * 4;
		while(latch.isLocked() && timeout > 0){
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			timeout--;
		}
		
		if(timeout < 1){
			//It timed out
			throw new TaskTimeoutException();
		}
	}
	
	public static void runBlockingSyncTaskNoTimeout(final Runnable run) throws Exception{
		final ToggleLatch latch = new ToggleLatch().lock(); //Create a new latch, and lock it
		
		Bukkit.getScheduler().runTask(main.plugin, new Runnable(){

			@Override
			public void run() {
				try {
					run.run();
				}
				finally {
					//It's finished
					latch.unlock();
				}
				return;
			}});
		
		while(latch.isLocked()){
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			}
		}
	}
}
