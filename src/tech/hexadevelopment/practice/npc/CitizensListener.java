package tech.hexadevelopment.practice.npc;

import org.bukkit.entity.Damageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import tech.hexadevelopment.practice.LegionPractice;

public class CitizensListener implements Listener {


	@EventHandler
	public void onDespawn(NPCDespawnEvent e) {
		for(CitizensNPC npc : CitizensNPC.npcs) {
			if(e.getNPC().getUniqueId().equals(npc.getNPC().getUniqueId())) {
				CombatTask task = npc.combatTask;
				task.cancel();
				if(e.getReason() == DespawnReason.PENDING_RESPAWN) {
					new BukkitRunnable() {
						
						@Override
						public void run() {
							npc.combatTask = new CombatTask(npc, task.players, task.fight, task.difficulty);				
						}
					}.runTaskLater(LegionPractice.getInstance(), 5);
				}
				return;
			}
		}
	}
	
	@EventHandler
	public void onSpawn(NPCSpawnEvent e) {
		Damageable damageable = (Damageable) e.getNPC().getEntity();
		if(damageable.getHealth() <= 0) {
			e.setCancelled(true);
		}
	}
}