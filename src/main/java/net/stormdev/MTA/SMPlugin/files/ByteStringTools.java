package net.stormdev.MTA.SMPlugin.files;

public class ByteStringTools {
	public static String getAsString(byte[] bytes){
		StringBuilder sb = new StringBuilder();
		
		for(byte b:bytes){
			int i = ((Byte)b).intValue();
			String a = i+"";
			
			if(sb.length() < 1){
				sb.append(a);
				continue;
			}
			sb.append(",");
			sb.append(a);
		}
		
		return sb.toString();
	}
	
	public static byte[] fromString(String in){
		String[] parts = in.split(",");
		byte[] out = new byte[parts.length];
		
		for(int z = 0;z<parts.length;z++){
			String s = parts[z];
			int i = 0;
			try {
				i = Integer.parseInt(s);
			} catch (NumberFormatException e) {
			}
			out[z] = (byte) i;
		}
		
		return out;
	}
}
