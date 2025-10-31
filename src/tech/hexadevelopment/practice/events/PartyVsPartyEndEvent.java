package tech.hexadevelopment.practice.events;

import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsParty;
import tech.hexadevelopment.practice.party.Party;

public class PartyVsPartyEndEvent extends FightEndEvent {
	
	private Party winner;
	private Party loser;
	
	public PartyVsPartyEndEvent(PartyVsParty fight, Party winner, Party loser) {
		super(fight);
		this.winner = winner;
		this.loser = loser;
	}
	
	public Party getLoser() {
		return loser;
	}
	
	public Party getWinner() {
		return winner;
	}
	
	@Override
	public PartyVsParty getFight() {
		return (PartyVsParty) super.getFight();
	}
	
}
