package tech.hexadevelopment.practice.playerkits.kiteditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.playerkits.PlayerKits;
import tech.hexadevelopment.practice.utils.ItemStackUtil;
import tech.hexadevelopment.practice.utils.SerializableLocation;

public class KitEditorManager {

	public static HashMap<UUID, BattleKit> editing = new HashMap<UUID, BattleKit>();

	public static void startEditing(Player p, BattleKit kit) {
		if(!kit.isEditable()) return;
		if(LegionPractice.getInstance().getConfig().get("editing-place") == null) return;
		SerializableLocation ser = SerializableLocation.fromString(LegionPractice.getInstance().getConfig().getString("editing-place"));
		p.teleport(ser.toLocation());
		LegionPractice.getInstance().clear(p, false, false);
		BattleKit ownKit = LegionPractice.getInstance().getPlayerKitsHandler().getPlayerKits(p).getEditedKit(kit, true);
		if(ownKit == null) {
			ownKit = new BattleKit(kit.getName());
			ownKit.setInventory(new ArrayList<ItemStack>(kit.getInventory()));
			for(ItemStack is : ownKit.getInventory()) {
				if(is != null && is.getAmount() < 1) {
					is.setAmount(1);
				}
			}
			ownKit.setHelmet(kit.getHelmet() == null ? null : kit.getHelmet().clone());
			if(ownKit.getHelmet() != null && ownKit.getHelmet().getAmount() < 1) {
				ownKit.getHelmet().setAmount(1);
			}
			ownKit.setChestplate(kit.getChestplate() == null ? null : kit.getChestplate().clone());
			if(ownKit.getChestplate() != null && ownKit.getChestplate().getAmount() < 1) {
				ownKit.getChestplate().setAmount(1);
			}
			ownKit.setLeggings(kit.getLeggings() == null ? null : kit.getLeggings().clone());
			if(ownKit.getLeggings() != null && ownKit.getLeggings().getAmount() < 1) {
				ownKit.getLeggings().setAmount(1);
			}
			ownKit.setBoots(kit.getBoots() == null ? null : kit.getBoots().clone());
			if(ownKit.getBoots() != null && ownKit.getBoots().getAmount() < 1) {
				ownKit.getBoots().setAmount(1);
			}
			ownKit.getTypes().clear();
		}
		//so the chest access works
		ownKit.setChestAccess(kit.isChestAccess());
		editing.put(p.getUniqueId(), ownKit);
		p.getInventory().setHelmet(ownKit.getHelmet());
		p.getInventory().setChestplate(ownKit.getChestplate());
		p.getInventory().setLeggings(ownKit.getLeggings());
		p.getInventory().setBoots(ownKit.getBoots());
		p.getInventory().setContents(ownKit.getInv());
		p.updateInventory();
	}

	public static BattleKit getKitEditing(Player p) {
		if(editing.containsKey(p.getUniqueId())) {
			return editing.get(p.getUniqueId());
		}
		return null;
	}

