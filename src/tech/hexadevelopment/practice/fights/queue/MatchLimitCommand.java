package tech.hexadevelopment.practice.fights.queue;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.stats.PlayerStats;

public class MatchLimitCommand implements CommandExecutor {



	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
		if(args.length == 2 && args[0].equalsIgnoreCase("update")) {
			Player target = Bukkit.getPlayer(args[1]);
			if(target == null) {
				sender.sendMessage(ChatColor.RED + "The player is not online!");
			}
			else {
				PlayerStats stats = PlayerStats.getStats(target.getUniqueId());
				stats.checkAndResetNow();
				sender.sendMessage(ChatColor.GREEN + "Update done!");
				sender.sendMessage(ChatColor.GREEN + "Rankeds left: " + stats.getRankedsLeft());
				sender.sendMessage(ChatColor.GREEN + "Unrankeds left: " + stats.getUnrankedsLeft());
			}
		}
		else if(args.length == 4 && (args[1].equalsIgnoreCase("ranked") || args[1].equalsIgnoreCase("unranked"))
				&& (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add"))) {
			Player target = Bukkit.getPlayer(args[2]);
			boolean ranked = args[1].equalsIgnoreCase("ranked");
			boolean set = args[0].equalsIgnoreCase("set");
			int num = 0;
			try {
				num = Integer.parseInt(args[3]);
			}catch(NumberFormatException e) {
				sender.sendMessage(ChatColor.YELLOW + "/matchlimit add unranked/ranked <player> <number>");
				sender.sendMessage(ChatColor.YELLOW + "/matchlimit set unranked/ranked <player> <number>");
				sender.sendMessage(ChatColor.YELLOW + "/matchlimit update <player>");
				return true;
			}
			int number = num;
			Bukkit.getScheduler().runTaskAsynchronously(LegionPractice.getInstance(), new Runnable() {

				@SuppressWarnings("deprecation")
				@Override
				public void run() {
					UUID uuid = target != null ? target.getUniqueId() : Bukkit.getOfflinePlayer(args[2]).getUniqueId();
					PlayerStats stats = PlayerStats.getStats(uuid, false, false);
					if(ranked) {
						stats.setRankedsLeft(set ? number : stats.getRankedsLeft()+number);
					}
					else {
						stats.setUnrankedsLeft(set ? number : stats.getUnrankedsLeft()+number);
					}
					stats.save(false);
					if(set) {
						sender.sendMessage(ChatColor.GOLD + (ranked ? "Rankeds" : "Unrankeds") +  " set to " + number + " for " + (target != null ? target.getName() : uuid));
					}
					else {
						sender.sendMessage(ChatColor.GOLD + Integer.toString(number) + (ranked ? " rankeds" : " unrankeds") +  " added for " + (target != null ? target.getName() : uuid));
					}	
				}
			});
		}
		else {
			sender.sendMessage(ChatColor.YELLOW + "/matchlimit add unranked/ranked <player> <number>");
			sender.sendMessage(ChatColor.YELLOW + "/matchlimit set unranked/ranked <player> <number>");
			sender.sendMessage(ChatColor.YELLOW + "/matchlimit update <player>");
		}
		return true;
	}


}
