package tech.hexadevelopment.practice.playerkits;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.playerdata.JAndQListener;
import tech.hexadevelopment.practice.playerdata.PlayerDataFile;
import tech.hexadevelopment.practice.utils.ItemStackUtils;

/**
 * Per player custom kit handler class.
 * @author Toppe5
 * @since 0.1
 */
public class PlayerKitsManager {

	private LegionPractice plugin;
	public static String PLAYERKITS_META = "LegionPracticePlayerKits";

	/**
	 * Create a new custom kit handler with the given LegionPractice instance.
	 * @param plugin LegionPractice plugin.
	 */
	public PlayerKitsManager(LegionPractice plugin) {
		this.plugin = plugin;
	}

	/**
	 * Opens a custom kit editor GUI for the player with the player's custom kit.
	 * Creates a new custom kit if it doesn't exist.
	 * @param p player to open the custom kit editor GUI.
	 */
	public boolean openCustomKit(Player p) {
		if(p.hasMetadata(JAndQListener.LOADING_DATA)) {
			p.sendMessage(plugin.translateMessage(p, "data-not-loaded"));
			return false;
		}
		Inventory inv = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.main-title")));
		plugin.getPlayerKitsHandler().createIfNotExists(p);
		BattleKit kit = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
		String air = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.air"));
		if(PermissionsManager.hasPermission(p, Permission.CUSTOM_KIT_ARMOR)) {
			if(kit.getHelmet() == null || kit.getHelmet().getType() == Material.AIR) {
				inv.setItem(0, ItemStackUtils.createItem(Material.STAINED_GLASS_PANE,  air, (byte)1));
			}
			else inv.setItem(0, kit.getHelmet());
			if(kit.getChestplate() == null || kit.getChestplate().getType() == Material.AIR) {
				inv.setItem(1, ItemStackUtils.createItem(Material.STAINED_GLASS_PANE,  air, (byte)1));
			}
			else inv.setItem(1, kit.getChestplate());
			if(kit.getLeggings() == null || kit.getLeggings().getType() == Material.AIR) {
				inv.setItem(2, ItemStackUtils.createItem(Material.STAINED_GLASS_PANE,  air, (byte)1));
			}
			else inv.setItem(2, kit.getLeggings());
			if(kit.getBoots() == null || kit.getBoots().getType() == Material.AIR) {
				inv.setItem(3, ItemStackUtils.createItem(Material.STAINED_GLASS_PANE,  air, (byte)1));
			}
			else inv.setItem(3, kit.getBoots());	
		}
		inv.setItem(4, kit.getIcon());
		if(inv.getItem(4) == null) {
			Material iconMat = Material.getMaterial(plugin.getConfig().getString("custom-kit.icon-material"));
			if(iconMat != null) {
				kit.setIcon(ItemStackUtils.createItem(iconMat, kit.getName()));
			}
			inv.setItem(4, ItemStackUtils.createItem(kit.getIcon().getType(), kit.getName()));
		}
		if(PermissionsManager.hasPermission(p, Permission.CUSTOM_KIT_COMBO)) {
			Material combotMat = Material.getMaterial(plugin.getConfig().getString("custom-kit.combo-item"));
			if(combotMat != null) inv.setItem(5, ItemStackUtils.createItem(combotMat, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.combo"))
					.replace("<value>", (kit.isCombo() + "").replace("true", plugin.translateMessage(p, "yes-or-true", false)).replace("false", plugin.translateMessage(p, "no-or-false", false)))));
		}
		if(PermissionsManager.hasPermission(p, Permission.CUSTOM_KIT_HORSE)) {
			Material horseMat = Material.getMaterial(plugin.getConfig().getString("custom-kit.horse-item"));
			if(horseMat != null) inv.setItem(6, ItemStackUtils.createItem(horseMat, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.horse"))
					.replace("<value>", (kit.isHorse() + "").replace("true", plugin.translateMessage(p, "yes-or-true", false)).replace("false", plugin.translateMessage(p, "no-or-false", false)))));	
		}
		if(PermissionsManager.hasPermission(p, Permission.CUSTOM_KIT_BUILD)) {
			Material buildMat = Material.getMaterial(plugin.getConfig().getString("custom-kit.build-item"));
			if(buildMat != null) inv.setItem(7, ItemStackUtils.createItem(buildMat, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.build"))
					.replace("<value>", (kit.isBuild() + "").replace("true", plugin.translateMessage(p, "yes-or-true", false)).replace("false", plugin.translateMessage(p, "no-or-false", false)))));
		}
		if(PermissionsManager.hasPermission(p, Permission.CUSTOM_KIT_BOW)) {
			inv.setItem(8, ItemStackUtils.createItem(Material.BOW, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.bow"))
					.replace("<value>", (kit.isOnlyBow() + "").replace("true", plugin.translateMessage(p, "yes-or-true", false)).replace("false", plugin.translateMessage(p, "no-or-false", false)))));
		}
		for(int i = 18; i < 54; i++) {
			ItemStack item = kit.getInv()[i-18];
			if(item == null || item.getType() == Material.AIR) inv.setItem(i, ItemStackUtils.createItem(Material.STAINED_GLASS_PANE,  air, (byte)1));
			else inv.setItem(i, item);
		}
		String ma = plugin.getConfig().getString("custom-kit.second-line");
		String[] m = ma.split(":");
		Material sp = Material.getMaterial(m[0]);
		if(sp == null) sp = Material.STAINED_GLASS_PANE;
		byte dur = (byte) (m.length > 1 ? Integer.parseInt(m[1]) : 0);
		for(int i = 9; i<18; i++) {
			inv.setItem(i, ItemStackUtils.createItem(sp, " ", dur));
		}
		p.openInventory(inv);
		return true;
	}

