package net.aseom.mc.wgregioninfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

//TODO: "/regioninfo help" command
//TODO: "Group|Region ~ not exist. Nothing to remove." Message
//TODO: regions와 groups에 title conf 중복되는 경우 처리

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
		String groupName = args[1];
		
		if (args.length > 2) {
			sender.sendMessage("group name cannot contain space!");
			return;
		}
		
		Config.rgRules.getConfigurationSection("groups").createSection(groupName).set("region-ids", new ArrayList<String>());
		Config.rgRules.save(Config.rgRulesFile);
	}

	private void runDelGroupCmd(CommandSender sender, String[] args) throws IOException {
		String groupName = args[1];
		
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
		String groupName = args[1];
		String[] regionIDsToAdd = Arrays.copyOfRange(args, 2, args.length);
		
		if (!Config.rgRules.getConfigurationSection("groups").getKeys(false).contains(groupName)) {
			sender.sendMessage("Group \"" + groupName + "\" not found!");
			return;
		}
		
		List<String> regionIDs = Config.rgRules.getStringList("groups." + groupName + ".region-ids");
		for (String each : regionIDsToAdd) {
			if (!regionIDs.contains(each)) regionIDs.add(each);
		}
		Config.rgRules.set("groups." + groupName + ".region-ids", regionIDs);
		Config.rgRules.save(Config.rgRulesFile);
	}
	
	private void runDelRegionCmd(CommandSender sender, String[] args) throws IOException {
		String groupName = args[1];
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
		String groupName = args[1];
		String[] splitedText = Arrays.copyOfRange(args, 3, args.length);
		
		if (!Config.rgRules.getConfigurationSection("groups").getKeys(false).contains(groupName)) {
			sender.sendMessage("Group \"" + groupName + "\" not found!");
			return;
		}
		
		if (splitedText.length > 0) {
			// Add
			String combinedText = combineArgsToString(splitedText);
			Config.rgRules.set("groups." + groupName + "." + greetOrBye + "-title", combinedText);
		} else {
			// Remove
			Config.rgRules.set("groups." + groupName + "." + greetOrBye + "-title", null);
		}
		Config.rgRules.save(Config.rgRulesFile);
	}
	
	private void runTitleCmd(String greetOrBye, CommandSender sender, String[] args) throws IOException {
		String regionID = args[1];
		String[] splitedText = Arrays.copyOfRange(args, 3, args.length);
		
		if (splitedText.length > 0) {
			// Add
			String combinedText = combineArgsToString(splitedText);
			Config.rgRules.set("regions." + regionID + "." + greetOrBye + "-title", combinedText);
		} else {
			// Remove
			Config.rgRules.set("regions." + regionID + "." + greetOrBye + "-title", null);
			// 아무 설정도 안남으면 해당 region 섹션 삭제
			if (Config.rgRules.getConfigurationSection("regions." + regionID).getKeys(false).size() == 0)
				Config.rgRules.set("regions." + regionID, null);
		}
		Config.rgRules.save(Config.rgRulesFile);
	}

	/**
	 * 띄어쓰기 포함 텍스트가 배열로 쪼개진 것 다시 합침
	 * @return String Combined text
	 */
	public String combineArgsToString(String[] splitedText) {
		String combinedText = "";
		for (String each : splitedText) {
			combinedText += each + " ";
		}
		combinedText = combinedText.trim();
		//TODO: Escape " char, \ char
		return combinedText;
	}
}
