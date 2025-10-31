package tech.hexadevelopment.practice.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerUtil {

	
	public static Player getPlayer(String name) {
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(p.getName().equals(name)) {
				return p;
			}
		}
		return Bukkit.getPlayer(name);
	}
}
