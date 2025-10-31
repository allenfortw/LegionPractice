package tech.hexadevelopment.practice.matchrecorder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.fightinventory.FightInventory;
import tech.hexadevelopment.practice.fights.savedfights.SavedFight;
import tech.hexadevelopment.practice.matchrecorder.playback.PlaybackFight;
import tech.hexadevelopment.practice.matchrecorder.playback.PlaybackLoadCallback;
import tech.hexadevelopment.practice.matchrecorder.playback.PlaybackStarter;
import tech.hexadevelopment.practice.overwatch.OverwatchManager;

@SerializableAs("RecordedMatch")
public class RecordedMatch extends PlaybackStarter implements ConfigurationSerializable {

	public static boolean FULL_LOAD;
	public static HashMap<RecordedMatch, HashSet<PlaybackFight>> fights = new HashMap<RecordedMatch, HashSet<PlaybackFight>>();

	private List<RecordedPlayer> recordedPlayers = new ArrayList<RecordedPlayer>();
	private List<FightInventory> fightInventories = new ArrayList<FightInventory>();
	private HashMap<UUID, List<UUID>> overwatchReports = new HashMap<UUID, List<UUID>>();
	private HashMap<UUID, List<UUID>> overwatchLegit = new HashMap<UUID, List<UUID>>();
	private HashMap<UUID, List<UUID>> originalReports = new HashMap<UUID, List<UUID>>();
	private List<UUID> overwatched = new ArrayList<UUID>();
	private HashMap<UUID, String> players;
	private Arena arena;
	private String arenaName;
	private BattleKit kit;
	private MatchFile file;
	private String dateFormat;
	private long started, ended;
	private boolean mySQLInventories;
	private int killCamSeconds;

	public RecordedMatch(HashMap<UUID, String> players, BattleKit kit, Arena arena) {
		setArena(arena);
		this.kit = kit;
		this.players = players;
		started = System.currentTimeMillis();
	}

	@Override
	public PlaybackFight startPlayback(boolean overwatch) {
		if(updateArena()) {
			freeMemory();
			PlaybackFight playbackFight = new PlaybackFight(new ArrayList<RecordedPlayer>(recordedPlayers), this, kit, arena, overwatch, killCamSeconds);
			if(playbackFight.canStart()) {
				if(recordedPlayers.size() == 0) {
					loadEverythingAsync(new PlaybackLoadCallback() {

						@Override
						public void onSuccess() {
							if(recordedPlayers.isEmpty()) {
								playbackFight.forceEnd(ChatColor.RED + "The playback could not be started. The playback might be damaged or corrupted.");
								for(UUID uuid : new ArrayList<UUID>(playbackFight.spectators)) {
									Player p = Bukkit.getPlayer(uuid);
									if(p != null) {
										PlaybackFight.removeSpectator(p);
										p.sendMessage(ChatColor.RED + "The playback could not be started. The playback might be damaged or corrupted.");
									}
								}
								if(fights.containsKey(RecordedMatch.this)) {
									HashSet<PlaybackFight> fs = fights.get(RecordedMatch.this);
									if(fs.remove(playbackFight)) {
										fights.put(RecordedMatch.this, fs);
									}
								}
								playbackFight.getArena().setUsing(false);
							}
							else {
								playbackFight.setRecordedPlayers(new ArrayList<RecordedPlayer>(recordedPlayers));
								playbackFight.start();
								playbackFight.sendFIMessages(fightInventories, uuid);
							}
						}
					});
				}
				else {
					playbackFight.setRecordedPlayers(new ArrayList<RecordedPlayer>(recordedPlayers));
					playbackFight.start();
					playbackFight.sendFIMessages(fightInventories, uuid);
				}
				HashSet<PlaybackFight> fs = fights.getOrDefault(RecordedMatch.this, new HashSet<PlaybackFight>());
				fs.add(playbackFight);
				fights.put(RecordedMatch.this, fs);
				return playbackFight;
			}
		}
		return null;
	}

