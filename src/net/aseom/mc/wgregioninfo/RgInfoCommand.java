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

import net.aseom.mc.wgregioninfo.config.RegionRule;

//TODO: "/regioninfo help" command

public class RgInfoCommand implements CommandExecutor {
	private final RegionRule regionRule;
	private final String[] availableRules = {
		"greet-title", "greet-subtitle", "bye-title", "bye-subtitle"
	};
	
	public RgInfoCommand() {
		this.regionRule = WGRegionInfo.plugin.getRegionRuleClass();
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
				sender.sendMessage(Lang.USAGE_NEWGROUP.get());
			}
			return true;
		} else if (args[0].equalsIgnoreCase("delgroup")) {
			//TODO: Check permission
			if (args.length > 1) {
				runDelGroupCmd(sender, args);
			} else {
				sender.sendMessage(Lang.USAGE_DELGROUP.get());
			}
			return true;
		} else if (args[0].equalsIgnoreCase("addregion")) {
			//TODO: Check permission
			if (args.length > 2) {
				runAddRegionCmd(sender, args);
			} else {
				sender.sendMessage(Lang.USAGE_ADDREGION.get());
			}
			return true;
		} else if (args[0].equalsIgnoreCase("delregion")) {
			//TODO: Check permission
			if (args.length > 2) {
				runDelRegionCmd(sender, args);
			} else {
				sender.sendMessage(Lang.USAGE_DELREGION.get());
			}
			return true;
		} else if (args[0].equalsIgnoreCase("grouprule")) {
			//TODO: Check permission
			if (args.length < 3 || !Arrays.asList(availableRules).contains(args[2])) {
				sender.sendMessage(Lang.USAGE_GROUPRULE.get());
				sender.sendMessage(Lang.AVAILABLE_RULES.get());
			} else {
				runGroupRuleCmd(args[2].toLowerCase(), sender, args);
			}
			return true;
		} else if (args[0].equalsIgnoreCase("rule")) {
			//TODO: Check permission
			if (args.length < 3 || !Arrays.asList(availableRules).contains(args[2])) {
				sender.sendMessage(Lang.USAGE_RULE.get());
				sender.sendMessage(Lang.AVAILABLE_RULES.get());
			} else {
				runRuleCmd(args[2].toLowerCase(), sender, args);
			}
			return true;
		} else if (args[0].equalsIgnoreCase("reload")) {
			try {
				WGRegionInfo.plugin.reloadPlugin();
				sender.sendMessage(Lang.PLUGIN_RELOADED.get());
			} catch (Exception e) {
				sender.sendMessage(MessageFormat.format(Lang.ERROR_WHILE_RELOAD.get(), e.toString()));
				e.printStackTrace();
			}
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
		
		regionRule.getRegionRulesConf().getConfigurationSection("groups").createSection(groupName).set("region-ids", new ArrayList<String>());
		regionRule.savaRegionRulesConf();
		sender.sendMessage(MessageFormat.format(Lang.SUCCESS_NEWGROUP.get(), groupName));
	}

	private void runDelGroupCmd(CommandSender sender, String[] args) throws IOException {
		String groupName = args[1].toLowerCase();
		
		if (args.length > 2) {
			sender.sendMessage(Lang.GROUPNAME_CANT_SPACE.get());
			return;
		}
		if (!regionRule.getRegionRulesConf().getConfigurationSection("groups").getKeys(false).contains(groupName)) {
			sender.sendMessage(MessageFormat.format(Lang.GROUP_NOT_FOUND.get(), groupName));
			return;
		}
		
		regionRule.getRegionRulesConf().set("groups." + groupName, null);
		regionRule.savaRegionRulesConf();
		sender.sendMessage(MessageFormat.format(Lang.SUCCESS_DELGROUP.get(), groupName));
	}
	
	private void runAddRegionCmd(CommandSender sender, String[] args) throws IOException {
		String groupName = args[1].toLowerCase(); // 기존에 있던것들
		String[] regionIDsToAdd = Arrays.copyOfRange(args, 2, args.length); // 입력받은 것들
		List<String> addedRegionIDs = new ArrayList<String>(); // 실제 추가된 것들
		
		if (!regionRule.getRegionRulesConf().getConfigurationSection("groups").getKeys(false).contains(groupName)) {
			sender.sendMessage(MessageFormat.format(Lang.GROUP_NOT_FOUND.get(), groupName));
			return;
		}
		
		List<String> regionIDs = regionRule.getRegionRulesConf().getStringList("groups." + groupName + ".region-ids");
		for (String eRgID : regionIDsToAdd) {
			// 그룹에 추가하려는 each region이 region specific rule을 갖고있으면 remove
			ConfigurationSection rgSpecificCfg = regionRule.getRegionRulesConf().getConfigurationSection("regions." + eRgID);
			if (rgSpecificCfg != null) {
				regionRule.getRegionRulesConf().set("regions." + eRgID, null);
				sender.sendMessage(MessageFormat.format(Lang.RG_SPECI_RULE_REMOVED.get(), eRgID));
			}
			// 중복확인 and add each region
			if (!regionIDs.contains(eRgID)) {
				regionIDs.add(eRgID);
				addedRegionIDs.add(eRgID);
			}
		}
		regionRule.getRegionRulesConf().set("groups." + groupName + ".region-ids", regionIDs);
		regionRule.savaRegionRulesConf();
		if (addedRegionIDs.size() == 0) sender.sendMessage(Lang.NOTHING_TO_ADD.get());
		else if (addedRegionIDs.size() < 2) sender.sendMessage(MessageFormat.format(Lang.SUCCESS_ADDREGION.get(), addedRegionIDs.get(0), groupName));
		else sender.sendMessage(MessageFormat.format(Lang.SUCCESS_ADDREGION_MULTI.get(), addedRegionIDs.get(0), (addedRegionIDs.size() - 1), groupName));
	}
	
	private void runDelRegionCmd(CommandSender sender, String[] args) throws IOException {
		String groupName = args[1].toLowerCase(); // 기존에 있던것들
		String[] regionIDsToDel = Arrays.copyOfRange(args, 2, args.length); // 입력받은 것들
		List<String> deletedRegionIDs = new ArrayList<String>(); // 실제 삭제된 것들
		
		if (!regionRule.getRegionRulesConf().getConfigurationSection("groups").getKeys(false).contains(groupName)) {
			sender.sendMessage(MessageFormat.format(Lang.GROUP_NOT_FOUND.get(), groupName));
			return;
		}
		
		List<String> regionIDs = regionRule.getRegionRulesConf().getStringList("groups." + groupName + ".region-ids");
		for (String eRgID : regionIDsToDel) {
			// 존재확인 and delete each region
			if (regionIDs.contains(eRgID)) {
				regionIDs.remove(eRgID);
				deletedRegionIDs.add(eRgID);
			}
		}
		regionRule.getRegionRulesConf().set("groups." + groupName + ".region-ids", regionIDs);
		regionRule.savaRegionRulesConf();
		if (deletedRegionIDs.size() == 0) sender.sendMessage(Lang.NOTHING_TO_DEL.get());
		else if (deletedRegionIDs.size() < 2) sender.sendMessage(MessageFormat.format(Lang.SUCCESS_DELREGION.get(), deletedRegionIDs.get(0), groupName));
		else sender.sendMessage(MessageFormat.format(Lang.SUCCESS_DELREGION_MULTI.get(), deletedRegionIDs.get(0), (deletedRegionIDs.size() - 1), groupName));
	}
	
	private void runGroupRuleCmd(String ruleID, CommandSender sender, String[] args) throws IOException {
		String groupName = args[1].toLowerCase();
		String value = combineStrArr(Arrays.copyOfRange(args, 3, args.length));
		
		if (!regionRule.getRegionRulesConf().getConfigurationSection("groups").getKeys(false).contains(groupName)) {
			sender.sendMessage(MessageFormat.format(Lang.GROUP_NOT_FOUND.get(), groupName));
			return;
		}
		
		if (!value.equals("")) {
			// Add
			regionRule.getRegionRulesConf().set("groups." + groupName + "." + ruleID, value);
			regionRule.savaRegionRulesConf();
			sender.sendMessage(Lang.SUCCESS_GROUPRULE.get());
		} else {
			// Remove
			regionRule.getRegionRulesConf().set("groups." + groupName + "." + ruleID, null);
			regionRule.savaRegionRulesConf();
			sender.sendMessage(Lang.SUCCESS_GROUPRULE_REM.get());
		}
	}
	
	private void runRuleCmd(String ruleID, CommandSender sender, String[] args) throws IOException {
		String regionID = args[1].toLowerCase();
		String value = combineStrArr(Arrays.copyOfRange(args, 3, args.length));
		
		if (!value.equals("")) {
			// Add
			// The region exists in group rule, move to region rule
			String group = regionRule.getGroup(regionID);
			if (group != null) {
				regionRule.moveGroupRuleToRg(regionID, group);
				sender.sendMessage(MessageFormat.format(Lang.RG_SPECI_RULE_MOVED.get(), regionID, group));
			}
			regionRule.getRegionRulesConf().set("regions." + regionID + "." + ruleID, value);
			regionRule.savaRegionRulesConf();
			sender.sendMessage(Lang.SUCCESS_RULE.get());
		} else {
			// Remove
			regionRule.getRegionRulesConf().set("regions." + regionID + "." + ruleID, null);
			regionRule.cleanupRgSpeciRule(regionID);
			regionRule.savaRegionRulesConf();
			sender.sendMessage(Lang.SUCCESS_RULE_REM.get());
		}
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
