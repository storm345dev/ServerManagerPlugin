package net.stormdev.MTA.SMPlugin.servers;

import java.io.IOException;
import java.util.regex.Pattern;

import net.stormdev.MTA.SMPlugin.connections.Message;
import net.stormdev.MTA.SMPlugin.core.Core;

public class Server {
	private String connectionId;
	private String title;
	private String description;
	private double TPS;
	private int playerCount;
	private int maxPlayers;
	private int resourceScore;
	private boolean open;
	
	public static Server create(String con, String title, String description, double TPS, int playerCount, int maxPlayers, int resourceScore, boolean open){
		return new Server(con, title, description, TPS, playerCount, maxPlayers, resourceScore, open);
	}
	
	public static Server createBlank(String con){
		return new Server(con);
	}
	
	public static Server createBasic(String con, String title, String description){
		return new Server(con, title, description);
	}
	
	private Server(String con){
		this(con, "A minecraft server", "A default minecraft server");
	}
	
	private Server(String con, String title, String description){
		this(con, title, description, 20.0, 0, 20, 100, true);
	}
	
	private Server(String con, String title, String description, double TPS, int playerCount, int maxPlayers, int resourceScore, boolean open){
		this.connectionId = con;
		this.title = title;
		this.description = description;
		this.TPS = TPS;
		this.playerCount = playerCount;
		this.maxPlayers = maxPlayers;
		this.resourceScore = resourceScore;
		this.open = open;
	}
	
	public String getTitle(){
		return title;
	}
	
	public void setTitle(String title){ //Semi-useless as next server-update ping; it'll go back to as set by the server
		this.title = title;
	}
	
	public String getDescription(){
		return description;
	}
	
	public void setDescription(String description){ //Semi-useless as next server-update ping; it'll go back to as set by the server
		this.description = description;
	}
	
	public double getTPS(){
		return TPS;
	}
	
	public int getPlayerCount(){
		return playerCount;
	}
	
	public int getMaxPlayers(){
		return maxPlayers;
	}
	
	public int getResourceScore(){
		return resourceScore;
	}
	
	public boolean isOpen(){
		return open;
	}
	
	public void setOpen(boolean open) throws IOException{
		if(Core.plugin.connection.isConnected()){
			Core.plugin.connection.sendMsg(new Message(connectionId, Core.plugin.connection.getConnectionID(), "setOpen", open+""));
		}
	}
	
	public String getConnectionId(){
		return connectionId;
	}
	
	public String getRaw(){
		return connectionId+"|"+title+"|"+description+"|"+TPS+"|"+playerCount+"|"+maxPlayers+"|"+resourceScore+"|"+open;
	}
	
	public static Server fromRawString(String in) throws NumberFormatException,ArrayIndexOutOfBoundsException,Exception {
		String[] parts = in.split(Pattern.quote("|"));
		String conId = parts[0];
		String title = parts[1];
		String desc = parts[2];
		String tpsRaw = parts[3];
		String playerCountRaw = parts[4];
		String maxPlayersRaw = parts[5];
		String rScoreRaw = parts[6];
		String openRaw = parts[7];
		
		double tps = Double.parseDouble(tpsRaw);
		int playerCount = Integer.parseInt(playerCountRaw);
		int maxPlayers = Integer.parseInt(maxPlayersRaw);
		int rScore = Integer.parseInt(rScoreRaw);
		boolean open = Boolean.parseBoolean(openRaw);
		
		return create(conId, title, desc, tps, playerCount, maxPlayers, rScore, open);
	}
	
	public synchronized void handleUpdatePacket(String in) throws NumberFormatException,ArrayIndexOutOfBoundsException,Exception {
		String[] parts = in.split(Pattern.quote("|"));
		String title = parts[0];
		String desc = parts[1];
		String tpsRaw = parts[2];
		String playerCountRaw = parts[3];
		String maxPlayersRaw = parts[4];
		String rScoreRaw = parts[5];
		String openRaw = parts[6];
		
		double tps = Double.parseDouble(tpsRaw);
		int playerCount = Integer.parseInt(playerCountRaw);
		int maxPlayers = Integer.parseInt(maxPlayersRaw);
		int rScore = Integer.parseInt(rScoreRaw);
		boolean open = Boolean.parseBoolean(openRaw);
		
		this.title = title;
		this.description = desc;
		this.TPS = tps;
		this.playerCount = playerCount;
		this.maxPlayers = maxPlayers;
		this.resourceScore = rScore;
		this.open = open;
	}
}
