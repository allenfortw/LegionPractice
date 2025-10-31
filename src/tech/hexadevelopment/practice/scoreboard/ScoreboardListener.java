package tech.hexadevelopment.practice.scoreboard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import tech.hexadevelopment.practice.LegionPractice;

public class ScoreboardListener implements Listener {

	private static HashMap<UUID, Long> joined = new HashMap<UUID, Long>();
	private boolean teleportUpdate;

	public ScoreboardListener() {
		teleportUpdate = LegionPractice.getInstance().getConfig().getBoolean("scoreboard.teleport-update");	
	}

	public static boolean tooEarly(Player p) {
		return tooEarly(p.getUniqueId());
	}

	public static boolean tooEarly(UUID uuid) {
		if(joined.containsKey(uuid)) {
			if(joined.get(uuid)+500 > System.currentTimeMillis()) {
				return true;
			}
			else joined.remove(uuid); 
		}
		return false;
	}

	@EventHandler(ignoreCancelled=true)
	public void onTeleport(PlayerTeleportEvent e) {
		ScoreboardManager.getScoreboardManager().handleDisabledWorlds(e);
		if(!teleportUpdate || Bukkit.getPlayer(e.getPlayer().getUniqueId()) == null
				|| tooEarly(e.getPlayer())) return;
		if(Bukkit.isPrimaryThread()) {
			ScoreboardUpdater.addUpdateNextTick(e.getPlayer().getUniqueId());
		}
		else {
			new BukkitRunnable() {

				@Override
				public void run() {
					if(e.getPlayer() != null) {
						ScoreboardUpdater.addUpdateNextTick(e.getPlayer().getUniqueId());				
					}
				}
			}.runTask(LegionPractice.getInstance());
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		joined.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
		new BukkitRunnable() {

			@Override
			public void run() {
				if(e.getPlayer() != null) {
					joined.remove(e.getPlayer().getUniqueId());
					ScoreboardUpdater.addUpdateNextTick(e.getPlayer().getUniqueId());
				}
			}
		}.runTaskLater(LegionPractice.getInstance(), 8);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		if(ScoreboardManager.getScoreboardManager().getScoreboards().containsKey(e.getPlayer().getUniqueId())) {
			Board board = ScoreboardManager.getScoreboardManager().getScoreboards().get(e.getPlayer().getUniqueId());
			if(board != null) {
				HealthBar.unregisterHealthBar(e.getPlayer());
				for(Team team : new HashSet<>(board.getScoreboard().getTeams())) {
					team.unregister();
				}
			}
		}
		ScoreboardManager.getScoreboardManager().getScoreboards().remove(e.getPlayer().getUniqueId());
	}

}
