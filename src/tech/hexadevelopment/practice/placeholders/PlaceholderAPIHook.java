package tech.hexadevelopment.practice.placeholders;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;
import me.clip.placeholderapi.external.EZPlaceholderHook;

public class PlaceholderAPIHook extends EZPlaceholderHook{


	private static String identifier = "LegionPractice";

	private LegionPractice plugin;

	public PlaceholderAPIHook(LegionPractice plugin) {
		super(plugin, identifier);
		this.plugin = plugin;
	}

	@Override
	public String onPlaceholderRequest(Player p, String s) {
		try{
			if(s == null) return null;
			HashMap<String, String> placeholders;
			if(p == null) {
				placeholders = plugin.getPlaceholders().getCommonPlaceHolders("");
				for(Entry<String, String> e : placeholders.entrySet()) {
					if(e.getKey().replace("<", "").replace(">", "").equals(s)) {
						return e.getValue();
					}
				}
			}
			else {
				return plugin.getPlaceholders().doPlaceholders(p, "<" + s + ">", "", true);
			}
		}catch(Exception e) {}
		return null;
	}



}
