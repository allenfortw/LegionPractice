package tech.hexadevelopment.practice.arena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.utils.TPSUtil;
import tech.hexadevelopment.practice.LegionPractice;

public class AutomaticRollbackRate extends BukkitRunnable{


	public static int rollbacksSinceLast;


	@Override
	public void run() {
		if(rollbacksSinceLast > 1) {
			double tps = TPSUtil.get1MinTPS();
			if(Arena.maxChangesPerTick < 5) Arena.maxChangesPerTick = 5;
			if(Arena.maxChecksPerTick < 50) Arena.maxChecksPerTick = 50;
			int oldChange = Arena.maxChangesPerTick;
			int oldCheck = Arena.maxChecksPerTick;
			int defaultChangeRate = LegionPractice.getInstance().getConfig().getInt("max-block-changes-per-tick-per-arena");
			int defaultCheckRate = LegionPractice.getInstance().getConfig().getInt("max-block-checks-per-tick-per-arena");
			double x = (20-tps);
			if(x <= 0) x = 0.1;
			if(rollbacksSinceLast >= 5) {
				if(tps >= 19.9) {
					int newChangeRate = (int) (Arena.maxChangesPerTick*1.2);
					int newCheckRate = (int) (Arena.maxChecksPerTick*1.2);
					if(defaultChangeRate*3 > newChangeRate) {
						Arena.maxChangesPerTick = newChangeRate;
					}
					else Arena.maxChangesPerTick = defaultChangeRate*3;
					if(defaultCheckRate*3 > newCheckRate) {
						Arena.maxChecksPerTick = newCheckRate;
					}
					else Arena.maxChecksPerTick = defaultCheckRate*3;
				}
				else if(tps < 19.8){
					Arena.maxChangesPerTick = (int) (Arena.maxChangesPerTick*(0.8));
					Arena.maxChecksPerTick = (int) (Arena.maxChecksPerTick*0.8);
				}
			}
			if(Arena.maxChangesPerTick < 1) {
				Arena.maxChangesPerTick = 1;
			}
			if(Arena.maxChecksPerTick < 20) {
				Arena.maxChecksPerTick = 20;
			}
			double rounded = TPSUtil.get1MinTPSRounded();
			String prefix = LegionPractice.getInstance().getConfig().getString("prefix");
			if(oldChange != Arena.maxChangesPerTick) {
				int c = Arena.maxChangesPerTick-oldChange;
				String change = c + "";
				if(c > 0) change = "+" + c;
				for(Player p : Bukkit.getOnlinePlayers()) {
					if(p.hasMetadata("ToppeBattlesRollbackRateAlerts")) {
						p.sendMessage(prefix + ChatColor.GRAY + "Rollback block change rate has changed! " + oldChange + " ➡ " + Arena.maxChangesPerTick + " (" + change + ") blocks/s , tps: " + rounded + ", rollbacks: " + rollbacksSinceLast);
					}
				}
			}
			if(oldCheck != Arena.maxChecksPerTick) {
				int c = Arena.maxChecksPerTick-oldCheck;
				String change = c + "";
				if(c > 0) change = "+" + c;
				for(Player p : Bukkit.getOnlinePlayers()) {
					if(p.hasMetadata("ToppeBattlesRollbackRateAlerts")) {
						p.sendMessage(prefix + ChatColor.GRAY + "Rollback block check rate has changed! " + oldCheck + " ➡ " + Arena.maxChecksPerTick + " (" + change + ") blocks/s , tps: " + rounded + ", rollbacks: " + rollbacksSinceLast);
					}
				}
			}
		}
		rollbacksSinceLast = 0;
	}
}
