package tech.hexadevelopment.practice.language;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.playersettings.PlayerSettings;

/**
 * @author Toppe5
 * @since 0.1
 */
public class LanguageListener implements Listener {
	
	public static String search = "ToppeBattlesLanguageSearch";
	
	private LegionPractice plugin;
	public LanguageListener(LegionPractice plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(!(e.getWhoClicked() instanceof Player)) return;
		if(e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
		Player p = (Player) e.getWhoClicked();
		ItemStack item = e.getCurrentItem();
		if(e.getClickedInventory().getTitle().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("language.title")))) {
			e.setCancelled(true);
			for(LanguageItem li : plugin.languageItems) {
				if(li.getItem().equals(item)) {
					PlayerSettings.getPlayerSettings(p.getUniqueId()).setLanguage(li.getLanguage(), plugin, true);
					p.closeInventory();
					break;
				}
			}
		}
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		if(!e.getPlayer().hasMetadata(search)) return;
		e.setCancelled(true);
		e.getPlayer().removeMetadata(search, plugin);
		LanguageManager.openSearched(plugin, e.getPlayer(), e.getMessage());
	}
	
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		e.getPlayer().removeMetadata(search, plugin);
	}
}
