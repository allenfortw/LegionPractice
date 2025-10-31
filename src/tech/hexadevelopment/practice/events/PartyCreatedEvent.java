package tech.hexadevelopment.practice.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import tech.hexadevelopment.practice.party.Party;

public class PartyCreatedEvent extends Event{

	private static HandlerList handlers = new HandlerList();
	private Party party;
	
	public PartyCreatedEvent(Party party) {
		this.party = party;
	}
	
	public Party getParty() {
		return party;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
