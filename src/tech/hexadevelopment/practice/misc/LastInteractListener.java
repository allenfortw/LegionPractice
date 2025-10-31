package tech.hexadevelopment.practice.misc;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import tech.hexadevelopment.practice.LegionPractice;

public class LastInteractListener implements Listener {
	
	
	private static HashMap<UUID, Long> last = new HashMap<UUID, Long>();
	
	
	@EventHandler
	public void onComsume(PlayerItemConsumeEvent e) {
		last.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(e.getAction() == Action.RIGHT_CLICK_AIR) {
			last.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
		}
	}
	

	public static boolean hasConsumed(UUID uuid, long l) {
		if(last.containsKey(uuid)) {
			return last.get(uuid)+l > System.currentTimeMillis();
		}
		return LegionPractice.performanceMode;
	}
	
}
