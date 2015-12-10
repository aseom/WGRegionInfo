package net.aseom.mc.wgregioninfo;

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
		if (args.length > 2) {
			sender.sendMessage("group name not allow using space!");
			return;
		}
		String groupName = args[1];
		//TODO: Add group
	}

	private void runDelGroupCmd(CommandSender sender, String[] args) {
		String groupName = args[1];
		//TODO: Delete group
		//TODO: If (args.length > 2 || group not found) Error
	}
}
