package tech.hexadevelopment.practice.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.scoreboard.ScoreboardManager;

public class TagManager {

	public boolean COLORED_TAGS;

	public TagManager(LegionPractice plugin) {
		COLORED_TAGS = plugin.getConfig().getBoolean("enable-colored-names");
	}

	public void setTag(Player p, String team) {
		HashSet<UUID> uuids = new HashSet<UUID>();
		for(Player pl : Bukkit.getOnlinePlayers()) {
			uuids.add(pl.getUniqueId());
		}
		setTagToUUIDS(p, team, uuids);
	}

	public void removeFromTeams(Player p) {
		ScoreboardManager.removeTag(p.getName());
	}

	public void setTagToUUIDS(Player p, String team, Collection<UUID> uuids) {
		ScoreboardManager.setTag(p.getName(), team, uuids);
	}

	public void setTagToNames(Player p, String team, Collection<String> names) {
		HashSet<UUID> uuids = new HashSet<UUID>();
		for(String name : names) {
			Player pl = Bukkit.getPlayer(name);
			if(pl != null) {
				uuids.add(pl.getUniqueId());
			}
		}
		setTagToUUIDS(p, team, uuids);
	}
}
