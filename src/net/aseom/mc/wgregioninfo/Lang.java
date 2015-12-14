package net.aseom.mc.wgregioninfo;

import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.jline.internal.InputStreamReader;

public enum Lang {
	PLUGIN_ENABLED("plugin-enabled"),
	WG_NOT_FOUND("worldguard-not-found"),
	WGRGEVENTS_NOT_FOUND("wgrgevents-not-found"),
	ERR_CANT_SAVE_CONFIG("err-cant-save-config"),
	GROUP_NOT_FOUND("group-not-found"),
	GROUPNAME_CANT_SPACE("groupname-cant-space"),
	RG_SPECI_RULE_REMOVED("rg-speci-rule-removed"),
	RG_SPECI_RULE_MOVED("rg-speci-rule-moved");
	
	public static YamlConfiguration langConf;
	private final String path;
	
	/**
	 * Constructor
	 * @param Yaml config path
	 */
	Lang(String path) {
		this.path = path;
	}
	
	/**
	 * Get localized text
	 * @return The text
	 */
	public String get() {
		return ChatColor.translateAlternateColorCodes('&', langConf.getString(path));
	}
	
	/**
	 * Set locale and load language config, before use
	 * @param Locale code
	 */
	public static void loadLang(String locale) {
		try {
			Reader confReader = new InputStreamReader(WGRegionInfo.plugin.getResource("lang/" + locale + ".yml"), "UTF-8");
			langConf = YamlConfiguration.loadConfiguration(confReader);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
