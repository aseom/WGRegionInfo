package net.aseom.mc.wgregioninfo;

import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RgInfoCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) return false;
		
		if (args[0].equalsIgnoreCase("addgroup")) {
			//TODO: Check permission
			if (args.length > 1) {
				runAddGroupCmd(sender, args);
			} else {
				sender.sendMessage("Usage: /regioninfo addgroup <GroupName>");
			}
			return true;
			
		} else if (args[0].equalsIgnoreCase("delgroup")) {
			//TODO: Check permission
			if (args.length > 1) {
				runDelGroupCmd(sender, args);
			} else {
				sender.sendMessage("Usage: /regioninfo delgroup <GroupName>");
			}
			return true;
		}
		
		return false; // Show usage
	}
	
	private void runAddGroupCmd(CommandSender sender, String[] args) {
		String groupName = args[1];
		if (args.length > 2) {
			sender.sendMessage("group name cannot contain space!");
			return;
		}
		Config.rgRules.createSection(groupName).set("region-ids", new ArrayList<String>());
		try {
			Config.rgRules.save(Config.rgRulesFile);
		} catch (IOException e) {
			sender.sendMessage("Error: Can't save config!");
			e.printStackTrace();
		}
	}

	private void runDelGroupCmd(CommandSender sender, String[] args) {
		String groupName = args[1];
		if (args.length > 2) {
			sender.sendMessage("group name does not contain space!");
			return;
		}
		if (!Config.rgRules.getKeys(false).contains(groupName)) {
			sender.sendMessage("Group \"" + groupName + "\" not found!");
			return;
		}
		Config.rgRules.set(groupName, null);
		try {
			Config.rgRules.save(Config.rgRulesFile);
		} catch (IOException e) {
			sender.sendMessage("Error: Can't save config!");
			e.printStackTrace();
		}
	}
}
