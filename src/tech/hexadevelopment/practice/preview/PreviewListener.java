package tech.hexadevelopment.practice.preview;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;

public class PreviewListener implements Listener {

	public static String backInventory = "ToppeBattlesBackButtonInventory";
	private LegionPractice plugin;
	public PreviewListener(LegionPractice plugin) {	
		this.plugin = plugin;
	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		ItemStack item = e.getCurrentItem();
		if(!(e.getWhoClicked() instanceof Player)) return;
		Player p = (Player) e.getWhoClicked();
		if(e.getInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("preview.title")))) {
			e.setCancelled(true);
		}
		if(item == null || item.getType() == Material.AIR) return;
		if(item.getType() == Material.WOOL && item.getDurability() == 14) {
			if(item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("back-button-name")))) {
				if(p.hasMetadata(backInventory)) {
					MetadataValue m = plugin.getMetadata(p, backInventory);
					if(m != null && m.value() != null && m.value() instanceof Inventory) {
						Inventory inv = (Inventory) m.value();
						if(inv.getType() == InventoryType.CHEST) {
							p.openInventory(inv);
							p.removeMetadata(backInventory, plugin);
							return;
						}
					}
				}
				p.removeMetadata(backInventory, plugin);
				p.closeInventory();
			}
		}
		if(e.getInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("preview.select-kit-title")))) {
			e.setCancelled(true);
			List<BattleKit> kits = BattleKit.getKits(e.getCurrentItem());
			BattleKit kit = null;
			for(BattleKit bk : kits) {
				if(bk.getMergedEditor() == null) {
					kit = bk;
					break;
				}
			}
			BattleKit c = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
			if(c != null && c.getIcon() != null) {
				if(e.getCurrentItem().equals(c.getIcon())) {
					kit = c;
				}
			}
			if(kit == null) {
				((Player) e.getWhoClicked()).sendMessage(ChatColor.RED + "Error: Invalid kit, please try another kit.");
			}
			else Preview.preview((Player) e.getWhoClicked(), kit, plugin);
		}
	}
}
