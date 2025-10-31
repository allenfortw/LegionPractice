package tech.hexadevelopment.practice.hostedevents.juggernaut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.events.PvPEventEndEvent;
import tech.hexadevelopment.practice.events.PvPEventStartEvent;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.utils.Countdown;

public class Juggernaut implements PvPEvent{

	private LegionPractice plugin;
	private boolean started;
	private BattleKit othersKit;
	public static String inJuggernaut = "LegionPracticeJuggernaut";
	private BattleKit juggernautKit;
	private Location spawn;
	private String juggernaut;
	private long startTime;

	public Juggernaut(String juggernaut, BattleKit juggernautKit, BattleKit othersKit, Location location, LegionPractice plugin) {
		this.plugin = plugin;
		this.juggernautKit = juggernautKit;
		this.othersKit = othersKit;
		this.juggernaut = juggernaut;
		this.spawn = location;
	}


	public void start() {
		PvPEventStartEvent event = new PvPEventStartEvent(this);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) return;
		startTime = System.currentTimeMillis();
		setStarted(true);
		Player jug = Bukkit.getPlayer(juggernaut);
		if(jug == null) {
			eliminatedForLoggingOut();
			return;
		}
		jug.teleport(spawn);
		juggernautKit.giveKit(jug);
		if(LegionPractice.getInstance().getConfig().getBoolean("enable-colored-names")) {
			List<UUID> uuids = uuidsInJuggernaut();
			plugin.getTagManager().setTagToUUIDS(jug, "team2", uuids);
			for(Player pl : Bukkit.getOnlinePlayers()) {
				if(uuids.contains(pl.getUniqueId())) {
					plugin.getTagManager().setTagToUUIDS(pl, "team1", uuids);
				}
			}
		}
		List<String> players = new ArrayList<String>();
		players.add(jug.getName());
		Countdown.startCountdown(players);
		for(Player pl : Bukkit.getOnlinePlayers()) {
			pl.sendMessage(plugin.translateMessage(pl, "player-is-juggernaut").replace("<player>", jug.getName()));
		}
		for(String s : JuggernautCommand.juggernautRandom) {
			Player p = Bukkit.getPlayer(s);
			if(p != null && Party.getParty(p) == null && Fight.getCurrentFight(p, LegionPractice.getInstance()) == null
					&& !PvPEvent.isInEvent(p)) {
				addPlayer(p);
			}
		}
	}

	private List<UUID> uuidsInJuggernaut() {
		List<UUID> players = new ArrayList<UUID>();
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(p.hasMetadata(inJuggernaut)) {
				players.add(p.getUniqueId());
			}
		}
		return players;
	}

	private List<Player> playersInJuggernaut() {
		List<Player> players = new ArrayList<Player>();
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(p.hasMetadata(inJuggernaut)) {
				players.add(p);
			}
		}
		return players;
	}


	public void stop() {
		PvPEventEndEvent event = new PvPEventEndEvent(this, null);
		Bukkit.getPluginManager().callEvent(event);
		for(Player pl : Bukkit.getOnlinePlayers()) {
			if(pl.hasMetadata(inJuggernaut)) {
				plugin.clear(pl, true, true);
				pl.removeMetadata(inJuggernaut, plugin);
			}
		}
		for(Player pl : Bukkit.getOnlinePlayers()) {
			pl.sendMessage(plugin.translateMessage(pl, "event-stopped"));
		}
		Player jug = Bukkit.getPlayer(juggernaut);
		this.juggernaut = "";
		if(jug != null) {
			plugin.clear(jug, true, true);
			if(LegionPractice.getInstance().getConfig().getBoolean("enable-colored-names")) {
				plugin.getTagManager().removeFromTeams(jug);
			}
		}
		JuggernautCommand.open = false;
		JuggernautCommand.juggernaut = null;
	}

	public void addPlayer(Player p) {
		if(plugin.getSpectatorHandler().isSpectator(p)) {
			plugin.getSpectatorHandler().removeSpectator(p, false);
		}
		plugin.clear(p, false, true);
		p.teleport(spawn);
		p.setMetadata(inJuggernaut, new FixedMetadataValue(plugin, true));
		Player jug = Bukkit.getPlayer(juggernaut);
		if(jug == null) {
			eliminatedForLoggingOut();
			return;
		}
		if(LegionPractice.getInstance().getConfig().getBoolean("enable-colored-names")) {
			plugin.getTagManager().setTagToUUIDS(jug, "team2", uuidsInJuggernaut());
			for(Player pl : playersInJuggernaut()) {
				plugin.getTagManager().setTagToUUIDS(pl, "team1", uuidsInJuggernaut());
			}
		}
		Countdown.startCountdown(Arrays.asList(p.getName()));
		othersKit.giveKit(p);
	}

	public void eliminated(String killer) {
		Player jug = Bukkit.getPlayer(juggernaut);
		if(jug != null) {
			plugin.clear(jug, true, true);
			if(LegionPractice.getInstance().getConfig().getBoolean("enable-colored-names")) {
				plugin.getTagManager().removeFromTeams(jug);
			}
		}
		for(Player pl : Bukkit.getOnlinePlayers()) {
			if(pl.hasMetadata(inJuggernaut)) {
				plugin.clear(pl, true, true);
				pl.removeMetadata(inJuggernaut, plugin);
			}
		}
		for(Player pl : Bukkit.getOnlinePlayers()) {
			pl.sendMessage(plugin.translateMessage(pl, "juggernaut-eliminated")
					.replace("<player>", juggernaut).replace("<killer>", killer));
		}
		JuggernautCommand.juggernaut = null;
		juggernaut = null;
		stop();
	}

	public void eliminatedForLoggingOut() {
		Player jug = Bukkit.getPlayer(juggernaut);
		if(jug != null) {
			plugin.clear(jug, true, true);
			if(LegionPractice.getInstance().getConfig().getBoolean("enable-colored-names")) {
				plugin.getTagManager().removeFromTeams(jug);
			}
		}
		for(Player pl : Bukkit.getOnlinePlayers()) {
			if(pl.hasMetadata(inJuggernaut)) {
				plugin.clear(pl, true, true);
				pl.removeMetadata(inJuggernaut, plugin);
			}
		}
		for(Player pl : Bukkit.getOnlinePlayers()) {
			pl.sendMessage(plugin.translateMessage(pl, "juggernaut-eliminated-logging-out"));
		}
		stop();
		JuggernautCommand.juggernaut = null;
		juggernaut = null;
	}

	public boolean hasStarted() {
		return started;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public String getJuggernaut() {
		return juggernaut;
	}
}
