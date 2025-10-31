package tech.hexadevelopment.practice.utils;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackUtils {

	public static ItemStack createItem(Material material, String name) {
		ItemStack is = new ItemStack(material);
		ItemMeta itemMeta = is.getItemMeta();
		itemMeta.setDisplayName(name);
		is.setItemMeta(itemMeta);
		return is;
	}

	public static ItemStack createItem(Material material, String name, byte dur) {
		ItemStack is = new ItemStack(material);
		ItemMeta itemMeta = is.getItemMeta();
		itemMeta.setDisplayName(name);
		is.setItemMeta(itemMeta);
		is.setDurability(dur);
		return is;
	}
	
	public static ItemStack createItem(Material material, String name, byte dur, String lore) {
		ItemStack is = new ItemStack(material);
		ItemMeta itemMeta = is.getItemMeta();
		itemMeta.setDisplayName(name);
		itemMeta.setLore(Arrays.asList(lore));
		is.setItemMeta(itemMeta);
		is.setDurability(dur);
		return is;
	}
	
	public static ItemStack createItem(Material material, String name, byte dur, List<String> lore) {
		ItemStack is = new ItemStack(material);
		ItemMeta itemMeta = is.getItemMeta();
		itemMeta.setDisplayName(name);
		itemMeta.setLore(lore);
		is.setItemMeta(itemMeta);
		is.setDurability(dur);
		return is;
	}
}
