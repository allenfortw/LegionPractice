package tech.hexadevelopment.practice.stats;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.utils.SerializableLocation;
import tech.hexadevelopment.practice.utils.signgui.SignFinishCallback;
import tech.hexadevelopment.practice.utils.signgui.SignGUI;

public class SignStats implements Listener {

	private LegionPractice plugin;

	public SignStats(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onCreateSign(SignChangeEvent e) {
		String[] lines = e.getLines();
		if (lines.length == 0) return;
		if(!PermissionsManager.hasPermission(e.getPlayer(), Permission.ADMIN)) return;
		if (lines[0].toLowerCase().contains("[stats]")) {
			e.setLine(0, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.line-1")));
			if(lines.length == 1){
				e.setLine(1, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.line-2")));
				return;
			}
			if(lines[1].toLowerCase().contains("kills")) {
				plugin.getConfig().set("sign-stats.locations.kills", new SerializableLocation(e.getBlock().getLocation()).toString());
				e.setLine(1, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.kills-line")));
				plugin.saveConfig();
				return;
			}
			if(lines[1].toLowerCase().contains("deaths")) {
				plugin.getConfig().set("sign-stats.locations.deaths", new SerializableLocation(e.getBlock().getLocation()).toString());
				e.setLine(1, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.deaths-line")));
				plugin.saveConfig();
				return;
			}
			if(lines[1].toLowerCase().contains("lms")) {
				plugin.getConfig().set("sign-stats.locations.lms", new SerializableLocation(e.getBlock().getLocation()).toString());
				e.setLine(1, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.lms-line")));
				plugin.saveConfig();
				return;
			}
			if(lines[1].toLowerCase().contains("party")) {
				plugin.getConfig().set("sign-stats.locations.party-wins", new SerializableLocation(e.getBlock().getLocation()).toString());
				e.setLine(1, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.party-wins-line")));
				plugin.saveConfig();
				return;
			}
			if(lines[1].toLowerCase().contains("brackets")) {
				plugin.getConfig().set("sign-stats.locations.brackets", new SerializableLocation(e.getBlock().getLocation()).toString());
				e.setLine(1, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.brackets-line")));
				plugin.saveConfig();
				return;
			}
			if(lines[1].toLowerCase().contains("global")) {
				plugin.getConfig().set("sign-stats.locations.global-elo", new SerializableLocation(e.getBlock().getLocation()).toString());
				e.setLine(1, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.global-elo-line")));
				plugin.saveConfig();
				return;
			}
			if(lines.length > 2) {
				if(lines[1].toLowerCase().contains("elo")) {
					BattleKit kit = BattleKit.getKit(ChatColor.stripColor(lines[2]));
					if(kit == null) {
						e.getPlayer().sendMessage(ChatColor.RED + "Kit not found.");
					}
					else {
						plugin.getConfig().set("sign-stats.locations.elo." + ChatColor.stripColor(lines[2].toLowerCase()), new SerializableLocation(e.getBlock().getLocation()).toString());
						e.setLine(1, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.elo-line")).replace("<kit>", kit.getFancyName()));
						plugin.saveConfig();
					}
					return;
				}
			}
			else e.getPlayer().sendMessage(ChatColor.RED + "Line 3 must be the name of the kit!");
		}
	}

	@EventHandler
	public void onRightClick(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK || !(e.getClickedBlock().getState() instanceof Sign)) {
			return;
		}
		Sign s = (Sign)e.getClickedBlock().getState();
		if(s.getLine(0).equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.line-1")))) {
			Player p = e.getPlayer();
			String[] split = plugin.translateMessage(p, "type-name", false).split("/");
			SignGUI.openSignEditor(p, split, new SignFinishCallback() {

				@Override
				public void onFinish(String[] lines) {
					if(lines.length > 0 && lines[0] != null) {
						String name = lines[0].replace(" ", "");
						if(name != null && !name.equals("")) {
							Player tar = Bukkit.getPlayer(name);
							if(tar != null) {
								showStats(p, tar.getUniqueId(), tar.getName());
							}
							else {
								showOfflineStats(p, lines[0]);
							}
						}
					}
				}
			});
		}
	}

	private void showOfflineStats(Player p, String name) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				@SuppressWarnings("deprecation")
				OfflinePlayer of = Bukkit.getOfflinePlayer(name);
				if(!of.hasPlayedBefore()) {
					p.sendMessage(plugin.translateMessage(p, "has-not-played"));
				}
				else {
					UUID uuid = of.getUniqueId();
					Bukkit.getScheduler().runTask(plugin, new Runnable() {

						@Override
						public void run() {
							showStats(p, uuid, of.getName());
						}
					});
				}
			}
		});
	}

	private void showStats(Player p, UUID uuid, String name) {
		p.sendMessage(plugin.translateMessage(p, "showing-stats").replace("<player>", name));
		if(plugin.getConfig().getString("sign-stats.locations.kills") != null) {
			Location l = SerializableLocation.fromString(plugin.getConfig().getString("sign-stats.locations.kills")).toLocation();
			if(l != null) {
				if(l.getBlock().getType().equals(Material.SIGN) || l.getBlock().getType().equals(Material.SIGN_POST) || l.getBlock().getType().equals(Material.WALL_SIGN)) {
					int result = PlayerStats.getStats(uuid).getKills();
					String lines[] = new String[]{
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.line-1")),
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.player-line"))
							.replace("<player>", name),
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.kills-line")),
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.value")
									.replace("<value>", result + ""))};
					p.sendSignChange(l, lines);
				}
			}
		}
		if(plugin.getConfig().getString("sign-stats.locations.deaths") != null) {
			Location l = SerializableLocation.fromString(plugin.getConfig().getString("sign-stats.locations.deaths")).toLocation();
			if(l != null) {
				if(l.getBlock().getType().equals(Material.SIGN) || l.getBlock().getType().equals(Material.SIGN_POST) || l.getBlock().getType().equals(Material.WALL_SIGN)) {
					int result = PlayerStats.getStats(uuid).getDeaths();
					String lines[] = new String[]{
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.line-1")),
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.player-line"))
							.replace("<player>", name),
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.deaths-line")),
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.value")
									.replace("<value>", result + ""))};
					p.sendSignChange(l, lines);
				}
			}
		}
		if(plugin.getConfig().getString("sign-stats.locations.brackets") != null) {
			Location l = SerializableLocation.fromString(plugin.getConfig().getString("sign-stats.locations.brackets")).toLocation();
			if(l != null) {
				if(l.getBlock().getType().equals(Material.SIGN) || l.getBlock().getType().equals(Material.SIGN_POST) || l.getBlock().getType().equals(Material.WALL_SIGN)) {
					int result = PlayerStats.getStats(uuid).getBracketsWins();
					String lines[] = new String[]{
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.line-1")),
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.player-line"))
							.replace("<player>", name),
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.brackets-line")),
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.value")
									.replace("<value>", result + ""))};
					p.sendSignChange(l, lines);
				}
			}
		}
		if(plugin.getConfig().getString("sign-stats.locations.lms") != null) {
			Location l = SerializableLocation.fromString(plugin.getConfig().getString("sign-stats.locations.lms")).toLocation();
			if(l != null) {
				if(l.getBlock().getType().equals(Material.SIGN) || l.getBlock().getType().equals(Material.SIGN_POST) || l.getBlock().getType().equals(Material.WALL_SIGN)) {
					int result = PlayerStats.getStats(uuid).getLMSWins();
					String lines[] = new String[]{
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.line-1")),
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.player-line"))
							.replace("<player>", name),
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.lms-line")),
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.value")
									.replace("<value>", result + ""))};
					p.sendSignChange(l, lines);
				}
			}
		}
		if(plugin.getConfig().getString("sign-stats.locations.party-wins") != null) {
			Location l = SerializableLocation.fromString(plugin.getConfig().getString("sign-stats.locations.party-wins")).toLocation();
			if(l != null) {
				if(l.getBlock().getType().equals(Material.SIGN) || l.getBlock().getType().equals(Material.SIGN_POST) || l.getBlock().getType().equals(Material.WALL_SIGN)) {
					int result = PlayerStats.getStats(uuid).getPartyVsPartyWins();
					String lines[] = new String[]{
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.line-1")),
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.player-line")).replace("<player>", name),
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.party-wins-line")),
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.value").replace("<value>", result + ""))};
					p.sendSignChange(l, lines);
				}
			}
		}
		if(plugin.getConfig().getString("sign-stats.locations.global-elo") != null) {
			Location l = SerializableLocation.fromString(plugin.getConfig().getString("sign-stats.locations.global-elo")).toLocation();
			if(l != null) {
				if(l.getBlock().getType().equals(Material.SIGN) || l.getBlock().getType().equals(Material.SIGN_POST) || l.getBlock().getType().equals(Material.WALL_SIGN)) {
					int result = PlayerStats.getStats(uuid).getGlobalElo();
					String lines[] = new String[]{
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.line-1")),
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.player-line")).replace("<player>", name),
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.global-elo-line")),
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.value").replace("<value>", result + ""))};
					p.sendSignChange(l, lines);
				}
			}
		}
		for(BattleKit kit : plugin.kits) {
			if(kit.isElo()) {
				if(plugin.getConfig().getString("sign-stats.locations.elo." + kit.getName().toLowerCase()) != null) {			
					Location l = SerializableLocation.fromString(plugin.getConfig().getString("sign-stats.locations.elo." + kit.getName())).toLocation();
					if(l != null) {
						if(l.getBlock().getType().equals(Material.SIGN) || l.getBlock().getType().equals(Material.SIGN_POST) || l.getBlock().getType().equals(Material.WALL_SIGN)) {
							int result = PlayerStats.getStats(uuid).getElo(kit);
							String lines[] = new String[]{
									ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.line-1")),
									ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.player-line")).replace("<player>", name),
									ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.elo-line").replace("<kit>", kit.getFancyName())),
									ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sign-stats.value").replace("<value>", result + ""))};
							p.sendSignChange(l, lines);
						}
					}
				}
			}
		}
	}
}