	public void stopPlayback(PlaybackFight playbackFight) {
		if(playbackFight.hasEnded()) return;
		playbackFight.setEnded(true);
		playbackFight.getArena().setUsing(true);
		new BukkitRunnable() {

			@Override
			public void run() {
				playbackFight.forceEnd(null);
				for(UUID uuid : new ArrayList<UUID>(playbackFight.spectators)) {
					Player p = Bukkit.getPlayer(uuid);
					if(p != null) {
						PlaybackFight.removeSpectator(p);
					}
				}
				if(fights.containsKey(RecordedMatch.this)) {
					HashSet<PlaybackFight> fs = fights.get(RecordedMatch.this);
					if(fs.remove(playbackFight)) {
						fights.put(RecordedMatch.this, fs);
					}
				}
				playbackFight.getArena().setUsing(false);
			}
		}.runTaskLater(LegionPractice.getInstance(), 20*LegionPractice.getInstance().getConfig().getInt("wait-before-teleport"));
		freeMemory();
	}

	public boolean updateArena() {
		if(arena != null) {
			return (this.arena = findArena(arena)) != null && !arena.isUsing() && !arena.needsRollback();
		}
		return false;
	}

	private Arena findArena(Arena arena) {
		if(arena.getName().toLowerCase().contains("brackets") || arena.getName().equalsIgnoreCase("koth")) {
			for(Arena ar : LegionPractice.getInstance().arenas) {
				if(ar.getName().endsWith(":" + arena.getName()) && !ar.isUsing() && (kit == null || (!ar.needsRollback() && kit.isBuild()))) {
					this.arenaName = ar.getDisplayName();
					return ar;
				}
			}
			this.arenaName = arena.getDisplayName();
			return null;
		}
		String realName = arena.getName();
		if(arena.getName().contains(":")) {
			realName = arena.getName().split(":")[1];
		}
		boolean correctBuildMode = false;
		for(Arena ar : LegionPractice.getInstance().arenas) {
			if(ar.getName().equals(realName) || ar.getName().endsWith(":" + realName)) {
				if(!arena.getName().equalsIgnoreCase("koth") && !arena.getName().equalsIgnoreCase("brackets")) {
					if(ar.isBuild() || (kit != null && !kit.isBuild())) {
						correctBuildMode = true;
						if(!ar.isUsing() && !ar.needsRollback()) {
							this.arenaName = ar.getDisplayName();
							return ar;
						}
					}
				}
			}
		}
		if(!correctBuildMode) {
			for(Arena ar : LegionPractice.getInstance().arenas) {
				if(ar.getName().equals(realName) || ar.getName().endsWith(":" + realName)) {
					if(!arena.getName().equalsIgnoreCase("koth") && !arena.getName().equalsIgnoreCase("brackets")) {
						if(!ar.isUsing() && !ar.needsRollback()) {
							this.arenaName = ar.getDisplayName();
							return ar;
						}
					}
				}
			}
		}
		this.arenaName = arena.getDisplayName();
		return arena;
	}

	public void loadEverything() {
		FULL_LOAD = true;
		getFile().reloadFile();
		RecordedMatch rm = (RecordedMatch) getFile().getConfig().get("match");
		recordedPlayers = rm.getRecordedPlayers();
		if(mySQLInventories) {
			if(fightInventories.isEmpty() && (LegionPractice.getInstance().getConfig().getBoolean("save-elo-fights") || LegionPractice.getInstance().getConfig().getBoolean("save-all-fights")
					&& LegionPractice.getInstance().isMySQL)) {
				try{
					SavedFight sf = SavedFight.byPlayBackUUID(getUUID());
					if(sf != null) {
						for(FightInventory inv : sf.getLosersInventories()) {
							fightInventories.add(inv);
						}
						for(FightInventory inv : sf.getWinnersInventories()) {
							fightInventories.add(inv);
						}
					}
				}catch(Exception e){}
			}
		}
		else fightInventories = rm.getFightInventories();
	}

	public void loadEverythingAsync(PlaybackLoadCallback callback) {
		Bukkit.getScheduler().runTaskAsynchronously(LegionPractice.getInstance(), new Runnable() {

			@Override
			public void run() {
				loadEverything();
				Bukkit.getScheduler().runTask(LegionPractice.getInstance(), new Runnable() {

					@Override
					public void run() {
						callback.onSuccess();
					}
				});
			}
		});
	}

	public RecordedMatch(Map<String, Object> serialized) {
		load(serialized);
	}

