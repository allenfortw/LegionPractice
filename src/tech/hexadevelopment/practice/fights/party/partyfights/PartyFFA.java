package tech.hexadevelopment.practice.fights.party.partyfights;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.events.PartyFFAEndEvent;
import tech.hexadevelopment.practice.events.PartyFFAStartEvent;
import tech.hexadevelopment.practice.fightinventory.FightInventory;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.savedfights.SavedFight;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.utils.Circle;
import tech.hexadevelopment.practice.utils.Countdown;
import tech.hexadevelopment.practice.utils.PlayerUtil;
import tech.hexadevelopment.practice.utils.Teleporter;

public class PartyFFA extends Fight {

	private LegionPractice plugin;
	private Party party;
	private boolean cancelled;
	private HashSet<String> alive = new HashSet<String>();
	private boolean ended;

	public PartyFFA(LegionPractice plugin, Party party, BattleKit asd) {
		this.plugin = plugin;
		this.party = party;
		this.kit = asd;
		PartyFFAStartEvent event = new PartyFFAStartEvent(this, party);
		Bukkit.getPluginManager().callEvent(event);
		cancelled = event.isCancelled();
	}

	public boolean canStart() {
		if (kit.isBuild()) {
			this.arena = Arena.findEmptyBuildArena(PlayerUtil.getPlayer(party.getOwner()), kit);
		}
		else this.arena = Arena.findEmptyArena(PlayerUtil.getPlayer(party.getOwner()), kit);
		return arena != null && !arena.needsRollback() && !arena.isUsing()
				&& arena.getLoc1() != null && arena.getLoc2() != null
				&& arena.getLoc1().getWorld() != null && arena.getLoc1().getWorld()
				.getName().equals(arena.getLoc2().getWorld().getName());
	}

	public void start() {
		if(!cancelled) {
			setStartedAfterCountdown();
			List<Location> locs = Circle.getCircle(arena.getCenter(),
					plugin.getConfig().getInt("circle-radius"), party.getMembers().size());
			arena.setUsing(true, this);
			party.setFight(this);
			boolean noDefaultWorld = plugin.getConfig().getBoolean("no-arenas-in-default-world");
			String prefix = plugin.getPrefix();
			for(String name : party.getMembers()) {
				if(name != null) {
					alive.add(name);
					Player member = PlayerUtil.getPlayer(name);
					if(plugin.getSpectatorHandler().isSpectator(member)) {
						plugin.getSpectatorHandler().removeSpectator(member, false);
					}
					if(member.isDead()) member.spigot().respawn();
					plugin.clear(member, false, false);
					member.setFallDistance(0);
					Location center = arena.getCenter().clone();
					Location loc = locs.get(0);
					Location target = loc.setDirection(center.subtract(loc).toVector());
					if(!Teleporter.teleport(member, target, noDefaultWorld)) {
						forceEnd(ChatColor.RED + "An error occurred in your fight and the fight was forced to end!");
						return;
					}
					Fight.setCurrentFight(member, this, plugin);
					kit.giveKit(member);
					locs.remove(0);
					member.setMetadata(plugin.IN_FIGHT, new FixedMetadataValue(plugin, true));
					String msg = plugin.translateMessage(member, "fight-start-message.party-ffa", false);
					if(!msg.equals("false")) {
						member.sendMessage(prefix + msg);
					}
					kit.sendFightInfo(member);
					if(plugin.getTagManager().COLORED_TAGS) {
						List<String> others = new ArrayList<String>(party.getMembers());
						others.remove(member.getName());
						plugin.getTagManager().setTagToNames(member, "team2", others);
						plugin.getTagManager().setTagToNames(member, "team1", Arrays.asList(member.getName()));
					}
				}
			}
			startCountdown();
			plugin.getFileManager().addFight(kit);
		}
	}

	private void startCountdown() {
		List<String> players = new ArrayList<String>();
		players.addAll(party.getMembers());
		Countdown.startCountdown(players);
	}

