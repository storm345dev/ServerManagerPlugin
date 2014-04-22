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
	
	@Override
	public String toString(){
		return name+":"+dir;
	}
	
	public static FileResult fromString(String in){
		int endSeg = in.lastIndexOf(":");
		if(endSeg < 0 || (endSeg+1)>in.length()){
			return null;
		}
		int startPos = endSeg+1;
		String isDirRaw = in.substring(startPos);
		boolean isDir = isDirRaw.equalsIgnoreCase("true") ? true:false;
		String name = in.substring(0, endSeg);
		return new FileResult(name, isDir);
	}
}
