package tech.hexadevelopment.practice.fightinventory;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.matchrecorder.RecordedMatch;
import tech.hexadevelopment.practice.matchrecorder.playback.PlaybackFight;
import tech.hexadevelopment.practice.matchrecorder.playback.PlaybackInventoryManager;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.utils.PlayerUtil;

/**
 * Listener class of the FightFightinventory..
 * @author Toppe5
 * @since 0.1
 */
public class FightInventoryListener implements Listener {

	private LegionPractice plugin;
	
	/**
	 * 
	 * @param plugin the LegionPractice plugin.
	 */
	public FightInventoryListener(LegionPractice plugin) {
		this.plugin = plugin;
	}


	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(!(e.getWhoClicked() instanceof Player) || e.getCurrentItem() == null || e.getInventory() == null || e.getInventory().getHolder() == null) return;
		if(e.getInventory().getHolder() instanceof FightInventoryHolder) {
			e.setCancelled(true);
			if(e.getCurrentItem().getType() == Material.PAPER && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
				Player p = (Player) e.getWhoClicked();
				p.closeInventory();
				for(List<RecordedMatch> matches : LegionPractice.getInstance().getRecordedMatchesManager().getRecordedDuels().values()) {
					for(RecordedMatch rm : matches) {
						if(PlaybackInventoryManager.buildItem(rm).equals(e.getCurrentItem())) {
							PlaybackFight fight = rm.startPlayback(false);
							if(fight != null) {
								Party party = Party.getParty(p);
								if(party != null) {
									if(!party.isInFight()) {
										for(String member : party.getMembers()) {
											Player mem = PlayerUtil.getPlayer(member);
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
	}
}
