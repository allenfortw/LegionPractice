package tech.hexadevelopment.practice.events;

import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.fights.botduel.BotDuel;
import tech.hexadevelopment.practice.npc.CitizensNPC;
import net.citizensnpcs.api.npc.NPC;

public class BotDuelStartEvent extends FightStartEvent{
	
	private Player player;
	private CitizensNPC bot;
	
	public BotDuelStartEvent(BotDuel fight, Player player, CitizensNPC bot) {
		super(fight);
		this.player = player;
		this.bot = bot;
	}

	public NPC getBot() {
		return bot.getNPC();
	}
	
	public Player getPlayer() {
		return player;
	}
	
	@Override
	public BotDuel getFight() {
		return (BotDuel) super.getFight();
	}
	
}
