package tech.hexadevelopment.practice.fights;

import org.bukkit.metadata.Metadatable;

import tech.hexadevelopment.practice.npc.CitizensNPC.Difficulty;

public interface BotFight {

	public Difficulty getDifficulty();
	
	public void setDifficulty(Difficulty difficulty);
	
	public void handleBotDeath(Metadatable ent);
	
}
