package tech.hexadevelopment.practice.playerkits.kiteditor;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.utils.SoundManager;

public class KitEditorListener implements Listener{


	private LegionPractice plugin;


	public KitEditorListener(LegionPractice plugin) {
		this.plugin = plugin;
	}


	@EventHandler
	public void onRightClick(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if(KitEditorManager.isEditing(p)) {
			e.setCancelled(true);
		}
		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if(e.getClickedBlock().getType() == Material.ANVIL && plugin.getConfig().getBoolean("kit-editor-anvil")) {
				plugin.getPlayerKitsHandler().openKitEditorSelector(p);
			}
			else if(e.getClickedBlock().getType().toString().contains("DOOR") && KitEditorManager.isEditing(p)) {
				KitEditorManager.leaveEditing(p);
			}
			else if(e.getClickedBlock().getType().toString().contains("CHEST") && KitEditorManager.isEditing(p)) {
				BattleKit kit = KitEditorManager.getKitEditing(p);
				if(kit != null && kit.isChestAccess()) {
					SoundManager.playSound(p, e.getClickedBlock().getLocation(), "CHEST_OPEN", 1, (float) 0.6);
					Inventory inv = Bukkit.createInventory(null, 36);
					ItemStack[] contents = kit.getInv().clone();
					inv.setContents(contents);
					p.openInventory(inv);
				}
			}
			else if (e.getClickedBlock().getState() instanceof Sign && KitEditorManager.isEditing(p)) {
				Sign s = (Sign) e.getClickedBlock().getState();
				if(s.getLines() != null && s.getLines().length > 0) {
					if(s.getLines()[0].toLowerCase().contains(plugin.getConfig().getString("kit-editor-save-sing-line-1").toLowerCase())) {
						KitEditorManager.saveKit(p);
					}
					else if(s.getLines()[0].toLowerCase().contains(plugin.getConfig().getString("kit-editor-leave-line-1").toLowerCase())) {
						KitEditorManager.leaveEditing(p);
					}
					else if(s.getLines()[0].toLowerCase().contains(plugin.getConfig().getString("kit-editor-reset-kit-line-1").toLowerCase())) {
						KitEditorManager.resetKitToDefault(p);
					}
				}
			}
		}
	}


	@EventHandler
	public void onTeleport(PlayerTeleportEvent e) {
		if(e.getFrom() != null && e.getFrom().getWorld() != null && e.getTo() != null
				&& e.getTo().getWorld() != null && KitEditorManager.isEditing(e.getPlayer())) {
			if(!e.getFrom().getWorld().getName().equals(e.getTo().getWorld().getName()) || e.getFrom().distanceSquared(e.getTo()) > 3*3) {
				KitEditorManager.editing.remove(e.getPlayer().getUniqueId());
			}
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(!(e.getWhoClicked() instanceof Player)) return;
		ItemStack item = e.getCurrentItem();
		Player p = (Player) e.getWhoClicked();
		if(e.getClickedInventory() == p.getInventory()) return;
		if(item == null || item.getType() == Material.AIR) return;
		if(e.getInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("kit-editor-title")))) {
			List<BattleKit> kits = BattleKit.getKits(item);
			BattleKit kit = null;
			for(BattleKit bk : kits) {
				if(bk.isEditable()) {
					kit = bk;
					break;
				}
			}
			if(kit != null) {
				KitEditorManager.startEditing(p, kit);
			}
			e.setCancelled(true);
		}
	}
}
