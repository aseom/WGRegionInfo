package net.aseom.mc.wgregioninfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

//TODO: "/regioninfo help" command
//TODO: "Group|Region ~ not exist. Nothing to remove." Message

public class RgInfoCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) return false;
		try {
			boolean firstArgVaild = cmdHandling(sender, args);
			if (!firstArgVaild) return false; // Show "/regioninfo" usage
		} catch (IOException e) {
			sender.sendMessage("Error: Can't save config!");
		}
		return true;
	}
	
	/**
	 * @return Boolean First argument vaild or unvaild
	 * @throws IOException Config save fail
	 */
	public boolean cmdHandling(CommandSender sender, String[] args) throws IOException {
		if (args[0].equalsIgnoreCase("newgroup")) {
			//TODO: Check permission
			if (args.length > 1) {
				runNewGroupCmd(sender, args);
			} else {
				sender.sendMessage("Usage: /regioninfo newgroup <GroupName>");
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
		} else if (args[0].equalsIgnoreCase("grouptitle")) {
			//TODO: Check permission
			if (args.length < 3 || !args[2].equalsIgnoreCase("greet") && !args[2].equalsIgnoreCase("bye"))
				sender.sendMessage("Usage: /regioninfo grouptitle <GroupName> <greet|bye> <Text>");
			else
				runGroupTitleCmd(args[2].toLowerCase(), sender, args);
			return true;
		} else if (args[0].equalsIgnoreCase("title")) {
			//TODO: Check permission
			if (args.length < 3 || !args[2].equalsIgnoreCase("greet") && !args[2].equalsIgnoreCase("bye"))
				sender.sendMessage("Usage: /regioninfo title <RegionID> <greet|bye> <Text>");
			else
				runTitleCmd(args[2].toLowerCase(), sender, args);
			return true;
		} else {
			return false;
		}
	}

	private void runNewGroupCmd(CommandSender sender, String[] args) throws IOException {
		String groupName = args[1].toLowerCase();
		
		if (args.length > 2) {
			sender.sendMessage("group name cannot contain space!");
			return;
		}
		
		Config.rgRules.getConfigurationSection("groups").createSection(groupName).set("region-ids", new ArrayList<String>());
		Config.rgRules.save(Config.rgRulesFile);
	}

	private void runDelGroupCmd(CommandSender sender, String[] args) throws IOException {
		String groupName = args[1].toLowerCase();
		
		if (args.length > 2) {
			sender.sendMessage("group name does not contain space!");
			return;
		}
		if (!Config.rgRules.getConfigurationSection("groups").getKeys(false).contains(groupName)) {
			sender.sendMessage("Group \"" + groupName + "\" not found!");
			return;
		}
		
		Config.rgRules.set("groups." + groupName, null);
		Config.rgRules.save(Config.rgRulesFile);
	}
	
	private void runAddRegionCmd(CommandSender sender, String[] args) throws IOException {
		String groupName = args[1].toLowerCase();
		String[] regionIDsToAdd = Arrays.copyOfRange(args, 2, args.length);
		
		if (!Config.rgRules.getConfigurationSection("groups").getKeys(false).contains(groupName)) {
			sender.sendMessage("Group \"" + groupName + "\" not found!");
			return;
		}
		
		List<String> regionIDs = Config.rgRules.getStringList("groups." + groupName + ".region-ids");
		for (String each : regionIDsToAdd) {
			// 그룹에 추가하려는 each region이 region specific rule을 갖고있으면 remove
			ConfigurationSection rgSpecificCfg = Config.rgRules.getConfigurationSection("regions." + each);
			if (rgSpecificCfg != null) {
				Config.rgRules.set("regions." + each, null);
				sender.sendMessage("\"" + each + "\": Region specific rule removed!");
			}
			// 중복확인 and add each region
			if (!regionIDs.contains(each)) regionIDs.add(each);
		}
		Config.rgRules.set("groups." + groupName + ".region-ids", regionIDs);
		Config.rgRules.save(Config.rgRulesFile);
	}
	
	private void runDelRegionCmd(CommandSender sender, String[] args) throws IOException {
		String groupName = args[1].toLowerCase();
		String[] regionIDsToDel = Arrays.copyOfRange(args, 2, args.length);
		
		if (!Config.rgRules.getConfigurationSection("groups").getKeys(false).contains(groupName)) {
			sender.sendMessage("Group \"" + groupName + "\" not found!");
			return;
		}
		
		List<String> regionIDs = Config.rgRules.getStringList("groups." + groupName + ".region-ids");
		regionIDs.removeAll(Arrays.asList(regionIDsToDel));
		Config.rgRules.set("groups." + groupName + ".region-ids", regionIDs);
		Config.rgRules.save(Config.rgRulesFile);
	}
	
	private void runGroupTitleCmd(String greetOrBye, CommandSender sender, String[] args) throws IOException {
		String groupName = args[1].toLowerCase();
		String text = combineStrArr(Arrays.copyOfRange(args, 3, args.length));
		
		if (!Config.rgRules.getConfigurationSection("groups").getKeys(false).contains(groupName)) {
			sender.sendMessage("Group \"" + groupName + "\" not found!");
			return;
		}
		
		if (text != "") {
			// Add
			Config.rgRules.set("groups." + groupName + "." + greetOrBye + "-title", text);
		} else {
			// Remove
			Config.rgRules.set("groups." + groupName + "." + greetOrBye + "-title", null);
		}
		Config.rgRules.save(Config.rgRulesFile);
	}
	
	private void runTitleCmd(String greetOrBye, CommandSender sender, String[] args) throws IOException {
		String regionID = args[1].toLowerCase();
		String text = combineStrArr(Arrays.copyOfRange(args, 3, args.length));
		
		if (text != "") {
			// Add
			// The region exists in group rule, move to region rule
			String group = Config.getGroup(regionID);
			if (group != null) {
				Config.moveGroupCfgToRg(regionID, group);
				sender.sendMessage("From now, region \"" + regionID + "\" not in group \"" + group + "\"");
			}
			Config.rgRules.set("regions." + regionID + "." + greetOrBye + "-title", text);
		} else {
			// Remove
			Config.rgRules.set("regions." + regionID + "." + greetOrBye + "-title", null);
			Config.cleanupRgConfSection(regionID);
		}
		Config.rgRules.save(Config.rgRulesFile);
	}

	/**
	 * 띄어쓰기 포함 텍스트가 배열로 쪼개진 것 다시 합침
	 * @return String Combined text
	 */
	public String combineStrArr(String[] splitedText) {
		String combinedText = "";
		for (String each : splitedText) {
			combinedText += each + " ";
		}
		combinedText = combinedText.trim();
		//TODO: Escape " char, \ char
		return combinedText;
	}
}
