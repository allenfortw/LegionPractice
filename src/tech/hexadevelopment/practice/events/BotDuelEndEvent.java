package tech.hexadevelopment.practice.events;

import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.fights.botduel.BotDuel;
import tech.hexadevelopment.practice.npc.CitizensNPC;
import net.citizensnpcs.api.npc.NPC;

public class BotDuelEndEvent extends FightEndEvent {
	
	private Player player;
	private CitizensNPC bot;
	private String winner;
	
	public BotDuelEndEvent(BotDuel fight, Player player, CitizensNPC bot, String winner) {
		super(fight);
		this.player = player;
		this.bot = bot;
		this.winner = winner;
	}
	
	public NPC getBot() {
		return bot.getNPC();
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public String getWinner() {
		return winner;
	}
	
	@Override
	public BotDuel getFight() {
		return (BotDuel) super.getFight();
	}
}
