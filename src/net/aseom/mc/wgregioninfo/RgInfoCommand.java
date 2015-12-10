package net.aseom.mc.wgregioninfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
			
		} else if (args[0].equalsIgnoreCase("addregion")) {
			//TODO: Check permission
			if (args.length > 2) {
				runAddRegionCmd(sender, args);
			} else {
				sender.sendMessage("Usage: /regioninfo addregion <GroupName> <RegionID>");
			}
			return true;
			
		} else if (args[0].equalsIgnoreCase("delregion")) {
			//TODO: Check permission
			if (args.length > 2) {
				runDelRegionCmd(sender, args);
			} else {
				sender.sendMessage("Usage: /regioninfo delregion <GroupName> <RegionID>");
			}
			return true;
			
		} else if (args[0].equalsIgnoreCase("setgreet")) {
			//TODO: Check permission
			if (args.length > 2) {
				try {
					runSetCmd("greet", sender, args);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				sender.sendMessage("Usage: /regioninfo setgreet <GroupName> <Text>");
			}
			return true;
			
		} else if (args[0].equalsIgnoreCase("setbye")) {
			//TODO: Check permission
			if (args.length > 2) {
				try {
					runSetCmd("bye", sender, args);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				sender.sendMessage("Usage: /regioninfo setbye <GroupName> <Text>");
			}
			return true;
			
		} else if (args[0].equalsIgnoreCase("unsetgreet")) {
			//TODO: Check permission
			if (args.length > 1) {
				try {
					runUnsetCmd("greet", sender, args);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				sender.sendMessage("Usage: /regioninfo unsetgreet <GroupName>");
			}
			return true;
			
		} else if (args[0].equalsIgnoreCase("unsetbye")) {
			//TODO: Check permission
			if (args.length > 1) {
				try {
					runUnsetCmd("bye", sender, args);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				sender.sendMessage("Usage: /regioninfo unsetbye <GroupName>");
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
	
	private void runAddRegionCmd(CommandSender sender, String[] args) {
		String groupName = args[1];
		String[] regionIDsToAdd = Arrays.copyOfRange(args, 2, args.length);
		
		if (!Config.rgRules.getKeys(false).contains(groupName)) {
			sender.sendMessage("Group \"" + groupName + "\" not found!");
			return;
		}
		
		List<String> regionIDs = Config.rgRules.getStringList(groupName + ".region-ids");
		for (String each : regionIDsToAdd) {
			if (!regionIDs.contains(each)) regionIDs.add(each);
		}
		Config.rgRules.set(groupName + ".region-ids", regionIDs);
		
		try {
			Config.rgRules.save(Config.rgRulesFile);
		} catch (IOException e) {
			sender.sendMessage("Error: Can't save config!");
			e.printStackTrace();
		}
	}
	
	private void runDelRegionCmd(CommandSender sender, String[] args) {
		String groupName = args[1];
		String[] regionIDsToDel = Arrays.copyOfRange(args, 2, args.length);
		
		if (!Config.rgRules.getKeys(false).contains(groupName)) {
			sender.sendMessage("Group \"" + groupName + "\" not found!");
			return;
		}
		
		List<String> regionIDs = Config.rgRules.getStringList(groupName + ".region-ids");
		regionIDs.removeAll(Arrays.asList(regionIDsToDel));
		Config.rgRules.set(groupName + ".region-ids", regionIDs);
		
		try {
			Config.rgRules.save(Config.rgRulesFile);
		} catch (IOException e) {
			sender.sendMessage("Error: Can't save config!");
			e.printStackTrace();
		}
	}
	
	private void runSetCmd(String greetOrBye, CommandSender sender, String[] args) throws Exception {
		if (greetOrBye != "greet" && greetOrBye != "bye")
			throw new Exception("Argument \"greetOrBye\" must be \"greet\" or \"bye\"");
		
		String groupName = args[1];
		
		// 띄어쓰기로 분리된 텍스트를 다시 합침
		String[] splitedText = Arrays.copyOfRange(args, 2, args.length);
		String combinedText = "";
		for (String each : splitedText) {
			combinedText += each + " ";
		}
		combinedText = combinedText.trim();
		//TODO: Escape " char, \ char
		
		if (!Config.rgRules.getKeys(false).contains(groupName)) {
			sender.sendMessage("Group \"" + groupName + "\" not found!");
			return;
		}
		
		Config.rgRules.set(groupName + "." + greetOrBye + "-title", combinedText);
		try {
			Config.rgRules.save(Config.rgRulesFile);
		} catch (IOException e) {
			sender.sendMessage("Error: Can't save config!");
			e.printStackTrace();
		}
	}
	
	private void runUnsetCmd(String greetOrBye, CommandSender sender, String[] args) throws Exception {
		if (greetOrBye != "greet" && greetOrBye != "bye")
			throw new Exception("Argument \"greetOrBye\" must be \"greet\" or \"bye\"");
		
		String groupName = args[1];
		
		if (args.length > 2) {
			sender.sendMessage("group name does not contain space!");
			return;
		}
		if (!Config.rgRules.getKeys(false).contains(groupName)) {
			sender.sendMessage("Group \"" + groupName + "\" not found!");
			return;
		}
		
		Config.rgRules.set(groupName + "." + greetOrBye + "-title", null);
		try {
			Config.rgRules.save(Config.rgRulesFile);
		} catch (IOException e) {
			sender.sendMessage("Error: Can't save config!");
			e.printStackTrace();
		}
	}
}
