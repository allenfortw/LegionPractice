package tech.hexadevelopment.practice.events;

import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsParty;
import tech.hexadevelopment.practice.party.Party;

public class PartyVsPartyStartEvent extends FightStartEvent{
	
	private Party challangerParty;
	private Party enemyParty;
	
	public PartyVsPartyStartEvent(PartyVsParty fight, Party challengerParty, Party enemyParty) {
		super(fight);
		this.challangerParty = challengerParty;
		this.enemyParty = enemyParty;
	}
	
	public Party getChallangerParty() {
		return challangerParty;
	}
	
	public Party getEnemyParty() {
		return enemyParty;
	}
	
	@Override
	public PartyVsParty getFight() {
		return (PartyVsParty) super.getFight();
	}
	
}
