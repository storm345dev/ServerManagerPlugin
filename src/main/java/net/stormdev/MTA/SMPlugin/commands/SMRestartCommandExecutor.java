package net.stormdev.MTA.SMPlugin.commands;

import net.stormdev.MTA.SMPlugin.core.AntiCrash;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SMRestartCommandExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2,
			String[] arg3) {
		AntiCrash.getInstance().restart();
		return true;
	}

}
