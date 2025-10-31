package tech.hexadevelopment.practice.utils;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class FightInventorySending {

	public static void sendGUI(Player p, List<String> names) {
		Inventory inv = Bukkit.createInventory(null, 3, ChatColor.YELLOW + "Fight Inventories:");
		inv.setItem(0, createHead(names.get(0)));
		inv.setItem(2, createHead(names.get(1)));
		p.openInventory(inv);
	}
	
	private static ItemStack createHead(String s) {
		ItemStack is = new ItemStack(Material.SKULL);
		ItemMeta meta = is.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + s);
		is.setItemMeta(meta);
		return is;
	}
}
