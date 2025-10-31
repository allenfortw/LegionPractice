package tech.hexadevelopment.practice.hostedevents.lms;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.queue.QueueManager;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.utils.SerializableLocation;

public class LMSCommand implements CommandExecutor {


	public static String lmsWaiting = "ToppeBattlesLmsWaiting";
	public static LMS lms;
	public static boolean open;
	public static HashSet<String> joined = new HashSet<String>();
	private LegionPractice plugin;


	public LMSCommand(LegionPractice plugin) {
		this.plugin = plugin;
	}

	private static Location getSpawn(LegionPractice plugin) {
		SerializableLocation serLoc = ((SerializableLocation) plugin.getConfig()
				.get("lms.spawn"));
		return serLoc != null ? serLoc.toLocation() : null;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String labe, String[] args) {
		if (args.length > 1 && args[0].equalsIgnoreCase("start")
				&& PermissionsManager.hasPermission(sender, Permission.LMS_HOST)) {
			tryToStart(sender, args, plugin);
		}
		else if(args.length == 0) {
			if(PermissionsManager.hasPermission(sender, Permission.LMS_HOST)) {
				sender.sendMessage(ChatColor.GOLD + "/lms open");
				sender.sendMessage(ChatColor.GOLD + "/lms stop");
				sender.sendMessage(ChatColor.GOLD + "/lms start <kit>");
				sender.sendMessage(ChatColor.GOLD + "/lms setspawn");
			}
			sender.sendMessage(ChatColor.YELLOW + "/lms join");
			sender.sendMessage(ChatColor.YELLOW + "/lms leave");
		}
		else if (args[0].equalsIgnoreCase("stop")
				&& PermissionsManager.hasPermission(sender, Permission.LMS_HOST)) {
			if(lms != null) {
				lms.stop();
				joined.clear();
				sender.sendMessage(ChatColor.RED + "Stopped!");
			}
			open = false;
		}
		else if (args[0].equalsIgnoreCase("open")
				&& PermissionsManager.hasPermission(sender, Permission.LMS_HOST)) {
			if(getSpawn(plugin) == null) {
				sender.sendMessage(ChatColor.RED + "The LMS spawn is invalid!");
			}
			else if(!open) {
				sender.sendMessage(ChatColor.BLUE + "The event is now open!");
				open = true;
			}
			else sender.sendMessage(ChatColor.RED + "The event is already open!");
		}
		else if(sender instanceof Player) {
			Player p =(Player) sender;
			if(Party.getParty(p) != null) {
				p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-party"));
				return true;
			}
			if(args[0].equalsIgnoreCase("leave")) {
				p.removeMetadata(lmsWaiting, plugin);
				if(joined.contains(p.getName())) {
					if(lms != null && lms.hasStarted()) p.setHealth(0);
					for(String s : joined) {
						Player sp = Bukkit.getPlayer(s);
						if(sp != null) {
							sp.sendMessage(plugin.translateMessage(sp, "lms-left").replace("<player>", p.getName()));
						}
					}
					joined.remove(p.getName());
					return true;
				}
				if(lms != null && !lms.hasStarted()) {
					lms.getPlayers().remove(p.getName());
				}
				return true;
			}
			if(Fight.getCurrentFight(p, plugin) != null || p.hasMetadata(plugin.IN_FIGHT) || p.hasMetadata(QueueManager.waitingQueue) || PvPEvent.isInEvent(p)) {
				p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-match"));
				return true;
			}
			if (args[0].equalsIgnoreCase("join")) {
				if(!open) {
					p.sendMessage(plugin.translateMessage(p, "lms-has-not-started"));
					return true;
				}
				if(!joined.contains(p.getName())) {
					if(lms != null && lms.hasStarted()) {
						p.sendMessage(plugin.translateMessage(p, "lms-already-started"));
						return true;
					}
					if(plugin.getSpectatorHandler().isSpectator(p)) {
						plugin.getSpectatorHandler().removeSpectator(p, false);
					}
					QueueManager.leaveQueue(p, true);
					joined.add(p.getName());
					p.setMetadata(lmsWaiting, new FixedMetadataValue(plugin, true));
					for(String s : joined) {
						Player sp = Bukkit.getPlayer(s);
						if(sp != null) {
							sp.sendMessage(plugin.translateMessage(sp, "lms-joined").replace("<player>", p.getName()));
						}
					}
				}
			}
			else if(args[0].equalsIgnoreCase("setspawn") &&  PermissionsManager.hasPermission(sender, Permission.ADMIN)) {
				plugin.getConfig().set("lms.spawn", new SerializableLocation(p.getLocation()));
				plugin.saveConfig();
				p.sendMessage(ChatColor.RED + "LMS spawn set!");
			}
		}
		return false;
	}

	public static boolean tryToStart(CommandSender sender, String[] args, LegionPractice plugin) {
		if(lms != null && lms.hasStarted()) {
			if(sender instanceof Player) sender.sendMessage(plugin.translateMessage((Player) sender, "lms-already-started"));
			else sender.sendMessage("LMS event has already started!");
			return false;
		}
		if(getSpawn(plugin) == null) {
			sender.sendMessage(ChatColor.RED + "Invalid lms spawn!");
			return false;
		}
		if(joined.size() < 2) {
			sender.sendMessage("Can't start, there are under 2 players in the lms.");
			return false;
		}
		BattleKit kit = BattleKit.getKit(args[1]);
		if(kit == null) {
			sender.sendMessage(ChatColor.RED + "Invanlid kit!");
			return false;
		}
		Location spawn = getSpawn(plugin);
		if(spawn != null && Bukkit.getWorld(spawn.getWorld().getName()) != null) {
			lms = new LMS(joined, kit, spawn, plugin);
			lms.start();
			sender.sendMessage("Starting...");	
			return true;
		}
		else sender.sendMessage("The lms spawn is invalid!");
		return false;
	}
}
