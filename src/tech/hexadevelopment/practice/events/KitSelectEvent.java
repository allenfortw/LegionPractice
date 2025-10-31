package tech.hexadevelopment.practice.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import tech.hexadevelopment.practice.battlekit.BattleKit;

public class KitSelectEvent extends Event implements Cancellable {
	
	private static HandlerList handlers = new HandlerList();
	private Player player;
	private BattleKit kit;
	private boolean cancelled;
	
	public KitSelectEvent(Player p, BattleKit kit) {
		this.player = p;
		this.kit = kit;
	}
	
	public BattleKit getKit() {
		return kit;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public void setKit(BattleKit kit) {
		this.kit = kit;
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
