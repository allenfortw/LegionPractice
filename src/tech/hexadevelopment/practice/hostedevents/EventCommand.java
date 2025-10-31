package tech.hexadevelopment.practice.hostedevents;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.events.PlayerHostEvent;
import tech.hexadevelopment.practice.hostedevents.automaticevents.AutomaticEvent;
import tech.hexadevelopment.practice.hostedevents.brackets.BracketsCommand;
import tech.hexadevelopment.practice.hostedevents.juggernaut.JuggernautCommand;
import tech.hexadevelopment.practice.hostedevents.koth.KOTHCommand;
import tech.hexadevelopment.practice.hostedevents.lms.LMSCommand;
import tech.hexadevelopment.practice.hostedevents.sumo.SumoCommand;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.stats.Callback;
import tech.hexadevelopment.practice.stats.PlayerStats;
import tech.hexadevelopment.practice.LegionPractice;

public class EventCommand implements CommandExecutor {


	private LegionPractice plugin;

	public EventCommand(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		boolean kit = PermissionsManager.hasPermission(sender, Permission.HOST_EVENT_KIT);
		if(args.length > 0) {
			if(hasCooldown(sender, args[0])) {
				return true;
			}
			if(args[0].equalsIgnoreCase("brackets")) {
				if(PermissionsManager.hasPermission(sender, Permission.AUTOMATIC_BRACKETS)) {
					if(BracketsCommand.brackets != null) {
						if(sender instanceof Player) {
							sender.sendMessage(plugin.translateMessage((Player) sender, "brackets-already-started"));
						}
						else {
							sender.sendMessage(plugin.translateMessage("brackets-already-started", false));
						}
					}
					else {
						PlayerHostEvent event = new PlayerHostEvent(args[0], sender);
						Bukkit.getPluginManager().callEvent(event);
						if(event.isCancelled()) return true;
						sender.sendMessage(ChatColor.YELLOW + "Trying to start a brackets event...");
						AutomaticEvent autoEvent = new AutomaticEvent(plugin, args[0], kit && args.length > 1 ? BattleKit.getKit(args[1]) : null);
						autoEvent.setHoster(sender.getName());
						autoEvent.setCallback(callback(sender, args[0]));
					}
				}
				else if(sender instanceof Player) {
					sender.sendMessage(plugin.translateMessage((Player) sender, "no-permission"));
				}
				else sender.sendMessage("No permissions!");
				return true;
			}
			if(args[0].equalsIgnoreCase("sumo")) {
				if(PermissionsManager.hasPermission(sender, Permission.AUTOMATIC_SUMO)) {
					if(SumoCommand.sumo != null) {
						if(sender instanceof Player) {
							sender.sendMessage(plugin.translateMessage((Player) sender, "sumo-already-started"));
						}
						else {
							sender.sendMessage(plugin.translateMessage("sumo-already-started", false));
						}
					}
					else {
						PlayerHostEvent event = new PlayerHostEvent(args[0], sender);
						Bukkit.getPluginManager().callEvent(event);
						if(event.isCancelled()) return true;
						sender.sendMessage(ChatColor.YELLOW + "Trying to start a sumo event...");
						AutomaticEvent autoEvent = new AutomaticEvent(plugin, args[0]);
						autoEvent.setHoster(sender.getName());
						autoEvent.setCallback(callback(sender, args[0]));
					}
				}
				else if(sender instanceof Player) {
					sender.sendMessage(plugin.translateMessage((Player) sender, "no-permission"));
				}
				else sender.sendMessage("No permissions!");
				return true;
			}
			if(args[0].equalsIgnoreCase("koth")) {
				if(PermissionsManager.hasPermission(sender, Permission.AUTOMATIC_KOTH)) {
					if(KOTHCommand.open || (KOTHCommand.koth != null && KOTHCommand.koth.hasStarted())) {
						if(sender instanceof Player) {
							sender.sendMessage(plugin.translateMessage((Player) sender, "koth-already-started"));
						}
						else {
							sender.sendMessage(plugin.translateMessage("koth-already-started", false));
						}
					}
					else {
						PlayerHostEvent event = new PlayerHostEvent(args[0], sender);
						Bukkit.getPluginManager().callEvent(event);
						if(event.isCancelled()) return true;
						sender.sendMessage(ChatColor.YELLOW + "Trying to start a king of the hill event...");
						AutomaticEvent autoEvent = new AutomaticEvent(plugin, args[0], kit && args.length > 1 ? BattleKit.getKit(args[1]) : null);
						autoEvent.setHoster(sender.getName());
						autoEvent.setCallback(callback(sender, args[0]));
					}
				}
				else if(sender instanceof Player) {
					sender.sendMessage(plugin.translateMessage((Player) sender, "no-permission"));
				}
				else sender.sendMessage("No permissions!");
				return true;
			}
			if(args[0].equalsIgnoreCase("lms")) {
				if(PermissionsManager.hasPermission(sender, Permission.AUTOMATIC_LMS)) {
					if(LMSCommand.open || (LMSCommand.lms != null && LMSCommand.lms.hasStarted())) {
						if(sender instanceof Player) {
							sender.sendMessage(plugin.translateMessage((Player) sender, "lms-already-started"));
						}
						else {
							sender.sendMessage(plugin.translateMessage("lms-already-started", false));
						}
					}
					else {
						PlayerHostEvent event = new PlayerHostEvent(args[0], sender);
						Bukkit.getPluginManager().callEvent(event);
						if(event.isCancelled()) return true;
						sender.sendMessage(ChatColor.YELLOW + "Trying to start a last man standing event...");
						AutomaticEvent autoEvent = new AutomaticEvent(plugin, args[0], kit && args.length > 1 ? BattleKit.getKit(args[1]) : null);
						autoEvent.setHoster(sender.getName());
						autoEvent.setCallback(callback(sender, args[0]));
					}
				}
				else if(sender instanceof Player) {
					sender.sendMessage(plugin.translateMessage((Player) sender, "no-permission"));
				}
				else sender.sendMessage("No permissions!");
				return true;
			}
			if(args[0].equalsIgnoreCase("juggernaut")) {
				if(PermissionsManager.hasPermission(sender, Permission.AUTOMATIC_JUGGERNUT)) {
					if(args.length > 1) {
						if(JuggernautCommand.open) {
							if(sender instanceof Player) {
								sender.sendMessage(plugin.translateMessage((Player) sender, "juggernaut-already-started"));
							}
							else {
								sender.sendMessage(plugin.translateMessage("juggernaut-already-started", false));
							}
						}
						else {
							Player target = Bukkit.getPlayer(args[1]);
							if(target == null && !args[1].equalsIgnoreCase("-random")) {
								sender.sendMessage(sender instanceof Player ?
										plugin.translateMessage((Player) sender, "not-online") : "Player not found!");
							}
							else {
								PlayerHostEvent event = new PlayerHostEvent(args[0], sender);
								Bukkit.getPluginManager().callEvent(event);
								if(event.isCancelled()) return true;
								sender.sendMessage(ChatColor.YELLOW + "Trying to start a juggernaut event...");
								AutomaticEvent autoEvent = new AutomaticEvent(plugin, args[0], args[1]);
								autoEvent.setHoster(sender.getName());
								autoEvent.setCallback(callback(sender, args[0]));
							}
						}
					}
					else sender.sendMessage(ChatColor.GOLD + "/hostevent juggernaut <juggernaut player or -random>");
					return true;
				}
				else if(sender instanceof Player) {
					sender.sendMessage(plugin.translateMessage((Player) sender, "no-permission"));
				}
				else sender.sendMessage("No permissions!");
				return true;
			}
		}
		if(kit) {
			sender.sendMessage(ChatColor.GOLD + "/hostevent brackets [kit]");
			sender.sendMessage(ChatColor.GOLD + "/hostevent sumo");
			sender.sendMessage(ChatColor.GOLD + "/hostevent lms [kit]");
			sender.sendMessage(ChatColor.GOLD + "/hostevent koth [kit]");
			sender.sendMessage(ChatColor.GOLD + "/hostevent juggernaut <juggernaut player or -random>");
		}
		else {
			sender.sendMessage(ChatColor.GOLD + "/hostevent brackets");
			sender.sendMessage(ChatColor.GOLD + "/hostevent sumo");
			sender.sendMessage(ChatColor.GOLD + "/hostevent lms");
			sender.sendMessage(ChatColor.GOLD + "/hostevent koth");
			sender.sendMessage(ChatColor.GOLD + "/hostevent juggernaut <juggernaut player or -random>");	
		}
		return true;
	}
	
	
	private boolean hasCooldown(CommandSender sender, String event) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			PlayerStats stats = PlayerStats.getStats(p.getUniqueId());
			if(stats != null) {
				return stats.isOnCooldown(event, true);
			}
		}
		return false;
	}
	
	private Callback callback(CommandSender sender, String event) {
		UUID uuid = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
		return new Callback() {
			
			@Override
			public void onSuccess(int result) {
				if(result == 1 && uuid != null) {
					Player p = Bukkit.getPlayer(uuid);
					if(p != null) {
						PlayerStats stats = PlayerStats.getStats(uuid);
						long l = plugin.getConfig().getInt("cooldowns.hostevent." + event)*60*1000;
						stats.putOnCooldown(event, System.currentTimeMillis()+l);
					}
					else {
						new BukkitRunnable() {
							
							@Override
							public void run() {
								PlayerStats stats = PlayerStats.getStats(uuid, true, true);
								long l = plugin.getConfig().getInt("cooldowns.hostevent." + event)*60*1000;
								stats.putOnCooldown(event, System.currentTimeMillis()+l);
								stats.save();
							}
						}.runTaskAsynchronously(plugin);
					}
				}
			}
		};
	}
}
