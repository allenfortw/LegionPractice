package tech.hexadevelopment.practice.misc;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;

public class BottleDrop implements Listener {

	private LegionPractice plugin;


	public BottleDrop(LegionPractice plugin) {
		this.plugin = plugin;
	}


	@EventHandler(ignoreCancelled=true)
	public void onConsume(PlayerItemConsumeEvent e) {
		if(e.getItem().getType() == Material.POTION) {
			Player p = e.getPlayer();
			new BukkitRunnable() {

				@Override
				public void run() {
					if(p != null) {
						p.getInventory().remove(Material.GLASS_BOTTLE);
					}
				}
			}.runTaskLater(plugin, 1);
		}
	}
}
