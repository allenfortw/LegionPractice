package tech.hexadevelopment.practice.arena;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import tech.hexadevelopment.practice.utils.BlockUtil;
import tech.hexadevelopment.practice.utils.LocationUtil;
import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.Fight;

public class RollbackListener implements Listener{


	public static String PLACED_IN_FIGHT = "LegionPracticePlacedInFight";

	private LegionPractice plugin;

	public RollbackListener(LegionPractice plugin) {
		this.plugin = plugin;
		CachedBlockChange.CACHE_BLOCKS = plugin.getConfig().getBoolean("cache-rollback-blocks");
	}


	@EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
	public void onBlockFromTo(BlockFromToEvent e) {
		if(e.getBlock().hasMetadata(PLACED_IN_FIGHT)) {
			MetadataValue mv = plugin.getMetadata(e.getBlock(), PLACED_IN_FIGHT);
			if(mv != null && mv.value() != null && mv.value() instanceof Fight) {
				Fight fight = (Fight) mv.value();
				if(fight.hasEnded()) {
					e.setCancelled(true);
				}
				else if(!e.getToBlock().getType().isSolid()){
					e.getToBlock().setMetadata(PLACED_IN_FIGHT, new FixedMetadataValue(plugin, fight));
					if(CachedBlockChange.CACHE_BLOCKS) {
						fight.addBlockChange(new CachedBlockChange(e.getToBlock().getLocation(), Material.AIR, (byte) 0));
						Block b2 = e.getToBlock().getLocation().subtract(0, 1, 0).getBlock();
						if(turnsToDirt(b2)) {
							fight.addBlockChange(new CachedBlockChange(b2.getLocation(), b2));
						}
					}
				}
			}
		}
		else {
			for(BlockFace face : BlockUtil.blockFaces){
				Block b = e.getBlock().getRelative(face, 1);
				if(b.hasMetadata(PLACED_IN_FIGHT)) {
					MetadataValue mv = plugin.getMetadata(b, PLACED_IN_FIGHT);
					if(mv != null && mv.value() != null && mv.value() instanceof Fight) {
						Fight fight = (Fight) mv.value();
						if(fight != null && !e.getToBlock().getType().isSolid()){
							e.getToBlock().setMetadata(PLACED_IN_FIGHT, new FixedMetadataValue(plugin, fight));
							if(CachedBlockChange.CACHE_BLOCKS) {
								fight.addBlockChange(new CachedBlockChange(e.getToBlock().getLocation(), Material.AIR, (byte) 0));
								Block b2 = e.getToBlock().getLocation().subtract(0, 1, 0).getBlock();
								if(turnsToDirt(b2)) {
									fight.addBlockChange(new CachedBlockChange(b2.getLocation(), b2));
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
	public void onBucketEmpty(PlayerBucketEmptyEvent e) {
		Fight fight = Fight.getCurrentFight(e.getPlayer(), plugin);
		if(fight != null) {
			e.getBlockClicked().getRelative(e.getBlockFace()).setMetadata(RollbackListener.PLACED_IN_FIGHT, new FixedMetadataValue(plugin, fight));
			if(CachedBlockChange.CACHE_BLOCKS) {
				if(e.getBlockClicked().getLocation().equals(e.getBlockClicked().getRelative(e.getBlockFace()).getLocation())) {
					fight.addBlockChange(new CachedBlockChange(e.getBlockClicked().getRelative(e.getBlockFace()).getLocation(), e.getBlockClicked()));
				}
				else {
					fight.addBlockChange(new CachedBlockChange(e.getBlockClicked().getRelative(e.getBlockFace()).getLocation(), Material.AIR, (byte) 0));
					Block b2 = e.getBlockClicked().getRelative(e.getBlockFace()).getLocation().subtract(0, 1, 0).getBlock();
					if(turnsToDirt(b2)) {
						fight.addBlockChange(new CachedBlockChange(b2.getLocation(), b2));
					}
				}
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(e.getClickedBlock() != null && (e.getClickedBlock().getType() == Material.CHEST || e.getClickedBlock().getType() == Material.TRAPPED_CHEST)) {
				Fight fight = Fight.getCurrentFight(e.getPlayer(), plugin);
				if(fight != null) {
					fight.addBlockChange(new CachedBlockChange(e.getClickedBlock().getLocation(), e.getClickedBlock()));
				}
			}
		}
	}


	@EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
	public void onExplosion(EntityExplodeEvent e) {
		Location loc = e.getLocation();
		if(!plugin.getConfig().getBoolean("rollback-arenas")) return;
		for(Arena arena : plugin.arenas) {
			if(arena.getCorner1() != null && arena.getCorner2() != null
					&& LocationUtil.isInregion(loc, arena.getCorner1(), arena.getCorner2())) {
				if(arena.isBuild() && arena.getCurrentFight() != null && CachedBlockChange.CACHE_BLOCKS) {
					for(Block b : e.blockList()) {
						arena.getCurrentFight().addBlockChange(new CachedBlockChange(b.getLocation(), b));
					}
					return;
				}
				else {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
	public void onPlace(BlockPlaceEvent e) {
		Fight fight = Fight.getCurrentFight(e.getPlayer(), plugin);
		if(fight != null) {
			if(CachedBlockChange.CACHE_BLOCKS) {
				fight.addBlockChange(new CachedBlockChange(e.getBlockPlaced().getLocation(), e.getBlockReplacedState().getType(), e.getBlockReplacedState().getRawData()));
				Block b2 = e.getBlockPlaced().getLocation().subtract(0, 1, 0).getBlock();
				if(turnsToDirt(b2)) {
					fight.addBlockChange(new CachedBlockChange(b2.getLocation(), b2));
				}
			}
			else {
				e.getBlockPlaced().setMetadata(RollbackListener.PLACED_IN_FIGHT, new FixedMetadataValue(plugin, true));
			}
		}
	}

	@EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
	public void onBreak(BlockBreakEvent e) {
		if(CachedBlockChange.CACHE_BLOCKS) {
			Fight fight = Fight.getCurrentFight(e.getPlayer(), plugin);
			if(fight != null) {
				fight.addBlockChange(new CachedBlockChange(e.getBlock().getLocation(), e.getBlock()));
				Block b2 = e.getBlock().getLocation().subtract(0, 1, 0).getBlock();
				if(b2.getType() == Material.DIRT) {
					fight.addBlockChange(new CachedBlockChange(b2.getLocation(), b2));
				}
			}
		}
	}

	public static boolean turnsToDirt(Block b) {
		return b.getType() == Material.GRASS || b.getType() == Material.MYCEL || (b.getType() == Material.DIRT && b.getData() == 2);
	}
}
