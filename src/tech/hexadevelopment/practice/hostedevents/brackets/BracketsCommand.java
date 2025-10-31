package tech.hexadevelopment.practice.hostedevents.brackets;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.queue.QueueManager;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.hostedevents.automaticevents.AutomaticEventTask;
import tech.hexadevelopment.practice.hostedevents.automaticevents.AutomaticEventType;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.utils.SerializableLocation;
import tech.hexadevelopment.practice.LegionPractice;

public class BracketsCommand implements CommandExecutor{

	
	public static Brackets brackets;
	private static BukkitTask autostart;
	private LegionPractice plugin;
	public static boolean starting;

	public BracketsCommand(LegionPractice plugin) {
		this.plugin = plugin;
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length > 0 && PermissionsManager.hasPermission(sender, Permission.BRACKETS_HOST) && args[0].equalsIgnoreCase("start")) {
			if(args.length > 1) {
				tryToStart(sender, args[1], plugin);
				return true;
			}
			else sender.sendMessage(ChatColor.GRAY + "/Brackets start <kit>");
		}
		if(args.length > 0 && PermissionsManager.hasPermission(sender, Permission.BRACKETS_HOST) && args[0].equalsIgnoreCase("open")) {
			if(Brackets.getLobby("brackets") == null) {
				sender.sendMessage(ChatColor.RED + "The brackets lobby is invalid!");
			}
			else if(brackets == null) {
				sender.sendMessage(ChatColor.BLUE + "The event is now open!");
				brackets = new Brackets(plugin);
			}
			else sender.sendMessage(ChatColor.RED + "The event is already open!");
		}
		else if(args.length > 0 && PermissionsManager.hasPermission(sender, Permission.BRACKETS_HOST) && args[0].equalsIgnoreCase("stop")) {
			if(brackets != null) {
				brackets.stop();
				sender.sendMessage(ChatColor.RED + "Stopped!");
			}
			starting = false;
		}
		else if(sender instanceof Player) {
			Player p = (Player) sender;
			if(args.length == 0) {
				if(PermissionsManager.hasPermission(sender, Permission.BRACKETS_HOST)) {
					p.sendMessage(ChatColor.GOLD + "/Brackets open");
					p.sendMessage(ChatColor.GOLD + "/Brackets start <kit>");
					p.sendMessage(ChatColor.GOLD + "/Brackets stop");
					p.sendMessage(ChatColor.GOLD + "/Brackets setlobby");
				}
				p.sendMessage(ChatColor.YELLOW + "/Brackets join");
				p.sendMessage(ChatColor.YELLOW + "/Brackets leave");
				return true;
			}
			if(args[0].equalsIgnoreCase("leave")) {
				if(brackets != null && brackets.getPlayers().containsKey(p.getName())) {
					if(brackets.getP1() != null && brackets.getP2() != null) {
						if(brackets.getP1().equals(p.getName()) || brackets.getP2().equals(p.getName())) {
							p.setHealth(0);
							return true;
						}
					}
					for(String s : brackets.getPlayers().keySet()) {
						Player pl = Bukkit.getPlayer(s);
						if(pl != null) {
							pl.sendMessage(plugin.translateMessage(pl, "brackets-left").replace("<player>", p.getName()));
						}
					}
					plugin.arenaPvP.lobby(p);
					brackets.getPlayers().remove(p.getName());
					if(!brackets.hasStarted()) {
						brackets.totalPlayers = brackets.getPlayers().size();
					}
					if(autostart != null && !enoughPlayers()) {
						for(Player pl : Bukkit.getOnlinePlayers()) {
							if(PermissionsManager.hasPermission(pl, Permission.STAFF)) {
								pl.sendMessage(ChatColor.RED + "Brackets does not have has enough players to start automatically. Cancelling the brackets.");
							}
						}
						autostart.cancel();
					}
				}
				return true;
			}
			if(Party.getParty(p) != null) {
				p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-party"));
				return true;
			}
			if(Fight.getCurrentFight(p, plugin) != null || p.hasMetadata(plugin.IN_FIGHT) || p.hasMetadata(QueueManager.waitingQueue) || PvPEvent.isInEvent(p)) {
				p.sendMessage(plugin.translateMessage(p, "can-not-do-while-in-match"));
				return true;
			}
			if(args[0].equalsIgnoreCase("join")) {
				if(brackets == null) {
					p.sendMessage(plugin.translateMessage(p, "brackets-not-started"));
					return true;
				}
				if(Brackets.getLobby("brackets") == null) {
					p.sendMessage(ChatColor.RED + "Invalid brackets lobby!");
					return true;
				}
				if(brackets.hasStarted()) {
					p.sendMessage(plugin.translateMessage(p, "brackets-already-started"));
				}
				else if(!brackets.getPlayers().containsKey(p.getName())) {
					if(plugin.getSpectatorHandler().isSpectator(p)) {
						plugin.getSpectatorHandler().removeSpectator(p, false);
					}
					brackets.getPlayers().put(p.getName(), false);
					if(!brackets.hasStarted()) {
						brackets.totalPlayers = brackets.getPlayers().size();
					}
					brackets.lobby(p);
					for(String s : brackets.getPlayers().keySet()) {
						Player pl = Bukkit.getPlayer(s);
						if(pl != null) {
							pl.sendMessage(plugin.translateMessage(pl, "brackets-joined")
									.replace("<player>", p.getName()));
						}
					}
					int playersNeeded = plugin.getConfig().getInt("brackets.auto-start");
					if(!starting && autostart == null && playersNeeded == brackets.getPlayers().size() && !AutomaticEventTask.onGoing.containsKey(AutomaticEventType.BRACKETS)) {
						int secs = plugin.getConfig().getInt("brackets.auto-start-after-seconds");
						for(Player pl : Bukkit.getOnlinePlayers()) {
							if(PermissionsManager.hasPermission(pl, Permission.STAFF)) {
								pl.sendMessage(ChatColor.RED + "Brackets has " + playersNeeded + " players, starting it automatically in " + secs + " seconds.");
							}
						}
						autostart = new BukkitRunnable() {

							@Override
							public void run() {
								for(Player pl : Bukkit.getOnlinePlayers()) {
									if(PermissionsManager.hasPermission(pl, Permission.STAFF)) {
										pl.sendMessage(ChatColor.RED + "Starting brackets automatically!");
									}
								}
								tryToStart(Bukkit.getConsoleSender(),
										plugin.getConfig().getString("automatic-events.brackets-kit"), plugin);
							}
						}.runTaskLater(plugin, 20*secs);
					}
				}
			}
			else if(args[0].equalsIgnoreCase("setlobby") && PermissionsManager.hasPermission(p, Permission.ADMIN)) {
				plugin.getConfig().set("brackets.lobby", new SerializableLocation(p.getLocation()));
				plugin.saveConfig();
				p.sendMessage(ChatColor.BLUE + "Brackets lobby set!");
			}
		}
		return true;

	}

	public static boolean enoughPlayers() {
		if(brackets == null || brackets.getPlayers() == null) {
			return false;
		}
		return brackets.enoughPlayers() && brackets.getPlayers().size() >= LegionPractice.getInstance().getConfig().getInt("brackets.auto-start");
	}

	public static boolean tryToStart(CommandSender sender, String k, LegionPractice plugin) {
		if(brackets == null) {
			sender.sendMessage(ChatColor.RED + "Can't start the brackets! The game is not open.");
		}
		else if(brackets.hasStarted()) {
			sender.sendMessage(ChatColor.RED + "The event has already started.");
		}
		else if(brackets.enoughPlayers()) {
			if(autostart != null) {
				autostart.cancel();
				autostart = null;
			}
			BattleKit kit = BattleKit.getKit(k);
			if(kit == null) {
				sender.sendMessage(ChatColor.RED + "Invanlid kit!");
				return false;
			}
			brackets.setKit(kit);
			brackets.start();
			return true;
		}
		else {
			sender.sendMessage(ChatColor.RED + "There are under 2 players in the brackets!");
			brackets.stop();
		}
		return false;
	}
}
