package tech.hexadevelopment.practice.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.NumberConversions;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.FightsListener;

public class Teleporter {
	
	
	public static boolean teleport(Player p, Location to, boolean noDefaultWorld) {
		if(to.getWorld() == null || Bukkit.getWorld(to.getWorld().getName()) == null) return false;
		if(noDefaultWorld && to.getWorld().getName().equals(Bukkit.getWorlds().get(0).getName())) return false;
		if(Bukkit.isPrimaryThread()) {
			boolean b = execute(p, to);
			if(!b) {
				return LegionPractice.getInstance().getConfig().getBoolean("allow-unsafe-teleport");
			}
			return b;
		}
		new BukkitRunnable() {
			
			@Override
			public void run() {
				execute(p, to);
			}
		}.runTask(LegionPractice.getInstance());
		return true;
	}
	
	public static void syncTeleport(Entity ent, Location to) {
		if(Bukkit.isPrimaryThread()) {
			ent.teleport(to);
			return;
		}
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if(ent != null) {
					ent.teleport(to);
				}
			}
		}.runTask(LegionPractice.getInstance());
	}
	
	private static boolean execute(Player p, Location to) {
		for(Entity ent : to.getChunk().getEntities()) {
			if(ent instanceof Item && ent.hasMetadata(FightsListener.itemDrop)) {
				ent.remove();
			}
		}
		Block b = to.clone().subtract(0, 1, 0).getBlock();
		if(b.getType() != null && b.getType().isSolid()) {
			to.add(0, 1, 0);
		}
		for(int i = 0; i < 3; i++) {
			if(to.getBlock().getType().isSolid()) {
				to.add(0, 1, 0);
			}
		}
		if(p.teleport(to)) {
			Location l = p.getLocation().clone();
			Location l2 = to.clone();
			if(l.getWorld().getName().equals(l2.getWorld().getName())) {
				return dist(l.getX(), l2.getX(), l.getZ(), l2.getZ()) <= 3*3;
			}
		}
		return false;
	}
	
	private static double dist(double x1, double x2, double z1, double z2) {
		return NumberConversions.square(x1 - x2) + NumberConversions.square(z1 - z2);
	}
}
