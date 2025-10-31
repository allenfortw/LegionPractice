package tech.hexadevelopment.practice.arena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import tech.hexadevelopment.practice.utils.LocationUtil;
import tech.hexadevelopment.practice.utils.PotionEffectUtil;
import tech.hexadevelopment.practice.utils.SoundManager;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.LegionPractice;

public class ArenaLeaveRunnable extends BukkitRunnable{

	private LegionPractice plugin;
	public static String IN_STORM = "LegionPracticeInStorm";

	public ArenaLeaveRunnable(LegionPractice plugin) {
		runTaskTimerAsynchronously(this.plugin = plugin, 10*20, 10*20);
	}

	@Override
	public void run() {
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(!p.hasMetadata(IN_STORM)) {
				Fight fight = Fight.getCurrentFight(p, plugin);
				if(fight != null && fight.getArena() != null) {
					Arena ar = fight.getArena();
					if(ar.getCorner1() != null && ar.getCorner2() != null && !LocationUtil.isInregion(p.getLocation(), ar.getCorner1(), ar.getCorner2())) {
						new ForcefieldDamage(p);
					}
				}
			}
		}
	}

	private class ForcefieldDamage extends BukkitRunnable {

		private Player p;
		int counter = 0;

		public ForcefieldDamage(Player p) {
			this.p = p;
			p.setMetadata(IN_STORM, new FixedMetadataValue(plugin, true));
			runTaskTimer(plugin, 10, 10);
		}

		@Override
		public void run() {
			if(p != null && !p.isDead()) {
				Fight fight = Fight.getCurrentFight(p, plugin);
				if(fight != null && !fight.hasEnded()) {
					Arena ar = fight.getArena();
					if(ar.getCorner1() != null && ar.getCorner2() != null && !LocationUtil.isInregion(p.getLocation(), ar.getCorner1(), ar.getCorner2())) {
						counter++;
						if(counter > 60 || counter % 2 == 0) {
							if(counter > 5) {
								int i = 1;
								if(p.hasPotionEffect(PotionEffectType.REGENERATION)) {
									i += PotionEffectUtil.getPotionEffectDurability(p, PotionEffectType.REGENERATION);
								}
								p.damage(i);
								if(p.getPlayerWeather() == WeatherType.CLEAR) {
									p.setPlayerWeather(WeatherType.DOWNFALL);
								}
							}
							if(counter % 5 == 0 || counter == 2) {
								p.sendMessage(ChatColor.RED + "You are not allowed to leave the arena!");
							}
							if(counter > 15 && LegionPractice.random.nextInt(3) == 0) {
								p.setVelocity(p.getVelocity().add(new Vector(LegionPractice.random.nextDouble()-0.5, LegionPractice.random.nextDouble()-0.5, LegionPractice.random.nextDouble()-0.5)));
							}
							if(counter % 3 == 0 && LegionPractice.random.nextBoolean()) {
								Location loc =  LocationUtil.randomLocation(p.getLocation(), 24, false);
								plugin.getNMSAccessProvider().getAccess().strikeLightning(p, loc);
								SoundManager.playSound(p, loc, "AMBIENCE_THUNDER", 2, 1);
							}
						}
						return;
					}
					else {
						p.removeMetadata(IN_STORM, plugin);
						p.setPlayerWeather(WeatherType.CLEAR);
					}
				}
			}
			p.removeMetadata(IN_STORM, plugin);
			p.setPlayerWeather(WeatherType.CLEAR);
			this.cancel();
		}
	}
}
