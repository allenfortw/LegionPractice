package tech.hexadevelopment.practice.misc;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.botduel.BotDuel;
import tech.hexadevelopment.practice.fights.duel.BedWars;
import tech.hexadevelopment.practice.fights.duel.BestOf;
import tech.hexadevelopment.practice.fights.duel.Duel;

public class InstaVoidRunnable extends BukkitRunnable{


	private LegionPractice plugin;

	public InstaVoidRunnable(LegionPractice plugin) {
		if(LegionPractice.ASYNC_EVERYTHING) {
			runTaskTimerAsynchronously(this.plugin = plugin, 10, 10);
		}
		else {
			runTaskTimer(this.plugin = plugin, 10, 10);
		}
	}

	@Override
	public void run() {
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(!p.isDead() & (p.getLocation().getBlockY() < 0 || p.getLocation().getBlock().getType() == Material.STATIONARY_WATER)) {
				Fight fight = Fight.getCurrentFight(p, plugin);
				if(fight != null && !fight.hasEnded()) {
					if(p.getLocation().getBlock().getType() == Material.STATIONARY_WATER) {
						if(fight.getKit() == null || !fight.getKit().getName().contains("sumo")) {
							return;
						}
					}
					BestOf bo = null;
					if(fight instanceof Duel) {
						bo = ((Duel) fight).getBestOf();
						if(((Duel) fight).breaktime) return;
					}
					else if(fight instanceof BotDuel) {
						bo = ((BotDuel) fight).getBestOf();
						if(((BotDuel) fight).breaktime) return;
					}
					if(bo != null && !bo.endsNow(p.getUniqueId())) {
						if(!Bukkit.isPrimaryThread()) {
							new BukkitRunnable() {

								@Override
								public void run() {
									if(fight.getKit() != null && fight.getKit().isBedwars()) {
										BedWars.respawn(p);
									}
									else {
										fight.handleDeath(p);
									}
								}
							}.runTask(plugin);
						}
						else {
							if(fight.getKit() != null && fight.getKit().isBedwars()) {
								BedWars.respawn(p);
							}
							else {
								fight.handleDeath(p);
							}
						}
						return;
					}
					if(fight.getKit() != null && fight.getKit().isBedwars()) {
						if(!Bukkit.isPrimaryThread()) {
							new BukkitRunnable() {

								@Override
								public void run() {
									BedWars.respawn(p);
								}
							}.runTask(plugin);
						}
						else {
							BedWars.respawn(p);
						}
					}
					else {
						if(!Bukkit.isPrimaryThread()) {
							new BukkitRunnable() {

								@Override
								public void run() {
									p.setHealth(0);
								}
							}.runTask(plugin);
						}
						else {
							p.setHealth(0);
						}
					}
				}
			}
		}
	}
}
