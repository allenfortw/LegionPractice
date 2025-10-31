package tech.hexadevelopment.practice.stats;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.playerdata.PlayerDataFile;

public class PlayerStats {


	public static String[] COOLDOWNS = new String[] {"brackets", "sumo", "lms", "juggernaut", "koth"};
	private static HashMap<UUID, PlayerStats> stats = new HashMap<UUID, PlayerStats>();
	public static HashMap<String, LinkedHashMap<String, Double>> top = new HashMap<String, LinkedHashMap<String, Double>>();
	private int kills;
	private int deaths;
	private int bracketsWins;
	private int partyVsPartyWins;
	private int lmsWins;
	private int rankedsLeft;
	private int unrankedsLeft;
	private int premiumMatches;
	private long startedLoading, limitsUpdated;
	private HashMap<String, Integer> elos = new HashMap<String, Integer>();
	private HashMap<String, Long> cooldowns = new HashMap<String, Long>();


	public PlayerStats(UUID uuid, boolean async) {
		stats.put(uuid, this);
		startedLoading = System.currentTimeMillis();
		if(async) {
			Stats.getQueryManager().getStatsAsync(uuid, Stats.DEATHS, new Callback() {

				@Override
				public void onSuccess(int result) {
					PlayerStats.this.deaths = result;
				}
			});
			Stats.getQueryManager().getStatsAsync(uuid, Stats.KILLS, new Callback() {

				@Override
				public void onSuccess(int result) {
					PlayerStats.this.kills = result;
				}
			});
			Stats.getQueryManager().getStatsAsync(uuid, Stats.BRACKETS, new Callback() {

				@Override
				public void onSuccess(int result) {
					PlayerStats.this.bracketsWins = result;
				}
			});
			Stats.getQueryManager().getStatsAsync(uuid, Stats.LMS, new Callback() {

				@Override
				public void onSuccess(int result) {
					PlayerStats.this.lmsWins = result;
				}
			});
			Stats.getQueryManager().getStatsAsync(uuid, Stats.PARTY_VS_PARTY_WINS, new Callback() {

				@Override
				public void onSuccess(int result) {
					PlayerStats.this.partyVsPartyWins = result;
				}
			});
			for(BattleKit kit : LegionPractice.getInstance().kits) {
				if(kit.isElo()) {
					Stats.getQueryManager().getStatsAsync(uuid, Stats.elo(kit), new Callback() {

						@Override
						public void onSuccess(int result) {
							PlayerStats.this.elos.put(Stats.elo(kit), result);
						}
					});
				}
			}
			new BukkitRunnable() {

				@Override
				public void run() {
					PlayerDataFile f = new PlayerDataFile(LegionPractice.getInstance(), uuid + "");
					YamlConfiguration conf = f.getConfig();
					limitsUpdated = conf.getLong("limits-updated");
					rankedsLeft = conf.getInt("rankeds-left");
					unrankedsLeft = conf.getInt("unrankeds-left");
					premiumMatches = conf.getInt("premium-matches");
					for(String str : conf.getStringList("cooldowns")) {
						String[] s = str.split(":");
						if(s.length == 2) {
							try {
								long l = Long.parseLong(s[1]);
								cooldowns.put(s[0], l);
							}catch(Exception e) {}
						}
					}
					checkMatchLimitUpdate(LegionPractice.getInstance().getConfig().getBoolean("limit-rankeds"), LegionPractice.getInstance().getConfig().getBoolean("limit-unrankeds"));				
				}
			}.runTaskAsynchronously(LegionPractice.getInstance());
		}
		else {
			this.deaths = Stats.getQueryManager().getStatsSync(uuid, Stats.DEATHS);
			this.kills = Stats.getQueryManager().getStatsSync(uuid, Stats.KILLS);
			this.bracketsWins = Stats.getQueryManager().getStatsSync(uuid, Stats.BRACKETS);
			this.lmsWins = Stats.getQueryManager().getStatsSync(uuid, Stats.LMS);
			this.partyVsPartyWins = Stats.getQueryManager().getStatsSync(uuid, Stats.PARTY_VS_PARTY_WINS);
			for(BattleKit kit : LegionPractice.getInstance().kits) {
				if(kit.isElo()) {
					Stats.getQueryManager().getStatsSync(uuid, Stats.elo(kit));
					PlayerStats.this.elos.put(Stats.elo(kit), Stats.getQueryManager().getStatsSync(uuid, Stats.elo(kit)));
				}
			}
			PlayerDataFile f = new PlayerDataFile(LegionPractice.getInstance(), uuid + "");
			YamlConfiguration conf = f.getConfig();
			limitsUpdated = conf.getLong("limits-updated");
			rankedsLeft = conf.getInt("rankeds-left");
			unrankedsLeft = conf.getInt("unrankeds-left");
			premiumMatches = conf.getInt("premium-matches");
			for(String str : conf.getStringList("cooldowns")) {
				String[] s = str.split(":");
				if(s.length == 2) {
					try {
						long l = Long.parseLong(s[1]);
						cooldowns.put(s[0], l);
					}catch(Exception e) {}
				}
			}
			checkMatchLimitUpdate(LegionPractice.getInstance().getConfig().getBoolean("limit-rankeds"), LegionPractice.getInstance().getConfig().getBoolean("limit-unrankeds"));
		}
	}

