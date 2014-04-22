package net.stormdev.MTA.SMPlugin.files;

public class IsADirectoryException extends Exception {
	private static final long serialVersionUID = 1L;
	public IsADirectoryException(){
		super("Path is a directory!");
	}
}
