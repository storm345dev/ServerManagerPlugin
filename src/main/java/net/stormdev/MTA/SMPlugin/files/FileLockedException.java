package net.stormdev.MTA.SMPlugin.files;

public class FileLockedException extends Exception {
	private static final long serialVersionUID = 1L;
	public FileLockedException(){
		super("File is locked!");
	}
}
