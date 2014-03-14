package net.stormdev.MTA.SMPlugin.requests;

import java.io.IOException;
import java.text.DecimalFormat;

import net.stormdev.MTA.SMPlugin.connections.Message;
import net.stormdev.MTA.SMPlugin.core.Core;
import net.stormdev.MTA.SMPlugin.core.HostConnection;
import net.stormdev.MTA.SMPlugin.core.ServerMonitor;
import net.stormdev.MTA.SMPlugin.messaging.MessageRecipient;

import org.bukkit.Bukkit;
import org.bukkit.Server;

public class UpdateRequest {
	public static void reply() throws IOException{
		if(!Core.plugin.connection.isConnected()){
			return;
		}
		HostConnection con = Core.plugin.connection;
		Server server = Bukkit.getServer();
		//Compile 'toSend' matching the needed format to tell the host about us
		
		String name = Core.plugin.getServerName();
		String desc = Core.plugin.getServerDescription();
		boolean open = Core.plugin.isServerOpen();
		int players = server.getOnlinePlayers().length;
		int maxPlayers = server.getMaxPlayers();
		double TPS = roundTo2Decimals(ServerMonitor.getTPS());
		
		int resourceScore = ServerMonitor.getResourceScore();
		
		String toSend = name+"|"+desc+"|"+TPS+"|"+players+"|"+maxPlayers+"|"+resourceScore+"|"+open; // Name|Description|TPS|PlayerCount|MaxPlayers|ResourceScore|isOpen
		
		con.sendMsg(new Message(MessageRecipient.HOST.getConnectionID(), con.getConnectionID(), "serverUpdate", toSend));
		return;
	}
	
	private static double roundTo2Decimals(double val) {
        DecimalFormat df2 = new DecimalFormat("###.##");
        return Double.valueOf(df2.format(val));
	}
	
}
