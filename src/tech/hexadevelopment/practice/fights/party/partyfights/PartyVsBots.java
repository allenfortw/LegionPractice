package tech.hexadevelopment.practice.fights.party.partyfights;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.events.PartyVsBotsEndEvent;
import tech.hexadevelopment.practice.events.PartyVsBotsStartEvent;
import tech.hexadevelopment.practice.fightinventory.FightInventory;
import tech.hexadevelopment.practice.fights.BotFight;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.savedfights.SavedFight;
import tech.hexadevelopment.practice.npc.CitizensNPC;
import tech.hexadevelopment.practice.npc.CitizensNPC.Difficulty;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.utils.Countdown;
import tech.hexadevelopment.practice.utils.PlayerUtil;
import tech.hexadevelopment.practice.utils.Teleporter;
import tech.hexadevelopment.practice.utils.VersionChecker;

public class PartyVsBots extends Fight implements BotFight{

	private LegionPractice plugin;
	private Party party;
	private boolean cancelled;
	private HashMap<CitizensNPC, Boolean> botsAlive = new HashMap<CitizensNPC, Boolean>();
	private HashSet<String> playersAlive = new HashSet<String>();
	private boolean ended;
	private Difficulty difficulty;

	public PartyVsBots(LegionPractice plugin, Party party, BattleKit asd) {
		this.plugin = plugin;
		this.party = party;
		this.kit = asd;
		PartyVsBotsStartEvent event = new PartyVsBotsStartEvent(this, party);
		Bukkit.getPluginManager().callEvent(event);
		cancelled = event.isCancelled();
	}

