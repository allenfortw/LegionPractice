package tech.hexadevelopment.practice.stats;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.playerdata.PlayerDataFile;

public class QueryManager {

	private LegionPractice plugin;
	public static int startingElo;
	private HashMap<UUID, List<Callback>> creation = new HashMap<UUID, List<Callback>>();

	public QueryManager(LegionPractice plugin) {
		super();
		this.plugin = plugin;
		startingElo = plugin.getConfig().getInt("starting-elo");
	}

	public void setStatsAsync(UUID uuid, String stats, int amount) {
		if(!plugin.isMySQL) {
			File file = new File(plugin.getDataFolder(), "playerdata" + File.separator + uuid.toString() + ".yml");
			if(file.exists()) {
				YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
				conf.set("stats." + stats, amount);
				try {
					conf.save(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				PlayerDataFile f = new PlayerDataFile(plugin, uuid + "");
				YamlConfiguration conf = f.getConfig();
				conf.set("stats." + stats, amount);
				f.save();
			}
		}
		else {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

				@Override
				public void run() {
					createIfNotExists(uuid, new Callback() {

						@Override
						public void onSuccess(int result) {
							plugin.mySQL.query("UPDATE " + MySQL.STATS_TABLE + " SET " + stats
									+ " = '" + amount + "' WHERE uuid = '" + uuid + "'");
						}
					});
				}
			});
		}
	}

	public void setStatsSync(UUID uuid, String stats, int amount) {
		setStatsSync(uuid, stats, amount, plugin.isMySQL);
	}
	
