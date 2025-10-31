package tech.hexadevelopment.practice.matchrecorder.playback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.matchrecorder.RecordedMatch;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.utils.signgui.SignFinishCallback;
import tech.hexadevelopment.practice.utils.signgui.SignGUI;

public class PlaybackInventoryListener implements Listener{


	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getWhoClicked() instanceof Player && e.getCurrentItem() != null && e.getClickedInventory() != null) {
			String title = ChatColor.translateAlternateColorCodes('&',LegionPractice.getInstance().getConfig().getString("playback-gui-title"));
			if(e.getClickedInventory().getSize() == 45 && e.getClickedInventory().getTitle().startsWith(title)) {
				Player p = (Player) e.getWhoClicked();
				e.setCancelled(true);
				if(e.getCurrentItem().getType() == Material.COMPASS && e.getSlot() == 4) {
					SignGUI.openSignEditor(p, new String[] {"", "", "", "Search here"}, new SignFinishCallback() {

						@Override
						public void onFinish(String[] lines) {
							HashSet<RecordedMatch> searchResults = new HashSet<RecordedMatch>();
							String matchInfo = null;
							outer: for(List<RecordedMatch> matches : LegionPractice.getInstance().getRecordedMatchesManager().getRecordedDuels().values()) {
								for(RecordedMatch match : matches) {
									matchInfo = (match.getArenaName() + match.getKit().getName()
											+ match.getKit().getFancyName() + match.getKit().getMergedEditor()
											+ match.getPlayers().values().toString() + match.getDateFormat() + match.getUUID()).replace(" ", "").toLowerCase();
									if((lines[0] + lines[1] + lines[2]).length() > 0 || (lines[0] + lines[1] + lines[2]).equalsIgnoreCase("all")) {
										for(String s : (lines[0] + lines[1] + lines[2]).toLowerCase().split(" ")) {
											if(matchInfo.contains(s)) {
												searchResults.add(match);
												continue outer;
											}
										}
									}
									else {
										searchResults.add(match); 
										continue outer;
									}
								}
							}
							PlaybackInventoryManager.openGUI(p, 0, new ArrayList<RecordedMatch>(searchResults));
						}
					});
					return;
				}
				if(e.getCurrentItem().getType() == Material.ARROW) {
					if(e.getSlot() == 26) {
						PlaybackInventoryManager.openGUI(p, Integer.parseInt(e.getInventory().getTitle().replace(title + " | Page: ", ""))+1);
					}
					else if(e.getSlot() == 18) {
						PlaybackInventoryManager.openGUI(p, Integer.parseInt(e.getInventory().getTitle().replace(title + " | Page: ", ""))-1);
					}
				}
				for(RecordedMatch rm : PlaybackInventoryManager.viewing.getOrDefault(p.getUniqueId(), LegionPractice.getInstance().getRecordedMatchesManager().getRecordedDuels().getOrDefault(p.getUniqueId(), new ArrayList<RecordedMatch>()))) {
					if(PlaybackInventoryManager.buildItem(rm).equals(e.getCurrentItem())) {
						PlaybackFight fight = rm.startPlayback(false);
						if(fight != null) {
							Party party = Party.getParty(p);
							if(party != null) {
								if(!party.isInFight()) {
									for(String member : party.getMembers()) {
										Player mem = Bukkit.getPlayer(member);
										if(mem != null) {
											fight.addSpectator(mem);
										}
									}
								}
								else p.closeInventory();
							}
							else {
								fight.addSpectator(p);
							}
						}
						else {
							p.sendMessage(LegionPractice.getInstance().translateMessage(p, "playback-can-not-start"));
						}
						return;
					}
				}
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		PlaybackInventoryManager.viewing.remove(e.getPlayer());
	}
}