	public void checkMatchLimitUpdate(boolean rankeds, boolean unrankeds) {
		long day = 86400000;
		if(System.currentTimeMillis() - limitsUpdated > day) {
			int defaultRankeds = LegionPractice.getInstance().getConfig().getInt("rankeds-per-day");
			int defaultUnrankeds = LegionPractice.getInstance().getConfig().getInt("unrankeds-per-day");
			Player p = Bukkit.getPlayer(getUUID());
			if(p != null) {
				int addRankeds = 0, addUnrankeds = 0;;
				for(PermissionAttachmentInfo perm : p.getEffectivePermissions()) {
					if(perm.getPermission().toLowerCase().startsWith("LegionPractice.extrarankeds.")) {
						try {
							int i = Integer.parseInt(perm.getPermission().toLowerCase().replace("LegionPractice.extrarankeds.", ""));
							if(i > addRankeds) {
								addRankeds = i;
							}
						}catch(NumberFormatException e) {}
					}
					else if(perm.getPermission().toLowerCase().startsWith("LegionPractice.extraunrankeds.")) {
						try {
							int i = Integer.parseInt(perm.getPermission().toLowerCase().replace("LegionPractice.extraunrankeds.", ""));
							if(i > addUnrankeds) {
								addUnrankeds = i;
							}
						}catch(NumberFormatException e) {}
					}
				}
				defaultRankeds += addRankeds;
				defaultUnrankeds += addUnrankeds;
			}
			limitsUpdated = System.currentTimeMillis();
			if(defaultRankeds > rankedsLeft && rankeds) {
				rankedsLeft = defaultRankeds;
			}
			if(defaultUnrankeds > unrankedsLeft && unrankeds) {
				unrankedsLeft = defaultUnrankeds;
			}
		}
	}

	public void checkAndResetNow() {
		int defaultRankeds = LegionPractice.getInstance().getConfig().getInt("rankeds-per-day");
		int defaultUnrankeds = LegionPractice.getInstance().getConfig().getInt("unrankeds-per-day");
		Player p = Bukkit.getPlayer(getUUID());
		if(p != null) {
			int addRankeds = 0, addUnrankeds = 0;;
			for(PermissionAttachmentInfo perm : p.getEffectivePermissions()) {
				if(perm.getPermission().toLowerCase().startsWith("LegionPractice.extrarankeds.")) {
					try {
						int i = Integer.parseInt(perm.getPermission().toLowerCase().replace("LegionPractice.extrarankeds.", ""));
						if(i > addRankeds) {
							addRankeds = i;
						}
					}catch(NumberFormatException e) {}
				}
				else if(perm.getPermission().toLowerCase().startsWith("LegionPractice.extraunrankeds.")) {
					try {
						int i = Integer.parseInt(perm.getPermission().toLowerCase().replace("LegionPractice.extraunrankeds.", ""));
						if(i > addUnrankeds) {
							addUnrankeds = i;
						}
					}catch(NumberFormatException e) {}
				}
			}
			defaultRankeds += addRankeds;
			defaultUnrankeds += addUnrankeds;
		}
		limitsUpdated = System.currentTimeMillis();
		if(defaultRankeds > rankedsLeft) {
			rankedsLeft = defaultRankeds;
		}
		if(defaultUnrankeds > unrankedsLeft) {
			unrankedsLeft = defaultUnrankeds;
		}
	}

