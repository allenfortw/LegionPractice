package tech.hexadevelopment.practice.party;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import tech.hexadevelopment.practice.LegionPractice;

public class PartyListener implements Listener {

	
	private LegionPractice plugin;
	
	public PartyListener(LegionPractice plugin) {
		this.plugin = plugin;
	}

	
	@EventHandler(priority=EventPriority.LOW)
	public void onChat(AsyncPlayerChatEvent e) {
		if(e.isCancelled()) return;
		Player p = e.getPlayer();
		Party party = Party.getParty(p);
		if(party == null) return;
		String prefix = null;
		if(!p.hasMetadata(PartyCommand.chat) && !e.getMessage().startsWith(prefix = plugin.getConfig().getString("party.player-chat-prefix"))) return;
		if(prefix != null) {
			e.setMessage(e.getMessage().substring(prefix.length()));
		}
		e.setCancelled(true);
		if(e.getMessage().length() > 0) {
			party.partyMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("party.chat-format")).replace("<player>", p.getName()).replace("<message>", e.getMessage()));
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		Party party = Party.getParty(p);
		if(party != null) {
			for(String s : party.getMembers()) {
				Player mem = Bukkit.getPlayer(s);
				if(mem != null) {
					mem.sendMessage(plugin.translateMessage(mem, "left-party").replace("<player>", p.getName()));
				}
			}
			if(party.isInFight()) p.setHealth(0);
			party.getMembers().remove(p.getName());
			if(party.getOwner().equals(p.getName())) {
				if(party.getMembers().size() == 1) {
					party.disbandParty();
					for(String s : party.getMembers()) {
						Player mem = Bukkit.getPlayer(s);
						if(mem != null) {
							mem.sendMessage(plugin.translateMessage(mem, "party-was-deleted"));
						}
					}
				}
				else {
					party.getMembers().remove(p.getName());
					for(String n : party.getMembers()) {
						party.setOwner(n);
						for(String s : party.getMembers()) {
							Player mem = Bukkit.getPlayer(s);
							if(mem != null) {
								mem.sendMessage(plugin.translateMessage(mem, "party-promoted").replace("<player>", n));
							}
						}
						break;
					}
				}
			}
		}
		p.removeMetadata(plugin.META_IN_PARTY, plugin);
	}
}