package net.stormdev.MTA.SMPlugin.utils;

public class TaskTimeoutException extends Exception {
	private static final long serialVersionUID = 1L;
	public TaskTimeoutException(){
		super("Sync blocking task in ServerManager failed to finish in time and was timed out! This isn't a bug, it's just an operation failure.");
	}
}
