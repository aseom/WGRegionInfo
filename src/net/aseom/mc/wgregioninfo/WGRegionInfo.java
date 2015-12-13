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
		Lang.loadLang("en-us");
		PluginManager plugMgr = getServer().getPluginManager();
		
		this.worldGuardPlugin = (WorldGuardPlugin) plugMgr.getPlugin("WorldGuard");
		if (worldGuardPlugin == null) {
			getLogger().warning(Lang.WG_NOT_FOUND.get());
			plugMgr.disablePlugin(this);
			return;
		}
		if (!plugMgr.isPluginEnabled("WGRegionEvents")) {
			getLogger().warning(Lang.WGRGEVT_NOT_FOUND.get());
			plugMgr.disablePlugin(this);
			return;
		}
		
		config.loadRegionRules();
		
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
		if (greetTitle != null) {
			Title title = new Title("", greetTitle, 10, 20, 10);
			title.setTimingsToTicks();
			title.send(player);
		}
	}
	
	@EventHandler
	public void onRegionLeave(RegionLeaveEvent event) {
		Player player = event.getPlayer();
		ProtectedRegion region = event.getRegion();
		
		Scoreboard blankBoard = Bukkit.getScoreboardManager().getNewScoreboard();
		event.getPlayer().setScoreboard(blankBoard);

		String byeTitle = config.getRegionRule("bye-title", region.getId());
		if (byeTitle != null) {
			Title title = new Title("", byeTitle, 10, 20, 10);
			title.setTimingsToTicks();
			title.send(player);
		}
	}
	
	public Scoreboard getRegionInfoBoard(ProtectedRegion region) {
		String[] ownerName = getUsersName(region.getOwners());
		String[] memberName = getUsersName(region.getMembers());
		
		Scoreboard scoreBoard = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective scroreObj = scoreBoard.registerNewObjective("test", "dummy");
		scroreObj.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		scroreObj.setDisplayName(ChatColor.BOLD + "WorldGuard Region");
		int score = -1; // 한줄 출력마다 1씩 감소하게될 줄번호
		scroreObj.getScore(ChatColor.GOLD + "ID:").setScore(score--);
		scroreObj.getScore(" - " + region.getId()).setScore(score--);
		if (ownerName != null) {
			scroreObj.getScore(ChatColor.GOLD + "Owners:").setScore(score--);
			for (String eOwner : ownerName) {
				scroreObj.getScore(" - " + eOwner).setScore(score--);
			}
		}
		if (memberName != null) {
			scroreObj.getScore(ChatColor.GOLD + "Members:").setScore(score--);
			for (String eMember : memberName) {
				scroreObj.getScore(" - " + eMember).setScore(score--);
			}
		}
		return scoreBoard;
	}

	public String[] getUsersName(DefaultDomain domain) {
		String owners = domain.toPlayersString(worldGuardPlugin.getProfileCache());
		if (owners.length() == 0) return null;
		String[] ownerNames = owners.replace("name:", "").toLowerCase().split(", ");
		return ownerNames;
	}
}
