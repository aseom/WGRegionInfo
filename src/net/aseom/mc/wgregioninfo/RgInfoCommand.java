package net.aseom.mc.wgregioninfo;

import java.io.IOException;
import java.text.MessageFormat;
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
	private final Config config;
	
	public RgInfoCommand() {
		this.config = WGRegionInfo.plugin.getConfigClass();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) return false;
		try {
			boolean firstArgVaild = cmdHandling(sender, args);
			if (!firstArgVaild) return false; // Show "/regioninfo" usage
		} catch (IOException e) {
			sender.sendMessage(Lang.ERR_CANT_SAVE_CONFIG.get());
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * @return First argument vaild or unvaild
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
			sender.sendMessage(Lang.GROUPNAME_CANT_SPACE.get());
			return;
		}
		
		config.getRegionRulesConf().getConfigurationSection("groups").createSection(groupName).set("region-ids", new ArrayList<String>());
		config.savaRegionRulesConf();
	}

	private void runDelGroupCmd(CommandSender sender, String[] args) throws IOException {
		String groupName = args[1].toLowerCase();
		
		if (args.length > 2) {
			sender.sendMessage(Lang.GROUPNAME_CANT_SPACE.get());
			return;
		}
		if (!config.getRegionRulesConf().getConfigurationSection("groups").getKeys(false).contains(groupName)) {
			sender.sendMessage(MessageFormat.format(Lang.GROUP_NOT_FOUND.get(), groupName));
			return;
		}
		
		config.getRegionRulesConf().set("groups." + groupName, null);
		config.savaRegionRulesConf();
	}
	
	private void runAddRegionCmd(CommandSender sender, String[] args) throws IOException {
		String groupName = args[1].toLowerCase();
		String[] regionIDsToAdd = Arrays.copyOfRange(args, 2, args.length);
		
		if (!config.getRegionRulesConf().getConfigurationSection("groups").getKeys(false).contains(groupName)) {
			sender.sendMessage(MessageFormat.format(Lang.GROUP_NOT_FOUND.get(), groupName));
			return;
		}
		
		List<String> regionIDs = config.getRegionRulesConf().getStringList("groups." + groupName + ".region-ids");
		for (String eRgID : regionIDsToAdd) {
			// 그룹에 추가하려는 each region이 region specific rule을 갖고있으면 remove
			ConfigurationSection rgSpecificCfg = config.getRegionRulesConf().getConfigurationSection("regions." + eRgID);
			if (rgSpecificCfg != null) {
				config.getRegionRulesConf().set("regions." + eRgID, null);
				sender.sendMessage(MessageFormat.format(Lang.RG_SPECI_RULE_REMOVED.get(), eRgID));
			}
			// 중복확인 and add each region
			if (!regionIDs.contains(eRgID)) regionIDs.add(eRgID);
		}
		config.getRegionRulesConf().set("groups." + groupName + ".region-ids", regionIDs);
		config.savaRegionRulesConf();
	}
	
	private void runDelRegionCmd(CommandSender sender, String[] args) throws IOException {
		String groupName = args[1].toLowerCase();
		String[] regionIDsToDel = Arrays.copyOfRange(args, 2, args.length);
		
		if (!config.getRegionRulesConf().getConfigurationSection("groups").getKeys(false).contains(groupName)) {
			sender.sendMessage(MessageFormat.format(Lang.GROUP_NOT_FOUND.get(), groupName));
			return;
		}
		
		List<String> regionIDs = config.getRegionRulesConf().getStringList("groups." + groupName + ".region-ids");
		regionIDs.removeAll(Arrays.asList(regionIDsToDel));
		//TODO: 삭제할 region이 존재 안하는 경우 메시지
		config.getRegionRulesConf().set("groups." + groupName + ".region-ids", regionIDs);
		config.savaRegionRulesConf();
	}
	
	private void runGroupTitleCmd(String greetOrBye, CommandSender sender, String[] args) throws IOException {
		String groupName = args[1].toLowerCase();
		String text = combineStrArr(Arrays.copyOfRange(args, 3, args.length));
		
		if (!config.getRegionRulesConf().getConfigurationSection("groups").getKeys(false).contains(groupName)) {
			sender.sendMessage(MessageFormat.format(Lang.GROUP_NOT_FOUND.get(), groupName));
			return;
		}
		
		if (text != "") {
			// Add
			config.getRegionRulesConf().set("groups." + groupName + "." + greetOrBye + "-title", text);
		} else {
			// Remove
			config.getRegionRulesConf().set("groups." + groupName + "." + greetOrBye + "-title", null);
		}
		config.savaRegionRulesConf();
	}
	
	private void runTitleCmd(String greetOrBye, CommandSender sender, String[] args) throws IOException {
		String regionID = args[1].toLowerCase();
		String text = combineStrArr(Arrays.copyOfRange(args, 3, args.length));
		
		if (text != "") {
			// Add
			// The region exists in group rule, move to region rule
			String group = config.getGroup(regionID);
			if (group != null) {
				config.moveGroupRuleToRg(regionID, group);
				sender.sendMessage(MessageFormat.format(Lang.RG_SPECI_RULE_MOVED.get(), regionID, group));
			}
			config.getRegionRulesConf().set("regions." + regionID + "." + greetOrBye + "-title", text);
		} else {
			// Remove
			config.getRegionRulesConf().set("regions." + regionID + "." + greetOrBye + "-title", null);
			config.cleanupRgSpeciRule(regionID);
		}
		config.savaRegionRulesConf();
	}

	/**
	 * 띄어쓰기 포함 텍스트가 배열로 쪼개진 것 다시 합침
	 * @return Combined text
	 */
	public String combineStrArr(String[] splitedTexts) {
		String combinedText = "";
		for (String eSplText : splitedTexts) {
			combinedText += eSplText + " ";
		}
		combinedText = combinedText.trim();
		//TODO: Escape " char, \ char
		return combinedText;
	}
}
