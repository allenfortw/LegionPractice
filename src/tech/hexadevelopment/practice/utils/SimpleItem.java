package tech.hexadevelopment.practice.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class SimpleItem {
	
	private boolean enchanted;
	private Material material;
	private short durability;
	
	public SimpleItem(Material material, short durability) {
		this.material = material;
		this.durability = durability;
	}
	
	public SimpleItem(ItemStack is) {
		if(is == null) is = new ItemStack(Material.AIR);
		this.material = is.getType();
		this.durability = is.getDurability();
		this.enchanted = !is.getEnchantments().isEmpty();
	}

	public SimpleItem(Material material, boolean enchanted, short durability) {
		this.material = material;
		this.enchanted = enchanted;
		this.durability = durability;
	}

	public ItemStack toItemStack() {
		ItemStack is = new ItemStack(material);
		is.setDurability(durability);
		if(enchanted) {
			is.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
		}
		return is;
	}
	
	public Material getMaterial() {
		return material;
	}
	
	public boolean isEnchanted() {
		return enchanted;
	}
	
	public short getDurability() {
		return durability;
	}

	public boolean isSimilar(SimpleItem item) {
		return item != null && material.equals(item.getMaterial()) && (((enchanted && item.isEnchanted()) || (!enchanted && !item.isEnchanted())));
	}
}
