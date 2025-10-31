package tech.hexadevelopment.practice.hostedevents.juggernaut;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.utils.ClickableMessage;

/**
 * Juggernaut pvpevent listener.
 * Cancels team ffa and handles deaths, quits and teleports for the juggernaut event.
 * @author Toppe5
 * @since 0.1
 */
public class JuggernautListener implements Listener {

	private LegionPractice plugin;

	public JuggernautListener(LegionPractice plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if(e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			if(e.getEntity().hasMetadata(Juggernaut.inJuggernaut)
					&& e.getDamager().hasMetadata(Juggernaut.inJuggernaut) && JuggernautCommand.juggernaut != null
					&& !JuggernautCommand.juggernaut.getJuggernaut().equals(((Player) e.getEntity()).getName())
					&& !JuggernautCommand.juggernaut.getJuggernaut().equals(((Player) e.getDamager()).getName())) {
				e.setCancelled(true);
			}
		}
		else if(e.getEntity() instanceof Player && e.getDamager() instanceof Projectile) {
			Projectile proj = (Projectile) e.getDamager();
			if(proj.getShooter() != null && proj.getShooter() instanceof Player
					&& ((Player) proj.getShooter()).hasMetadata(Juggernaut.inJuggernaut) && JuggernautCommand.juggernaut != null
					&& !JuggernautCommand.juggernaut.getJuggernaut().equals(((Player) e.getEntity()).getName())
					&& !JuggernautCommand.juggernaut.getJuggernaut().equals(((Player) proj.getShooter()).getName())) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		if(JuggernautCommand.juggernaut == null) return;
		if(JuggernautCommand.juggernaut.getJuggernaut().equalsIgnoreCase(e.getPlayer().getName())) {
			JuggernautCommand.juggernaut.eliminatedForLoggingOut();
		}
		if(e.getPlayer().hasMetadata(Juggernaut.inJuggernaut)) {
			e.getPlayer().removeMetadata(Juggernaut.inJuggernaut, plugin);
			plugin.clear(e.getPlayer(), true, true);
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onDeath(PlayerDeathEvent e) {
		if(JuggernautCommand.juggernaut == null) return;
		Player p = e.getEntity();
		if(JuggernautCommand.juggernaut.getJuggernaut() != null
				&& JuggernautCommand.juggernaut.getJuggernaut().equalsIgnoreCase(p.getName())) {
			e.getDrops().clear();
			if(p.getKiller() != null && p.getKiller() instanceof Player) {
				Player killer = p.getKiller();
				JuggernautCommand.juggernaut.eliminated(killer.getName());
			}
			else {
				JuggernautCommand.juggernaut.eliminated("*unknown*");
			}
		}
		else if(p.hasMetadata(Juggernaut.inJuggernaut)) {
			p.removeMetadata(Juggernaut.inJuggernaut, plugin);
			e.getDrops().clear();
			plugin.clear(p, true, true);
			e.getEntity().sendMessage(LegionPractice.getInstance().translateMessage(e.getEntity(), "juggernaut-join-back"));
			if(LegionPractice.getInstance().getConfig().getBoolean("clickable-messages")) {
				ClickableMessage.sendMessage(e.getEntity(), ChatColor.translateAlternateColorCodes('&', LegionPractice.getInstance().getConfig().getString("clickable-message")), "/juggernaut join");
			}
		}
	}
}
