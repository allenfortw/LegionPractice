package tech.hexadevelopment.practice.misc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.events.DuelEndEvent;
import tech.hexadevelopment.practice.stats.PlayerStats;
import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.LegionPracticeAPI;

public class EloReward implements Listener {


	private LegionPractice plugin;

	public EloReward(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onFightEnd(DuelEndEvent e) {
		Player p = e.getWinner();
		if(p == null || e.getFight() == null || e.getFight().getKit() == null || !e.getFight().getKit().isElo()) return;
		PlayerStats stats = LegionPracticeAPI.getPlayerStats(p);
		int oldGelo = stats.getGlobalElo();
		new BukkitRunnable() {

			@Override
			public void run() {
				if(p != null) {
					PlayerStats stats = LegionPracticeAPI.getPlayerStats(p);
					int gelo = stats.getGlobalElo();
					List<String> commands = new ArrayList<String>();
					int i = gelo;
					for(String s : plugin.getConfig().getConfigurationSection("elo-rewards").getKeys(false)) {
						int x = plugin.getConfig().getInt("elo-rewards." + s + ".elo");
						if(i >= x) {
							commands.clear();
							for(String c : plugin.getConfig().getStringList("elo-rewards." + s + ".commands")) {
								commands.add(c.replace("<level>", s));
							}
						}
					}
					if(oldGelo < i && !commands.isEmpty()) {
						for(String cmd : commands) {
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("<player>", p.getName())
									.replace("<player_elo>", gelo + "").replace("<required_elo>", i + ""));
						}
					}
				}
			}
		}.runTaskLater(plugin, 5);
	}

}
