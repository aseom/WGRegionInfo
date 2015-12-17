package net.aseom.mc.wgregioninfo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.mewin.WGRegionEvents.events.RegionEnterEvent;
import com.mewin.WGRegionEvents.events.RegionLeaveEvent;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.aseom.mc.wgregioninfo.config.PluginConfig;
import net.aseom.mc.wgregioninfo.config.RegionRule;

public class WGRegionInfo extends JavaPlugin implements Listener {
	public static WGRegionInfo plugin;
	private PluginConfig pluginConfig;
	private RegionRule regionRule;
	private WorldGuardPlugin worldGuardPlugin;
	private Scoreboard currentHudBoard;
	
	public RegionRule getRegionRuleClass() {
		return this.regionRule;
	}

	public PluginConfig getPluginConfigClass() {
		return this.pluginConfig;
	}

	public Scoreboard getCurrentHudBoard() {
		return this.currentHudBoard;
	}

	@Override
	public void onEnable() {
		plugin = this;
		if (!getDataFolder().exists()) getDataFolder().mkdirs();
		this.pluginConfig = new PluginConfig();
		try {
			pluginConfig.loadPluginConfig();
		} catch (Exception e) {
			e.printStackTrace();
		}
		setPluginLanguage();
		
		PluginManager plugMgr = getServer().getPluginManager();
		this.worldGuardPlugin = (WorldGuardPlugin) plugMgr.getPlugin("WorldGuard");
		if (worldGuardPlugin == null) {
			getLogger().warning(Lang.WG_NOT_FOUND.get());
			plugMgr.disablePlugin(this);
			return;
		}
		if (!plugMgr.isPluginEnabled("WGRegionEvents")) {
			getLogger().warning(Lang.WGRGEVENTS_NOT_FOUND.get());
			plugMgr.disablePlugin(this);
			return;
		}
		
		plugMgr.registerEvents(this, this);
		getCommand("regioninfo").setExecutor(new RgInfoCommand());
		getCommand("regionhud").setExecutor(new RgInfoCommand());
		
		this.regionRule = new RegionRule();
		try {
			regionRule.loadRegionRules();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		getServer().getConsoleSender().sendMessage(Lang.PLUGIN_ENABLED.get());
	}
	
	@EventHandler
	public void onRegionEnter(RegionEnterEvent event) {
		Player player = event.getPlayer();
		ProtectedRegion region = event.getRegion();
		YamlConfiguration pConf = pluginConfig.getPluginConfig();
		
		this.currentHudBoard = getHudBoard(region);
		if (pConf.getBoolean("enable-hud")
				&& !pConf.getStringList("hud-off-users").contains(player.getName())) {
			player.setScoreboard(currentHudBoard);
		}
		
		String greetTitle = regionRule.getRegionRule("greet-title", region.getId());
		String greetSubtitle = regionRule.getRegionRule("greet-subtitle", region.getId());
		if (greetTitle != null || greetSubtitle != null) {
			sendTitleSubtitle(greetTitle, greetSubtitle, player);
		}
	}
	
	@EventHandler
	public void onRegionLeave(RegionLeaveEvent event) {
		Player player = event.getPlayer();
		ProtectedRegion region = event.getRegion();
		
		if (currentHudBoard != null) {
			Scoreboard blankBoard = Bukkit.getScoreboardManager().getNewScoreboard();
			event.getPlayer().setScoreboard(blankBoard);
			this.currentHudBoard = null;
		}

		String byeTitle = regionRule.getRegionRule("bye-title", region.getId());
		String byeSubtitle = regionRule.getRegionRule("bye-subtitle", region.getId());
		if (byeTitle != null || byeSubtitle != null) {
			sendTitleSubtitle(byeTitle, byeSubtitle, player);
		}
	}
	
	public Scoreboard getHudBoard(ProtectedRegion region) {
		String[] ownerName = getUsersName(region.getOwners());
		String[] memberName = getUsersName(region.getMembers());
		
		Scoreboard scoreBoard = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective scoreObj = scoreBoard.registerNewObjective("test", "dummy");
		scoreObj.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		scoreObj.setDisplayName(ChatColor.BOLD + "WorldGuard Region");
		int score = -1; // 한줄 출력마다 1씩 감소하게될 줄번호
		scoreObj.getScore(ChatColor.GOLD + "ID:").setScore(score--);
		scoreObj.getScore(" - " + region.getId()).setScore(score--);
		if (ownerName != null) {
			scoreObj.getScore(ChatColor.GOLD + "Owners:").setScore(score--);
			for (String eOwner : ownerName) {
				scoreObj.getScore(" - " + eOwner).setScore(score--);
			}
		}
		if (memberName != null) {
			scoreObj.getScore(ChatColor.GOLD + "Members:").setScore(score--);
			for (String eMember : memberName) {
				scoreObj.getScore(" - " + eMember).setScore(score--);
			}
		}
		scoreObj.getScore(ChatColor.GRAY + "________________").setScore(score--);
		scoreObj.getScore(Lang.HUD_OFF.get()).setScore(score--);
		return scoreBoard;
	}
	
	public void reloadHudBoard(Player player) {
		if (currentHudBoard == null) return;
		YamlConfiguration pConf = pluginConfig.getPluginConfig();
		if (pConf.getBoolean("enable-hud")
				&& !pConf.getStringList("hud-off-users").contains(player.getName())) {
			// 현재 로드되어 있는 HUD를 띄움
			player.setScoreboard(currentHudBoard);
		} else {
			// 숨김
			Scoreboard blankBoard = Bukkit.getScoreboardManager().getNewScoreboard();
			player.setScoreboard(blankBoard);
		}
	}

	public void sendTitleSubtitle(String byeTitle, String byeSubtitle, Player player) {
		if (byeTitle == null) byeTitle = "";
		if (byeSubtitle == null) byeSubtitle = "";
		Title title = new Title(byeTitle, byeSubtitle, 10, 20, 10);
		title.setTimingsToTicks();
		title.send(player);
	}

	public String[] getUsersName(DefaultDomain domain) {
		String owners = domain.toPlayersString(worldGuardPlugin.getProfileCache());
		if (owners.length() == 0) return null;
		return owners.replace("name:", "").toLowerCase().split(", ");
	}

	public void setPluginLanguage() {
		String language = pluginConfig.getPluginConfig().getString("language");
		if (!language.matches("en-us|ko-kr")) {
			getLogger().warning("Language " + language + " is unvaild. Loading en-us...");
			Lang.loadLang("en-us");
			return;
		}
		Lang.loadLang(language);
	}
	
	public void reloadPlugin() throws Exception {
		if (!getDataFolder().exists()) getDataFolder().mkdirs();
		pluginConfig.loadPluginConfig();
		setPluginLanguage();
		regionRule.loadRegionRules();
	}
}
