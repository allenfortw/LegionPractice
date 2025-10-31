package tech.hexadevelopment.practice.fights.botduel;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.bukkit.metadata.Metadatable;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.events.BotDuelEndEvent;
import tech.hexadevelopment.practice.fightinventory.FightInventory;
import tech.hexadevelopment.practice.fights.BotFight;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.duel.BestOf;
import tech.hexadevelopment.practice.fights.queue.QueueManager;
import tech.hexadevelopment.practice.fights.savedfights.SavedFight;
import tech.hexadevelopment.practice.npc.CitizensNPC;
import tech.hexadevelopment.practice.npc.CitizensNPC.Difficulty;
import tech.hexadevelopment.practice.utils.ClickableMessage;
import tech.hexadevelopment.practice.utils.Countdown;
import tech.hexadevelopment.practice.utils.PlayerUtil;
import tech.hexadevelopment.practice.utils.Teleporter;
import tech.hexadevelopment.practice.LegionPractice;

public class BotDuel extends Fight implements BotFight{

	private LegionPractice plugin;
	private String p1;
	private CitizensNPC p2;
	private boolean ended;
	private int winnerEloChange;
	private int loserEloChange;
	private int winnerOldElo;
	private int loserOldElo;
	private Difficulty difficulty;
	private BestOf bestOf;
	private UUID fakeBotUUIDForBestOf = UUID.randomUUID();
	public boolean breaktime;

	public BotDuel(LegionPractice plugin, String p1, BattleKit kit) {
		this.plugin = plugin;
		this.p1 = p1;
		super.kit = kit;
		if(kit != null) {
			bestOf = new BestOf(kit.getBestOf());
		}
	}

	public boolean canStart() {
		if(super.arena == null) {
			if(kit.isBuild()) {
				this.arena = Arena.findEmptyBuildArena(Bukkit.getPlayer(p1), kit);
			}
			else this.arena = Arena.findEmptyArena(Bukkit.getPlayer(p1), kit);
		}
		return p1 != null && arena != null && !arena.needsRollback() && !arena.isUsing() && arena.getLoc1() != null
				&& arena.getLoc2() != null && arena.getLoc1().getWorld() != null && arena.getLoc1().getWorld().getName().equals(arena.getLoc2().getWorld().getName());
	}

	public void start() {
		setStartedAfterCountdown();
		Player pl1 = PlayerUtil.getPlayer(p1);
		QueueManager.leaveQueue(pl1, true);
		if(plugin.getSpectatorHandler().isSpectator(pl1)) {
			plugin.getSpectatorHandler().removeSpectator(pl1, false);
		}
		if(pl1.isDead()) pl1.spigot().respawn();
		plugin.clear(pl1, false, false);
		arena.setUsing(true, this);
		doStuff(pl1);
		plugin.getFileManager().addFight(kit);
		if(!plugin.translateMessage(pl1, "fight-start-message.duel", false).equals("false")) {
			pl1.sendMessage(plugin.translateMessage(pl1, "fight-start-message.duel").replace("<opponent>", getP2().getNPC().getName()));	
		}
		kit.sendFightInfo(pl1);
		/*
		if(LegionPractice.getInstance().getConfig().getBoolean("record-all-fights") || (kit.isElo()
				&& LegionPractice.getInstance().getConfig().getBoolean("record-bot-fights"))) {
			recorder = new DuelRecorder(this);
			recorder.startRecording();
		}
		 */
	}
	
