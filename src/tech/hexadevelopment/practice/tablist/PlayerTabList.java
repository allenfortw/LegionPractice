package tech.hexadevelopment.practice.tablist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.scoreboard.ScoreboardListener;
import tech.hexadevelopment.practice.scoreboard.ScoreboardManager;

public class PlayerTabList {


	private static HashSet<PlayerTabList> customTabLists = new HashSet<PlayerTabList>();

	private LegionPractice plugin;
	private List<TabEntry> entries = new ArrayList<TabEntry>();
	private Scoreboard scoreboard;
	private UUID uuid;

	public PlayerTabList(Player p, LegionPractice plugin) {
		this.plugin = plugin;
		uuid = p.getUniqueId();
		Bukkit.getScheduler().runTask(LegionPractice.getInstance(), new Runnable() {

			@Override
			public void run() {
				if(p != null) {
					//scoreboard should change the scoreboard and it shouldn't be default
					if(p.getScoreboard().equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
						//happens if you join while the plugin is still loading, checking that here
						if(System.currentTimeMillis()-LegionPractice.endedEnable < 5000
								|| ScoreboardManager.getScoreboardManager() != null) {
							new BukkitRunnable() {

								@Override
								public void run() {
									if(p != null) {
										//still has default scoreboard
										if(p.getScoreboard().equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
											scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
											p.setScoreboard(scoreboard);
										}
										scoreboard = p.getScoreboard();
										synchronized(customTabLists) {
											if(p != null) {
												if(!customTabLists.contains(PlayerTabList.this)) {
													customTabLists.add(PlayerTabList.this);
												}
												clear();
												assemble();
												update();
											}
										}
									}
								}
							}.runTaskLater(plugin, 20);
						}
						else {
							scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
							p.setScoreboard(scoreboard);
							synchronized(customTabLists) {
								if(p != null) {
									if(!customTabLists.contains(PlayerTabList.this)) {
										customTabLists.add(PlayerTabList.this);
									}
									clear();
									assemble();
									update();
								}
							}
						}
					}
					else {
						scoreboard = p.getScoreboard();
						if(p != null) {
							synchronized(customTabLists) {
								if(!customTabLists.contains(PlayerTabList.this)) {
									customTabLists.add(PlayerTabList.this);
								}
								clear();
								assemble();
								update();
							}
						}
					}
				}
			}
		});
	}

	void clear() {
		if(getPlayer() != null){
			plugin.getNMSAccessProvider().getAccess().clearTabList(getPlayer(), entries);
		}
	}
	
	void assemble() {
		for (int i = 0; i < TabListManager.getTabListManager().getRawTablist().size(); i++) {
			int x = i % 3;
			int y = i / 3;
			TabEntry entry = new TabEntry(this, getNextBlank(), x, y);
			entries.add(entry);
			entry.send();
		}
	}

	public void update() {
		if(ScoreboardListener.tooEarly(uuid)) return;
		List<String> tabList = new ArrayList<String>(TabListManager.getTabListManager().getRawTablist());
		int counter = 0;
		Player p = Bukkit.getPlayer(uuid);
		if(p != null) {
			for (int i = 0; i < 60; i++) {
				String nextText = tabList.size() > counter ? plugin.getPlaceholders().doPlaceholders(p, tabList.get(counter), "", true) : "";
				int x = i % 3;
				int y = i / 3;
				TabEntry entry = getByPosition(x, y);
				if(entry != null) {
					entry.setText(nextText);
					entry.send();
				}
				counter++;
			}
		}
	}

	public TabEntry getByPosition(int x, int y) {
		for (TabEntry tabEntry : entries) {
			if (tabEntry.getX() == x && tabEntry.getY() == y) {
				return tabEntry;
			}
		}
		return null;
	}

	public String getNextBlank() {
		outer: for (String string : getAllBlanks()) {
			for (TabEntry tabEntry : entries) {
				if (tabEntry.getText() != null && tabEntry.getText().startsWith(string)) {
					continue outer;
				}
			}
			return string;
		}
	return null;
	}

	private static List<String> getAllBlanks() {
		List<String> toReturn = new ArrayList<>();
		for (ChatColor chatColor : ChatColor.values()) {
			toReturn.add(chatColor + "" + ChatColor.RESET);
			for (ChatColor chatColor1 : ChatColor.values()) {
				if (toReturn.size() >= 60) {
					return toReturn;
				}
				toReturn.add(chatColor + "" + chatColor1 + ChatColor.RESET);
			}
		}

		return toReturn;
	}

	public static PlayerTabList getCustomTab(Player player) {
		for (PlayerTabList playerTab : customTabLists) {
			if(playerTab.getPlayer() != null && playerTab.getPlayer().getName().equals(player.getName())) {
				return playerTab;
			}
		}
		return null;
	}

	public static HashSet<PlayerTabList> getCustomTabLists() {
		return customTabLists;
	}

	public List<TabEntry> getEntries() {
		return entries;
	}

	public Scoreboard getScoreboard() {
		if(scoreboard == null || getPlayer().getScoreboard() != scoreboard) {
			entries.clear();
			scoreboard = getPlayer().getScoreboard();
			clear();
			assemble();
			update();
		}
		return scoreboard;
	}

	public Player getPlayer() {
		return Bukkit.getPlayer(uuid);
	}
}
