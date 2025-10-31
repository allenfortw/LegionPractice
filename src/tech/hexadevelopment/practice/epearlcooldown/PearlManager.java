package tech.hexadevelopment.practice.epearlcooldown;

import java.text.DecimalFormat;

import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

import tech.hexadevelopment.practice.LegionPractice;

public class PearlManager {
	
	private static String pearlName;
	private static double cooldown;
	private static int interval;
	private static DecimalFormat format;
	
	
	public static void load(LegionPractice plugin) {
		setFormat(new DecimalFormat(plugin.getConfig().getString("enderpearl-cooldown.format")));
		setPearlName(plugin.getConfig().getString("enderpearl-cooldown.pearl-name"));
		setCooldown(plugin.getConfig().getDouble("enderpearl-cooldown.cooldown"));
		setInterval(plugin.getConfig().getInt("enderpearl-cooldown.interval"));
		if(interval < 20 && LegionPractice.performanceMode) interval = 20;
	}
	
	
	public MetadataValue getMetadata(Metadatable m, String tag) {
		for (MetadataValue mv : m.getMetadata(tag))
			if (mv.getOwningPlugin().equals(this)) {
				return mv;
			}
		return null;
	}
	
	public static DecimalFormat getFormat() {
		return format;
	}
	
	public static void setFormat(DecimalFormat format) {
		PearlManager.format = format;
	}
	
	public static double getCooldown() {
		return cooldown;
	}
	
	public static void setCooldown(double cooldown) {
		PearlManager.cooldown = cooldown;
	}
	
	public static String getPearlName() {
		return pearlName;
	}
	
	public static void setPearlName(String pearlName) {
		PearlManager.pearlName = pearlName;
	}
	
	public static int getInterval() {
		return interval;
	}
	
	public static void setInterval(int task) {
		interval = task;
	}
}
