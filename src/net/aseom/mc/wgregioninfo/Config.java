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

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Config {
	private final WGRegionInfo main;
	private final String defaultRegionRules
		= "# Region Configuration\r\n"
		+ "#\r\n"
		+ "# Region ID가 'spawn' 또는 'shop'인 곳에 유저가 들어갈 때\r\n"
		+ "# \"Welcome!\" 타이틀을 띄우고 싶다면 이렇게 설정하면 됩니다.\r\n"
		+ "\r\n"
		+ "example-rule:\r\n"
		+ "  region-ids:\r\n"
		+ "    - spawn\r\n"
		+ "    - shop\r\n"
		+ "  greet-title: \"Welcome!\"\r\n";
	private File regionRulesFile;
	public YamlConfiguration rgRules;
	
	public Config(WGRegionInfo wgRegInf) {
		main = wgRegInf;
		this.rgRules = loadRegionRules();
	}
	
	public String getGreetTitle(ProtectedRegion region) {
		Set<String> ruleIds = rgRules.getKeys(false);
		for (String eachRule : ruleIds) {
			List<String> regionIdList = rgRules.getStringList(eachRule + ".region-ids");
			if (regionIdList.contains(region.getId())) {
				return rgRules.getString(eachRule + ".greet-title");
			}
		}
		return null;
	}

	public YamlConfiguration loadRegionRules() {
		this.regionRulesFile = new File(main.getDataFolder(), "region-rules.yml");
		// rules 파일이 없으면 새로 만든다.
		if (!regionRulesFile.exists()) {
			createDefaultRegionRules();
		}
		
		YamlConfiguration regionRules = new YamlConfiguration();
		try {
			regionRules.load(regionRulesFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		
		return regionRules;
	}
	
	public void createDefaultRegionRules() {
		// 데이터 폴더 없으면 생성
		if (!main.getDataFolder().exists()) {
			main.getDataFolder().mkdirs();
		}
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(regionRulesFile), "UTF-8"));
			writer.write(defaultRegionRules);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
