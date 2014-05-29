package net.stormdev.MTA.SMPlugin.commands;

import net.stormdev.MTA.SMPlugin.core.Core;
import net.stormdev.MTA.SMPlugin.menus.ServerListMenu;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

public class ServerListCommandExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(final CommandSender sender, Command cmd, String alias,
			String[] args) {
		Player player = null;
		if(sender instanceof Player){
			player = (Player) sender;
		}
		final Player pl = player;
		
		if(cmd.getName().equalsIgnoreCase("serverlist")){
			//Open the GUI
			if(player == null){
				sender.sendMessage(Core.colors.getError()+"Players only!");
				return true;
			}
			
			InventoryView view = player.getOpenInventory();
			if(view != null){ //They are already looking at an inventory
				view.close();
			}
			
			Bukkit.getScheduler().runTaskLaterAsynchronously(Core.plugin, new Runnable(){

				@Override
				public void run() {
					//Open the ManagerGUI
					new ServerListMenu(pl);
					return;
				}}, 2l);
			return true;
		}
		return false;
	}

}
