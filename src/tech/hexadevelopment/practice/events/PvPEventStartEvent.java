package tech.hexadevelopment.practice.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import tech.hexadevelopment.practice.hostedevents.PvPEvent;

public class PvPEventStartEvent extends Event implements Cancellable{

	private static HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private PvPEvent event;
	
	public PvPEventStartEvent(PvPEvent event) {
		this.event = event;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
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
