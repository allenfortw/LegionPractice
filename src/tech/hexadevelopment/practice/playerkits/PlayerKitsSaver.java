package tech.hexadevelopment.practice.playerkits;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;

public class PlayerKitsSaver extends BukkitRunnable{

	private static int period;
	private boolean x;
	private LegionPractice plugin;

	public PlayerKitsSaver(LegionPractice plugin) {
		this.plugin = plugin;
		runTaskTimerAsynchronously(plugin, 20*period, 20*period);
	}

	@Override
	public void run() {
		x = !x;
		long st = System.currentTimeMillis();
		boolean hasPlayersOnline = false;
		for(Player pl : Bukkit.getOnlinePlayers()) {
			if(LegionPractice.performanceMode && x) {
				plugin.getPlayerKitsHandler().getPlayerKits(pl).savePlayerKitsToFile();
				hasPlayersOnline = true;
			}
			x = !x;
		}
		if(hasPlayersOnline) {
			long l = System.currentTimeMillis()-st;
			Bukkit.getLogger().info("Saved all custom kits of the online players in " + l + " ms.");
		}
	}

	public static void setPeriod(int period) {
		PlayerKitsSaver.period = period;
	}

}
