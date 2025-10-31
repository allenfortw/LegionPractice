package tech.hexadevelopment.practice.fights.ranks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.events.DuelEndEvent;

public class RankListener implements Listener{


	private LegionPractice plugin;

	public RankListener(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onDuelEnd(DuelEndEvent e) {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if(e.getFight().getKit() != null && e.getFight().getKit().isElo()) {
					Player winner = e.getWinner();
					Player loser = e.getLoser();
					if(plugin.getRankManager().sendEverytime()) {
						plugin.getRankManager().sendMessage(winner);
						plugin.getRankManager().sendMessage(loser);
					}
					else {
						Rank winnerRank = plugin.getRankManager().getRank(winner);
						Rank loserRank = plugin.getRankManager().getRank(loser);
						new BukkitRunnable() {

							@Override
							public void run() {
								if(winner != null) {
									Rank winnerRank2 = plugin.getRankManager().getRank(winner);
									if(winnerRank != winnerRank2) {
										plugin.getRankManager().sendMessage(winner, winnerRank2);
									}
								}
								if(loser != null) {
									Rank loserRank2 = plugin.getRankManager().getRank(loser);
									if(loserRank != loserRank2) {
										plugin.getRankManager().sendMessage(loser, loserRank2);
									}
								}
							}
						}.runTaskLaterAsynchronously(plugin, 5);
					}
				}
			}
		}.runTaskAsynchronously(plugin);
	}
}