	public void openKitEditorSelector(Player p) {
		Inventory inv = Bukkit.createInventory(null, plugin.arenaPvP.getEditorSize(0), ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("kit-editor-title")));
		for(BattleKit kit : plugin.kits) {
			if(kit.isEditable() && kit.getMergedEditor() == null && kit.getIcon() != null) {
				inv.addItem(kit.getIcon());
			}
		}
		p.openInventory(inv);
	}

	/**
	 * Create a custom kit for the player if it doesn't already exist.
	 * @param p player to create her/his custom kit if it doesn't exist.
	 */
	public void createIfNotExists(Player p) {
		setCustomKitMeta(p, getPlayerKits(p).getCustomKit());
	}

	/**
	 * Gets the default custom kit.
	 * @return the default BattleKit.
	 */
	public BattleKit getDefaultKit() {
		BattleKit bk = new BattleKit(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("custom-kit.default-name")));
		Material iconMat = Material.getMaterial(plugin.getConfig().getString("custom-kit.icon-material"));
		if(iconMat != null) bk.setIcon(ItemStackUtils.createItem(iconMat, bk.getName()));
		else bk.setIcon(ItemStackUtils.createItem(Material.DIAMOND_SWORD, bk.getName()));
		if(plugin.getConfig().getString("custom-kit.premade") != null) {
			String s = plugin.getConfig().getString("custom-kit.premade");
			for(BattleKit kit : plugin.kits) {
				if(kit.getName().equals(s)) {
					bk.setInventory(new ArrayList<ItemStack>(kit.getInventory()));
					bk.setHelmet(kit.getHelmet() == null ? null : kit.getHelmet().clone());
					bk.setChestplate(kit.getChestplate() == null ? null : kit.getChestplate().clone());
					bk.setLeggings(kit.getLeggings() == null ? null : kit.getLeggings().clone());
					bk.setBoots(kit.getBoots() == null ? null : kit.getBoots().clone());
					bk.getTypes().clear();
					return kit;
				}
			}
		}
		bk.setHelmet(new ItemStack(Material.LEATHER_HELMET));
		bk.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
		bk.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
		bk.setBoots(new ItemStack(Material.LEATHER_BOOTS));
		return bk;
	}

	public PlayerKits getPlayerKits(Player p) {
		return getPlayerKits(p, true);
	}
	
	public PlayerKits getPlayerKits(Player p, boolean loadFromFile) {
		if(p.hasMetadata(PLAYERKITS_META)) {
			MetadataValue m = plugin.getMetadata(p, PLAYERKITS_META);
			if(m != null && m.value() != null && m.value() instanceof PlayerKits) {
				return (PlayerKits) m.value();
			}
		}
		if(loadFromFile) {
			PlayerKits pk = loadFromFile(p.getUniqueId());
			return pk;
		}
		return null;
	}

	/**
	 * Loads the custom kit from the player data file.
	 * @param uuid uuid whose custom it should load.
	 * @return the custom kit or if it doesn't exist it will return the default custom kit.
	 */
	public PlayerKits loadFromFile(UUID uuid) {
		PlayerDataFile f = new PlayerDataFile(plugin, uuid + "");
		return loadFromFile(uuid, f);
	}

	/**
	 * Loads the custom kit from the player data file.
	 * @param uuid uuid whose custom it should load.
	 * @return the custom kit or if it doesn't exist it will return the default custom kit.
	 */
	public PlayerKits loadFromFile(UUID uuid, PlayerDataFile playerDataFile) {
		YamlConfiguration conf = playerDataFile.getConfig();
		PlayerKits pk = new PlayerKits(uuid);
		if(conf.get("custom-kit") != null && conf.get("custom-kit") instanceof BattleKit) {
			pk.setCustomKit((BattleKit) conf.get("custom-kit"));
		}
		if(pk.getCustomKit() == null) pk.setCustomKit(getDefaultKit());
		if(conf.get("kits") != null) {
			for(Object o : conf.getList("kits")) {
				if(o instanceof BattleKit) {
					pk.getEditedKits().add((BattleKit) o);
				}
			}
		}
		return pk;
	}

	/**
	 * Save the player's custom kit to Metadata.
	 * This method doesn't save the kit to player file.
	 * The custom kit will be automatically saved to the file when the player leaves the server.
	 * @param p
	 * @param kit
	 */
	public void setCustomKitMeta(Player p, BattleKit kit) {
		PlayerKits pk = getPlayerKits(p);
		pk.setCustomKit(kit);
		setPlayerKitsMeta(p, pk);
	}

	public void setPlayerKitsMeta(Player p, PlayerKits playerKits) {
		p.setMetadata(PLAYERKITS_META, new FixedMetadataValue(plugin, playerKits));
	}
}
