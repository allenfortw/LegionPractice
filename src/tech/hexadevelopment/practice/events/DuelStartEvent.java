package tech.hexadevelopment.practice.events;

import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.fights.duel.Duel;

public class DuelStartEvent extends FightStartEvent{
	
	private Player player1;
	private Player player2;
	private BattleKit kit;
	
	public DuelStartEvent(Duel fight, Player player1, Player player2, BattleKit kit) {
		super(fight);
		this.player1 = player1;
		this.player2 = player2;
		this.kit = kit;
	}
	
	public Player getPlayer1() {
		return player1;
	}
	
	public Player getPlayer2() {
		return player2;
	}
	
	public BattleKit getKit() {
		return kit;
	}
	
	public void setKit(BattleKit kit) {
		this.kit = kit;
	}
	
	@Override
	public Duel getFight() {
		return (Duel) super.getFight();
	}
	
}
