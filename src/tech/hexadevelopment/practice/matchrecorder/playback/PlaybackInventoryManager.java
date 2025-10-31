package tech.hexadevelopment.practice.matchrecorder.playback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.matchrecorder.RecordedMatch;
import tech.hexadevelopment.practice.utils.ItemStackUtil;

public class PlaybackInventoryManager {


	public static HashMap<UUID, List<RecordedMatch>> viewing = new HashMap<UUID, List<RecordedMatch>>();


	public static void openGUI(Player p, int page) {
		if(viewing.containsKey(p.getUniqueId())) {
			openGUI(p, page, viewing.get(p.getUniqueId()));	
		}
		else {
			openGUI(p, page, LegionPractice.getInstance().getRecordedMatchesManager().getRecordedDuels().getOrDefault(p.getUniqueId(), new ArrayList<RecordedMatch>()));
		}
	}

	public static void openGUI(Player p, int page, List<RecordedMatch> matches) {
		if(PlaybackCommand.disabled) {
			p.sendMessage(ChatColor.RED + "Playback feature has not been published yet!");
			return;
		}
		LegionPractice plugin = LegionPractice.getInstance();
		if(page < 1) page = 1; 
		if(plugin.getRecordedMatchesManager().isLoading()) {
			p.sendMessage(plugin.translateMessage(p, "data-not-loaded"));
			return;
		}
		Inventory inv = Bukkit.createInventory(null, 45, ChatColor.translateAlternateColorCodes('&',
				plugin.getConfig().getString("playback-gui-title")) + " | Page: " + page);
		updateInventory(inv, page, matches);
		p.openInventory(inv);
		viewing.put(p.getUniqueId(), matches);
		int finalPage = page;
		new BukkitRunnable() {

			@Override
			public void run() {
				if(!inv.getViewers().isEmpty()) {
					updateInventory(inv, finalPage, matches);
				}
				else this.cancel();
			}
		}.runTaskTimer(plugin, 50, 50);
	}

	private static void updateInventory(Inventory inv, int page, List<RecordedMatch> matches) {
		for(int i = 0; i < 9; i++) {
			inv.setItem(i, ItemStackUtil.createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15));
		}
		for(int i = 36; i < 45; i++) {
			inv.setItem(i, ItemStackUtil.createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15));
		}
		inv.setItem(4, ItemStackUtil.createItem(Material.COMPASS, ChatColor.GOLD + "Search (Click)"));
		inv.setItem(9, ItemStackUtil.createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15));
		inv.setItem(17, ItemStackUtil.createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15));
		inv.setItem(26, ItemStackUtil.createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15));
		inv.setItem(35, ItemStackUtil.createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15));
		inv.setItem(18, ItemStackUtil.createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15));
		inv.setItem(27, ItemStackUtil.createItem(Material.STAINED_GLASS_PANE, " ", (byte) 15));	
		int allMatches = matches.size();
		if(allMatches > 21*page) {
			inv.setItem(26, ItemStackUtil.createItem(Material.ARROW, ChatColor.GREEN + "Next Page"));
		}
		if(page > 1) {
			inv.setItem(18, ItemStackUtil.createItem(Material.ARROW, ChatColor.GREEN + "Previous Page"));	
		}
		int counter = 0;
		int pageStart = 21*(page-1);
		for(RecordedMatch rm : matches) {
			pageStart--;
			if(pageStart < 0) {
				if(counter != 21) {
					inv.setItem(new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34}[counter], buildItem(rm));
					counter++;
				}
				else break;
			}
		}
	}

	public static ItemStack buildItem(RecordedMatch rm) {
		ItemStack is = new ItemStack(Material.PAPER);
		ItemMeta meta = is.getItemMeta();
		String players = rm.getPlayers().values().toString().replace("[", "").replaceAll("]", "");
		if(players.length() > 32) {
			players = players.substring(0, 29) + "...";
		}
		meta.setDisplayName(ChatColor.AQUA + players);
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.GOLD + "Date: " + ChatColor.YELLOW + rm.getDateFormat());
		lore.add(ChatColor.GOLD + "Kit: " + ChatColor.YELLOW +  rm.getKit().getFancyName());
		boolean st = rm.updateArena() && rm.getArena() != null && !rm.getArena().needsRollback() && !rm.getArena().isUsing() && rm.getArena().getLoc1() != null
				&& rm.getArena().getLoc2() != null && rm.getArena().getLoc1().getWorld() != null;
		String status = st ? ChatColor.GREEN + " (Available) " : ChatColor.RED + " (Unavailable) ";
		lore.add(ChatColor.GOLD + "Arena: " + ChatColor.YELLOW +  rm.getArenaName() + status);
		lore.add(ChatColor.GOLD + "Length: " + ChatColor.YELLOW +  new SimpleDateFormat("mm:ss").format(new Date(LegionPractice.dateFix + rm.getEnded()-rm.getStarted())));
		lore.add(ChatColor.GOLD + "Share Code: " + ChatColor.YELLOW +  rm.getUUID().toString().substring(rm.getUUID().toString().length()-4).toUpperCase());
		meta.setLore(lore);
		is.setItemMeta(meta);
		return is;
	}
}
