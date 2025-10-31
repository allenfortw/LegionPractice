package tech.hexadevelopment.practice.preview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.utils.FormatUtils;
import tech.hexadevelopment.practice.utils.ItemStackUtils;

public class Preview implements CommandExecutor {

	public static HashMap<UUID, BattleKit> requestKits = new HashMap<UUID, BattleKit>();
	private LegionPractice plugin;
	public Preview(LegionPractice plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if(args.length > 0) {
				try{
					UUID uuid =  UUID.fromString(args[0]);
					if(requestKits.containsKey(uuid)) {
						preview(p, requestKits.get(uuid), plugin);
						return true;
					}
					else p.sendMessage(plugin.translateMessage(p, "invanlid-kit"));
				}catch(IllegalArgumentException e) {}
				BattleKit k = BattleKit.getKit(args[0]);
				if(k != null) {
					preview(p, k, plugin);
					return true;
				}
			}
			Inventory prev = Bukkit.createInventory(null, plugin.arenaPvP.getEditorSize(1), ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("preview.select-kit-title")));
			BattleKit custom = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
			if(custom != null && custom.getIcon() != null) prev.addItem(custom.getIcon());
			for(BattleKit kit : plugin.kits) {
				if(kit.getMergedEditor() == null && kit.getIcon() != null) {
					prev.addItem(kit.getIcon());
				}
			}
			p.openInventory(prev);
		}
		return true;
	}
	
	public static ItemStack backButtonCurrentInv(Player p) {
		LegionPractice plugin = LegionPractice.getInstance();
		p.setMetadata(PreviewListener.backInventory, new FixedMetadataValue(plugin, p.getOpenInventory().getTopInventory()));
		ItemStack back = new ItemStack(Material.WOOL);
		ItemMeta meta = back.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("back-button-name")));
		back.setItemMeta(meta);
		back.setDurability((short) 14);
		return back;
	}

	public static void preview(Player p, BattleKit kit, LegionPractice plugin) {
		p.setMetadata(PreviewListener.backInventory, new FixedMetadataValue(plugin, p.getOpenInventory().getTopInventory()));
		p.closeInventory();
		Inventory inv = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("preview.title")));
		p.openInventory(inv);
		ItemStack back = new ItemStack(Material.WOOL);
		ItemMeta meta = back.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("back-button-name")));
		back.setItemMeta(meta);
		back.setDurability((short) 14);
		inv.setItem(8, back);
		BattleKit editedKit = kit;
		if(kit.isLegionPracticeKit()) {
			if(LegionPractice.getInstance().getConfig().getStringList("replaces-kits").contains(kit.getName())) {
				editedKit = LegionPractice.getInstance().getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
			}
			if(kit.isEditable() || kit.getMergedEditor() != null) {
				if(kit.getMergedEditor() != null) {
					BattleKit mergedKit = BattleKit.getKit(kit.getMergedEditor());
					if(mergedKit != null) {
						editedKit = LegionPractice.getInstance().getPlayerKitsHandler().getPlayerKits(p).getEditedKit(mergedKit, false);
					}
					else {
						editedKit = LegionPractice.getInstance().getPlayerKitsHandler().getPlayerKits(p).getEditedKit(kit, false);
					}
				}
				else {
					editedKit = LegionPractice.getInstance().getPlayerKitsHandler().getPlayerKits(p).getEditedKit(kit, false);
				}
			}
		}
		if(editedKit.getHelmet() != null) inv.setItem(0, kit.getHelmet());
		if(editedKit.getChestplate() != null) inv.setItem(1, kit.getChestplate());
		if(editedKit.getLeggings() != null) inv.setItem(2, kit.getLeggings());
		if(editedKit.getBoots() != null) inv.setItem(3, kit.getBoots());
		if(kit.isCombo()) {
			Material combotMat = Material.getMaterial(plugin.getConfig().getString("custom-kit.combo-item"));
			if(combotMat != null) inv.addItem(ItemStackUtils.createItem(combotMat, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.combo")).replace("<value>", (kit.isCombo() + "").replace("true", plugin.translateMessage(p, "yes-or-true", false)).replace("false", plugin.translateMessage(p, "no-or-false", false)))));
		}
		if(kit.isHorse()) {
			Material horseMat = Material.getMaterial(plugin.getConfig().getString("custom-kit.horse-item"));
			if(horseMat != null) inv.addItem(ItemStackUtils.createItem(horseMat, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.horse")).replace("<value>", (kit.isHorse() + "").replace("true", plugin.translateMessage(p, "yes-or-true")).replace("false", plugin.translateMessage(p, "no-or-false", false)))));	
		}
		if(kit.isBuild()) {
			Material buildMat = Material.getMaterial(plugin.getConfig().getString("custom-kit.build-item"));
			if(buildMat != null) inv.addItem(ItemStackUtils.createItem(buildMat, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.build")).replace("<value>", (kit.isBuild() + "").replace("true", plugin.translateMessage(p, "yes-or-true", false)).replace("false", plugin.translateMessage(p, "no-or-false", false)))));
		}
		if(kit.isOnlyBow()) {
			Material bowMat = Material.getMaterial(plugin.getConfig().getString("custom-kit.bow-item"));
			if(bowMat != null) inv.addItem(ItemStackUtils.createItem(bowMat, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.bow")).replace("<value>", (kit.isOnlyBow() + "").replace("true", plugin.translateMessage(p, "yes-or-true", false)).replace("false", plugin.translateMessage(p, "no-or-false", false)))));
		}
		if(kit.isElo()) {
			Material elo = Material.getMaterial(plugin.getConfig().getString("preview.elo-item"));
			if(elo != null) inv.addItem(ItemStackUtils.createItem(elo, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("preview.elo-name")).replace("<value>", (kit.isElo() + "").replace("true", plugin.translateMessage(p, "yes-or-true", false)).replace("false", plugin.translateMessage(p, "no-or-false", false)))));
		}
		if(!kit.getPotions().isEmpty()) {
			ItemStack effects = new ItemStack(Material.POTION);
			ItemMeta efMeta = effects.getItemMeta();
			efMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("preview.pots")));
			List<String> lores = new ArrayList<String>();
			String pot = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("preview.potions"));
			for(PotionEffect e : kit.getPotions()) {
				lores.add(FormatUtils.formatPotionEffect(pot, e));
			}
			efMeta.setLore(lores);
			effects.setItemMeta(efMeta);
			inv.addItem(effects);
		}
		for(int i = 18; i < 36+18; i++) {
			inv.setItem(i, editedKit.getInventory().get(i-18));
		}
	}
}
