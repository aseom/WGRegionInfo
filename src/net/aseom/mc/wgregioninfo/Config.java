package net.aseom.mc.wgregioninfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Config {
	private final WGRegionInfo main;
	private final String defaultRegionConfig
		= "# Region Configuration\r\n"
		+ "#\r\n"
		+ "# filter-type은 ID, OWNER, MEMBER 세가지 값중 하나여야 합니다.\r\n"
		+ "#\r\n"
		+ "# Region ID가 'spawn' 또는 'shop'인 곳에 유저가 들어갈 때\r\n"
		+ "# 정보 패널을 표시하지 않도록 하고, \"Welcome!\" 타이틀을 띄우고 싶다면\r\n"
		+ "# 이렇게 설정하면 됩니다:\r\n"
		+ "\r\n"
		+ "example:\r\n"
		+ "  filter-type: RegionID\r\n"
		+ "  filter: spawn\r\n"
		+ "  greet-title: \"Welcome to Example Region!\"\r\n"
		+ "  show-info-panel: true\r\n";
	private File regionConfigFile;
	public YamlConfiguration rgConf;
	
	public Config(WGRegionInfo wgRegInf) {
		main = wgRegInf;
		this.rgConf = loadRegionConfig();
	}
	
	public String getGreetTitle(ProtectedRegion region) {
		Set<String> ruleIds = rgConf.getKeys(false);
		for (String eachRule : ruleIds) {
			String filterType = rgConf.getString(eachRule + ".filter-type");
			String filter = rgConf.getString(eachRule + ".filter");
			
			if (filterType.equalsIgnoreCase("RegionID") && filter.equalsIgnoreCase(region.getId())) {
				return rgConf.getString(eachRule + ".greet-title");
			}
		}
		return null;
	}

	public YamlConfiguration loadRegionConfig() {
		this.regionConfigFile = new File(main.getDataFolder(), "region-config.yml");
		// config 파일이 없으면 새로 만든다.
		if (!regionConfigFile.exists()) {
			createDefaultRegionConfig();
		}
		
		YamlConfiguration regionConfig = new YamlConfiguration();
		try {
			regionConfig.load(regionConfigFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		
		return regionConfig;
	}
	
	public void createDefaultRegionConfig() {
		// 데이터 폴더 없으면 생성
		if (!main.getDataFolder().exists()) {
			main.getDataFolder().mkdirs();
		}
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(regionConfigFile), "UTF-8"));
			writer.write(defaultRegionConfig);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
