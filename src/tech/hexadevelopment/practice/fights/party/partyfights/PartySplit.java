package tech.hexadevelopment.practice.fights.party.partyfights;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.events.PartySplitEndEvent;
import tech.hexadevelopment.practice.events.PartySplitStartEvent;
import tech.hexadevelopment.practice.fightinventory.FightInventory;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.savedfights.SavedFight;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.utils.Countdown;
import tech.hexadevelopment.practice.utils.PlayerUtil;
import tech.hexadevelopment.practice.utils.Teleporter;

public class PartySplit extends Fight {

	private LegionPractice plugin;
	private Party party;
	private boolean cancelled;
	public HashSet<String> alive = new HashSet<String>();
	public HashSet<String> alive2 = new HashSet<String>();
	private HashSet<String> team = new HashSet<String>();
	private HashSet<String> team2 = new HashSet<String>();
	private boolean ended;

	public PartySplit(LegionPractice plugin, Party party, BattleKit asd) {
		this.plugin = plugin;
		this.party = party;
		this.kit = asd;
		PartySplitStartEvent event = new PartySplitStartEvent(this, party);
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
			arena.setUsing(true, this);
			party.setFight(this);
			List<String> l = new ArrayList<String>(party.getMembers());
			Collections.shuffle(l);
			for(String s : l) {
				if(alive2.size() > alive.size()) {
					alive.add(s);
				}
				else {
					alive2.add(s);
				}
			}
			String prefix = plugin.getPrefix();
			boolean noDefaultWorld = plugin.getConfig().getBoolean("no-arenas-in-default-world");
			for(String name : alive) {
				Player member = PlayerUtil.getPlayer(name);
				if(member != null) {
					if(plugin.getSpectatorHandler().isSpectator(member)) {
						plugin.getSpectatorHandler().removeSpectator(member, false);
					}
					if(member.isDead()) member.spigot().respawn();
					plugin.clear(member, false, false);
					member.setFallDistance(0);
					if(!Teleporter.teleport(member, arena.getLoc1(), noDefaultWorld)) {
						forceEnd(ChatColor.RED + "An error occurred in your fight and the fight was forced to end!");
						return;
					}
					member.setHealth(20);
					member.setFoodLevel(20);
					Fight.setCurrentFight(member, this, plugin);
					member.setMetadata(plugin.IN_FIGHT, new FixedMetadataValue(plugin, true));
					kit.giveKit(member);
					if(plugin.getTagManager().COLORED_TAGS) {
						plugin.getTagManager().setTagToNames(member, "team1", alive);
						plugin.getTagManager().setTagToNames(member, "team2", alive2);
					}
					String msg = plugin.translateMessage(member, "fight-start-message.party-split", false);
					if(!msg.equals("false")) {
						member.sendMessage(prefix + msg);	
					}
					kit.sendFightInfo(member);
				}
			}
			for(String name : alive2) {
				Player member = PlayerUtil.getPlayer(name);
				if(member != null) {
					if(plugin.getSpectatorHandler().isSpectator(member)) {
						plugin.getSpectatorHandler().removeSpectator(member, false);
					}
					if(member.isDead()) member.spigot().respawn();
					plugin.clear(member, false, false);
					member.setFallDistance(0);
					if(!Teleporter.teleport(member, arena.getLoc2(), noDefaultWorld)) {
						forceEnd(ChatColor.RED + "An error occurred in your fight and the fight was forced to end!");
						return;
					}
					member.setHealth(20);
					member.setFoodLevel(20);
					Fight.setCurrentFight(member, this, plugin);
					member.setMetadata(plugin.IN_FIGHT, new FixedMetadataValue(plugin, true));
					kit.giveKit(member);;
					if(plugin.getTagManager().COLORED_TAGS) {
						plugin.getTagManager().setTagToNames(member, "team1", alive2);
						plugin.getTagManager().setTagToNames(member, "team2", alive);
					}
					String msg = plugin.translateMessage(member, "fight-start-message.party-split", false);
					if(!msg.equals("false")) {
						member.sendMessage(prefix + msg);	
					}
					kit.sendFightInfo(member);
				}
			}
			team = new HashSet<String>(alive);
			team2 = new HashSet<String>(alive2);
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
		Party party = Party.getParty(p);
		HashSet<String> alive;
		HashSet<String> oAlive;
		HashSet<String> team;
		HashSet<String> oTeam;
		if(this.alive.contains(p.getName())) {
			alive = this.alive;
			oAlive = alive2;
			team = this.team;
			oTeam = this.team2;
		}
		else if(alive2.contains(p.getName())) {
			alive = alive2;
			oAlive = this.alive;
			team = this.team2;
			oTeam = this.team;
		}
		else return;
		new FightInventory(p, plugin);
		plugin.clear(p, false, true);
		Fight.setCurrentFight(p, null, plugin);
		alive.remove(p.getName());
		int counter = alive.size();
		for(String s : team) {
			Player mem = PlayerUtil.getPlayer(s);
			if(mem != null) {
				mem.sendMessage(plugin.translateMessage(mem, "your-member-died")
						.replace("<player>", p.getName()).replace("<alive>", counter + ""));
			}
		}
		for(String s : oTeam) {
			Player mem = PlayerUtil.getPlayer(s);
			if(mem != null) {
				mem.sendMessage(plugin.translateMessage(mem, "opponent-member-died")
						.replace("<player>", p.getName()).replace("<alive>", counter + ""));
			}
		}
		if(plugin.getTagManager().COLORED_TAGS) {
			plugin.getTagManager().removeFromTeams(p);
		}
		if(counter == 0) {
			Bukkit.getPluginManager().callEvent(new PartySplitEndEvent(this, party, oAlive, alive));
			ended = true;
			super.ended = System.currentTimeMillis();
			for(String d : oAlive) {
				Player member = PlayerUtil.getPlayer(d);
				if(member != null) {
					new FightInventory(member, plugin);
					member.setMetadata(plugin.IN_FIGHT, new FixedMetadataValue(plugin, true));
					if(plugin.getTagManager().COLORED_TAGS) {
						plugin.getTagManager().removeFromTeams(member);
					}
				}
			}
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					boolean spec = allowSpectating();
					for(String s : team) {
						Player member = PlayerUtil.getPlayer(s);
						if(member != null) {
							if(spec && plugin.getSpectatorHandler().isSpectator(member)) {
								plugin.getSpectatorHandler().removeSpectator(member, true);
							}
							Fight.setCurrentFight(member, null, plugin);
						}
					}
					for(String s : oTeam) {
						Player member = PlayerUtil.getPlayer(s);
						if(member != null) {
							if(spec && plugin.getSpectatorHandler().isSpectator(member)) {
								plugin.getSpectatorHandler().removeSpectator(member, true);
							}
							Fight.setCurrentFight(member, null, plugin);
						}
					}
					for(String s : oAlive) {
						Player member = PlayerUtil.getPlayer(s);
						if(member != null) {
							plugin.clear(member, true, true);
						}
					}
					if(party != null) {
						party.setFight(null);
						party.setOpponent(null);
					}
					if(arena != null) {
						arena.setUsing(false, null);
					}
				}
			}, 20*plugin.getConfig().getInt("wait-before-teleport"));
			for(String s : team) {
				Player mem = Bukkit.getPlayer(s);
				if(mem != null) {
					mem.sendMessage(plugin.translateMessage(mem, "your-team-did-not-win"));
				}
			}
			for(String s : oTeam) {
				Player mem = Bukkit.getPlayer(s);
				if(mem != null) {
					mem.sendMessage(plugin.translateMessage(mem, "your-team-won"));
				}
			}
				FightInventory.message(team, oTeam);
				FightInventory.message(oTeam, team);
		}
	}

	public boolean allowSpectating() {
		return plugin.getConfig().getBoolean("allow-spectating");
	}

	/**
	 * @return the alive
	 */
	public HashSet<String> getAlive1() {
		return alive;
	}

	/**
	 * @return the alive2
	 */
	public HashSet<String> getAlive2() {
		return alive2;
	}

	/**
	 * @return the team
	 */
	public HashSet<String> getTeam1() {
		return team;
	}

	/**
	 * @return the team2
	 */
	public HashSet<String> getTeam2() {
		return team2;
	}

	/**
	 * Gets if this fight has ended.
	 */
	public boolean hasEnded() {
		return ended;
	}

	@Override
	public void forceEnd(String reason) {
		Bukkit.getPluginManager().callEvent(new PartySplitEndEvent(this, party, new HashSet<String>(), new HashSet<String>()));
		for(String s : party.getMembers()) {
			Player mem = Bukkit.getPlayer(s);
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
