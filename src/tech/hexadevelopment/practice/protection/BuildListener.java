package tech.hexadevelopment.practice.protection;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.LegionPractice;

public class BuildListener implements Listener {

	@EventHandler(ignoreCancelled=true)
	public void onPlace(BlockPlaceEvent e) {
		if(e.getPlayer().getGameMode() == GameMode.CREATIVE && PermissionsManager.hasPermission(e.getPlayer(), Permission.ADMIN)) return;
		Fight fight = Fight.getCurrentFight(e.getPlayer(), LegionPractice.getInstance());
		if(fight == null || fight.getKit() == null || !fight.getKit().isBuild()) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onBreak(BlockBreakEvent e) {
		if(e.getPlayer().getGameMode() == GameMode.CREATIVE && PermissionsManager.hasPermission(e.getPlayer(), Permission.ADMIN)) return;
		Fight fight = Fight.getCurrentFight(e.getPlayer(), LegionPractice.getInstance());
		if(fight == null || fight.getKit() == null || !fight.getKit().isBuild()) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onBucketPlace(PlayerBucketEmptyEvent e) {
		if(e.getPlayer().getGameMode() == GameMode.CREATIVE && PermissionsManager.hasPermission(e.getPlayer(), Permission.ADMIN)) return;
		Fight fight = Fight.getCurrentFight(e.getPlayer(), LegionPractice.getInstance());
		if(fight == null || fight.getKit() == null || !fight.getKit().isBuild()) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onCropTrample(PlayerInteractEvent e) {
		if(e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.SOIL) e.setCancelled(true);
	}

}