	private void doStuff(Player pl1) {
		pl1.setFallDistance(0);
		if(!Teleporter.teleport(pl1, arena.getLoc1(), plugin.getConfig().getBoolean("no-arenas-in-default-world"))) {
			forceEnd(ChatColor.RED + "An error occurred in your fight and the fight was forced to end!");
			return;
		}
		pl1.setMetadata(plugin.IN_FIGHT, new FixedMetadataValue(plugin, true));
		pl1.setHealth(20);
		pl1.setFoodLevel(20);
		pl1.setFireTicks(0);
		kit.giveKit(pl1);
		p2 = new CitizensNPC(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("bot-name").replace("<player>", pl1.getName())), pl1.getName(), arena.getLoc2());
		p2.combatTask = null;
		p2.startCombatTask(Arrays.asList(pl1.getUniqueId()), this, difficulty);
		startCountdown();
		Fight.setCurrentFight(pl1, this, plugin);
		pl1.updateInventory();
		if(bestOf != null && bestOf.getRounds() > 1) {
			bestOf.message(pl1, fakeBotUUIDForBestOf, getP2().getNPC().getName());
		}
	}

	private void startCountdown() {
		List<String> players = new ArrayList<String>();
		players.add(p1);
		Countdown.startCountdown(players);
	}

	public void handleDeath(Player p) {
		try {
			if(ended || breaktime) return;
			if(bestOf != null && !bestOf.handleWin(fakeBotUUIDForBestOf)) {
				breaktime = true;
				p2.destroy();
				arena.rollbackArena(this);
				new BukkitRunnable() {

					@Override
					public void run() {
						if(p != null) {
							doStuff(Bukkit.getPlayer(p1));
							breaktime = false;
						}
					}
				}.runTaskLater(plugin, BestOf.TICKS_BEFORE_NEXT_ROUND);
				return;
			}
			ended = true;
			super.ended = System.currentTimeMillis();
			Bukkit.getPluginManager().callEvent(new BotDuelEndEvent(this, p, getP2(), getP2().getNPC().getName()));
			FightInventory inv = new FightInventory(p, plugin);
			FightInventory inv2 =  new FightInventory(getP2().getBukkitEntity(), getP2().getNPC().getName(), getP2().getBukkitEntity().getInventory(), plugin);
			plugin.clear(p, true, true);
			HashSet<String> names = new HashSet<String>();	
			names.add(p.getName());
			names.add("BOT_" + p.getName());
			getP2().destroy();
			arena.setUsing(false, null);
			Fight.setCurrentFight(p, null, plugin);
			p.sendMessage(plugin.translateMessage(p, "did-not-win").replace("<loser>", p.getName()).replace("<winner>", getP2().getNPC().getName()).replace("<health>", Double.toString(Math.round(inv2.getHealth()))));
			List<LinkedHashMap<String, String>> map = null;
			if(plugin.getConfig().getBoolean("clickable-messages")) {
				map = FightInventory.duelMessage(getP2().getNPC().getName(), p.getName(), inv2, inv);
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
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void handleBotDeath(Metadatable metadatable) {
		if(ended || breaktime) return;
		Player opponent = PlayerUtil.getPlayer(getP1());
		if(bestOf != null && !bestOf.handleWin(opponent.getUniqueId())) {
			breaktime = true;
			arena.rollbackArena(this);
			new BukkitRunnable() {

				@Override
				public void run() {
					if(opponent != null) {
						p2.destroy();
						bestOf.message(opponent, fakeBotUUIDForBestOf, getP2().getNPC().getName());
						doStuff(opponent);
						breaktime = false;
					}
				}
			}.runTaskLater(plugin, BestOf.TICKS_BEFORE_NEXT_ROUND);
			return;
		}
		ended = true;
		super.ended = System.currentTimeMillis();
		Bukkit.getPluginManager().callEvent(new BotDuelEndEvent(this, opponent, getP2(), opponent.getName()));
		FightInventory inv = null;
		try{
			inv = new FightInventory(getP2().getBukkitEntity(), getP2().getNPC().getName(), getP2().getBukkitEntity().getInventory(), plugin);
		}catch(Exception e){}
		FightInventory inv2 = new FightInventory(opponent, plugin);
		opponent.setMetadata(plugin.IN_FIGHT, new FixedMetadataValue(plugin, true));
		HashSet<String> names = new HashSet<String>();	
		names.add("BOT_" + opponent.getName());
		names.add(opponent.getName());
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				arena.setUsing(false, null);
				if(opponent != null) {
					Fight.setCurrentFight(opponent, null, plugin);
					plugin.clear(opponent, true, true);
				}
			}
		}, 20*plugin.getConfig().getInt("wait-before-teleport"));
		opponent.sendMessage(plugin.translateMessage(opponent, "you-won").replace("<loser>", getP2().getNPC().getName()).replace("<winner>", opponent.getName()));
		List<LinkedHashMap<String, String>> map = null;
		if(plugin.getConfig().getBoolean("clickable-messages") && inv2 != null && inv != null) {
			map = FightInventory.duelMessage(opponent.getName(), getP2().getNPC().getName(), inv2, inv);
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
		getP2().destroy();
	}


	@Override
	public void forceEnd(String reason) {
		Player p1 = PlayerUtil.getPlayer(this.p1);
		Bukkit.getPluginManager().callEvent(new BotDuelEndEvent(this, p1, getP2(), null));
		arena.setUsing(false, null);
		try{
			getP2().destroy(true);
		}catch(Exception e){}
		if(p1 != null) {
			p1.sendMessage(reason);
			Fight.setCurrentFight(p1, null, plugin);
			plugin.clear(p1, true, true, true);
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
		sf.setHorse(kit.isHorse());
		sf.setOnlyBow(kit.isOnlyBow());
		sf.getLosersNewElo().add(loserOldElo+loserEloChange);
		sf.getLosersOldElo().add(loserOldElo);
		sf.getWinnersNewElo().add(winnerOldElo+winnerEloChange);
		sf.getWinnersOldElo().add(winnerOldElo);
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

	public void setBestOf(BestOf bestOf) {
		this.bestOf = bestOf;
	}

	public BestOf getBestOf() {
		return bestOf;
	}

	/**
	 * Gets the first player of this duel.
	 * @return the first player of this duel.
	 */
	public String getP1() {
		return p1;
	}

	public CitizensNPC getP2() {
		return p2;
	}

	/**
	 * Sets the kit of this duel.
	 * @param kit BattleKit will be the kit in this duel.
	 */
	public void setKit(BattleKit kit) {
		this.kit = kit;
	}

	@Override
	public Difficulty getDifficulty() {
		return difficulty;
	}

	@Override
	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	}

}

class BotDuelHolder implements InventoryHolder {

	@Override
	public Inventory getInventory() {
		return null;
	}

}