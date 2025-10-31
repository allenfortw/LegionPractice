package tech.hexadevelopment.practice.matchrecorder;

import java.util.Comparator;

public class RecordedMatchComparatorByDate implements Comparator<RecordedMatch>{

	@Override
	public int compare(RecordedMatch x, RecordedMatch y) {
		int startComparison = compare(x.getStarted(), y.getStarted());
		return startComparison != 0 ? startComparison
				: compare(x.getEnded(), y.getEnded());
	}
	
	private static int compare(long a, long b) {
		return a < b ? 1 : a > b ? -1 : 0;
	}
}
