package tech.hexadevelopment.practice.overwatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.matchrecorder.RecordedMatch;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.utils.FormatUtils;
import tech.hexadevelopment.practice.utils.ItemStackUtil;

public class OverwatchManager {

	public String getTitle() {
		return ChatColor.GREEN + "Overwatch";
	}

	public void openGUI(Player p) {
		Inventory inv = Bukkit.createInventory(null, PermissionsManager.hasPermission(p, Permission.ADMIN) ? 45 : 27, getTitle());
		p.openInventory(inv);
		List<RecordedMatch> matches = new ArrayList<RecordedMatch>();
		List<RecordedMatch> adminMatches = new ArrayList<RecordedMatch>();
		for(List<RecordedMatch> all : LegionPractice.getInstance().getRecordedMatchesManager().getRecordedDuels().values()) {
			for(RecordedMatch m : all) {
				if(m.needsOverwatch()) {
					if(!hasWatched(p.getUniqueId(), m)) {
						if(!matches.contains(m)) {
							matches.add(m);
						}
					}
					else if(!adminMatches.contains(m)) {
						adminMatches.add(m);
					}
				}
			}
		}
		Collections.sort(adminMatches, new OverwatchComparator());
		Collections.sort(matches, new OverwatchComparator());
		for(int i = 9; i < 18; i++) {
			if(matches.size() > i-9) {
				inv.setItem(i, createItem(matches.get(i-9), p, ChatColor.GREEN + "Pending case #" + (i-8)));
				p.updateInventory();
			}
			else break;
		}
		for(int i = 27; i < 36; i++) {
			if(adminMatches.size() > i-27) {
				inv.setItem(i, createItem(adminMatches.get(i-27), p, "Needs Admin Judgement #" + (i-27)));
				p.updateInventory();
			}
			else break;
		}
	}


	boolean hasWatched(UUID uuid, RecordedMatch m) {
		for(List<UUID> v : m.getOverwatchReports().values()) {
			if(v.contains(uuid)) {
				return true;
			}
		}
		for(List<UUID> v : m.getOverwatchLegit().values()) {
			if(v.contains(uuid)) {
				return true;
			}
		}
		return false;
	}

	public ItemStack createItem(RecordedMatch match, Player p, String displayName) {
		ItemStack item = new ItemStack(Material.SIGN);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayName);
		if(PermissionsManager.hasPermission(p, Permission.ADMIN)) {
			double ration = FormatUtils.toTwoDecimals(
					OverwatchComparator.highest(match.getOverwatchReports())/(OverwatchComparator.highest(match.getOverwatchLegit()) == 0 ? 1 : OverwatchComparator.highest(match.getOverwatchLegit())));
			UUID reported = highestUUID(match.getOriginalReports());
			String name = null;
			if(reported != null) {
				Player tar = Bukkit.getPlayer(reported);
				if(tar != null) {
					name = tar.getName();
				}
				else {
					OfflinePlayer op = Bukkit.getOfflinePlayer(reported);
					if(op != null) {
						name = op.getName();
					}
				}
			}
			String player = reported == null ? "unknown..." : name;
			meta.setLore(Arrays.asList(ChatColor.RED + "Admin information", "",
					ChatColor.GOLD + "Player: " + ChatColor.YELLOW + player,
					ChatColor.GOLD + "Overwatch Reports: " + ChatColor.YELLOW + OverwatchComparator.highest(match.getOverwatchReports()),
					ChatColor.GOLD + "Overwatch Legit: " + ChatColor.YELLOW + OverwatchComparator.highest(match.getOverwatchLegit()),
					ChatColor.GOLD + "Original Reports: " + ChatColor.YELLOW + OverwatchComparator.highest(match.getOriginalReports()),
					ChatColor.GOLD + "Overwatch Reports: " + ChatColor.YELLOW + OverwatchComparator.highest(match.getOverwatchReports()),
					ChatColor.GOLD + "Report/legit ration: " + ChatColor.YELLOW + (ration),
					ChatColor.GOLD + "UUID: " + ChatColor.YELLOW + reported));
		}
		item.setItemMeta(meta);
		return item;
	}


	public static UUID highestUUID(HashMap<UUID, List<UUID>> map) {
		int i = 0;
		UUID uuid = null;
		for(Entry<UUID, List<UUID>> e : map.entrySet()) {
			if(e.getValue().size() > i) {
				i = e.getValue().size();
				uuid = e.getKey();
			}
		}
		return uuid;
	}
	
	public void openJudgementGUI(Player p, RecordedMatch match) {
		Inventory inv = Bukkit.createInventory(new MatchInventoryHolder(match), 27, ChatColor.RED + "Judgement");
		inv.setItem(10, ItemStackUtil.createItem(Material.WOOL, ChatColor.RED + "The Suspect Is Legit", (byte) 5));
		inv.setItem(13, ItemStackUtil.createItem(Material.WOOL, ChatColor.RED + "Postpone Judgement", (byte) 8));
		inv.setItem(16, ItemStackUtil.createItem(Material.WOOL, ChatColor.RED + "The Suspect Is Cheating", (byte) 14));
		p.openInventory(inv);
	}

}
