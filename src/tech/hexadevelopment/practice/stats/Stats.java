package tech.hexadevelopment.practice.stats;

import java.util.ArrayList;
import java.util.List;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;

public class Stats {
	
	public static QueryManager queryManager;
	public static String KILLS = "kills", DEATHS = "deaths",
			LMS = "lms", PARTY_VS_PARTY_WINS = "party_vs_party_wins", BRACKETS = "brackets", GLOBAL_ELO = "global_elo";
	
	public static String elo(BattleKit kit) {
		return "elo_" + kit.getName();
	}
	
	public static String elo(String kit) {
		return "elo_" + kit;
	}
	
	public static QueryManager getQueryManager() {
		return queryManager;
	}
	
	public static void setQueryManager(QueryManager queryManager) {
		Stats.queryManager = queryManager;
	}
	
	public static List<String> allStats() {
		List<String> list = new ArrayList<String>();
		list.add(KILLS);
		list.add(DEATHS);
		list.add(LMS);
		list.add(PARTY_VS_PARTY_WINS);
		list.add(BRACKETS);
		list.add(GLOBAL_ELO);
		for(BattleKit kit : LegionPractice.getInstance().kits) {
			if(kit.isElo()) {
				list.add(elo(kit));
			}
		}
		return list;
	}
}