	public static void resetKitToDefault(Player p) {
		if(!isEditing(p)) return;
		BattleKit relative = null;
		BattleKit cur = getKitEditing(p);
		if(cur != null) {
			for(BattleKit bk : LegionPractice.getInstance().kits) {
				if(cur.getName().equals(bk.getName())) {
					relative = bk;
				}
			}
		}
		if(relative != null) {
			BattleKit ownKit = new BattleKit(relative.getName());
			ownKit.setInventory(new ArrayList<ItemStack>(relative.getInventory()));
			for(ItemStack is : ownKit.getInventory()) {
				if(is != null && is.getAmount() < 1) {
					is.setAmount(1);
				}
			}
			ownKit.setHelmet(relative.getHelmet() == null ? null : relative.getHelmet().clone());
			if(ownKit.getHelmet() != null && ownKit.getHelmet().getAmount() < 1) {
				ownKit.getHelmet().setAmount(1);
			}
			ownKit.setChestplate(relative.getChestplate() == null ? null : relative.getChestplate().clone());
			if(ownKit.getChestplate() != null && ownKit.getChestplate().getAmount() < 1) {
				ownKit.getChestplate().setAmount(1);
			}
			ownKit.setLeggings(relative.getLeggings() == null ? null : relative.getLeggings().clone());
			if(ownKit.getLeggings() != null && ownKit.getLeggings().getAmount() < 1) {
				ownKit.getLeggings().setAmount(1);
			}
			ownKit.setBoots(relative.getBoots() == null ? null : relative.getBoots().clone());
			if(ownKit.getBoots() != null && ownKit.getBoots().getAmount() < 1) {
				ownKit.getBoots().setAmount(1);
			}
			ownKit.setChestAccess(relative.isChestAccess());
			ownKit.getTypes().clear();
			LegionPractice.getInstance().clear(p, false, true);
			p.getInventory().setHelmet(ownKit.getHelmet());
			p.getInventory().setChestplate(ownKit.getChestplate());
			p.getInventory().setLeggings(ownKit.getLeggings());
			p.getInventory().setBoots(ownKit.getBoots());
			p.getInventory().setContents(ownKit.getInv());
			p.updateInventory();
			PlayerKits pk = LegionPractice.getInstance().getPlayerKitsHandler().getPlayerKits(p);
			BattleKit old = null;
			for(BattleKit bk : pk.getEditedKits()) {
				if(ownKit.getName().equals(bk.getName())) {
					old = bk;
				}
			}
			if(old != null) {
				pk.getEditedKits().remove(old);
			}
			pk.getEditedKits().add(ownKit);
			p.sendMessage(LegionPractice.getInstance().translateMessage(p, "kit-reset"));
			savePlayerKitsAsync(p);
		}
	}

	public static boolean isEditing(Player p) {
		return editing.containsKey(p.getUniqueId());
	}

	public static void saveKit(Player p) {
		BattleKit kit = getKitEditing(p);
		if(kit != null) {
			kit.setHelmet(p.getInventory().getHelmet());
			kit.setChestplate(p.getInventory().getChestplate());
			kit.setLeggings(p.getInventory().getLeggings());
			kit.setBoots(p.getInventory().getBoots());
			kit.getInventory().clear();
			kit.setInventory(new ArrayList<ItemStack>(Arrays.asList(ItemStackUtil.getContents(p))));
			for(ItemStack is : kit.getInventory()) {
				if(is != null && is.getAmount() < 1) {
					is.setAmount(1);
				}
			}
			if(kit.getHelmet() != null && kit.getHelmet().getAmount() < 1) {
				kit.getHelmet().setAmount((short) 1);
			}
			if(kit.getChestplate() != null && kit.getChestplate().getAmount() < 1) {
				kit.getChestplate().setAmount((short) 1);
			}
			if(kit.getLeggings() != null && kit.getLeggings().getAmount() < 1) {
				kit.getLeggings().setAmount((short) 1);
			}
			if(kit.getBoots() != null && kit.getBoots().getAmount() < 1) {
				kit.getBoots().setAmount((short) 1);
			}
			PlayerKits pk = LegionPractice.getInstance().getPlayerKitsHandler().getPlayerKits(p);
			BattleKit old = null;
			for(BattleKit bk : pk.getEditedKits()) {
				if(kit.getName().equals(bk.getName())) {
					old = bk;
				}
			}
			if(old != null) {
				pk.getEditedKits().remove(old);
			}
			pk.getEditedKits().add(kit);
			p.sendMessage(LegionPractice.getInstance().translateMessage(p, "kit-saved"));
			savePlayerKitsAsync(p);
		}
	}

	public static void leaveEditing(Player p) {
		editing.remove(p.getUniqueId());
		LegionPractice.getInstance().clear(p, true, true);
	}


	private static void savePlayerKitsAsync(Player p) {
		LegionPractice plugin = LegionPractice.getInstance();
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				if(p != null) {
					PlayerKits kit = plugin.getPlayerKitsHandler().getPlayerKits(p);
					kit.savePlayerKitsToFile();
				}
			}
		});
	}
}
