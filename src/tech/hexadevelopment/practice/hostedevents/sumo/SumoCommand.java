package tech.hexadevelopment.practice.hostedevents.sumo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import tech.hexadevelopment.practice.LegionPractice;
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

public class SumoCommand implements CommandExecutor{

	
	public static Sumo sumo;
	private static BukkitTask autostart;
	private LegionPractice plugin;
	public static boolean starting;

	public SumoCommand(LegionPractice plugin) {
		this.plugin = plugin;
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length > 0 && PermissionsManager.hasPermission(sender, Permission.SUMO_HOST) && args[0].equalsIgnoreCase("start")) {
			if(args.length > 1) {
				tryToStart(sender, args[1], plugin);
				return true;
			}
			else sender.sendMessage(ChatColor.GRAY + "/sumo start <kit>");
		}
		if(args.length > 0 && PermissionsManager.hasPermission(sender, Permission.SUMO_HOST) && args[0].equalsIgnoreCase("open")) {
			if(Sumo.getLobby("sumo") == null) {
				sender.sendMessage(ChatColor.RED + "The sumo lobby is invalid!");
			}
			else if(sumo == null) {
				sender.sendMessage(ChatColor.BLUE + "The event is now open!");
				sumo = new Sumo(plugin);
			}
			else sender.sendMessage(ChatColor.RED + "The event is already open!");
		}
		else if(args.length > 0 && PermissionsManager.hasPermission(sender, Permission.SUMO_HOST) && args[0].equalsIgnoreCase("stop")) {
			if(sumo != null) {
				sumo.stop();
				sender.sendMessage(ChatColor.RED + "Stopped!");
			}
			starting = false;
		}
		else if(sender instanceof Player) {
			Player p = (Player) sender;
			if(args.length == 0) {
				if(PermissionsManager.hasPermission(sender, Permission.SUMO_HOST)) {
					p.sendMessage(ChatColor.GOLD + "/sumo open");
					p.sendMessage(ChatColor.GOLD + "/sumo start <kit>");
					p.sendMessage(ChatColor.GOLD + "/sumo stop");
					p.sendMessage(ChatColor.GOLD + "/sumo setlobby");
				}
				p.sendMessage(ChatColor.YELLOW + "/sumo join");
				p.sendMessage(ChatColor.YELLOW + "/sumo leave");
				return true;
			}
			if(args[0].equalsIgnoreCase("leave")) {
				if(sumo != null && sumo.getPlayers().containsKey(p.getName())) {
					if(sumo.getP1() != null && sumo.getP2() != null) {
						if(sumo.getP1().equals(p.getName()) || sumo.getP2().equals(p.getName())) {
							p.setHealth(0);
							return true;
						}
					}
					for(String s : sumo.getPlayers().keySet()) {
						Player pl = Bukkit.getPlayer(s);
						if(pl != null) pl.sendMessage(plugin.translateMessage(pl, "sumo-left").replace("<player>", p.getName()));
					}
					plugin.arenaPvP.lobby(p);
					sumo.getPlayers().remove(p.getName());
					if(!sumo.hasStarted()) {
						sumo.totalPlayers = sumo.getPlayers().size();
					}
					if(autostart != null && !enoughPlayers()) {
						for(Player pl : Bukkit.getOnlinePlayers()) {
							if(PermissionsManager.hasPermission(pl, Permission.STAFF)) {
								pl.sendMessage(ChatColor.RED + "sumo does not have has enough players to start automatically. Cancelling the sumo.");
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
				if(sumo == null) {
					p.sendMessage(plugin.translateMessage(p, "sumo-not-started"));
					return true;
				}
				if(Sumo.getLobby("sumo") == null) {
					p.sendMessage(ChatColor.RED + "Invalid sumo lobby!");
					return true;
				}
				if(sumo.hasStarted()) {
					p.sendMessage(plugin.translateMessage(p, "sumo-already-started"));
				}
				else if(!sumo.getPlayers().containsKey(p.getName())) {
					if(plugin.getSpectatorHandler().isSpectator(p)) {
						plugin.getSpectatorHandler().removeSpectator(p, false);
					}
					sumo.getPlayers().put(p.getName(), false);
					if(!sumo.hasStarted()) {
						sumo.totalPlayers = sumo.getPlayers().size();
					}
					sumo.lobby(p);
					for(String s : sumo.getPlayers().keySet()) {
						Player pl = Bukkit.getPlayer(s);
						if(pl != null) {
							pl.sendMessage(plugin.translateMessage(pl, "sumo-joined")
									.replace("<player>", p.getName()));
						}
					}
					int playersNeeded = plugin.getConfig().getInt("sumo.auto-start");
					if(!starting && autostart == null && playersNeeded == sumo.getPlayers().size() && !AutomaticEventTask.onGoing.containsKey(AutomaticEventType.SUMO)) {
						int secs = plugin.getConfig().getInt("sumo.auto-start-after-seconds");
						for(Player pl : Bukkit.getOnlinePlayers()) {
							if(PermissionsManager.hasPermission(pl, Permission.STAFF)) {
								pl.sendMessage(ChatColor.RED + "Sumo has now " + playersNeeded + " players, starting it automatically in " + secs + " seconds.");
							}
						}
						autostart = new BukkitRunnable() {

							@Override
							public void run() {
								for(Player pl : Bukkit.getOnlinePlayers()) {
									if(PermissionsManager.hasPermission(pl, Permission.STAFF)) {
										pl.sendMessage(ChatColor.RED + "Starting sumo automatically!");
									}
								}
								tryToStart(Bukkit.getConsoleSender(),
										plugin.getConfig().getString("automatic-events.sumo-kit"), plugin);
							}
						}.runTaskLater(plugin, 20*secs);
					}
				}
			}
			else if(args[0].equalsIgnoreCase("setlobby") && PermissionsManager.hasPermission(p, Permission.ADMIN)) {
				plugin.getConfig().set("sumo.lobby", new SerializableLocation(p.getLocation()));
				plugin.saveConfig();
				p.sendMessage(ChatColor.BLUE + "Sumo lobby set!");
			}
		}
		return true;

	}

	public static boolean enoughPlayers() {
		if(sumo == null || sumo.getPlayers() == null) {
			return false;
		}
		return sumo.enoughPlayers() && sumo.getPlayers().size() >= LegionPractice.getInstance().getConfig().getInt("sumo.auto-start");
	}

	public static boolean tryToStart(CommandSender sender, String k, LegionPractice plugin) {
		if(sumo == null) {
			sender.sendMessage(ChatColor.RED + "Can't start the sumo! The game is not open.");
		}
		else if(sumo.hasStarted()) {
			sender.sendMessage(ChatColor.RED + "The event has already started.");
		}
		else if(sumo.enoughPlayers()) {
			if(autostart != null) {
				autostart.cancel();
			}
			BattleKit kit = BattleKit.getKit(k);
			if(kit == null) {
				sender.sendMessage(ChatColor.RED + "Invanlid kit!");
				return false;
			}
			sumo.setKit(kit);
			sumo.start();
			return true;
		}
		else {
			sender.sendMessage(ChatColor.RED + "There are under 2 players in the sumo!");
			sumo.stop();
		}
		return false;
	}
}
