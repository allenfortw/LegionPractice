package tech.hexadevelopment.practice.fights.party.actions;

import org.bukkit.inventory.ItemStack;

import tech.hexadevelopment.practice.party.Party;

public abstract class PartyAction {
	
	private String name;
	
	public PartyAction(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public abstract boolean start(Party party);
	
	public abstract ItemStack getIcon();
	
	public abstract int getSlot();
	
}
