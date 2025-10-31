package tech.hexadevelopment.practice.misc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.duel.Duel;
import tech.hexadevelopment.practice.matchrecorder.RecordedMatch;

public class ReportHook implements Listener {


	public static String lastReport;
	private static HashSet<String> commands = new HashSet<String>();

	
	public ReportHook() {
		commands.add("report");
		if(Bukkit.getCommandAliases().containsKey("report")) {
			for(String s : Bukkit.getCommandAliases().get("report")) {
				commands.add(s.toLowerCase());
			}
		}
	}
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		String s = e.getMessage().toLowerCase();
		for(String cmd : commands) {
			if(s.startsWith("/" + cmd)) {
				String[] args = e.getMessage().split(" ");
				if(args.length > 1) {
					Player target = Bukkit.getPlayer(args[1]);
					if(target != null) {
						lastReport = target.getName();
						LegionPractice plugin = LegionPractice.getInstance();
						Fight fight = Fight.getCurrentFight(target, plugin);
						if(fight != null && fight instanceof Duel) {
							Duel duel = (Duel) fight;
							if(duel.getRecorder() != null) {
								RecordedMatch record = duel.getRecorder().getRecordedMatch();
								if(record != null) {
									List<UUID> values = record.getOriginalReports().getOrDefault(target.getUniqueId(), new ArrayList<UUID>());
									values.add(e.getPlayer().getUniqueId());
									record.getOriginalReports().put(target.getUniqueId(), values);
								}
							}
						}
					}
				}
				return;
			}
		}
	}

}
