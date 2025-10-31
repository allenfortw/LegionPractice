package tech.hexadevelopment.practice.fights;

import tech.hexadevelopment.practice.battlekit.BattleKit;

public class EloChange {

	private BattleKit kit;
	private int eloChange;
	
	
	public EloChange(BattleKit kit, int eloChange) {
		this.kit = kit;
		this.eloChange = eloChange;
	}
	
	
	public int getEloChange() {
		return eloChange;
	}
	
	public BattleKit getKit() {
		return kit;
	}
}
