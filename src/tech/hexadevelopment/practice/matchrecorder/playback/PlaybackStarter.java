package tech.hexadevelopment.practice.matchrecorder.playback;

import java.util.UUID;

public abstract class PlaybackStarter {
	
	protected UUID uuid;
	
	public abstract PlaybackFight startPlayback(boolean overwatch);
	
	public abstract void stopPlayback(PlaybackFight fight);
	
	public UUID getUUID() {
		if(uuid == null) uuid = UUID.randomUUID();
		return uuid;
	}
	
}
