package tech.hexadevelopment.practice.stats;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.fightinventory.FightInventory;
import tech.hexadevelopment.practice.fights.savedfights.SavedFight;
import tech.hexadevelopment.practice.fights.savedfights.SavedFightDatabase;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.utils.ItemStackUtil;

/**
 * MySQL system.
 * I'm not very familiar with the MySQL things so I'm sorry if it's badly made now.
 * @author Toppe5
 * @since 0.1
 */
public class MySQL {

	private String host;
	private int port;
	private String user;
	private String password;
	private String db;
	private Connection conn;
	public boolean shouldBeConnected;
	public static String STATS_TABLE = "stats";
	public static String FIGHTS_TABLE = "fights";
	public SavedFightDatabase saveFightDatabase;

	/**
	 * Create a new MySQL with the given values.
	 * @param host
	 * @param port
	 * @param user
	 * @param password
	 * @param name
	 */
	public MySQL(String host, int port, String user, String password, String name) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
		this.db = name;
		this.saveFightDatabase = new SavedFightDatabase(LegionPractice.getInstance());
	}

	public boolean isConnected(boolean reconnect) {
		try {
			if(conn != null && !conn.isClosed() && conn.isValid(5000)) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(reconnect){
			connect();
			return isConnected(false);
		}
		return false;
	}

	public void connect() {
		if (!isConnected(false)) {
			try{
				conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/"+
						db,	user, password);
				shouldBeConnected = true;
			}catch (SQLException e){
				Bukkit.getLogger().warning("There was an error while connecting to the MySQL...");
				e.printStackTrace();
				return;
			}
		}
	}

	public void createTable() {
		if(isConnected(true)) {
			query("CREATE TABLE IF NOT EXISTS " + STATS_TABLE + " (uuid VARCHAR(100) NOT NULL, username VARCHAR(100) NOT NULL, " + Stats.KILLS
					+ " int NOT NULL, " + Stats.DEATHS + " int NOT NULL, " + Stats.LMS + " int NOT NULL, "
					+ Stats.BRACKETS + " int NOT NULL, " + Stats.PARTY_VS_PARTY_WINS + " int NOT NULL)");
			for(BattleKit kit : LegionPractice.getInstance().kits) {
				if(kit.isElo()) {
					addKit(kit);
				}
			}
			//people who bought before don't yet have this
			PreparedStatement ps = null;
			try{
				ps = conn.prepareStatement("ALTER TABLE " + STATS_TABLE + " ADD global_elo int NOT NULL DEFAULT " + QueryManager.startingElo);
				ps.execute();
			}catch(Exception  e) {
			} finally {
				if(ps != null) {
					try {
						ps.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			if(LegionPractice.getInstance().getConfig().getBoolean("save-elo-fights") || LegionPractice.getInstance().getConfig().getBoolean("save-all-fights")) {
				query("CREATE TABLE IF NOT EXISTS " + FIGHTS_TABLE + " (playback VARCHAR(100) NOT NULL, "
						+ SavedFightDatabase.WINNERS + " VARCHAR(1000) NOT NULL, " + SavedFightDatabase.LOSERS + " VARCHAR(1000) NOT NULL, "
						+ SavedFightDatabase.KIT_INFO + " VARCHAR(128) NOT NULL, " + SavedFightDatabase.ARENA + " VARCHAR(1000) NOT NULL, "
						+ SavedFightDatabase.STARTED + " BIGINT, " + SavedFightDatabase.ENDED + " BIGINT NOT NULL, "
						+ SavedFightDatabase.WINNERS_OLD_ELOS + " VARCHAR(128) NOT NULL, " + SavedFightDatabase.LOSERS_OLD_ELOS + " VARCHAR(128) NOT NULL, "
						+ SavedFightDatabase.WINNERS_NEW_ELOS + " VARCHAR(128) NOT NULL, " + SavedFightDatabase.LOSERS_NEW_ELOS + " VARCHAR(128) NOT NULL, "
						+ SavedFightDatabase.INVENTORIES + " VARCHAR(5000) NOT NULL)");
				ResultSet rs = getResult("SELECT COUNT(*) FROM " + FIGHTS_TABLE);
				try {
					if(rs != null && !rs.next()) {
						exampleFightsEntry();
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void exampleFightsEntry() {
		SavedFight sf = new SavedFight();
		UUID winnerUUID = UUID.randomUUID();
		UUID loserUUID = UUID.randomUUID();
		sf.getWinners().put(winnerUUID, "exampleWinner");
		sf.getLosers().put(loserUUID, "exampleLoser");
		sf.setArena("Arena Name");
		sf.setKit("kit name");
		sf.setCombo(true);
		sf.setElo(true);
		sf.setBuild(true);
		sf.setHorse(true);
		sf.setOnlyBow(true);
		FightInventory f = new FightInventory(LegionPractice.getInstance());
		f.setBoots(ItemStackUtil.createItem(Material.DIAMOND_BOOTS, null, (byte) 37));
		f.setChestplate(ItemStackUtil.createItem(Material.DIAMOND_CHESTPLATE, null, (byte) 37));
		f.setLeggings(ItemStackUtil.createItem(Material.DIAMOND_LEGGINGS, null, (byte) 37));
		f.setHelmet(ItemStackUtil.createItem(Material.DIAMOND_HELMET, null, (byte) 37));
		f.setDead(true);
		f.setFood(16);
		f.setEffects(Arrays.asList(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 150, 0)));
		f.setHealth(0);
		f.setOwner("exampleLoser");
		f.setUUID(UUID.randomUUID());
		f.setOwnerUUID(loserUUID);
		f.setMainInv(LegionPractice.getInstance().kits.get(0).getInv());
		FightInventory f2 = new FightInventory(LegionPractice.getInstance());
		f2.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
		f2.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
		f2.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
		f2.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
		f2.setDead(false);
		f2.setFood(20);
		f2.setEffects(Arrays.asList(new PotionEffect(PotionEffectType.SPEED, 30, 1), new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 150, 0)));
		f2.setHealth(20);
		f2.setOwner("exampleWinner");
		f2.setUUID(UUID.randomUUID());
		f2.setOwnerUUID(winnerUUID);
		f2.setMainInv(LegionPractice.getInstance().kits.get(0).getInv());
		sf.setLosersInventories(Arrays.asList(f));
		sf.setWinnersInventories(Arrays.asList(f2));
		int loserOldElo = 1010;
		Integer winnerOldElo = 1000;
		int eloChange = 7;
		sf.getLosersNewElo().add(loserOldElo-eloChange);
		sf.getLosersOldElo().add(loserOldElo);
		sf.getWinnersNewElo().add(winnerOldElo+eloChange);
		sf.getWinnersOldElo().add(winnerOldElo);
		sf.setStarted((long) (System.currentTimeMillis()-(1000*60*3.5)));
		sf.setEnded(System.currentTimeMillis());
		sf.setPlaybackUUID(UUID.randomUUID());
		sf.saveToDatabase();
	}

	public void addKit(BattleKit kit) {
		if (isConnected(true)) {
			String elo = Stats.elo(kit);
			PreparedStatement ps = null;
			try{
				ps = conn.prepareStatement("ALTER TABLE " + STATS_TABLE + " ADD " + elo + " int NOT NULL DEFAULT " + QueryManager.startingElo);
				ps.execute();
			}catch(Exception  e) {
			} finally {
				if(ps != null) {
					try {
						ps.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void close() {
		try {
			if (isConnected(false)) {
				conn.close();
				conn = null;
			}
		}catch (SQLException e) {}
		shouldBeConnected = false;
	}

	public void query(String q) {
		query(q, true);
	}

	private void query(String q, boolean first) {
		if (isConnected(true)) {
			PreparedStatement ps = null;
			try{
				ps = conn.prepareStatement(q);
				ps.execute();
			} catch (SQLException e) {
				if(first) {
					query(q, false);
				}
				else {
					e.printStackTrace();
					Bukkit.getScheduler().runTask(LegionPractice.getInstance(), new Runnable() {

						@Override
						public void run() {
							for(Player pl : Bukkit.getOnlinePlayers()) {
								if(PermissionsManager.hasPermission(pl, Permission.ADMIN)) {
									pl.sendMessage(ChatColor.RED + "Failed to update data "
											+ "(Query: " +q + ")");
								}
							}
						}
					});
				}
			} finally {
				if(ps != null) {
					try {
						ps.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public ResultSet getResult(String q) {
		return getResult(q, true);
	}

	private ResultSet getResult(String q, boolean info) {
		if (isConnected(true)) {
			Statement ps = null;
			try {
				ps = conn.createStatement();
				ResultSet rs = ps.executeQuery(q);
				return rs;
			} catch (SQLException e) {
				if(info) {
					e.printStackTrace();
					Bukkit.getScheduler().runTask(LegionPractice.getInstance(), new Runnable() {

						@Override
						public void run() {
							for(Player pl : Bukkit.getOnlinePlayers()) {
								if(PermissionsManager.hasPermission(pl, Permission.ADMIN)) {
									pl.sendMessage(ChatColor.RED + "Failed to collect data "
											+ "(Query: " + q + ")");
								}
							}
						}
					});
				}
			}
		}
		return null;
	}
}

