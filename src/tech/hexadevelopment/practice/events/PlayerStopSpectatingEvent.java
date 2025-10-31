package tech.hexadevelopment.practice.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import tech.hexadevelopment.practice.fights.Fight;

public class PlayerStopSpectatingEvent extends Event implements Cancellable{


	private static HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private Player player;
	private Fight fight;
	
	public PlayerStopSpectatingEvent(Player player, Fight fight) {
		this.player = player;
		this.fight = fight;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Fight getFight() {
		return fight;
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
