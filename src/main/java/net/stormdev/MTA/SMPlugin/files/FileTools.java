package net.stormdev.MTA.SMPlugin.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.org.apache.commons.io.FileUtils;

import com.google.common.io.Files;

public class FileTools {
	
	private static String serverDir = null;
	
	public static String getServerDirPath(){
		if(serverDir == null){
			try {
				serverDir = new File(".").getCanonicalPath();
			} catch (IOException e) {
				serverDir = new File(".").getAbsolutePath();
			}
			if(serverDir.endsWith(Pattern.quote(File.separator))){
				serverDir = serverDir.substring(0, (serverDir.length()-1));
			}
		}
		return serverDir;
	}
	
	public static boolean deleteDirectory(File directory) {
	    if(directory.exists()){
	        File[] files = directory.listFiles();
	        if(null!=files){
	            for(int i=0; i<files.length; i++) {
	                if(files[i].isDirectory()) {
	                    deleteDirectory(files[i]);
	                }
	                else {
	                    files[i].delete();
	                }
	            }
	        }
	    }
	    return(directory.delete());
	}
	
	public static boolean isFileLocked(File file){
		boolean isFileUnlocked = false;
		try {
		    FileUtils.touch(file);
		    isFileUnlocked = true;
		} catch (IOException e) {
		    isFileUnlocked = false;
		}
		return !isFileUnlocked;
	}
	
	public static String getPathFromOnlinePath(String in, boolean allowRoot){
		in = in.replaceAll(Pattern.quote("/"), Matcher.quoteReplacement(File.separator));
		String lin = in.toLowerCase();
		if(lin.startsWith("server")){
			int pathStart = in.indexOf(File.separator)+1; //First instance of '/' + 1
			if(pathStart <= 0 || pathStart > in.length()){ //They typed 'server', or '', or 'server/'
				return getServerDirPath();
			}
			String path = in.substring(pathStart);
			in = getServerDirPath()+File.separator+path;
		}
		else{
			if(!allowRoot){
				throw new SecurityException("Access denied");
			}
		}
		return in;
	}
	
	public static String getOnlinePathFromFilePath(String in){
		in = in.replaceAll(Pattern.quote(getServerDirPath()), "server");
		in = in.replaceAll(Pattern.quote(File.separator), Matcher.quoteReplacement("/"));
		return in;
	}
	
	public static List<FileResult> listFiles(String path) throws NotADirectoryException{
		List<FileResult> results = new ArrayList<FileResult>();
		File dir = new File(path);
		if(!dir.isDirectory()){
			throw new NotADirectoryException();
		}
		File[] files = dir.listFiles();
		for(File f:files){
			results.add(new FileResult(f.getName(), f.isDirectory()));
		}
		return results;
	}
	
	public static File[] getFileList(String path) throws NotADirectoryException{
		File dir = new File(path);
		if(!dir.isDirectory()){
			throw new NotADirectoryException();
		}
		File[] files = dir.listFiles();
		return files;
	}
	
	public static List<String> getFileNames(File[] list){
		List<String> fNames = new ArrayList<String>();
		for(File f:list){
			fNames.add(f.getName());
		}
		return fNames;
	}
	
	public static List<String> getFileNames(String path) throws NotADirectoryException{
		File[] list = getFileList(path);
		return getFileNames(list);
	}
	
	public static boolean renameFile(String path, String newName, boolean override) throws DoesNotExistException, AlreadyExistsException, FileLockedException{
		File original = new File(path);
		if(!original.exists()){
			throw new DoesNotExistException();
		}
		if(isFileLocked(original)){
			throw new FileLockedException();
		}
		String separator = File.separator;
		int pathEnd = path.lastIndexOf(separator);
		String newPath = newName;
		if(pathEnd > -1){
			String dir = path.substring(0, (pathEnd+1));
			newPath = dir+newName;
		}
		File newFile = new File(newPath);
		if(original.isDirectory()){
			newFile.mkdirs();
			File[] files = original.listFiles();
			boolean failed = false;
			for(File f:files){
				File newF = new File(newFile.getAbsolutePath()+File.separator+f.getName());
				newF.getParentFile().mkdirs();
				try {
					Files.move(f, newF);
				} catch (IOException e) {
					failed = true;
					continue;
				}
				
				//It was moved
				if(!f.delete()){
					f.deleteOnExit();
				}
			}
			if(!failed){
				if(!original.delete()){
					original.deleteOnExit();
				}
			}
			return true;//Update list regardless of if it succeeded
		}
		else {
			if(!override && (newFile.exists() || newFile.length() > 0)){
				throw new AlreadyExistsException();
			}
			if(isFileLocked(newFile)){
				throw new FileLockedException();
			}
			try {
		     	Files.move(original, newFile);
		     	return true;
			} catch (IOException e) {
				return false;
			}
		}
	}
	
	public static byte[] getFileContents(String path) throws DoesNotExistException, IsADirectoryException, IOException, FileLockedException{
		
		File file = new File(path);
		if(!file.exists() || file.length()<1){
			throw new DoesNotExistException();
		}
		else if(file.isDirectory()){
			throw new IsADirectoryException();
		}
		
		if(isFileLocked(file)){
			throw new FileLockedException();
		}
		byte[] read = FileUtils.readFileToByteArray(file);
		
		return read;
	}
	
	public static void saveFile(String path, byte[] data, boolean override) throws AlreadyExistsException, IOException, FileLockedException{
		File file = new File(path);
		if(!override && (file.exists() || file.length()>0)){
			throw new AlreadyExistsException();
		}
		if(!file.exists() || file.length()<1){
			file.createNewFile();
		}
		if(isFileLocked(file)){
			throw new FileLockedException();
		}
		
		Files.write(data, file);
		return;
	}
}
