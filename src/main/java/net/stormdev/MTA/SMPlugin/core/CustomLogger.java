package net.stormdev.MTA.SMPlugin.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.stormdev.MTA.SMPlugin.utils.Colors;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public class CustomLogger {
	public Boolean coloured = true;
	ConsoleCommandSender console = null;
	Logger logger = null;

	public CustomLogger(ConsoleCommandSender console, Logger logger) {
		try {
			coloured = Core.config.getBoolean("general.logger.colour");
		} catch (Exception e) {
			coloured = false;
		}
		this.console = console;
		this.logger = logger;
	}

	public void setColoured(Boolean coloured) {
		this.coloured = coloured;
	}

	public String getMsg(String raw) {
		String colour = Colors.colorise(raw);
		if (!coloured) {
			return ChatColor.stripColor(colour);
		}
		return colour;
	}

	public void log(String message, Level level) {
		print(message);
		log(level);
	}

	public void defaultLog(String msg, Level level) {
		logger.log(level, msg);
	}

	public void log(Level level) {
		logger.log(level, "");
	}

	public void error(Exception e) {
		print(Core.colors.getError() + e.getLocalizedMessage());
		e.printStackTrace();
	}

	public void error(String msg, Exception e) {
		print(Core.colors.getError() + msg);
		e.printStackTrace();
	}

	public void info(String message) {
		print(Core.colors.getInfo() + message);
	}

	public void print(String message) {
		if (coloured) {
			console.sendMessage(ChatColor.RED + "[ServerManagerPlugin] "
					+ ChatColor.RESET + getMsg(message));
		} else {
			logger.info(getMsg(message));
		}
		return;
	}
}
