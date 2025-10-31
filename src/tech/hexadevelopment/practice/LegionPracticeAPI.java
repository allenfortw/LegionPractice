package tech.hexadevelopment.practice;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.utils.PlayerUtil;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.botduel.BotDuel;
import tech.hexadevelopment.practice.fights.duel.Duel;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyFFA;
import tech.hexadevelopment.practice.fights.party.partyfights.PartySplit;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsBots;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsParty;
import tech.hexadevelopment.practice.fights.queue.QueueManager;
import tech.hexadevelopment.practice.fights.queue.QueueRunnable;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.matchrecorder.RecordedMatch;
import tech.hexadevelopment.practice.matchrecorder.playback.PlaybackFight;
import tech.hexadevelopment.practice.npc.CitizensNPC;
import tech.hexadevelopment.practice.npc.CitizensNPC.Difficulty;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.playersettings.PlayerSettings;
import tech.hexadevelopment.practice.stats.PlayerStats;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

/**
 * The LegionPractice API with a few useful methods to make this even more custom!
 * @author Toppe5
 * @since 1.0
 */
public class LegionPracticeAPI {



	public static UUID getCurrentFightsPlaybackUUID(Player p) {
		LegionPractice plugin = LegionPractice.getInstance();
		Fight fight = Fight.getCurrentFight(p, plugin);
		if(fight != null && fight instanceof Duel) {
			Duel duel = (Duel) fight;
			if(duel.getRecorder() != null && duel.getRecorder().getRecordedMatch() != null) {
				return duel.getRecorder().getRecordedMatch().getUUID();
			}
		}
		return null;
	}

	public static boolean startPlayback(Player p, UUID playbackUUID) {
		for(List<RecordedMatch> matches : LegionPractice.getInstance().getRecordedMatchesManager().getRecordedDuels().values()) {
			for(RecordedMatch match : matches) {
				if(match.getUUID().equals(playbackUUID)) {
					PlaybackFight fight = match.startPlayback(false);
					if(fight != null) {
						fight.addSpectator(p);
						return true;
					}
				}
			}
		}
		return false;
	}

	public static Party getParty(Player p) {
		return Party.getParty(p);
	}

	public static void spawnBot(Location location, String name, String skin, List<UUID> targets, String difficulty, Fight fight) {
		CitizensNPC bot = new CitizensNPC(name, skin, location);
		bot.startCombatTask(targets, fight, Difficulty.valueOf(difficulty.toUpperCase()));
	}

	public static void forceWin(Player p) {
		Fight fight = Fight.getCurrentFight(p, LegionPractice.getInstance());
		if(fight != null) {
			if(fight instanceof Duel) {
				String s = ((Duel) fight).getP1();
				if(s == null || s.equals(p.getName())) {
					s = ((Duel) fight).getP2();
				}
				fight.handleDeath(Bukkit.getPlayer(s));
			}
			else if(fight instanceof BotDuel) {
				for(NPC npc : CitizensAPI.getNPCRegistry()) {
					if(npc.getEntity() != null && !npc.getEntity().isDead()) {
						Fight f = Fight.getCurrentFight(npc.getEntity(), LegionPractice.getInstance());
						if(f != null && f == fight) {
							((BotDuel) fight).handleBotDeath(npc.getEntity());
						}
					}
				}
			}
			else if(fight instanceof PartyVsBots) {
				for(NPC npc : CitizensAPI.getNPCRegistry()) {
					if(npc.getEntity() != null && !npc.getEntity().isDead()) {
						Fight f = Fight.getCurrentFight(npc.getEntity(), LegionPractice.getInstance());
						if(f != null && f == fight) {
							((PartyVsBots) fight).handleBotDeath(npc.getEntity());
						}
					}
				}
				teleportParty(Party.getParty(p));
			}
			else if(fight instanceof PartyFFA) {
				for(String s : ((PartyFFA) fight).getParty().getMembers()) {
					Player pl = PlayerUtil.getPlayer(s);
					if(pl != null && pl != p) {
						fight.handleDeath(pl);
					}
				}
				teleportParty(Party.getParty(p));
			}
			else if(fight instanceof PartySplit) {
				if(!contains(((PartySplit) fight).getTeam1(), p.getName())) {
					for(String s : ((PartySplit) fight).getTeam1()) {
						Player pl = PlayerUtil.getPlayer(s);
						if(pl != null && pl != p) {
							fight.handleDeath(pl);
						}
					}
				}
				else if(!contains(((PartySplit) fight).getTeam2(), p.getName())) {
					for(String s : ((PartySplit) fight).getTeam2()) {
						Player pl = PlayerUtil.getPlayer(s);
						if(pl != null && pl != p) {
							fight.handleDeath(pl);
						}
					}
				}
				teleportParty(Party.getParty(p));
			}
			else if(fight instanceof PartyVsParty) {
				if(!((PartyVsParty) fight).getParty1().getMembers().contains(p.getName())) {
					for(String s : ((PartyVsParty) fight).getParty1().getMembers()) {
						Player pl = PlayerUtil.getPlayer(s);
						if(pl != null && pl != p) {
							fight.handleDeath(pl);
						}
					}
				}
				if(!((PartyVsParty) fight).getParty2().getMembers().contains(p.getName())) {
					for(String s : ((PartyVsParty) fight).getParty2().getMembers()) {
						Player pl = PlayerUtil.getPlayer(s);
						if(pl != null && pl != p) {
							fight.handleDeath(pl);
						}
					}
				}
				teleportParty(((PartyVsParty) fight).getParty1());
				teleportParty(((PartyVsParty) fight).getParty2());
			}
		}
	}

