package tech.hexadevelopment.practice.strikecheat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import tech.hexadevelopment.practice.LegionPractice;

public class StrikeCheatCheck {

	public static boolean debug = false;

	public static HashMap<UUID, Integer> firstTimers = new HashMap<UUID, Integer>();
	public static String[] required = new String[]{
			"com.gmail.thetoppe5.strikecheat.logger.logger",
			"com.gmail.thetoppe5.strikecheat.checks.antihack.aimassist",
			"com.gmail.thetoppe5.strikecheat.checks.antihack.aimbot",
			"com.gmail.thetoppe5.strikecheat.checks.antihack.antihackcheck",
			"com.gmail.thetoppe5.strikecheat.checks.antihack.antihackmanager",
			"com.gmail.thetoppe5.strikecheat.checks.antihack.autopotion",
			"com.gmail.thetoppe5.strikecheat.checks.antihack.autosoup",
			"com.gmail.thetoppe5.strikecheat.checks.antihack.killauraownlook",
			"com.gmail.thetoppe5.strikecheat.checks.antihack.killaura",
			"com.gmail.thetoppe5.strikecheat.checks.antihack.knockbackmodifier",
			"com.gmail.thetoppe5.strikecheat.checks.antihack.reducedknockback",
			"com.gmail.thetoppe5.strikecheat.checks.antihack.otherantiknockbacklike",
			"com.gmail.thetoppe5.strikecheat.checks.antihack.otherkillauralike",
			"com.gmail.thetoppe5.strikecheat.checks.antihack.otherautoclickerlike",
			"com.gmail.thetoppe5.strikecheat.checks.antihack.smoothaimbot",
			"com.gmail.thetoppe5.strikecheat.checks.antihack.triggerbot",
			"com.gmail.thetoppe5.strikecheat.checks.antihack.velocitymodifier"
	};
	public static String[] notAllowed = new String[]{
			"thehen",
			"killaura",
			"triggerbot",
			"autoclicker",
			"aimassist",
			"aimbot",
			"smoothaim",
			"clicker",
			
	};

	private boolean legit = true;

	public StrikeCheatCheck(UUID uuid, String data) {
		data = data.toLowerCase();
		if(debug) {
			File file = new File(LegionPractice.getInstance().getDataFolder() + File.separator + "data.yml");
			if(file.exists())
				try {
					file.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
			conf.set("data", data);
			try {
				conf.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(firstTimers.containsKey(uuid)) {
			if(firstTimers.get(uuid) > data.length()) {
				legit = false;
				Bukkit.getLogger().info(uuid.toString() + " failed (0): " + data.length());
			}
		}
		else {
			firstTimers.put(uuid, data.length());
		}
		if(data.contains("thehen") || data.length() < 10000) legit = false;
		int counter = 0;
		for(String s : required) {
			if(data.contains(s)) {
				//Bukkit.getLogger().info("Contains: " + s);
				data = data.replace(s, "");
			}
			else {
				legit = false;
				Bukkit.getLogger().info(uuid.toString() + " failed (1): " + counter);
				break;
			}
			counter++;
		}
		counter = 0;
		for(String s : notAllowed) {
			if(data.contains(s)) {
				legit = false;
				//int index = data.indexOf(s);
				Bukkit.getLogger().info(uuid.toString() + " failed (2): " + counter);
				break;
			}
			counter++;
		}
		if(debug) {
			File file = new File(LegionPractice.getInstance().getDataFolder() + File.separator + "data-2.yml");
			if(file.exists())
				try {
					file.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
			conf.set("data", data);
			try {
				conf.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isLegit() {
		return legit;
	}

}
