package tech.hexadevelopment.practice.party;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.utils.ClickableMessage;

public class PartySettingsListener implements Listener{

	private LegionPractice plugin;

	public PartySettingsListener(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(!(e.getWhoClicked() instanceof Player)) return;
		Player p = (Player) e.getWhoClicked();
		if(e.getInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("party-settings-title")))) {
			if(e.getClickedInventory() != null && e.getClickedInventory().equals(p.getInventory())) return;
			if(e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
				e.setCancelled(true); 
				ItemStack item = e.getCurrentItem();
				Party party = Party.getParty(p);
				if(party == null || !party.getOwner().equals(p.getName())) {
					p.closeInventory();
					return;
				}
				if(item.getType() == Material.ENCHANTED_BOOK || item.getType() == Material.BOOK) {
					if(!PermissionsManager.hasPermission(p, Permission.PUBLIC_PARTY)) {
						p.sendMessage(plugin.translateMessage(p, "public-party-no-permission"));
						p.closeInventory();
						return;
					}
					party.getSettings().setPublicParty(!party.getSettings().isPublicParty());
					party.getSettings().updateMenu();
					if(!party.getSettings().isPublicParty()) {
						p.sendMessage(plugin.translateMessage(p, "public-party-off"));
						if(party.getBroadcastTask() != null) {
							party.getBroadcastTask().cancel();
							party.setBroadcastTask(null);
						}
						return;
					}
					p.sendMessage(plugin.translateMessage(p, "public-party-on"));
					party.setBroadcastTask(LegionPractice.ASYNC_EVERYTHING ? new BukkitRunnable() {

						int start = plugin.getConfig().getInt("public-party-broadcast-time");
						int counter = start;

						@Override
						public void run() {
							counter++;
							Party party = Party.getParty(p);
							if(party != null && party.getSettings().isPublicParty() && p != null && party.getOwner().equals(p.getName())) {
								if(counter > start) {
									counter = 0;
									if(plugin.getConfig().getBoolean("clickable-messages")) {
										for(Player pl : Bukkit.getOnlinePlayers()) {
											if(pl.isOnline() && Bukkit.getPlayer(pl.getUniqueId()) != null && canJoinPublicParty(pl) && isNotInSameParty(pl, party)) {
												String message = plugin.translateMessage(pl, "public-party-broadcast").replace("<player>", p.getName()); 
												ClickableMessage.sendMessage(pl, message, "/LegionPractice:party join " + p.getName());
											}
										}
									}
									else {
										for(Player pl : Bukkit.getOnlinePlayers()) {
											if(canJoinPublicParty(pl)) {
												pl.sendMessage(plugin.translateMessage(pl, "public-party-broadcast").replace("<player>", p.getName()));
											}
										}
									}
								}
							}
							else this.cancel();
						}
					}.runTaskTimerAsynchronously(plugin, 20, 20)
					: new BukkitRunnable() {

						int start = plugin.getConfig().getInt("public-party-broadcast-time");
						int counter = start;

						@Override
						public void run() {
							counter++;
							Party party = Party.getParty(p);
							if(party != null && party.getSettings().isPublicParty() && p != null && party.getOwner().equals(p.getName())) {
								if(counter > start) {
									counter = 0;
									if(plugin.getConfig().getBoolean("clickable-messages")) {
										for(Player pl : Bukkit.getOnlinePlayers()) {
											if(pl.isOnline() && Bukkit.getPlayer(pl.getUniqueId()) != null && canJoinPublicParty(pl) && isNotInSameParty(pl, party)) {
												String message = plugin.translateMessage(pl, "public-party-broadcast").replace("<player>", p.getName()); 
												ClickableMessage.sendMessage(pl, message, "/LegionPractice:party join " + p.getName());
											}
										}
									}
									else {
										for(Player pl : Bukkit.getOnlinePlayers()) {
											if(canJoinPublicParty(pl)) {
												pl.sendMessage(plugin.translateMessage(pl, "public-party-broadcast").replace("<player>", p.getName()));
											}
										}
									}
								}
							}
							else this.cancel();
						}
					}.runTaskTimer(plugin, 20, 20));
					//TODO async?
					return;
				}
				else if(item.getType() == Material.CHEST || item.getType() == Material.ENDER_CHEST) {
					if(!PermissionsManager.hasPermission(p, Permission.OPEN_PARTY)) {
						p.sendMessage(plugin.translateMessage(p, "no-permission"));
					}
					else {
						party.getSettings().setOpenParty(!party.getSettings().isOpenParty());
						party.getSettings().updateMenu();
					}
				}
				else if(item.getType() == Material.SKULL_ITEM) {
					if(!PermissionsManager.hasPermission(p, Permission.PARTY_LIMIT_BYPASS)) {
						p.sendMessage(plugin.translateMessage(p, "no-party-limit-bypass"));
						p.closeInventory();
						return;
					}
					if(e.getClick() == ClickType.LEFT || e.getClick() == ClickType.SHIFT_LEFT) {
						if(p.hasPermission("*") || !p.hasPermission("LegionPractice.partylimit.maxvalue." + party.getSettings().getMaxPlayerLimit())) {
							party.getSettings().setMaxPlayerLimit(party.getSettings().getMaxPlayerLimit()+1);
							party.getSettings().updateMenu();
						}
					}
					else if(e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.SHIFT_RIGHT) {
						if(party.getSettings().getMaxPlayerLimit() > 1) {
							party.getSettings().setMaxPlayerLimit(party.getSettings().getMaxPlayerLimit()-1);
							party.getSettings().updateMenu();
						}
					}
				}
			}
		}
	}

	private boolean isNotInSameParty(Player p, Party party) {
		Party party2 = Party.getParty(p);
		return party2 == null || !party2.getOwner().equals(party.getOwner());
	}

	private boolean canJoinPublicParty(Player p) {
		return !PvPEvent.isInEvent(p) && Fight.getCurrentFight(p, plugin) == null;
	}
}
