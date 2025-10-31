package tech.hexadevelopment.practice.events;

import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsBots;
import tech.hexadevelopment.practice.party.Party;

public class PartyVsBotsEndEvent extends FightEndEvent{
	
	private Party party;
	private Winners winners;
	
	public PartyVsBotsEndEvent(PartyVsBots fight, Party party, Winners winners) {
		super(fight);
		this.winners = winners;
		this.party = party;
	}
	
	public Party getParty() {
		return party;
	}
	
	public Winners getWinners() {
		return winners;
	}
	
	@Override
	public PartyVsBots getFight() {
		return (PartyVsBots) super.getFight();
	}
	
	
	public enum Winners {
		PLAYERS,
		BOTS,
		FORCE_END;
	}
}
