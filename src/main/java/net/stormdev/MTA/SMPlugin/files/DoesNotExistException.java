package net.stormdev.MTA.SMPlugin.files;

public class DoesNotExistException extends Exception {
	private static final long serialVersionUID = 1L;
	public DoesNotExistException(){
		super("Path does not exist");
	}
}
