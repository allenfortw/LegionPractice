package tech.hexadevelopment.practice.fights.savedfights;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.stats.MySQL;

public class SavedFightDatabase {

	public static String WINNERS = "winners";
	public static String LOSERS = "losers";
	public static String KIT_INFO = "kit_info";
	public static String ARENA = "arena";
	public static String STARTED = "started";
	public static String ENDED = "ended";
	public static String LOSERS_OLD_ELOS = "losers_old_elos";
	public static String WINNERS_OLD_ELOS = "winners_old_elos";
	public static String LOSERS_NEW_ELOS = "losers_new_elos";
	public static String WINNERS_NEW_ELOS = "winners_new_elos";
	public static String INVENTORIES = "inventories";

	private LegionPractice plugin;
	
	public SavedFightDatabase(LegionPractice plugin) {
		this.plugin = plugin;
	}
	
	public String[] get(UUID uuid) {
		if (exists(uuid)) {
			ResultSet rs = plugin.mySQL.getResult("SELECT * FROM " + 
					MySQL.FIGHTS_TABLE + " WHERE uuid='" + 
					uuid + "'");
			String[] r = new String[9];
			try {
				if (rs.next()) {
					r[0] = rs.getString(WINNERS);
				}
				if (rs.next()) {
					r[1] = rs.getString(LOSERS);
				}
				if (rs.next()) {
					r[2] = rs.getString(KIT_INFO);
				}
				if (rs.next()) {
					r[3] = rs.getString(ARENA);
				}
				if (rs.next()) {
					r[4] = rs.getString(STARTED);
				}
				if (rs.next()) {
					r[5] = rs.getString(ENDED);
				}
				if (rs.next()) {
					r[6] = rs.getString(WINNERS_OLD_ELOS);
				}
				if (rs.next()) {
					r[7] = rs.getString(LOSERS_OLD_ELOS);
				}
				if (rs.next()) {
					r[8] = rs.getString(WINNERS_NEW_ELOS);
				}
				if (rs.next()) {
					r[9] = rs.getString(LOSERS_NEW_ELOS);
				}
				if (rs.next()) {
					r[10] = rs.getString(INVENTORIES);
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return r;
		}
		return null;
	}
	
	private boolean exists(UUID uuid) {
		ResultSet res = plugin.mySQL.getResult("SELECT * FROM " + MySQL.FIGHTS_TABLE + " WHERE uuid='" + uuid + "'");
		try {
			if(res.next()) {
				boolean ex = res.getString("uuid") != null;
				res.close();
				return ex;
			}
		}catch(SQLException e) {
			e.printStackTrace();
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
}
