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
	HUD_OFF("hud-off"),
	USAGE_REGIONCONF("usage-regionconf"),
	USAGE_NEWGROUP("usage-newgroup"),
	USAGE_DELGROUP("usage-delgroup"),
	USAGE_ADDREGION("usage-addregion"),
	USAGE_DELREGION("usage-delregion"),
	SUCCESS_GROUPCONF("success-groupconf"),
	SUCCESS_GROUPCONF_REM("success-groupconf-rem"),
	SUCCESS_REGIONCONF("success-regionconf"),
	SUCCESS_REGIONCONF_REM("success-regionconf-rem"),
	SUCCESS_NEWGROUP("success-newgroup"),
	SUCCESS_DELGROUP("success-delgroup"),
	SUCCESS_ADDREGION("success-addregion"),
	SUCCESS_ADDREGION_MULTI("success-addregion-multi"),
	SUCCESS_DELREGION("success-delregion"),
	SUCCESS_DELREGION_MULTI("success-delregion-multi"),
	NOTHING_TO_ADD("nothing-to-add"),
	NOTHING_TO_DEL("nothing-to-del"),
	CANT_USE_CONSOLE("cant-use-console"),
	NO_PERMISSION("no-permission"),
	ERR_CANT_SAVE_CONFIG("err-cant-save-config"),
	GROUP_NOT_FOUND("group-not-found"),
	GROUPNAME_CANT_SPACE("groupname-cant-space"),
	RG_SPECI_CONF_REMOVED("rg-speci-conf-removed"),
	RG_SPECI_CONF_MOVED("rg-speci-conf-moved"),
	PLUGIN_RELOADED("plugin-reloaded"),
	ERROR_WHILE_RELOAD("error-while-reload");
	
	public static YamlConfiguration langConf;
	private final String path;
	
	/**
	 * Constructor
	 * @param path Yaml config path
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
	 * @param locale Locale code
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
