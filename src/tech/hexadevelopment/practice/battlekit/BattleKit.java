package tech.hexadevelopment.practice.battlekit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.arena.ArenaLeaveRunnable;
import tech.hexadevelopment.practice.events.KitDeselectEvent;
import tech.hexadevelopment.practice.events.KitSelectEvent;
import tech.hexadevelopment.practice.fights.queue.QueueManager;
import tech.hexadevelopment.practice.misc.SketchSMHook;
import tech.hexadevelopment.practice.scoreboard.HealthBar;
import tech.hexadevelopment.practice.LegionPractice;

/**
 * LegionPractice's kit system.
 * @author Toppe5
 * @since 0.1
 */
@SerializableAs("BattleKit")
public class BattleKit implements ConfigurationSerializable{

	public static String currentKit = "StrikPracticeCurrentBattleKit";
	public static String battleKitHorse = "StrikPracticeBattleKitHorse";
	public static int comboHitDelay;
	public static int defaultHitDelay;

	private String name;
	private boolean onlyBow;
	private boolean horse;
	private boolean combo;
	private boolean build;
	private boolean elo;
	private boolean editable;
	private boolean chestAccess;
	private boolean anticheatProtected;
	private boolean noHunger;
	private boolean stickSpawn;
	private boolean healthbar;
	private boolean bedwars;
	private int bestOf = 1;
	private List<ItemStack> inv = new ArrayList<ItemStack>();
	private String mergedEditor;
	private ItemStack helmet;
	private ItemStack chestplate;
	private ItemStack leggings;
	private ItemStack boots;
	private Collection<PotionEffect> potions = new ArrayList<PotionEffect>();
	private ItemStack icon;
	private HashSet<BattleKitType> types = new HashSet<BattleKitType>();
	private HashSet<Material> rollbackExtraMaterials = new HashSet<Material>();

	/**
	 * Creates a BattleKit using the given name.
	 * The BattleKit won't be added to LegionPractice automatically.
	 * @param name the name of the BattleKit
	 */
	public BattleKit(String name) {
		this.name = name;
		types.add(BattleKitType.ANY);
		fillEmptySlotsWithAir();
	}

