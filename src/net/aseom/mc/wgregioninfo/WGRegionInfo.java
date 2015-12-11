package net.aseom.mc.wgregioninfo;

import java.util.List;
import java.util.Set;

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
	private WorldGuardPlugin WorldGuard;

	@Override
	public void onEnable() {
		PluginManager plugMgr = getServer().getPluginManager();
		
		this.WorldGuard = (WorldGuardPlugin) plugMgr.getPlugin("WorldGuard");
		if (WorldGuard == null) {
			getLogger().warning("WorldGuard plugin not found. Disabling plugin.");
			plugMgr.disablePlugin(this);
			return;
		}
		if (!plugMgr.isPluginEnabled("WGRegionEvents")) {
			getLogger().warning("WGRegionEvents dependency not found. Disabling plugin.");
			plugMgr.disablePlugin(this);
			return;
		}
		
		new Config(this).loadRegionRules();
		getCommand("regioninfo").setExecutor(new RgInfoCommand());
		plugMgr.registerEvents(this, this);
		
		ConsoleCommandSender console = getServer().getConsoleSender();
		console.sendMessage(ChatColor.GREEN + "WGRegionInfo Enabled!");
	}
	
	@EventHandler
	public void onRegionEnter(RegionEnterEvent event) {
		Player player = event.getPlayer();
		ProtectedRegion region = event.getRegion();
		
		Scoreboard scoreBoard = getRegionInfoBoard(region);
		player.setScoreboard(scoreBoard);
		
		String greetTitle = getRegionTitle("greet-title", region);
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

		String byeTitle = getRegionTitle("bye-title", region);
		if (byeTitle != null) {
			Title title = new Title("", byeTitle, 10, 20, 10);
			title.setTimingsToTicks();
			title.send(player);
		}
	}
	
	public String getRegionTitle(String type, ProtectedRegion region) {
		// Get by region
		if (Config.rgRules.get("regions." + region.getId() + "." + type) != null)
			return Config.rgRules.getString("regions." + region.getId() + "." + type);
		// Get by group
		Set<String> groupIDs = Config.rgRules.getConfigurationSection("groups").getKeys(false);
		for (String aGroupID : groupIDs) {
			List<String> regionIdList = Config.rgRules.getStringList("groups." + aGroupID + ".region-ids");
			if (regionIdList.contains(region.getId())) {
				String title = Config.rgRules.getString("groups." + aGroupID + "." + type);
				if (title != null) return title;
			}
		}
		return null;
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
			for (String each : ownerName) {
				scroreObj.getScore(" - " + each).setScore(score--);
			}
		}
		if (memberName != null) {
			scroreObj.getScore(ChatColor.GOLD + "Members:").setScore(score--);
			for (String each : memberName) {
				scroreObj.getScore(" - " + each).setScore(score--);
			}
		}
		return scoreBoard;
	}

	public String[] getUsersName(DefaultDomain domain) {
		String owners = domain.toPlayersString(WorldGuard.getProfileCache());
		if (owners.length() == 0) return null;
		String[] arrOwnerName = owners.replace("name:", "").toLowerCase().split(", ");
		return arrOwnerName;
	}
}