	private void load(Map<String, Object> serialized) {
		if (serialized == null) return;
		if (serialized.isEmpty()) return;
		if(serialized.containsKey("started") 
				&& serialized.get("started") instanceof Long) {
			started = ((Long) serialized.get("started"));
		}
		if(serialized.containsKey("ended") 
				&& serialized.get("ended") instanceof Long) {
			ended = ((Long) serialized.get("ended"));
		}
		if(serialized.containsKey("arena") 
				&& serialized.get("arena") instanceof String) {
			setArena((String) serialized.get("arena"));
		}
		if(serialized.containsKey("match-uuid") 
				&& serialized.get("match-uuid") instanceof String) {
			uuid = UUID.fromString((String) serialized.get("match-uuid"));
		}
		if(serialized.containsKey("kit") 
				&& serialized.get("kit") instanceof String) {
			String kit = (String) serialized.get("kit");
			for(BattleKit k : LegionPractice.getInstance().kits) {
				if(k.getName().equals(kit)) {
					this.kit = k;
				}
			}
			if(this.kit == null) {
				this.kit = new BattleKit("Unknown Kit");
			}
		}
		try{
			if(serialized.containsKey("original-reports")
					&& serialized.get("original-reports") instanceof List<?>) {
				for(Object o : (List<?>)serialized.get("original-reports")) {
					if(o instanceof String) {
						String[] s = ((String) o).split(":");
						UUID reported = UUID.fromString(s[0]);
						UUID report = UUID.fromString(s[1]);
						List<UUID> reports = originalReports.getOrDefault(reported, new ArrayList<UUID>());
						if(!reports.contains(report)){
							reports.add(report);
						}
						originalReports.put(reported, reports);
					}
				}
			}
			if(serialized.containsKey("overwatch-reports")
					&& serialized.get("overwatch-reports") instanceof List<?>) {
				for(Object o : (List<?>)serialized.get("overwatch-reports")) {
					if(o instanceof String) {
						String[] s = ((String) o).split(":");
						UUID reported = UUID.fromString(s[0]);
						UUID report = UUID.fromString(s[1]);
						List<UUID> reports = overwatchReports.getOrDefault(reported, new ArrayList<UUID>());
						if(!reports.contains(report)){
							reports.add(report);
						}
						overwatchReports.put(reported, reports);
					}
				}
			}
			if(serialized.containsKey("overwatch-legit")
					&& serialized.get("overwatch-legit") instanceof List<?>) {
				for(Object o : (List<?>)serialized.get("overwatch-legit")) {
					if(o instanceof String) {
						String[] s = ((String) o).split(":");
						UUID reported = UUID.fromString(s[0]);
						UUID report = UUID.fromString(s[1]);
						List<UUID> reports = overwatchLegit.getOrDefault(reported, new ArrayList<UUID>());
						if(!reports.contains(report)){
							reports.add(report);
						}
						overwatchLegit.put(reported, reports);
					}
				}
			}
			if(serialized.containsKey("overwatched") && serialized.get("overwatched") instanceof List<?>) {
				for(Object o : (List<?>)serialized.get("overwatched")) {
					if(o instanceof String) {
						overwatched.add(UUID.fromString((String) o));
					}
				}
			}
		}catch(Exception e) {}
		if(FULL_LOAD && serialized.containsKey("fight-inventories") && serialized.get("fight-inventories")
				instanceof List<?>) {
			for(Object o : (List<?>)serialized.get("fight-inventories")) {
				if(o instanceof String) {
					fightInventories.add(FightInventory.fromString((String) o));
				}
			}
		}
		else if((LegionPractice.getInstance().getConfig().getBoolean("save-elo-fights") && kit.isElo()) || LegionPractice.getInstance().getConfig().getBoolean("save-all-fights")){
			mySQLInventories = true;
		}
		players = new HashMap<UUID, String>();
		if(serialized.containsKey("activity")) {
			Object list = serialized.get("activity");
			if(list instanceof List<?>) {
				for(Object o : (List<?>)list) {
					if(o instanceof RecordedPlayer) {
						RecordedPlayer rec = (RecordedPlayer) o;
						recordedPlayers.add(rec);
						players.put(rec.getUuid(), rec.getName());
					}
				}
			}
		}
		if(!FULL_LOAD) {
			recordedPlayers.clear();
		}
	}

