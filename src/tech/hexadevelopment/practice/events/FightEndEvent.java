package tech.hexadevelopment.practice.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import tech.hexadevelopment.practice.fights.Fight;

public class FightEndEvent extends Event {
	
	
	private static HandlerList handlers = new HandlerList();
	private Fight fight;
	
	public FightEndEvent(Fight fight) {
		this.fight = fight;
	}
	
	public Fight getFight() {
		return fight;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }
}