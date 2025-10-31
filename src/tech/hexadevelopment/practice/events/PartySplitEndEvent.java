package tech.hexadevelopment.practice.events;

import java.util.HashSet;

import tech.hexadevelopment.practice.fights.party.partyfights.PartySplit;
import tech.hexadevelopment.practice.party.Party;

public class PartySplitEndEvent extends FightEndEvent{
	
	private Party party;
	private HashSet<String> losers, winners;
	
	public PartySplitEndEvent(PartySplit fight, Party party, HashSet<String> winners, HashSet<String> losers) {
		super(fight);
		this.party = party;
		this.losers = losers;
		this.winners = winners;
	}
	
	public Party getParty() {
		return party;
	}
	
	public HashSet<String> getWinners() {
		return winners;
	}
	
	public HashSet<String> getLosers() {
		return losers;
	}
	
	@Override
	public PartySplit getFight() {
		return (PartySplit) super.getFight();
	}
}
