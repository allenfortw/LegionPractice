package tech.hexadevelopment.practice.misc;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.botduel.BotDuel;
import tech.hexadevelopment.practice.fights.duel.BedWars;
import tech.hexadevelopment.practice.fights.duel.BestOf;
import tech.hexadevelopment.practice.fights.duel.Duel;

public class InstaVoidListener implements Listener {

	private LegionPractice plugin;

	public InstaVoidListener(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onMove(PlayerMoveEvent e) {
		if(e.getTo().getBlockY() < e.getFrom().getBlockY() && (e.getTo().getBlockY() < 0 || e.getTo().getBlock().getType() == Material.STATIONARY_WATER)
				&& !e.getPlayer().isDead() && Fight.isInFight(e.getPlayer(), plugin)) {
			Fight fight = Fight.getCurrentFight(e.getPlayer(), plugin);
			if(fight != null && !fight.hasEnded()) {
				if(e.getTo().getBlock().getType() == Material.STATIONARY_WATER) {
					if(fight.getKit() == null || !fight.getKit().getName().contains("sumo")) {
						return;
					}
				}
				BestOf bo = null;
				if(fight instanceof Duel) {
					bo = ((Duel) fight).getBestOf();
					if(((Duel) fight).breaktime) return;
				}
				else if(fight instanceof BotDuel && Bukkit.getPlayer(e.getPlayer().getUniqueId()) != null) {
					bo = ((BotDuel) fight).getBestOf();
					if(((BotDuel) fight).breaktime) return;
				}
				if(bo != null && bo.getRounds() > 1 && !bo.endsNow(e.getPlayer().getUniqueId())) {
					if(fight.getKit() != null && fight.getKit().isBedwars()) {
						BedWars.respawn(e.getPlayer());
					}
					else {
						fight.handleDeath(e.getPlayer());
					}
					return;
				}
				if(fight.getKit() != null && fight.getKit().isBedwars()) {
					BedWars.respawn(e.getPlayer());
				}
				else {
					e.getPlayer().setHealth(0);
				}
			}
		}
	}
}