package tech.hexadevelopment.practice.events;

import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.fights.party.partyfights.PartyFFA;
import tech.hexadevelopment.practice.party.Party;

public class PartyFFAEndEvent extends FightEndEvent {
	
	private Player winner;
	private Party party;

	public PartyFFAEndEvent(PartyFFA fight, Party party, Player winner) {
		super(fight);
		this.party = party;
		this.winner = winner;
	}
	
	public Party getParty() {
		return party;
	}
	
	public Player getWinner() {
		return winner;
	}
	
	@Override
	public PartyFFA getFight() {
		return (PartyFFA) super.getFight();
	}
	
}
