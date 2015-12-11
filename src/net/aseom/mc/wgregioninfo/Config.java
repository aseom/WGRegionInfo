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
	private final WGRegionInfo main;
	private final String defaultRegionRules
		= "regions:\r\n"
		+ "  example-region:\r\n"
		+ "    greet-title: \"Welcome!\"\r\n"
		+ "    bye-title: \"Goodbye!\"\r\n"
		+ "groups:\r\n"
		+ "  example-group:\r\n"
		+ "    region-ids:\r\n"
		+ "      - spawn\r\n"
		+ "      - shop\r\n"
		+ "    greet-title: \"Welcome!\"\r\n"
		+ "    bye-title: \"Goodbye!\"\r\n";
	public static File rgRulesFile;
	public static YamlConfiguration rgRules;
	
	public Config(WGRegionInfo wgRegInf) {
		main = wgRegInf;
	}

	public void loadRegionRules() {
		rgRulesFile = new File(main.getDataFolder(), "region-rules.yml");
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
		// 데이터 폴더 없으면 생성
		if (!main.getDataFolder().exists()) {
			main.getDataFolder().mkdirs();
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
	 * Check existing region config, in Both of region/group specific rule
	 * @return If the config already exist -> confPath, else -> null
	 */
	public static String checkRgConf(String confName, String rgName) {
		String path;
		
		// Get by region
		path = "regions." + rgName + "." + confName;
		if (rgRules.get(path) != null) return path;
		
		// Get by group
		Set<String> groupIDs = rgRules.getConfigurationSection("groups").getKeys(false);
		for (String aGroupID : groupIDs) {
			List<String> regionIdList = rgRules.getStringList("groups." + aGroupID + ".region-ids");
			if (regionIdList.contains(rgName)) {
				path = "groups." + aGroupID + "." + confName;
				if (rgRules.getString(path) != null) return path;
			}
		}
		return null;
	}

	/**
	 * Remove already existing region config, in Both of region/group specific rule
	 */
	public static void rmConflictRgConf(String confName, String regionID) {
		String existingConfPath = checkRgConf(confName, regionID);
		if (existingConfPath != null) {
			rgRules.set(existingConfPath, null);
			if (existingConfPath.startsWith("regions.")) cleanupRgConfSection(regionID);
		}
	}

	/**
	 * If no config left, Remove ConfigSection in region specific rule
	 */
	public static void cleanupRgConfSection(String regionID) {
		if (rgRules.getConfigurationSection("regions." + regionID).getKeys(false).size() == 0)
			rgRules.set("regions." + regionID, null);
	}
}
