package tech.hexadevelopment.practice.utils;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.misc.LastInteractListener;
import tech.hexadevelopment.practice.LegionPractice;

public class Countdown extends BukkitRunnable{

	private List<String> players;
	private HashMap<String, Location> locations = new HashMap<String, Location>();
	private int count;
	private int startCount;
	private LegionPractice plugin;
	private boolean fight;
	private String sound, startSound;
	private BukkitTask stickSpawn;

	private Countdown(LegionPractice plugin, List<String> players, int count) {
		this.players = players;
		this.plugin = plugin;
		this.count = count;
		startCount = count;
		for(String s : players) {
			Player pl = PlayerUtil.getPlayer(s);
			if(pl != null) {
				Location l = pl.getLocation();
				locations.put(s, l);
				pl.setMetadata(LegionPractice.getInstance().NO_DAMAGE, new FixedMetadataValue(LegionPractice.getInstance(), true));
			}
		}
		sound = plugin.getConfig().getString("countdown-sound");
		startSound = plugin.getConfig().getString("match-start-sound");
	}

	@Override
	public void run() {
		if(players.size() > 0) {
			if(count > 0) {
				for(String name : players) {
					Player pl = PlayerUtil.getPlayer(name);
					if(pl != null) {
						boolean pf = Fight.isInFight(pl, plugin);
						if(!fight) fight = pf;
						pl.setMetadata(plugin.NO_DAMAGE, new FixedMetadataValue(plugin, true));
						if(count <= 10 || count % 5 == 0) {
							pl.sendMessage(plugin.translateMessage(pl, "countdown-message").replace("<seconds>", count + ""));
							SoundManager.playSound(pl, pl.getLocation(), sound, 1, 1);
						}
						if(fight && !pf) continue;
					}
				}
				count--;
			}
			else {
				for(String name : players) {
					Player pl = PlayerUtil.getPlayer(name);
					if(pl != null) {
						boolean pf = Fight.isInFight(pl, plugin);
						if(!fight) fight = pf;
						SoundManager.playSound(pl, pl.getLocation(), startSound, 1, 1);
						pl.sendMessage(plugin.translateMessage(pl, "countdown-go"));
						pl.removeMetadata(plugin.NO_DAMAGE, plugin);
						if(!LastInteractListener.hasConsumed(pl.getUniqueId(), startCount*1000)) {
							pl.updateInventory();
						}
						if(fight && !pf) continue;
						BattleKit kit = BattleKit.getCurrentKit(pl);
						if(kit != null && kit.isStickSpawn()) {
							synchronized(locations) {
								if(locations.containsKey(name)) {
									Location l = locations.get(name);
									Teleporter.syncTeleport(pl, l);
								}
							}
						}
					}
				}
				if(stickSpawn != null) {
					stickSpawn.cancel();
				}
				this.cancel();
			}
		}
		else this.cancel();
	}

	public static void startCountdown(List<String> players, int count) {
		LegionPractice plugin = LegionPractice.getInstance();
		Countdown cd = new Countdown(plugin, players, count);
		cd.runTaskTimerAsynchronously(plugin, 20, 20);
		cd.stickSpawn = new BukkitRunnable() {

			@Override
			public void run() {
				for(String name : players) {
					Player pl = PlayerUtil.getPlayer(name);
					if(pl != null) {
						boolean pf = Fight.isInFight(pl, plugin);
						if(!cd.fight) cd.fight = pf;
						if(cd.fight && !pf) continue;
						BattleKit kit = BattleKit.getCurrentKit(pl);
						if(kit != null && kit.isStickSpawn()) {
							if(cd.locations.containsKey(name)) {
								Location l = cd.locations.get(name);
								Location plLoc = pl.getLocation();
								plLoc.setY(0);
								Location clone = l.clone();
								clone.setY(0);
								if(plLoc.getWorld().getName().equals(clone.getWorld().getName()) && plLoc.distanceSquared(clone) > 0.1) {
									Teleporter.syncTeleport(pl, l);
								}
							}
							else {
								Location l = pl.getLocation();
								cd.locations.put(name, l);
							}
						}
					}
				}
			}
		}.runTaskTimerAsynchronously(plugin, 2, 2);
	}


	public static void startCountdown(List<String> players) {
		startCountdown(players, LegionPractice.getInstance().getConfig().getInt("countdown-time"));
	}
}
