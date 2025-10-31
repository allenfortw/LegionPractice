package tech.hexadevelopment.practice.fights.duel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.events.DuelEndEvent;
import tech.hexadevelopment.practice.events.DuelStartEvent;
import tech.hexadevelopment.practice.fightinventory.FightInventory;
import tech.hexadevelopment.practice.fights.EloChange;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.queue.QueueManager;
import tech.hexadevelopment.practice.fights.savedfights.SavedFight;
import tech.hexadevelopment.practice.hostedevents.brackets.Brackets;
import tech.hexadevelopment.practice.hostedevents.brackets.BracketsCommand;
import tech.hexadevelopment.practice.matchrecorder.fightrecorder.DuelRecorder;
import tech.hexadevelopment.practice.stats.PlayerStats;
import tech.hexadevelopment.practice.stats.Stats;
import tech.hexadevelopment.practice.utils.ClickableMessage;
import tech.hexadevelopment.practice.utils.Countdown;
import tech.hexadevelopment.practice.utils.PlayerUtil;
import tech.hexadevelopment.practice.utils.Teleporter;
import tech.hexadevelopment.practice.LegionPractice;

/**
 * 1v1 class with start and death handling.
 * @author Toppe5
 * @since 0.1
 */
public class Duel extends Fight{

	public static HashMap<UUID, List<EloChange>> eloFights = new HashMap<UUID, List<EloChange>>();

	private LegionPractice plugin;
	private String p1;
	private String p2;
	private boolean cancelled;
	private boolean doNotTeleport;
	private boolean queue;
	private boolean ended;
	private boolean premiumQueue;
	private int winnerEloChange;
	private int loserEloChange;
	private int winnerOldElo;
	private int loserOldElo;
	private DuelRecorder recorder;
	private BestOf bestOf;
	public boolean breaktime;

	/**
	 * Create a new Duel with the given values.
	 * Automatically handles the start.
	 * @param plugin LegionPractice plugin.
	 * @param p1 the first player.
	 * @param p2 the second player.
	 * @param kit the BattleKit that is used in this Duel.
	 */
	public Duel(LegionPractice plugin, String p1, String p2, BattleKit kit) {
		this.plugin = plugin;
		this.p1 = p1;
		this.p2 = p2;
		super.kit = kit;
		handleStart();
	}

	/**
	 * Create a new Duel with the given values.
	 * Does not handle the start automatically.
	 * @param plugin
	 * @param p1
	 * @param p2
	 */
	public Duel(LegionPractice plugin, String p1, String p2){
		this.plugin = plugin;
		this.p1 = p1;
		this.p2 = p2;
	}

	/**
	 * Handles the start.
	 * Calls DuelStartEvent
	 * This also gets the arena.
	 * It will try to get a build arena if the kit is a build kit.
	 */
	public void handleStart() {
		DuelStartEvent event = new DuelStartEvent(this, PlayerUtil.getPlayer(p1), PlayerUtil.getPlayer(p2), kit);
		Bukkit.getPluginManager().callEvent(event);
		cancelled = event.isCancelled();
		super.kit = event.getKit();
		if(kit != null && bestOf == null) {
			bestOf = new BestOf(kit.getBestOf());
		}
	}

	/**
	 * Checks if the duel can start.
	 * If the player's are invalid, the arena is used, invalid or it needs rollback or the positions are in a different worlds this will return false.
	 */
	public boolean canStart() {
		if(super.arena == null) {
			if(kit != null && kit.isBuild()) {
				arena = Arena.findEmptyBuildArena(PlayerUtil.getPlayer(p1), kit);
			}
			else {
				arena = Arena.findEmptyArena(PlayerUtil.getPlayer(p1), kit);
			}
		}
		return p1 != null && p2 != null && arena != null && !arena.needsRollback() && !arena.isUsing()
				&& arena.getLoc1() != null && arena.getLoc2() != null && arena.getLoc1().getWorld() != null;
	}

