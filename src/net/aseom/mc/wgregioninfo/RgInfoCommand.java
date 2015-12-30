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
import org.bukkit.entity.Player;
import net.aseom.mc.wgregioninfo.config.PluginConfig;
import net.aseom.mc.wgregioninfo.config.RegionConfig;

public class RgInfoCommand implements CommandExecutor {
	private RegionConfig regionConfig;

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		this.regionConfig = WGRegionInfo.plugin.getRegionConfigClass();
		
		if (command.getName().equalsIgnoreCase("regionhud")) {
			if (sender instanceof Player) {
				try {
					toggleRegionHUD((Player) sender);
				} catch (IOException e) {
					sender.sendMessage(Lang.ERR_CANT_SAVE_CONFIG.get());
					e.printStackTrace();
				}
			} else {
				sender.sendMessage(Lang.CANT_USE_CONSOLE.get());
			}
		} else if (command.getName().equalsIgnoreCase("regioninfo")) {
			if (args.length == 0) return false;
			try {
				if (Arrays.asList(regionConfig.getAvailableRgConfs()).contains(args[0])) {
					runRgConfCmd(sender, args);
				} else {
					regionInfoCmdHandling(command, sender, args);
				}
			} catch (IOException e) {
				sender.sendMessage(Lang.ERR_CANT_SAVE_CONFIG.get());
				e.printStackTrace();
			}
		}
		return true;
	}
	
	public void toggleRegionHUD(Player player) throws IOException {
		if (!player.hasPermission("rginfo.togglehud")) {
			player.sendMessage(Lang.NO_PERMISSION.get());
			return;
		}
		
		PluginConfig pluginConfig = WGRegionInfo.plugin.getPluginConfigClass();
		List<String> hudOffUsers = pluginConfig.getPluginConfig().getStringList("hud-off-users");
		
		if (hudOffUsers.contains(player.getName())) {
			// On
			hudOffUsers.remove(player.getName());
			pluginConfig.getPluginConfig().set("hud-off-users", hudOffUsers);
			pluginConfig.savaPluginConfig();
			player.sendMessage("Region HUD On!");
			WGRegionInfo.plugin.reloadHudBoard(player);
		} else {
			// Off
			hudOffUsers.add(player.getName());
			pluginConfig.getPluginConfig().set("hud-off-users", hudOffUsers);
			pluginConfig.savaPluginConfig();
			player.sendMessage("Region HUD Off!");
			WGRegionInfo.plugin.reloadHudBoard(player);
		}
	}
	
	/* Begin region config commands handling */
	
	private void runRgConfCmd(CommandSender sender, String[] args) throws IOException {
		if (!sender.hasPermission("rginfo.modify")) {
			sender.sendMessage(Lang.NO_PERMISSION.get());
			return;
		}
		if (args.length < 2) {
			sender.sendMessage(MessageFormat.format(Lang.USAGE_REGIONCONF.get(), args[0]));
			return;
		}
		
		String rgOrGrpName = args[1].toLowerCase();
		String confId = args[0];
		String value = combineStrArr(Arrays.copyOfRange(args, 2, args.length));
		
		if (rgOrGrpName.startsWith("g:")) {
			// Group specific config
			String grpName = rgOrGrpName.substring(2);
			setGrpSpeciConf(grpName, confId, value, sender);
		} else {
			// Region specific config
			String rgId = rgOrGrpName;
			setRgSpeciConf(rgId, confId, value, sender);
		}
	}
	
	private void setGrpSpeciConf(String grpName, String confId, String value, CommandSender sender) throws IOException {
		if (!regionConfig.getRgYmlConf().getConfigurationSection("groups").getKeys(false).contains(grpName)) {
			sender.sendMessage(MessageFormat.format(Lang.GROUP_NOT_FOUND.get(), grpName));
			return;
		}
		
		if (!value.equals("")) {
			// Add
			regionConfig.getRgYmlConf().set("groups." + grpName + "." + confId, value);
			regionConfig.savaRgConf();
			sender.sendMessage(Lang.SUCCESS_GROUPCONF.get());
		} else {
			// Remove
			regionConfig.getRgYmlConf().set("groups." + grpName + "." + confId, null);
			regionConfig.savaRgConf();
			sender.sendMessage(Lang.SUCCESS_GROUPCONF_REM.get());
		}
	}
	
	private void setRgSpeciConf(String rgId, String confId, String value, CommandSender sender) throws IOException {
		if (!value.equals("")) {
			// Add
			// The region exists in group rule, move to region rule
			String group = regionConfig.getGroup(rgId);
			if (group != null) {
				regionConfig.moveGroupConfToRg(rgId, group);
				sender.sendMessage(MessageFormat.format(Lang.RG_SPECI_CONF_MOVED.get(), rgId, group));
			}
			regionConfig.getRgYmlConf().set("regions." + rgId + "." + confId, value);
			regionConfig.savaRgConf();
			sender.sendMessage(Lang.SUCCESS_REGIONCONF.get());
		} else {
			// Remove
			regionConfig.getRgYmlConf().set("regions." + rgId + "." + confId, null);
			regionConfig.cleanupRgSpeciConf(rgId);
			regionConfig.savaRgConf();
			sender.sendMessage(Lang.SUCCESS_REGIONCONF_REM.get());
		}
	}

	/* End region config commands handling */

	/**
	 * Command handling (except '/rghud' & region config commands)
	 * @throws IOException Config save fail
	 */
	private void regionInfoCmdHandling(Command command, CommandSender sender, String[] args) throws IOException {
		if (args[0].equalsIgnoreCase("newgroup")) {
			if (!sender.hasPermission("rginfo.modify")) {
				sender.sendMessage(Lang.NO_PERMISSION.get());
				return;
			}
			if (args.length > 1) {
				runNewGroupCmd(sender, args);
			} else {
				sender.sendMessage(Lang.USAGE_NEWGROUP.get());
			}
		} else if (args[0].equalsIgnoreCase("delgroup")) {
			if (!sender.hasPermission("rginfo.modify")) {
				sender.sendMessage(Lang.NO_PERMISSION.get());
				return;
			}
			if (args.length > 1) {
				runDelGroupCmd(sender, args);
			} else {
				sender.sendMessage(Lang.USAGE_DELGROUP.get());
			}
		} else if (args[0].equalsIgnoreCase("addregion")) {
			if (!sender.hasPermission("rginfo.modify")) {
				sender.sendMessage(Lang.NO_PERMISSION.get());
				return;
			}
			if (args.length > 2) {
				runAddRegionCmd(sender, args);
			} else {
				sender.sendMessage(Lang.USAGE_ADDREGION.get());
			}
		} else if (args[0].equalsIgnoreCase("delregion")) {
			if (!sender.hasPermission("rginfo.modify")) {
				sender.sendMessage(Lang.NO_PERMISSION.get());
				return;
			}
			if (args.length > 2) {
				runDelRegionCmd(sender, args);
			} else {
				sender.sendMessage(Lang.USAGE_DELREGION.get());
			}
		} else if (args[0].equalsIgnoreCase("reload")) {
			if (!sender.hasPermission("rginfo.reload")) {
				sender.sendMessage(Lang.NO_PERMISSION.get());
				return;
			}
			try {
				WGRegionInfo.plugin.reloadPlugin();
				sender.sendMessage(Lang.PLUGIN_RELOADED.get());
			} catch (Exception e) {
				sender.sendMessage(MessageFormat.format(Lang.ERROR_WHILE_RELOAD.get(), e.toString()));
				e.printStackTrace();
			}
		} else {
			sender.sendMessage(command.getUsage());
		}
	}

	private void runNewGroupCmd(CommandSender sender, String[] args) throws IOException {
		String groupName = args[1].toLowerCase();
		
		if (args.length > 2) {
			sender.sendMessage(Lang.GROUPNAME_CANT_SPACE.get());
			return;
		}
		
		regionConfig.getRgYmlConf().getConfigurationSection("groups").createSection(groupName).set("region-ids", new ArrayList<String>());
		regionConfig.savaRgConf();
		sender.sendMessage(MessageFormat.format(Lang.SUCCESS_NEWGROUP.get(), groupName));
	}

	private void runDelGroupCmd(CommandSender sender, String[] args) throws IOException {
		String groupName = args[1].toLowerCase();
		
		if (args.length > 2) {
			sender.sendMessage(Lang.GROUPNAME_CANT_SPACE.get());
			return;
		}
		if (!regionConfig.getRgYmlConf().getConfigurationSection("groups").getKeys(false).contains(groupName)) {
			sender.sendMessage(MessageFormat.format(Lang.GROUP_NOT_FOUND.get(), groupName));
			return;
		}
		
		regionConfig.getRgYmlConf().set("groups." + groupName, null);
		regionConfig.savaRgConf();
		sender.sendMessage(MessageFormat.format(Lang.SUCCESS_DELGROUP.get(), groupName));
	}
	
	private void runAddRegionCmd(CommandSender sender, String[] args) throws IOException {
		String groupName = args[1].toLowerCase(); // 기존에 있던것들
		String[] regionIDsToAdd = Arrays.copyOfRange(args, 2, args.length); // 입력받은 것들
		List<String> addedRegionIDs = new ArrayList<String>(); // 실제 추가된 것들
		
		if (!regionConfig.getRgYmlConf().getConfigurationSection("groups").getKeys(false).contains(groupName)) {
			sender.sendMessage(MessageFormat.format(Lang.GROUP_NOT_FOUND.get(), groupName));
			return;
		}
		
		List<String> regionIDs = regionConfig.getRgYmlConf().getStringList("groups." + groupName + ".region-ids");
		for (String eRgID : regionIDsToAdd) {
			// 그룹에 추가하려는 each region이 region specific rule을 갖고있으면 remove
			ConfigurationSection rgSpecificCfg = regionConfig.getRgYmlConf().getConfigurationSection("regions." + eRgID);
			if (rgSpecificCfg != null) {
				regionConfig.getRgYmlConf().set("regions." + eRgID, null);
				sender.sendMessage(MessageFormat.format(Lang.RG_SPECI_CONF_REMOVED.get(), eRgID));
			}
			// 중복확인 and add each region
			if (!regionIDs.contains(eRgID)) {
				regionIDs.add(eRgID);
				addedRegionIDs.add(eRgID);
			}
		}
		regionConfig.getRgYmlConf().set("groups." + groupName + ".region-ids", regionIDs);
		regionConfig.savaRgConf();
		if (addedRegionIDs.size() == 0) sender.sendMessage(Lang.NOTHING_TO_ADD.get());
		else if (addedRegionIDs.size() < 2) sender.sendMessage(MessageFormat.format(Lang.SUCCESS_ADDREGION.get(), addedRegionIDs.get(0), groupName));
		else sender.sendMessage(MessageFormat.format(Lang.SUCCESS_ADDREGION_MULTI.get(), addedRegionIDs.get(0), (addedRegionIDs.size() - 1), groupName));
	}
	
	private void runDelRegionCmd(CommandSender sender, String[] args) throws IOException {
		String groupName = args[1].toLowerCase(); // 기존에 있던것들
		String[] regionIDsToDel = Arrays.copyOfRange(args, 2, args.length); // 입력받은 것들
		List<String> deletedRegionIDs = new ArrayList<String>(); // 실제 삭제된 것들
		
		if (!regionConfig.getRgYmlConf().getConfigurationSection("groups").getKeys(false).contains(groupName)) {
			sender.sendMessage(MessageFormat.format(Lang.GROUP_NOT_FOUND.get(), groupName));
			return;
		}
		
		List<String> regionIDs = regionConfig.getRgYmlConf().getStringList("groups." + groupName + ".region-ids");
		for (String eRgID : regionIDsToDel) {
			// 존재확인 and delete each region
			if (regionIDs.contains(eRgID)) {
				regionIDs.remove(eRgID);
				deletedRegionIDs.add(eRgID);
			}
		}
		regionConfig.getRgYmlConf().set("groups." + groupName + ".region-ids", regionIDs);
		regionConfig.savaRgConf();
		if (deletedRegionIDs.size() == 0) sender.sendMessage(Lang.NOTHING_TO_DEL.get());
		else if (deletedRegionIDs.size() < 2) sender.sendMessage(MessageFormat.format(Lang.SUCCESS_DELREGION.get(), deletedRegionIDs.get(0), groupName));
		else sender.sendMessage(MessageFormat.format(Lang.SUCCESS_DELREGION_MULTI.get(), deletedRegionIDs.get(0), (deletedRegionIDs.size() - 1), groupName));
	}

	/**
	 * 띄어쓰기 포함 텍스트가 배열로 쪼개진 것 다시 합침
	 * + Escape characters
	 * @return Combined text
	 */
	public String combineStrArr(String[] splitedTexts) {
		String combinedText = "";
		for (String eSplText : splitedTexts) {
			combinedText += eSplText + " ";
		}
		return combinedText.trim();
	}
}
