package tech.hexadevelopment.practice.misc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.playersettings.PlayerSettings;
import tech.hexadevelopment.practice.utils.LocationUtil;
import tech.hexadevelopment.practice.utils.SerializableLocation;

public class PlayerHider implements Listener {

	private Location corner1, corner2;

	public PlayerHider(LegionPractice plugin) {
		String c1 = plugin.getConfig().getString("player-spawn-hider.spawn-corner1");
		String c2 = plugin.getConfig().getString("player-spawn-hider.spawn-corner2");
		if(c1 != null && c2 != null) {
			corner1 = SerializableLocation.fromString(c1).toLocation();
			corner2 = SerializableLocation.fromString(c2).toLocation();
		}
		for(Player p : Bukkit.getOnlinePlayers()) {
			handleHide(p);
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				for(Player pl : Bukkit.getOnlinePlayers()) {
					if(PlayerSettings.getPlayerSettings(pl).isHideOtherPlayers()) {
						pl.hidePlayer(e.getPlayer());
					}
				}	
			}
		}.runTaskLater(LegionPractice.getInstance(), 2);
	}

	@EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
	public void onTeleport(PlayerTeleportEvent e) {
		if(!Bukkit.isPrimaryThread()) {
			new BukkitRunnable() {
				
				@Override
				public void run() {
					handleEvent(e);
				}
			}.runTask(LegionPractice.getInstance());
		}
		else {
			handleEvent(e);
		}
	}
	
	private void handleEvent(PlayerTeleportEvent e) {
		boolean fromIs = LocationUtil.isInregion(e.getFrom(), corner1, corner2);
		boolean toIs = LocationUtil.isInregion(e.getTo(), corner1, corner2);
		LegionPractice plugin = LegionPractice.getInstance();
		if(fromIs && !toIs) {
			new BukkitRunnable() {
				
				@Override
				public void run() {
					Fight f = Fight.getCurrentFight(e.getPlayer(), plugin);
					boolean spec = LegionPractice.getInstance().getSpectatorHandler().isSpectator(e.getPlayer());
					for(Player pl : Bukkit.getOnlinePlayers()) {
						if(LegionPractice.getInstance().getSpectatorHandler().isSpectator(pl) == spec
								&& (f == null || f == Fight.getCurrentFight(pl, plugin))
								&& sameEvent(pl, e.getPlayer())) {
							e.getPlayer().showPlayer(pl);
						}
					}
				}
			}.runTaskLater(plugin, 1);
		}
		else if(toIs && !fromIs) {
			boolean hide = PlayerSettings.getPlayerSettings(e.getPlayer()).isHideOtherPlayers();
			Party party = Party.getParty(e.getPlayer());
			for(Player pl : Bukkit.getOnlinePlayers()) {
				if(sameEvent(pl, e.getPlayer())) continue;
				if((party == null || !party.getMembers().contains(pl.getName())) && PlayerSettings.getPlayerSettings(pl).isHideOtherPlayers() && LocationUtil.isInregion(pl.getLocation(), corner1, corner2)) {
					pl.hidePlayer(e.getPlayer());
				}
				if(hide && (party == null || !party.getMembers().contains(pl.getName()))) {
					e.getPlayer().hidePlayer(pl);
				}
			}
		}
	}
	
	private boolean sameEvent(Player p1, Player p2) {
		PvPEvent e1 = PvPEvent.getEvent(p1);
		PvPEvent e2 = PvPEvent.getEvent(p2);
		if(e1 == null && e2 == null) return false;
		return e1 == e2;
	}

	public void handleHide(Player p) {
		if(corner1 == null || corner2 == null) return;
		if(!LocationUtil.isInregion(p.getLocation(), corner1, corner2)) {
			for(Player pl : Bukkit.getOnlinePlayers()) {
				if(LegionPractice.getInstance().getSpectatorHandler().isSpectator(pl)
						== LegionPractice.getInstance().getSpectatorHandler().isSpectator(p)) {
					p.showPlayer(pl);
				}
			}
		}
		else {
			boolean hide = PlayerSettings.getPlayerSettings(p).isHideOtherPlayers();
			Party party = Party.getParty(p);
			for(Player pl : Bukkit.getOnlinePlayers()) {
				if((party == null || !party.getMembers().contains(pl.getName())) && PlayerSettings.getPlayerSettings(pl).isHideOtherPlayers() && LocationUtil.isInregion(pl.getLocation(), corner1, corner2)) {
					pl.hidePlayer(p);
				}
				if(hide && (party == null || !party.getMembers().contains(pl.getName()))) {
					p.hidePlayer(pl);
				}
				else if(LegionPractice.getInstance().getSpectatorHandler().isSpectator(pl)
						== LegionPractice.getInstance().getSpectatorHandler().isSpectator(p)) {
					p.showPlayer(pl);
				}
			}
		}
	}
}
