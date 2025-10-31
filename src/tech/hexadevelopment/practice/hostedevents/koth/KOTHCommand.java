package tech.hexadevelopment.practice.hostedevents.koth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.queue.QueueManager;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.utils.SerializableLocation;

public class KOTHCommand implements CommandExecutor{


	public static KOTH koth;
	private LegionPractice plugin;
	public static boolean open;
	public static List<UUID> joined = new ArrayList<UUID>();


	public KOTHCommand(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
		if (args.length > 1 && args[0].equalsIgnoreCase("start")
				&& PermissionsManager.hasPermission(sender, Permission.KOTH_HOST)) {
			tryToStart(sender, new String[]{args[1]}, plugin);
			return true;
		}
		else if(args.length > 0 && PermissionsManager.hasPermission(sender, Permission.KOTH_HOST)
				&& args[0].equalsIgnoreCase("stop")) {
			if(koth != null) {
				koth.stop();
				joined.clear();
				sender.sendMessage(ChatColor.RED + "Stopped!");
			}
			open = false;
			return true;
		}
		else if(args.length > 0) {
			if (args[0].equalsIgnoreCase("open") && PermissionsManager.hasPermission(sender, Permission.KOTH_HOST)) {
				if(getSpawn1(plugin) == null) {
					sender.sendMessage(ChatColor.RED + "The spawn1 location is invalid!");
				}
				if(getSpawn2(plugin) == null) {
					sender.sendMessage(ChatColor.RED + "The spawn2 location is invalid!");
				}
				if(getCorner1(plugin) == null) {
					sender.sendMessage(ChatColor.RED + "The corner1 location is invalid!");
				}
				if(getCorner2(plugin) == null) {
					sender.sendMessage(ChatColor.RED + "The corner2 location is invalid!");
				}
				else if(!open) {
					sender.sendMessage(ChatColor.BLUE + "The event is now open!");
					open = true;
				}
				else sender.sendMessage(ChatColor.RED + "The event is already open!");
				return true;
			}
		}
		if(sender instanceof Player) {
			Player p =(Player) sender;
			if(Fight.getCurrentFight(p, plugin) != null || p.hasMetadata(plugin.IN_FIGHT) || p.hasMetadata(QueueManager.waitingQueue) || PvPEvent.isInEvent(p)) {
				if(args.length > 0 && !args[0].equalsIgnoreCase("leave")) {
					p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-match"));
					return true;
				}
			}
			if(args.length > 0 && args[0].equalsIgnoreCase("leave")) {
				if(koth != null && (koth.getTeam1().getMembers().contains(p.getUniqueId())
						|| koth.getTeam2().getMembers().contains(p.getUniqueId()))) {
					koth.getTeam1().getMembers().remove(p.getUniqueId());
					koth.getTeam2().getMembers().remove(p.getUniqueId());
					plugin.clear(p, true, true);
					for(UUID s : joined) {
						Player sp = Bukkit.getPlayer(s);
						if(sp != null) {
							sp.sendMessage(plugin.translateMessage(sp, "koth-left").replace("<player>", p.getName()));
						}
					}
					if(plugin.getTagManager().COLORED_TAGS) {
						plugin.getTagManager().removeFromTeams(p);
					}
				}
				else {
					for(UUID s : joined) {
						Player sp = Bukkit.getPlayer(s);
						if(sp != null) {
							sp.sendMessage(plugin.translateMessage(sp, "koth-left").replace("<player>", p.getName()));
						}
					}
				}
				if(!joined.contains(p.getUniqueId())){
					p.sendMessage(plugin.translateMessage(p, "koth-left").replace("<player>", p.getName()));
				}
				joined.remove(p.getUniqueId());
				return true;
			}
			if(Party.getParty(p) != null) {
				p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-party"));
				return true;
			}
			if(args.length > 0 && args[0].equalsIgnoreCase("join")) {
				if(Fight.getCurrentFight(p, plugin) != null || PvPEvent.isInEvent(p)) {
					p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-match"));
					return true;
				}
				else {
					if(!open) {
						p.sendMessage(plugin.translateMessage(p, "koth-has-not-started"));
						return true;
					}
					if(koth == null || !koth.hasStarted()) {
						if(!joined.contains(p.getUniqueId())) {
							QueueManager.leaveQueue(p, true);
							if(plugin.getSpectatorHandler().isSpectator(p)) {
								plugin.getSpectatorHandler().removeSpectator(p, false);
							}
							joined.add(p.getUniqueId());
							for(UUID s : joined) {
								Player sp = Bukkit.getPlayer(s);
								if(sp != null) {
									sp.sendMessage(plugin.translateMessage(sp, "koth-joined").replace("<player>", p.getName()));
								}
							}
						}
					}
					else if(LegionPractice.getInstance().getConfig().getBoolean("koth.anytime-join")) {
						if(koth != null) {
							if(koth.hasStarted()) {
								koth.join(p);
							}
							else {
								p.sendMessage(plugin.translateMessage(p, "koth-has-not-started"));
							}
						}
					}
					else {
						p.sendMessage(plugin.translateMessage(p, "koth-already-started"));
					}
				}
			}
			else if(args.length > 0 && args[0].equalsIgnoreCase("spawn1") && PermissionsManager.hasPermission(p, Permission.ADMIN)) {
				plugin.getConfig().set("koth.spawn1", new SerializableLocation(p.getLocation()));
				plugin.saveConfig();
				p.sendMessage(ChatColor.RED + "You have set the spawn1.");
			}
			else if(args.length > 0 && args[0].equalsIgnoreCase("spawn2") && PermissionsManager.hasPermission(p, Permission.ADMIN)) {
				plugin.getConfig().set("koth.spawn2", new SerializableLocation(p.getLocation()));
				plugin.saveConfig();
				p.sendMessage(ChatColor.RED + "You have set the spawn2.");
			}
			else if(args.length > 0 && args[0].equalsIgnoreCase("corner1") && PermissionsManager.hasPermission(p, Permission.ADMIN)) {
				plugin.getConfig().set("koth.corner1", new SerializableLocation(p.getLocation()));
				plugin.saveConfig();
				p.sendMessage(ChatColor.RED + "You have set the corner1.");
			}
			else if(args.length > 0 && args[0].equalsIgnoreCase("corner2") && PermissionsManager.hasPermission(p, Permission.ADMIN)) {
				plugin.getConfig().set("koth.corner2", new SerializableLocation(p.getLocation()));
				plugin.saveConfig();
				p.sendMessage(ChatColor.RED + "You have set the corner2.");
			}
			else {
				if(PermissionsManager.hasPermission(p, Permission.KOTH_HOST)) {
					p.sendMessage(ChatColor.GOLD + "/koth open");
					p.sendMessage(ChatColor.GOLD + "/koth start <kit>");
					p.sendMessage(ChatColor.GOLD + "/koth spawn1");
					p.sendMessage(ChatColor.GOLD + "/koth spawn2");
					p.sendMessage(ChatColor.GOLD + "/koth corner1");
					p.sendMessage(ChatColor.GOLD + "/koth corner2");
					p.sendMessage(ChatColor.GOLD + "/koth stop");
				}
				p.sendMessage(ChatColor.YELLOW + "/koth join");
				p.sendMessage(ChatColor.YELLOW + "/koth leave");
			}
		}
		return false;
	}

