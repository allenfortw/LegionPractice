package tech.hexadevelopment.practice.misc;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;

public class VoidSpawn implements Listener {

	private LegionPractice plugin;

	public VoidSpawn(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if(e.getTo().getBlockY() < -5 && e.getTo().getBlockY() < e.getFrom().getBlockY()
				&& !p.isDead() && !Fight.isInFight(p, plugin)
				&& !PvPEvent.isInEvent(p) && !plugin.getSpectatorHandler().isSpectator(p)) {
			plugin.arenaPvP.lobby(p);
		}
	}
}