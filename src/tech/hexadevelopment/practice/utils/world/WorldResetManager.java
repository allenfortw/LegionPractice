package tech.hexadevelopment.practice.utils.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public class WorldResetManager {

	private static String world;
	private static boolean goingToRollBack;
	
	public static boolean rollBack() {
		World w = Bukkit.getWorld(world);
		if(w == null) return false;
		if(getPlayersInWorld() > 0) return false;
        if(Bukkit.getServer().unloadWorld(w, false)) {
            Bukkit.getLogger().info("Unloaded the world " + w.getName());
        }
        else {
            Bukkit.getLogger().severe("Failed to unload the world " + w.getName());
            return false;
        }
        Bukkit.getServer().createWorld(new WorldCreator(w.getName()));
        setGoingToRollBack(false);
        return true;
	}
	
	public static int getPlayersInWorld() {
		World w = Bukkit.getWorld(world);
		if(w == null) return 0;
		return w.getPlayers().size();
	}
	
	public static boolean isGoingToRollBack() {
		return goingToRollBack;
	}
	
	public static String getWorld() {
		return world;
	}
	
	public static void setGoingToRollBack(boolean goingToRollBack) {
		WorldResetManager.goingToRollBack = goingToRollBack;
	}
	
	public static void setWorld(String world) {
		WorldResetManager.world = world;
	}
}
