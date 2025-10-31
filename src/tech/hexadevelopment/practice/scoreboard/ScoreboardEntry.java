package tech.hexadevelopment.practice.scoreboard;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

import com.google.common.base.Splitter;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.npc.CitizensNPC;

public class ScoreboardEntry {

	private Board scoreboard;
	private Team team;
	private String name;
	private Score score;
	private int line;

	public ScoreboardEntry(Board board, String name, int lineFromBottom) {
		this.scoreboard = board;
		this.line = lineFromBottom;
		this.name = name;
	}

	public void create(String name, boolean removeAll) {
		this.name = name;
		Map.Entry<Team, String> team = createTeam(name);
		if (team.getKey() != null) {
			team.getKey().addEntry(team.getValue());
		}
		Iterator<ScoreboardEntry> iterator = scoreboard.getEntries().iterator();
		while(iterator.hasNext()) {
			ScoreboardEntry ent = iterator.next();
			if(removeAll || ent.getScore().getScore() == line) {
				ent.remove();
				iterator.remove();
			}
		}
		score = scoreboard.getObjective().getScore(team.getValue());
		score.setScore(line);
	}

	private Map.Entry<Team, String> createTeam(String text) {
		String result = "";
		while(text.length() <= 32) {
			text += ChatColor.values()[LegionPractice.random.nextInt(ChatColor.values().length)].toString();
		}
		String name = "spracteam-" + scoreboard.newId();
		if(scoreboard.getScoreboard().getTeam(name) != null) {
			return new AbstractMap.SimpleEntry<>(scoreboard.getScoreboard().getTeam(name), result);
		}
		Iterator<String> iterator = Splitter.fixedLength(16).split(text).iterator();
		String prefix = iterator.next();
		result = iterator.next();
		//this will fix the name tag bug when result equals to someone's in game name
		if(Bukkit.getPlayerExact(result) != null || citizensCheck(result)) {
			if(text.length() > 46) {
				text.substring(0, 46);
			}
			return createTeam(ChatColor.RESET + text);
		}
		Team team = scoreboard.getScoreboard().registerNewTeam(name);
		team.setPrefix(prefix);
		if (text.length() > 32 && iterator.hasNext()) {
			String suffix = iterator.next();
			team.setSuffix(suffix);
		}
		return new AbstractMap.SimpleEntry<>(team, result);
	}

	private boolean citizensCheck(String s) {
		if(LegionPractice.getInstance().citizens) {
			for(CitizensNPC npc : CitizensNPC.npcs) {
				if(npc.getNPC().getName().equals(s)) {
					return true;
				}
			}
		}
		return false;
	}

	public void setValue(int value) {
		if (!score.isScoreSet()) {
			score.setScore(-1);
		}
		score.setScore(value);
	}


	public void remove() {
		if (score != null) {
			score.getScoreboard().resetScores(score.getEntry());
		}
		if (team != null) {
			team.unregister();
		}
	}

	public int getLine() {
		if(score == null) {
			return line;
		}
		return line = score.getScore();
	}

	public Board getScoreboard() {
		return scoreboard;
	}

	public String getName() {
		return name;
	}

	public Team getTeam() {
		return team;
	}

	public Score getScore() {
		return score;
	}

}
