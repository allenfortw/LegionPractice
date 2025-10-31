package tech.hexadevelopment.practice.tablist;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.scoreboard.ScoreboardListener;

public class TabListManager {

	private List<String> rawTablist = new ArrayList<String>();

	private static TabListManager tabListManager;
	
	public TabListManager(LegionPractice plugin) {
		tabListManager = this;
		this.rawTablist = plugin.getConfig().getStringList("tab-list.slots");
		new TabListUpdater(plugin);
		new BukkitRunnable() {
			@Override
			public void run() {
				for(Player p : Bukkit.getOnlinePlayers()) {
					if(LegionPractice.getInstance().getNMSAccessProvider().getAccess().getVersion(p) != 47) {
						PlayerTabList playerTab = PlayerTabList.getCustomTab(p);
						if (PlayerTabList.getCustomTab(p) == null && !ScoreboardListener.tooEarly(p)) {
							new PlayerTabList(p, plugin);
						}
						else if((playerTab = PlayerTabList.getCustomTab(p)) != null){
							playerTab.clear();
							playerTab.assemble();
						}
					}
				}
			}
		}.runTaskLater(LegionPractice.getInstance(), 50);
	}

	public List<String> getRawTablist() {
		return rawTablist;
	}

	public static TabListManager getTabListManager() {
		return tabListManager;
	}

	public void updateTabLists() {
		for(Player p : Bukkit.getOnlinePlayers()) {
			PlayerTabList tabList = PlayerTabList.getCustomTab(p);
			if(tabList != null) {
				tabList.update();
			}
		}
	}

}
