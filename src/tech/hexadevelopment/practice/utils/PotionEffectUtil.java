package tech.hexadevelopment.practice.utils;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectUtil {

	public static int getPotionEffectDurability(Player p, PotionEffectType type) {
		for(PotionEffect ef : p.getActivePotionEffects()) {
			if(ef.getType() == type) {
				return ef.getDuration();
			}
		}
		return 0;
	}

}
