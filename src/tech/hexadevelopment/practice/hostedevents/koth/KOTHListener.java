package tech.hexadevelopment.practice.hostedevents.koth;

import java.util.UUID;

import org.bukkit.Bukkit;
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

public class KOTHListener implements Listener {


	@EventHandler(priority=EventPriority.HIGHEST)
	public void onDeath(PlayerDeathEvent e) {
		KOTH koth = KOTHCommand.koth;
		Player p = e.getEntity();
		if(koth != null) {
			if(koth.getTeam1().getMembers().contains(p.getUniqueId())
					|| koth.getTeam2().getMembers().contains(p.getUniqueId())) {
				e.getDrops().clear();
				LegionPractice plugin = LegionPractice.getInstance();
				plugin.clear(p, true, true);
				koth.getTeam1().getMembers().remove(p.getUniqueId());
				koth.getTeam2().getMembers().remove(p.getUniqueId());
				if(plugin.getTagManager().COLORED_TAGS) {
					plugin.getTagManager().removeFromTeams(p);
				}
				if(plugin.getConfig().getBoolean("koth.anytime-join")) {
					p.sendMessage(plugin.translateMessage(p, "koth-join-back"));
					if(plugin.getConfig().getBoolean("clickable-messages")) {
						ClickableMessage.sendMessage(p, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("clickable-message")), "/koth join");
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		LegionPractice plugin = LegionPractice.getInstance();
		if(KOTHCommand.koth != null && (KOTHCommand.koth.getTeam1().getMembers().contains(p.getUniqueId())
				|| KOTHCommand.koth.getTeam2().getMembers().contains(p.getUniqueId()))) {
			KOTHCommand.koth.getTeam1().getMembers().remove(p.getUniqueId());
			KOTHCommand.koth.getTeam2().getMembers().remove(p.getUniqueId());
			plugin.clear(p, true, true);
			for(UUID s : KOTHCommand.joined) {
				Player sp = Bukkit.getPlayer(s);
				if(sp != null) {
					sp.sendMessage(plugin.translateMessage(sp, "koth-left").replace("<player>", p.getName()));
				}
			}
		}
		else if(KOTHCommand.joined.contains(p.getUniqueId())) {
			p.sendMessage(plugin.translateMessage(p, "koth-left"));
		}
		KOTHCommand.joined.remove(p.getUniqueId());
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if(e.getEntity() instanceof Player) {
			if(e.getDamager() instanceof Player) {
				Player p = (Player) e.getDamager();
				Player target = (Player) e.getEntity();
				if(isInSameTeam(p, target)) {
					e.setCancelled(true);
					return;
				}
			}
			if(e.getDamager() instanceof Projectile) {
				if(((Projectile) e.getDamager()).getShooter() instanceof Player) {
					Player p = (Player) ((Projectile) e.getDamager()).getShooter();
					Player target = (Player) e.getEntity();
					if(isInSameTeam(p, target)) {
						e.setCancelled(true);
					}
				}
			}
		}
	}
	
	private boolean isInSameTeam(Player p1, Player p2) {
		if(KOTHCommand.koth != null) {
			return (KOTHCommand.koth.getTeam1().getMembers().contains(p1.getUniqueId())
					&& KOTHCommand.koth.getTeam1().getMembers().contains(p2.getUniqueId())
							|| (KOTHCommand.koth.getTeam2().getMembers().contains(p1.getUniqueId())
									&& KOTHCommand.koth.getTeam2().getMembers().contains(p2.getUniqueId())));
		}
		return false;
	}
}