	/**
	 * Deserializes a BattleKit from configuration. It is not recommended to use this manually, as it is
	 * intended for the Bukkit configuration serialization system.
	 * @param serialized map to deserialize.
	 */
	@SuppressWarnings("unchecked")
	public BattleKit(Map<String, Object> serialized) {
		if (serialized == null) return;
		if (serialized.isEmpty()) return;
		if(serialized.containsKey("inventory")) {
			Object serItemsList = serialized.get("inventory");
			if(serItemsList instanceof List<?>) {
				inv = (List<ItemStack>) serItemsList;
				for(ItemStack is : inv) {
					if(is != null && is.getAmount() <= 0) {
						is.setAmount(1);
					}
				}
				while(getInventory().size() < 36) {
					getInventory().add(new ItemStack(Material.AIR));
				}
			}
		}
		if(serialized.containsKey("helmet")) {
			Object o = serialized.get("helmet");
			if(o instanceof ItemStack) {
				if(((ItemStack) o).getAmount() <= 0) {
					((ItemStack) o).setAmount(1);
				}
				helmet = (ItemStack) o;
			}
		}
		if(serialized.containsKey("chestplate")) {
			Object o = serialized.get("chestplate");
			if(o instanceof ItemStack) {
				if(((ItemStack) o).getAmount() <= 0) {
					((ItemStack) o).setAmount(1);
				}
				chestplate = (ItemStack) o;
			}
		}
		if(serialized.containsKey("legs")) {
			Object o = serialized.get("legs");
			if(o instanceof ItemStack) {
				if(((ItemStack) o).getAmount() <= 0) {
					((ItemStack) o).setAmount(1);
				}
				leggings = (ItemStack) o;
			}
		}
		else if(serialized.containsKey("leggings")) {
			Object o = serialized.get("leggings");
			if(o instanceof ItemStack) {
				if(((ItemStack) o).getAmount() <= 0) {
					((ItemStack) o).setAmount(1);
				}
				leggings = (ItemStack) o;
			}
		}
		if(serialized.containsKey("boots")) {
			Object o = serialized.get("boots");
			if(o instanceof ItemStack) {
				if(((ItemStack) o).getAmount() <= 0) {
					((ItemStack) o).setAmount(1);
				}
				boots = (ItemStack) o;
			}
		}
		if(serialized.containsKey("types")) {
			Object t = serialized.get("types");
			if(t instanceof String) {
				for(String type : ((String) t).split(", ")) {
					try{
						types.add(BattleKitType.valueOf(type));
					}catch(Exception e){}
				}
			}
		}
		if(serialized.containsKey("editable")) {
			Object ed = serialized.get("editable");
			if(ed instanceof Boolean) this.editable = (Boolean) ed;
		}
		if(serialized.containsKey("best-of")) {
			Object bo = serialized.get("best-of");
			if(bo instanceof Integer) this.bestOf = (int) bo;
		}
		if(serialized.containsKey("build")) {
			Object bu = serialized.get("build");
			if(bu instanceof Boolean) this.build = (Boolean) bu;
		}
		if(serialized.containsKey("elo")) {
			Object e = serialized.get("elo");
			if(e instanceof Boolean) this.elo = (Boolean) e;
		}
		if(serialized.containsKey("horse")) {
			Object h = serialized.get("horse");
			if(h instanceof Boolean) this.horse = (Boolean) h;
		}
		if(serialized.containsKey("combo")) {
			Object c = serialized.get("combo");
			if(c instanceof Boolean) this.combo = (Boolean) c;
		}
		if(serialized.containsKey("stick-spawn")) {
			Object s = serialized.get("stick-spawn");
			if(s instanceof Boolean) this.stickSpawn = (Boolean) s;
		}
		if(serialized.containsKey("bedwars")) {
			Object b = serialized.get("bedwars");
			if(b instanceof Boolean) this.bedwars = (Boolean) b;
		}
		if(serialized.containsKey("health-bar")) {
			Object h = serialized.get("health-bar");
			if(h instanceof Boolean) this.healthbar = (boolean) h;
		}
		if(serialized.containsKey("only-bow")) {
			Object b = serialized.get("only-bow");
			if(b instanceof Boolean) this.onlyBow = (Boolean) b;
		}
		if(serialized.containsKey("no-hunger")) {
			Object b = serialized.get("no-hunger");
			if(b instanceof Boolean) this.noHunger = (Boolean) b;
		}
		if(serialized.containsKey("chest-access")) {
			Object b = serialized.get("chest-access");
			if(b instanceof Boolean) this.chestAccess = (Boolean) b;
		}
		if(serialized.containsKey("anticheat-protected")) {
			Object ac = serialized.get("anticheat-protected");
			if(ac instanceof Boolean) this.anticheatProtected = (Boolean) ac;
		}
		if(serialized.containsKey("name")) {
			Object n = serialized.get("name");
			if(n instanceof String) this.name = (String) n;
		}
		if(serialized.containsKey("merged-editable-kit")) {
			Object e = serialized.get("merged-editable-kit");
			if(e instanceof String) this.mergedEditor = (String) e;
		}
		if(serialized.containsKey("potions")) {
			Object potList = serialized.get("potions");
			if(potList instanceof Collection<?>) {

				Collection<PotionEffect> pots = new ArrayList<PotionEffect>();
				for(Object i : ((Collection<?>)potList)) {
					if(i instanceof PotionEffect) {
						pots.add((PotionEffect) i);
					}
				}
				potions = pots;
			}
		}
		if(serialized.containsKey("icon")) {
			Object ic = serialized.get("icon");
			if(ic instanceof ItemStack) {
				this.icon = (ItemStack) ic;
				if(icon.getAmount() <= 0) {
					icon.setAmount(1);
				}
			}
		}
		if(serialized.containsKey("extra-rollback-materials")) {
			Object list = serialized.get("extra-rollback-materials");
			if(list instanceof String) {
				for(String type : ((String) list).split(",")) {
					try{
						rollbackExtraMaterials.add(Material.valueOf(type));
					}catch(Exception e){}
				}
			}
		}
		//only works with 1.8 and newer
		LegionPractice plugin = LegionPractice.getInstance();
		if(plugin.getNMSAccessProvider().laterThan1_8 || plugin.getNMSAccessProvider().getVersion().contains("1_8")) {
			try {
				ItemMeta meta = icon.getItemMeta();
				icon.setItemMeta(meta);
			}catch(Exception e) {}
		}
	}

