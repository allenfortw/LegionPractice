package tech.hexadevelopment.practice.overwatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.matchrecorder.RecordedMatch;
import tech.hexadevelopment.practice.matchrecorder.playback.PlaybackFight;
import tech.hexadevelopment.practice.matchrecorder.playback.PlaybackLoadCallback;

public class OverwatchListener implements Listener {


	private LegionPractice plugin;

	public OverwatchListener(LegionPractice plugin) {
		this.plugin = plugin;
	}


	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getWhoClicked() instanceof Player && e.getInventory() != null && e.getCurrentItem() != null) {
			Player p = (Player) e.getWhoClicked();
			if(e.getInventory().getTitle().equals(plugin.getOverwatchManager().getTitle())) {
				e.setCancelled(true);
				ItemStack item = e.getCurrentItem();
				if(item.getType() == Material.SIGN) {
					List<RecordedMatch> matches = new ArrayList<RecordedMatch>();
					for(List<RecordedMatch> all : LegionPractice.getInstance().getRecordedMatchesManager().getRecordedDuels().values()) {
						for(RecordedMatch m : all) {
							if(m.needsOverwatch() && !plugin.getOverwatchManager().hasWatched(p.getUniqueId(), m)) {
								if(!matches.contains(m)) {
									matches.add(m);
								}
							}
						}
					}
					Collections.sort(matches, new OverwatchComparator());
					for(int i = 9; i < 18; i++) {
						if(matches.size() > i-9) {
							if(e.getSlot() == i) {
								PlaybackFight fight = matches.get(i-9).startPlayback(true);
								if(fight != null) {
									fight.addSpectator(p);
								}
								else {
									p.sendMessage(LegionPractice.getInstance().translateMessage(p, "playback-can-not-start"));
								}
							}
						}
						else break;
					}
				}
			}
			else if(e.getInventory().getTitle().equals(ChatColor.RED + "Judgement") && e.getInventory().getHolder() != null
					&& e.getInventory().getHolder() instanceof MatchInventoryHolder) {
				MatchInventoryHolder holder = (MatchInventoryHolder) e.getInventory().getHolder();
				e.setCancelled(true);
				if(holder.getMatch() != null) {
					UUID reported = OverwatchManager.highestUUID(holder.getMatch().getOriginalReports());
					if(e.getSlot() == 16) {
						List<UUID> values = holder.getMatch().getOverwatchReports().getOrDefault(reported, new ArrayList<UUID>());
						values.add(p.getUniqueId());
						holder.getMatch().getOverwatchReports().put(reported, values);
						holder.getMatch().loadEverythingAsync(new PlaybackLoadCallback() {

							@Override
							public void onSuccess() {
								holder.getMatch().saveToFile(true);
							}
						});
						p.sendMessage(plugin.getPrefix() + ChatColor.GREEN + "Thank you for your judgement!");
						check(holder.getMatch());
					}
					else if(e.getSlot() == 10) {
						List<UUID> values = holder.getMatch().getOverwatchLegit().getOrDefault(reported, new ArrayList<UUID>());
						values.add(p.getUniqueId());
						holder.getMatch().getOverwatchLegit().put(reported, values);
						holder.getMatch().loadEverythingAsync(new PlaybackLoadCallback() {

							@Override
							public void onSuccess() {
								holder.getMatch().saveToFile(true);
							}
						});
						p.sendMessage(plugin.getPrefix() + ChatColor.GREEN + "Thank you for your time!");
						check(holder.getMatch());
					}
				}
				p.closeInventory();
			}
		}
	}


	private void check(RecordedMatch match) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				int cheaterReports = match.getOverwatchReports().size();
				int total = cheaterReports+match.getOverwatchLegit().size();
				if(total == plugin.getConfig().getInt("overwatch.judgements-required")) {
					match.setOverwatched();
					if(total > 0 && cheaterReports/total >= plugin.getConfig().getDouble("overwatch.minimum-judgement-rate")) {
						UUID uuid = OverwatchManager.highestUUID(match.getOverwatchReports());
						String name = uuid.toString();
						if(uuid != null) {
							Player tar = Bukkit.getPlayer(uuid);
							if(tar != null) {
								name = tar.getName();
							}
							else name = Bukkit.getOfflinePlayer(uuid).getName();
						}
						String banCommand = plugin.getConfig().getString("overwatch.ban-command").replace("<player>", name);
						Bukkit.getScheduler().runTask(plugin, new Runnable() {

							@Override
							public void run() {
								Bukkit.dispatchCommand(Bukkit.getConsoleSender(), banCommand);
							}
						});
					}
				}
			}
		});
	}
}
