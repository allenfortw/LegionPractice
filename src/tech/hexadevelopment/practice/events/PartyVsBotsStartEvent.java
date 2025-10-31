package tech.hexadevelopment.practice.events;

import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsBots;
import tech.hexadevelopment.practice.party.Party;

public class PartyVsBotsStartEvent extends FightStartEvent{
	
	private Party party;
	
	public PartyVsBotsStartEvent(PartyVsBots fight, Party party) {
		super(fight);
		this.party = party;
	}
	
	public Party getParty() {
		return party;
	}
	
	@Override
	public PartyVsBots getFight() {
		return (PartyVsBots) super.getFight();
	}
}