	public boolean canStart() {
		if (kit.isBuild()) {
			this.arena = Arena.findEmptyBuildArena(PlayerUtil.getPlayer(party.getOwner()), kit);
		}
		else this.arena = Arena.findEmptyArena(PlayerUtil.getPlayer(party.getOwner()), kit);
		String site = VersionChecker.s;
		if(!site.equals("http://LegionPractice.ga/") || site.length() != 25
				|| LegionPractice.getInstance().arenaPvP.z().length() < 3) {
			return false;
		}
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
			boolean tags = plugin.getConfig().getBoolean("enable-colored-names");
			boolean noDefaultWorld = plugin.getConfig().getBoolean("no-arenas-in-default-world");
			List<UUID> players = new ArrayList<UUID>();
			for(String name : party.getMembers()) {
				Player member = PlayerUtil.getPlayer(name);
				if(member != null) {
					players.add(member.getUniqueId());
				}
			}
			for(UUID uuid : players) {
				Player member = Bukkit.getPlayer(uuid);
				if(member != null) {
					if(plugin.getSpectatorHandler().isSpectator(member)) {
						plugin.getSpectatorHandler().removeSpectator(member, false);
					}
					if(member.isDead()) {
						member.spigot().respawn();
					}
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
					if(tags) {
						plugin.getTagManager().setTagToUUIDS(member, "team1", players);
					}
					/*
					if(!plugin.translateMessage(member, "fight-start-message.party-vs-bots", false).equals("false")) {
						member.sendMessage(plugin.translateMessage(member, "fight-start-message.party-vs-bots"));	
					}
					 */
					kit.sendFightInfo(member);
					playersAlive.add(member.getName());
					CitizensNPC bot = new CitizensNPC(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("bot-name").replace("<player>", member.getName())), member.getName(), arena.getLoc2());
					bot.startCombatTask(players, this, difficulty);
					botsAlive.put(bot, true);
				}
			}
			startCountdown();
			plugin.getFileManager().addFight(kit);
			new BukkitRunnable() {

				@Override
				public void run() {
					if(hasEnded()) {
						this.cancel();
					}
					else {
						for(Entry<CitizensNPC, Boolean> e : botsAlive.entrySet()) {
							if(e.getValue()) {
								CitizensNPC c = e.getKey();
								if(c == null || c.getBukkitEntity() == null || c.getNPC() == null || c.getBukkitEntity().isDead()) {
									botsAlive.put(c, false);
									botDeathEndsFight("*offline*");
								}
							}
						}
					}
				}
			}.runTaskTimer(plugin, 20*20, 20*5);
		}
	}

	private void startCountdown() {
		List<String> players = new ArrayList<String>();
		players.addAll(party.getMembers());
		Countdown.startCountdown(players);
	}

	@Override
	public void handleBotDeath(Metadatable ent) {
		for(Entry<CitizensNPC, Boolean> e : botsAlive.entrySet()) {
			if(e.getKey().getBukkitEntity() != null && e.getKey().getBukkitEntity().getUniqueId().equals(((Entity) ent).getUniqueId())
					&& e.getValue()) {
				botsAlive.put(e.getKey(), false);
				String name = e.getKey().getNPC().getFullName();
				try{
					new FightInventory(e.getKey().getBukkitEntity(), e.getKey().getNPC().getName(), e.getKey().getBukkitEntity().getInventory(), plugin);
					e.getKey().destroy();
				}catch(Exception ex) {}
				botDeathEndsFight(name);
			}
		}
	}

	private void botDeathEndsFight(String botName) {
		int counter = 0;
		try{
			for(Entry<CitizensNPC, Boolean> c : botsAlive.entrySet()) {
				if(c.getValue()) {
					counter++;
				}
			}
			for(String s : party.getMembers()) {
				Player mem = PlayerUtil.getPlayer(s);
				if(mem != null) {
					mem.sendMessage(plugin.translateMessage(mem, "opponent-member-died")
							.replace("<player>", botName).replace("<alive>", counter + ""));
				}
			}
		}catch(Exception ex){}
		if(counter == 0) {
			Bukkit.getPluginManager().callEvent(new PartyVsBotsEndEvent(this, party, PartyVsBotsEndEvent.Winners.PLAYERS));
			ended = true;
			super.ended = System.currentTimeMillis();
			for(String d : playersAlive) {
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
					for(String s : playersAlive) {
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
			for(String s : party.getMembers()) {
				Player mem = PlayerUtil.getPlayer(s);
				if(mem != null) {
					mem.sendMessage(plugin.translateMessage(mem, "your-team-won"));
				}
			}
			HashSet<String> botNames = new HashSet<String>();
			for(CitizensNPC d : botsAlive.keySet()) {
				botNames.add(d.getNPC().getName());
			}
			FightInventory.message(party.getMembers(), botNames);
		}
	}

	@Override
	public void handleDeath(Player p) {
		Party party = Party.getParty(p);
		if(playersAlive.contains(p.getName())) {
			new FightInventory(p, plugin);
			plugin.clear(p, false, true);
			Fight.setCurrentFight(p, null, plugin);
			playersAlive.remove(p.getName());
			int counter = playersAlive.size();
			for(String s : party.getMembers()) {
				Player mem = PlayerUtil.getPlayer(s);
				if(mem != null) {
					mem.sendMessage(plugin.translateMessage(mem, "your-member-died")
							.replace("<player>", p.getName()).replace("<alive>", counter + ""));
				}
			}
			if(plugin.getTagManager().COLORED_TAGS) {
				plugin.getTagManager().removeFromTeams(p);
			}
			if(counter == 0) {
				Bukkit.getPluginManager().callEvent(new PartyVsBotsEndEvent(this, party, PartyVsBotsEndEvent.Winners.BOTS));
				ended = true;
				super.ended = System.currentTimeMillis();
				for(Entry<CitizensNPC, Boolean> d : botsAlive.entrySet()) {
					if(d.getValue()){
						try{
							new FightInventory(d.getKey().getBukkitEntity(), d.getKey().getNPC().getName(), d.getKey().getBukkitEntity().getInventory(), plugin);
							d.getKey().getBukkitEntity().setMetadata(plugin.IN_FIGHT, new FixedMetadataValue(plugin, true));
						}catch(Exception e) {}
					}
				}
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						boolean spec = allowSpectating();
						for(Entry<CitizensNPC, Boolean> d : botsAlive.entrySet()) {
							if(d.getValue()){
								d.getKey().destroy();
							}
						}
						for(String s : party.getMembers()) {
							Player member = PlayerUtil.getPlayer(s);
							if(member != null) {
								if(spec && plugin.getSpectatorHandler().isSpectator(member)) {
									plugin.getSpectatorHandler().removeSpectator(member, true);
								}
								Fight.setCurrentFight(member, null, plugin);
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
				for(String s : party.getMembers()) {
					Player mem = PlayerUtil.getPlayer(s);
					if(mem != null) {
						mem.sendMessage(plugin.translateMessage(mem, "your-team-did-not-win"));
					}
				}
				HashSet<String> botNames = new HashSet<String>();
				for(CitizensNPC d : botsAlive.keySet()) {
					botNames.add(d.getNPC().getName());
				}
				FightInventory.message(party.getMembers(), botNames);
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

	@Override
	public void forceEnd(String reason) {
		Bukkit.getPluginManager().callEvent(new PartyVsBotsEndEvent(this, party, PartyVsBotsEndEvent.Winners.FORCE_END));
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
		for(CitizensNPC npc : botsAlive.keySet()) {
			npc.destroy(true);
		}
		if(arena != null) {
			arena.setUsing(false, null);
		}
	}

	@Override
	public SavedFight saveFight(List<UUID> winners, List<UUID> losers, List<FightInventory> winnersInventories, List<FightInventory> losersInventories, UUID playbackUUID) {
		return null;
	}

	public HashMap<CitizensNPC, Boolean> getBotsTeam() {
		return botsAlive;
	}

	public HashSet<String> getPlayersTeam() {
		return party.getMembers();
	}

	public HashSet<String> getPlayersAlive() {
		return playersAlive;
	}

	public HashSet<CitizensNPC> getBotsAlive() {
		HashSet<CitizensNPC> alive = new HashSet<CitizensNPC>();
		for(Entry<CitizensNPC, Boolean> e : botsAlive.entrySet()) {
			if(e.getValue()) {
				alive.add(e.getKey());
			}
		}
		return alive;
	}

	@Override
	public Difficulty getDifficulty() {
		return difficulty;
	}

	@Override
	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	}

}
