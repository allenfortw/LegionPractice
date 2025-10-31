package tech.hexadevelopment.practice.fights.savedfights;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fightinventory.FightInventory;
import tech.hexadevelopment.practice.stats.MySQL;
import tech.hexadevelopment.practice.stats.Stats;

public class SavedFight {

	private UUID playbackUUID;
	private String arena;
	private String kit;
	private List<Integer> winnersOldElo = new ArrayList<Integer>();
	private List<Integer> winnersNewElo = new ArrayList<Integer>();
	private List<Integer> losersOldElo = new ArrayList<Integer>();
	private List<Integer> losersNewElo = new ArrayList<Integer>();
	private HashMap<UUID, String> winners = new HashMap<UUID, String>();
	private HashMap<UUID, String> losers = new HashMap<UUID, String>();
	private List<FightInventory> winnersInventories = new ArrayList<FightInventory>();
	private List<FightInventory> losersInventories = new ArrayList<FightInventory>();
	private boolean elo;
	private boolean horse;
	private boolean onlyBow;
	private boolean combo;
	private boolean build;
	private long ended;
	private long started;


	public void saveToDatabase() {
		if(LegionPractice.getInstance().isMySQL) {
			String kitInfo = kit + ":elo=" + elo + ":horse=" + horse + ":onlyBow=" + onlyBow + ":combo=" + combo + ":build=" + build;
			String lsers = "";
			String comma = "";
			for(Entry<UUID, String> l : losers.entrySet()) {
				lsers += comma;
				lsers += l.getKey() + ":" + l.getValue();
				comma = ", ";
			}
			String wners = "";
			comma = "";
			for(Entry<UUID, String> l : winners.entrySet()) {
				wners += comma;
				wners += l.getKey() + ":" + l.getValue();
				comma = ", ";
			}
			String losers_old_elos = losersOldElo.toString().replace("[", "").replace("]", "");
			String winners_old_elos = winnersOldElo.toString().replace("[", "").replace("]", "");
			String losers_new_elos = losersNewElo.toString().replace("[", "").replace("]", "");
			String winners_new_elos = winnersNewElo.toString().replace("[", "").replace("]", "");
			List<String> invs = new ArrayList<String>();
			for(FightInventory fi : winnersInventories) {
				invs.add(fi.toString());
			}
			for(FightInventory fi : losersInventories) {
				invs.add(fi.toString());
			}
			String inventories = invs.toString().replace("[", "").replace("]", "").replace(",", ";");
			Stats.getQueryManager().queryAsync("INSERT INTO "
					+ MySQL.FIGHTS_TABLE + " (playback, "+ SavedFightDatabase.WINNERS + ", " + SavedFightDatabase.LOSERS + ", "
					+ SavedFightDatabase.KIT_INFO + ", " + SavedFightDatabase.ARENA
					+ ", " + SavedFightDatabase.STARTED + ", " + SavedFightDatabase.ENDED
					+ ", " + SavedFightDatabase.WINNERS_OLD_ELOS + ", " + SavedFightDatabase.LOSERS_OLD_ELOS
					+ ", " + SavedFightDatabase.WINNERS_NEW_ELOS + ", " + SavedFightDatabase.LOSERS_NEW_ELOS
					+ ", " + SavedFightDatabase.INVENTORIES + ")"
					+ "VALUES('" + (playbackUUID == null ? "not recorded" : playbackUUID.toString()) + "', '" + wners + "', '" + lsers + "', '" + kitInfo
					+ "', '" + arena + "', '" + started + "', '" + ended + "', '" + winners_old_elos + "', '"
					+ losers_old_elos + "', '" + winners_new_elos + "', '" + losers_new_elos + "', '"
					+ inventories + "')");
		}
	}

