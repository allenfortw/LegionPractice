package tech.hexadevelopment.practice.misc;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.npc.CitizensNPC;
import tech.hexadevelopment.practice.LegionPractice;

public class BotSumoRunnable extends BukkitRunnable {

	private LegionPractice plugin;
	
	public BotSumoRunnable(LegionPractice plugin) {
		runTaskTimer(this.plugin = plugin, 100, LegionPractice.performanceMode ? 50 : 20);
	}
	
	
	@Override
	public void run() {
		for(CitizensNPC npc : CitizensNPC.npcs) {
			if(npc.getBukkitEntity() != null) {
				Location l = npc.getBukkitEntity().getLocation();
				if(l.getBlock().getType() == Material.STATIONARY_WATER && !npc.getBukkitEntity().isDead()) {
					Fight fight = Fight.getCurrentFight(npc.getBukkitEntity(), plugin);
					if(fight != null && fight.getKit() != null && fight.getKit().getName().contains("sumo")) {
						npc.getBukkitEntity().setHealth(0);
					}
				}
			}
		}
	}
}
