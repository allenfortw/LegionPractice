package tech.hexadevelopment.practice.misc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;

public class DeathListener implements Listener {

	private LegionPractice plugin;
	public static String SKIP_RESPAWN_META = "LegionPracticeDoNotRespawn";
	private boolean disableMessage, lightning, disableKeepInventory;
	
	public DeathListener(LegionPractice plugin) {
		this.plugin = plugin;
		this.disableMessage = plugin.getConfig().getBoolean("death.disable-message");
		this.lightning = plugin.getConfig().getBoolean("death.lightning");
		this.disableKeepInventory = plugin.getConfig().getBoolean("death.disable-keep-inventory");
	}

	@EventHandler(priority=EventPriority.LOW)
	public void onDeath(PlayerDeathEvent e) {
		if(Bukkit.getPlayer(e.getEntity().getUniqueId()) != null && !e.getEntity().hasMetadata(SKIP_RESPAWN_META)) {
			new BukkitRunnable() {

				@Override
				public void run() {
					if(e.getEntity() != null) {
						e.getEntity().spigot().respawn();
						plugin.arenaPvP.lobby(e.getEntity());
					}
				}
			}.runTaskLater(plugin, 1);
		}
		if(disableMessage) {
			e.setDeathMessage(null);	
		}
		if(lightning) {
			for(Player p : e.getEntity().getWorld().getPlayers()) {
				//for some reason players aren't always in the same world
				if(p.getWorld().getName().equals(e.getEntity().getWorld().getName()) && p.getLocation().distanceSquared(e.getEntity().getLocation()) < 200*200) {
					plugin.getNMSAccessProvider().getAccess().strikeLightning(p, e.getEntity().getLocation());
				}
			}
		}
		if(disableKeepInventory) {
			//all versions don't have this
			try{
				e.setKeepInventory(false);
			}catch(Exception ex)  {}
		}
	}
}