	/**
	 * Starts the Duel.
	 * Teleports, heals, clears player's inventory, leaves queue automatically, marks arena as used and sets the players' current fights etc.
	 */
	public void start() {
		if(!cancelled) {
			setStartedAfterCountdown();
			plugin.getFileManager().addFight(kit);
			Player pl1 = PlayerUtil.getPlayer(p1);
			Player pl2 = PlayerUtil.getPlayer(p2);
			QueueManager.leaveQueue(pl1, true);
			QueueManager.leaveQueue(pl2, true);
			PlayerStats stats1 = PlayerStats.getStats(pl1.getUniqueId(), true, true);
			PlayerStats stats2 = PlayerStats.getStats(pl2.getUniqueId(), true, true);
			if(premiumQueue) {
				stats1.setPremiumMatches(stats1.getPremiumMatches()-1);
				stats2.setPremiumMatches(stats2.getPremiumMatches()-1);
			}
			else {				
				if(kit.isElo()) {
					if(plugin.getConfig().getBoolean("limit-rankeds")) {
						stats1.removeRanked();
						stats2.removeRanked();
					}
				}
				else if(plugin.getConfig().getBoolean("limit-unrankeds")) {
					stats1.removeUnranked();
					stats2.removeUnranked();
				}
			}
			doStuff(pl1, pl2);
			String prefix = plugin.getPrefix();
			String msg = plugin.translateMessage(pl1, "fight-start-message.duel", false);
			if(!msg.equals("false")) {
				pl1.sendMessage(prefix + msg.replace("<player>", pl1.getName()).replace("<opponent>", pl2.getName()));	
			}
			msg = plugin.translateMessage(pl2, "fight-start-message.duel", false);
			if(!msg.equals("false")) {
				pl2.sendMessage(prefix + msg.replace("<player>", pl2.getName()).replace("<opponent>", pl1.getName()));	
			}
			kit.sendFightInfo(pl1);
			kit.sendFightInfo(pl2);
			if(kit.isElo()) {
				int pl1Elo = PlayerStats.getStats(pl1.getUniqueId()).getElo(kit);
				int pl2Elo = PlayerStats.getStats(pl2.getUniqueId()).getElo(kit);
				if(pl1 != null && pl2 != null) {
					msg = plugin.translateMessage(pl1, "fight-start-message.elo", false);
					if(!msg.equals("false")) {
						pl1.sendMessage(prefix + msg
								.replace("<your_elo>", pl1Elo + "").replace("<opponent_elo>", pl2Elo + "")
								.replace("<you>", pl1.getName()).replace("<opponent>", pl2.getName()));
					}
					msg = plugin.translateMessage(pl2, "fight-start-message.elo", false);
					if(!msg.equals("false")) {
						pl2.sendMessage(prefix + msg
								.replace("<your_elo>", pl2Elo + "").replace("<opponent_elo>", pl1Elo + "")
								.replace("<you>", pl2.getName()).replace("<opponent>", pl1.getName()));
					}
				}
				if(plugin.getConfig().getBoolean("record-elo-fights") && !LegionPractice.performanceMode) {
					recorder = new DuelRecorder(this);
					recorder.startRecording();
				}
			}
			if(recorder == null && plugin.getConfig().getBoolean("record-all-fights") && !LegionPractice.performanceMode) {
				recorder = new DuelRecorder(this);
				recorder.startRecording();
			}
		}
	}

