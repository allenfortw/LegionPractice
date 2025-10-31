package tech.hexadevelopment.practice.hostedevents.juggernaut;

import java.util.ArrayList;
import java.util.List;

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

public class JuggernautCommand implements CommandExecutor {

	
	public static List<String> juggernautRandom = new ArrayList<String>();
	public static Juggernaut juggernaut;
	public static boolean open;
	private LegionPractice plugin;
	
	
	public JuggernautCommand(LegionPractice plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String labe, String[] args) {
		if (args.length > 3 && args[0].equalsIgnoreCase("start")
				&& PermissionsManager.hasPermission(sender, Permission.JUGGERNAUT_HOST)) {
			tryToStart(sender, args, plugin);
		}
		else if(args.length == 0) {
			if(PermissionsManager.hasPermission(sender, Permission.JUGGERNAUT_HOST)) {
				sender.sendMessage(ChatColor.GOLD + "/juggernaut open");
				sender.sendMessage(ChatColor.GOLD + "/juggernaut start <player|-random> <juggernautkit> <otherskit>");
				sender.sendMessage(ChatColor.GOLD + "/juggernaut setspawn");
				sender.sendMessage(ChatColor.GOLD + "/juggernaut stop");
			}
			sender.sendMessage(ChatColor.YELLOW + "/juggernaut join");
			sender.sendMessage(ChatColor.YELLOW + "/juggernaut leave");
		}
		else if(args.length > 0 && PermissionsManager.hasPermission(sender, Permission.JUGGERNAUT_HOST)
				&& args[0].equalsIgnoreCase("stop")) {
			if(juggernaut != null) {
				juggernaut.stop();
				juggernautRandom.clear();
				sender.sendMessage(ChatColor.RED + "Stopped!");
			}
			open = false;
		}
		else if (args[0].equalsIgnoreCase("open")
				&& PermissionsManager.hasPermission(sender, Permission.JUGGERNAUT_HOST)) {
			if(!open) {
				sender.sendMessage(ChatColor.BLUE + "The event is now open!");
				open = true;
			}
			else sender.sendMessage(ChatColor.RED + "The event is already open!");
		}
		else if(sender instanceof Player) {
			Player p =(Player) sender;
			if(Fight.getCurrentFight(p, plugin) != null || p.hasMetadata(plugin.IN_FIGHT) || p.hasMetadata(QueueManager.waitingQueue) || PvPEvent.isInEvent(p)) {
				p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-match"));
				return true;
			}
			if(args.length > 0 && args[0].equalsIgnoreCase("leave")) {
				if(juggernaut != null) {
					if(juggernaut.getJuggernaut().equals(p.getName())) {
						juggernaut.eliminated(p.getName());
						return true;
					}
				}
				if(p.hasMetadata(Juggernaut.inJuggernaut)) {
					plugin.clear(p, true, true);
					p.removeMetadata(Juggernaut.inJuggernaut, plugin);
				}
				if(juggernautRandom.contains(p.getName())) {
					p.sendMessage(ChatColor.GOLD + "You won't become the juggernaut.");
					juggernautRandom.remove(p.getName());
				}
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
						p.sendMessage(plugin.translateMessage(p, "juggernaut-not-open"));
						return true;
					}
					else if(juggernaut == null || !juggernaut.hasStarted()) {
						p.sendMessage(plugin.translateMessage(p, "juggernaut-not-started"));
						if(!juggernautRandom.contains(p.getName())) {
							juggernautRandom.add(p.getName());
						}
						p.sendMessage(ChatColor.GOLD + "You have your chance to be the juggernaut!");
						return true;
					}
					juggernaut.addPlayer(p);
				}
			}
			else if(args.length > 0 && args[0].equalsIgnoreCase("setspawn") && PermissionsManager.hasPermission(p, Permission.ADMIN)) {
				plugin.getConfig().set("juggernaut.spawn", new SerializableLocation(p.getLocation()));
				plugin.saveConfig();
				p.sendMessage(ChatColor.RED + "Juggernaut spawn set!");
			}
			else {
				if(PermissionsManager.hasPermission(p, Permission.JUGGERNAUT_HOST)) {
					p.sendMessage(ChatColor.GOLD + "/juggernaut open");
					p.sendMessage(ChatColor.GOLD + "/juggernaut start <player|-random> <juggernautkit> <otherskit>");
					p.sendMessage(ChatColor.GOLD + "/juggernaut setspawn");
					p.sendMessage(ChatColor.GOLD + "/juggernaut stop");
				}
				p.sendMessage(ChatColor.YELLOW + "/juggernaut join");
				p.sendMessage(ChatColor.YELLOW + "/juggernaut leave");
			}
		}
		return false;
	}

	public static boolean tryToStart(CommandSender sender, String[] args, LegionPractice plugin) {
		if(juggernaut != null && juggernaut.hasStarted()) {
			sender.sendMessage("Juggernaut event has already started!");
			return false;
		}
		BattleKit juggernautKit = BattleKit.getKit(args[2]);
		BattleKit othersKit = BattleKit.getKit(args[3]);
		if(juggernautKit == null || othersKit == null) {
			sender.sendMessage(ChatColor.RED + "Invanlid kit!");
			return false;
		}
		String name = args[1];
		if(args[1].equalsIgnoreCase("-random")) {
			name = randomJuggernaut();
		}
		if(name == null) {
			sender.sendMessage(ChatColor.RED + "That player is not online!");
			return false;
		}
		Player juggernautPlayer = Bukkit.getPlayer(name);
		if(juggernautPlayer == null) {
			sender.sendMessage(ChatColor.RED + "That player is not online!");
			return false;
		}
		if(juggernautPlayer != null && Party.getParty(juggernautPlayer) == null && Fight.getCurrentFight(juggernautPlayer, LegionPractice.getInstance()) == null
				&& !PvPEvent.isInEvent(juggernautPlayer)) {
			Location spawn = getSpawn(plugin);
			if(spawn != null && Bukkit.getWorld(spawn.getWorld().getName()) != null) {
				sender.sendMessage(ChatColor.GREEN + "Starting...");	
				juggernaut = new Juggernaut(juggernautPlayer.getName(), juggernautKit, othersKit, spawn, plugin);
				juggernaut.start();
				for(String s : juggernautRandom) {
					Player p = Bukkit.getPlayer(s);
					if(p != null && Party.getParty(p) == null && Fight.getCurrentFight(p, LegionPractice.getInstance()) == null
							&& !PvPEvent.isInEvent(p)) {
						juggernaut.addPlayer(p);
					}
				}
				juggernautRandom.clear();
				return true;
			}
			else sender.sendMessage("The juggernaut spawn is invalid!");
		}
		else sender.sendMessage(ChatColor.RED + "The player is an event, party or fight.");
		return false;
	}

	private static Location getSpawn(LegionPractice plugin) {
		return ((SerializableLocation) plugin.getConfig()
				.get("juggernaut.spawn")).toLocation();
	}

	public static String randomJuggernaut() {
		List<String> asd = new ArrayList<String>();
		for(String s : juggernautRandom) {
			Player p = Bukkit.getPlayer(s);
			if(p != null && Party.getParty(p) == null && Fight.getCurrentFight(p, LegionPractice.getInstance()) == null
					&& !PvPEvent.isInEvent(p)) {
				asd.add(s);
			}
		}
		if(asd.size() == 1) return asd.get(0);
		if(asd.size() == 0) return null;
		String r =  asd.get(LegionPractice.random.nextInt(asd.size()));
		return r;
	}
}
