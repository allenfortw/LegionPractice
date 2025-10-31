package tech.hexadevelopment.practice.party;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.utils.ItemStackUtils;

public class PartySettings {

	private boolean publicParty;
	private boolean openParty;
	private int maxPlayerLimit;
	private Inventory inv;

	public PartySettings() {
		setMaxPlayerLimit(LegionPractice.getInstance().getConfig().getInt("max-party-members"));
	}

	/**
	 * @return the publicParty
	 */
	public boolean isPublicParty() {
		return publicParty;
	}
	/**
	 * @param publicParty the publicParty to set
	 */
	public void setPublicParty(boolean publicParty) {
		this.publicParty = publicParty;
	}
	/**
	 * @return the maxPlayerLimit
	 */
	public int getMaxPlayerLimit() {
		return maxPlayerLimit;
	}
	/**
	 * @param maxPlayerLimit the maxPlayerLimit to set
	 */
	public void setMaxPlayerLimit(int maxPlayerLimit) {
		this.maxPlayerLimit = maxPlayerLimit;
	}

	public boolean isOpenParty() {
		return openParty;
	}

	public void setOpenParty(boolean openParty) {
		this.openParty = openParty;
	}

	public void updateMenu(){
		LegionPractice plugin = LegionPractice.getInstance();
		inv.setItem(11, ItemStackUtils.createItem(Material.SKULL_ITEM, ChatColor.translateAlternateColorCodes('&',
				plugin.getConfig().getString("party.settings.player-limit").replace("<value>", getMaxPlayerLimit() + "")), (byte) 3));
		if(!publicParty) {
			String name = isOpenParty() ? plugin.getConfig().getString("party.settings.open-party.enabled") : plugin.getConfig().getString("party.settings.open-party.disabled");
			inv.setItem(13, ItemStackUtils.createItem(isOpenParty() ? Material.ENDER_CHEST : Material.CHEST, ChatColor.translateAlternateColorCodes('&', name)));
		}
		else {
			inv.setItem(13, new ItemStack(Material.AIR));
		}
		String name = isPublicParty() ? plugin.getConfig().getString("party.settings.public-party.enabled") : plugin.getConfig().getString("party.settings.public-party.disabled");
		inv.setItem(15, ItemStackUtils.createItem(isPublicParty() ? Material.ENCHANTED_BOOK : Material.BOOK,  ChatColor.translateAlternateColorCodes('&', name)));
	}
	
	public Inventory getSettingsMenu() {
		if(inv == null) {
			inv = Bukkit.createInventory(null, 27,
					ChatColor.translateAlternateColorCodes('&',
							LegionPractice.getInstance().getConfig().getString("party-settings-title")));
			updateMenu();
		}
		return inv;
	}

}
