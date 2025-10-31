package tech.hexadevelopment.practice.fights.party.holders;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import tech.hexadevelopment.practice.party.Party;

public class PartyFFAHolder implements InventoryHolder{

	private Party party;

	public PartyFFAHolder(Party party) {
		this.party = party;
	}

	@Override
	public Inventory getInventory() {
		return null;
	}

	public Party getParty() {
		return party;
	}
	
}
