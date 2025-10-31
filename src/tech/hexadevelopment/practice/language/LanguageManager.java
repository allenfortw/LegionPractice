package tech.hexadevelopment.practice.language;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.playersettings.PlayerSettings;
import tech.hexadevelopment.practice.preview.Preview;
import tech.hexadevelopment.practice.utils.ItemStackUtils;

public class LanguageManager {

	public static void open(Player p, LegionPractice plugin) {
		int size = plugin.getConfig().getInt("language.inv-size");
		Inventory langs = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("language.title")));
		for(int i = 0; i < 9; i++) {
			langs.setItem(i, ItemStackUtils.createItem(Material.STAINED_GLASS_PANE, " "));
		}
		for(LanguageItem li : plugin.languageItems) {
			langs.setItem(li.getSlot(), li.getItem());
		}
		langs.setItem(4, ItemStackUtils.createItem(Material.getMaterial(plugin.getConfig().getString("language.your-language-item")), ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("language.your-language")).replace("<language>", PlayerSettings.getPlayerSettings(p).getLanguage())));
		langs.setItem(langs.getSize()-1, Preview.backButtonCurrentInv(p));
		p.openInventory(langs);
	}

	public static void openSearched(LegionPractice plugin, Player p,  String search) {
		int size = plugin.getConfig().getInt("language.inv-size");
		Inventory langs = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("language.title")));
		for(int i = 0; i < 9; i++) {
			langs.setItem(i, ItemStackUtils.createItem(Material.STAINED_GLASS_PANE, " "));
		}
		langs.setItem(4, ItemStackUtils.createItem(Material.getMaterial(plugin.getConfig().getString("language.your-language-item")), ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("language.your-language")).replace("<language>", PlayerSettings.getPlayerSettings(p).getLanguage())));
		for(LanguageItem li : plugin.languageItems) {
			if(li.getLanguage().toLowerCase().contains(search.toLowerCase())) {
				langs.addItem(li.getItem());
			}
		}
		langs.setItem(langs.getSize()-1, Preview.backButtonCurrentInv(p));
		p.openInventory(langs);
	}
	
	public static String getDefaultLanguage(Player p) {
		LegionPractice plugin = LegionPractice.getInstance();
		if(p != null && plugin.getConfig().getBoolean("language.automatic-language")){
			String locale = plugin.getNMSAccessProvider().getAccess().getLanguage(p);
			if(locale != null) {
				//will add support for other languages after this has been tested
				if(locale.equals("fi_FI") && plugin.translateMessage(p, "language-set", false, "suomi") != null) {
					return "suomi";
				}
				if(locale.equals("en_CA") || locale.equals("en_GB") || locale.equals("en_AU")
						|| locale.equals("en_US")
						&& (plugin.translateMessage(p, "language-set", false, "english") != null)) {
					return "english";
				}
			}
		}
		return plugin.getConfig().getString("default-language");
	}
}
