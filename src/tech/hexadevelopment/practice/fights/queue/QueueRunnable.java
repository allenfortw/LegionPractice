package tech.hexadevelopment.practice.fights.queue;

import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.fights.duel.Duel;
import tech.hexadevelopment.practice.scoreboard.ScoreboardManager;
import tech.hexadevelopment.practice.stats.PlayerStats;

public class QueueRunnable extends BukkitRunnable{

	public static String RANKED_QUEUE = "ToppeBattlesRankedQUEUE";

	private LegionPractice plugin;
	private int currentElo;
	private int range;
	private boolean anyone, premium;
	private int counter;
	private BattleKit kit;
	private UUID uuid;

	public QueueRunnable(LegionPractice plugin, Player p, BattleKit kit, boolean premium) {
		this.uuid = p.getUniqueId();
		this.plugin = plugin;
		this.kit = kit;
		this.premium = premium;
		int result = PlayerStats.getStats(uuid).getElo(kit);
		currentElo = result;
		range += plugin.getConfig().getInt("ranked.elo-range-increase");
		p.setMetadata(RANKED_QUEUE, new FixedMetadataValue(plugin, this));
		runTaskTimer(plugin, 0, LegionPractice.performanceMode ? 40 : 20);
	}

	@Override
	public void run() {
		Player p = Bukkit.getPlayer(uuid);
		if(p == null || getRanked(p) == null || !getRanked(p).equals(this)) {
			this.cancel();
			QueueManager.updateGUIs(true);
		}
		else {
			counter++;
			if(counter >= plugin.getConfig().getInt("ranked.anyone-after")) {
				if(!anyone) {
					p.sendMessage(plugin.translateMessage(p, "ranked-anyone"));
				}
				anyone = true;
			}
			else if(counter % plugin.getConfig().getInt("ranked.elo-range-time") == 0) {
				range += plugin.getConfig().getInt("ranked.elo-range-increase");
				p.sendMessage(plugin.translateMessage(p, "ranked-range").replace("<range1>", (currentElo-range) + "").replace("<range2>", (currentElo+range) + ""));
			}
			if(LegionPractice.performanceMode) {
				counter++;
				if(counter % plugin.getConfig().getInt("ranked.elo-range-time") == 0) {
					range += plugin.getConfig().getInt("ranked.elo-range-increase");
					p.sendMessage(plugin.translateMessage(p, "ranked-range").replace("<range1>", (currentElo-range) + "").replace("<range2>", (currentElo+range) + ""));
				}
			}
			if(ScoreboardManager.isEnabled()) {
				ScoreboardManager.getScoreboardManager().updateScoreboard(p);
			}
			List<QueueRunnable> list = null;
			QueueRunnable opponent = null;
			BattleKit key = null;
			outer: for(Entry<BattleKit, List<QueueRunnable>> qrSet : QueueManager.ranked.entrySet()) {
				list = qrSet.getValue();
				key = qrSet.getKey();
				if(list != null) {
					for(QueueRunnable qr : list) {
						if(!qr.equals(this)) {
							Player p2 = Bukkit.getPlayer(qr.getUUID());
							if(p2 != null) {
								if(qr.getKit().equals(this.kit) && qr.premium == premium) {
									if(Math.abs(qr.getCurrentElo()-getCurrentElo()) < range && Math.abs(qr.getCurrentElo()-getCurrentElo()) < qr.getRange() || qr.isAnyone() && anyone) {
										p2.removeMetadata(QueueManager.waitingQueue, plugin);
										p.removeMetadata(QueueManager.waitingQueue, plugin);
										p.closeInventory();
										Duel duel = new Duel(plugin, p.getName(), p2.getName(), qrSet.getKey());
										opponent = qr;
										if(duel.canStart()) {
											duel.setQueue(true);
											duel.setPremiumQueue(premium);
											duel.start();
											break outer;
										}
										else {
											plugin.arenaPvP.giveSpawnItems(p);
											plugin.arenaPvP.giveSpawnItems(p2);
											Arena.sendNoArenas(p2);
											Arena.sendNoArenas(p);
										}
									}
								}
							}
						}
					}
				}
			}
			if(opponent != null) {
				opponent.cancel();
				list.remove(opponent);
				list.remove(this);
				QueueManager.ranked.put(key, list);
				QueueManager.updateGUIs(true);
				this.cancel();
				return;
			}
		}
	}

	public static QueueRunnable getRanked(Player p) {
		MetadataValue mv = LegionPractice.getInstance().getMetadata(p, RANKED_QUEUE);
		if(mv != null && mv.value() instanceof QueueRunnable) {
			return (QueueRunnable) mv.value();
		}
		return null;
	}

	/**
	 * @return the currentElo
	 */
	public int getCurrentElo() {
		return currentElo;
	}

	public boolean isPremium() {
		return premium;
	}
	
	/**
	 * @return the range
	 */
	public int getRange() {
		return range;
	}

	public BattleKit getKit() {
		return kit;
	}

	/**
	 * @param range the range to set
	 */
	public void setRange(int range) {
		this.range = range;
	}

	/**
	 * @return the anyone
	 */
	public boolean isAnyone() {
		return anyone;
	}

	/**
	 * @param anyone the anyone to set
	 */
	public void setAnyone(boolean anyone) {
		this.anyone = anyone;
	}

	/**
	 * @return the counter
	 */
	public int getCounter() {
		return counter;
	}

	/**
	 * @param counter the counter to set
	 */
	public void setCounter(int counter) {
		this.counter = counter;
	}

	/**
	 * @return the uuid
	 */
	public UUID getUUID() {
		return uuid;
	}
}
