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

public class RegionConfig {
	private final String defRgConf
		= "regions:\r\n"
		+ "  example-region:\r\n"
		+ "    enter-title: \"Welcome!\"\r\n"
		+ "    leave-title: \"Goodbye!\"\r\n"
		+ "groups:\r\n"
		+ "  example-group:\r\n"
		+ "    region-ids:\r\n"
		+ "    - the_spawn_region\r\n"
		+ "    - the_shop_region\r\n"
		+ "    enter-title: \"Welcome!\"\r\n"
		+ "    leave-title: \"Goodbye!\"\r\n";
	private final String[] availableRgConfs = {
		"enter-title", "enter-subtitle", "leave-title", "leave-subtitle"
	};
	private File rgConfFile;
	private YamlConfiguration rgYmlConf;
	
	public YamlConfiguration getRgYmlConf() {
		return this.rgYmlConf;
	}

	public String[] getAvailableRgConfs() {
		return this.availableRgConfs;
	}

	public void loadRgConf() throws Exception {
		this.rgConfFile = new File(WGRegionInfo.plugin.getDataFolder(), "regions.yml");
		if (!rgConfFile.exists()) createDefRgConf();
		
		this.rgYmlConf = new YamlConfiguration();
		rgYmlConf.load(rgConfFile);
	}
	
	public void createDefRgConf() {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rgConfFile), "UTF-8"));
			writer.write(defRgConf);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void savaRgConf() throws IOException {
		rgYmlConf.save(rgConfFile);
	}
	
	/**
	 * Get group name that contained the region 
	 * @return Group name
	 */
	public String getGroup(String regionID) {
		Set<String> groupNames = rgYmlConf.getConfigurationSection("groups").getKeys(false);
		for (String eGrpName : groupNames) {
			List<String> rgIDs = rgYmlConf.getStringList("groups." + eGrpName + ".region-ids");
			if (rgIDs.contains(regionID)) {
				return eGrpName;
			}
		}
		return null;
	}

	/**
	 * Copy group specific config to region specific config
	 */
	public void moveGroupConfToRg(String regionID, String group) {
		for (String eConf : availableRgConfs) {
			String str = rgYmlConf.getString("groups." + group + "." + eConf);
			if (str != null) rgYmlConf.set("regions." + regionID + "." + eConf, str);
		}
		rgYmlConf.getList("groups." + group + ".region-ids").remove(regionID);
	}
	
	/**
	 * Get region config, in both of region/group specific config
	 * @return The config
	 */
	public String getRgConfValue(String confName, String regionID) {
		
		// Get by region 1st
		String regionConf = rgYmlConf.getString("regions." + regionID + "." + confName);
		if (regionConf != null) return regionConf;
		
		// Get by group 2nd
		String groupConf = rgYmlConf.getString("groups." + getGroup(regionID) + "." + confName);
		if (groupConf != null) return groupConf;

		return null;
	}

	/**
	 * If no config left, Remove ConfigSection of the region in region specific config
	 */
	public void cleanupRgSpeciConf(String regionID) {
		if (rgYmlConf.getConfigurationSection("regions." + regionID).getKeys(false).size() == 0)
			rgYmlConf.set("regions." + regionID, null);
	}
}
