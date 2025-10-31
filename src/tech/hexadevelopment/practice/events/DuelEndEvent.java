package tech.hexadevelopment.practice.events;

import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.fights.duel.Duel;

public class DuelEndEvent extends FightEndEvent {
	
	private Player winner;
	private Player loser;
	
	public DuelEndEvent(Duel fight, Player winner, Player loser) {
		super(fight);
		this.winner = winner;
		this.loser = loser;
	}
	
	public Player getLoser() {
		return loser;
	}
	
	public Player getWinner() {
		return winner;
	}
	
	@Override
	public Duel getFight() {
		return (Duel) super.getFight();
	}
}
