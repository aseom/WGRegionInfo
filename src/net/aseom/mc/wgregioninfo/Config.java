package net.aseom.mc.wgregioninfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
	private final WGRegionInfo main;
	private final String defaultRegionRules
		= "# Region Rules\r\n"
		+ "#\r\n"
		+ "# Region ID가 'spawn' 또는 'shop'인 곳에 유저가 들어갈 때\r\n"
		+ "# \"Welcome!\" 타이틀을 띄우고 싶다면 이렇게 설정하면 됩니다.\r\n"
		+ "\r\n"
		+ "example-rule:\r\n"
		+ "  region-ids:\r\n"
		+ "    - spawn\r\n"
		+ "    - shop\r\n"
		+ "  greet-title: \"Welcome!\"\r\n"
		+ "  bye-title: \"Goodbye!\"\r\n";
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
}
