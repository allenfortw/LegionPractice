package tech.hexadevelopment.practice.misc;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import tech.hexadevelopment.practice.battlekit.BattleKit;

public class RegenListener implements Listener{

	@EventHandler
	public void onRegen(EntityRegainHealthEvent e){
		if (((e.getEntity() instanceof Player)) && 
				(e.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED)){
			Player p = (Player)e.getEntity();
			BattleKit kit = BattleKit.getCurrentKit(p);
			if ((kit != null) && (kit.getName() != null) && (kit.getName().toLowerCase().contains("uhc"))) {
				e.setCancelled(true);
			}
		}
	}
}
