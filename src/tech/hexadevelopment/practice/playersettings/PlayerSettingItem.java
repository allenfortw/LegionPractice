package tech.hexadevelopment.practice.playersettings;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface PlayerSettingItem {

	public void onClick(Player p);
	
	public ItemStack getItem(Player p);
	
	public int getSlot();
	
	public boolean adminOnly();
}
