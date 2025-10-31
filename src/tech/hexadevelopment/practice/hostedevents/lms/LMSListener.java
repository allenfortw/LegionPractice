package tech.hexadevelopment.practice.hostedevents.lms;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.events.PvPEventEndEvent;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.stats.PlayerStats;
import tech.hexadevelopment.practice.utils.Circle;

public class LMSListener implements Listener {


	private LegionPractice plugin;

	public LMSListener(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		e.getPlayer().removeMetadata(LMSCommand.lmsWaiting, plugin);
		if(LMSCommand.lms == null || LMSCommand.lms.getAlive() == null) return;
		if(LMSCommand.lms.hasStarted()) {
			if(LMSCommand.lms.getAlive().contains(e.getPlayer().getName())) {
				e.getPlayer().setHealth(0);
			}
		}
		if(LMSCommand.joined.contains(e.getPlayer().getName())) {
			if(LMSCommand.lms != null && LMSCommand.lms.hasStarted()) {
				e.getPlayer().setHealth(0);
			}
			for(String s : LMSCommand.joined) {
				Player sp = Bukkit.getPlayer(s);
				if(sp != null) {
					sp.sendMessage(plugin.translateMessage(sp, "lms-left").replace("<player>", e.getPlayer().getName()));
				}
			}
			LMSCommand.joined.remove(e.getPlayer().getName());
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onDeath(PlayerDeathEvent e) {
		if(LMSCommand.lms == null || LMSCommand.lms.getAlive() == null) return;
		Player p = e.getEntity();
		if(LMSCommand.lms.hasStarted()) {
			if(LMSCommand.lms.getAlive().contains(p.getName())) {
				p.removeMetadata(plugin.IN_FIGHT, plugin);
				for(String s : LMSCommand.lms.getAlive()) {
					Player sp = Bukkit.getPlayer(s);
					if(sp != null) {
						sp.sendMessage(plugin.translateMessage(sp, "lms-death-message").replace("<player>", p.getName()).replace("<alive>", (LMSCommand.lms.getAlive().size()-1) + ""));
					}
				}
				LMSCommand.joined.remove(p.getName());
				LMSCommand.lms.getAlive().remove(p.getName());
				Fight.setCurrentFight(p, null, plugin);
				p.removeMetadata(LMSCommand.lmsWaiting, plugin);
				if(plugin.getConfig().getBoolean("allow-spectating")) {
					Location loc = p.getLocation().add(0, 1, 0);
					plugin.clear(p, false, true);
					new BukkitRunnable() {

						@Override
						public void run() {
							if(p != null) {
								p.teleport(loc);
								plugin.getSpectatorHandler().addSpectator(p);
								LMSCommand.lms.spectators.add(p.getUniqueId());
								p.teleport(loc);		
							}
						}
					}.runTaskLater(plugin, 5);
				}
				else {
					plugin.clear(p, true, true);
				}
				if(LMSCommand.lms.getAlive().size() == 1) {
					for(String name : LMSCommand.lms.getAlive()) {
						Player winner = Bukkit.getPlayer(name);
						PvPEventEndEvent event = new PvPEventEndEvent(LMSCommand.lms, winner);
						Bukkit.getPluginManager().callEvent(event);
						new BukkitRunnable() {

							@Override
							public void run() {
								if(winner != null) {
									Fight.setCurrentFight(winner, null, plugin);
									plugin.clear(winner, true, true);
								}
								if(LMSCommand.lms != null) {
									LMSCommand.lms.setStarted(false);
									LMSCommand.lms.stop();
								}
							}
						}.runTaskLater(plugin, 20*plugin.getConfig().getInt("wait-before-teleport"));
						for(Player pl : Bukkit.getOnlinePlayers()) {
							pl.sendMessage(plugin.translateMessage(pl, "lms-winner").replace("<player>", name));
						}
						UUID uuid = winner.getUniqueId();
						PlayerStats.getStats(uuid).setLmsWins(PlayerStats.getStats(uuid).getLMSWins()+1);
						for(Location l : Circle.getCircle(winner.getLocation(), 5, 32)) {
							l.getWorld().spawnEntity(l, EntityType.FIREWORK);
						}
						break;
					}
					LMSCommand.lms.getAlive().clear();
				}
			}
		}
	}
}
