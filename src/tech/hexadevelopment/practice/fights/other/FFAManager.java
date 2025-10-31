package tech.hexadevelopment.practice.fights.other;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.delayedteleport.DelayedAction;
import tech.hexadevelopment.practice.delayedteleport.DelayedTeleport;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.LegionPractice;

public class FFAManager implements Listener {


	private LegionPractice plugin;
	private boolean reseting;
	private HashMap<Arena, FFAFight> ffaFights = new HashMap<Arena, FFAFight>();
	private long lastReset;
	private long delay;


	public FFAManager(LegionPractice plugin) {
		this.plugin = plugin;
		delay = plugin.getConfig().getLong("ffa-reset-delay")*20*60;
		new FFAResetTask().runTaskTimer(plugin, delay, delay);
		lastReset = System.currentTimeMillis();
		for(Arena arena : plugin.arenas) {
			if(arena.isFFA()) {
				if(!arena.getKits().isEmpty()) {
					FFAFight fight = new FFAFight(plugin);
					fight.setArena(arena);
					fight.setKit(BattleKit.getKit(arena.getKits().get(0)));
					if(fight.getKit() != null) {
						fight.start();
						ffaFights.put(arena, fight);
					}
					else {
						Bukkit.getLogger().warning("Could not start FFA fight because the kit is not valid. Arena: " + arena.getName()); 
					}
				}
				else {
					Bukkit.getLogger().warning("Could not start FFA fight because the kit is not valid. Arena: " + arena.getName()); 
				}
			}
		}
	}


	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onFallDamage(EntityDamageEvent e) {
		if(e.getCause() != null && e.getCause() == DamageCause.FALL && reseting) {
			Fight fight = Fight.getCurrentFight(e.getEntity(), plugin);
			if(fight != null && fight instanceof FFAFight) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onCommand(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		Fight fight = Fight.getCurrentFight(p, plugin);
		if(fight != null && fight instanceof FFAFight) {
			if(e.getMessage().equalsIgnoreCase("/spawn")) {
				new DelayedTeleport(plugin, plugin.getConfig().getInt("wait-before-leaving-ffa-arena"), p, plugin.arenaPvP.getLobby(), new DelayedAction() {
					
					@Override
					public void onTeleport() {
						Fight.setCurrentFight(p, null, plugin);
						plugin.clear(p, true, true);
					}
				});
				e.setCancelled(true);
				return;
			}
		}
		for(Entry<Arena, FFAFight> set : ffaFights.entrySet()) {
			if(e.getMessage().equalsIgnoreCase("/"+ set.getKey().getName())) {
				if(PvPEvent.isInEvent(p) || p.hasMetadata(plugin.IN_FIGHT) || Fight.getCurrentFight(p, plugin) != null) {
					p.sendMessage(plugin.translateMessage(p, "can-not-do-command"));
				}
				else {
					set.getValue().joinPlayer(p);
				}
				e.setCancelled(true);
				return;
			}
		}
	}
	
	public long nextReset() {
		return (delay*50+30000)-(System.currentTimeMillis()-lastReset);
	}

	private class FFAResetTask extends BukkitRunnable {

		private BukkitTask resetingTask;
		
		@Override
		public void run() {

			resetingTask = new BukkitRunnable() {

				int counter = 30;

				@Override
				public void run() {
					for(Entry<Arena, FFAFight> fight : ffaFights.entrySet()) {
						if(!fight.getKey().isUsing()) {
							fight.getKey().setUsing(true);
						}
					}
					if(counter == 10 || counter <= 5 || counter == 15 || counter == 20 || counter == 30) {
						if(counter > 0) {
							for(Player p : Bukkit.getOnlinePlayers()) {
								Fight fight = Fight.getCurrentFight(p, plugin);
								if(fight != null && fight instanceof FFAFight && fight.getArena().isBuild()) {
									p.sendMessage(plugin.translateMessage(p, "ffa-arena-reset").replace("<time>", counter + ""));
								}
							}
						}
						else {
							if(counter == 0) {
								reseting = true;
								for(Entry<Arena, FFAFight> fight : ffaFights.entrySet()) {
									if(fight.getKey().isBuild()) {
										fight.getValue().setEnded(true);
										fight.getKey().rollbackArena(fight.getValue());
									}
								}
							}
							else if(!reseting()) {
								new BukkitRunnable() {

									@Override
									public void run() {
										//make sure the arena resets
										//might cause little lag but..
										for(Entry<Arena, FFAFight> fight : ffaFights.entrySet()) {
											if(fight.getKey().isBuild()) {
												fight.getValue().setEnded(true);
												fight.getKey().setCurrentFight(fight.getValue());
												fight.getKey().quickRollback();
											}
										}
										for(Entry<Arena, FFAFight> fight : ffaFights.entrySet()) {
											if(fight.getKey().isBuild()) {
												fight.getValue().setEnded(false);
											}
										}
										reseting = false;
									}
								}.runTaskLater(plugin, 20*10);
								resetingTask.cancel();
							}
							//reseting taking too much time
							//something went wrong (probably)
							if(counter < -120) {
								reseting = false;
								resetingTask.cancel();
							}
						}
					}
					counter--;
				}
			}.runTaskTimer(plugin, 20, 20);
		}
	}
	
	public void onDisable() {
		for(Entry<Arena, FFAFight> fight : ffaFights.entrySet()) {
			if(fight.getKey().isBuild()) {
				fight.getValue().setEnded(true);
				fight.getKey().setCurrentFight(fight.getValue());
				fight.getKey().quickRollback();
			}
		}
	}

	private boolean reseting() {
		for(Entry<Arena, FFAFight> fight : ffaFights.entrySet()) {
			if(fight.getKey().isBuild()) {
				if(fight.getKey().needsRollback()) {
					return true;
				}
			}
		}
		return false;
	}
}
