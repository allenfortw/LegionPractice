package tech.hexadevelopment.practice.fights;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fightinventory.FightInventory;
import tech.hexadevelopment.practice.fights.savedfights.SavedFight;

public class SimpleFight extends Fight{

	@Override
	public void start() {}

	@Override
	public boolean allowSpectating() {
		return false;
	}

	@Override
	public boolean hasEnded() {
		return ended != 0;
	}

	@Override
	public boolean canStart() {
		return true;
	}

	@Override
	public void handleDeath(Player p) {
		Fight.setCurrentFight(p, null, LegionPractice.getInstance());
	}

	@Override
	public void forceEnd(String reason) {
		for(Player p : Bukkit.getOnlinePlayers()) {
			Fight f = Fight.getCurrentFight(p, LegionPractice.getInstance());
			if(f != null && f.equals(this)) {
				p.sendMessage(reason);
				Fight.setCurrentFight(p, null, LegionPractice.getInstance());
				LegionPractice.getInstance().clear(p, true, true);
			}
		}
	}

	@Override
	public SavedFight saveFight(List<UUID> winners, List<UUID> losers, List<FightInventory> winnersInventories,
			List<FightInventory> losersInventories, UUID playbackUUID) {
		return null;
	}

}
