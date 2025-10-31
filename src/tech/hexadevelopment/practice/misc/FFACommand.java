package tech.hexadevelopment.practice.misc;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class FFACommand implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "How to setup ffa arenas?");
		sender.sendMessage(ChatColor.GREEN + "1. Create a new arena with /arena create and set pos1, pos2 and center.");
		sender.sendMessage(ChatColor.GREEN + "2. Add a kit with /arena kits <arena> <kit>");
		sender.sendMessage(ChatColor.GREEN + "3. Do /arena ffa <arena>");
		sender.sendMessage(ChatColor.GREEN + "4. Restart the server");
		sender.sendMessage(ChatColor.GREEN + "5. Players can now use /<arena> to join the ffa arena.");
		sender.sendMessage(ChatColor.GREEN + "For example if you did '/arena create builduhcffa' players can now do '/builduhcffa'");
		return true;
	}

}
