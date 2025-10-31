package tech.hexadevelopment.practice.protection;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.hostedevents.brackets.BracketsCommand;
import tech.hexadevelopment.practice.hostedevents.koth.KOTHCommand;
import tech.hexadevelopment.practice.hostedevents.lms.LMSCommand;
import tech.hexadevelopment.practice.hostedevents.sumo.SumoCommand;

public class SpawnDamageListener implements Listener {


	private LegionPractice plugin;

	public SpawnDamageListener(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled=true)
	public void onDamage(EntityDamageEvent e) {
		Entity ent = e.getEntity();
		if(!ent.hasMetadata(plugin.IN_FIGHT)) {
			if(ent instanceof Player) {
				Player p = (Player) ent;
				if(!Fight.isInFight(p, plugin) && !PvPEvent.isInEvent(p)) {
					e.setCancelled(true);
				}
				else if(BracketsCommand.brackets != null && BracketsCommand.brackets.getPlayers().containsKey(p.getName())) {
					e.setCancelled(true);
				}
				else if(SumoCommand.sumo != null && SumoCommand.sumo.getPlayers().containsKey(p.getName())) {
					e.setCancelled(true);
				}
				else if(KOTHCommand.joined.contains(p.getUniqueId())) {
					e.setCancelled(true);
				}
				else if(p.hasMetadata(LMSCommand.lmsWaiting)) {
					e.setCancelled(true);
				}
			}
		}
	}
}
