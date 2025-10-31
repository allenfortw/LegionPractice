package tech.hexadevelopment.practice.events;

import tech.hexadevelopment.practice.fights.party.partyfights.PartySplit;
import tech.hexadevelopment.practice.party.Party;

public class PartySplitStartEvent extends FightStartEvent{
	
	private Party party;
	
	public PartySplitStartEvent(PartySplit fight, Party party) {
		super(fight);
		this.party = party;
	}
	
	public Party getParty() {
		return party;
	}
	
	@Override
	public PartySplit getFight() {
		return (PartySplit) super.getFight();
	}
}
