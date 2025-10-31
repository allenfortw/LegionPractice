package tech.hexadevelopment.practice.delayedteleport;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.MetadataValue;

import tech.hexadevelopment.practice.LegionPractice;

public class DelayedTeleportListener implements Listener {
	
	
	private LegionPractice plugin;
	
	
	public DelayedTeleportListener(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
	public void onDamage(EntityDamageEvent e) {
		if ((e.getEntity() instanceof Player)) {
			Player p = (Player)e.getEntity();
			if (p.hasMetadata(DelayedTeleport.TELEPORTING_TAG)) {
				MetadataValue m = this.plugin.getMetadata(p, DelayedTeleport.TELEPORTING_TAG);
				if ((m != null) && (m.value() != null) && ((m.value() instanceof DelayedTeleport))) {
					DelayedTeleport dt = (DelayedTeleport)m.value();
					dt.cancelTeleport();
				}
			}
		}
	}
}