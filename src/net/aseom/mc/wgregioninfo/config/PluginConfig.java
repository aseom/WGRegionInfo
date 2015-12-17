package net.aseom.mc.wgregioninfo.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.bukkit.configuration.file.YamlConfiguration;

import net.aseom.mc.wgregioninfo.WGRegionInfo;

public class PluginConfig {
	private final String defaultConfig
	= "language: ko-kr\r\n"
	+ "enable-hud: true\r\n"
	+ "hud-off-users: []";
	private File configFile;
	private YamlConfiguration config;
	
	public YamlConfiguration getPluginConfig() {
		return this.config;
	}
	
	public void loadPluginConfig() throws Exception {
		this.configFile = new File(WGRegionInfo.plugin.getDataFolder(), "plugin-config.yml");
		if (!configFile.exists()) createDefaultConfig();
		
		this.config = new YamlConfiguration();
		config.load(configFile);
	}
	
	public void createDefaultConfig() {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), "UTF-8"));
			writer.write(defaultConfig);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void savaPluginConfig() throws IOException {
		config.save(configFile);
	}
}
