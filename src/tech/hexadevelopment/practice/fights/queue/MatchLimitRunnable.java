package tech.hexadevelopment.practice.fights.queue;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.stats.PlayerStats;

public class MatchLimitRunnable extends BukkitRunnable{

	
	private boolean rankeds, unrankeds;
	
	public MatchLimitRunnable(LegionPractice plugin, boolean rankeds, boolean unrankeds) {
		this.rankeds = rankeds;
		this.unrankeds = unrankeds;
		runTaskTimerAsynchronously(plugin, 20*60, 20*60*5);
	}
	
	
	@Override
	public void run() {
		for(Player p : Bukkit.getOnlinePlayers()) {
			PlayerStats ps = PlayerStats.getStats(p.getUniqueId(), true, false);
			ps.checkMatchLimitUpdate(rankeds, unrankeds);
		}
	}

}
