package tech.hexadevelopment.practice.spawnitems;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.playerkits.kiteditor.KitEditorManager;

public class SpawnItemsListener implements Listener{


	private HashMap<UUID, Long> cooldowns = new HashMap<UUID, Long>();
	private static long MIN_DELAY;
	private LegionPractice plugin;

	public SpawnItemsListener(LegionPractice plugin) {
		this.plugin = plugin;
		MIN_DELAY = plugin.getConfig().getLong("spawnitem-cooldown");
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(e.getItem() != null && e.getItem().getType() != Material.AIR && e.getItem().hasItemMeta()
				&& e.getItem().getItemMeta().hasDisplayName()) {
			if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if(!Fight.isInFight(e.getPlayer(), plugin) && !PvPEvent.isInEvent(e.getPlayer())) {
					Player p = e.getPlayer();
					UUID uuid = p.getUniqueId();
					if(cooldowns.containsKey(uuid) && cooldowns.get(uuid)+MIN_DELAY > System.currentTimeMillis()) {
						p.sendMessage(plugin.translateMessage(p, "do-not-spam-things"));
						e.setCancelled(true);
					}
					else {
						for(SpawnItem si : plugin.spawnItems) {
							if(si.getItem() != null) {
								ItemStack item = si.getItem().clone();
								item.setDurability(e.getItem().getDurability());
								if(si.getCommand() != null && item.equals(e.getItem())) {
									cooldowns.put(uuid, System.currentTimeMillis());
									p.chat(si.getCommand());
									e.setCancelled(true);
									new BukkitRunnable() {
										
										@Override
										public void run() {
											if(p != null) {
												p.updateInventory();
											}
										}
									}.runTaskLaterAsynchronously(plugin, 1);
									return;
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onClick(InventoryClickEvent e) {
		ItemStack is = e.getCurrentItem();
		Player p = (Player) e.getWhoClicked();
		if(p.getGameMode() != GameMode.CREATIVE && Fight.getCurrentFight(p, LegionPractice.getInstance()) == null
				&& !PvPEvent.isInEvent(p) && !KitEditorManager.isEditing(p)) {
			if(e.getClick() == ClickType.NUMBER_KEY) {
				is = e.getWhoClicked().getInventory().getItem(e.getHotbarButton());
				for(SpawnItem si : LegionPractice.getInstance().spawnItems) {
					if(si != null && si.getItem() != null && si.getSlot() == e.getHotbarButton()
							&& is != null && is.equals(si.getItem())) {
						e.setCancelled(true);
						return;
					}
				}
			}
			else { 
				for(SpawnItem si : LegionPractice.getInstance().spawnItems) {
					if(si != null && si.getItem() != null && si.getItem().equals(is)
							&& si.getSlot() == e.getSlot()) {
						e.setCancelled(true);
						return;
					}
				}
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		cooldowns.remove(e.getPlayer().getUniqueId());
	}
}
