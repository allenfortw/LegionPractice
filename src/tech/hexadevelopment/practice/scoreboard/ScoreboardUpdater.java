package tech.hexadevelopment.practice.scoreboard;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.placeholders.PlaceholderMode;
import tech.hexadevelopment.practice.utils.TPSUtil;

public class ScoreboardUpdater extends BukkitRunnable{

	private static long delay, lobbyDelay;
	public static boolean async, enhancedPerformance, needsMode, avoidErrors;
	private static HashMap<UUID, Integer> update = new HashMap<UUID, Integer>();
	private double tps;
	private int tpsCounter;
	private double skip = 20;
	public static BukkitTask task;

	public ScoreboardUpdater(LegionPractice plugin) {
		delay = plugin.getConfig().getLong("scoreboard.update-time");
		lobbyDelay = plugin.getConfig().getLong("scoreboard.lobby-update-time");
		async = plugin.getConfig().getBoolean("scoreboard.async-update");
		avoidErrors = true;
		//plugin.getConfig().getBoolean("scoreboard.avoid-errors");
		enhancedPerformance = plugin.getConfig().getBoolean("scoreboard.enhanced-performance");
		if(!enhancedPerformance) enhancedPerformance = LegionPractice.performanceMode;
		if(delay <= 0) delay = 1;
		if(lobbyDelay != delay) needsMode = true;
		if(async || enhancedPerformance) {
			task = runTaskTimerAsynchronously(plugin, 1, 1);
			new BukkitRunnable() {

				@Override
				public void run() {
					try {
						ScoreboardManager.getScoreboardManager().updateTags();
					}catch(Exception e) {}
					tpsCounter--;
					if(tpsCounter <= 0) {
						tps = TPSUtil.get1MinTPS();
						tpsCounter = 30;
					}
				}
			}.runTaskTimerAsynchronously(plugin, 20, 20);
		}
		else {
			runTaskTimer(plugin, 1, 1);
		}
		for(Player p : Bukkit.getOnlinePlayers()) {
			LegionPractice.getInstance();
			addToQueue(p, LegionPractice.random.nextInt((int) (delay+1)));
		}
	}

	public static long getDelay() {
		return delay;
	}

	public static void addUpdateNextTick(UUID uuid) {
		if(async || avoidErrors) {
			synchronized (update) {
				update.put(uuid, 1);
			}
		}
		else update.put(uuid, 1);
	}

	public static void addToQueue(Player p) {
		if(async || avoidErrors) {
			synchronized(update) {
				update.put(p.getUniqueId(), (int) delay);
			}
		}
		else update.put(p.getUniqueId(), (int) delay);
	}

	public static void addToQueue(Player p, int delay) {
		if(async || avoidErrors) {
			synchronized(update) {
				update.put(p.getUniqueId(), delay);
			}
		}
		else update.put(p.getUniqueId(), delay);
	}


	@Override
	public void run() {
		double x = 20-tps;
		if(x > 0) {
			skip -= x;
			if(skip < 0) {
				skip = 20;
				return;
			}
		}
		tick();
	}


	private void tick() {
		if(update.size() > 0) {
			Iterator<Entry<UUID, Integer>> iterator = getNextUp().entrySet().iterator();
			HashMap<UUID, Integer> pp = new HashMap<UUID, Integer>();
			while(iterator.hasNext()) {
				Entry<UUID, Integer> e = iterator.next();
				int i = e.getValue();
				i--;
				if(i <= 0) {
					Player p = Bukkit.getPlayer(e.getKey());
					if(p != null) {
						if(ScoreboardManager.getScoreboardManager().updateScoreboard(p) == PlaceholderMode.DEFAULT) {
							i = (int) lobbyDelay;
						}
						else {
							i = (int) delay;
						}
					}
					else iterator.remove();
				}
				pp.put(e.getKey(), i);
			}
			synchronized(update) {
				update.putAll(pp);
			}
		}
	}

	private HashMap<UUID, Integer> getNextUp() {
		synchronized (update) {
			return new HashMap<UUID, Integer>(update);
		}
	}
}