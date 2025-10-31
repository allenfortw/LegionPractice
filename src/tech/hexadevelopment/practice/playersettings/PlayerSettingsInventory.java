package tech.hexadevelopment.practice.playersettings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.duel.DuelRequestSender;
import tech.hexadevelopment.practice.language.LanguageManager;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.utils.ItemStackUtil;
import tech.hexadevelopment.practice.utils.ItemStackUtils;
import tech.hexadevelopment.practice.utils.SoundManager;

public class PlayerSettingsInventory {

	public static PlayerSettingsInventory settingsInventory;
	private static HashSet<PlayerSettingItem> items = new HashSet<PlayerSettingItem>();
	private HashMap<UUID, Long> lastClick = new HashMap<UUID, Long>();

	private String title;
			
	public PlayerSettingsInventory(LegionPractice plugin) {
		this.title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("player-settings.title"));
		int slot = plugin.getConfig().getInt("player-settings.item-slot-addition");
		items.add(new PlayerSettingItem() {

			@Override
			public void onClick(Player p) {
				PlayerSettings.getPlayerSettings(p).setAdminScoreboard(!PlayerSettings.getPlayerSettings(p).isAdminScoreboard());
			}

			@Override
			public int getSlot() {
				return LegionPractice.getInstance().getConfig().getInt("player-settings.inventory-size")-1;
			}
			@Override
			public ItemStack getItem(Player p) {
				ItemStack item = ItemStackUtils.createItem(Material.PAPER, ChatColor.RED + "Admin Scoreboard", (byte)0,  p == null ? Arrays.asList("") : getLore(PlayerSettings.getPlayerSettings(p).isAdminScoreboard()));
				return item;
			}

			@Override
			public boolean adminOnly() {
				return true;
			}
		});
		if(plugin.getPlayerHider() != null) {
			items.add(new PlayerSettingItem() {

				@Override
				public void onClick(Player p) {
					PlayerSettings.getPlayerSettings(p).setHideOtherPlayers(!PlayerSettings.getPlayerSettings(p).isHideOtherPlayers());
					plugin.getPlayerHider().handleHide(p);
				}

				@Override
				public int getSlot() {	
					return slot+6;
				}

				@Override
				public ItemStack getItem(Player p) {
					ItemStack item = ItemStackUtils.createItem(Material.getMaterial(plugin.getConfig().getString("player-settings.hide-players-item")),
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("player-settings.hide-players-name")), (byte)0,  p == null ? Arrays.asList("") : getLore(PlayerSettings.getPlayerSettings(p).isHideOtherPlayers()));
					return item;
				}

				@Override
				public boolean adminOnly() {
					return false;
				}
			});
		}
		items.add(new PlayerSettingItem() {

			@Override
			public void onClick(Player p) {
				LanguageManager.open(p, plugin);
			}

			@Override
			public int getSlot() {
				return slot+4;
			}

			ItemStack item = ItemStackUtil.createItem(Material.getMaterial(plugin.getConfig().getString("player-settings.language-item")),
					ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("player-settings.language-name")));
			@Override
			public ItemStack getItem(Player p) {
				return item;
			}

			@Override
			public boolean adminOnly() {
				return false;
			}
		});
		items.add(new PlayerSettingItem() {

			@Override
			public void onClick(Player p) {
				DuelRequestSender.openInventory(p, plugin);
			}

			@Override
			public int getSlot() {
				return slot+2;
			}

			ItemStack item = ItemStackUtil.createItem(Material.getMaterial(plugin.getConfig().getString("player-settings.duel-requests-item")),
					ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("player-settings.duel-requests-name")));
			@Override
			public ItemStack getItem(Player p) {
				return item;
			}

			@Override
			public boolean adminOnly() {
				return false;
			}
		});
		items.add(new PlayerSettingItem() {

			@Override
			public void onClick(Player p) {
				PlayerSettings.getPlayerSettings(p).setScoreboardDisabled(!PlayerSettings.getPlayerSettings(p).isScoreboardDisabled());
			}

			@Override
			public int getSlot() {
				return slot+0;
			}

			@Override
			public ItemStack getItem(Player p) {
				ItemStack item = ItemStackUtils.createItem(Material.getMaterial(plugin.getConfig().getString("player-settings.scoreboard-item")),
						ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("player-settings.scoreboard-name")), (byte) 0, p == null ? Arrays.asList("") : getLore(!PlayerSettings.getPlayerSettings(p).isScoreboardDisabled()));
				return item;
			}

			@Override
			public boolean adminOnly() {
				return false;
			}
		});
	}
	
	private List<String> getLore(boolean b) {
		String p1 = b ? ChatColor.GREEN + "" + ChatColor.BOLD + "»Enabled" : ChatColor.GREEN + "Enabled";
		String p2 = !b ? ChatColor.RED + "" + ChatColor.BOLD + "»Disable" : ChatColor.RED + "Disabled";
		return Arrays.asList(p1, p2);
	}

	public String getTitle() {
		return title;
	}

	public int getOwnSlot(PlayerSettingItem setting) {
		int counter = 0;
		for(PlayerSettingItem s : items) {
			if(s.equals(setting)) {
				return counter;
			}
			counter++;
		}
		return 0;
	}

	public boolean executeClick(InventoryClickEvent e) {
		if(e.getWhoClicked() instanceof Player && e.getCurrentItem() != null
				&& e.getCurrentItem().getType() != Material.AIR) {
			if(e.getInventory().getTitle().equals(getTitle())) {
				long l = lastClick.getOrDefault(e.getWhoClicked().getUniqueId(), (long) 0);
				e.setCancelled(true);
				if(System.currentTimeMillis()-l > 300) {
					PlayerSettingItem setting = getSettingByItem(e.getCurrentItem().getType(), e.getSlot());
					if(setting != null) {
						lastClick.put(e.getWhoClicked().getUniqueId(), System.currentTimeMillis());
						SoundManager.playSound((Player) e.getWhoClicked(), e.getWhoClicked().getLocation(), "CHICKEN_EGG_POP", 1, (float) 0.7);
						setting.onClick((Player) e.getWhoClicked());
						e.getInventory().setItem(e.getSlot(), setting.getItem((Player) e.getWhoClicked()));
						return true;
					}
				}
			}
		}
		return false;
	}


	public void openInventory(Player p) {
		Inventory inv = Bukkit.createInventory(null, getSize(), title);
		for(PlayerSettingItem item : items) {
			if(item.getSlot() < 54) {
				if(item.adminOnly() && !PermissionsManager.hasPermission(p, Permission.ADMIN)) continue;
				inv.setItem(item.getSlot(), item.getItem(p));
			}
		}
		p.openInventory(inv);
	}

	private int getSize() {
		int size = 9;
		for(PlayerSettingItem item : items) {
			if(item.getSlot() > size) {
				if(item.getSlot() >= 9) size = 18;
				if(item.getSlot() >= 18) size = 27;
				if(item.getSlot() >= 27) size = 36;
				if(item.getSlot() >= 36) size = 45;
				if(item.getSlot() >= 45) size = 54;
			}
		}
		int configSize = LegionPractice.getInstance().getConfig().getInt("player-settings.inventory-size");
		return size > configSize ? size : configSize;
	}

	private PlayerSettingItem getSettingByItem(Material mat, int slot) {
		for(PlayerSettingItem i : items) {
			if(i.getItem(null).getType() == mat && slot == i.getSlot()) {
				return i;
			}
		}
		return null;
	}

}
