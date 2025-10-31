package tech.hexadevelopment.practice.scoreboard;

import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import tech.hexadevelopment.practice.placeholders.PlaceholderMode;
import tech.hexadevelopment.practice.LegionPractice;

public class Board {

	private Scoreboard scoreboard;
	private Objective objective;
	private int id = 1;
	private HashSet<ScoreboardEntry> entries = new HashSet<ScoreboardEntry>();
	private PlaceholderMode placeholderMode;
	public boolean created, party, epearlCooldown, adminScoreboard, needsArenas;

	public Board(Player p, String title) {
		Bukkit.getScheduler().runTask(LegionPractice.getInstance(), new Runnable() {

			@Override
			public void run() {
				scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
				objective = scoreboard.registerNewObjective(LegionPractice.getInstance().getName(), "dummy");
				objective.setDisplaySlot(DisplaySlot.SIDEBAR);
				objective.setDisplayName(title);
				created = true;
				if(p != null) {
					ScoreboardManager.getScoreboardManager().updateScoreboard(p);
				}
			}
		});
	}

	public ScoreboardEntry set(int line, String name, boolean removeAll) {
		ScoreboardEntry entry = new ScoreboardEntry(this, name, line);
		entry.create(name, removeAll);
		entries.add(entry);
		return entry;
	}

	public void removeAll() {
		Iterator<ScoreboardEntry> iterator = getEntries().iterator();
		while(iterator.hasNext()) {
			ScoreboardEntry ent = iterator.next();
			ent.remove();
			iterator.remove();
		}
	}

	public ScoreboardEntry getEntry(String name) {
		synchronized (entries) {
			for(ScoreboardEntry entry : entries) {
				if(entry.getName().equals(name)){
					return entry;
				}
			}	
		}
		return null;
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public PlaceholderMode getPlaceholderMode() {
		return placeholderMode;
	}

	public void setPlaceholderMode(PlaceholderMode placeholderMode) {
		this.placeholderMode = placeholderMode;
	}

	public Objective getObjective() {
		return objective;
	}

	public HashSet<ScoreboardEntry> getEntries() {
		return entries;
	}

	public void setObjective(Objective objective) {
		this.objective = objective;
	}

	public void addPlayer(Player p) {
		if(scoreboard != null && p.getScoreboard() != scoreboard) {
			p.setScoreboard(scoreboard);
		}
	}

	public int newId() {
		return id++;
	}

}