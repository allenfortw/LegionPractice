package tech.hexadevelopment.practice.stats;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;

public class StatsCommand implements CommandExecutor{

	private LegionPractice plugin;
	public StatsCommand(LegionPractice plugin) {
		this.plugin = plugin;
	}

	private void formatStats(Player p, UUID target, String targetName) {
		p.sendMessage(plugin.translateMessage(p, "stats-message").replace("<player>", targetName));
		if(plugin.getConfig().getBoolean("show-stats.kills")) {
			int result = PlayerStats.getStats(target).getKills();
			p.sendMessage(plugin.translateMessage(p, "stats.kills").replace("<value>", result + ""));	
		}
		if(plugin.getConfig().getBoolean("show-stats.brackets")) {
			int result = PlayerStats.getStats(target).getBracketsWins();
			p.sendMessage(plugin.translateMessage(p, "stats.brackets").replace("<value>", result + ""));	
		}
		if(plugin.getConfig().getBoolean("show-stats.deaths")) {
			int result = PlayerStats.getStats(target).getDeaths();
			p.sendMessage(plugin.translateMessage(p, "stats.deaths").replace("<value>", result + ""));	
		}
		if(plugin.getConfig().getBoolean("show-stats.party-wins")) {
			int result = PlayerStats.getStats(target).getPartyVsPartyWins();
			p.sendMessage(plugin.translateMessage(p, "stats.party-wins").replace("<value>", result + ""));	
		}
		if(plugin.getConfig().getBoolean("show-stats.lms")) {
			int result = PlayerStats.getStats(target).getLMSWins();
			p.sendMessage(plugin.translateMessage(p, "stats.lms").replace("<value>", result + ""));	
		}
		if(plugin.getConfig().getBoolean("show-stats.elo")) {
			for(BattleKit kit : plugin.kits) {
				if(kit.isElo()) {
					int result = PlayerStats.getStats(target).getElo(kit);
					p.sendMessage(plugin.translateMessage(p, "stats.elo").replace("<value>", result + "").replace("<kit>", kit.getFancyName()));	
				}
			}
		}
		if(plugin.getConfig().getBoolean("show-stats.global-elo")) {
			int result = PlayerStats.getStats(target).getGlobalElo();
			p.sendMessage(plugin.translateMessage(p, "stats.global-elo").replace("<value>", result + ""));	
		}
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if(args.length == 0) {
				formatStats(p, p.getUniqueId(), p.getName());
			}
			if(args.length > 0) {
				Player tar = Bukkit.getPlayer(args[0]);
				if(tar != null) {
					formatStats(p, tar.getUniqueId(), tar.getName());
				}
				else if(!plugin.getConfig().getBoolean("offline-player-stats")) {
					p.sendMessage(plugin.translateMessage(p, "not-online"));
				}
				else {
					new BukkitRunnable() {

						@Override
						public void run() {

							OfflinePlayer of = Bukkit.getOfflinePlayer(args[0]);

							new BukkitRunnable() {

								@Override
								public void run() {
									if(p != null) {
										if(!of.hasPlayedBefore()) {
											p.sendMessage(plugin.translateMessage(p, "has-not-played"));
										}
										else {
											formatStats(p, of.getUniqueId(), of.getName());
										}
									}
								}
							}.runTask(plugin);
						}
					}.runTaskAsynchronously(plugin);
				}
			}
		}
		return true;
	}

}
