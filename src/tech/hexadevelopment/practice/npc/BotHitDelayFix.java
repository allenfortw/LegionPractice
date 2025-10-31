package tech.hexadevelopment.practice.npc;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import tech.hexadevelopment.practice.battlekit.BattleKit;

public class BotHitDelayFix implements Listener {
	
	
	private static long minHitDelay = 490;
	
	private HashMap<UUID, Long> times = new HashMap<UUID, Long>();
	
	@EventHandler(ignoreCancelled=true,priority=EventPriority.NORMAL)
	public void onDamage(EntityDamageEvent e) {
		BattleKit kit = BattleKit.getCurrentKit(e.getEntity());
		if((kit != null && kit.isCombo()) || Bukkit.getPlayer(e.getEntity().getUniqueId()) != null) return;
		UUID uuid = e.getEntity().getUniqueId();
		if(times.containsKey(uuid)) {
			long l = times.get(uuid);
			if(System.currentTimeMillis()-l < minHitDelay) {
				e.setCancelled(true);
				return;
			}
		}
		times.put(uuid, System.currentTimeMillis());
	}
	
}
