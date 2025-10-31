package tech.hexadevelopment.practice.matchrecorder.playback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.fightinventory.FightInventory;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.savedfights.SavedFight;
import tech.hexadevelopment.practice.matchrecorder.RecordedMatch;
import tech.hexadevelopment.practice.matchrecorder.RecordedPlayer;
import tech.hexadevelopment.practice.npc.CitizensNPC;
import tech.hexadevelopment.practice.overwatch.OverwatchManager;

public class PlaybackFight extends Fight{

	private boolean ended;
	public HashSet<UUID> spectators = new HashSet<UUID>();
	private HashSet<PlayerPlaybackPlayer> playbackPlayers = new HashSet<PlayerPlaybackPlayer>();
	private List<RecordedPlayer> recordedPlayers = new ArrayList<RecordedPlayer>();
	public PlaybackStarter playbackStarter;
	private boolean overwatch;
	private RecordedMatch match;
	private int killCamSeconds;

	public PlaybackFight(List<RecordedPlayer> recordedPlayers, PlaybackStarter playbackStarter, BattleKit kit, Arena arena, boolean overwatch, int killCamSeconds) {
		super.setKit(kit);
		this.arena = arena;
		this.killCamSeconds = killCamSeconds;
		this.overwatch = overwatch;
		this.recordedPlayers = recordedPlayers;
		this.playbackStarter = playbackStarter;
	}

	@Override
	public void start() {
		arena.setUsing(true, this);
		String nameFormat = LegionPractice.getInstance().getConfig().getString("playback-bot-name");
		match = (RecordedMatch) getPlaybackStarter();
		UUID suspect = OverwatchManager.highestUUID(match.getOriginalReports());
		for(RecordedPlayer rp : recordedPlayers) {
			String name = overwatch ? (suspect.equals(rp.getUuid()) ? "The Suspect" : "Anonymous") : nameFormat.replace("<player>", rp.getName());
			CitizensNPC npc = new CitizensNPC(name, overwatch ? "steve" : rp.getName(), arena.getCenter());
			npc.getNPC().setProtected(true);
			PlayerPlaybackPlayer playbackPlayer = new PlayerPlaybackPlayer(rp, npc, this, killCamSeconds);
			playbackPlayer.start();
			playbackPlayers.add(playbackPlayer);
		}
	}

	public void sendFIMessages(List<FightInventory> fightInventories, UUID playbackUUID) {
		if(!fightInventories.isEmpty()){
			for(UUID uuid : spectators) {
				Player p = Bukkit.getPlayer(uuid);
				if(p != null) {
					FightInventory.inventoryMessage(p, fightInventories);
				}
			}
		}
	}

	@Override
	public boolean allowSpectating() {
		return false;
	}

	@Override
	public boolean hasEnded() {
		return ended;
	}

	@Override
	public boolean canStart() {
		return arena != null && !arena.needsRollback() && !arena.isUsing() && arena.getLoc1() != null
				&& arena.getLoc2() != null && !arena.getName().equalsIgnoreCase("brackets") && arena.getLoc1().getWorld() != null
				&& arena.getLoc1().getWorld().getName().equals(arena.getLoc2().getWorld().getName());
	}

	@Override
	public void handleDeath(Player p) {}

	public void handleRecordEnd(PlayerPlaybackPlayer playbackPlayer) {
		if(ended) return;
		List<UUID> spec = new ArrayList<UUID>(spectators);
		boolean playing = false;
		for(PlayerPlaybackPlayer p : playbackPlayers) {
			if(p.isPlaying()) {
				playing = true;
			}
		}
		if(!playing) {
			playbackStarter.stopPlayback(this);
			arena.setUsing(false, null);
			ended = true;
		}
		if(overwatch && match != null) {
			new BukkitRunnable() {

				@Override
				public void run() {
					for(UUID uuid : spec) {
						Player p = Bukkit.getPlayer(uuid);
						if(p != null) {
							LegionPractice.getInstance().getOverwatchManager().openJudgementGUI(p, match);
						}
					}
				}
			}.runTaskLater(LegionPractice.getInstance(), LegionPractice.getInstance().getConfig().getLong("wait-before-teleport")*20+10);
		}
	}

	@Override
	public void forceEnd(String reason) {
		ended = true;
		if(arena.isUsing()) {
			arena.setUsing(false, null);
		}
		for(PlayerPlaybackPlayer p : playbackPlayers) {
			if(!p.getNPC().isDestroyed()) {
				p.getNPC().destroy();
			}
		}
	}

	public void addSpectator(Player p) {
		spectators.add(p.getUniqueId());
		p.teleport(arena.getCenter());
		LegionPractice.getInstance().getSpectatorHandler().addSpectator(p);
	}


	public void removeSpectator(Player p, boolean stop) {
		if(spectators.contains(p.getUniqueId())) {
			spectators.remove(p.getUniqueId());
		}
		if(spectators.size() == 0 && !hasEnded() && stop) {
			playbackStarter.stopPlayback(this);
		}
	}

	public static void removeSpectator(Player p) {
		for(HashSet<PlaybackFight> fights : RecordedMatch.fights.values()) {
			for(PlaybackFight fight : fights) {
				fight.removeSpectator(p, true);
			}
		}
		if(LegionPractice.getInstance().getSpectatorHandler().isSpectator(p)) {
			LegionPractice.getInstance().getSpectatorHandler().removeSpectator(p, true);
		}	
	}

	public PlaybackStarter getPlaybackStarter() {
		return playbackStarter;
	}

	public HashSet<PlayerPlaybackPlayer> getPlaybackPlayers() {
		return playbackPlayers;
	}

	public List<RecordedPlayer> getRecordedPlayers() {
		return recordedPlayers;
	}

	public void setRecordedPlayers(List<RecordedPlayer> recordedPlayers) {
		this.recordedPlayers = recordedPlayers;
	}

	@Override
	public SavedFight saveFight(List<UUID> winners, List<UUID> losers, List<FightInventory> winnersInventories,
			List<FightInventory> losersInventories, UUID playbackUUID) {
		return null;
	}

	public void setEnded(boolean ended) {
		this.ended = ended;
	}

}
