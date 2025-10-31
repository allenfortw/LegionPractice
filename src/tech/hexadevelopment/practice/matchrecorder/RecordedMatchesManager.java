package tech.hexadevelopment.practice.matchrecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;

import tech.hexadevelopment.practice.LegionPractice;

public class RecordedMatchesManager {

	private HashMap<UUID, List<RecordedMatch>> recordedDuels = new HashMap<UUID, List<RecordedMatch>>();
	private boolean loading;
	private boolean stop;

	public void preloadDuels() {
		loading = true;
		long daysToRemove = (long) (86400000*LegionPractice.getInstance().getConfig().getDouble("remove-record-after"));
		long l = System.currentTimeMillis();
		File file = new File(LegionPractice.getInstance().getDataFolder(), "matches");
		file.mkdirs();
		if(file.listFiles().length == 0) {
			loading = false;
			Bukkit.getLogger().info("Didn't find any playback files in the matches folder.");
		}
		else {
			Bukkit.getScheduler().runTaskAsynchronously(LegionPractice.getInstance(), new Runnable() {

				int allMatches = 0;

				@Override
				public void run() {
					MatchFile mf = null;
					Object match = null;
					RecordedMatch rm = null;
					for(File f : file.listFiles()) {
						if(!stop) {
							try{
								mf = new MatchFile(LegionPractice.getInstance(), f);
								if(mf != null) {
									match = mf.getConfig().get("match");
									if(match != null && match instanceof RecordedMatch) {
										rm = (RecordedMatch) match;
										if(l-rm.getStarted() > daysToRemove) {
											f.delete();
										}
										else {
											allMatches++;
											//Bukkit.broadcastMessage("pl: " + rm.getPlayers().keySet().size());
											for(UUID uuid : rm.getPlayers().keySet()) {
												List<RecordedMatch> rec = recordedDuels.getOrDefault(uuid, new ArrayList<RecordedMatch>());
												rec.add(rm);
												recordedDuels.put(uuid, rec);
												//Bukkit.broadcastMessage(uuid.toString());
											}
										}
									}
									else {
										f.delete();
										Bukkit.getLogger().info("Deleted an invalid playback file in matches folder: " + f.getName());
									}
								}
							}catch(Exception e){
								f.delete();
								Bukkit.getLogger().warning("The file was deleted because recorded match (" + file.getName() + ") could not be loaded.");
							}
						}
					}
					for(Entry<UUID, List<RecordedMatch>> e : recordedDuels.entrySet()) {
						List<RecordedMatch> list = e.getValue();
						Collections.sort(list, new RecordedMatchComparatorByDate());
						recordedDuels.put(e.getKey(), list);
						//Bukkit.broadcastMessage("list: " + list.size());
					}
					loading = false;
					RecordedMatch.FULL_LOAD = true;
					Bukkit.getLogger().info("Preloaded " + allMatches + " matches in " + ((System.currentTimeMillis()-l)/1000) + " seconds.");
				}
			});
		}
	}

	public HashMap<UUID, List<RecordedMatch>> getRecordedDuels() {
		return recordedDuels;
	}

	public void stopLoadTask() {
		stop = true;
	}

	public boolean isLoading() {
		return loading;
	}

}
