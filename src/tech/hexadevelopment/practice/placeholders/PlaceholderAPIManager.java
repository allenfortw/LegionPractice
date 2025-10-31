package tech.hexadevelopment.practice.placeholders;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;
import me.clip.placeholderapi.PlaceholderAPI;

public class PlaceholderAPIManager {


	private static PlaceholderAPIManager manager;
	private static boolean enabled;
	
	public PlaceholderAPIManager(LegionPractice plugin) {
		manager = this;
		if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			new PlaceholderAPIHook(plugin).hook();
			enabled = true;
		}
	}
	
	public String handlePlaceHolders(Player p, String text) {
		if(!enabled || text == null) return text;
		return PlaceholderAPI.setPlaceholders(p, text);
	}
	
	public static PlaceholderAPIManager getManager() {
		return manager != null ? manager : (manager = new PlaceholderAPIManager(LegionPractice.getInstance()));
	}
}