	public int getRankedsLeft() {
		return rankedsLeft;
	}

	public int getUnrankedsLeft() {
		return unrankedsLeft;
	}

	public long getLimitsUpdated() {
		checkMatchLimitUpdate(true, true);
		return limitsUpdated;
	}

	public void setRankedsLeft(int rankedsLeft) {
		this.rankedsLeft = rankedsLeft;
	}

	public void setUnrankedsLeft(int unrankedsLeft) {
		this.unrankedsLeft = unrankedsLeft;
	}

	public boolean removeRanked() {
		if(rankedsLeft > 0) {
			rankedsLeft--;
			return true;
		}
		return false;
	}

	public boolean removeUnranked() {
		if(unrankedsLeft > 0) {
			unrankedsLeft--;
			return true;
		}
		return false;
	}

	/**
	 * @return the stats
	 */
	public static HashMap<UUID, PlayerStats> getStats() {
		return stats;
	}

	/**
	 * @param stats the stats to set
	 */
	public static void setStats(HashMap<UUID, PlayerStats> stats) {
		PlayerStats.stats = stats;
	}

	/**
	 * @return the kills
	 */
	public int getKills() {
		return kills;
	}

	/**
	 * @param kills the kills to set
	 */
	public void setKills(int kills) {
		this.kills = kills;
	}

	/**
	 * @return the deaths
	 */
	public int getDeaths() {
		return deaths;
	}