	public void setStatsSync(UUID uuid, String stats, int amount, boolean mysql) {
		if(!mysql) {
			File file = new File(plugin.getDataFolder(), "playerdata" + File.separator + uuid.toString() + ".yml");
			if(file.exists()) {
				YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
				conf.set("stats." + stats, amount);
				try {
					conf.save(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				PlayerDataFile f = new PlayerDataFile(plugin, uuid + "");
				YamlConfiguration conf = f.getConfig();
				conf.set("stats." + stats, amount);
				f.save();
			}
		}
		else {
			createIfNotExists(uuid, new Callback() {

				@Override
				public void onSuccess(int result) {
					plugin.mySQL.query("UPDATE " + MySQL.STATS_TABLE + " SET " + stats
							+ " = '" + amount + "' WHERE uuid = '" + uuid + "'");	
				}
			});
		}
	}

	public void getStatsAsync(UUID uuid, String stats, Callback callback) {
		if(!plugin.isMySQL) {
			File f = new File(plugin.getDataFolder(), "playerdata" + File.separator + uuid.toString() + ".yml");
			if(f.exists()) {
				YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
				if(conf != null && conf.get("stats." + stats) != null) {
					callback.onSuccess(conf.getInt("stats." + stats));
					return;
				}
			}
			if(stats.startsWith("elo_")) {
				callback.onSuccess(startingElo);
				return;
			}
			callback.onSuccess(0);
		}
		else {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

				@Override
				public void run() {
					if (exists(uuid)) {
						ResultSet rs = plugin.mySQL.getResult("SELECT * FROM " + 
								MySQL.STATS_TABLE + " WHERE uuid='" + 
								uuid + "'");
						int r = 0;
						boolean found = false;
						try {
							if (rs.next()) {
								r = rs.getInt(stats);
								found = true;
							}
							rs.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
						if(stats.startsWith("elo_") && !found) {
							r = startingElo;
						}
						int result = r;
						Bukkit.getScheduler().runTask(plugin, new Runnable() {

							@Override
							public void run() {
								callback.onSuccess(result);
							}
						});
					}
				}
			});
		}
	}
	
	public int getStatsSync(UUID uuid, String stats) {
		return getStatsSync(uuid, stats, plugin.isMySQL);
	}

	public int getStatsSync(UUID uuid, String stats, boolean mysql) {
		if(!mysql) {
			File f = new File(plugin.getDataFolder(), "playerdata" + File.separator + uuid.toString() + ".yml");
			if(f.exists()) {
				YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
				if(conf != null && conf.get("stats." + stats) != null) {
					return conf.getInt("stats." + stats);
				}
			}
			if(stats.startsWith("elo_")) {
				return startingElo;
			}
			return 0;
		}
		if (exists(uuid)) {
			ResultSet rs = plugin.mySQL.getResult("SELECT * FROM " + 
					MySQL.STATS_TABLE + " WHERE uuid='" + 
					uuid + "'");
			int r = 0;
			boolean found = false;
			try {
				if (rs.next()) {
					r = rs.getInt(stats);
					found = true;
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if(stats.startsWith("elo_") && !found) {
				r = startingElo;
			}
			int result = r;
			return result;
		}
		if(stats.startsWith("elo_")) {
			return startingElo;
		}
		return 0;
	}


	public LinkedHashMap<String, Double> topStats(String stats, int limit) {
		LinkedHashMap<String, Double> top = new LinkedHashMap<String, Double>();
		if(plugin.isMySQL && plugin.mySQL.shouldBeConnected) {
			String query = "SELECT username, " + stats + " from " + MySQL.STATS_TABLE + " order by " + stats + " desc limit " + limit;
			ResultSet rs = plugin.mySQL.getResult(query);
			try {
				while(rs.next()) {
					String player = rs.getString("username");
					if(!top.containsKey(player)) {
						top.put(player, rs.getDouble(stats));
					}
				}
				rs.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
		else if(!plugin.isMySQL && !plugin.getConfig().getBoolean("top-placeholders-require-mysql")) {
			HashMap<String, Double> all = new HashMap<String, Double>();
			File folder = new File(plugin.getDataFolder(), "playerdata");
			if(folder == null || !folder.exists()) return top;
			for(File f : folder.listFiles()) {
				YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
				if(conf != null) {
					Object res = conf.get("stats." + stats);
					String name = conf.getString("username");
					if(res != null && name != null) {
						if(res instanceof Double) {
							all.put(name, (double) res);
						}
						else if(res instanceof Integer) {
							int r = (int) res;
							all.put(name, (double) r);	
						}
					}
				}
			}
			top = all.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
					.limit(10).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			
		}
		return top;
	}

	public void updateUsername(UUID uuid) {
		createIfNotExists(uuid, new Callback() {

			@Override
			public void onSuccess(int result) {
				String name = null;
				if(Bukkit.getPlayer(uuid) != null) {
					name = Bukkit.getPlayer(uuid).getName();
				}
				else {
					name = Bukkit.getOfflinePlayer(uuid).getName();
				}
				plugin.mySQL.query("UPDATE " + MySQL.STATS_TABLE + " SET " + "username"
						+ " = '" + name + "' WHERE uuid = '" + uuid + "'");	
			}
		});
	}

	private boolean exists(UUID uuid) {
		ResultSet res = plugin.mySQL.getResult("SELECT * FROM " + MySQL.STATS_TABLE + " WHERE uuid='" + uuid + "'");
		try {
			boolean r = res.last();
			res.close();
			return r;
		}catch(SQLException e) {
			e.printStackTrace();
			Bukkit.getLogger().info("Error in ResultSet. Trying again.");
			if(res != null) {
				try {
					res.close();
				} catch (SQLException e1) {}
			}
			res = plugin.mySQL.getResult("SELECT * FROM " + MySQL.STATS_TABLE + " WHERE uuid='" + uuid + "'");
			try {
				boolean r = res.isBeforeFirst();
				res.close();
				return r;
			}catch(SQLException ex) {
				ex.printStackTrace();
			}
		}
		return false;
	}

	public void queryAsync(String query) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				plugin.mySQL.query(query);
			}
		});
	}

	/**
	 * 
	 * @param uuid
	 * @param callback, returns 1 if the data was created, otherwise 0
	 */
	public void createIfNotExists(UUID uuid, Callback callback) {
		if(!exists(uuid)) {
			synchronized (creation) {
				List<Callback> l = creation.containsKey(uuid) ? creation.get(uuid) : new ArrayList<Callback>();
				l.add(callback);
				creation.put(uuid, l);
			}
			String elos = "";
			String start = "";
			for(BattleKit kit : LegionPractice.getInstance().kits) {
				if(kit.isElo()) {
					elos +=  ", " + Stats.elo(kit);
					start += ", '" + startingElo + "'";
				}
			}
			elos +=  ", " + "global_elo";
			String elosFinal = elos;
			start += ", '" + startingElo + "'";
			String startFinal = start;
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

				@Override
				public void run() {
					plugin.mySQL.query("INSERT INTO " + MySQL.STATS_TABLE + " (uuid, username, " + Stats.KILLS
							+ ", " + Stats.DEATHS + ", " + Stats.LMS + ", "
							+ Stats.BRACKETS + ", " + Stats.PARTY_VS_PARTY_WINS + elosFinal + ") VALUES('" + uuid
							+ "', '" + (Bukkit.getPlayer(uuid) != null ? Bukkit.getPlayer(uuid).getName() : Bukkit.getOfflinePlayer(uuid).getName()) + "', '0', '0', '0', '0', '0'" + startFinal + ")");
					synchronized (creation) {
						if(creation.containsKey(uuid)) {
							for(Callback c : creation.get(uuid)) {
								c.onSuccess(1);
							}
							creation.remove(uuid);
						}
					}
				}
			});
			return;
		}
		callback.onSuccess(0);
	}
}