	public void freeMemory() {
		recordedPlayers.clear();
		fightInventories.clear();
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> serialized = new HashMap<String, Object>();
		serialized.put("arena", arena.getName());
		serialized.put("kit", kit.getName());
		serialized.put("match-uuid", getUUID().toString());
		serialized.put("started", started);
		serialized.put("ended", ended);
		serialized.put("activity", recordedPlayers);
		if(!originalReports.isEmpty()) {
			List<String> list = new ArrayList<String>();
			for(Entry<UUID, List<UUID>> uuid : originalReports.entrySet()) {
				for(UUID reportedBy : uuid.getValue()) {
					list.add(uuid.getKey() + ":" + reportedBy);
				}
			}
			serialized.put("original-reports", list);
		}
		if(!overwatchReports.isEmpty()) {
			List<String> list = new ArrayList<String>();
			for(Entry<UUID, List<UUID>> uuid : overwatchReports.entrySet()) {
				for(UUID reportedBy : uuid.getValue()) {
					list.add(uuid.getKey() + ":" + reportedBy);
				}
			}
			serialized.put("overwatch-reports", list);
		}
		if(!overwatchLegit.isEmpty()) {
			List<String> list = new ArrayList<String>();
			for(Entry<UUID, List<UUID>> uuid : overwatchLegit.entrySet()) {
				for(UUID reportedBy : uuid.getValue()) {
					list.add(uuid.getKey() + ":" + reportedBy);
				}
			}
			serialized.put("overwatch-legit", list);
		}
		if(!overwatched.isEmpty()) {
			List<String> list = new ArrayList<String>();
			for(UUID uuid : overwatched) {
				list.add(uuid.toString());
			}
			serialized.put("overwatch", list);
		}
		if(fightInventories != null && fightInventories.isEmpty()) {
			if((LegionPractice.getInstance().getConfig().getBoolean("save-elo-fights") && kit.isElo())
					|| (LegionPractice.getInstance().getConfig().getBoolean("save-all-fights"))) {
				return serialized;
			}
			else {
				List<String> fis = new ArrayList<String>();
				for(FightInventory fi : fightInventories) {
					fis.add(fi.toString());
				}
				serialized.put("fight-inventories", fis);
			}
		}
		return serialized;
	}

	public static RecordedMatch deserialize(Map<String, Object> serialized) {
		return new RecordedMatch(serialized);
	}

	public void saveToFile(boolean async) {
		ended = System.currentTimeMillis();
		if(async) {

			Bukkit.getScheduler().runTaskAsynchronously(LegionPractice.getInstance(), new Runnable() {

				@Override
				public void run() {

					getFile().getConfig().set("match", RecordedMatch.this);
					getFile().save();

					Bukkit.getScheduler().runTask(LegionPractice.getInstance(), new Runnable() {

						@Override
						public void run() {
							for(UUID uuid : players.keySet()) {
								if(LegionPractice.getInstance().getRecordedMatchesManager().getRecordedDuels().containsKey(uuid)) {
									List<RecordedMatch> rec = LegionPractice.getInstance().getRecordedMatchesManager().getRecordedDuels().get(uuid);
									rec.add(RecordedMatch.this);
									LegionPractice.getInstance().getRecordedMatchesManager().getRecordedDuels().put(uuid, rec);
								}
								else {
									List<RecordedMatch> rec = new ArrayList<RecordedMatch>();
									rec.add(RecordedMatch.this);
									LegionPractice.getInstance().getRecordedMatchesManager().getRecordedDuels().put(uuid, rec);
								}
							}
							freeMemory();
						}
					});
				}
			});
		}
		else {
			getFile().getConfig().set("match", this);
			getFile().save();
			for(UUID uuid : players.keySet()) {
				if(LegionPractice.getInstance().getRecordedMatchesManager().getRecordedDuels().containsKey(uuid)) {
					List<RecordedMatch> rec = LegionPractice.getInstance().getRecordedMatchesManager().getRecordedDuels().get(uuid);
					rec.add(RecordedMatch.this);
					Collections.sort(rec, new RecordedMatchComparatorByDate());
					LegionPractice.getInstance().getRecordedMatchesManager().getRecordedDuels().put(uuid, rec);
				}
				else {
					List<RecordedMatch> rec = new ArrayList<RecordedMatch>();
					rec.add(RecordedMatch.this);
					Collections.sort(rec, new RecordedMatchComparatorByDate());
					LegionPractice.getInstance().getRecordedMatchesManager().getRecordedDuels().put(uuid, rec);
				}
			}
			freeMemory();
		}
	}

