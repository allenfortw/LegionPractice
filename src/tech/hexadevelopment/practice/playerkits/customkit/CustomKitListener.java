package tech.hexadevelopment.practice.playerkits.customkit;	

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.utils.ItemStackUtils;
import tech.hexadevelopment.practice.utils.MaterialIdRemake;
import tech.hexadevelopment.practice.utils.SoundManager;
import tech.hexadevelopment.practice.LegionPractice;

/**
 * Per player custom kit listener class for events.
 * @author Toppe5
 * @since 0.1
 */
public class CustomKitListener implements Listener {

	private LegionPractice plugin;

	/**
	 * 
	 * @param plugin LegionPractice plugin.
	 */
	public CustomKitListener(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(!(e.getWhoClicked() instanceof Player)) return;
		ItemStack item = e.getCurrentItem();
		Player p = (Player) e.getWhoClicked();
		if(e.getClickedInventory() == p.getInventory()) return;
		if(item == null || item.getType() == Material.AIR) return;
		if(e.getInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.custom-kit-name")))) {
			e.setCancelled(true);
			if(e.getSlot() > 8) {
				ItemStack i = e.getClickedInventory().getContents()[4];
				ItemMeta meta = i.getItemMeta();
				if(!meta.hasDisplayName()) {
					meta.setDisplayName("");
				}
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', meta.getDisplayName() + item.getItemMeta().getDisplayName().replace(ChatColor.BLUE + "" + ChatColor.BOLD, "").replace("SPACE", " ")));
				if(item.getItemMeta().getDisplayName().equals(ChatColor.RED + "DELETE NAME")) {
					meta.setDisplayName("");
				}
				i.setItemMeta(meta);
				plugin.getPlayerKitsHandler().createIfNotExists(p);
				BattleKit kit = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
				if(meta.getDisplayName() != null && !meta.getDisplayName().equals("")) {
					kit.setName(meta.getDisplayName());
					kit.setIcon(ItemStackUtils.createItem(kit.getIcon().getType(), kit.getName()));
				}
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.new-name")).replace("<name>", meta.getDisplayName()));
			}
			else if(e.getSlot() == 4){
				plugin.getPlayerKitsHandler().openCustomKit(p);
			}
			else if(e.getSlot() == 3) {
				BattleKit kit = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
				ItemStack old = kit.getIcon();
				int x = MaterialIdRemake.getFakeId(old.getType())-1;
				ItemStack newIcon = new ItemStack(MaterialIdRemake.fromFakeId(x));
				while(newIcon == null || newIcon.getType() == null || newIcon.getType() == Material.AIR){
					x--;
					if(x < 1) return;
					newIcon = new ItemStack(MaterialIdRemake.fromFakeId(x));
				}
				ItemMeta meta = newIcon.getItemMeta();
				meta.setDisplayName(kit.getFancyName());
				newIcon.setItemMeta(meta);
				kit.setIcon(newIcon);
				e.getInventory().setItem(4, newIcon);
				SoundManager.playSound(p, p.getLocation(), "CHICKEN_EGG_POP", 1, (float) 0.7);
			}
			else if(e.getSlot() == 5) {
				BattleKit kit = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
				ItemStack old = kit.getIcon();
				int x = MaterialIdRemake.getFakeId(old.getType())+1;
				ItemStack newIcon = new ItemStack(MaterialIdRemake.fromFakeId(x));
				while(newIcon == null || newIcon.getType() == null || newIcon.getType() == Material.AIR){
					x++;
					if(x > 450) return;
					newIcon = new ItemStack(MaterialIdRemake.fromFakeId(x));
				}
				ItemMeta meta = newIcon.getItemMeta();
				meta.setDisplayName(kit.getFancyName());
				newIcon.setItemMeta(meta);
				kit.setIcon(newIcon);
				e.getInventory().setItem(4, newIcon);
				SoundManager.playSound(p, p.getLocation(), "CHICKEN_EGG_POP", 1, (float) 0.7);
			}
		}
		else if(e.getInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.main-title")))) {
			e.setCancelled(true);
			if(e.getSlot() == 5) {
				if(!PermissionsManager.hasPermission(p, Permission.CUSTOM_KIT_COMBO)) {
					p.sendMessage(plugin.translateMessage(p, "no-permission"));
					return;
				}
				plugin.getPlayerKitsHandler().createIfNotExists(p);
				BattleKit kit = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
				kit.setCombo(!kit.isCombo());
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.combo")).replace("<value>", (kit.isCombo() + "").replace("true", plugin.translateMessage(p, "yes-or-true", false)).replace("false", plugin.translateMessage(p, "no-or-false", false))));
				item.setItemMeta(meta);
			}
			else if(e.getSlot() == 6) {
				if(!PermissionsManager.hasPermission(p, Permission.CUSTOM_KIT_HORSE)) {
					p.sendMessage(plugin.translateMessage(p, "no-permission"));
					return;
				}
				plugin.getPlayerKitsHandler().createIfNotExists(p);
				BattleKit kit = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
				kit.setHorse(!kit.isHorse());
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.horse")).replace("<value>", (kit.isHorse() + "").replace("true", plugin.translateMessage(p, "yes-or-true", false)).replace("false", plugin.translateMessage(p, "no-or-false", false))));
				item.setItemMeta(meta);
			}
			else if(e.getSlot() == 7) {
				if(!PermissionsManager.hasPermission(p, Permission.CUSTOM_KIT_BUILD)) {
					p.sendMessage(plugin.translateMessage(p, "no-permission"));
					return;
				}
				plugin.getPlayerKitsHandler().createIfNotExists(p);
				BattleKit kit = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
				kit.setBuild(!kit.isBuild());
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.build")).replace("<value>", (kit.isBuild() + "").replace("true", plugin.translateMessage(p, "yes-or-true", false)).replace("false", plugin.translateMessage(p, "no-or-false", false))));
				item.setItemMeta(meta);
			}
			else if(e.getSlot() == 8) {
				if(!PermissionsManager.hasPermission(p, Permission.CUSTOM_KIT_BOW)) {
					p.sendMessage(plugin.translateMessage(p, "no-permission"));
					return;
				}
				plugin.getPlayerKitsHandler().createIfNotExists(p);
				BattleKit kit = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
				kit.setOnlyBow(!kit.isOnlyBow());
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.bow")).replace("<value>", (kit.isOnlyBow() + "").replace("true", plugin.translateMessage(p, "yes-or-true", false)).replace("false", plugin.translateMessage(p, "no-or-false", false))));
				item.setItemMeta(meta);
			}
			else if(e.getSlot() == 4) {
				BattleKit kit = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
				Inventory inv = Bukkit.createInventory(null, 6*9, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.custom-kit-name")));
				for(int i = 0; i < 9; i++) {
					if(i == 3) inv.setItem(i, ItemStackUtils.createItem(Material.STAINED_GLASS_PANE, ChatColor.DARK_PURPLE + "<-", (byte) 2));
					else if(i == 5) inv.setItem(i, ItemStackUtils.createItem(Material.STAINED_GLASS_PANE, ChatColor.DARK_PURPLE + "->", (byte) 2));
					else inv.setItem(i, ItemStackUtils.createItem(Material.STAINED_GLASS_PANE, " "));
				}
				inv.setItem(4, ItemStackUtils.createItem(kit.getIcon().getType(), kit.getName()));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "A"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "B"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "C"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "D"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "E"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "F"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "G"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "H"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "I"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "J"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "K"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "L"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "M"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "N"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "O"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "P"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "Q"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "R"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "S"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "T"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "U"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "V"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "W"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "X"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "Y"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "Z"));				
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "Å"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "Ä"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "Ö"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "0"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "1"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "2"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "3"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "4"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "5"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "6"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "7"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "8"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "9"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "&"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "#"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "-"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_BUTTON, ChatColor.BLUE + "" + ChatColor.BOLD + "_"));
				inv.addItem(ItemStackUtils.createItem(Material.STONE_PLATE, ChatColor.BLUE + "" + ChatColor.BOLD + "SPACE"));			
				inv.addItem(ItemStackUtils.createItem(Material.BEDROCK, ChatColor.RED + "DELETE NAME"));
				p.openInventory(inv);
			}
			else if(e.getSlot() > 17){
				if(item.getType() != Material.STAINED_GLASS_PANE) {
					if(e.getClick() == ClickType.SHIFT_LEFT) {
						BattleKit kit = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
						int x = e.getSlot()-18;
						int amount = kit.getInventory().get(x).getAmount()+1;
						if(amount > 64) amount = 1;
						if(amount < 1) amount = 64;
						item.setAmount(amount);
						kit.getInventory().set(x, item);
						return;
					}
					if(e.getClick() == ClickType.SHIFT_RIGHT) {
						BattleKit kit = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
						int x = e.getSlot()-18;
						int amount = kit.getInventory().get(x).getAmount()-1;
						if(amount > 64) amount = 1;
						if(amount < 1) amount = 64;
						item.setAmount(amount);
						kit.getInventory().set(x, item);
						return;
					}
				}
				Inventory inv = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.item")) + (e.getSlot()-17));
				if(plugin.getConfig().getBoolean("custom-kit.use-custom-items") && plugin.getConfig().get("custom-kit.items") != null) {
					for(Object o : plugin.getConfig().getList("custom-kit.items")) {
						if(o != null && o instanceof ItemStack) {
							inv.addItem((ItemStack) o);
						}
					}
				}
				else {
					inv.addItem(new ItemStack(Material.WOOD_SWORD));
					inv.addItem(new ItemStack(Material.STONE_SWORD));
					inv.addItem(new ItemStack(Material.IRON_SWORD));
					inv.addItem(new ItemStack(Material.DIAMOND_SWORD));
					for(int i = 1; i < 6; i++) {
						ItemStack d = new ItemStack(Material.DIAMOND_SWORD);
						d.addEnchantment(Enchantment.DAMAGE_ALL, i);
						inv.addItem(d);
					}
					inv.addItem(new ItemStack(Material.WOOD_AXE));
					inv.addItem(new ItemStack(Material.GOLD_AXE));
					inv.addItem(new ItemStack(Material.STONE_AXE));
					inv.addItem(new ItemStack(Material.IRON_AXE));
					inv.addItem(new ItemStack(Material.DIAMOND_AXE));
					inv.addItem(new ItemStack(Material.BOW));
					for(int i = 1; i < 6; i++) {
						ItemStack d = new ItemStack(Material.BOW);
						d.addEnchantment(Enchantment.ARROW_DAMAGE, i);
						inv.addItem(d);
					}
					inv.addItem(new ItemStack(Material.ARROW, 64));
					inv.addItem(new ItemStack(Material.BOWL, 32));
					inv.addItem(new ItemStack(Material.RED_MUSHROOM, 32));
					inv.addItem(new ItemStack(Material.BROWN_MUSHROOM, 32));
					inv.addItem(new ItemStack(Material.MUSHROOM_SOUP));
					inv.addItem(new ItemStack(Material.GOLDEN_APPLE, 8));
					ItemStack gapple = new ItemStack(Material.GOLDEN_APPLE);
					gapple.setDurability((short) 1);
					inv.addItem(gapple);
					inv.addItem(new ItemStack(Material.COOKED_BEEF, 64));
					inv.addItem(new ItemStack(Material.ENDER_PEARL, 16));
					inv.addItem(ItemStackUtils.createItem(Material.STAINED_GLASS_PANE,  ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.air")), (byte)1));
					try{
						for(PotionType type : PotionType.values()) {
							if(type != PotionType.WATER_BREATHING && type != PotionType.NIGHT_VISION && type != PotionType.WATER && type != PotionType.INSTANT_DAMAGE && type != PotionType.SLOWNESS
									&& type != PotionType.POISON && type != PotionType.WEAKNESS) {
								inv.addItem(createPotion(type, 1, false));
							}
						}
						for(PotionType type : PotionType.values()) {
							if(type != PotionType.INVISIBILITY && type != PotionType.WATER_BREATHING && type != PotionType.NIGHT_VISION && type != PotionType.WATER && type != PotionType.INSTANT_DAMAGE && type != PotionType.SLOWNESS
									&& type != PotionType.POISON && type != PotionType.FIRE_RESISTANCE && type != PotionType.WEAKNESS) {
								inv.addItem(createPotion(type, 2, false));
							}
						}
						for(PotionType type : PotionType.values()) {
							if(type == PotionType.INSTANT_DAMAGE || type == PotionType.SLOWNESS
									|| type == PotionType.POISON || type == PotionType.WEAKNESS || type == PotionType.INSTANT_HEAL) {
								inv.addItem(createPotion(type, 1, true));
							}
						}
						for(PotionType type : PotionType.values()) {
							if(type == PotionType.INSTANT_DAMAGE || type == PotionType.SLOWNESS
									|| type == PotionType.POISON || type == PotionType.WEAKNESS || type == PotionType.INSTANT_HEAL) {
								inv.addItem(createPotion(type, 2, true));
							}
						}
					}catch(Exception exception) {}
				}
				p.openInventory(inv);
			}
			else if(!PermissionsManager.hasPermission(p, Permission.CUSTOM_KIT_ARMOR)) {
				p.sendMessage(plugin.translateMessage(p, "no-permission"));
				return;
			}
			else if(e.getSlot() == 0) {
				Inventory helmets = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.helmets")));
				helmets.addItem(new ItemStack(Material.LEATHER_HELMET));
				helmets.addItem(new ItemStack(Material.GOLD_HELMET));
				helmets.addItem(new ItemStack(Material.CHAINMAIL_HELMET));
				helmets.addItem(new ItemStack(Material.IRON_HELMET));
				helmets.addItem(new ItemStack(Material.DIAMOND_HELMET));
				helmets.addItem(ItemStackUtils.createItem(Material.STAINED_GLASS_PANE,  ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.air")), (byte)1));
				addEnchantmentBooks(helmets);
				p.openInventory(helmets);
			}
			else if(e.getSlot() == 1) {
				Inventory chestplates = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.chestplates")));
				chestplates.addItem(new ItemStack(Material.LEATHER_CHESTPLATE));
				chestplates.addItem(new ItemStack(Material.GOLD_CHESTPLATE));
				chestplates.addItem(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
				chestplates.addItem(new ItemStack(Material.IRON_CHESTPLATE));
				chestplates.addItem(new ItemStack(Material.DIAMOND_CHESTPLATE));
				chestplates.addItem(ItemStackUtils.createItem(Material.STAINED_GLASS_PANE,  ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.air")), (byte)1));
				addEnchantmentBooks(chestplates);
				p.openInventory(chestplates);
			}
			else if(e.getSlot() == 2) {
				Inventory leggings = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.leggings")));
				leggings.addItem(new ItemStack(Material.LEATHER_LEGGINGS));
				leggings.addItem(new ItemStack(Material.GOLD_LEGGINGS));
				leggings.addItem(new ItemStack(Material.CHAINMAIL_LEGGINGS));
				leggings.addItem(new ItemStack(Material.IRON_LEGGINGS));
				leggings.addItem(new ItemStack(Material.DIAMOND_LEGGINGS));
				leggings.addItem(ItemStackUtils.createItem(Material.STAINED_GLASS_PANE,  ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.air")), (byte)1));
				addEnchantmentBooks(leggings);
				p.openInventory(leggings);
			}
			else if(e.getSlot() == 3) {
				Inventory boots = Bukkit.createInventory(null, 36, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.boots")));
				boots.addItem(new ItemStack(Material.LEATHER_BOOTS));
				boots.addItem(new ItemStack(Material.GOLD_BOOTS));
				boots.addItem(new ItemStack(Material.CHAINMAIL_BOOTS));
				boots.addItem(new ItemStack(Material.IRON_BOOTS));
				boots.addItem(new ItemStack(Material.DIAMOND_BOOTS));
				boots.addItem(ItemStackUtils.createItem(Material.STAINED_GLASS_PANE,  ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.air")), (byte)1));
				addEnchantmentBooks(boots);
				p.openInventory(boots);
			}
		}
		if(e.getInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.helmets")))) {
			e.setCancelled(true);
			if(item.getType() == Material.ENCHANTED_BOOK) {
				if(item.getEnchantments().isEmpty()) {
					for(ItemStack is : e.getInventory().getContents()) {
						if(is != null && is.getType() != Material.ENCHANTED_BOOK && is.getType() != Material.STAINED_GLASS_PANE) {
							is.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
							is.removeEnchantment(Enchantment.DURABILITY);
						}
					}
				}
				else {
					for(ItemStack is : e.getInventory().getContents()) {
						if(is != null && is.getType() != Material.ENCHANTED_BOOK && is.getType() != Material.STAINED_GLASS_PANE) is.addUnsafeEnchantments(item.getEnchantments());
					}
				}
			}
			else {
				if(item.getType() == Material.STAINED_GLASS_PANE) {
					item = new ItemStack(Material.AIR);
				}
				plugin.getPlayerKitsHandler().createIfNotExists(p);
				BattleKit kit = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
				kit.setHelmet(item);
				plugin.getPlayerKitsHandler().openCustomKit(p);
			}
		}
		else if(e.getInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.chestplates")))) {
			e.setCancelled(true);
			if(item.getType() == Material.ENCHANTED_BOOK) {
				if(item.getEnchantments().isEmpty()) {
					for(ItemStack is : e.getInventory().getContents()) {
						if(is != null && is.getType() != Material.ENCHANTED_BOOK && is.getType() != Material.STAINED_GLASS_PANE) {
							is.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
							is.removeEnchantment(Enchantment.DURABILITY);
						}
					}
				}
				else {
					for(ItemStack is : e.getInventory().getContents()) {
						if(is != null && is.getType() != Material.ENCHANTED_BOOK && is.getType() != Material.STAINED_GLASS_PANE) is.addUnsafeEnchantments(item.getEnchantments());
					}
				}
			}
			else {
				if(item.getType() == Material.STAINED_GLASS_PANE) {
					item = new ItemStack(Material.AIR);
				}
				plugin.getPlayerKitsHandler().createIfNotExists(p);
				BattleKit kit = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
				kit.setChestplate(item);
				plugin.getPlayerKitsHandler().openCustomKit(p);
			}
		}
		else if(e.getInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.leggings")))) {
			e.setCancelled(true);
			if(item.getType() == Material.ENCHANTED_BOOK) {
				if(item.getEnchantments().isEmpty()) {
					for(ItemStack is : e.getInventory().getContents()) {
						if(is != null && is.getType() != Material.ENCHANTED_BOOK && is.getType() != Material.STAINED_GLASS_PANE) {
							is.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
							is.removeEnchantment(Enchantment.DURABILITY);
						}
					}
				}
				else {
					for(ItemStack is : e.getInventory().getContents()) {
						if(is != null && is.getType() != Material.ENCHANTED_BOOK && is.getType() != Material.STAINED_GLASS_PANE) is.addUnsafeEnchantments(item.getEnchantments());
					}
				}
			}
			else {
				if(item.getType() == Material.STAINED_GLASS_PANE) {
					item = new ItemStack(Material.AIR);
				}
				plugin.getPlayerKitsHandler().createIfNotExists(p);
				BattleKit kit = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
				kit.setLeggings(item);
				plugin.getPlayerKitsHandler().openCustomKit(p);
			}
		}
		else if(e.getInventory().getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.boots")))) {
			e.setCancelled(true);
			if(item.getType() == Material.ENCHANTED_BOOK) {
				if(item.getEnchantments().isEmpty()) {
					for(ItemStack is : e.getInventory().getContents()) {
						if(is != null && is.getType() != Material.ENCHANTED_BOOK && is.getType() != Material.STAINED_GLASS_PANE) {
							is.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
							is.removeEnchantment(Enchantment.PROTECTION_FALL);
							is.removeEnchantment(Enchantment.DURABILITY);
						}
					}
				}
				else {
					for(ItemStack is : e.getInventory().getContents()) {
						if(is != null && is.getType() != Material.ENCHANTED_BOOK && is.getType() != Material.STAINED_GLASS_PANE) is.addUnsafeEnchantments(item.getEnchantments());
					}
				}
			}
			else {
				if(item.getType() == Material.STAINED_GLASS_PANE) {
					item = new ItemStack(Material.AIR);
				}
				plugin.getPlayerKitsHandler().createIfNotExists(p);
				BattleKit kit = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
				kit.setBoots(item);
				plugin.getPlayerKitsHandler().openCustomKit(p);
			}
		}
		else if (e.getInventory().getName().contains(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.item")))){
			e.setCancelled(true);
			plugin.getPlayerKitsHandler().createIfNotExists(p);
			BattleKit kit = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
			String n = e.getInventory().getName().replace(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.item")), "");
			if(e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
				for(int i = 0; i < kit.getInventory().size(); i++) {
					ItemStack is = kit.getInventory().get(i);
					if(is == null || is.getType() == Material.AIR || (item.getType() == Material.STAINED_GLASS_PANE && item.getItemMeta().hasDisplayName())) {
						if(item.getType().equals(Material.STAINED_GLASS_PANE) && item.getItemMeta().hasDisplayName()) kit.getInventory().set(i, new ItemStack(Material.AIR));
						else kit.getInventory().set(i, item);
					}
				}
			}
			else {
				try {
				int x = Integer.parseInt(n)-1;
				if(item.getType().equals(Material.STAINED_GLASS_PANE)) kit.getInventory().set(x, new ItemStack(Material.AIR));
				else {
					kit.getInventory().set(x, item);
				}
				}catch(Exception ex) {}
			}
			plugin.getPlayerKitsHandler().openCustomKit(p);
		}
	}

	@SuppressWarnings("deprecation")
	/**
	 * Creates a new potion itemstack with the given values.
	 * @param potionType type of the potion.
	 * @param level level of the potion.
	 * @param splash true for splash potion, otherwise false.
	 * @return a new potion itemstack that is equal to the given values.
	 */
	private ItemStack createPotion(PotionType potionType, int level, boolean splash) {
		if(level == 1) return new Potion(potionType, 1, splash, true).toItemStack(1);
		return new Potion(potionType, level, splash).toItemStack(1);

	}

	/**
	 * Create a enchanted book with the given enchantment.
	 * @param enchantment enchantment the book will have.
	 * @param level level of the enchantment.
	 * @return a new enchanted book that has the given enchantment with the given level.
	 */
	private ItemStack createEnchantBook(Enchantment enchantment, int level) {
		ItemStack is = new ItemStack(Material.ENCHANTED_BOOK);
		is.addUnsafeEnchantment(enchantment, level);
		return is;
	}

	/**
	 * Add the enchantment books to the given inventory depending on its size.
	 * @param inv the inventory where the books should be added.
	 */
	private void addEnchantmentBooks(Inventory inv) {
		inv.setItem(9, new ItemStack(Material.ENCHANTED_BOOK));
		inv.setItem(18, new ItemStack(Material.ENCHANTED_BOOK));
		for(int i = 1; i < 5; i++) {
			inv.setItem(9+i, createEnchantBook(Enchantment.PROTECTION_ENVIRONMENTAL, i));
		}
		for(int i = 1; i < 4; i++) {
			inv.setItem(18+i, createEnchantBook(Enchantment.DURABILITY, i));
		}
		if(inv.getName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.boots")))) {
			inv.setItem(27, new ItemStack(Material.ENCHANTED_BOOK));
			for(int i = 1; i < 5; i++) {
				inv.setItem(27+i, createEnchantBook(Enchantment.PROTECTION_FALL, i));
			}
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		if(e.getPlayer() instanceof Player) {
			if(e.getInventory().getName().equals("Edit Custom Kit Items")) {
				Player p = (Player) e.getPlayer();
				List<ItemStack> items = new ArrayList<ItemStack>();
				for(ItemStack is : e.getInventory().getContents()) {
					if(is != null && is.getType() != Material.AIR) {
						items.add(is);
					}
				}
				plugin.getConfig().set("custom-kit.items", items);
				plugin.getConfig().set("custom-kit.use-custom-items", true);
				plugin.saveConfig();
				p.sendMessage(ChatColor.RED + "Saved custom kit items and enabled custom items!");
			}
		}
	}
}
