package net.stormdev.MTA.SMPlugin.utils;

public class CountDown {
	private int count;
	private int max;
	public CountDown(int max){
		this.max = max;
		this.count = 0;
	}
	
	public synchronized void increment(){
		count++;
		if(count > max){
			count = 0;
		}
	}
	
	public synchronized int get(){
		return count;
	}
}
