package tech.hexadevelopment.practice.hostedevents.sumo;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.events.PvPEventEndEvent;
import tech.hexadevelopment.practice.fights.Fight;

public class SumoListener implements Listener {

	private LegionPractice plugin;

	public SumoListener(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		if(SumoCommand.sumo == null) return;
		if(SumoCommand.sumo.getPlayers().containsKey(e.getPlayer().getName())) {
			e.getPlayer().setHealth(0);
			Sumo brks = SumoCommand.sumo;
			if(brks.getP1() == null || brks.getP2() == null) return;
			if(!brks.getP1().equals(e.getPlayer().getName())
					&& !brks.getP2().equals(e.getPlayer().getName())) {
				SumoCommand.sumo.getPlayers().remove(e.getPlayer().getName());
				plugin.clear(e.getPlayer(), true, true);
			}
		}
	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if(SumoCommand.sumo == null || !(e.getEntity() instanceof Player)) return;
		Player p = (Player) e.getEntity();
		if(SumoCommand.sumo.getPlayers().containsKey(p.getName())
				&& !Fight.isInFight(p, plugin)) {
			e.setCancelled(true);
		}
	}


	@EventHandler(priority=EventPriority.MONITOR)
	public void onDeath(PlayerDeathEvent e) {
		if(SumoCommand.sumo == null) return;
		String name = e.getEntity().getName();
		if(name == null) return;
		if(SumoCommand.sumo.getPlayers().containsKey(name)) {
			SumoCommand.sumo.getPlayers().remove(name);
			if(SumoCommand.sumo.getP1() == null ||
					SumoCommand.sumo.getP2() == null) {
				return;
			}
			String o = null;
			if(SumoCommand.sumo.getP1().equals(name)) {
				o = SumoCommand.sumo.getP2();
			}
			else if(SumoCommand.sumo.getP2().equals(name)) {
				o = SumoCommand.sumo.getP1();
			}
			if(o == null) return;
			for(String s : SumoCommand.sumo.getPlayers().keySet()) {
				Player pl = Bukkit.getPlayer(s);
				if(pl != null) {
					pl.sendMessage(plugin.translateMessage(pl, "sumo-slays").replace("<player1>", o).replace("<player2>", name));
				}
			}
			Player op = Bukkit.getPlayer(o);
			plugin.clear(op, false, true);
			plugin.clear(e.getEntity(), true, true);
			SumoCommand.sumo.getPlayers().put(op.getName(), true);
			new BukkitRunnable() {

				@Override
				public void run() {
					if(e.getEntity() != null && SumoCommand.sumo != null && SumoCommand.sumo.enoughPlayers()) {
						SumoCommand.sumo.lobby(e.getEntity());
						plugin.arenaPvP.giveSpawnItems(e.getEntity());
						SumoCommand.sumo.spectators.add(e.getEntity().getUniqueId());
					}
				}
			}.runTaskLater(plugin, 5);
			if(SumoCommand.sumo.enoughPlayers()) {
				SumoCommand.sumo.nextFight();
				SumoCommand.sumo.lobby(op);
			}
			else {
				plugin.clear(op, false, true);
				String w = null;
				for(String s : SumoCommand.sumo.getPlayers().keySet()) {
					w = s;
				}
				for(Player pl : Bukkit.getOnlinePlayers()) {
					pl.sendMessage(plugin.translateMessage(pl, "sumo-winner").replace("<player>", w));
				}
				SumoCommand.sumo.stop();
				PvPEventEndEvent event = new PvPEventEndEvent(SumoCommand.sumo, op);
				Bukkit.getPluginManager().callEvent(event);
				/*
				Player winner = Bukkit.getPlayer(w);
				UUID uuid = winner.getUniqueId();
				PlayerStats.getStats(uuid).setSumoWins(PlayerStats.getStats(uuid).getSumoWins()+1);
				 */
				if(w != null) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), plugin.getConfig().getString("sumo.winner-cmd").replace("<player>", w));
				}
			}
		}
	}
}
