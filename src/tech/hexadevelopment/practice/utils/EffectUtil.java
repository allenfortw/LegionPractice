package tech.hexadevelopment.practice.utils;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EffectUtil {

	public static String effectToString(PotionEffect ef) {
		return ef.getType() + "/" + ef.getAmplifier() + "/" + ef.getDuration();
	}

	public static PotionEffect fromString(String string) {
		String[] s = string.split("/");
		return new PotionEffect(PotionEffectType.getByName(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]));
	}
	
	public static int getLevel(Player p, PotionEffectType type) {
		for(PotionEffect ef : p.getActivePotionEffects()) {
			if(ef.getType() == type) {
				return ef.getAmplifier();
			}
		}
		return 0;
	}
	
}
