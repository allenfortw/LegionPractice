package tech.hexadevelopment.practice.misc;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.battlekit.BattleKit;

public class StaffJoinCommand implements CommandExecutor {
	
	
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			BattleKit kit = BattleKit.getCurrentKit(p);
			if(kit == null) {
				p.sendMessage(ChatColor.RED + "You don't have any kit.");
			}
			else {
				p.chat("/staff");
				BattleKit.deselectKit(p);
				kit.giveKit(p);
				p.sendMessage(ChatColor.GREEN + "You have executed '/staff' and received the kit!");
			}
		}
		return true;
	}

}
