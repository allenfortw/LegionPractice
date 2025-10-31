package tech.hexadevelopment.practice.protection;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;

public class MaxHitDelayListener implements Listener {
	
	
	private static long minHitDelay;
	
	private HashMap<UUID, Long> times = new HashMap<UUID, Long>();
	
	
	public MaxHitDelayListener() {
		minHitDelay = LegionPractice.getInstance().getConfig().getLong("hit-delay-limit");
	}
	
	@EventHandler(ignoreCancelled=true,priority=EventPriority.NORMAL)
	public void onDamage(EntityDamageEvent e) {
		BattleKit kit = BattleKit.getCurrentKit(e.getEntity());
		if(kit != null && kit.isCombo()) return;
		UUID uuid = e.getEntity().getUniqueId();
		if(times.containsKey(uuid)) {
			long l = times.get(uuid);
			if(System.currentTimeMillis()+2-l < minHitDelay) {
				e.setCancelled(true);
				return;
			}
		}
		times.put(uuid, System.currentTimeMillis());
	}
	
}
