package tech.hexadevelopment.practice.fights.requests;

import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsParty;
import tech.hexadevelopment.practice.party.Party;

public class PartyVsPartyRequest implements Request{

	private long startTime;
	private static int seconds = 15;
	private Party dueler;
	private Party dueled;
	private PartyVsParty fight;
	
	
	public PartyVsPartyRequest(Party dueler, Party dueled, PartyVsParty fight) {
		this.startTime = System.currentTimeMillis();
		this.dueler = dueler;
		this.dueled = dueled;
		this.fight = fight;
	}
	
	@Override
	public boolean hasExpired() {
		return startTime/1000+seconds < System.currentTimeMillis()/1000;
	}
	
	@Override
	public Fight getFight() {
		return fight;
	}
	
	public Party getDueled() {
		return dueled;
	}
	
	public Party getDueler() {
		return dueler;
	}
}
