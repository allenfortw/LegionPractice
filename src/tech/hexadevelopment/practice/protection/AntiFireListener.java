package tech.hexadevelopment.practice.protection;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;

public class AntiFireListener implements Listener {


	@EventHandler(ignoreCancelled=true)
	public void onFireDestroyBlock(BlockBurnEvent e) {
		e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled=true)
	public void onBlockIgnite(BlockIgniteEvent e) {
		if(e.getCause() != IgniteCause.FLINT_AND_STEEL) {
			e.setCancelled(true);
		}
	}
}
