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

public class PremiumMatchesCommand implements CommandExecutor {



	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
		if(args.length == 3 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add"))) {
			Player target = Bukkit.getPlayer(args[1]);
			boolean set = args[0].equalsIgnoreCase("set");
			int num = 0;
			try {
				num = Integer.parseInt(args[2]);
			}catch(NumberFormatException e) {
				sender.sendMessage(ChatColor.YELLOW + "/premiummatches add <player> <number>");
				sender.sendMessage(ChatColor.YELLOW + "/premiummatches set <player> <number>");
				return true;
			}
			int number = num;
			Bukkit.getScheduler().runTaskAsynchronously(LegionPractice.getInstance(), new Runnable() {

				@SuppressWarnings("deprecation")
				@Override
				public void run() {
					UUID uuid = target != null ? target.getUniqueId() : Bukkit.getOfflinePlayer(args[2]).getUniqueId();
					PlayerStats stats = PlayerStats.getStats(uuid, false, false);
					int old = stats.getPremiumMatches();
					stats.setPremiumMatches(set ? number : stats.getPremiumMatches()+number);
					stats.save(false);
					if(set) {
						sender.sendMessage(ChatColor.GOLD + "Premium matches set to " + number + " for " + (target != null ? target.getName() : uuid));
					}
					else {
						sender.sendMessage(ChatColor.GOLD + Integer.toString(number) + " premium matches added for " + (target != null ? target.getName() : uuid));
					}
					int added = stats.getPremiumMatches()-old;
					if(target != null && added > 0) {
						target.sendMessage(LegionPractice.getInstance().getPrefix() + ChatColor.GREEN + "You have received " + added + " premium " + (added > 1 ? "matches!" : "match!"));
					}
				}
			});
		}
		else {
			sender.sendMessage(ChatColor.YELLOW + "/premiummatches add <player> <number>");
			sender.sendMessage(ChatColor.YELLOW + "/premiummatches set <player> <number>");
		}
		return true;
	}


}
