package tech.hexadevelopment.practice.spectator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.party.Party;

public class SpectatorCommand implements CommandExecutor {

	private LegionPractice plugin;

	public SpectatorCommand(LegionPractice plugin) {
		this.plugin = plugin;
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if(p.hasMetadata(plugin.IN_FIGHT) || PvPEvent.isInEvent(p) || Fight.getCurrentFight(p, plugin) != null) {
				p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-match"));
			}
			else if(Party.getParty(p) != null) {
				p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-party"));
			}
			else if(plugin.getSpectatorHandler().isSpectator(p)) {
				plugin.getSpectatorHandler().removeSpectator(p, true);
			}
			else if(args.length == 0){
				p.openInventory(plugin.getSpectatorHandler().getSpecateMatchesInventory());
			}
			else {
				Player tar = Bukkit.getPlayer(args[0]);
				if(tar == null) {
					p.sendMessage(plugin.translateMessage(p, "not-online"));
					return true;
				}
				Fight fight = Fight.getCurrentFight(tar, plugin);
				if(fight == null) {
					p.sendMessage(plugin.translateMessage(p, "not-in-fight"));
				}
				else if(fight.getArena() == null || fight.getArena().getCenter() == null) {
					p.sendMessage(ChatColor.RED + "Invalid arena!");
				}
				else {
					for(Player pl : Bukkit.getOnlinePlayers()) {
						if(Fight.isInFight(pl, plugin)) {
							pl.hidePlayer(p);
						}
					}
					plugin.getSpectatorHandler().getSpectatingFight().put(p.getUniqueId(), fight);
					p.teleport(fight.getArena().getCenter());
					plugin.getSpectatorHandler().addSpectator(p, fight);
				}
			}
		}
		return true;
	}

}
