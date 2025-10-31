package tech.hexadevelopment.practice.fights.other;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import tech.hexadevelopment.practice.fightinventory.FightInventory;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.queue.QueueManager;
import tech.hexadevelopment.practice.fights.savedfights.SavedFight;
import tech.hexadevelopment.practice.utils.Countdown;
import tech.hexadevelopment.practice.utils.Teleporter;
import tech.hexadevelopment.practice.LegionPractice;

public class FFAFight extends Fight{

	private LegionPractice plugin;
	private HashSet<UUID> players = new HashSet<UUID>();
	private boolean ended;

	public FFAFight(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@Override
	public void start() {
		if(arena != null) {
			arena.setUsing(true, this);
		}
	}

	public void joinPlayer(Player p) {
		players.add(p.getUniqueId());
		QueueManager.leaveQueue(p, true);
		if(plugin.getSpectatorHandler().isSpectator(p)) {
			plugin.getSpectatorHandler().removeSpectator(p, false);
		}
		if(p.isDead()) p.spigot().respawn();
		plugin.clear(p, false, false);
		if(!Teleporter.teleport(p, arena.getCenter(), plugin.getConfig().getBoolean("no-arenas-in-default-world"))) {
			forceEnd(ChatColor.RED + "An error occurred when joining the FFA arena!");
			return;
		}
		p.setHealth(20);
		p.setFoodLevel(20);
		p.setFireTicks(0);
		kit.giveKit(p);
		List<String> players = new ArrayList<String>();
		players.add(p.getName());
		Countdown.startCountdown(players);
		p.closeInventory();
		Fight.setCurrentFight(p, this, plugin);
		p.setMetadata(plugin.IN_FIGHT, new FixedMetadataValue(plugin, true));
		kit.sendFightInfo(p);
	}

	@Override
	public boolean allowSpectating() {
		return false;
	}

	@Override
	public boolean hasEnded() {
		return ended;
	}
	
	public void setEnded(boolean ended) {
		this.ended = ended;
	}

	@Override
	public boolean canStart() {
		return arena != null && arena.getCenter() != null;
	}

	@Override
	public void handleDeath(Player p) {
		Fight.setCurrentFight(p, null, plugin);
		plugin.clear(p, true, true);
		players.remove(p.getUniqueId());
	}

	@Override
	public void forceEnd(String reason) {
		for(Player p : Bukkit.getOnlinePlayers()) {
			Fight fight = Fight.getCurrentFight(p, plugin);
			if(fight != null && fight.equals(this)) {
				handleDeath(p);
				p.sendMessage(reason);
			}
		}
	}

	@Override
	public SavedFight saveFight(List<UUID> winners, List<UUID> losers, List<FightInventory> winnersInventories,
			List<FightInventory> losersInventories, UUID playbackUUID) { 
		return null;
	}
	
	public HashSet<UUID> getPlayers() {
		return players;
	}

}
