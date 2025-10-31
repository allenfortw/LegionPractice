package tech.hexadevelopment.practice.hostedevents.brackets;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.events.PvPEventEndEvent;
import tech.hexadevelopment.practice.events.PvPEventStartEvent;
import tech.hexadevelopment.practice.fights.duel.Duel;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.utils.SerializableLocation;
import tech.hexadevelopment.practice.LegionPractice;

public class Brackets implements PvPEvent{

	protected HashMap<String, Boolean> players = new HashMap<String, Boolean>();
	public HashSet<UUID> spectators = new HashSet<UUID>();
	protected BattleKit kit;
	protected LegionPractice plugin;
	protected String p1;
	protected String p2;
	protected boolean started;
	protected Duel duel;
	protected Arena arena;
	protected long startTime;
	protected boolean spec;
	public int totalPlayers;
	protected long lastFightStarted;

	public Brackets(LegionPractice plugin) {
		this.plugin = plugin;
		this.spec = plugin.getConfig().getBoolean("allow-brackets-spectating");
	}

	public void start() {
		PvPEventStartEvent event = new PvPEventStartEvent(this);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) return;
		startEvent();
	}

	public void startEvent() {
		startTime = System.currentTimeMillis();
		started = true;
		findArena();
		totalPlayers = players.size();
		if(arena != null) {
			arena.setBuild(kit.isBuild());
		}
		for(String s : players.keySet()) {
			Player pl = Bukkit.getPlayer(s);
			if(pl != null) {
				if(spec && arena != null) {
					pl.teleport(arena.getCenter());
					plugin.getSpectatorHandler().addSpectator(pl);
				}
				else {
					lobby(pl);
				}
			}
		}
		nextFight();
	}

	public boolean enoughPlayers(){
		int count = 0;
		for(String s : players.keySet()) {
			if(Bukkit.getPlayer(s) != null) {
				count++;
			}
		}
		return count >= 2;
	}

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
								plugin.clear(pl, true, true);
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
								}.runTaskLater(plugin, 40);
							}
						}
					}.runTaskLater(plugin, 20*plugin.getConfig().getInt("wait-before-teleport"));
				}
			}
		}
		BracketsCommand.brackets = null;
		BracketsCommand.starting = false;
	}

	public String getEstimatedTimeLeft() {
		if(!started) return "--:--";
		int roundsLeft = players.size()-1;
		int roundsPlayed = totalPlayers-players.size();
		long timelapsed = System.currentTimeMillis()-startTime;
		long sinceLastRound = System.currentTimeMillis()-lastFightStarted;
		long averageRoundLegth = roundsPlayed > 0 ? timelapsed/roundsPlayed : sinceLastRound > 30 ? sinceLastRound : 30;
		long estimated = (averageRoundLegth*roundsLeft)-sinceLastRound;
		if(estimated < 5000) estimated = 5000;
		return new SimpleDateFormat("mm:ss").format(new Date(estimated+LegionPractice.dateFix));
	}

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
					Bukkit.broadcastMessage(ChatColor.RED + "Error: brackets player is invalid!");
					stop();
					this.cancel();
					return;
				}
				Brackets.this.p1 = p;
				Brackets.this.p2 = p2;
				for(String s : getPlayers().keySet()) {
					Player pl = Bukkit.getPlayer(s);
					if(pl != null) {
						pl.sendMessage(plugin.translateMessage(pl, "brackets-next-fight").replace("<player1>", p).replace("<player2>", p2));
					}
				}
				if(arena == null) {
					findArena();
				}
				if(arena == null) {
					Bukkit.broadcastMessage(ChatColor.RED + "Brackets cannot be started... the arena is invalid or it can not be used.");
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

	public void lobby(Player p) {
		Location l = getLobby("brackets");
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


	public void findArena() {
		for(Arena ar : plugin.arenas) {
			if(ar.getName().toLowerCase().contains("brackets") && ar.getKits().contains(kit.getName().toLowerCase())) {
				arena = ar;
				arena.setCustomMaxChangesPerTick(LegionPractice.getInstance().getConfig().getInt("max-block-changes-per-tick-brackets-arena"));
				arena.setCustomMaxChecksPerTick(LegionPractice.getInstance().getConfig().getInt("max-block-checks-per-tick-brackets-arena"));
			}
		}
		if(arena == null) {
			for(Arena ar : plugin.arenas) {
				if(ar.getName().equalsIgnoreCase("brackets")) {
					arena = ar;
					arena.setCustomMaxChangesPerTick(LegionPractice.getInstance().getConfig().getInt("max-block-changes-per-tick-brackets-arena"));
					arena.setCustomMaxChecksPerTick(LegionPractice.getInstance().getConfig().getInt("max-block-checks-per-tick-brackets-arena"));
				}
			}
		}
	}

	public static Location getLobby(String event) {
		Object s = LegionPractice.getInstance().getConfig()
				.get(event + ".lobby");
		if(s == null) {
			Bukkit.getLogger().warning(ChatColor.RED + "Brackets lobby is not valid!");
			return null;
		}
		return ((SerializableLocation) s).toLocation();
	}

	public String getP1() {
		return p1;
	}

	public Duel getDuel() {
		return duel;
	}

	public String getP2() {
		return p2;
	}

	public BattleKit getKit() {
		return kit;
	}

	public HashMap<String, Boolean> getPlayers() {
		return players;
	}

	public long getStartTime() {
		return startTime;
	}

	public Arena getArena() {
		return arena;
	}

	public boolean hasStarted() {
		return started;
	}

	public void setKit(BattleKit kit) {
		this.kit = kit;
	}

	public void setP1(String p1) {
		this.p1 = p1;
	}

	public void setP2(String p2) {
		this.p2 = p2;
	}

	public void setPlayers(HashMap<String, Boolean> players) {
		this.players = players;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}
}
