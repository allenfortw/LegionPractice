package tech.hexadevelopment.practice.protection;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

import tech.hexadevelopment.practice.utils.BlockUtil;

public class AntiLavaGenerator implements Listener{

	
	
	@EventHandler(ignoreCancelled=true)
	public void onFromTo(BlockFromToEvent e){
		Material mat = e.getBlock().getType();
		Material mat1 = (mat == Material.WATER || mat == Material.STATIONARY_WATER ? Material.LAVA : Material.WATER);
		Material mat2 = (mat == Material.WATER || mat == Material.STATIONARY_WATER ? Material.STATIONARY_LAVA : Material.STATIONARY_WATER);
		for(BlockFace face : BlockUtil.blockFaces){
			Block r = e.getBlock().getRelative(face, 1);
			if(r.getType() == mat1 || r.getType() == mat2){
				e.setCancelled(true);
				return;
			}
		}
	}
	
}
