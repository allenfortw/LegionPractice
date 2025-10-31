package tech.hexadevelopment.practice.misc;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.Fight;

public class HungerListener implements Listener {


	private LegionPractice plugin;


	public HungerListener(LegionPractice plugin) {
		this.plugin = plugin;
	}


	@EventHandler
	public void onHunger(FoodLevelChangeEvent e) {
		if(e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if(!Fight.isInFight(p, plugin)) {
				e.setFoodLevel(20);
			}
		}
	}
}