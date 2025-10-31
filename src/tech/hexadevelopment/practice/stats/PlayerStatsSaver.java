package tech.hexadevelopment.practice.stats;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;

public class PlayerStatsSaver extends BukkitRunnable{


	private LegionPractice plugin;


	public PlayerStatsSaver(LegionPractice plugin) {
		this.plugin = plugin;
		int i = LegionPractice.getInstance().getConfig().getInt("player-stats-auto-save-period")*20;
		if(i < 60) {
			Bukkit.getLogger().warning("player-stats-auto-save-period was set to " + i + " seconds. Increasing the period to avoid problems and lag.");
			i = 60;
		}
		runTaskTimerAsynchronously(plugin, 20*10, LegionPractice.performanceMode ? i*5 : i);
	}


	@Override
	public void run() {
		for(Player p : Bukkit.getOnlinePlayers()) {
			PlayerStats stats = PlayerStats.getStats(p.getUniqueId(), false);
			if(stats != null) {
				stats.save();
			}
		}
		synchronized(PlayerStats.top) {
			for(BattleKit kit : plugin.kits) {
				if(kit.isElo()) {
					String stat = Stats.elo(kit.getName());
					PlayerStats.top.put(stat, Stats.getQueryManager().topStats(stat, 10));
				}
			}
			PlayerStats.top.put("global_elo", Stats.getQueryManager().topStats("global_elo", 10));
			PlayerStats.top.put(Stats.BRACKETS, Stats.getQueryManager().topStats(Stats.BRACKETS, 10));
			PlayerStats.top.put(Stats.DEATHS, Stats.getQueryManager().topStats(Stats.DEATHS, 10));
			PlayerStats.top.put(Stats.GLOBAL_ELO, Stats.getQueryManager().topStats(Stats.GLOBAL_ELO, 10));
			PlayerStats.top.put(Stats.KILLS, Stats.getQueryManager().topStats(Stats.KILLS, 10));
			PlayerStats.top.put(Stats.LMS, Stats.getQueryManager().topStats(Stats.LMS, 10));
			PlayerStats.top.put(Stats.PARTY_VS_PARTY_WINS, Stats.getQueryManager().topStats(Stats.PARTY_VS_PARTY_WINS, 10));
		}
	}
}
