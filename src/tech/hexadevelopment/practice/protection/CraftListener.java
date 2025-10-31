package tech.hexadevelopment.practice.protection;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.LegionPractice;

public class CraftListener implements Listener {


	@EventHandler(ignoreCancelled=true)
	public void onCraft(CraftItemEvent e) {
		if(e.getWhoClicked() instanceof Player) {
			Player p = (Player) e.getWhoClicked();
			if(Fight.getCurrentFight(p, LegionPractice.getInstance()) != null) {
				e.setCancelled(true);
			}
		}
	}

}
