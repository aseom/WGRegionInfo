package net.aseom.mc.wgregioninfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
	private final WGRegionInfo wgRegionInfo;
	private final String defaultRegionRules
		= "regions:\r\n"
		+ "  example-region:\r\n"
		+ "    greet-title: \"Welcome!\"\r\n"
		+ "    bye-title: \"Goodbye!\"\r\n"
		+ "groups:\r\n"
		+ "  example-group:\r\n"
		+ "    region-ids:\r\n"
		+ "    - the_spawn_region\r\n"
		+ "    - the_shop_region\r\n"
		+ "    greet-title: \"Welcome!\"\r\n"
		+ "    bye-title: \"Goodbye!\"\r\n";
	public static File rgRulesFile;
	public static YamlConfiguration rgRules;
	
	public Config(WGRegionInfo wgRgI) {
		wgRegionInfo = wgRgI;
	}

	public void loadRegionRules() {
		rgRulesFile = new File(wgRegionInfo.getDataFolder(), "region-rules.yml");
		if (!rgRulesFile.exists()) createDefaultRegionRules();
		
		try {
			rgRules = new YamlConfiguration();
			rgRules.load(rgRulesFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public void createDefaultRegionRules() {
		if (!wgRegionInfo.getDataFolder().exists()) {
			wgRegionInfo.getDataFolder().mkdirs();
		}
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rgRulesFile), "UTF-8"));
			writer.write(defaultRegionRules);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get group name that contained the region 
	 * @return Group name
	 */
	public static String getGroup(String regionID) {
		Set<String> groupNames = rgRules.getConfigurationSection("groups").getKeys(false);
		for (String eGrpName : groupNames) {
			List<String> rgIDs = rgRules.getStringList("groups." + eGrpName + ".region-ids");
			if (rgIDs.contains(regionID)) {
				return eGrpName;
			}
		}
		return null;
	}

	/**
	 * Copy group specific rule to region specific rule
	 */
	public static void moveGroupRuleToRg(String regionID, String group) {
		String[] confs = {"greet-title", "bye-title"};
		for (String eConf : confs) {
			String str = rgRules.getString("groups." + group + "." + eConf);
			if (str != null) rgRules.set("regions." + regionID + "." + eConf, str);
		}
		rgRules.getList("groups." + group + ".region-ids").remove(regionID);
	}
	
	/**
	 * Get region rule, in both of region/group specific rule
	 * @return The rule
	 */
	public static String getRegionRule(String ruleName, String regionID) {
		
		// Get by region 1st
		String regionRule = rgRules.getString("regions." + regionID + "." + ruleName);
		if (regionRule != null) return regionRule;
		
		// Get by group 2nd
		String groupRule = rgRules.getString("groups." + getGroup(regionID) + "." + ruleName);
		if (groupRule != null) return groupRule;

		return null;
	}

	/**
	 * If no rule left, Remove ConfigSection of the region in region specific rule
	 */
	public static void cleanupRgSpeciRule(String regionID) {
		if (rgRules.getConfigurationSection("regions." + regionID).getKeys(false).size() == 0)
			rgRules.set("regions." + regionID, null);
	}
}
