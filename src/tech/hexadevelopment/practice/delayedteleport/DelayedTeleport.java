package tech.hexadevelopment.practice.delayedteleport;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.utils.PlayerUtil;
import tech.hexadevelopment.practice.LegionPractice;

public class DelayedTeleport extends BukkitRunnable{

	public static String TELEPORTING_TAG = "LegionPracticeDelayedTeleport";

	private String player;
	private Location location;
	private Location to;
	private int counter;
	private LegionPractice plugin;
	private DelayedAction action;

	public DelayedTeleport(LegionPractice plugin, int seconds, Player p, Location to){
		this.player = p.getName();
		this.location = p.getLocation();
		this.to = to;
		this.plugin = plugin;
		this.counter = seconds;
		p.sendMessage(plugin.translateMessage(p, "teleporting").replace("<seconds>", Integer.toString(seconds)));
		runTaskTimer(plugin, 20L, 20L);
	}
	
	public DelayedTeleport(LegionPractice plugin, int seconds, Player p, Location to, DelayedAction action){
		this.player = p.getName();
		this.location = p.getLocation();
		this.to = to;
		this.plugin = plugin;
		this.counter = seconds;
		this.action = action;
		p.sendMessage(plugin.translateMessage(p, "teleporting").replace("<seconds>", Integer.toString(seconds)));
		runTaskTimer(plugin, 20L, 20L);
	}

	public void run(){
		Player p = PlayerUtil.getPlayer(player);
		if (p != null && p.getLocation().getWorld().getName().equals(location.getWorld().getName())) {
			if (p.getLocation().distance(location) < 1.0D) {
				counter -= 1;
				if (counter == 0) {
					if(action != null) {
						action.onTeleport();
					}
					p.teleport(to);
					cancel();
				}
				return;
			}
		}
		cancel();
	}
	
	public void cancelTeleport() {
		Player p = Bukkit.getPlayer(player);
		if (p != null) {
			p.sendMessage(plugin.translateMessage(p, "teleport-cancelled"));
			p.removeMetadata(TELEPORTING_TAG, plugin);
		}
	}
}
