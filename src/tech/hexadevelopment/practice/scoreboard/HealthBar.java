package tech.hexadevelopment.practice.scoreboard;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;


public class HealthBar {


	public static void register(Player p) {
		Scoreboard sb = p.getScoreboard();
		if (sb.getObjective("health") != null) {
			sb.getObjective("health").unregister();
		}
		Objective o = sb.registerNewObjective("health", "health");
		o.setDisplayName(ChatColor.RED + "â�¤");
		o.setDisplaySlot(DisplaySlot.BELOW_NAME);
		for(Player pl : Bukkit.getOnlinePlayers()) {
			Damageable dam = (Damageable) pl;
			o.getScore(pl.getName()).setScore((int) dam.getHealth());
		}
	}


	public static void unregisterHealthBar(Player p){
		Set<Objective> sidebar = p.getScoreboard().getObjectives();
		for (Objective o : sidebar) {
			if (o.getName().contains("health")) {
				o.unregister();
			}
		}
	}

}