	public static boolean tryToStart(CommandSender sender, String[] args, LegionPractice plugin) {
		if(koth != null && koth.hasStarted()) {
			sender.sendMessage("KOTH event has already started!");
			return false;
		}
		BattleKit kit = BattleKit.getKit(args[0]);
		if(kit == null) {
			sender.sendMessage(ChatColor.RED + "Invanlid kit!");
			return false;
		}
		Location spawn1 = null;
		Location spawn2 = null;
		Location corner1 = null;
		Location corner2 = null;
		try{
			spawn1 = getSpawn1(plugin);
			spawn2 = getSpawn2(plugin);
			corner1 = getCorner1(plugin);
			corner2 = getCorner2(plugin);
		}catch(Exception e) {
			sender.sendMessage(ChatColor.RED + "Invalid locations! Be sure that spawn, corner1 and corner2 are set before starting the event.");
			return false;
		}
		koth = new KOTH(kit, spawn1, spawn2, corner1, corner2, getTimer(plugin), plugin);
		koth.start();
		sender.sendMessage(ChatColor.YELLOW + "Starting...");	
		return true;
	}


	private static int getTimer(LegionPractice plugin) {
		return plugin.getConfig().getInt("koth.timer");
	}

	private static Location getSpawn1(LegionPractice plugin) {
		return getLocation(plugin, "koth.spawn1");
	}

	private static Location getSpawn2(LegionPractice plugin) {
		return getLocation(plugin, "koth.spawn2");
	}

	private static Location getCorner1(LegionPractice plugin) {
		return getLocation(plugin, "koth.corner1");
	}

	private static Location getCorner2(LegionPractice plugin) {
		return getLocation(plugin, "koth.corner2");
	}

	private static Location getLocation(LegionPractice plugin, String path) {
		Object o = plugin.getConfig()
				.get(path);
		if(o != null && o instanceof SerializableLocation) {
			return ((SerializableLocation) o).toLocation();
		}
		return null;
	}
}