	/**
	 * Serializes a BattleKit to configuration. It is not recommended to use this manually, as it is
	 * intended for the Bukkit configuration serialization system.
	 * @return the serialized map.
	 */
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> serialized = new HashMap<String, Object>();
		serialized.put("name", name);
		if(icon != null) {
			ItemMeta m = icon.getItemMeta();
			LegionPractice plugin = LegionPractice.getInstance();
			if(plugin.getNMSAccessProvider().laterThan1_8 || plugin.getNMSAccessProvider().getVersion().contains("1_8")) {
				try {
					ItemMeta meta = m.clone();
					icon.setItemMeta(meta);
				}catch(Exception e) {}
			}
			serialized.put("icon", icon.clone());
			icon.setItemMeta(m);
		}
		if(editable) serialized.put("editable", editable);
		if(combo) serialized.put("combo", combo);
		if(elo) serialized.put("elo", elo);
		if(horse) serialized.put("horse", horse);
		if(onlyBow) serialized.put("only-bow", onlyBow);
		if(build) serialized.put("build", build);
		if(noHunger) serialized.put("no-hunger", noHunger);
		if(chestAccess) serialized.put("chest-access", chestAccess);
		if(anticheatProtected) serialized.put("anticheat-protected", anticheatProtected);
		if(stickSpawn) serialized.put("stick-spawn", stickSpawn);
		if(bedwars) serialized.put("bedwars", bedwars);
		if(healthbar) serialized.put("health-bar", healthbar);
		if(!potions.isEmpty()) serialized.put("potions", potions);
		if(mergedEditor != null) serialized.put("merged-editable-kit", mergedEditor);
		HashSet<String> stringTypes = new HashSet<String>();
		if(!types.isEmpty()) {
			for(BattleKitType t : types) {
				stringTypes.add(t.toString());
			}
			serialized.put("types", stringTypes.toString().replace("[", "").replace("]", ""));
		}
		if(!rollbackExtraMaterials.isEmpty()) {
			String mats = "";
			String sep = "";
			for(Material t : rollbackExtraMaterials) {
				mats += sep;
				mats += t.toString();
				sep = ",";
			}
			serialized.put("extra-rollback-materials", mats);
		}
		serialized.put("helmet", helmet);
		serialized.put("chestplate", chestplate);
		serialized.put("leggings", leggings);
		serialized.put("boots", boots);
		serialized.put("inventory", inv);
		serialized.put("best-of", bestOf);
		return serialized;
	}

	/**
	 * Save this BattleKit so LegionPractice can use it.
	 * This BattleKit will be saved to the kits.yml file when the plugin gets unloaded and the BattleKit will be loaded back when the plugin gets loaded again.
	 */
	public void saveForLegionPractice() {
		LegionPractice.getInstance().kits.add(this);
	}

	/**
	 * Remove this BattleKit from LegionPractice so it won't be used by LegionPractice anymore
	 * @return true if the BattleKit was known by LegionPractice, false if it wasn't saved for LegionPractice
	 */
	public boolean removeFromLegionPractice() {
		LegionPractice plugin = LegionPractice.getInstance();
		if(plugin.kits.contains(this)) {
			plugin.kits.remove(this);
			return true;
		}
		return false;
	}

	public boolean isLegionPracticeKit() {
		return LegionPractice.getInstance().kits.contains(this);
	}

	/**
	 * Gets a BattleKit with the given name. Ignores custom kit.
	 * @param name name of the BattleKit.
	 * @return a BattleKit if found, null if not found.
	 */
	public static BattleKit getKit(String name) {
		for(BattleKit bk : LegionPractice.getInstance().kits) {
			if(bk.getName().equalsIgnoreCase(name)) {
				return bk;
			}
		}
		return null;
	}

	/**
	 * Gets the first BattleKit with the given icon.
	 * Returns player's custom kit if the player has a custom kit with the given icon.
	 * It checks the player's custom kit first then it will search thought all ToppeBattle's BattleKits.
	 * @param p player whose custom kit it might be.
	 * @param icon icon of the BattleKit.
	 * @return a BattleKit if found otherwise null.
	 */
	public static BattleKit getKit(Player p, ItemStack icon, boolean includeRanked) {
		LegionPractice plugin = LegionPractice.getInstance();
		BattleKit custom = plugin.getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
		if(custom != null && custom.getIcon() != null && icon.equals(custom.getIcon())) {
			return custom;
		}
		List<BattleKit> kits = new ArrayList<BattleKit>();
		for(BattleKit bk : getKits(icon)) {
			if(bk.isElo() && includeRanked) {
				kits.add(bk);
			}
			else if(!bk.isElo()) {
				kits.add(bk);
			}
		}
		if(kits.size() > 0) {
			return kits.get(0);
		}
		return null;
	}


	/**
	 * Gets a BattleKit with the given icon. Ignores custom kits.
	 * @param icon itemstack that is the BattleKit's icon.
	 * @return a BattleKit if found otherwise null.
	 */
	public static BattleKit getKit(ItemStack icon) {
		for(BattleKit bk : LegionPractice.getInstance().kits) {
			if(bk.getIcon() != null && bk.getIcon().getType().equals(icon.getType())
					&& bk.getIcon().getDurability() == icon.getDurability()) {
				if(!bk.getIcon().getItemMeta().hasDisplayName()) return bk;
				if(bk.getIcon().getItemMeta().getDisplayName().equals(icon.getItemMeta().getDisplayName())) {
					return bk;
				}
			}
		}
		return null;
	}

	/**
	 * Gets a BattleKit with the given icon. Ignores custom kits.
	 * @param icon itemstack that is the BattleKit's icon.
	 * @return a BattleKit if found otherwise null.
	 */
	public static List<BattleKit> getKits(ItemStack icon) {
		List<BattleKit> kits = new ArrayList<BattleKit>();
		for(BattleKit bk : LegionPractice.getInstance().kits) {
			if(bk.getIcon() != null && bk.getIcon().getType().equals(icon.getType())
					&& bk.getIcon().getDurability() == icon.getDurability()) {
				if(!bk.getIcon().getItemMeta().hasDisplayName()) kits.add(bk);
				else if(bk.getIcon().getItemMeta().getDisplayName().equals(icon.getItemMeta().getDisplayName())) {
					kits.add(bk);
				}
			}
		}
		return kits;
	}

	/**
	 * Gets the current kit of the player.
	 * @param entity the kit of this player
	 * @return null if the player doesn't have a current kit other
	 */
	public static BattleKit getCurrentKit(Metadatable metadatable){
		if(metadatable.hasMetadata(currentKit)) {
			MetadataValue m = LegionPractice.getInstance().getMetadata(metadatable, currentKit);
			if(m != null && m.value() != null && m.value() instanceof BattleKit) {
				return (BattleKit) m.value();
			}
		}
		return null;
	}

	public void giveKitMeta(Metadatable metadatable) {
		metadatable.setMetadata(currentKit, new FixedMetadataValue(LegionPractice.getInstance(), this));
	}

	/**
	 * Gives the player this BattleKit.
	 * It will call the KitSelectEvent and if it is cancelled the player won't get the kit.
	 * @param p player to give the BattleKit.
	 */
	public void giveKit(Player p) {
		p.removeMetadata(ArenaLeaveRunnable.IN_STORM, LegionPractice.getInstance());
		BattleKit kit = this;
		KitSelectEvent event = new KitSelectEvent(p, kit);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) return;
		if(SketchSMHook.isInStaffMode(p)) {
			p.chat("/staff");
		}
		if(LegionPractice.getInstance().getSpectatorHandler().isSpectator(p)) {
			LegionPractice.getInstance().getSpectatorHandler().removeSpectator(p, false);
		}
		p.setFlying(false);
		p.setAllowFlight(false);
		kit = event.getKit();
		if(kit == null) return;
		BattleKit editedKit = kit;
		if(!LegionPractice.performanceMode && LegionPractice.getInstance().getConfig().getStringList("replaces-kits").contains(kit.getName())) {
			kit = LegionPractice.getInstance().getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
			editedKit = kit;
		}
		if(editable || mergedEditor != null) {
			if(mergedEditor != null) {
				BattleKit mergedKit = BattleKit.getKit(mergedEditor);
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
		QueueManager.leaveQueue(p, true);
		LegionPractice plugin = LegionPractice.getInstance();
		boolean meta = p.hasMetadata(plugin.IN_FIGHT);
		plugin.clear(p, false, true);
		p.getInventory().setHelmet(editedKit.getHelmet());
		p.getInventory().setChestplate(editedKit.getChestplate());
		p.getInventory().setLeggings(editedKit.getLeggings());
		p.getInventory().setBoots(editedKit.getBoots());
		for(ItemStack is : editedKit.getInv()) {
			if(is != null && is.getAmount() <= 0) {
				is.setAmount(1);
			}
		}
		p.getInventory().setContents(editedKit.getInv());
		for(PotionEffect ef : kit.getPotions()) {
			p.addPotionEffect(ef);
		}
		if(kit.isCombo()) {
			p.setMaximumNoDamageTicks(comboHitDelay);
		}
		else p.setMaximumNoDamageTicks(defaultHitDelay);
		if(kit.isHorse()) {
			new BukkitRunnable() {

				@Override
				public void run() {
					if(p != null) {
						if(!p.getLocation().getChunk().isLoaded()) p.getLocation().getChunk().load();
						boolean meta = p.hasMetadata(plugin.IN_FIGHT);
						p.removeMetadata(plugin.IN_FIGHT, plugin);
						Horse horse = p.getWorld().spawn(p.getLocation(), Horse.class);
						horse.setAdult();
						horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
						horse.setTamed(true);
						horse.setOwner(p);
						horse.setPassenger(p);
						horse.setMetadata(battleKitHorse, new FixedMetadataValue(plugin, p.getName()));
						if(meta) p.setMetadata(plugin.IN_FIGHT, new FixedMetadataValue(plugin, true));
					}
				}
			}.runTaskLater(plugin, 20);
		}
		if(p.getGameMode() != GameMode.SURVIVAL) {
			p.setGameMode(GameMode.SURVIVAL);
		}
		if(meta) p.setMetadata(plugin.IN_FIGHT, new FixedMetadataValue(plugin, true));
		p.setMetadata(currentKit, new FixedMetadataValue(plugin, this));
		if(isHealthbar()) HealthBar.register(p);
	}


	public HashSet<Material> getBlocks() {
		HashSet<Material> materials = new HashSet<Material>();
		boolean water = false;
		boolean lava = false;
		for(ItemStack is : getInv()) {
			if(is != null && is.getType() != Material.AIR) {
				if(is.getType().isBlock()) {
					materials.add(is.getType());
				}
				else if(is.getType() == Material.WATER_BUCKET || is.getType() == Material.WATER) {
					materials.add(Material.WATER);
					materials.add(Material.STATIONARY_WATER);
					if(lava){
						materials.add(Material.OBSIDIAN);
						materials.add(Material.STONE);
						materials.add(Material.COBBLESTONE);
					}
					water = true;
				}
				else if(is.getType() == Material.LAVA_BUCKET || is.getType() == Material.LAVA) {
					materials.add(Material.LAVA);
					materials.add(Material.STATIONARY_LAVA);
					if(water){
						materials.add(Material.OBSIDIAN);
						materials.add(Material.STONE);
						materials.add(Material.COBBLESTONE);
					}
					lava = true;
				}
				else if(is.getType() == Material.FLINT_AND_STEEL || is.getType() == Material.FIREBALL) {
					materials.add(Material.FIRE);
				}
				else if(is.getType() == Material.BOAT) {
					materials.add(Material.WOOD);
				}
			}
			materials.addAll(rollbackExtraMaterials);
		}
		return materials;
	}

	/**
	 * Deselects player's current kit.
	 * It will call the KitDeselectEvent and if it is cancelled nothing will happen.
	 * It also removes a few metadatas of the player and sets MaximumNoDamageTicks to 20 even if the player doesn't have a BattleKit selected.
	 * @param p whose kit will be deselected.
	 */
	public static void deselectKit(Player p) {
		BattleKit kit = getCurrentKit(p);
		KitDeselectEvent event = new KitDeselectEvent(p);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) return;
		if(kit != null && kit.isHorse()) {
			for(World w : Bukkit.getWorlds()) {
				for(Horse horse : w.getEntitiesByClass(Horse.class)) {
					if(horse.hasMetadata(battleKitHorse)) {
						MetadataValue m = LegionPractice.getInstance().getMetadata(horse, battleKitHorse);
						if(m != null && m.value() != null && m.asString().equals(p.getName())) {
							horse.remove();
						}
					}
				}
			}
		}
		LegionPractice plugin = LegionPractice.getInstance();
		p.setMaximumNoDamageTicks(defaultHitDelay);
		p.removeMetadata(plugin.IN_FIGHT, plugin);
		p.removeMetadata(currentKit, plugin);
		HealthBar.unregisterHealthBar(p);
	}

	public void fillEmptySlotsWithAir() {
		while(getInventory().size() < 36) {
			getInventory().add(new ItemStack(Material.AIR));
		}
	}

	/**
	 * After giving a player this kit the player can not attack other players with swords, fists etc.
	 * @param onlyBow true for an only bow kit, otherwise false.
	 */
	public void setOnlyBow(boolean onlyBow) {
		this.onlyBow = onlyBow;
	}

	/**
	 * Sets the boots of this kit.
	 * @param boots an itemStack that will be used as the boots.
	 */
	public void setBoots(ItemStack boots) {
		this.boots = boots;
	}

	public HashSet<BattleKitType> getTypes() {
		return types;
	}

	public void setTypes(HashSet<BattleKitType> types) {
		this.types = types;
	}

	/**
	 * Sets the chestplate of this kit.
	 * @param chestplate the itemStack that will be used as the chestplate.
	 */
	public void setChestplate(ItemStack chestplate) {
		this.chestplate = chestplate;
	}

	/**
	 * Sets the chesplate of this kit.
	 * @param helmet the itemStack that will be used as the helmet.
	 */
	public void setHelmet(ItemStack helmet) {
		this.helmet = helmet;
	}

	/**
	 * Sets the leggings of this kit.
	 * @param leggings the itemStack that will be used as the leggings.
	 */
	public void setLeggings(ItemStack leggings) {
		this.leggings = leggings;
	}

	/**
	 * @return the chestAccess
	 */
	public boolean isChestAccess() {
		return chestAccess;
	}

	/**
	 * @param chestAccess the chestAccess to set
	 */
	public void setChestAccess(boolean chestAccess) {
		this.chestAccess = chestAccess;
	}

	/**
	 * @return the mergedEditor
	 */
	public String getMergedEditor() {
		return mergedEditor;
	}

	/**
	 * @param mergedEditor the mergedEditor to set
	 */
	public void setMergedEditor(String mergedEditor) {
		this.mergedEditor = mergedEditor;
	}

	/**
	 * Sets the inventory contents of this kit.
	 * ItemStacks that will be gotten when giving the kit.
	 * @param inventory the list of items that will be used as inventory contents in the kit.
	 */
	public void setInventory(List<ItemStack> inventory) {
		this.inv = inventory;
	}

	public void setBestOf(int bestOf) {
		this.bestOf = bestOf;
	}

	public int getBestOf() {
		if(bestOf < 1) return 1;
		return bestOf;
	}

	/**
	 * @return the editable
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * @param editable the editable to set
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	/**
	 * Sets the collection of potion effects of this kit.
	 * @param potions the collection of potion effects that will be applied when giving a player the kit.
	 */
	public void setPotions(Collection<PotionEffect> potions) {
		this.potions = potions;
	}

	/**
	 * Usually fights will get a build arena if the BattleKit is a build BattleKit.
	 * @param build true for a build BattleKit, false for not a build BattleKit.
	 */
	public void setBuild(boolean build) {
		this.build = build;
	}

	/**
	 * Sets this BattleKit's name.
	 * Many BattleKit searches will be handled through names.
	 * It might not be a good idea to have multiple BattleKits with the same name.
	 * @param name the name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the boots of this BattleKit.
	 * @return the boots itemstack of this kit.
	 */
	public ItemStack getBoots() {
		return boots;
	}

	/**
	 * Gets the leggings of this BattleKit.
	 * @return the leggings itemstack of this kit.
	 */
	public ItemStack getLeggings() {
		return leggings;
	}

	/**
	 * Gets the chestplate of this BattleKit.
	 * @return the chestplate itemstack of this kit.
	 */
	public ItemStack getChestplate() {
		return chestplate;
	}

	/**
	 * Gets the helmet of this BattleKit.
	 * @return the helmet itemstack of this kit.
	 */
	public ItemStack getHelmet() {
		return helmet;
	}

	/**
	 * Gets the inventory contents of this kit.
	 * @return the inventory contents of this kit.
	 */
	public List<ItemStack> getInventory() {
		return inv;
	}

	/**
	 * Gets a copy of the inventory contents of this kit.
	 * @return the inventory contents of this kit.
	 */
	public ItemStack[] getInv() {
		return getInventory().toArray(new ItemStack[36]);
	}

	/**
	 * Gets the potion effects of this kit.
	 * @return potion effects of this kit.
	 */
	public Collection<PotionEffect> getPotions() {
		return potions;
	}

	/**
	 * Gets the icon itemstack of this kit.
	 * Many BattleKit searches will be handled through icons.
	 * @return the icon itemstack of this kit.
	 */
	public ItemStack getIcon() {
		return icon;
	}

	/**
	 * @return true if the kit is protected by the anticheat system
	 */
	public boolean isAnticheatProtected() {
		return anticheatProtected;
	}

	/**
	 * @param anticheatProtected true if the kit should be protected by the anticheat system
	 */
	public void setAnticheatProtected(boolean anticheatProtected) {
		this.anticheatProtected = anticheatProtected;
	}

	/**
	 * Sets the icon itemstack of this kit.
	 * Many BattleKit searches will be handled through icons.
	 * @param icon the itemstack that will be the icon.
	 */
	public void setIcon(ItemStack icon) {
		this.icon = icon;
	}

	/**
	 * Gets the name of this kit.
	 * Many BattleKit searches will be handled through names.
	 * @return the name of this kit.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns true if this kit is a only bow kit.
	 * @return true if this kit is a only bow kit, false if it's not.
	 */
	public boolean isOnlyBow() {
		return onlyBow;
	}

	/**
	 * When giving a player a combo BattleKit the player's MaximumNoDamageTicks will be set to 2.
	 * @param combo	true for a combo kit, false for not a combo kit.
	 */
	public void setCombo(boolean combo) {
		this.combo = combo;
	}

	/**
	 * @param elo true for a elo kit, false for not a elo kit.
	 */
	public void setElo(boolean elo) {
		this.elo = elo;
	}

	/**
	 * When giving a player a horse BattleKit he will be riding a horse.
	 * @param horse true for a horse kit, false for not a horse kit.
	 */
	public void setHorse(boolean horse) {
		this.horse = horse;
	}

	/**
	 * Returns true if the kit is a combo kit.
	 * When giving a player a combo BattleKit the player's MaximumNoDamageTicks will be set to 2.
	 * @return true if the BattleKit is a combo kit, false if it's not.
	 */
	public boolean isCombo() {
		return combo;
	}

	public HashSet<Material> getRollbackExtraMaterials() {
		return rollbackExtraMaterials;
	}

	public void addExtraRollbackMaterial(Material material) {
		if(material == Material.WATER_BUCKET || material == Material.WATER) {
			rollbackExtraMaterials.add(Material.WATER);
			rollbackExtraMaterials.add(Material.STATIONARY_WATER);
		}
		else if(material == Material.LAVA_BUCKET || material == Material.LAVA) {
			rollbackExtraMaterials.add(Material.LAVA);
			rollbackExtraMaterials.add(Material.STATIONARY_LAVA);
		}
		else if(material == Material.FLINT_AND_STEEL || material == Material.FIREBALL) {
			rollbackExtraMaterials.add(Material.FIRE);
		}
		else if(material == Material.BOAT) {
			rollbackExtraMaterials.add(Material.WOOD);
		}
		rollbackExtraMaterials.add(material);
	}

	public void setRollbackExtraMaterials(HashSet<Material> rollbackExtraMaterials) {
		this.rollbackExtraMaterials = rollbackExtraMaterials;
	}

	/**
	 * Returns true if the BattleKit is an elo kit.
	 * Usually only used for 1v1's.
	 * Elo will be calculated and given after the 1v1 normally.
	 * @return true if the BattleKit is a elo kit, false if it's not.
	 */
	public boolean isElo() {
		return elo;
	}

	/**
	 * Returns true if the BattleKit is a horse kit.
	 * When giving a player a horse BattleKit he will be riding a horse.
	 * @return true if the BattleKit is a horse kit, false if it's not.
	 */
	public boolean isHorse() {
		return horse;
	}

	public void setNoHunger(boolean noHunger) {
		this.noHunger = noHunger;
	}

	public boolean isNoHunger() {
		return noHunger;
	}

	/**
	 * Returns true if the BattleKit is a build kit.
	 * @return true of the BattleKit is a build kit, false if it's not.
	 */
	public boolean isBuild() {
		return build;
	}

	public boolean isStickSpawn() {
		return stickSpawn;
	}
	
	public boolean isBedwars() {
		return bedwars;
	}
	
	public void setBedwars(boolean bedwars) {
		this.bedwars = bedwars;
	}
	
	public boolean isHealthbar() {
		return healthbar;
	}
	
	public void setHealthbar(boolean healthbar) {
		this.healthbar = healthbar;
	}

	public void setStickSpawn(boolean stickSpawn) {
		this.stickSpawn = stickSpawn;
	}

	public void sendFightInfo(Player p) {
		LegionPractice plugin = LegionPractice.getInstance();
		if(isBuild()) {
			if(!plugin.translateMessage(p, "fight-start-message.build", false).equals("false")) {
				p.sendMessage(plugin.translateMessage(p, "fight-start-message.build"));	
			}
		}
		if(isHorse()) {
			if(!plugin.translateMessage(p, "fight-start-message.horse", false).equals("false")) {
				p.sendMessage(plugin.translateMessage(p, "fight-start-message.horse"));	
			}
		}
		if(isCombo()) {
			if(!plugin.translateMessage(p, "fight-start-message.combo", false).equals("false")) {
				p.sendMessage(plugin.translateMessage(p, "fight-start-message.combo"));	
			}
		}
		if(isOnlyBow()) {
			if(!plugin.translateMessage(p, "fight-start-message.only-bow", false).equals("false")) {
				p.sendMessage(plugin.translateMessage(p, "fight-start-message.only-bow"));	
			}
		}
	}

	/**
	 * Deserializes a BattleKit from configuration. It is not recommended to use this manually, as it is
	 * intended for the Bukkit configuration serialization system.
	 * @param serialized map to deserialize.
	 */
	public static BattleKit deserialize(Map<String, Object> serialized) {
		return new BattleKit(serialized);
	}

	/**
	 * Deserializes a BattleKit from configuration. It is not recommended to use this manually, as it is
	 * intended for the Bukkit configuration serialization system.
	 * @param serialized map to deserialize.
	 */
	public static BattleKit valueOf(Map<String, Object> serialized) {
		return new BattleKit(serialized);
	}

	public String getFancyName() {
		if(icon != null && icon.hasItemMeta() && icon.getItemMeta().hasDisplayName()) {
			return icon.getItemMeta().getDisplayName();
		}
		return name;
	}

	public boolean isSimilar(BattleKit kit) {
		if(!kit.getInventory().equals(getInventory())) {
			return false;
		}
		if((kit.getHelmet() == null && getHelmet() != null) || getHelmet() == null && kit.getHelmet() != null) {
			return false;
		}
		if((kit.getChestplate() == null && getChestplate() != null) || getChestplate() == null && kit.getChestplate() != null) {
			return false;
		}
		if((kit.getLeggings() == null && getLeggings() != null) || getLeggings() == null && kit.getLeggings() != null) {
			return false;
		}
		if((kit.getBoots() == null && getBoots() != null) || getBoots() == null && kit.getBoots() != null) {
			return false;
		}
		if(!kit.getHelmet().equals(getHelmet()) || !kit.getChestplate().equals(getChestplate())
				|| !kit.getLeggings().equals(getLeggings()) || !kit.getBoots().equals(getBoots())) {
			return false;
		}
		if(kit.getPotions().size() != getPotions().size()) {
			return false;
		}
		for(PotionEffect ef : getPotions()) {
			boolean bothHave = false;
			for(PotionEffect ef2 : kit.getPotions()) {
				if(ef.equals(ef2)) {
					bothHave = true;
				}
			}
			if(!bothHave) return false;
		}
		return kit.isBuild() == isBuild() && kit.isCombo() == isCombo() && kit.isElo() == isElo()
				&& kit.isNoHunger() == isNoHunger() && kit.isHorse() == isHorse() && kit.isOnlyBow() == isOnlyBow()
				&& kit.isStickSpawn() == isStickSpawn() && kit.isBedwars() == isBedwars() && kit.isHealthbar() == isHealthbar() && kit.isAnticheatProtected() == isAnticheatProtected()
				&& kit.isChestAccess() == isChestAccess() && kit.isEditable() == isEditable();
	}

}
