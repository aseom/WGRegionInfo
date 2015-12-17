package net.aseom.mc.wgregioninfo.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;

import net.aseom.mc.wgregioninfo.WGRegionInfo;

public class RegionRule {
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
	private File regionRulesFile;
	private YamlConfiguration regionRulesConf;
	
	public YamlConfiguration getRegionRulesConf() {
		return this.regionRulesConf;
	}

	public void loadRegionRules() throws Exception {
		this.regionRulesFile = new File(WGRegionInfo.plugin.getDataFolder(), "region-rules.yml");
		if (!regionRulesFile.exists()) createDefaultRegionRules();
		
		this.regionRulesConf = new YamlConfiguration();
		regionRulesConf.load(regionRulesFile);
	}
	
	public void createDefaultRegionRules() {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(regionRulesFile), "UTF-8"));
			writer.write(defaultRegionRules);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void savaRegionRulesConf() throws IOException {
		regionRulesConf.save(regionRulesFile);
	}
	
	/**
	 * Get group name that contained the region 
	 * @return Group name
	 */
	public String getGroup(String regionID) {
		Set<String> groupNames = regionRulesConf.getConfigurationSection("groups").getKeys(false);
		for (String eGrpName : groupNames) {
			List<String> rgIDs = regionRulesConf.getStringList("groups." + eGrpName + ".region-ids");
			if (rgIDs.contains(regionID)) {
				return eGrpName;
			}
		}
		return null;
	}

	/**
	 * Copy group specific rule to region specific rule
	 */
	public void moveGroupRuleToRg(String regionID, String group) {
		String[] confs = {"greet-title", "bye-title"};
		for (String eConf : confs) {
			String str = regionRulesConf.getString("groups." + group + "." + eConf);
			if (str != null) regionRulesConf.set("regions." + regionID + "." + eConf, str);
		}
		regionRulesConf.getList("groups." + group + ".region-ids").remove(regionID);
	}
	
	/**
	 * Get region rule, in both of region/group specific rule
	 * @return The rule
	 */
	public String getRegionRule(String ruleName, String regionID) {
		
		// Get by region 1st
		String regionRule = regionRulesConf.getString("regions." + regionID + "." + ruleName);
		if (regionRule != null) return regionRule;
		
		// Get by group 2nd
		String groupRule = regionRulesConf.getString("groups." + getGroup(regionID) + "." + ruleName);
		if (groupRule != null) return groupRule;

		return null;
	}

	/**
	 * If no rule left, Remove ConfigSection of the region in region specific rule
	 */
	public void cleanupRgSpeciRule(String regionID) {
		if (regionRulesConf.getConfigurationSection("regions." + regionID).getKeys(false).size() == 0)
			regionRulesConf.set("regions." + regionID, null);
	}
}
