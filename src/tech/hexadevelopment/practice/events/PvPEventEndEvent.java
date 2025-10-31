package tech.hexadevelopment.practice.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import tech.hexadevelopment.practice.hostedevents.PvPEvent;

public class PvPEventEndEvent extends Event {

	private static HandlerList handlers = new HandlerList();
	private PvPEvent event;
	private Player winner;
	
	public PvPEventEndEvent(PvPEvent event, Player winner) {
		this.event = event;
		this.winner = winner;
	}
	
	public String getPvPEventName() {
		return event != null ? event.getClass().getSimpleName() : null;
	}
	
	public void setEvent(PvPEvent event) {
		this.event = event;
	}
	
	public PvPEvent getEvent() {
		return event;
	}
	
	public Player getWinner() {
		return winner;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
}
