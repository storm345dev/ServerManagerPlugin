package net.stormdev.MTA.SMPlugin.files;

public class FileResult {
	private String name;
	private boolean dir;
	
	public FileResult(String name, boolean dir){
		this.name = name;
		this.dir = dir;
	}
	
	public String getName(){
		return name;
	}
	
	public boolean isDirectory(){
		return dir;
	}
}
