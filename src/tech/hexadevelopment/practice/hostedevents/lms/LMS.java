package tech.hexadevelopment.practice.hostedevents.lms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.arena.CachedBlockChange;
import tech.hexadevelopment.practice.arena.RollbackListener;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.events.PvPEventEndEvent;
import tech.hexadevelopment.practice.events.PvPEventStartEvent;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.SimpleFight;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.utils.Circle;
import tech.hexadevelopment.practice.utils.Countdown;
import tech.hexadevelopment.practice.utils.Teleporter;

public class LMS implements PvPEvent{

	private LegionPractice plugin;
	private boolean started;
	private BattleKit kit;
	private long startTime;
	private Location spawn;
	private HashSet<String> players = new HashSet<String>();
	private HashSet<String> alive = new HashSet<String>();
	public HashSet<UUID> spectators = new HashSet<UUID>();
	private List<Fight> fights = new ArrayList<Fight>();

	public LMS(HashSet<String> players, BattleKit kit, Location location, LegionPractice plugin) {
		this.players = new HashSet<String>(players);
		this.plugin = plugin;
		this.kit = kit;
		this.spawn = location;
	}


	public void start() {
		LMSCommand.open = false;
		PvPEventStartEvent event = new PvPEventStartEvent(this);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) return;
		startTime = System.currentTimeMillis();
		setStarted(true);
		List<String> cdPlayers = new ArrayList<String>();
		List<Location> locs = Circle.getCircle(spawn,
				plugin.getConfig().getInt("circle-radius"), players.size());
		for(String name : players) {
			Player p = Bukkit.getPlayer(name);
			if(p != null) {
				if(plugin.getSpectatorHandler().isSpectator(p)) {
					plugin.getSpectatorHandler().removeSpectator(p, true);
				}
				plugin.clear(p, false, false);
				Location center = spawn.clone();
				Location loc = locs.get(0);
				Location target = loc.setDirection(center.subtract(loc).toVector());
				if(Teleporter.teleport(p, target, false)) {
					p.removeMetadata(LMSCommand.lmsWaiting, plugin);
					p.setMetadata(plugin.IN_FIGHT, new FixedMetadataValue(plugin, true));
					if(CachedBlockChange.CACHE_BLOCKS) {
						Fight f = new SimpleFight();
						f.setKit(kit);
						Fight.setCurrentFight(p, f, plugin);
						fights.add(f);
					}
					locs.remove(0);
					new BukkitRunnable() {
						
						@Override
						public void run() {
							if(p != null) {
								kit.giveKit(p);
							}
						}
					}.runTaskLater(plugin, 20);
					cdPlayers.add(p.getName());
				}
			}
		}
		alive.addAll(cdPlayers);
		Countdown.startCountdown(cdPlayers, 10);
	}

	public void stop() {
		try{
			PvPEventEndEvent event = new PvPEventEndEvent(this, null);
			Bukkit.getPluginManager().callEvent(event);
			if(CachedBlockChange.CACHE_BLOCKS) {
				for(Fight f : fights) {
					Iterator<CachedBlockChange> iterator = new HashSet<CachedBlockChange>(f.getBlockChanges()).iterator();
					f.getBlockChanges().clear();
					List<CachedBlockChange> dirtToGrassLater = new ArrayList<CachedBlockChange>();
					new BukkitRunnable() {

						@Override
						public void run() {
							int changeCounter = 0;
							int checkCounter = 0;
							while(iterator.hasNext()) {
								if(changeCounter < Arena.maxChangesPerTick && checkCounter < Arena.maxChecksPerTick) {
									CachedBlockChange l = iterator.next();
									if(l != null) {
										if(l.getOldMaterial() == Material.GRASS || l.getOldMaterial() == Material.MYCEL
												|| (l.getOldMaterial() == Material.DIRT && l.getOldData() == 2)) {
											dirtToGrassLater.add(l);
										}
										else {
											changeCounter++;
											l.reset();
											Block b = l.getLocation().getBlock();
											b.removeMetadata(RollbackListener.PLACED_IN_FIGHT, LegionPractice.getInstance());
											checkCounter++;
										}
									}
									iterator.remove();
								}
								else return;
							}
							this.cancel();
							for(CachedBlockChange l : dirtToGrassLater) {
								l.reset();
							}
						}
					}.runTaskTimer(LegionPractice.getInstance(), 0, LegionPractice.performanceMode ? 5 : 1);
				}
				fights.clear();
			}
		}catch(Exception e) {}
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(spectators.contains(p.getUniqueId()) && plugin.getSpectatorHandler().isSpectator(p)) {
				plugin.getSpectatorHandler().removeSpectator(p, true);
			}
		}
		for(String name : getAlive()) {
			Player p = Bukkit.getPlayer(name);
			if(p != null) {
				plugin.clear(p, true, true);
				Fight.setCurrentFight(p, null, plugin);
			}
		}
		for(Player p : Bukkit.getOnlinePlayers()) {
			p.sendMessage(plugin.translateMessage(p, "event-stopped"));
			p.removeMetadata(LMSCommand.lmsWaiting, plugin);
		}
		LMSCommand.lms = null;
		getAlive().clear();
	}

	public boolean hasStarted() {
		return started;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public HashSet<String> getAlive() {
		return alive;
	}

	public HashSet<String> getPlayers() {
		return players;
	}
}
