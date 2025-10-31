package tech.hexadevelopment.practice.fights.party.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKitType;
import tech.hexadevelopment.practice.fights.party.holders.PartyFFAHolder;
import tech.hexadevelopment.practice.fights.party.holders.PartySplitHolder;
import tech.hexadevelopment.practice.fights.party.holders.PartyVsBotsHolder;
import tech.hexadevelopment.practice.matchrecorder.playback.PlaybackInventoryManager;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;

public class PartyActionsManager {

	private static PartyActionsManager partyActionsManager = new PartyActionsManager();

	private HashSet<PartyAction> partyActions = new HashSet<PartyAction>();


	public void loadIcons(LegionPractice plugin) {
		Material mat = Material.getMaterial(plugin.getConfig().getString("party.ffa-item"));
		ItemStack ffa = new ItemStack(mat == null ? Material.DIAMOND : mat);
		ItemMeta meta = ffa.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("party.ffa-item-name")));
		List<String> lore = new ArrayList<String>();
		for(String s : plugin.getConfig().getStringList("party.ffa-item-lore")) {
			lore.add(ChatColor.translateAlternateColorCodes('&', s));
		}
		meta.setLore(lore);
		ffa.setItemMeta(meta);
		int slot = plugin.getConfig().getInt("party.ffa-item-slot");
		partyActions.add(new PartyAction("partyffa") {

			@Override
			public boolean start(Party party) {
				Player p = Bukkit.getPlayer(party.getOwner());
				if(p != null) {
					if(party.getMembers().size() == 1) {
						p.sendMessage(plugin.translateMessage(p, "not-enough-players"));
						return false;
					}
					plugin.arenaPvP.openKitSelector(p, new PartyFFAHolder(party), BattleKitType.PARTY_FFA);
				}
				return true;
			}

			@Override
			public int getSlot() {
				return slot;
			}

			@Override
			public ItemStack getIcon() {
				return plugin.getConfig().getBoolean("party.party-ffa-enabled") ? ffa : new ItemStack(Material.AIR);
			}
		});
		Material mat2 = Material.getMaterial(plugin.getConfig().getString("party.split-item"));
		ItemStack split = new ItemStack(mat2 == null ? Material.DIAMOND : mat2);
		ItemMeta meta2 = split.getItemMeta();
		meta2.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("party.split-item-name")));
		List<String> lore2 = new ArrayList<String>();
		for(String s : plugin.getConfig().getStringList("party.split-item-lore")) {
			lore2.add(ChatColor.translateAlternateColorCodes('&', s));
		}
		meta2.setLore(lore2);
		split.setItemMeta(meta2);
		int slot2 = plugin.getConfig().getInt("party.split-item-slot");
		partyActions.add(new PartyAction("partysplit") {

			@Override
			public boolean start(Party party) {
				Player p = Bukkit.getPlayer(party.getOwner());
				if(p != null) {
					if(party.getMembers().size() == 1) {
						p.sendMessage(plugin.translateMessage(p, "not-enough-players"));
						return false;
					}
					plugin.arenaPvP.openKitSelector(p, new PartySplitHolder(party), BattleKitType.PARTY_SPLIT);
				}
				return true;
			}

			@Override
			public int getSlot() {
				return slot2;
			}

			@Override
			public ItemStack getIcon() {
				return plugin.getConfig().getBoolean("party.party-split-enabled") ? split : new ItemStack(Material.AIR);
			}
		});
		if(plugin.citizens) {
			Material mat3 = Material.getMaterial(plugin.getConfig().getString("party.party-playback-item"));
			ItemStack playback = new ItemStack(mat3 == null ? Material.DIAMOND : mat3);
			ItemMeta meta3 = playback.getItemMeta();
			meta3.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("party.party-playback-item-name")));
			List<String> lore3 = new ArrayList<String>();
			for(String s : plugin.getConfig().getStringList("party.party-playback-item-lore")) {
				lore3.add(ChatColor.translateAlternateColorCodes('&', s));
			}
			meta3.setLore(lore3);
			playback.setItemMeta(meta3);
			int slot3 = plugin.getConfig().getInt("party.party-playback-item-slot");
			partyActions.add(new PartyAction("partyplayback") {

				@Override
				public boolean start(Party party) {
					Player p = Bukkit.getPlayer(party.getOwner());
					if(p != null) {
						if(!PermissionsManager.hasPermission(p, Permission.PARTY_PLAYBACK)) {
							p.sendMessage(plugin.translateMessage(p, "no-permission"));
							return true;
						}
						PlaybackInventoryManager.openGUI(p, 0);
					}
					return true;
				}

				@Override
				public int getSlot() {
					return slot3;
				}

				@Override
				public ItemStack getIcon() {
					return plugin.getConfig().getBoolean("party.party-playback-enabled") ? playback : new ItemStack(Material.AIR);
				}
			});
			Material mat4 = Material.getMaterial(plugin.getConfig().getString("party.party-vs-bots-item"));
			ItemStack bot = new ItemStack(mat4 == null ? Material.SKULL_ITEM : mat4);
			ItemMeta meta4 = bot.getItemMeta();
			meta4.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("party.party-vs-bots-item-name")));
			List<String> lore4 = new ArrayList<String>();
			for(String s : plugin.getConfig().getStringList("party.party-vs-bots-item-lore")) {
				lore4.add(ChatColor.translateAlternateColorCodes('&', s));
			}
			meta4.setLore(lore4);
			bot.setItemMeta(meta4);
			int slot4 = plugin.getConfig().getInt("party.party-vs-bots-item-slot");
			partyActions.add(new PartyAction("partybots") {

				@Override
				public boolean start(Party party) {
					Player p = Bukkit.getPlayer(party.getOwner());
					if(p != null) {
						if(!PermissionsManager.hasPermission(p, Permission.PARTY_BOTS)) {
							p.sendMessage(plugin.translateMessage(p, "no-permission"));
							return true;
						}
						plugin.arenaPvP.openKitSelector(p, new PartyVsBotsHolder(party), BattleKitType.BOT_FIGHT);
					}
					return true;
				}

				@Override
				public int getSlot() {
					return slot4;
				}

				@Override
				public ItemStack getIcon() {
					return plugin.getConfig().getBoolean("party.party-vs-bots-enabled") ? bot : new ItemStack(Material.AIR);
				}
			});
		}
	}

	public PartyAction byName(String name) {
		for(PartyAction pa : partyActions) {
			if(pa.getName().equalsIgnoreCase(name)) {
				return pa;
			}
		}
		return null;
	}

	public PartyAction byIcon(ItemStack icon) {
		if(icon == null || icon.getType() == Material.AIR) return null;
		for(PartyAction pa : partyActions) {
			if(pa.getIcon().equals(icon)) {
				return pa;
			}
		}
		return null;
	}

	public HashSet<PartyAction> getPartyActions() {
		return partyActions;
	}

	public static PartyActionsManager getPartyActionsManager() {
		return partyActionsManager;
	}
}
