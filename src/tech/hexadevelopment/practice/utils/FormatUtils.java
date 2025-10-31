package tech.hexadevelopment.practice.utils;

import org.bukkit.potion.PotionEffect;
/**
 * Utility class for formating things to strings.
 * @author Toppe5
 * @since 0.1
 */
public class FormatUtils {

	/**
	 * Format seconds to secodns and minutes
	 * @param seconds seconds to format
	 * @return a formated string
	 */
	public static String formatToSecondsAndMinutes(int seconds) {
	    return String.format("%02d:%02d", seconds / 60, seconds % 60);
	}
	
	/**
	 * Add potion effect details to a string
	 * <type> will be replaced with the name of the potion effect.
	 * <durability> will be replaced with the durability of the potion effect.
	 * <amplifier> will be replaced with the amplifier of the potion effect.
	 * @param s string that will get the details
	 * @param e the potioneffect, its durability etc will be used.
	 * @return a formated string
	 */
	public static String formatPotionEffect(String s, PotionEffect e) {
		String name = e.getType().getName().replace("_", " ").toLowerCase();
		name = name.substring(0, 1).toUpperCase() + name.substring(1);
		return s.replace("<type>", name).replace("<amplifier>",
				e.getAmplifier()+1 + "").replace("<duration>", formatToSecondsAndMinutes(e.getDuration()/20) + "");
	}
	
	public static double toTwoDecimals(double d) {
		return (int) Math.round(d*100)/100.0;
	}
}