	public static SavedFight fromStrings(String winners, String losers, String kitInfo, String arena, long started, long ended, String winners_old_elos,
			String losers_old_elos, String winners_new_elos, String losers_new_elos, String fightInventories) {
		SavedFight sf = new SavedFight();
		String[] ki = kitInfo.replace("elo=", "").replace("horse=", "")
				.replace("onlyBow=", "").replace("combo=", "").replace("build=", "").split(":");
		sf.setKit(ki[0]);
		sf.setElo(Boolean.parseBoolean(ki[1]));
		sf.setHorse(Boolean.parseBoolean(ki[2]));
		sf.setOnlyBow(Boolean.parseBoolean(ki[3]));
		sf.setCombo(Boolean.parseBoolean(ki[4]));
		sf.setCombo(Boolean.parseBoolean(ki[5]));
		sf.setArena(arena);
		for(String s : losers.split(", ")) {
			String[] e = s.split(":");
			sf.getLosers().put(UUID.fromString(e[0]), e[1]);
		}
		for(String s : winners.split(", ")) {
			String[] e = s.split(":");
			sf.getWinners().put(UUID.fromString(e[0]), e[1]);
		}
		for(String s : losers_old_elos.split(", ")) {
			sf.getLosersOldElo().add(Integer.parseInt(s));
		}
		for(String s : winners_old_elos.split(", ")) {
			sf.getWinnersOldElo().add(Integer.parseInt(s));
		}
		for(String s : losers_new_elos.split(", ")) {
			sf.getLosersNewElo().add(Integer.parseInt(s));
		}
		for(String s : winners_new_elos.split(", ")) {
			sf.getWinnersNewElo().add(Integer.parseInt(s));
		}
		for(String s : fightInventories.split("; ")) {
			FightInventory finv = FightInventory.fromString(s);
			if(sf.getLosers().keySet().contains(finv.getOwnerUUID())) {
				sf.getLosersInventories().add(finv);
			}
			else sf.getWinnersInventories().add(finv);
		}
		return sf;
	}

	public static SavedFight byPlayBackUUID(UUID uuid) {
		String[] info = LegionPractice.getInstance().mySQL.saveFightDatabase.get(uuid);
		String winners = info[0];
		String losers = info[1];
		String kitInfo = info[2];
		String arena = info[3];
		String started = info[4];
		String ended = info[5];
		String winners_old_elos = info[6];
		String losers_old_elos = info[7];
		String winners_new_elos = info[8];
		String losers_new_elos = info[9];
		String fightInventories = info[10];
		SavedFight sf = fromStrings(winners, losers, kitInfo , arena,  Long.parseLong(started), Long.parseLong(ended), winners_old_elos,
				losers_old_elos, losers_new_elos, winners_new_elos, fightInventories);
		sf.setPlaybackUUID(uuid);
		return sf;
	}

	public void sendInfo(Player p) {
		p.sendMessage(ChatColor.GOLD + "Fight Info:");
		HashMap<String, FightInventory> winnersMap = new HashMap<String, FightInventory>();
		for(Entry<UUID, String> s : winners.entrySet()) {
			winnersMap.put(s.getValue(), winnersInventories.get(0));
		}
		HashMap<String, FightInventory> losersMap = new HashMap<String, FightInventory>();
		for(Entry<UUID, String> s : losers.entrySet()) {
			losersMap.put(s.getValue(), losersInventories.get(0));
		}		
		FightInventory.inventoryMessage(p, winnersMap, losersMap);
		p.sendMessage(ChatColor.YELLOW + "Kit: " + kit);
		if(elo) {
			p.sendMessage(ChatColor.YELLOW + "Elo: " + elo);
		}
		if(horse) {
			p.sendMessage(ChatColor.YELLOW + "Horse: " + horse);
		}
		if(onlyBow) {
			p.sendMessage(ChatColor.YELLOW + "Bow Only: " + onlyBow);
		}
		if(combo) {
			p.sendMessage(ChatColor.YELLOW + "Combo: " + combo);
		}
		p.sendMessage(ChatColor.YELLOW + "Length: " + length());
	}

	private String length() {
		return new SimpleDateFormat("mm:ss").format(new Date(ended-started));
	}

