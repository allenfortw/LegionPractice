package tech.hexadevelopment.practice.tablist;

import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import tech.hexadevelopment.practice.LegionPractice;

public class TabListListener implements Listener {


	private LegionPractice plugin;


	public TabListListener(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		new BukkitRunnable() {
			@Override
			public void run() {
				if(p == null) return;
				if(LegionPractice.getInstance().getNMSAccessProvider().getAccess().getVersion(p) != 47) {
					PlayerTabList playerTab = PlayerTabList.getCustomTab(p);
					if (playerTab == null) {
						new PlayerTabList(p, plugin);
					}
					else {
						playerTab.clear();
						playerTab.assemble();
						playerTab.update();
					}
				}
			}
		}.runTaskLater(LegionPractice.getInstance(), 20);
	}

	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		PlayerTabList playerTab = PlayerTabList.getCustomTab(p);
		if (playerTab != null) {
			for (Team team : new HashSet<>(playerTab.getScoreboard().getTeams())) {
				team.unregister();
			}
			PlayerTabList.getCustomTabLists().remove(playerTab);
		}
	}

}
