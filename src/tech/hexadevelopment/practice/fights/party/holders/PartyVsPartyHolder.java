package tech.hexadevelopment.practice.fights.party.holders;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import tech.hexadevelopment.practice.party.Party;

public class PartyVsPartyHolder implements InventoryHolder {

	private Party party;
	private Party party2;

	public PartyVsPartyHolder(Party party, Party party2) {
		this.party = party;
		this.party2 = party2;
	}

	@Override
	public Inventory getInventory() {
		return null;
	}

	public Party getParty() {
		return party;
	}

	public Party getParty2() {
		return party2;
	}
}
