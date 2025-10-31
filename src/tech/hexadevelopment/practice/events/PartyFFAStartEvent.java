package tech.hexadevelopment.practice.events;

import tech.hexadevelopment.practice.fights.party.partyfights.PartyFFA;
import tech.hexadevelopment.practice.party.Party;

public class PartyFFAStartEvent extends FightStartEvent {

	private Party party;


	public PartyFFAStartEvent(PartyFFA fight, Party party) {
		super(fight);
		this.party = party;
	}

	public Party getParty() {
		return party;
	}
	
	
	@Override
	public PartyFFA getFight() {
		return (PartyFFA) super.getFight();
	}
	
}
