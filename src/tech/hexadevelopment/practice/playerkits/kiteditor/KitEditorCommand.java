package tech.hexadevelopment.practice.playerkits.kiteditor;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.utils.SerializableLocation;

public class KitEditorCommand implements CommandExecutor{

	private LegionPractice plugin;

	/**
	 * 
	 * @param plugin LegionPractice plugin
	 */
	public KitEditorCommand(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if(args.length > 0 && (args[0].toLowerCase().contains("editing") || args[0].toLowerCase().contains("place"))) {
				if(PermissionsManager.hasPermission(p, Permission.ADMIN)) {
					plugin.getConfig().set("editing-place", new SerializableLocation(((Player) p).getLocation()).toString());
					plugin.saveConfig();
					p.sendMessage(ChatColor.GREEN + "You have set the new editing place!");
				}
			}
			if(args.length > 0 && KitEditorManager.isEditing(p)) {
				if(args[0].equalsIgnoreCase("leave")) {
					KitEditorManager.leaveEditing(p);
					return true;
				}
				if(args[0].equalsIgnoreCase("save")) {
					KitEditorManager.saveKit(p);
					return true;
				}
				if(args[0].equalsIgnoreCase("reset")) {
					KitEditorManager.resetKitToDefault(p);
					return true;
				}
			}
			if(KitEditorManager.isEditing(p)) {
				p.sendMessage(plugin.translateMessage(p, "can-not-do-command"));
			}
			else if(p.hasMetadata(plugin.IN_FIGHT) || PvPEvent.isInEvent(p) || Fight.getCurrentFight(p, plugin) != null) {
				p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-match"));
			}
			else if(Party.getParty(p) != null) {
				p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-party"));
			}
			else plugin.getPlayerKitsHandler().openKitEditorSelector(p);
		}
		return true;
	}
}
