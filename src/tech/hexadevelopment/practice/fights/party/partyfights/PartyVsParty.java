package tech.hexadevelopment.practice.fights.party.partyfights;

import java.util.ArrayList;
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
import tech.hexadevelopment.practice.events.PartyVsPartyEndEvent;
import tech.hexadevelopment.practice.events.PartyVsPartyStartEvent;
import tech.hexadevelopment.practice.fightinventory.FightInventory;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.requests.PartyVsPartyRequest;
import tech.hexadevelopment.practice.fights.savedfights.SavedFight;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.stats.PlayerStats;
import tech.hexadevelopment.practice.utils.Countdown;
import tech.hexadevelopment.practice.utils.PlayerUtil;
import tech.hexadevelopment.practice.utils.Teleporter;

public class PartyVsParty extends Fight {

	private LegionPractice plugin;
	public Party p1;
	public Party p2;
	private HashSet<String> partyAlive1 = new HashSet<String>();
	private HashSet<String> partyAlive2 = new HashSet<String>();
	private boolean cancelled;
	private boolean ended;
	public PartyVsPartyRequest request;


	public PartyVsParty(LegionPractice plugin, Party p1, Party p2, BattleKit kit) {
		this.plugin = plugin;
		this.p1 = p1;
		this.p2 = p2;
		this.kit = kit;
		PartyVsPartyStartEvent event = new PartyVsPartyStartEvent(this, p1, p2);
		Bukkit.getPluginManager().callEvent(event);
		cancelled = event.isCancelled();
	}

	public boolean canStart() {
		if(kit.isBuild()) {
			this.arena = Arena.findEmptyBuildArena(PlayerUtil.getPlayer(p1.getOwner()), kit);
		}
		else this.arena = Arena.findEmptyArena(PlayerUtil.getPlayer(p1.getOwner()), kit);
		return arena != null && !arena.needsRollback() && !arena.isUsing()
				&& arena.getLoc1() != null && arena.getLoc2() != null
				&& arena.getLoc1().getWorld() != null && arena.getLoc1().getWorld()
				.getName().equals(arena.getLoc2().getWorld().getName());
	}

