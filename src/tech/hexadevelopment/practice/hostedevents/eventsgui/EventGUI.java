package tech.hexadevelopment.practice.hostedevents.eventsgui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import tech.hexadevelopment.practice.utils.ItemStackUtil;
import tech.hexadevelopment.practice.LegionPractice;

public class EventGUI implements CommandExecutor, Listener {

	private LegionPractice plugin;
	private List<EventGUIItem> items = new ArrayList<EventGUIItem>();
	private String title;

	public EventGUI(LegionPractice plugin) {
		this.plugin = plugin;
		this.title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("events-gui.title"));
		if(plugin.getConfig().get("events-gui.items") != null) {
			for(Object o : plugin.getConfig().getList("events-gui.items")) {
				if(o instanceof EventGUIItem) {
					items.add((EventGUIItem) o);
				}
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			Inventory inv = Bukkit.createInventory(null, plugin.getConfig().getInt("events-gui.size"), title);
			for(EventGUIItem item : items) {
				inv.setItem(item.getSlot(), item.getItem());
			}
			p.openInventory(inv);
		}
		return true;
	}


	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getWhoClicked() instanceof Player && e.getCurrentItem() != null && e.getInventory() != null) {
			if(e.getInventory().getTitle().equals(title)) {
				e.setCancelled(true);
				for(EventGUIItem item : items) {
					if(item.getItem().equals(e.getCurrentItem())){
						e.getWhoClicked().closeInventory();
						item.execute((Player) e.getWhoClicked());
					}
				}
			}
		}
	}

}
