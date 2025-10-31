package tech.hexadevelopment.practice.fights.ranks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;

public class RanksCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length > 0) {
			Player target = Bukkit.getPlayer(args[0]);
			if(target != null) {
				sender.sendMessage(target.getName() + "'s rank is " + LegionPractice.getInstance().getRankManager().getRank(target));
				return true;
			}
		}
		sender.sendMessage(LegionPractice.getInstance().getPrefix() + ChatColor.GREEN + "All ranks:");
		sender.sendMessage("");
		Rank rank = sender instanceof Player ? LegionPractice.getInstance().getRankManager().getRank((Player) sender) : null;
		for(Rank r : LegionPractice.getInstance().getRankManager().getRanks()) {
			if(rank == r) {
				sender.sendMessage(ChatColor.BOLD + r.getName() + ChatColor.GRAY + ChatColor.BOLD + " (" + r.getMinElo() + "-" + r.getMaxElo() + ")");
			}
			else {
				sender.sendMessage(r.getName() + ChatColor.GRAY + " (" + r.getMinElo() + "-" + r.getMaxElo() + ")");
			}
		}
		if(sender instanceof Player) {
			sender.sendMessage("");
			LegionPractice.getInstance().getRankManager().sendMessage((Player) sender);
		}
		return true;
	}

}
