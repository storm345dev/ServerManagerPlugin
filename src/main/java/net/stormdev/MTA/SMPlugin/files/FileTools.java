package net.stormdev.MTA.SMPlugin.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.org.apache.commons.io.FileUtils;

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
	
	public static String getPathFromOnlinePath(String in, boolean allowRoot){
		in = in.replaceAll(Pattern.quote("/"), Matcher.quoteReplacement(File.separator));
		String lin = in.toLowerCase();
		if(lin.startsWith("server")){
			int pathStart = in.indexOf(File.separator)+1; //First instance of '/' + 1
			System.out.println("Path start: "+pathStart);
			if(pathStart < 0 || pathStart > in.length()){ //They typed 'server', or '', or 'server/'
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
	
	public static boolean renameFile(String path, String newName, boolean override) throws DoesNotExistException, AlreadyExistsException{
		File original = new File(path);
		if(!original.exists() || original.length() < 1){
			throw new DoesNotExistException();
		}
		String separator = File.separator;
		int pathEnd = path.lastIndexOf(separator);
		String newPath = newName;
		if(pathEnd > -1){
			String dir = path.substring(0, (pathEnd+1));
			newPath = dir+newName;
		}
		File newFile = new File(newPath);
		if(!override && (newFile.exists() || newFile.length() > 0)){
			throw new AlreadyExistsException();
		}
		boolean success = original.renameTo(newFile);
		return success;
	}
	
	public static byte[] getFileContents(String path) throws DoesNotExistException, IsADirectoryException, IOException{
		
		File file = new File(path);
		if(!file.exists() || file.length()<1){
			throw new DoesNotExistException();
		}
		else if(file.isDirectory()){
			throw new IsADirectoryException();
		}
		
		byte[] read = FileUtils.readFileToByteArray(file);
		
		return read;
	}
	
	public static void saveFile(String path, byte[] data, boolean override) throws AlreadyExistsException, IOException{
		File file = new File(path);
		if(!override && (file.exists() || file.length()>0)){
			throw new AlreadyExistsException();
		}
		if(!file.exists() || file.length()<1){
			file.createNewFile();
		}
		
		
		FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
		fos.write(data);
		fos.close();
		return;
	}
}