	/**
	 * @param deaths the deaths to set
	 */
	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}

	/**
	 * @return the bracketsWins
	 */
	public int getBracketsWins() {
		return bracketsWins;
	}

	/**
	 * @param bracketsWins the bracketsWins to set
	 */
	public void setBracketsWins(int bracketsWins) {
		this.bracketsWins = bracketsWins;
	}

	/**
	 * @return the partyVsPartyWins
	 */
	public int getPartyVsPartyWins() {
		return partyVsPartyWins;
	}

	/**
	 * @param partyVsPartyWins the partyVsPartyWins to set
	 */
	public void setPartyVsPartyWins(int partyVsPartyWins) {
		this.partyVsPartyWins = partyVsPartyWins;
	}

	/**
	 * @return the lMSWins
	 */
	public int getLMSWins() {
		return lmsWins;
	}

	/**
	 * @param lmsWins the lmsWins to set
	 */
	public void setLmsWins(int lmsWins) {
		this.lmsWins = lmsWins;
	}

	public int getGlobalElo() {
		int globalElo = 0;
		int counter = 0;
		for(BattleKit kit : LegionPractice.getInstance().kits) {
			if(kit.isElo()) {
				int elo = getElo(kit);
				if(elo != QueryManager.startingElo) {
					globalElo += elo;
					counter++;
				}
			}
		}
		if(counter == 0) {
			return QueryManager.startingElo;
		}
		return globalElo/counter;
	}

	public int getPremiumMatches() {
		return premiumMatches;
	}

	public void setPremiumMatches(int premiumMatches) {
		this.premiumMatches = premiumMatches;
	}

	/**
	 * @return the elos
	 */
	public HashMap<String, Integer> getElos() {
		return elos;
	}

	/**
	 * @param elos the elos to set
	 */
	public void setElos(HashMap<String, Integer> elos) {
		this.elos = elos;
	}

	public static PlayerStats getStats(UUID uuid, boolean create) {
		return getStats(uuid, create, true);
	}

	public static PlayerStats getStats(UUID uuid, boolean create, boolean async) {
		if(!stats.containsKey(uuid)) {
			if(create) {
				return new PlayerStats(uuid, async);
			}
			return null;
		}
		return stats.get(uuid);
	}

	public boolean isOnCooldown(String tag) {
		return cooldowns.containsKey(tag) && cooldowns.get(tag) > System.currentTimeMillis();
	}

	public boolean isOnCooldown(String tag, boolean message) {
		boolean b = isOnCooldown(tag);
		if(b && message) {
			Player p = Bukkit.getPlayer(getUUID());
			if(p != null) {
				LegionPractice plugin = LegionPractice.getInstance();
				String msg = plugin.translateMessage(p, "cooldown-message");
				long millisLeft = (cooldowns.get(tag)-System.currentTimeMillis());
				int secondsLeft = (int) (millisLeft/1000);
				String time = millisLeft <= 60000 ? secondsLeft + "s" : new SimpleDateFormat("HH:mm:ss").format(new Date(millisLeft));
				p.sendMessage(msg.replace("<time>", time));
			}
		}
		return b;
	}

	public String getCooldownString(String tag) {
		LegionPractice plugin = LegionPractice.getInstance();
		if(isOnCooldown(tag)) {
			Player p = Bukkit.getPlayer(getUUID());
			if(p != null) {
				long millisLeft = (cooldowns.get(tag)-System.currentTimeMillis());
				int secondsLeft = (int) (millisLeft/1000);
				String time = millisLeft <= 60000 ? secondsLeft + "s" : new SimpleDateFormat("HH:mm:ss").format(new Date(millisLeft));
				return time;
			}
		}
		return plugin.getTimeFormat().format(new Date(0));
	}

	public void putOnCooldown(String tag, long till) {
		cooldowns.put(tag, till);
	}

	public static PlayerStats getStats(UUID uuid) {
		return getStats(uuid, true);
	}

	public int getElo(String kit) {
		if(elos.containsKey(Stats.elo(kit))) {
			return elos.get(Stats.elo(kit));
		}
		return QueryManager.startingElo;
	}

	public int getElo(BattleKit kit) {
		if(elos.containsKey(Stats.elo(kit))) {
			return elos.get(Stats.elo(kit));
		}
		return QueryManager.startingElo;
	}

	/**
	 * Sync
	 */
	public void save() {
		UUID uuid = getUUID();
		checkMatchLimitUpdate(true, true);
		if(uuid != null) {
			if(System.currentTimeMillis()-startedLoading > 2500) {
				Stats.getQueryManager().setStatsSync(uuid, Stats.KILLS, kills);
				Stats.getQueryManager().setStatsSync(uuid, Stats.DEATHS, deaths);
				Stats.getQueryManager().setStatsSync(uuid, Stats.PARTY_VS_PARTY_WINS, partyVsPartyWins);
				Stats.getQueryManager().setStatsSync(uuid, Stats.BRACKETS, bracketsWins);
				Stats.getQueryManager().setStatsSync(uuid, Stats.LMS, lmsWins);
				Stats.getQueryManager().setStatsSync(uuid, Stats.GLOBAL_ELO, getGlobalElo());
				for(BattleKit kit : LegionPractice.getInstance().kits) {
					if(kit.isElo()) {
						Stats.getQueryManager().setStatsSync(uuid, Stats.elo(kit), getElo(kit));
					}
				}
				PlayerDataFile f = new PlayerDataFile(LegionPractice.getInstance(), uuid + "");
				YamlConfiguration conf = f.getConfig();
				conf.set("limits-updated", limitsUpdated);
				conf.set("rankeds-left", rankedsLeft);
				conf.set("unrankeds-left", unrankedsLeft);
				conf.set("premium-matches", premiumMatches);
				List<String> cds = new ArrayList<String>();
				for(Entry<String, Long> e : cooldowns.entrySet()) {
					//remove expired cooldowns
					if(isOnCooldown(e.getKey())) {
						cds.add(e.getKey() + ":" + e.getValue());
					}
				}
				conf.set("cooldowns", cds);
				f.save();
			}
		}
	}

	public HashMap<String, Long> getCooldowns() {
		return cooldowns;
	}

	private UUID getUUID() {
		synchronized (stats) {
			for(UUID u : stats.keySet()) {
				if(stats.get(u).equals(this)) {
					return u;
				}
			}	
		}
		return null;
	}

	public void save(boolean async) {
		if(async) {
			Bukkit.getScheduler().runTaskAsynchronously(LegionPractice.getInstance(), new Runnable() {

				@Override
				public void run() {
					save();
				}
			});
		}
		else save();
	}


	public static HashMap<String, LinkedHashMap<String, Double>> getTop() {
		return top;
	}

}