	/**
	 * @return the fightInventories
	 */
	public List<FightInventory> getFightInventories() {
		return fightInventories;
	}

	/**
	 * @param fightInventories the fightInventories to set
	 */
	public void setFightInventories(List<FightInventory> fightInventories) {
		this.fightInventories = fightInventories;
	}

	/**
	 * @return the recordedPlayers
	 */
	public List<RecordedPlayer> getRecordedPlayers() {
		return recordedPlayers;
	}

	/**
	 * @param recordedPlayers the recordedPlayers to set
	 */
	public void setRecordedPlayers(ArrayList<RecordedPlayer> recordedPlayers) {
		this.recordedPlayers = recordedPlayers;
	}

	/**
	 * @return the players
	 */
	public HashMap<UUID, String> getPlayers() {
		return players;
	}

	/**
	 * @param players the players to set
	 */
	public void setPlayers(HashMap<UUID, String> players) {
		this.players = players;
	}

	public HashMap<UUID, List<UUID>> getOriginalReports() {
		return originalReports;
	}

	public HashMap<UUID, List<UUID>> getOverwatchLegit() {
		return overwatchLegit;
	}

	public HashMap<UUID, List<UUID>> getOverwatchReports() {
		return overwatchReports;
	}

	/**
	 * @return the kit
	 */
	public BattleKit getKit() {
		return kit;
	}

	/**
	 * @param kit the kit to set
	 */
	public void setKit(BattleKit kit) {
		this.kit = kit;
	}

	/**
	 * @return the started
	 */
	public long getStarted() {
		return started;
	}
	
	public void setKillCamSeconds(int killCamSeconds) {
		this.killCamSeconds = killCamSeconds;
	}
	
	public int getKillCamSeconds() {
		return killCamSeconds;
	}

	/**
	 * @param started the started to set
	 */
	public void setStarted(long started) {
		this.started = started;
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
	 * @return the date
	 */
	public String getDateFormat() {
		if(dateFormat == null) {
			DateFormat df = new SimpleDateFormat(LegionPractice.getInstance().getConfig().getString("date-format"));
			dateFormat = df.format(new Date(started));	
		}
		return dateFormat;
	}

	/**
	 * @return the arena
	 */
	public Arena getArena() {
		return arena;
	}

	public List<UUID> getOverwatched() {
		return overwatched;
	}

	public boolean needsOverwatch() {
		if(!originalReports.isEmpty() || !overwatchReports.isEmpty() || !overwatchLegit.isEmpty()) {
			UUID uuid = OverwatchManager.highestUUID(originalReports);
			if(uuid != null && !overwatched.contains(uuid)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean setOverwatched() {
		UUID uuid = OverwatchManager.highestUUID(originalReports);
		if(uuid != null) {
			return overwatched.add(uuid);
		}
		return false;
	}

	public String getArenaName() {
		return arenaName;
	}

	/**
	 * @param arena the arena to set
	 */
	public void setArena(Arena arena) {
		if(setArena(arena.getName()) == null) {
			this.arena = arena;
		}
	}

	/**
	 * @param arena the name of the arena to set
	 */
	public Arena setArena(String arena) {
		if(arena.contains(":") && arena.split(":").length > 1) {
			String realName = arena.split(":")[1];
			for(Arena a : LegionPractice.getInstance().arenas) {
				if(a.getName().equals(realName)) {
					this.arenaName = a.getDisplayName();
					return this.arena = a;
				}
			}
		} 
		for(Arena a : LegionPractice.getInstance().arenas) {
			if(a.getName().equals(arena)) {
				this.arenaName = a.getDisplayName();
				return this.arena = a;
			}
		}
		return this.arena = null;
	}

	/**
	 * @return the file
	 */
	public MatchFile getFile() {
		if(file == null) {
			String ar = arena.getName().contains(":") ? arena.getName().split(":")[1] : arena.getName();
			file = new MatchFile(LegionPractice.getInstance(), players.values().toString().replace("[", "").replace("]", "") + " on " + ar + "- " + getUUID());
		}
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(MatchFile file) {
		this.file = file;
	}

}
