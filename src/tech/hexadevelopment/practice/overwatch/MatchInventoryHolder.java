package tech.hexadevelopment.practice.overwatch;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import tech.hexadevelopment.practice.matchrecorder.RecordedMatch;

public class MatchInventoryHolder implements InventoryHolder {

	private RecordedMatch match;

	public MatchInventoryHolder(RecordedMatch match) {
		this.match = match;
	}	

	public RecordedMatch getMatch() {
		return match;
	}

	@Override
	public Inventory getInventory() {
		return null;
	}
}