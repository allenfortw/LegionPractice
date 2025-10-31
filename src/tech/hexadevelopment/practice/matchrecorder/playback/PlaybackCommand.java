package tech.hexadevelopment.practice.matchrecorder.playback;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.matchrecorder.RecordedMatch;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.utils.VersionChecker;

public class PlaybackCommand implements CommandExecutor {

	static boolean disabled;

	private LegionPractice plugin;

	public PlaybackCommand(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			String site = VersionChecker.s;
			if(!site.equals("http://LegionPractice.ga/") || site.length() != 25
					|| LegionPractice.getInstance().arenaPvP.z().length() < 3) {
				return true;
			}
			if(p.hasMetadata(plugin.IN_FIGHT) || PvPEvent.isInEvent(p) || Fight.getCurrentFight(p, plugin) != null) {
				p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-match"));
			}
			else if(Party.getParty(p) != null) {
				p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-party"));
			}
			else if(!plugin.citizens) {
				p.sendMessage(ChatColor.RED + "Playback feature is disabled. Citizens plugin is missing!");
			}
			else if(args.length == 0) {
				PlaybackInventoryManager.viewing.remove(p.getUniqueId());
				PlaybackInventoryManager.openGUI(p, 0);
			}
			else {
				for(List<RecordedMatch> matches : LegionPractice.getInstance().getRecordedMatchesManager().getRecordedDuels().values()) {
					for(RecordedMatch match : matches) {
						if(match.getUUID().toString().equals(args[0]) || match.getUUID().toString().substring(match.getUUID().toString().length()-4).equalsIgnoreCase(args[0])) {
							if(args.length > 1 && args[1].equalsIgnoreCase("killcam")) {
								int i = 30;
								try {
									if(args.length > 2) {
										i = Integer.parseInt(args[2]);
									}
								}catch(Exception e) {}
								match.setKillCamSeconds(i);
							}
							PlaybackFight fight = match.startPlayback(false);
							if(fight != null) {
								fight.addSpectator(p);
							}
							else {
								p.sendMessage(plugin.translateMessage(p, "playback-can-not-start"));
							}
							return true;
						}
					}
				}
			}
		}
		return true;
	}

	public static void disablePlayback() {
		disabled = true;
	}
}