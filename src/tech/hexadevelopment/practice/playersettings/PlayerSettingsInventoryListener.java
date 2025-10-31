package tech.hexadevelopment.practice.playersettings;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PlayerSettingsInventoryListener implements Listener {


	@EventHandler
	public void onClick(InventoryClickEvent e) {
		PlayerSettingsInventory.settingsInventory.executeClick(e);
	}

}
