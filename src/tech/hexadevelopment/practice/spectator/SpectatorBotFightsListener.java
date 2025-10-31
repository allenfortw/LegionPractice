package tech.hexadevelopment.practice.spectator;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import tech.hexadevelopment.practice.events.BotDuelEndEvent;
import tech.hexadevelopment.practice.events.PartyVsBotsEndEvent;

public class SpectatorBotFightsListener implements Listener {

	
	private SpectatorFightsListener fightsListener;
	
	public SpectatorBotFightsListener(SpectatorFightsListener fightsListener) {
		this.fightsListener = fightsListener;
	}

	@EventHandler
	public void onBotEnd(BotDuelEndEvent e) {
		fightsListener.handleFightEnd(e);
	}
	
	@EventHandler
	public void onPartyVsBotsEnd(PartyVsBotsEndEvent e) {
		fightsListener.handleFightEnd(e);
	}
}
