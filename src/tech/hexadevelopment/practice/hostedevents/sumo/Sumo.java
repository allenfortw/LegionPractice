package tech.hexadevelopment.practice.hostedevents.sumo;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.events.PvPEventEndEvent;
import tech.hexadevelopment.practice.events.PvPEventStartEvent;
import tech.hexadevelopment.practice.fights.duel.Duel;
import tech.hexadevelopment.practice.hostedevents.brackets.Brackets;

public class Sumo extends Brackets{


	public Sumo(LegionPractice plugin) {
		super(plugin);
	}

	
	@Override
	public void start() {
		PvPEventStartEvent event = new PvPEventStartEvent(this);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) return;
		super.startEvent();
	}

	@Override
	public void stop() {
		PvPEventEndEvent event = new PvPEventEndEvent(this, null);
		Bukkit.getPluginManager().callEvent(event);
		totalPlayers = 0;
		started = false;
		new BukkitRunnable() {
			
			@Override
			public void run() {
				for(String s : players.keySet()) {
					Player pl = Bukkit.getPlayer(s);
					if(pl != null) {
						pl.sendMessage(plugin.translateMessage(pl, "event-stopped"));
						if(plugin.getSpectatorHandler().isSpectator(pl)) {
							plugin.getSpectatorHandler().removeSpectator(pl, true);
						}
						plugin.clear(pl, true, true);
						new BukkitRunnable() {
							
							@Override
							public void run() {
								if(pl != null) {
									plugin.clear(pl, true, true);
								}
							}
						}.runTaskLater(plugin, 40);
					}
				}
				players.clear();
			}
		}.runTaskLater(plugin, 20*plugin.getConfig().getInt("wait-before-teleport"));
		p1 = null;
		p2 = null;
		for(UUID uuid : spectators) {
			Player pl = Bukkit.getPlayer(uuid);
			if(pl != null) {
				pl.sendMessage(plugin.translateMessage(pl, "event-stopped"));
				if(LegionPractice.disabling) {
					if(plugin.getSpectatorHandler().isSpectator(pl)) {
						plugin.getSpectatorHandler().removeSpectator(pl, true);
					}
					plugin.clear(pl, true, true);
				}
				else {
					new BukkitRunnable() {

						@Override
						public void run() {
							if(pl != null) {
								if(plugin.getSpectatorHandler().isSpectator(pl)) {
									plugin.getSpectatorHandler().removeSpectator(pl, true);
								}
								plugin.clear(pl, true, true);
							}
						}
					}.runTaskLater(plugin, 20*plugin.getConfig().getInt("wait-before-teleport"));
				}
			}
		}
		SumoCommand.sumo = null;
		SumoCommand.starting = false;
	}

	@Override
	public void nextFight() {
		new BukkitRunnable() {

			@Override
			public void run() {
				if(!enoughPlayers()) {
					cancel();
					return;
				}
				if(arena != null && arena.needsRollback()) return;
				int x = 0;
				String p = null;
				for(String z : players.keySet()) {
					if(!players.get(z)) {
						x++;
						p = z;
					}
				}
				if(x < 2) {
					HashSet<String> set = new HashSet<String>();
					for(String a : players.keySet()) {
						if(a != null && Bukkit.getPlayer(a) != null) {
							set.add(a);
						}
					}
					players.clear();
					for(String a : set) {
						players.put(a, false);
					}
				}
				if(p == null) {
					for(String pl1 : players.keySet()) {
						if(!players.get(pl1)) {
							p = pl1;
							if(LegionPractice.random.nextInt(players.size()) == 0) {
								break;
							}
						}
					}
				}
				players.put(p, true);
				String p2 = null;
				for(String pl2 : players.keySet()) {
					if(!players.get(pl2)) {
						p2 = pl2;
						if(LegionPractice.random.nextInt(players.size()) == 0) {
							break;
						}
					}
				}
				players.put(p2, true);
				if(p == null || p2 == null || Bukkit.getPlayer(p) == null || Bukkit.getPlayer(p2) == null) {
					Bukkit.broadcastMessage(ChatColor.RED + "Error: sumo player is invalid!");
					stop();
					this.cancel();
					return;
				}
				Sumo.this.p1 = p;
				Sumo.this.p2 = p2;
				for(String s : getPlayers().keySet()) {
					Player pl = Bukkit.getPlayer(s);
					if(pl != null) {
						pl.sendMessage(plugin.translateMessage(pl, "sumo-next-fight").replace("<player1>", p).replace("<player2>", p2));
					}
				}
				if(arena == null) {
					findArena();
				}
				if(arena == null) {
					Bukkit.broadcastMessage(ChatColor.RED + "Sumo cannot be started... the arena is invalid or it can not be used.");
					stop();
					this.cancel();
					return;
				}
				arena.removeItems();
				duel = new Duel(plugin, p, p2, kit);
				duel.setDoNotTeleport(true);
				duel.setArena(arena);
				duel.start();
				lastFightStarted = System.currentTimeMillis();
				this.cancel();
				return;
			}
		}.runTaskTimer(plugin, 20, 20);
	}

	@Override
	public void findArena() {
		for(Arena ar : plugin.arenas) {
			if(ar.getName().toLowerCase().contains("sumoevent") && ar.getKits().contains(kit.getName().toLowerCase())) {
				arena = ar;
			}
		}
		if(arena == null) {
			for(Arena ar : plugin.arenas) {
				if(ar.getName().equalsIgnoreCase("sumoevent")) {
					arena = ar;
				}
			}
		}
	}

	@Override
	public void lobby(Player p) {
		Location l = Brackets.getLobby("sumo");
		if(l != null) {
			plugin.clear(p, false, false);
			if(spec && arena != null) {
				p.teleport(arena.getCenter());
				plugin.getSpectatorHandler().addSpectator(p);
			}
			else {
				p.teleport(l);
			}
		}
		else if(spec && arena != null) {
			p.teleport(arena.getCenter());
			plugin.getSpectatorHandler().addSpectator(p);
		}
	}

}
