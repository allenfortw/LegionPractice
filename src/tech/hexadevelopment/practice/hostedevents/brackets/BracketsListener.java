package tech.hexadevelopment.practice.hostedevents.brackets;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.events.PvPEventEndEvent;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.hostedevents.sumo.SumoCommand;
import tech.hexadevelopment.practice.stats.PlayerStats;
import tech.hexadevelopment.practice.LegionPractice;

public class BracketsListener implements Listener {

	private LegionPractice plugin;

	public BracketsListener(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		if(BracketsCommand.brackets == null) return;
		if(BracketsCommand.brackets.getPlayers().containsKey(e.getPlayer().getName())) {
			e.getPlayer().setHealth(0);
			Brackets brks = BracketsCommand.brackets;
			if(brks.getP1() == null || brks.getP2() == null) return;
			if(!brks.getP1().equals(e.getPlayer().getName())
					&& !brks.getP2().equals(e.getPlayer().getName())) {
				BracketsCommand.brackets.getPlayers().remove(e.getPlayer().getName());
				plugin.clear(e.getPlayer(), true, true);
			}
		}
	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if(BracketsCommand.brackets == null || !(e.getEntity() instanceof Player)) return;
		Player p = (Player) e.getEntity();
		if(BracketsCommand.brackets.getPlayers().containsKey(p.getName())
				&& !Fight.isInFight(p, plugin)) {
			e.setCancelled(true);
		}
	}

	@EventHandler 
	public void onTeleport(PlayerTeleportEvent e) {
		if(!e.getFrom().getWorld().getName().equals(e.getTo().getWorld().getName())
				|| e.getFrom().distanceSquared(e.getTo()) > 10*10) {
			UUID uuid = e.getPlayer().getUniqueId();
			if(BracketsCommand.brackets != null && BracketsCommand.brackets.spectators.contains(uuid)) {
				BracketsCommand.brackets.spectators.remove(uuid);
			}
			else if(SumoCommand.sumo != null && SumoCommand.sumo.spectators.contains(uuid)) {
				SumoCommand.sumo.spectators.remove(uuid);
			}
		}
	}


	@EventHandler(priority=EventPriority.MONITOR)
	public void onDeath(PlayerDeathEvent e) {
		if(BracketsCommand.brackets == null) return;
		String name = e.getEntity().getName();
		if(name == null) return;
		if(BracketsCommand.brackets.getPlayers().containsKey(name)) {
			BracketsCommand.brackets.getPlayers().remove(name);
			if(BracketsCommand.brackets.getP1() == null ||
					BracketsCommand.brackets.getP2() == null) {
				return;
			}
			String o = null;
			if(BracketsCommand.brackets.getP1().equals(name)) {
				o = BracketsCommand.brackets.getP2();
			}
			else if(BracketsCommand.brackets.getP2().equals(name)) {
				o = BracketsCommand.brackets.getP1();
			}
			if(o == null) return;
			for(String s : BracketsCommand.brackets.getPlayers().keySet()) {
				Player pl = Bukkit.getPlayer(s);
				if(pl != null) {
					pl.sendMessage(plugin.translateMessage(pl, "brackets-slays").replace("<player1>", o).replace("<player2>", name));
				}
			}
			Player op = Bukkit.getPlayer(o);
			plugin.clear(op, false, true);
			plugin.clear(e.getEntity(), true, true);
			BracketsCommand.brackets.getPlayers().put(op.getName(), true);
			new BukkitRunnable() {

				@Override
				public void run() {
					if(e.getEntity() != null && BracketsCommand.brackets != null && BracketsCommand.brackets.enoughPlayers()) {
						BracketsCommand.brackets.lobby(e.getEntity());
						plugin.arenaPvP.giveSpawnItems(e.getEntity());
						BracketsCommand.brackets.spectators.add(e.getEntity().getUniqueId());
					}
				}
			}.runTaskLater(plugin, 5);
			if(BracketsCommand.brackets.enoughPlayers()) {
				BracketsCommand.brackets.nextFight();
				BracketsCommand.brackets.lobby(op);
			}
			else {
				plugin.clear(op, false, true);
				String w = null;
				for(String s : BracketsCommand.brackets.getPlayers().keySet()) {
					w = s;
				}
				for(Player pl : Bukkit.getOnlinePlayers()) {
					pl.sendMessage(plugin.translateMessage(pl, "brackets-winner").replace("<player>", w));
				}
				Player winner = Bukkit.getPlayer(w);
				PvPEventEndEvent event = new PvPEventEndEvent(BracketsCommand.brackets, winner);
				Bukkit.getPluginManager().callEvent(event);
				BracketsCommand.brackets.stop();
				UUID uuid = winner.getUniqueId();
				PlayerStats.getStats(uuid).setBracketsWins(PlayerStats.getStats(uuid).getBracketsWins()+1);
				if(w != null) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), plugin.getConfig().getString("brackets.winner-cmd").replace("<player>", w));
				}
			}
		}
	}
}
