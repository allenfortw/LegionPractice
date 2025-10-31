package tech.hexadevelopment.practice.spectator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.events.DuelEndEvent;
import tech.hexadevelopment.practice.events.FightEndEvent;
import tech.hexadevelopment.practice.events.PartyFFAEndEvent;
import tech.hexadevelopment.practice.events.PartySplitEndEvent;
import tech.hexadevelopment.practice.events.PartyVsPartyEndEvent;

public class SpectatorFightsListener implements Listener {

	
	private LegionPractice plugin;

	public SpectatorFightsListener(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onDuelEnd(DuelEndEvent e) {
		handleFightEnd(e);
	}

	@EventHandler
	public void onPartyFFAEnd(PartyFFAEndEvent e) {
		handleFightEnd(e);
	}

	@EventHandler
	public void onPartySplitEnd(PartySplitEndEvent e) {
		handleFightEnd(e);
	}

	@EventHandler
	public void onPartyVsPartyEnd(PartyVsPartyEndEvent e) {
		handleFightEnd(e);
	}

	void handleFightEnd(FightEndEvent e) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				for(Player p : Bukkit.getOnlinePlayers()) {
					if(plugin.getSpectatorHandler().isSpectator(p) && plugin.getSpectatorHandler().getSpectatingFight().containsKey(p.getUniqueId())) {
						if(e.getFight().equals(plugin.getSpectatorHandler().getSpectatingFight().get(p.getUniqueId()))) {
							plugin.getSpectatorHandler().removeSpectator(p, true);
						}
					}
				}
			}
		}, 20*plugin.getConfig().getInt("wait-before-teleport")+1);
	}

}
