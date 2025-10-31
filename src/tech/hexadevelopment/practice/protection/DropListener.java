package tech.hexadevelopment.practice.protection;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.LegionPractice;

public class DropListener implements Listener {
	
	
	@EventHandler(ignoreCancelled=true)
	public void onDrop(PlayerDropItemEvent e) {
		if(Fight.getCurrentFight(e.getPlayer(), LegionPractice.getInstance()) == null && !PvPEvent.isInEvent(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

}