	/**
	 * @return the uuid
	 */
	public UUID getPlaybackUUID() {
		return playbackUUID;
	}
	/**
	 * @param uuid the uuid to set
	 */
	public void setPlaybackUUID(UUID uuid) {
		this.playbackUUID = uuid;
	}
	/**
	 * @return the elo
	 */
	public boolean isElo() {
		return elo;
	}
	/**
	 * @param elo the elo to set
	 */
	public void setElo(boolean elo) {
		this.elo = elo;
	}
	/**
	 * @return the horse
	 */
	public boolean isHorse() {
		return horse;
	}
	/**
	 * @param horse the horse to set
	 */
	public void setHorse(boolean horse) {
		this.horse = horse;
	}
	/**
	 * @return the onlyBow
	 */
	public boolean isOnlyBow() {
		return onlyBow;
	}
	/**
	 * @param onlyBow the onlyBow to set
	 */
	public void setOnlyBow(boolean onlyBow) {
		this.onlyBow = onlyBow;
	}
	/**
	 * @return the combo
	 */
	public boolean isCombo() {
		return combo;
	}
	/**
	 * @param combo the combo to set
	 */
	public void setCombo(boolean combo) {
		this.combo = combo;
	}
	/**
	 * @return the arena
	 */
	public String getArena() {
		return arena;
	}
	/**
	 * @param arena the arena to set
	 */
	public void setArena(String arena) {
		this.arena = arena;
	}
	/**
	 * @return the kit
	 */
	public String getKit() {
		return kit;
	}
	/**
	 * @return the build
	 */
	public boolean isBuild() {
		return build;
	}
	/**
	 * @param build the build to set
	 */
	public void setBuild(boolean build) {
		this.build = build;
	}
	/**
	 * @param kit the kit to set
	 */
	public void setKit(String kit) {
		this.kit = kit;
	}
	/**
	 * @return the winners
	 */
	public HashMap<UUID, String> getWinners() {
		return winners;
	}
	/**
	 * @param winners the winners to set
	 */
	public void setWinners(HashMap<UUID, String> winners) {
		this.winners = winners;
	}
	/**
	 * @return the winnersOldElo
	 */
	public List<Integer> getWinnersOldElo() {
		return winnersOldElo;
	}
	/**
	 * @param winnersOldElo the winnersOldElo to set
	 */
	public void setWinnersOldElo(List<Integer> winnersOldElo) {
		this.winnersOldElo = winnersOldElo;
	}
	/**
	 * @return the winnersNewElo
	 */
	public List<Integer> getWinnersNewElo() {
		return winnersNewElo;
	}
	/**
	 * @param winnersNewElo the winnersNewElo to set
	 */
	public void setWinnersNewElo(List<Integer> winnersNewElo) {
		this.winnersNewElo = winnersNewElo;
	}
	/**
	 * @return the losersOldElo
	 */
	public List<Integer> getLosersOldElo() {
		return losersOldElo;
	}
	/**
	 * @param losersOldElo the losersOldElo to set
	 */
	public void setLosersOldElo(List<Integer> losersOldElo) {
		this.losersOldElo = losersOldElo;
	}
	/**
	 * @return the losersNewElo
	 */
	public List<Integer> getLosersNewElo() {
		return losersNewElo;
	}
	/**
	 * @param losersNewElo the losersNewElo to set
	 */
	public void setLosersNewElo(List<Integer> losersNewElo) {
		this.losersNewElo = losersNewElo;
	}
	/**
	 * @return the losers
	 */
	public HashMap<UUID, String> getLosers() {
		return losers;
	}
	/**
	 * @return the ended
	 */
	public long getEnded() {
		return ended;
	}
	/**
	 * @param ended the ended to set
	 */
	public void setEnded(long ended) {
		this.ended = ended;
	}
	/**
	 * @return the started
	 */
	public long getStarted() {
		return started;
	}
	/**
	 * @param started the started to set
	 */
	public void setStarted(long started) {
		this.started = started;
	}
	/**
	 * @param losers the losers to set
	 */
	public void setLosers(HashMap<UUID, String> losers) {
		this.losers = losers;
	}
	/**
	 * @return the winnersInventories
	 */
	public List<FightInventory> getWinnersInventories() {
		return winnersInventories;
	}
	/**
	 * @param winnersInventories the winnersInventories to set
	 */
	public void setWinnersInventories(List<FightInventory> winnersInventories) {
		this.winnersInventories = winnersInventories;
	}
	/**
	 * @return the losersInventories
	 */
	public List<FightInventory> getLosersInventories() {
		return losersInventories;
	}
	/**
	 * @param losersInventories the losersInventories to set
	 */
	public void setLosersInventories(List<FightInventory> losersInventories) {
		this.losersInventories = losersInventories;
	}


}
