package tech.hexadevelopment.practice.playerdata;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.playerkits.PlayerKits;
import tech.hexadevelopment.practice.playerkits.PlayerKitsManager;
import tech.hexadevelopment.practice.playersettings.PlayerSettings;
import tech.hexadevelopment.practice.stats.PlayerStats;
import tech.hexadevelopment.practice.stats.Stats;
import tech.hexadevelopment.practice.strikecheat.FutureAPI;
import tech.hexadevelopment.practice.utils.ArenaPvP;

public class JAndQListener implements Listener{

	public static String LOADING_DATA = "LegionPracticeLoadingPlayerData";

	private LegionPractice plugin;

	public JAndQListener(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent e) {
		long st = System.currentTimeMillis();
		Player p = e.getPlayer();
		p.setMetadata(LOADING_DATA, new FixedMetadataValue(plugin, true));
		UUID uuid = e.getPlayer().getUniqueId();
		if(plugin.getConfig().getBoolean("join-lobby-teleport")) {
			new BukkitRunnable() {
				
				@Override
				public void run() {
					if(p != null) {
						plugin.clear(p, true, true);
					}
				}
			}.runTaskLater(plugin, 2);
		}
		if(ArenaPvP.UPDATE_MESSAGE != null && PermissionsManager.hasPermission(p, Permission.UPDATE)) {
			p.sendMessage(ArenaPvP.UPDATE_MESSAGE);
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				if(plugin.isMySQL) {
					Stats.getQueryManager().updateUsername(uuid);
				}
				PlayerKits kits = plugin.getPlayerKitsHandler().loadFromFile(uuid);
				new PlayerSettings(uuid);
				new PlayerStats(uuid, false);
				Bukkit.getScheduler().runTask(plugin, new Runnable() {

					@Override
					public void run() {
						if(p != null) {
							plugin.getPlayerKitsHandler().setPlayerKitsMeta(p, kits);
							p.removeMetadata(LOADING_DATA, plugin);
							long et = System.currentTimeMillis();
							Bukkit.getLogger().info("Loaded " + p.getName() + "'s data in " + (et-st) + " ms.");
						}
					}
				});
			}
		});
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		if(LegionPractice.disabling) return;
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		PlayerKits kit = plugin.getPlayerKitsHandler().getPlayerKits(p, false);
		p.removeMetadata("LegionPracticeRollbackRateAlerts", plugin);
		p.removeMetadata(PlayerKitsManager.PLAYERKITS_META, plugin);
		FutureAPI.cheatbreakers.remove(uuid);
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				PlayerStats stats = PlayerStats.getStats(uuid, false);
				if(stats != null) {
					stats.save();
					PlayerStats.getStats().remove(uuid);
				}
				if(kit != null) {
					kit.savePlayerKitsToFile();
				}
				PlayerSettings.getPlayerSettings(uuid).save();
				PlayerSettings.getSettings().remove(uuid);
			}
		});
	}
}