	private void doStuff(Player pl1, Player pl2) {
		if(plugin.getSpectatorHandler().isSpectator(pl1)) {
			plugin.getSpectatorHandler().removeSpectator(pl1, false);
		}
		if(plugin.getSpectatorHandler().isSpectator(pl2)) {
			plugin.getSpectatorHandler().removeSpectator(pl2, false);
		}
		if(pl1.isDead()) pl1.spigot().respawn();
		if(pl2.isDead()) pl2.spigot().respawn();
		plugin.clear(pl1, false, false);
		plugin.clear(pl2, false, false);
		arena.setUsing(true, this);
		if(plugin.getTagManager().COLORED_TAGS) {
			plugin.getTagManager().setTagToUUIDS(pl1, "team2", Arrays.asList(pl2.getUniqueId()));
			plugin.getTagManager().setTagToUUIDS(pl2, "team2", Arrays.asList(pl1.getUniqueId()));
			plugin.getTagManager().setTagToUUIDS(pl1, "team1", Arrays.asList(pl1.getUniqueId()));
			plugin.getTagManager().setTagToUUIDS(pl2, "team1", Arrays.asList(pl2.getUniqueId()));
		}
		pl1.setFallDistance(0);
		boolean noDefaultWorld = plugin.getConfig().getBoolean("no-arenas-in-default-world");
		if(!Teleporter.teleport(pl1, arena.getLoc1(), noDefaultWorld) || !Teleporter.teleport(pl2, arena.getLoc2(), noDefaultWorld)) {
			forceEnd(ChatColor.RED + "An error occurred in your fight and the fight was forced to end!");
			return;
		}
		pl1.setHealth(20);
		pl1.setFoodLevel(20);
		pl1.setFireTicks(0);
		pl2.setFireTicks(0);
		pl1.hidePlayer(pl2);
		pl2.hidePlayer(pl1);
		pl1.showPlayer(pl2);
		pl2.showPlayer(pl1);
		pl2.setHealth(20);
		pl2.setFoodLevel(20);
		kit.giveKit(pl1);
		kit.giveKit(pl2);
		startCountdown();
		pl1.updateInventory();
		pl2.updateInventory();
		Fight.setCurrentFight(pl1, this, plugin);
		Fight.setCurrentFight(pl2, this, plugin);
		pl1.setMetadata(plugin.IN_FIGHT, new FixedMetadataValue(plugin, true));
		pl2.setMetadata(plugin.IN_FIGHT, new FixedMetadataValue(plugin, true));
		if(bestOf != null && bestOf.getRounds() > 1) {
			bestOf.message(PlayerUtil.getPlayer(p1), PlayerUtil.getPlayer(p2));
		}
	}

	/**
	 * Starts the countdown.
	 */
	private void startCountdown() {
		List<String> players = new ArrayList<String>();
		players.add(p1);
		players.add(p2);
		Countdown.startCountdown(players);
	}

