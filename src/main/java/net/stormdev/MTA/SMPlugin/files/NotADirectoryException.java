package net.stormdev.MTA.SMPlugin.files;

public class NotADirectoryException extends Exception {
	private static final long serialVersionUID = 1L;
	public NotADirectoryException(){
		super("Path is not a directory!");
	}
}
