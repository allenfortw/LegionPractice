package tech.hexadevelopment.practice.protection;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class AntiRainListener implements Listener {
	
	
	public AntiRainListener() {
		for(World w : Bukkit.getWorlds()) {
			w.setStorm(false);
			w.setThundering(false);
		}
	}
	
	
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e) {
		e.setCancelled(true);
	}

}
