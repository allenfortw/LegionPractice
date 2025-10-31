package tech.hexadevelopment.practice.tablist;

import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;

public class TabListUpdater extends BukkitRunnable{

	public TabListUpdater(LegionPractice plugin) {
		if(plugin.getConfig().getBoolean("tab-list.async-update")) {
			runTaskTimerAsynchronously(plugin, 0, plugin.getConfig().getLong("tab-list.update-time"));
		}
		else {
			runTaskTimer(plugin, 0, plugin.getConfig().getLong("tab-list.update-time"));
		}
	}
	
	@Override
	public void run() {
		TabListManager.getTabListManager().updateTabLists();
	}
}