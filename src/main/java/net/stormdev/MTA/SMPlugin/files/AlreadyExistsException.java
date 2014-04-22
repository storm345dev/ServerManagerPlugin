package net.stormdev.MTA.SMPlugin.files;

public class AlreadyExistsException extends Exception {
	private static final long serialVersionUID = 1L;
	public AlreadyExistsException(){
		super("Path already exists");
	}
}