	public void start() {
		if(!cancelled) {
			setStartedAfterCountdown();
			arena.setUsing(true, this);
			p1.setFight(this);
			p2.setFight(this);
			p2.setOpponent(p1);
			p1.setOpponent(p2);
			boolean noDefaultWorld = plugin.getConfig().getBoolean("no-arenas-in-default-world");
			boolean tags = plugin.getConfig().getBoolean("enable-colored-names");
			String prefix = plugin.getPrefix();
			for(String name : p1.getMembers()) {
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
					partyAlive1.add(name);
					if(tags) {
						plugin.getTagManager().setTagToNames(member, "team1", p1.getMembers());
						plugin.getTagManager().setTagToNames(member, "team2", p2.getMembers());
					}
					String msg = plugin.translateMessage(member, "fight-start-message.party-vs-party", false);
					if(!msg.equals("false")) {
						member.sendMessage(prefix + msg.replace("<opponent>", p2.getOwner()));	
					}
					kit.sendFightInfo(member);
				}
			}
			for(String name : p2.getMembers()) {
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
					kit.giveKit(member);
					partyAlive2.add(name);
					if(tags) {
						plugin.getTagManager().setTagToNames(member, "team1", p2.getMembers());
						plugin.getTagManager().setTagToNames(member, "team2", p1.getMembers());
					}
					if(!plugin.translateMessage(member, "fight-start-message.party-vs-party", false).equals("false")) {
						member.sendMessage(plugin.translateMessage(member, "fight-start-message.party-vs-party").replace("<opponent>", p1.getOwner()));	
					}
					kit.sendFightInfo(member);
				}
			}
			startCountdown();
			plugin.getFileManager().addFight(kit);
		}
	}

	private void startCountdown() {
		List<String> players = new ArrayList<String>();
		players.addAll(p1.getMembers());
		players.addAll(p2.getMembers());
		Countdown.startCountdown(players);
	}

	public void handleDeath(Player p) {
		Party party = Party.getParty(p);
		HashSet<String> alive;
		HashSet<String> oAlive;
		if(partyAlive1.contains(p.getName())) {
			alive = partyAlive1;
			oAlive = partyAlive2;
		}
		else if(partyAlive2.contains(p.getName())) {
			alive = partyAlive2;
			oAlive = partyAlive1;
		}
		else return;
		new FightInventory(p, plugin);
		plugin.clear(p, false, true);
		Fight.setCurrentFight(p, null, plugin);
		alive.remove(p.getName());
		int counter = alive.size();
		for(String s : party.getMembers()) {
			Player mem = PlayerUtil.getPlayer(s);
			mem.sendMessage(plugin.translateMessage(mem, "your-member-died")
					.replace("<player>", p.getName()).replace("<alive>", counter + ""));
		}
		Party oParty = party.getOpponent();
		for(String s : oParty.getMembers()) {
			Player mem = PlayerUtil.getPlayer(s);
			mem.sendMessage(plugin.translateMessage(mem, "opponent-member-died")
					.replace("<player>", p.getName()).replace("<alive>", counter + ""));
		}
		if(plugin.getTagManager().COLORED_TAGS) {
			plugin.getTagManager().removeFromTeams(p);
		}
		if(counter == 0) {
			ended = true;
			super.ended = System.currentTimeMillis();
			Bukkit.getPluginManager().callEvent(new PartyVsPartyEndEvent(this, oParty, party));
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
					for(String s : party.getMembers()) {
						Player member = PlayerUtil.getPlayer(s);
						if(member != null) {
							if(spec && plugin.getSpectatorHandler().isSpectator(member)) {
								plugin.getSpectatorHandler().removeSpectator(member, true);
							}
							Fight.setCurrentFight(member, null, plugin);
						}
					}
					for(String s : oParty.getMembers()) {
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
					if(oParty != null) {
						oParty.setFight(null);
						oParty.setOpponent(null);
					}
					if(arena != null) {
						arena.setUsing(false, null);
					}
				}
			}, 20*plugin.getConfig().getInt("wait-before-teleport"));
			for(String d : oParty.getMembers()) {
				Player member = PlayerUtil.getPlayer(d);
				if(member != null) {
					UUID uuid = member.getUniqueId();
					PlayerStats.getStats(uuid).setPartyVsPartyWins(PlayerStats.getStats(uuid).getPartyVsPartyWins()+1);
				}
			}
			for(String s : party.getMembers()) {
				Player mem = PlayerUtil.getPlayer(s);
				if(mem != null) mem.sendMessage(plugin.translateMessage(mem, "your-team-did-not-win"));
			}
			for(String s : oParty.getMembers()) {
				Player mem = PlayerUtil.getPlayer(s);
				if(mem != null) mem.sendMessage(plugin.translateMessage(mem, "your-team-won"));
			}
			FightInventory.message(p1.getMembers(), p2.getMembers());
			FightInventory.message(p2.getMembers(), p1.getMembers());
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
	 * @return the party1
	 */
	public Party getParty1() {
		return p1;
	}

	/**
	 * @return the party2
	 */
	public Party getParty2() {
		return p2;
	}

	/**
	 * @return the partyAlive1
	 */
	public HashSet<String> getPartyAlive1() {
		return partyAlive1;
	}

	/**
	 * @return the partyAlive2
	 */
	public HashSet<String> getPartyAlive2() {
		return partyAlive2;
	}


	public void forceEnd(Party loser) {
		Bukkit.getPluginManager().callEvent(new PartyVsPartyEndEvent(this, p2 == loser ? p1 : p2, loser));
		for(String s : p2.getMembers()) {
			Player mem = PlayerUtil.getPlayer(s);
			if(mem != null) {
				if(!loser.getOwner().equals(p2.getOwner())) {
					mem.sendMessage(plugin.translateMessage(mem, "your-team-won-disband"));
				}
				Fight.setCurrentFight(mem, null, plugin);
				if(plugin.getSpectatorHandler().isSpectator(mem)) {
					plugin.getSpectatorHandler().removeSpectator(mem, true);
				}
				plugin.clear(mem, true, true);
				if(plugin.getTagManager().COLORED_TAGS) {
					plugin.getTagManager().removeFromTeams(mem);
				}
			}
		}
		p2.setFight(null);
		p2.setOpponent(null);
		for(String s : p1.getMembers()) {
			Player mem = PlayerUtil.getPlayer(s);
			if(mem != null) {
				if(!loser.getOwner().equals(p1.getOwner())) {
					mem.sendMessage(plugin.translateMessage(mem, "your-team-won-disband"));
				}
				Fight.setCurrentFight(mem, null, plugin);
				if(plugin.getSpectatorHandler().isSpectator(mem)) {
					plugin.getSpectatorHandler().removeSpectator(mem, true);
				}
				plugin.clear(mem, true, true);
				if(plugin.getTagManager().COLORED_TAGS) {
					plugin.getTagManager().removeFromTeams(mem);
				}
			}
		}
		p1.setFight(null);
		p1.setOpponent(null);
		if(arena != null) {
			arena.setUsing(false, null);
		}
	}

	@Override
	public void forceEnd(String reason) {
		Bukkit.getPluginManager().callEvent(new PartyVsPartyEndEvent(this, null, null));
		for(String s : p1.getMembers()) {
			Player mem = PlayerUtil.getPlayer(s);
			if(mem != null) {
				mem.sendMessage(reason);
				Fight.setCurrentFight(mem, null, plugin);
				if(plugin.getSpectatorHandler().isSpectator(mem)) {
					plugin.getSpectatorHandler().removeSpectator(mem, true);
				}
				plugin.clear(mem, true, true, true);
				p1.setFight(null);
				p1.setOpponent(null);
			}
		}
		for(String s : p2.getMembers()) {
			Player mem = PlayerUtil.getPlayer(s);
			if(mem != null) {
				mem.sendMessage(reason);
				Fight.setCurrentFight(mem, null, plugin);
				if(plugin.getSpectatorHandler().isSpectator(mem)) {
					plugin.getSpectatorHandler().removeSpectator(mem, true);
				}
				plugin.clear(mem, true, true, true);
				p1.setFight(null);
				p1.setOpponent(null);
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
