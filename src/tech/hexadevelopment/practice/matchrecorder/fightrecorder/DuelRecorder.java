package tech.hexadevelopment.practice.matchrecorder.fightrecorder;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.fightinventory.FightInventory;
import tech.hexadevelopment.practice.fights.duel.Duel;
import tech.hexadevelopment.practice.matchrecorder.RecordedMatch;
import tech.hexadevelopment.practice.matchrecorder.recorder.PlayerRecorder;
import tech.hexadevelopment.practice.LegionPractice;

public class DuelRecorder extends Recorder{

	private Duel duel;
	private PlayerRecorder player1Recorder;
	private PlayerRecorder player2Recorder;
	private boolean started;

	public DuelRecorder(Duel duel) {
		this.duel = duel;
	}

	@Override
	public void startRecording() {
		started = true;
		Player player1 = Bukkit.getPlayer(duel.getP1());
		Player player2 = Bukkit.getPlayer(duel.getP2());
		HashMap<UUID, String> players = new HashMap<UUID, String>();
		players.put(player1.getUniqueId(), player1.getName());
		players.put(player2.getUniqueId(), player2.getName());
		recordedMatch = new RecordedMatch(players, duel.getKit(), duel.getArena());
		recordedMatch.setArena(duel.getArena());
		player1Recorder = new PlayerRecorder(player1, duel.getArena().getCenter());
		player2Recorder = new PlayerRecorder(player2, duel.getArena().getCenter());
	}

	@Override
	public RecordedMatch stopRecording(List<FightInventory> fightInventories) {
		if(!started) return null;
		if(recordedMatch != null && player1Recorder != null && player2Recorder != null) {
			recordedMatch.getRecordedPlayers().add(player1Recorder.stopRecording());
			recordedMatch.getRecordedPlayers().add(player2Recorder.stopRecording());
			if(!LegionPractice.getInstance().getConfig().getBoolean("save-all-fights") && !(duel.getKit().isElo() && LegionPractice.getInstance().getConfig().getBoolean("save-elo-fights"))) {
				if(LegionPractice.getInstance().mySQL == null || !LegionPractice.getInstance().mySQL.shouldBeConnected) {
					recordedMatch.setFightInventories(fightInventories);
				}

			}
			recordedMatch.saveToFile(true);
		}
		return recordedMatch;
	}
	
	public boolean hasStartedRecording() {
		return started;
	}
}
