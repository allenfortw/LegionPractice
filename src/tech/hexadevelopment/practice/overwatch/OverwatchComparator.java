package tech.hexadevelopment.practice.overwatch;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import tech.hexadevelopment.practice.matchrecorder.RecordedMatch;

public class OverwatchComparator implements Comparator<RecordedMatch>{

	@Override
	public int compare(RecordedMatch x, RecordedMatch y) {
		int startComparison = compare(highest(x.getOriginalReports()), highest(y.getOriginalReports()));
		return startComparison != 0 ? startComparison
				: compare(highest(x.getOverwatchReports()), highest(y.getOverwatchReports()));
	}
	
	private static int compare(long a, long b) {
		return a < b ? -1 : a > b ? 1 : 0;
	}
	
	
	public static int highest(HashMap<UUID, List<UUID>> map) {
		int i = 0;
		for(List<UUID> e : map.values()) {
			if(e.size() > i) {
				i = e.size();
			}
		}
		return i;
	}
}