	/**
	 * Handles the death of a player in this duel.
	 * This is done automatically so it's not safe to call this method.
	 */
	public void handleDeath(Player p) {
		//This filthy check will fix duplicated ending if both players die
		if(ended || breaktime) return;
		String name = getP1();
		if(name.equals(p.getName())) name = getP2();
		Player opponent = PlayerUtil.getPlayer(name);
		if(opponent == null) {
			Fight.setCurrentFight(p, null, plugin);
			plugin.clear(p, true, true);
			p.sendMessage(ChatColor.RED + "An error occurred: opponent is null");
		}
		if(bestOf != null && !bestOf.handleWin(opponent.getUniqueId())) {
			breaktime = true;
			arena.rollbackArena(this);
			new BukkitRunnable() {

				@Override
				public void run() {
					if(PlayerUtil.getPlayer(p1) != null && PlayerUtil.getPlayer(p2) != null) {
						doStuff(PlayerUtil.getPlayer(p1), PlayerUtil.getPlayer(p2));
						breaktime = false;
					}
				}
			}.runTaskLater(plugin, BestOf.TICKS_BEFORE_NEXT_ROUND);
			return;
		}
		ended = true;
		super.ended = System.currentTimeMillis();
		Bukkit.getPluginManager().callEvent(new DuelEndEvent(this, opponent, p));
		FightInventory inv = new FightInventory(p, plugin);
		FightInventory inv2 = new FightInventory(opponent, plugin);
		Fight.setCurrentFight(p, null, plugin);
		plugin.clear(p, true, true);
		opponent.setMetadata(plugin.IN_FIGHT, new FixedMetadataValue(plugin, true));
		HashSet<String> names = new HashSet<String>();	
		names.add(p.getName());
		names.add(opponent.getName());
		if(plugin.getTagManager().COLORED_TAGS) {
			plugin.getTagManager().removeFromTeams(p);
			plugin.getTagManager().removeFromTeams(opponent);
		}
		UUID pUUID = p.getUniqueId();
		UUID oUUID = opponent.getUniqueId();
		boolean fightLink = plugin.getConfig().getBoolean("match-link-after-fight");
		if(fightLink) {
			String linkMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("match-link"))
					.replace("<ended>", Long.toString(getEnded())).replace("<started>", Long.toString(getStarted()));
			p.sendMessage(linkMessage);
			opponent.sendMessage(linkMessage);
		}
		if(!doNotTeleport) {
			new BukkitRunnable() {

				@Override
				public void run() {
					arena.setUsing(false, null);
					if(opponent != null) {
						Fight.setCurrentFight(opponent, null, plugin);
						plugin.clear(opponent, true, true);
					}
				}
			}.runTaskLater(plugin, 20*plugin.getConfig().getInt("wait-before-teleport"));
		}
		else {
			Fight.setCurrentFight(opponent, null, plugin);
			plugin.clear(opponent, false, true);
			arena.setUsing(false, null);
		}
		opponent.sendMessage(plugin.translateMessage(opponent, "you-won").replace("<loser>", p.getName()).replace("<winner>", opponent.getName()));
		p.sendMessage(plugin.translateMessage(p, "did-not-win").replace("<loser>", p.getName()).replace("<winner>", opponent.getName()).replace("<health>", Double.toString(Math.round(inv2.getHealth()))));
		if(kit.isElo()){
			p.setMetadata(QueueManager.eloCooldown, new FixedMetadataValue(plugin, System.currentTimeMillis()));
			opponent.setMetadata(QueueManager.eloCooldown, new FixedMetadataValue(plugin, System.currentTimeMillis()));
			int opponentELO = PlayerStats.getStats(oUUID).getElo(kit);
			int result2 = PlayerStats.getStats(pUUID).getElo(kit);
			doElo(opponent, opponentELO, p, result2);
		}
		boolean save = plugin.getConfig().getBoolean("save-all-fights")
				|| (kit.isElo() && plugin.getConfig().getBoolean("save-elo-fights"));
		if(recorder != null && recorder.getRecordedMatch() != null && recorder.hasStartedRecording()) {
			new BukkitRunnable() {

				@Override
				public void run() {
					List<FightInventory> fis = new ArrayList<FightInventory>();
					fis.add(inv);
					fis.add(inv2);
					UUID playback = recorder.stopRecording(fis).getUUID();
					if(save) {
						new BukkitRunnable() {

							@Override
							public void run() {
								saveFight(Arrays.asList(oUUID), Arrays.asList(pUUID), Arrays.asList(inv2), Arrays.asList(inv), playback).saveToDatabase();
							}
						}.runTaskAsynchronously(plugin);
					}
					if(playback != null && plugin.getConfig().getBoolean("kill-cam")) {
						ClickableMessage.sendMessage(opponent, plugin.translateMessage(opponent, "kill-cam"), "/replay " + playback + " killcam 20");
						ClickableMessage.sendMessage(p, plugin.translateMessage(p, "kill-cam"), "/replay " + playback + " killcam 20");
					}
				}
			}.runTaskLater(plugin, 40);
		}
		else if(save) {
			saveFight(Arrays.asList(oUUID), Arrays.asList(pUUID), Arrays.asList(inv2), Arrays.asList(inv), null).saveToDatabase();
		}
		List<LinkedHashMap<String, String>> map = null;
		if(plugin.getConfig().getBoolean("clickable-messages")) {
			map = FightInventory.duelMessage(opponent.getName(), p.getName(), inv2, inv);
		}
		if(isQueue()) {
			p.setMetadata(QueueManager.queueCooldown, new FixedMetadataValue(plugin, System.currentTimeMillis()));
			opponent.setMetadata(QueueManager.queueCooldown, new FixedMetadataValue(plugin, System.currentTimeMillis()));
		}
		if(map != null) {
			for(UUID uuid : plugin.getSpectatorHandler().getSpectating(this)) {
				Player spec = Bukkit.getPlayer(uuid);
				if(spec != null) {
					for(LinkedHashMap<String, String> e : map) {
						ClickableMessage.sendMultipleMessages(spec, e);
					}
				}
			}
		}
	}

	/**
	 * Does the elo changes.
	 * @param opponent the opponent player (died)
	 * @param i opponent's elo
	 * @param p the killer
	 * @param j the killer's elo
	 */
	private void doElo(Player opponent, int i, Player p, int j) {
		int old1 = i;
		int old2 = j;
		this.winnerOldElo = old1;
		this.loserOldElo = old2;
		UUID oUUID = opponent.getUniqueId();
		UUID pUUID = p.getUniqueId();
		int k = i;
		int m = j;
		int n;
		if (i > j) {
			n = i - j;
			if ((n > 100) && (n <= 200)){
				k = i + 4;
				m = j - 4;
			}
			if (n > 200){
				k = i + 3;
				m = j - 3;
			}
			if ((n > 50) && (n <= 100)){
				k = i + 6;
				m = j - 6;
			}
			if (n <= 50){
				k = i + 7;
				m = j - 7;
			}
		}
		if (i == j){
			k = i + 8;
			m = j - 8;
		}
		if (j > i){
			n = j - i;
			if ((n > 100) && (n <= 150)){
				m = j - 15;
				k = i + 15;
			}
			if ((n > 150) && (n <= 200)){
				m = j - 17;
				k = i + 17;
			}
			if (n > 200){
				m = j - 21;
				k = i + 21;
			}
			if ((n > 50) && (n <= 100)){
				m = j - 13;
				k = i + 13;
			}
			if (n <= 50){
				m = j - 10;
				k = i + 10;
			}
		}
		int diff1 = k-old1;
		int diff2 = m-old2;
		this.winnerEloChange = diff1;
		this.loserEloChange = diff2;
		PlayerStats.getStats(oUUID).getElos().put(Stats.elo(kit), k);
		PlayerStats.getStats(pUUID).getElos().put(Stats.elo(kit), m);
		if(p != null) {
			String pEloMessage = plugin.translateMessage(p, "elo-fight").replace("<player1>", opponent.getName()).replace("<player2>", p.getName())
					.replace("<elo1>", k + "").replace("<elo2>", m + "").replace("<old1>", old1 + "")
					.replace("<old2>", old2 + "").replace("<diff1>", diff1 + "").replace("<diff2>", diff2 + "");
			p.sendMessage(pEloMessage);
		}
		if(opponent != null) {
			String opponentEloMessage = plugin.translateMessage(opponent, "elo-fight").replace("<player1>", opponent.getName()).replace("<player2>", p.getName())
					.replace("<elo1>", k + "").replace("<elo2>", m + "").replace("<old1>", old1 + "")
					.replace("<old2>", old2 + "").replace("<diff1>", diff1 + "").replace("<diff2>", diff2 + "");
			opponent.sendMessage(opponentEloMessage);
		}
		List<EloChange> eloHistory = eloFights.getOrDefault(oUUID, new ArrayList<EloChange>());
		eloHistory.add(new EloChange(kit, diff1));
		eloFights.put(oUUID, eloHistory);
		List<EloChange> eloHistory2 = eloFights.getOrDefault(pUUID, new ArrayList<EloChange>());
		eloHistory2.add(new EloChange(kit, diff2));
		eloFights.put(pUUID, eloHistory2);
	}

	@Override
	public void forceEnd(String reason) {
		Player p1 = PlayerUtil.getPlayer(this.p1);
		Player p2 = PlayerUtil.getPlayer(this.p2);
		Bukkit.getPluginManager().callEvent(new DuelEndEvent(this, null, null));
		if(p1 != null) {
			if(reason != null) p1.sendMessage(reason);
			Fight.setCurrentFight(p1, null, plugin);
			p1.setHealth(20);
			plugin.clear(p1, true, true, true);
			if(plugin.getTagManager().COLORED_TAGS) {
				plugin.getTagManager().removeFromTeams(p1);
			}
		}
		if(p2 != null) {
			if(reason != null) p2.sendMessage(reason);
			Fight.setCurrentFight(p2, null, plugin);
			p2.setHealth(20);
			plugin.clear(p2, true, true, true);
			if(plugin.getTagManager().COLORED_TAGS) {
				plugin.getTagManager().removeFromTeams(p2);
			}
		}
		arena.setUsing(false, null);
		Brackets brackets = BracketsCommand.brackets;
		if(brackets != null && ((brackets.getP1() != null && brackets.getP1().equals(this.p1)) || (brackets.getP1() != null
				&& brackets.getP1().equals(this.p2))
				|| (brackets.getP2() != null && brackets.getP2().equals(this.p1)) || (brackets.getP2() != null && brackets.getP2().equals(this.p2)))) {
			if(reason != null) {
				for(String s : brackets.getPlayers().keySet()) {
					Player pl = PlayerUtil.getPlayer(s);
					if(pl != null) {
						pl.sendMessage(reason);
					}
				}
			}
			brackets.stop();
		}
	}

	@Override
	public SavedFight saveFight(List<UUID> winners, List<UUID> losers, List<FightInventory> winnersInventories, List<FightInventory> losersInventories, UUID playbackUUID) {
		SavedFight sf = new SavedFight();
		sf.getWinners().put(winners.get(0), Bukkit.getOfflinePlayer(winners.get(0)).getName());
		sf.getLosers().put(losers.get(0), Bukkit.getOfflinePlayer(losers.get(0)).getName());
		sf.setArena(arena.getDisplayName());
		sf.setKit(kit.getName());
		sf.setCombo(kit.isCombo());
		sf.setElo(kit.isElo());
		sf.setBuild(kit.isBuild());
		sf.setHorse(kit.isHorse());
		sf.setOnlyBow(kit.isOnlyBow());
		sf.setLosersInventories(losersInventories);
		sf.setWinnersInventories(winnersInventories);
		sf.getLosersNewElo().add(loserOldElo+loserEloChange);
		sf.getLosersOldElo().add(loserOldElo);
		sf.getWinnersNewElo().add(winnerOldElo+winnerEloChange);
		sf.getWinnersOldElo().add(winnerOldElo);
		sf.setStarted(getStarted());
		sf.setEnded(getEnded());
		sf.setPlaybackUUID(playbackUUID);
		return sf;
	}

	@Override
	public boolean allowSpectating() {
		return false;
	}

	/**
	 * Gets if this fight has ended.
	 */
	public boolean hasEnded() {
		return ended;
	}

	/**
	 * Sets the arena of this duel.
	 * @param arena the new arena.
	 */
	public void setArena(Arena arena) {
		this.arena = arena;
	}

	public DuelRecorder getRecorder() {
		return recorder;
	}

	/**
	 * Gets the first player of this duel.
	 * @return the first player of this duel.
	 */
	public String getP1() {
		return p1;
	}

	public boolean isPremiumQueue() {
		return premiumQueue;
	}

	public void setPremiumQueue(boolean premiumQueue) {
		this.premiumQueue = premiumQueue;
	}

	/**
	 * Gets the second player of this duel.
	 * @return the second player of this duel.
	 */
	public String getP2() {
		return p2;
	}

	public BestOf getBestOf() {
		return bestOf;
	}

	public void setBestOf(BestOf bestOf) {
		this.bestOf = bestOf;
	}

	/**
	 * Sets the kit of this duel.
	 * @param kit BattleKit will be the kit in this duel.
	 */
	public void setKit(BattleKit kit) {
		this.kit = kit;
	}

	/**
	 * Whether to teleport the player back to spawn after this duel has been ended.
	 * @param doNotTeleport if true the winner won't be teleported back to spawn, if false the winner will be teleported.
	 */
	public void setDoNotTeleport(boolean doNotTeleport) {
		this.doNotTeleport = doNotTeleport;
	}

	/**
	 * Gets if this duel is a queue fight.
	 * @return true if this duel is a queue fight, false if it's not.
	 */
	public boolean isQueue() {
		return queue;
	}

	/**
	 * Sets if this duel is a queue fight.
	 * @param queue true for a queue fight, false for not a queue duel.
	 */
	public void setQueue(boolean queue) {
		this.queue = queue;
	}

	/**
	 * Gets if the duel won't teleport the winner after the fight.
	 * @return true if the winner won't be teleproted after the fight, false if the winner will be teleported after the fight.
	 */
	public boolean isDoNotTeleport() {
		return doNotTeleport;
	}
}
/**
 * InventoryHolder for Duels.
 * @author Toppe5
 * @since 0.1
 */
class DuelHolder implements InventoryHolder {
	private Duel duel;
	private String player;

	/**
	 * Create a new DuelHolder with the given values.
	 * @param p name of the player stored in this InventoryHolder.
	 * @param duel duel stored in this InventoryHolder.
	 */
	public DuelHolder(String p, Duel duel) {
		this.duel = duel;
		this.player = p;
	}

	@Override
	public Inventory getInventory() {
		return null;
	}

	/**
	 * Gets the duel stored in this InventoryHolder.
	 * @return the duel stored in this InventoryHolder.
	 */
	public Duel getDuel() {
		return duel;
	}

	/**
	 * Gets the player name stored in this InventoryHolder.
	 * @return the player name stored in this InventoryHolder.
	 */
	public String getPlayer() {
		return player;
	}
}
