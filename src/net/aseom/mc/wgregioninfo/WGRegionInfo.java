package net.aseom.mc.wgregioninfo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import com.mewin.WGRegionEvents.events.RegionEnterEvent;
import com.mewin.WGRegionEvents.events.RegionLeaveEvent;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.profile.cache.ProfileCache;

public class WGRegionInfo extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		Server server = getServer();
		server.getPluginManager().registerEvents(this, this);
		
		ConsoleCommandSender console = server.getConsoleSender();
		console.sendMessage(ChatColor.GREEN + "WGRegionInfo Enabled!");
	}
	
	@EventHandler
	public void onRegionEnter(RegionEnterEvent event) {
		Player player = event.getPlayer();
		ProtectedRegion region = event.getRegion();
		
		Scoreboard scoreBoard = getRegionInfoBoard(region);
		player.setScoreboard(scoreBoard);
		
//		Title title = new Title("", region.getId(), 10, 20, 10);
//		title.setTimingsToTicks();
//		title.send(player);
//		ActionBarAPI.sendActionBar(player, region.getId());
	}
	
	@EventHandler
	public void onRegionLeave(RegionLeaveEvent event) {
		Scoreboard blankBoard = Bukkit.getScoreboardManager().getNewScoreboard();
		event.getPlayer().setScoreboard(blankBoard);
	}

	public Scoreboard getRegionInfoBoard(ProtectedRegion region) {
		String[] ownerName = getUsersName(region.getOwners());
		String[] memberName = getUsersName(region.getMembers());
		
		Scoreboard scoreBoard = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective scroreObj = scoreBoard.registerNewObjective("test", "dummy");
		scroreObj.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		scroreObj.setDisplayName(ChatColor.BOLD + "WorldGuard Region");
		int score = -1; // 한줄 출력마다 1씩 감소하게될 줄번호(?)
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
		Plugin worldguard = getServer().getPluginManager().getPlugin("WorldGuard");
		WorldGuardPlugin wg = (WorldGuardPlugin) worldguard;
		ProfileCache pCache = wg.getProfileCache();
		
		String owners = domain.toPlayersString(pCache);
		if (owners.length() == 0) return null;
		
		String[] arrOwnerName = owners.replace("name:", "").toLowerCase().split(", ");
		return arrOwnerName;
	}
}
