package tech.hexadevelopment.practice.language;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;

public class LanguageCommand implements CommandExecutor{

	private LegionPractice plugin;
	public LanguageCommand(LegionPractice plugin) {
		this.plugin = plugin;
	}

	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if(args.length == 0) {
				LanguageManager.open(p, plugin);
			}
			else LanguageManager.openSearched(plugin, p, args[0]);
		}
		return true;
	}
}
