package tech.hexadevelopment.practice.overwatch;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.party.Party;

public class OverwatchCommand implements CommandExecutor {
	
	
	private LegionPractice plugin;
	
	public OverwatchCommand(LegionPractice plugin) {
		this.plugin = plugin;
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if(p.hasMetadata(plugin.IN_FIGHT) || PvPEvent.isInEvent(p) || Fight.getCurrentFight(p, plugin) != null) {
				p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-match"));
			}
			else if(Party.getParty(p) != null) {
				p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-party"));
			}
			else if(!plugin.citizens) {
				p.sendMessage(ChatColor.RED + "Playback feature is disabled. Citizens plugin is missing!");
			}
			plugin.getOverwatchManager().openGUI(p);
		}
		return true;
	}

}
