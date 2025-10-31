package tech.hexadevelopment.practice.stats;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.Fight;

public class StatsDeathListener implements Listener{

	private static String lastDeath = "LegionPracticeLastDeathTime";
	protected LegionPractice plugin;
	public StatsDeathListener(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onDeath(PlayerDeathEvent e) {
		if(Bukkit.getPlayer(e.getEntity().getUniqueId()) == null) return;
		//fix double death bug with some other plugins
		if(e.getEntity().hasMetadata(lastDeath)) {
			MetadataValue m = plugin.getMetadata(e.getEntity(), lastDeath);
			if(m != null && m.value() != null) {
				if(m.asLong()+200 > System.currentTimeMillis()) {
					return;
				}
			}
		}
		e.getEntity().setMetadata(lastDeath, new FixedMetadataValue(plugin, System.currentTimeMillis()));
		UUID uuidE = e.getEntity().getUniqueId();
		PlayerStats.getStats(uuidE).setDeaths(PlayerStats.getStats(uuidE).getDeaths()+1);
		Player kill = e.getEntity().getKiller();
		if (kill != null){
			if (kill.equals(e.getEntity())) return;
			if(e.getEntity().getKiller() instanceof Player) {
				Player killer = e.getEntity().getKiller();
				if(Bukkit.getPlayer(killer.getUniqueId()) == null) return;
				UUID uuid = killer.getUniqueId();
				PlayerStats.getStats(uuid).setKills(PlayerStats.getStats(uuid).getKills()+1);
			}
			else if(e.getEntity().getKiller() instanceof Projectile) {
				Projectile projectile = (Projectile) e.getEntity().getKiller();
				if(projectile.getShooter() != null && projectile.getShooter() instanceof Player) {
					Player ent = (Player) projectile.getShooter();
					PlayerStats.getStats(ent.getUniqueId()).setKills(PlayerStats.getStats(ent.getUniqueId()).getKills()+1);
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onQuit(PlayerQuitEvent e) {
		Fight fight = Fight.getCurrentFight(e.getPlayer(), plugin);
		if(fight != null && e.getPlayer().isDead()) {
			PlayerStats.getStats(e.getPlayer().getUniqueId()).setDeaths(PlayerStats.getStats(e.getPlayer().getUniqueId()).getDeaths()+1);
		}
	}
}
