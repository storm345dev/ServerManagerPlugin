package net.stormdev.MTA.SMPlugin.files;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MessageFiles {
	private static final String splitter = "|%/\\%|"; // '|%/\%|'
	
	public static byte[] getFileResponse(String webPath, boolean allowRoot) throws DoesNotExistException, IsADirectoryException, IOException, FileLockedException{
		String sysPath = FileTools.getPathFromOnlinePath(webPath, allowRoot);
		byte[] data = FileTools.getFileContents(sysPath);
		return data;
	}
	
	public static String getFileListResponse(List<FileResult> results) throws NotADirectoryException{
		StringBuilder list = new StringBuilder(splitter); //Use illegal as a filename in most file systems
		for(FileResult result:results){
			if(!result.isDirectory()){
				continue;
			}
			list.append(result.toString());
			list.append(splitter);
		}
		for(FileResult result:results){
			if(result.isDirectory()){
				continue;
			}
			list.append(result.toString());
			list.append(splitter);
		}
		return list.toString();
	}
	
	public static String getFileListResponse(String dir) throws NotADirectoryException{
		List<FileResult> results = FileTools.listFiles(dir);
		return getFileListResponse(results);
	}
	
	public static List<FileResult> getFileListFromResponse(String response){
		List<FileResult> results = new ArrayList<FileResult>();
		String[] arr = response.split(Pattern.quote(splitter));
		
		for(String raw:arr){
			if(raw == null || raw.length() < 1){
				continue;
			}
			FileResult result = FileResult.fromString(raw);
			if(result != null){
				results.add(result);
			}
		}
		return results;
	}
}
