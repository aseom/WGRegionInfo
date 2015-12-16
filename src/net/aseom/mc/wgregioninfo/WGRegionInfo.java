package net.aseom.mc.wgregioninfo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
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

public class WGRegionInfo extends JavaPlugin implements Listener {
	public static WGRegionInfo plugin;
	private Config config;
	private WorldGuardPlugin worldGuardPlugin;
	
	public Config getConfigClass() {
		return this.config;
	}

	@Override
	public void onEnable() {
		plugin = this;
		this.config = new Config();
		Lang.loadLang("ko-kr");
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
		
		try {
			config.loadRegionRules();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		getCommand("regioninfo").setExecutor(new RgInfoCommand());
		plugMgr.registerEvents(this, this);
		
		ConsoleCommandSender console = getServer().getConsoleSender();
		console.sendMessage(Lang.PLUGIN_ENABLED.get());
	}
	
	@EventHandler
	public void onRegionEnter(RegionEnterEvent event) {
		Player player = event.getPlayer();
		ProtectedRegion region = event.getRegion();
		
		Scoreboard scoreBoard = getRegionInfoBoard(region);
		player.setScoreboard(scoreBoard);
		
		String greetTitle = config.getRegionRule("greet-title", region.getId());
		String greetSubtitle = config.getRegionRule("greet-subtitle", region.getId());
		if (greetTitle != null || greetSubtitle != null) {
			sendTitleSubtitle(greetTitle, greetSubtitle, player);
		}
	}
	
	@EventHandler
	public void onRegionLeave(RegionLeaveEvent event) {
		Player player = event.getPlayer();
		ProtectedRegion region = event.getRegion();
		
		Scoreboard blankBoard = Bukkit.getScoreboardManager().getNewScoreboard();
		event.getPlayer().setScoreboard(blankBoard);

		String byeTitle = config.getRegionRule("bye-title", region.getId());
		String byeSubtitle = config.getRegionRule("bye-subtitle", region.getId());
		if (byeTitle != null || byeSubtitle != null) {
			sendTitleSubtitle(byeTitle, byeSubtitle, player);
		}
	}
	
	public Scoreboard getRegionInfoBoard(ProtectedRegion region) {
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
		return scoreBoard;
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
}
