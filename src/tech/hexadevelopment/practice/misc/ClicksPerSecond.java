package tech.hexadevelopment.practice.misc;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;

public class ClicksPerSecond implements Listener{


	private static HashMap<UUID, Integer> lastCPS = new HashMap<UUID, Integer>();

	private static String time = "LegionPracticeCPSTimestamp";
	private static String tag = "LegionPracticeCPSCount";

	private LegionPractice plugin;


	public ClicksPerSecond(LegionPractice plugin) {
		this.plugin = plugin;
		new BukkitRunnable() {

			@Override
			public void run() {
				for(Player p : Bukkit.getOnlinePlayers()) {
					MetadataValue m = plugin.getMetadata(p, tag);
					if(m != null && m.value() != null) {
						MetadataValue m2 = plugin.getMetadata(p, time);
						if(m2 != null && m2.value() != null) {
							int cps = m.asInt();
							if(m2.asLong() < System.currentTimeMillis()) {
								lastCPS.put(p.getUniqueId(), 0);
							}
						}
					}
				}
			}
		}.runTaskTimer(plugin, 40, LegionPractice.performanceMode ? 40 : 20);
	}


	@EventHandler
	public void onClick(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if(e.getAction().equals(Action.LEFT_CLICK_AIR)) {
			if(p.getItemInHand() != null && p.getItemInHand().getType() == Material.FISHING_ROD) return;
			if(p.hasMetadata(tag) && p.hasMetadata(time)) {
				MetadataValue m = plugin.getMetadata(p, tag);
				if(m != null && m.value() != null) {
					MetadataValue m2 = plugin.getMetadata(p, time);
					if(m2 != null && m2.value() != null) {
						int cps = m.asInt();
						if(m2.asLong() <= System.currentTimeMillis()) {
							lastCPS.put(p.getUniqueId(), cps);
							p.removeMetadata(tag, plugin);
							p.removeMetadata(time, plugin);
						}
						else p.setMetadata(tag, new FixedMetadataValue(plugin, cps+1));
						return;
					}
				}
			}
			p.setMetadata(tag, new FixedMetadataValue(plugin, 1));
			p.setMetadata(time, new FixedMetadataValue(plugin, System.currentTimeMillis()+1000));
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		e.getPlayer().removeMetadata(time, plugin);
		e.getPlayer().removeMetadata(tag, plugin);
	}

	public static int getCPS(Player p) {
		if(!Bukkit.isPrimaryThread()) {
			synchronized(lastCPS) {
				return lastCPS.getOrDefault(p.getUniqueId(), 0);
			}
		}
		return lastCPS.getOrDefault(p.getUniqueId(), 0);
	}
}
