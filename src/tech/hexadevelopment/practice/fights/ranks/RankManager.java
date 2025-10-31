package tech.hexadevelopment.practice.fights.ranks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.stats.PlayerStats;
import tech.hexadevelopment.practice.stats.QueryManager;

public class RankManager {
	
	private List<Rank> ranks = new ArrayList<Rank>();
	private LegionPractice plugin;
	
	public RankManager(LegionPractice plugin) {
		this.plugin = plugin;
	}
	
	
	public void setup() {
		try {
		for(String s : plugin.getConfig().getConfigurationSection("elo-ranks.ranks").getKeys(false)) {
			String name = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("elo-ranks.ranks." + s + ".name"));
			String range = plugin.getConfig().getString("elo-ranks.ranks." + s + ".elo-range");
			String[] r = range.replace(" ", "").split("-");
			if(r.length == 2) {
				int min = Integer.parseInt(r[0]);
				int max = Integer.parseInt(r[1]);
				ranks.add(new Rank(name, s, min, max));
			}
		}
		}catch(Exception e) {
			e.printStackTrace();
			Bukkit.getLogger().warning("----------------------------------------");
			Bukkit.getLogger().warning("");
			Bukkit.getLogger().warning("Failed to load elo ranks. Check your config.yml)");
			Bukkit.getLogger().warning("");
			Bukkit.getLogger().warning("----------------------------------------");
		}
		new BukkitRunnable() {
			
			@Override
			public void run() {
				//long l = System.currentTimeMillis();
				//HashSet<Rank> checked = new HashSet<Rank>();
				for(int i = QueryManager.startingElo+1000; i < QueryManager.startingElo+1000; i++) {
					int c = 0;
					for(Rank r : ranks) {
						if(i <= r.getMaxElo() && i >= r.getMinElo()) {
							c++;
							if(c > 1) {
								Bukkit.getLogger().warning("LegionPractice >> Elo rank " + r.getIdentifier() + " overlaps with other elo ranks!");
							}
						}
					}
				}
				//Bukkit.broadcastMessage("Debug; Rank check done in "  + (System.currentTimeMillis()-l) + " ms.");
			}
		}.runTaskAsynchronously(plugin);
	}
	
	public boolean sendEverytime() {
		return plugin.getConfig().getBoolean("elo-ranks.send-everytime");
	}
	
	public void sendMessage(Player p) {
		sendMessage(p, getRank(p));
	}
	
	public void sendMessage(Player p, Rank rank) {
		p.sendMessage(plugin.translateMessage(p, "rank-message").replace("<rank>", rank == null ? "<rank>" : rank.getName()));
	}
	
	public List<Rank> getRanks() {
		return ranks;
	}
	
	public Rank getRank(Player p) {
		PlayerStats stats = PlayerStats.getStats(p.getUniqueId());
		if(stats == null) return null;
		return getRank(stats.getGlobalElo());
	}
	
	public Rank getRank(int elo) {
		for(Rank r : ranks) {
			if(elo <= r.getMaxElo() && elo >= r.getMinElo()) {
				return r;
			}
		}
		return null;
	}
	
}
