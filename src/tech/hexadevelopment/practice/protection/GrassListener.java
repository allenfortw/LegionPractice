package tech.hexadevelopment.practice.protection;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;

public class GrassListener implements Listener {


	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
	public void onBlockSpread(BlockSpreadEvent e){
		if(e.getSource().getType() == Material.DIRT) {
			e.setCancelled(true);
		}
	}

}