	private static boolean contains(HashSet<String> map, String s) {
		for(String str : map) {
			if(str.equals(s)) {
				return true;
			}
		}
		return false;
	}
	
	private static void teleportParty(Party party) {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				for(String s : party.getMembers()) {
					Player mem = Bukkit.getPlayer(s);
					if(mem != null) {
						removeSpectator(mem, false);
						clear(mem, true, true);
					}
				}	
			}
		}.runTaskLater(getLegionPractice(), getLegionPractice().getConfig().getInt("wait-before-telepot")*20);
	}

	public void joinQueue(Player p, BattleKit kit, boolean premiumQueue) {
		LegionPractice.getInstance().queueManager.tryToJoin(p, kit, premiumQueue);
	}

	public static void joinQueue(Player p, BattleKit kit) {
		LegionPractice.getInstance().queueManager.tryToJoin(p, kit, false);
	}

	public static void joinPremiumQueue(Player p, BattleKit kit) {
		LegionPractice.getInstance().queueManager.tryToJoin(p, kit, true);
	}

	public static void cancelFight(Player p, String reason) {
		Fight fight = Fight.getCurrentFight(p, LegionPractice.getInstance());
		if(fight != null) {
			fight.forceEnd(reason);
		}
	}

	public static PlayerStats getPlayerStats(Player p) {
		return PlayerStats.getStats(p.getUniqueId());
	}

	public static PlayerStats getPlayerStats(UUID uuid, boolean create, boolean async) {
		return PlayerStats.getStats(uuid, create, async);
	}

	public static void clear(Player p, boolean lobby, boolean deselect) {
		LegionPractice.getInstance().clear(p, lobby, deselect);
	}

	public static Location getSpawnLocation() {
		return LegionPractice.getInstance().arenaPvP.getLobby();
	}

	public static boolean isSpectator(Player p) {
		return LegionPractice.getInstance().getSpectatorHandler().isSpectator(p);
	}

	public static void addSpectator(Player p) {
		LegionPractice.getInstance().getSpectatorHandler().addSpectator(p);
	}

	public static void addSpectator(Player p, Player target) {
		LegionPractice.getInstance().getSpectatorHandler().addSpectator(p, target);
	}

	public static void removeSpectator(Player p, boolean clearAndTeleport) {
		LegionPractice.getInstance().getSpectatorHandler().removeSpectator(p, clearAndTeleport);
	}

	public static boolean isInFight(Player p) {
		return Fight.isInFight(p, LegionPractice.getInstance());
	}

	public static boolean isInEvent(Player p) {
		return PvPEvent.isInEvent(p);
	}

	public static String getLanguage(Player p) {
		return PlayerSettings.getPlayerSettings(p.getUniqueId()).getLanguage();
	}

	public static boolean isRanked(Player p) {
		Fight fight = Fight.getCurrentFight(p, LegionPractice.getInstance());
		if(fight != null) {
			return fight.getKit().isElo();
		}
		if(isInQueue(p)) {
			QueueRunnable ranked = QueueRunnable.getRanked(p);
			if(ranked != null) {
				return ranked.getKit() != null && ranked.getKit().isElo();
			}
		}
		BattleKit kit = BattleKit.getCurrentKit(p);
		if(kit != null) return kit.isElo();
		return false;
	}

	public static boolean isInQueue(Player p) {
		return p.hasMetadata(QueueManager.waitingQueue);
	}

	public static void setLanguage(Player p, String language, boolean message) {
		PlayerSettings.getPlayerSettings(p.getUniqueId()).setLanguage(language, LegionPractice.getInstance(), message);
	}

	public static Fight getFight(Player p) {
		return Fight.getCurrentFight(p, LegionPractice.getInstance());
	}

	public static BattleKit getKit(Player p) {
		return BattleKit.getCurrentKit(p);
	}

	public static LegionPractice getLegionPractice() {
		return LegionPractice.getInstance();
	}

	public static Player getDuelOpponent(Player p) {
		Fight fight = Fight.getCurrentFight(p, LegionPractice.getInstance());
		if(fight != null && fight instanceof Duel) {
			Duel duel = (Duel) fight;
			return duel.getP1().equals(p.getName()) ? Bukkit.getPlayer(duel.getP2()) : Bukkit.getPlayer(duel.getP1());
		}
		return null;
	}
}