	public void handleDeath(Player p) {
		if(!alive.contains(p.getName())) return;
		alive.remove(p.getName());
		int counter = alive.size();
		new FightInventory(p, plugin);
		for(String s : party.getMembers()) {
			Player mem = PlayerUtil.getPlayer(s);
			if(mem != null) {
				mem.sendMessage(plugin.translateMessage(mem, "ffa-died")
						.replace("<player>", p.getName()).replace("<alive>", counter + ""));
			}
		}
		plugin.clear(p, false, true);
		if(plugin.getTagManager().COLORED_TAGS) {
			plugin.getTagManager().removeFromTeams(p);
		}
		Fight.setCurrentFight(p, null, plugin);
		if (counter == 1){
			ended = true;
			super.ended = System.currentTimeMillis();
			Player winner = null;
			for (String name : alive){
				winner = PlayerUtil.getPlayer(name);
			}
			if(winner != null) {
				if(plugin.getTagManager().COLORED_TAGS) {
					plugin.getTagManager().removeFromTeams(winner);
				}
				Player w = winner;
				new FightInventory(winner, plugin);
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						if(arena != null) {
							arena.setUsing(false, null);
						}
						if(party != null) {
							party.setFight(null);
						}
						boolean spec = allowSpectating();
						for(String s : party.getMembers()) {
							Player mem = PlayerUtil.getPlayer(s);
							if(mem != null) {
								if(spec && plugin.getSpectatorHandler().isSpectator(mem)) {
									plugin.getSpectatorHandler().removeSpectator(mem, true);
								}
								Fight.setCurrentFight(mem, null, plugin);
							}
						}
						plugin.clear(w, true, true);
					}
				}, 20*plugin.getConfig().getInt("wait-before-teleport"));
			}
			Bukkit.getPluginManager().callEvent(new PartyFFAEndEvent(this, party, winner));
			for(String s : party.getMembers()) {
				Player mem = PlayerUtil.getPlayer(s);
				mem.sendMessage(plugin.translateMessage(mem, "ffa-winner")
						.replace("<player>", winner.getName()));
			}
			if(plugin.getConfig().getBoolean("clickable-messages")) {
				List<FightInventory> invs = new ArrayList<FightInventory>();
				for(String asd : party.getMembers()) {
					invs.add(plugin.getFightInventoryManager().getFightInventory(plugin.getFightInventoryManager().getInventoryUUIDByName(asd)));
				}
				for(String s : party.getMembers()) {
					Player mem = PlayerUtil.getPlayer(s);
					if(mem != null) {
						FightInventory.inventoryMessage(mem, invs);
					}
				}
			}
		}
	}

	public boolean allowSpectating() {
		return plugin.getConfig().getBoolean("allow-spectating");
	}

	/**
	 * Gets if this fight has ended.
	 */
	public boolean hasEnded() {
		return ended;
	}

	/**
	 * @return the party
	 */
	public Party getParty() {
		return party;
	}

	/**
	 * @return the alive
	 */
	public HashSet<String> getAlive() {
		return alive;
	}

	@Override
	public void forceEnd(String reason) {
		Bukkit.getPluginManager().callEvent(new PartyFFAEndEvent(this, party, null));
		if(party == null) return;
		for(String s : party.getMembers()) {
			Player mem = PlayerUtil.getPlayer(s);
			if(mem != null) {
				mem.sendMessage(reason);
				Fight.setCurrentFight(mem, null, plugin);
				if(plugin.getSpectatorHandler().isSpectator(mem)) {
					plugin.getSpectatorHandler().removeSpectator(mem, true);
				}
				plugin.clear(mem, true, true, true);
				party.setFight(null);
				if(plugin.getTagManager().COLORED_TAGS) {
					plugin.getTagManager().removeFromTeams(mem);
				}
			}
		}
		if(arena != null) {
			arena.setUsing(false, null);
		}
	}

	@Override
	public SavedFight saveFight(List<UUID> winners, List<UUID> losers, List<FightInventory> winnersInventories, List<FightInventory> losersInventories, UUID playbackUUID) {

		return null;
	}
}
