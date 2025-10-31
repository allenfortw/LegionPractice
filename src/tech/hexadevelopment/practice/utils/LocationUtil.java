package tech.hexadevelopment.practice.utils;

import org.bukkit.Location;

import tech.hexadevelopment.practice.LegionPractice;

public class LocationUtil {

	public static boolean isInregion(Location loc, Location corner1, Location corner2) {
		if(loc == null || corner1 == null || corner2 == null) return false;
		if(loc.getWorld() == null || corner1.getWorld() == null
				|| corner2.getWorld() == null || !loc.getWorld().getName().equals(corner1.getWorld().getName())
				|| !corner2.getWorld().getName().equals(corner1.getWorld().getName())) {
			return false;
		}
		int x = loc.getBlockX();
		int z = loc.getBlockZ();
		int y = loc.getBlockY();
		int bottomX = corner1.getBlockX() > corner2.getBlockX() ? corner2.getBlockX() : corner1.getBlockX();
		int bottomY = corner1.getBlockY() > corner2.getBlockY() ? corner2.getBlockY() : corner1.getBlockY();
		int bottomZ = corner1.getBlockZ() > corner2.getBlockZ() ? corner2.getBlockZ() : corner1.getBlockZ();
		int upperX = corner1.getBlockX() < corner2.getBlockX() ? corner2.getBlockX() : corner1.getBlockX();
		int upperY = corner1.getBlockY() < corner2.getBlockY() ? corner2.getBlockY() : corner1.getBlockY();
		int upperZ = corner1.getBlockZ() < corner2.getBlockZ() ? corner2.getBlockZ() : corner1.getBlockZ();
		return x >= bottomX && x <= upperX && y >= bottomY && y <= upperY && z >= bottomZ && z <= upperZ;
	}
	
	
	public static Location subtractIgnoreWorld(Location loc, Location loc2) {
		return loc.subtract(loc2.getX(), loc2.getY(), loc2.getZ());
	}


	public static Location randomLocation(Location location, int i, boolean randomY) {
		return location.add(LegionPractice.random.nextInt(i+i)-i, randomY ? LegionPractice.random.nextInt(i+i)-i : 0, LegionPractice.random.nextInt(i+i)-i);
	}
	
	
}
