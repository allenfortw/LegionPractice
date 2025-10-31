package tech.hexadevelopment.practice.fights.duel;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;

public class BestOf {

	public static int TICKS_BEFORE_NEXT_ROUND = 10;

	private int currentRound = 1;
	private int rounds;
	private HashMap<UUID, Integer> wins = new HashMap<UUID, Integer>();

	public BestOf(int rounds) {
		this.rounds = rounds;
	}


	/**
	 * 
	 * @param uuid
	 * @return whether the match ends
	 */
	public boolean handleWin(UUID uuid) {
		int i = wins.getOrDefault(uuid, 0)+1;
		wins.put(uuid, i);
		int x = rounds/2;
		currentRound += 1;
		return i > x;
	}


	public void message(Player p1, Player p2) {
		LegionPractice plugin = LegionPractice.getInstance();
		String msg1 = plugin.translateMessage(p1, "best-of-round");
		p1.sendMessage(msg1.replace("<player1>", p1.getName())
				.replace("<player2>", p2.getName())
				.replace("<total_rounds>", Integer.toString(rounds))
				.replace("<round>", Integer.toString(currentRound))
				.replace("<player1_wins>", Integer.toString(wins.getOrDefault(p1.getUniqueId(), 0)))
				.replace("<player2_wins>", Integer.toString(wins.getOrDefault(p2.getUniqueId(), 0))));
		msg1 = plugin.translateMessage(p2, "best-of-round");
		p2.sendMessage(msg1.replace("<player1>", p2.getName())
				.replace("<player2>", p1.getName())
				.replace("<total_rounds>", Integer.toString(rounds))
				.replace("<round>", Integer.toString(currentRound))
				.replace("<player1_wins>", Integer.toString(wins.getOrDefault(p2.getUniqueId(), 0)))
				.replace("<player2_wins>", Integer.toString(wins.getOrDefault(p1.getUniqueId(), 0))));
	}


	public void message(Player p1, UUID fakeBotUUIDForBestOf, String name2) {
		LegionPractice plugin = LegionPractice.getInstance();
		String msg1 = plugin.translateMessage(p1, "best-of-round");
		p1.sendMessage(msg1.replace("<player1>", p1.getName())
				.replace("<player2>", name2)
				.replace("<total_rounds>", Integer.toString(rounds))
				.replace("<round>", Integer.toString(currentRound))
				.replace("<player1_wins>", Integer.toString(wins.getOrDefault(p1.getUniqueId(), 0)))
				.replace("<player2_wins>", Integer.toString(wins.getOrDefault(fakeBotUUIDForBestOf, 0))));
	}

	public int getRounds() {
		return rounds;
	}
	
	public int getCurrentRound() {
		return currentRound;
	}
	
	public HashMap<UUID, Integer> getWins() {
		return wins;
	}
	
	public int getWins(Player p) {
		return wins.getOrDefault(p.getUniqueId(), 0);
	}
	
	public int getBotWins(Player p) {
		for(Entry<UUID, Integer> e : wins.entrySet()) {
			if(!p.getUniqueId().equals(e.getKey())) {
				return e.getValue();
			}
		}
		return 0;
	}

	public boolean endsNow(UUID dead) {
		if(rounds == 1) return true;
		boolean b = false;
		for(Entry<UUID, Integer> e : wins.entrySet()) {
			if(!e.getKey().equals(dead)) {
				int i = e.getValue()+1;
				int x = rounds/2;
				b = i > x;
				if(b) return b;
			}
		}
		return b;
	}

}
