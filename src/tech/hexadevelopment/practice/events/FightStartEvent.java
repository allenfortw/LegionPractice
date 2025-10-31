package tech.hexadevelopment.practice.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import tech.hexadevelopment.practice.fights.Fight;

public class FightStartEvent extends Event implements Cancellable{

	
	private static HandlerList handlers = new HandlerList();
	private Fight fight;
	private boolean cancelled;
	
	public FightStartEvent(Fight fight) {
		this.fight = fight;
	}
	
	public Fight getFight() {
		return fight;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
