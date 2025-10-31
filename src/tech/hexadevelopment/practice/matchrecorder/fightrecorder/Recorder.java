package tech.hexadevelopment.practice.matchrecorder.fightrecorder;

import java.util.List;

import tech.hexadevelopment.practice.fightinventory.FightInventory;
import tech.hexadevelopment.practice.matchrecorder.RecordedMatch;

public abstract class Recorder {

	protected RecordedMatch recordedMatch;
	
	
	public RecordedMatch getRecordedMatch() {
		return recordedMatch;
	}
	
	public void setRecordedMatch(RecordedMatch recordedMatch) {
		this.recordedMatch = recordedMatch;
	}
	
	public abstract void startRecording();
	
	public abstract RecordedMatch stopRecording(List<FightInventory> fightInventories);
	
}
