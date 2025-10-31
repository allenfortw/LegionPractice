package tech.hexadevelopment.practice.arena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.utils.SerializableLocation;
import tech.hexadevelopment.practice.utils.world.BuildWorldDelete;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.botduel.BotDuel;
import tech.hexadevelopment.practice.fights.duel.BestOf;
import tech.hexadevelopment.practice.fights.duel.Duel;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.LegionPractice;

/**
 * The arena system of the LegionPractice.
 * @author Toppe
 * @since 0.1
 */
@SerializableAs("Arena")
public class Arena implements ConfigurationSerializable{

	public static int maxChangesPerTick = 5;
	public static int maxChecksPerTick = 300;
	public static boolean replaceDirtWithGrass;
	public static boolean noFightsInArenasWorld;
	public static boolean lagBroadcast;
	public static boolean debug;
	public static int arenaWorldsNumber;
	private static boolean smartArenas;

	private int customMaxChangesPerTick;
	private int customMaxChecksPerTick;
	private Location loc1;
	private Location loc2;
	private Location center;
	private Location corner1;
	private Location corner2;
	private String name;
	private String displayName;
	private boolean using;
	private boolean build;
	private boolean needsRollback;
	private boolean ffa;
	private List<String> kits = new ArrayList<String>();
	private List<Location> wallBlocks = new ArrayList<Location>();
	private Fight currentFight;
	public static BattleKit needsMoreArenas;
	private static long createdMoreArenas;
	private static int createdMore;

	public static void load(LegionPractice plugin) {
		maxChangesPerTick = plugin.getConfig().getInt("max-block-changes-per-tick-per-arena");
		maxChecksPerTick = plugin.getConfig().getInt("max-block-checks-per-tick-per-arena");
		smartArenas = plugin.getConfig().getBoolean("smart-arenas");
	}

	public Arena(String name) {
		this.name = name;
	}

	/**
	 * Copies an arena with differrent world and name
	 * The arena won't be added to LegionPractice automatically.
	 * @param arena arena to copy.
	 * @param w the world for the new arena
	 * @param name the name of the new arena
	 * @return a new arena with the given name and locations set in the given world.
	 */
	public static Arena getCopy(Arena arena, World w, String name) {
		try{
			Arena ar = new Arena(name);
			Location l1 = arena.getLoc1().clone();
			l1.setWorld(w);
			ar.setLoc1(l1);
			if(arena.getCorner1() != null) {
				Location c1 = arena.getCorner1().clone();
				c1.setWorld(w);
				ar.setCorner1(c1);
			}
			if(arena.getCorner2() != null) {
				Location c2 = arena.getCorner2().clone();
				c2.setWorld(w);
				ar.setCorner2(c2);
			}
			ar.setCustomMaxChangesPerTick(arena.getCustomMaxChangesPerTick());
			ar.setCustomMaxChecksPerTick(arena.getCustomMaxChecksPerTick());
			Location l2 = arena.getLoc2().clone();
			l2.setWorld(w);
			ar.setLoc2(l2);
			ar.setBuild(arena.isBuild());
			Location c = arena.getCenter().clone();
			c.setWorld(w);
			ar.setCenter(c);
			ar.setFFA(arena.isFFA());
			ar.setKits(arena.getKits());
			Location loc = ar.getCenter();
			ar.setDisplayName(arena.getDisplayName());
			if(w == null || ar.getCenter() == null || ar.getLoc1() == null || ar.getLoc2() == null
					|| !ar.getCenter().getWorld().getName().equals(w.getName()) || !ar.getLoc1().getWorld().getName().equals(w.getName())
					|| !ar.getLoc1().getWorld().getName().equals(w.getName()) || loc == null || loc.getWorld() == null || Bukkit.getWorld(loc.getWorld().getName()) == null) return null;
			return ar;
		}catch(Exception e) {}
		return null;
	}

	/**
	 * Deserializes an arena from configuration. It is not recommended to use this manually, as it is
	 * intended for the Bukkit configuration serialization system.
	 * @param serialized map to deserialize.
	 */
	public Arena(Map<String, Object> serialized) {
		if (serialized == null) return;
		if (serialized.isEmpty()) return;
		if(serialized.containsKey("name") 
				&& serialized.get("name") instanceof String) { 
			name = (String) serialized.get("name");
		}
		if(serialized.containsKey("display-name") 
				&& serialized.get("display-name") instanceof String) { 
			displayName = ChatColor.translateAlternateColorCodes('&', (String) serialized.get("display-name"));
		}
		if(serialized.containsKey("loc1") 
				&& serialized.get("loc1")instanceof SerializableLocation) {
			loc1 = ((SerializableLocation) serialized.get("loc1")).toLocation();
		}
		if(serialized.containsKey("loc2") 
				&& serialized.get("loc2") instanceof SerializableLocation) {
			loc2 = ((SerializableLocation) serialized.get("loc2")).toLocation();
		}
		if(serialized.containsKey("corner1") 
				&& serialized.get("corner1")instanceof SerializableLocation) {
			corner1 = ((SerializableLocation) serialized.get("corner1")).toLocation();
		}
		if(serialized.containsKey("corner2") 
				&& serialized.get("corner2") instanceof SerializableLocation) {
			corner2 = ((SerializableLocation) serialized.get("corner2")).toLocation();
		}
		if(serialized.containsKey("center") 
				&& serialized.get("center") instanceof SerializableLocation) {
			center = ((SerializableLocation) serialized.get("center")).toLocation();
		}
		if(serialized.containsKey("build") 
				&& serialized.get("build") instanceof Boolean) {
			build = (Boolean) serialized.get("build");
		}
		if(serialized.containsKey("ffa") 
				&& serialized.get("ffa") instanceof Boolean) {
			ffa = (Boolean) serialized.get("ffa");
		}
		if(serialized.containsKey("kits") 
				&& serialized.get("kits") instanceof List<?>) {
			for(Object o : (List<?>) serialized.get("kits")) {
				if(o instanceof String) {
					kits.add(((String) o).toLowerCase());
				}
			}
		}
		if(displayName == null) {
			displayName = ChatColor.YELLOW + name.replaceAll("[0-9]", "");
			if(ChatColor.stripColor(displayName).length() == 0) {
				displayName = ChatColor.YELLOW + name;
			}
		}
		LegionPractice plugin = LegionPractice.getInstance();
		//make sure the locations are in the correct world
		//some times arena worlds are/were buggy but this should fix
		//not sure if this is needed anymore
		if(plugin.getConfig().getBoolean("no-arenas-in-default-world")) {
			String def = Bukkit.getWorlds().get(0).getName();
			if(loc1 != null && (loc1.getWorld() == null || loc1.getWorld().getName().equals(def))) {
				loc1.setWorld(Bukkit.getWorld(LegionPractice.stackArenasWorld));
			}
			if(loc2 != null && (loc2.getWorld() == null || loc2.getWorld().getName().equals(def))) {
				loc2.setWorld(Bukkit.getWorld(LegionPractice.stackArenasWorld));
			}
			if(center != null && (center.getWorld() == null || center.getWorld().getName().equals(def))) {
				center.setWorld(Bukkit.getWorld(LegionPractice.stackArenasWorld));
			}
			if(corner1 != null && (corner1.getWorld() == null || corner1.getWorld().getName().equals(def))) {
				corner1.setWorld(Bukkit.getWorld(LegionPractice.stackArenasWorld));
			}
			if(corner2 != null && (corner2.getWorld() == null || corner2.getWorld().getName().equals(def))) {
				corner2.setWorld(Bukkit.getWorld(LegionPractice.stackArenasWorld));
			}
		}
		if(name.toLowerCase().contains("brackets")) {
			customMaxChangesPerTick = plugin.getConfig().getInt("max-block-changes-per-tick-brackets-arena");
			customMaxChecksPerTick = plugin.getConfig().getInt("max-block-checks-per-tick-brackets-arena");
		}
	}

	/**
	 * Serializes an arena to configuration. It is not recommended to use this manually, as it is
	 * intended for the Bukkit configuration serialization system.
	 * @return the serialized map.
	 */
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> serialized = new HashMap<String, Object>();
		LegionPractice plugin = LegionPractice.getInstance();
		//make sure the locations are in the correct world
		//some times arena worlds are/were buggy but this should fix
		//not sure if this is needed anymore
		if(plugin.getConfig().getBoolean("no-arenas-in-default-world")) {
			String def = Bukkit.getWorlds().get(0).getName();
			if(loc1 != null && (loc1.getWorld() == null || loc1.getWorld().getName().equals(def))) {
				loc1.setWorld(Bukkit.getWorld(LegionPractice.stackArenasWorld));
			}
			if(loc2 != null && (loc2.getWorld() == null || loc2.getWorld().getName().equals(def))) {
				loc2.setWorld(Bukkit.getWorld(LegionPractice.stackArenasWorld));
			}
			if(center != null && (center.getWorld() == null || center.getWorld().getName().equals(def))) {
				center.setWorld(Bukkit.getWorld(LegionPractice.stackArenasWorld));
			}
			if(corner1 != null && (corner1.getWorld() == null || corner1.getWorld().getName().equals(def))) {
				corner1.setWorld(Bukkit.getWorld(LegionPractice.stackArenasWorld));
			}
			if(corner2 != null && (corner2.getWorld() == null || corner2.getWorld().getName().equals(def))) {
				corner2.setWorld(Bukkit.getWorld(LegionPractice.stackArenasWorld));
			}
		}
		serialized.put("name", name);
		serialized.put("display-name", displayName);
		serialized.put("loc1", new SerializableLocation(loc1));
		serialized.put("loc2", new SerializableLocation(loc2));
		serialized.put("center", new SerializableLocation(center));
		if(build)serialized.put("build", build);
		if(ffa)serialized.put("ffa", ffa);
		if(!kits.isEmpty())serialized.put("kits", kits);
		if(corner1 != null) {
			serialized.put("corner1", new SerializableLocation(corner1));
		}
		if(corner1 != null) {
			serialized.put("corner2", new SerializableLocation(corner2));
		}
		return serialized;
	}

	/**
	 * Save the arena so LegionPractice can use it.
	 * The arena will be saved to the arenas.yml file when the plugin gets unloaded and the arena will be loaded back when the plugin gets loaded again.
	 */
	public void saveForLegionPractice() {
		LegionPractice.getInstance().arenas.add(this);
	}

	/**
	 * Remove the arena from LegionPractice so it won't be used by LegionPractice anymore
	 * @return true if the arena was known by LegionPractice, false if it wasn't saved for LegionPractice
	 */
	public boolean removeFromLegionPractice() {
		LegionPractice plugin = LegionPractice.getInstance();
		if(plugin.arenas.contains(this)) {
			plugin.arenas.remove(this);
			return true;
		}
		return false;
	}


	public boolean canRollback() {
		return (getCorner1() != null && getCorner2() != null) || CachedBlockChange.CACHE_BLOCKS;
	}

	/**
	 * Finds all empty build arenas from LegionPractice that don't need rollback and aren't currently used.
	 * Ignores arenas in the build world (The build world is copied when making more build arenas and fights should never be in that world.)
	 * @return a list of all build arenas.
	 */
	private static List<Arena> getBuildArenas() {
		List<Arena> ars = new ArrayList<Arena>();
		for(Arena ar : LegionPractice.getInstance().arenas) {
			if(ar.isBuild()) {
				if(!ar.getName().toLowerCase().contains("brackets") && !ar.isFFA() && !ar.getName().equalsIgnoreCase("koth")
						&& !ar.getName().equalsIgnoreCase("sumoevent") && ar.getCenter() != null && ar.getCenter().getWorld() != null) {
					ars.add(ar);
				}
			}
		}
		return ars;
	}

	/**
	 * Finds a random arena that is not currently used.
	 * Tries to make more arenas if it's set so in the config.
	 * Never returns a non-build arena.
	 * @return an arena that's a free build arena.
	 */
	public static Arena findEmptyBuildArena(Player p, BattleKit kit) {
		List<Arena> ars = getBuildArenas();
		List<Arena> arenas = new ArrayList<Arena>();
		boolean kitArenasExist = false;
		BattleKit customKit = LegionPractice.getInstance().getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
		String aWorld = null;
		if(!LegionPractice.getInstance().getConfig().getBoolean("rollback-arenas")) {
			aWorld = LegionPractice.stackArenasWorld;
		}
		for(Arena ar : ars) {
			if((ar.getKits().contains("customkit") && kit.equals(customKit)) || (ar.getKits() != null && ar.getKits().contains(kit.getName().toLowerCase()))) {
				if(ar.getCenter() != null && ar.getLoc1() != null && ar.getLoc2() != null && ar.canRollback() && !ar.isFFA()) {
					kitArenasExist = true;
					if(aWorld == null || !aWorld.equalsIgnoreCase(ar.getCenter().getWorld().getName())) {
						if(!ar.isUsing() && !ar.needsRollback() && !ar.getName().toLowerCase().contains("brackets")
								&& !ar.getName().equalsIgnoreCase("sumoevent") && !ar.isFFA() && !ar.getName().equalsIgnoreCase("koth")) {
							arenas.add(ar);	
						}
					}
				}
			}
		}
		if(!kitArenasExist) {
			for(Arena ar : ars) {
				if(ar.getKits() == null || ar.getKits().size() == 0) {
					if(!ar.isUsing() && ar.getCenter() != null && !ar.getName().toLowerCase().contains("brackets")
							&& !ar.getName().equalsIgnoreCase("sumoevent") && !ar.isFFA() && !ar.getName().equalsIgnoreCase("koth")
							&& ar.getLoc1() != null && ar.getLoc2() != null && (aWorld == null || !aWorld.equalsIgnoreCase(ar.getCenter().getWorld().getName())) && !ar.needsRollback()) {
						arenas.add(ar);	
					}
				}
			}
		}
		Arena arena = recommendedArena(arenas, kit, p, true);
		if(needsMoreArenas != null && needsMoreArenas == kit && arena != null) {
			needsMoreArenas = null;
		}
		if(arena == null) {
			needsMoreArenas = kit;
		}
		return arena;
	}

	private static Arena recommendedArena(List<Arena> arenas, BattleKit kit, Player p, boolean build) {
		if(arenas.size() == 0) {
			if(createdMore < 2 && System.currentTimeMillis()-createdMoreArenas > 1000*60*5 && LegionPractice.getInstance().getConfig().getBoolean("make-arenas-automatically")) {
				createdMore++;
				createdMoreArenas = System.currentTimeMillis();
				return stackArenas(p, kit, build, lagBroadcast);
			}
			return null;
		}
		else if(arenas.size() == 1){
			return arenas.get(0);
		}
		else if(smartArenas){
			HashMap<Integer, Arena> recommendedArenas = new HashMap<Integer, Arena>();
			int atLeast = 5*LegionPractice.getInstance().getConfig().getInt("autostack-arenas-on-enable");
			if(atLeast < 5) atLeast = 5;
			for(Arena a : arenas) {
				if(recommendedArenas.size() < atLeast) {
					recommendedArenas.put(getRecommendation(a, kit), a);
				}
				else {
					int recommendation = getRecommendation(a, kit);
					if(recommendedArenas.entrySet().removeIf(e-> recommendation > e.getKey())) {
						recommendedArenas.put(getRecommendation(a, kit), a);
					}
				}
			}
			arenas.clear();
			arenas.addAll(recommendedArenas.values());
		}
		/*
			Iterator<Arena> it = arenas.iterator();
			while(it.hasNext() && arenas.size() > 1) {
				Arena arena = it.next();
				String name = arena.getName().contains(":") ? arena.getName().split(":")[1] : arena.getName();
				boolean atLeastOneFree = false;
				for(Arena ar : LegionPractice.getInstance().arenas) {
					if(!ar.isUsing() && (ar.getName().endsWith(":" + name) || (ar.getName().equals(name)
							&& !ar.equals(arena)))) {
						atLeastOneFree = true;
					}
				}
				if(!atLeastOneFree) {
					it.remove();
				}
			}
		 */
		if(arenas.size() == 1){
			return arenas.get(0);
		}
		return arenas.get(LegionPractice.random.nextInt(arenas.size()));
	}

	private static int getRecommendation(Arena arena, BattleKit kit) {
		double distance = arena.getLoc1().distanceSquared(arena.getLoc2());
		double recommendedDistance = 96;
		int enderpearls = 0;
		int gapple = 0;
		for(ItemStack is : kit.getInventory()) {
			if(is != null && is.getAmount() > 0) {
				if(is.getType() == Material.ENDER_PEARL) {
					enderpearls += is.getAmount();
				}
				else if(is.getType() == Material.GOLDEN_APPLE && is.getDurability() == 1) {
					gapple += is.getAmount();
				}
			}
		}
		if(enderpearls > 0) {
			recommendedDistance += 32;
			recommendedDistance += 2*(enderpearls > 16 ? 16 : enderpearls);
		}
		if(gapple > 0) {
			recommendedDistance -= enderpearls > 0 ? 32 : 16;
			recommendedDistance -= 2*(gapple > 16 ? 16 : gapple);
		}
		if(kit.isHorse() && recommendedDistance <= 96) {
			recommendedDistance = recommendedDistance*1.25 < 96 ? recommendedDistance* 1.75 : recommendedDistance*1.25;
		}
		int result = (int) (100-Math.abs(recommendedDistance*2-distance));
		if(result > 100) result = 100;
		if(result < 100) result = 0;
		return result;
	}

	public static void stackArenas() {
		stackArenas(null, null, false, false);
	}


	private static Arena stackArenas(Player p, BattleKit kit, boolean findBuild, boolean broadcast) {
		World world = Bukkit.getWorld(LegionPractice.stackArenasWorld);
		if(world == null) return null;
		boolean ex = false;
		if(p != null && kit != null) {
			BattleKit customKit = LegionPractice.getInstance().getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
			for(Arena ar : getArenasInWorld(world, true)) {
				if((ar.getKits() != null && ar.getKits().contains("customkit") && kit.equals(customKit))
						|| (ar.getKits() != null && ar.getKits().contains(kit.getName().toLowerCase()))) {
					ex = true;
				}
			}
		}
		if(!ex) {
			for(Arena ar : getArenasInWorld(world, true)) {
				if((ar.getKits() == null || ar.getKits().size() == 0) || (p == null && kit == null)) {
					ex = true;
				}
			}
		}
		if(ex) {
			arenaWorldsNumber++;
			if(broadcast) {
				for(Player pl : Bukkit.getOnlinePlayers()) {
					pl.sendMessage(LegionPractice.getInstance().translateMessage(pl, "lag-broadcast"));
				}
			}
			String newName = world.getName() + "_" + arenaWorldsNumber;
			LegionPractice.getInstance().getWorldStacker().stack(world, newName);
			BuildWorldDelete.worldsCreated.add(newName);
			if(p != null && kit != null) {
				if(findBuild) return findEmptyBuildArena(p, kit);
				else return findEmptyArena(p, kit);
			}
		}
		return null;
	}

	public void rollbackArena(Fight fight) {
		HashSet<Material> materials = fight.getKit().getBlocks();
		if(!build) {
			setNeedsRollback(false);
			return;
		}
		boolean bestOf = false;
		if(fight instanceof Duel) {
			bestOf = ((Duel) fight).breaktime;
		}
		else if(fight instanceof BotDuel) {
			bestOf = ((BotDuel) fight).breaktime;
		}
		needsRollback = true;
		if(LegionPractice.disabling) {
			quickRollback();
			return;
		}
		AutomaticRollbackRate.rollbacksSinceLast++;
		try{
			int maxChanges1;
			if(this.customMaxChangesPerTick > 0) {
				maxChanges1 = this.customMaxChangesPerTick;
			}
			else {
				if(Arena.maxChangesPerTick <= 0) {
					Arena.maxChangesPerTick = 5;
					Bukkit.getLogger().warning("max-block-changes-per-tick-per-arena config value is 0. Using 5!");
				}
				maxChanges1 = Arena.maxChangesPerTick;
			}
			int maxChecks;
			if(this.customMaxChecksPerTick > 0) {
				maxChecks = this.customMaxChecksPerTick;
			}
			else {
				if(Arena.maxChecksPerTick <= 0) {
					Arena.maxChecksPerTick = 300;
					Bukkit.getLogger().warning("max-block-checks-per-tick-per-arena config value is 0. Using 300!");
				}
				maxChecks = Arena.maxChecksPerTick;
			}
			if(bestOf) {
				//try to rollback best of x matches before next round
				maxChanges1 = fight.getBlockChanges().size()/(BestOf.TICKS_BEFORE_NEXT_ROUND-1);
				if(maxChanges1*3 > Arena.maxChangesPerTick) maxChanges1 = Arena.maxChangesPerTick*3;
			}
			boolean allowSlow = LegionPractice.performanceMode && !bestOf;
			int maxChanges = maxChanges1;
			if(CachedBlockChange.CACHE_BLOCKS) {
				if(debug) {
					for(Player p : Bukkit.getOnlinePlayers()) {
						if(p.hasMetadata("LegionPracticeRollbackRateAlerts")) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', LegionPractice.getInstance().getConfig().getString("prefix")) + ChatColor.GRAY + "Rolling back the arena '" + name + "' (blocks to change: " + fight.getBlockChanges().size() + ")");
						}
					}
				}
				Iterator<CachedBlockChange> iterator = new HashSet<CachedBlockChange>(fight.getBlockChanges()).iterator();
				fight.getBlockChanges().clear();
				List<CachedBlockChange> dirtToGrassLater = new ArrayList<CachedBlockChange>();
				new BukkitRunnable() {

					@Override
					public void run() {
						int changeCounter = 0;
						int checkCounter = 0;
						try{
							while(iterator.hasNext()) {
								if(changeCounter < maxChanges && checkCounter < maxChecks) {
									CachedBlockChange l = iterator.next();
									if(l != null) {
										if(l.getOldMaterial() == Material.GRASS || l.getOldMaterial() == Material.MYCEL
												|| (l.getOldMaterial() == Material.DIRT && l.getOldData() == 2)) {
											dirtToGrassLater.add(l);
										}
										else {
											changeCounter++;
											checkCounter++;
											l.reset();
											Block b = l.getLocation().getBlock();
											b.removeMetadata(RollbackListener.PLACED_IN_FIGHT, LegionPractice.getInstance());
										}
									}
									iterator.remove();
								}
								else return;
							}
						}catch(Exception e) {
							this.cancel();
							setNeedsRollback(false);
							for(Player p : Bukkit.getOnlinePlayers()) {
								if(PermissionsManager.hasPermission(p, Permission.ADMIN)) {
									p.sendMessage(ChatColor.translateAlternateColorCodes('&', LegionPractice.getInstance().getConfig().getString("prefix")) + ChatColor.GRAY + "Failed to rollback '" + name + "'.");
								}
							}
							e.printStackTrace();
						}
						this.cancel();
						new BukkitRunnable() {

							Iterator<CachedBlockChange> iterator = dirtToGrassLater.iterator();

							@Override
							public void run() {
								int changeCounter = 0;
								int checkCounter = 0;
								try{
									while(iterator.hasNext()) {
										if(changeCounter < maxChanges && checkCounter < maxChecks) {
											CachedBlockChange l = iterator.next();
											if(l != null) {
												changeCounter++;
												checkCounter++;
												l.reset();
												Block b = l.getLocation().getBlock();
												b.removeMetadata(RollbackListener.PLACED_IN_FIGHT, LegionPractice.getInstance());
											}
											iterator.remove();
										}
										else return;
									}
								}catch(Exception e) {
									this.cancel();
									setNeedsRollback(false);
									for(Player p : Bukkit.getOnlinePlayers()) {
										if(PermissionsManager.hasPermission(p, Permission.ADMIN)) {
											p.sendMessage(ChatColor.translateAlternateColorCodes('&', LegionPractice.getInstance().getConfig().getString("prefix")) + ChatColor.GRAY + "Failed to rollback '" + name + "'.");
										}
									}
									e.printStackTrace();
								}
								this.cancel();
								setNeedsRollback(false);
							}
						}.runTaskTimer(LegionPractice.getInstance(), 0, allowSlow ? 5 : 1);
					}
				}.runTaskTimer(LegionPractice.getInstance(), 0, allowSlow ? 5 : 1);
			}
			else {
				rollbackArena(materials, LegionPractice.getInstance().getConfig().getBoolean("rollback-only-blocks-placed-in-fight"), false);
			}
		}catch(Exception e) {
			e.printStackTrace();
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(PermissionsManager.hasPermission(p, Permission.ADMIN)) {
					p.sendMessage(ChatColor.RED + "Failed to reset arena '" + name + "'. Check console for the error!");
				}
			}
		}
	}

	public void quickRollback() {
		Fight fight = getCurrentFight();
		if(fight == null || fight.getKit() == null) return;
		if(!build) {
			setNeedsRollback(false);
			return;
		}
		needsRollback = true;
		try{
			if(CachedBlockChange.CACHE_BLOCKS) {
				if(debug) {
					for(Player p : Bukkit.getOnlinePlayers()) {
						if(p.hasMetadata("LegionPracticeRollbackRateAlerts")) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', LegionPractice.getInstance().getConfig().getString("prefix")) + ChatColor.GRAY + "Rolling back the arena '" + name + "' (blocks to change: " + fight.getBlockChanges().size() + ")");
						}
					}
				}
				Iterator<CachedBlockChange> iterator = new HashSet<CachedBlockChange>(fight.getBlockChanges()).iterator();
				fight.getBlockChanges().clear();
				while(iterator.hasNext()) {
					CachedBlockChange l = iterator.next();
					l.reset();
					Block b = l.getLocation().getBlock();
					b.removeMetadata(RollbackListener.PLACED_IN_FIGHT, LegionPractice.getInstance());
					iterator.remove();
				}
				Arena.this.needsRollback = false;
				if(debug) {
					for(Player p : Bukkit.getOnlinePlayers()) {
						if(p.hasMetadata("LegionPracticeRollbackRateAlerts")) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', LegionPractice.getInstance().getConfig().getString("prefix")) + ChatColor.GRAY + "The arena '" + name + "' has been rolled back.");
						}
					}
				}
				setNeedsRollback(false);
				if(debug) {
					for(Player p : Bukkit.getOnlinePlayers()) {
						if(p.hasMetadata("LegionPracticeRollbackRateAlerts")) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', LegionPractice.getInstance().getConfig().getString("prefix")) + ChatColor.GRAY + "The arena '" + name + "' has been rolled back.");
						}
					}
				}
			}
			else if(corner1 != null && corner2 != null){
				if(debug) {
					for(Player p : Bukkit.getOnlinePlayers()) {
						if(p.hasMetadata("LegionPracticeRollbackRateAlerts")) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', LegionPractice.getInstance().getConfig().getString("prefix")) + ChatColor.GRAY + "Rolling back the arena '" + name + "'...");
						}
					}
				}
				int topBlockX = (corner1.getBlockX() < corner2.getBlockX() ? corner2.getBlockX() : corner1.getBlockX());
				int bottomBlockX = (corner1.getBlockX() > corner2.getBlockX() ? corner2.getBlockX() : corner1.getBlockX());
				int topBlockY = (corner1.getBlockY() < corner2.getBlockY() ? corner2.getBlockY() : corner1.getBlockY());
				int bottomBlockY = (corner1.getBlockY() > corner2.getBlockY() ? corner2.getBlockY() : corner1.getBlockY());
				int topBlockZ = (corner1.getBlockZ() < corner2.getBlockZ() ? corner2.getBlockZ() : corner1.getBlockZ());
				int bottomBlockZ = (corner1.getBlockZ() > corner2.getBlockZ() ? corner2.getBlockZ() : corner1.getBlockZ());
				World world = loc1.getWorld();
				Location l = null;
				for(int x = bottomBlockX; x <= topBlockX; x++){
					for(int y = bottomBlockY; y <= topBlockY; y++){
						for(int z = bottomBlockZ; z <= topBlockZ; z++){
							l = new Location(world, x, y, z);
							l.getBlock().setType(Material.AIR);
						}
					}
				}
				setNeedsRollback(false);
				if(debug) {
					for(Player p : Bukkit.getOnlinePlayers()) {
						if(p.hasMetadata("LegionPracticeRollbackRateAlerts")) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', LegionPractice.getInstance().getConfig().getString("prefix")) + ChatColor.GRAY + "The arena '" + name + "' has been rolled back.");
						}
					}
				}
			}
			else {
				for(Player p : Bukkit.getOnlinePlayers()) {
					if(PermissionsManager.hasPermission(p, Permission.ADMIN)) {
						p.sendMessage(ChatColor.RED + "Failed to reset arena '" + name + "'. Corners are not valid and cache-rollback-blocks is not true!");
					}
				}
				Bukkit.getLogger().warning(ChatColor.RED + "Failed to reset arena '" + name + "'. Corners are not valid and cache-rollback-blocks is not true!");
				return;
			}
		}catch(Exception e) {
			e.printStackTrace();
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(PermissionsManager.hasPermission(p, Permission.ADMIN)) {
					p.sendMessage(ChatColor.RED + "Error: Failed to reset arena '" + name + "'. Check console for the error!");
				}
			}
		}
	}

	public static Arena getArena(String name) {
		for(Arena a : LegionPractice.getInstance().arenas) {
			if(a.getName().equals(name)) {
				return a;
			}
		}
		return null;
	}

	public void rollbackArena(HashSet<Material> materials) {
		rollbackArena(materials, false, false);
	}

	public boolean removeWall(int blocksPerTick) {
		if(wallBlocks.isEmpty()) return false;
		Iterator<Location> iterator = wallBlocks.iterator();
		HashSet<CachedBlockChange> caches = currentFight != null ? currentFight.getBlockChanges() : null;
		new BukkitRunnable() {

			@Override
			public void run() {
				int changeCounter = 0;
				while(iterator.hasNext()) {
					if(wallBlocks.size() == 0) {
						Arena.this.needsRollback = false;
						this.cancel();
					}
					else if(changeCounter < blocksPerTick) {
						Location l = iterator.next();
						if(caches == null) {
							wallBlocks.remove(l);
							l.getBlock().setType(Material.AIR);
							changeCounter++;
							iterator.remove();
						}
						else {
							CachedBlockChange cached = CachedBlockChange.getByLocation(l, caches);
							if(cached != null) {
								wallBlocks.remove(l);
								cached.reset();
								changeCounter++;
								iterator.remove();
							}
						}
					}
					else return;
				}
				this.cancel();
			}
		}.runTaskTimer(LegionPractice.getInstance(), 1, 1);
		return caches != null;
	}

	public boolean buildWall(Material material, int blocksPerTick) {
		if(center == null || corner1 == null || corner2 == null) return false;
		List<Location> locations = new ArrayList<Location>();
		int lowestY = corner1.getBlockY() < corner2.getBlockY() ? corner1.getBlockY() : corner2.getBlockY();
		int highestY = corner1.getBlockY() > corner2.getBlockY() ? corner1.getBlockY() : corner2.getBlockY();
		int lowestZ = corner1.getBlockZ() < corner2.getBlockZ() ? corner1.getBlockZ() : corner2.getBlockZ();
		int highestZ = corner1.getBlockZ() > corner2.getBlockZ() ? corner1.getBlockZ() : corner2.getBlockZ();
		int lowestX = corner1.getBlockX() < corner2.getBlockX() ? corner1.getBlockX() : corner2.getBlockX();
		int highestX = corner1.getBlockX() > corner2.getBlockX() ? corner1.getBlockX() : corner2.getBlockX();
		for(int y = lowestY; y < highestY; y++) {
			for(int x = lowestX; x < highestX; x++) {
				for(int z = lowestZ; z < highestZ; z++) {
					locations.add(new Location(center.getWorld(), x, y, z));
				}
			}
		}
		Iterator<Location> iterator = locations.iterator();
		new BukkitRunnable() {

			@Override
			public void run() {
				int changeCounter = 0;
				while(iterator.hasNext()) {
					if(locations.size() == 0) {
						Arena.this.needsRollback = false;
						this.cancel();
					}
					else if(changeCounter < blocksPerTick) {
						Location l = iterator.next();
						Block b = l.getBlock();
						if(b.getType() == null || !b.getType().isBlock()) {
							wallBlocks.add(b.getLocation());
							if(currentFight != null) {
								currentFight.getBlockChanges().add(new CachedBlockChange(b.getLocation(), b));
							}
							b.setType(material);
							changeCounter++;
							if(replaceDirtWithGrass) {
								Block b2 = b.getLocation().clone().add(0, -1, 0).getBlock();
								if(b2.getType() == Material.DIRT) {
									wallBlocks.add(b.getLocation());
									if(currentFight != null) {
										currentFight.getBlockChanges().add(new CachedBlockChange(b2.getLocation(), b));
									}
									b2.setType(Material.GRASS);
									changeCounter++;
								}
							}
							iterator.remove();
						}
					}
					else return;
				}
				this.cancel();
			}
		}.runTaskTimer(LegionPractice.getInstance(), 1, 1);
		return true;
	}

	public void rollbackArena(HashSet<Material> materials, boolean onlyFightMeta, boolean fastLiquid) {
		if(corner1 == null || corner2 == null) {
			setNeedsRollback(false);
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(PermissionsManager.hasPermission(p, Permission.ADMIN)) {
					p.sendMessage(ChatColor.RED + "Failed to reset arena '" + name + "'. Corners are not valid and cache-rollback-blocks is not true!");
				}
			}
			Bukkit.getLogger().warning(ChatColor.RED + "Failed to reset arena '" + name + "'. Corners are not valid and cache-rollback-blocks is not true!");
			return;
		}
		try{
			int maxChanges;
			if(this.customMaxChangesPerTick > 0) {
				maxChanges = this.customMaxChangesPerTick;
			}
			else {
				if(Arena.maxChangesPerTick <= 0) {
					Arena.maxChangesPerTick = 10;
					Bukkit.getLogger().warning("max-block-changes-per-tick-per-arena config value is 0. Using 10!");
				}
				maxChanges = Arena.maxChangesPerTick;
			}
			int maxChecks;
			if(this.customMaxChecksPerTick > 0) {
				maxChecks = this.customMaxChecksPerTick;
			}
			else {
				if(Arena.maxChecksPerTick <= 0) {
					Arena.maxChecksPerTick = 200;
					Bukkit.getLogger().warning("max-block-checks-per-tick-per-arena config value is 0. Using 200!");
				}
				maxChecks = Arena.maxChecksPerTick;
			}
			int topBlockX = (corner1.getBlockX() < corner2.getBlockX() ? corner2.getBlockX() : corner1.getBlockX());
			int bottomBlockX = (corner1.getBlockX() > corner2.getBlockX() ? corner2.getBlockX() : corner1.getBlockX());
			int topBlockY = (corner1.getBlockY() < corner2.getBlockY() ? corner2.getBlockY() : corner1.getBlockY());
			int bottomBlockY = (corner1.getBlockY() > corner2.getBlockY() ? corner2.getBlockY() : corner1.getBlockY());
			int topBlockZ = (corner1.getBlockZ() < corner2.getBlockZ() ? corner2.getBlockZ() : corner1.getBlockZ());
			int bottomBlockZ = (corner1.getBlockZ() > corner2.getBlockZ() ? corner2.getBlockZ() : corner1.getBlockZ());
			World world = loc1.getWorld();
			HashSet<Location> locs = new HashSet<Location>();
			Location l = null;
			if(fastLiquid) {
				for(int x = bottomBlockX; x <= topBlockX; x++){
					for(int y = bottomBlockY; y <= topBlockY; y++){
						for(int z = bottomBlockZ; z <= topBlockZ; z++){
							l = new Location(world, x, y, z);
							if(l.getBlock().isLiquid()) {
								l.getBlock().setType(Material.AIR);
							}
							else {
								locs.add(l);
							}
						}
					}
				}
			}
			Bukkit.getScheduler().runTaskAsynchronously(LegionPractice.getInstance(), new Runnable() {

				@Override
				public void run() {
					if(!fastLiquid) {
						Location l = null;
						for(int x = bottomBlockX; x <= topBlockX; x++){
							for(int y = bottomBlockY; y <= topBlockY; y++){
								for(int z = bottomBlockZ; z <= topBlockZ; z++){
									l = new Location(world, x, y, z);
									locs.add(l);
								}
							}
						}
					}
					Bukkit.getScheduler().runTask(LegionPractice.getInstance(), new Runnable() {

						@Override
						public void run() {
							if(debug) {
								for(Player p : Bukkit.getOnlinePlayers()) {
									if(p.hasMetadata("LegionPracticeRollbackRateAlerts")) {
										p.sendMessage(ChatColor.translateAlternateColorCodes('&', LegionPractice.getInstance().getConfig().getString("prefix")) + ChatColor.GRAY + "Rolling back the arena '" + name + "' (blocks to check: " + locs.size() + ")");
									}
								}
							}
							Iterator<Location> iterator = locs.iterator();
							new BukkitRunnable() {

								@Override
								public void run() {
									int changeCounter = 0;
									int checkCounter = 0;
									while(iterator.hasNext()) {
										if(locs.size() == 0) {
											Arena.this.needsRollback = false;
											this.cancel();
										}
										else if(changeCounter < maxChanges && checkCounter < maxChecks) {
											Location l = iterator.next();
											Block b = l.getBlock();
											if(materials.contains(b.getType())) {
												b.setType(Material.AIR);
												changeCounter++;
												if(replaceDirtWithGrass) {
													Block b2 = b.getLocation().clone().add(0, -1, 0).getBlock();
													if(b2.getType() == Material.DIRT) {
														b2.setType(Material.GRASS);
														changeCounter++;
													}
												}
												b.removeMetadata(RollbackListener.PLACED_IN_FIGHT, LegionPractice.getInstance());
											}
											checkCounter++;
											iterator.remove();
										}
										else return;
									}
									setNeedsRollback(false);
									this.cancel();
									if(debug) {
										for(Player p : Bukkit.getOnlinePlayers()) {
											if(p.hasMetadata("LegionPracticeRollbackRateAlerts")) {
												p.sendMessage(ChatColor.translateAlternateColorCodes('&', LegionPractice.getInstance().getConfig().getString("prefix")) + ChatColor.GRAY + "The arena '" + name + "' has been rolled back.");
											}
										}
									}
								}
							}.runTaskTimer(LegionPractice.getInstance(), 0, LegionPractice.performanceMode ? 3 : 1);
						}
					});
				}
			});
		}catch(Exception e) {
			e.printStackTrace();
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(PermissionsManager.hasPermission(p, Permission.ADMIN)) {
					p.sendMessage(ChatColor.RED + "Failed to reset arena '" + name + "'. Check console for the error!");
				}
			}
		}
	}

	/**
	 * Finds all build arenas.
	 * The list includes arenas in the build world (The build world is copied when making more build arenas)
	 * @return a list of all current build arenas.
	 */
	public static List<Arena> getAllBuildArenas() {
		List<Arena> ars = new ArrayList<Arena>();
		for(Arena ar : LegionPractice.getInstance().arenas) {
			if(ar.isBuild()) {
				ars.add(ar);
			}
		}
		return ars;
	}

	/**
	 * Gets all arenas in the given world.
	 * @param world the world of the arenas.
	 * @param build true for only search for build arenas, false for only not build arenas.
	 * @return a set of all arenas in the world.
	 */
	public static HashSet<Arena> getArenasInWorld(World world, boolean build) {
		HashSet<Arena> ars = new HashSet<Arena>();
		for(Arena ar : LegionPractice.getInstance().arenas) {
			if(ar.getCenter() != null && ar.getLoc1() != null && ar.getLoc2() != null && ar.getCenter().getWorld() != null) {
				if(ar.getCenter().getWorld().getName().equals(world.getName())) {
					if((build && ar.isBuild()) || (!build && !ar.isBuild())) {
						ars.add(ar);
					}
				}
			}
		}
		return ars;
	}

	/**
	 * Finds a random arena that is not currently used.
	 * Tries to make more arenas if it's set so in the config.
	 * Never returns a build arena.
	 * @return an arena that's free and not a build arena.
	 */
	public static Arena findEmptyArena(Player p, BattleKit kit) {
		HashSet<Arena> ars = LegionPractice.getInstance().arenas;
		List<Arena> arenas = new ArrayList<Arena>();
		boolean kitArenasExist = false;
		BattleKit customKit = LegionPractice.getInstance().getPlayerKitsHandler().getPlayerKits(p).getCustomKit();
		for(Arena ar : ars) {
			if((ar.getKits().contains("customkit") && kit.equals(customKit))
					|| (ar.getKits() != null && ar.getKits().contains(kit.getName().toLowerCase()))) {
				if(ar.getCenter() != null && ar.getLoc1() != null && ar.getLoc2() != null && !ar.isFFA()) {
					kitArenasExist = true;
					Location loc = ar.getCenter();
					if(!ar.isUsing() && !ar.isBuild() && !ar.getName().toLowerCase().contains("brackets")
							&& !ar.getName().toLowerCase().contains("sumoevent") && !ar.isFFA() && !ar.getName().equalsIgnoreCase("koth") && loc != null && loc.getWorld() != null && Bukkit.getWorld(loc.getWorld().getName()) != null) {
						arenas.add(ar);	
					}
				}
			}
		}
		if(!kitArenasExist) {
			for(Arena ar : ars) {
				if(ar.getKits() == null || ar.getKits().size() == 0) {
					if(!ar.isUsing() && !ar.isBuild() && !ar.getName().toLowerCase().contains("brackets")
							&& !ar.getName().toLowerCase().contains("sumoevent") && !ar.isFFA() && !ar.getName().equalsIgnoreCase("koth")) {
						arenas.add(ar);	
					}
				}
			}
		}
		Arena arena = recommendedArena(arenas, kit, p, false);
		if(needsMoreArenas != null && needsMoreArenas == kit && arena != null) {
			needsMoreArenas = null;
		}
		if(arena == null) {
			needsMoreArenas = kit;
		}
		return arena;
	}

	/**
	 * @param using true for using, false for not using.
	 */
	public void setUsing(boolean using, Fight fight) {
		if(!using && getCurrentFight() != null && getCurrentFight().getKit() != null) {
			if(getCurrentFight().getKit().isBuild()) {
				rollbackArena(getCurrentFight());
			}
			else setNeedsRollback(false);
		}
		else {
			currentFight = fight;
		}
		this.using = using;
		if(fight != null && build) {
			currentFight = fight;
			setNeedsRollback(true);
		}
	}

	/**
	 * Gets whether this arena is used or not.
	 * @return true if the arena is used, false if it's not.
	 */
	public boolean isUsing() {
		return using;
	}

	/**
	 * Gets whether this arena is a build arena or not.
	 * @return true if the arena is a build arena, false if it's not.
	 */
	public boolean isBuild() {
		return build;
	}

	/**
	 * @return the ffa
	 */
	public boolean isFFA() {
		return ffa;
	}

	/**
	 * @param ffa the ffa to set
	 */
	public void setFFA(boolean ffa) {
		this.ffa = ffa;
	}

	/**
	 * Gets whether this arena needs to be restored or not.
	 * If this arena won't be rolled back after a fight it might be destroyed.
	 * @return true if this arena needs to be restored, false if it doesn't.
	 */
	public boolean needsRollback() {
		return needsRollback;
	}

	/**
	 * @param build true for build arena, false for not build arena.
	 */
	public void setBuild(boolean build) {
		this.build = build;
	}

	/**
	 * Gets the first location of this arena.
	 * @return location of the first spawn of this arena.
	 */

	public Location getLoc1() {
		return loc1;
	}

	/**
	 * Gets the second location of this arena.
	 * @return location of the second spawn of this arena.
	 */
	public Location getLoc2() {
		return loc2;
	}

	/**
	 * Gets the center of this arena.
	 * @return location of the center spawn of this arena.
	 */
	public Location getCenter() {
		return center;
	}
	
	public boolean hasWall() {
		return !wallBlocks.isEmpty();
	}

	/**
	 * @return the kits
	 */
	public List<String> getKits() {
		return kits;
	}

	public void setUsing(boolean using) {
		this.using = using;
	}

	/**
	 * @param kits the kits to set
	 */
	public void setKits(List<String> kits) {
		this.kits = kits;
	}

	/**
	 * Gets the name of this arena.
	 * @return name of this arena.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName != null ? displayName : name;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Sets the first spawn of this arena. For example 1v1's use this location.
	 * @param loc1 Location that will be the first spawn.
	 */
	public void setLoc1(Location loc1) {
		this.loc1 = loc1;
	}

	/**
	 * Sets the second spawn of this arena. For example 1v1's use this location.
	 * @param loc2 Location that will be the second spawn.
	 */
	public void setLoc2(Location loc2) {
		this.loc2 = loc2;
	}

	public void setCorner1(Location corner1) {
		this.corner1 = corner1;
	}

	public void setCorner2(Location corner2) {
		this.corner2 = corner2;
	}

	public Location getCorner1() {
		return corner1;
	}

	public Location getCorner2() {
		return corner2;
	}

	public void setCurrentFight(Fight currentFight) {
		this.currentFight = currentFight;
	}

	/**
	 * Sets the center spawn of this arena. For example Party FFA will use this spawn.
	 * @param center Location that will be the center spawn.
	 */
	public void setCenter(Location centre) {
		this.center = centre;
	}

	/**
	 * @return the currentFight
	 */
	public Fight getCurrentFight() {
		return currentFight;
	}


	/**
	 * @param needsRollback true if it needs, false if it doesn't.
	 */
	private void setNeedsRollback(boolean needsRollback) {
		if(!LegionPractice.getInstance().getConfig().getBoolean("rollback-arenas")) {
			if(needsRollback) {
				this.needsRollback = needsRollback;
			}
			return;
		}
		this.needsRollback = needsRollback;
		if(LegionPractice.getInstance().getFileManager().rollbackOnCrash) {
			if(needsRollback && build && getCurrentFight() != null && getCurrentFight().getKit() != null) {
				LegionPractice.getInstance().getFileManager().saveRollbackKit(this, getCurrentFight().getKit());
			}
			else if(!needsRollback && build) {
				LegionPractice.getInstance().getFileManager().removeRollbackKit(this);
			}
		}
	}

	/**
	 * Deserializes an arena from configuration. It is not recommended to use this manually, as it is
	 * intended for the Bukkit configuration serialization system.
	 * @param serialized map to deserialize.
	 */
	public static Arena deserialize(Map<String, Object> serialized) {
		return new Arena(serialized);
	}

	public int getCustomMaxChangesPerTick() {
		return customMaxChangesPerTick;
	}

	public void setCustomMaxChangesPerTick(int customMaxChangePerTick) {
		this.customMaxChangesPerTick = customMaxChangePerTick;
	}

	public int getCustomMaxChecksPerTick() {
		return customMaxChecksPerTick;
	}

	public void setCustomMaxChecksPerTick(int customMaxChecksPerTick) {
		this.customMaxChecksPerTick = customMaxChecksPerTick;
	}

	public void sendPossibleWrongWorldInfo(Player p) {
		if(loc1 != null && loc2 != null && center != null && corner1 != null && corner2 != null && !areInSameWorld(Arrays.asList(loc1, loc2, center, corner1, corner2))) {
			p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "WARNING! All locations are not in the same world!");
			p.sendMessage(ChatColor.RED + "Position1: " + loc1.getWorld().getName());
			p.sendMessage(ChatColor.RED + "Position2: " + loc2.getWorld().getName());
			p.sendMessage(ChatColor.RED + "Center: " + center.getWorld().getName());
			p.sendMessage(ChatColor.RED + "Corner1: " + corner1.getWorld().getName());
			p.sendMessage(ChatColor.RED + "Corner2: " + corner2.getWorld().getName());
		}
		else if(loc1 != null && loc2 != null && center != null && !areInSameWorld(Arrays.asList(loc1, loc2, center))) {
			p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "WARNING! All locations are not in the same world!");
			p.sendMessage(ChatColor.RED + "Position1: " + loc1.getWorld().getName());
			p.sendMessage(ChatColor.RED + "Position2: " + loc2.getWorld().getName());
			p.sendMessage(ChatColor.RED + "Center: " + center.getWorld().getName());
		}
	}

	public void removeItems() {
		try{
			if(getCenter() != null) {
				Chunk centerChunk = getCenter().getChunk();
				for(int x = 0; x < 20; x++) {
					for(int z = 0; z < 20; z++) {
						Chunk c = getCenter().getWorld().getChunkAt(centerChunk.getX()+x-10, centerChunk.getZ()+z-10);
						c.load();
						for(Entity ent : c.getEntities()) {
							if(ent instanceof Item) {
								ent.remove();
							}
						}
					}
				}
			}
		}catch(Exception e) {}
	}

	private boolean areInSameWorld(List<Location> list) {
		if(list.isEmpty()) {
			return true;
		}
		UUID uuid = list.get(0).getWorld().getUID();
		for(Location l : list) {
			if(!l.getWorld().getUID().equals(uuid)) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Deserializes an arena from configuration. It is not recommended to use this manually, as it is
	 * intended for the Bukkit configuration serialization system.
	 * @param serialized map to deserialize.
	 */
	public static Arena valueOf(Map<String, Object> serialized) {
		return new Arena(serialized);
	}

	public static void sendNoArenas(Player p) {
		p.sendMessage(LegionPractice.getInstance().translateMessage(p, "no-arenas-found"));
		if(PermissionsManager.hasPermission(p, Permission.ADMIN)) {
			boolean buildFound = false, nonBuildFound = false;
			for(Arena ar : LegionPractice.getInstance().arenas) {
				if(ar.isBuild()) {
					buildFound = true;
				}
				else {
					nonBuildFound = true;
				}
			}
			if(!buildFound) {
				p.sendMessage(ChatColor.RED + "Hint: There are no build arenas! Use /arena build <arena name>.");
			}
			if(!nonBuildFound) {
				p.sendMessage(ChatColor.RED + "Hint: There are no non-build arenas!");
			}
		}
	}

	public static void intialize(LegionPractice plugin) {
		Arena.maxChangesPerTick = plugin.getConfig().getInt("max-block-changes-per-tick-per-arena");
		Arena.maxChecksPerTick = plugin.getConfig().getInt("max-block-checks-per-tick-per-arena");
		Arena.replaceDirtWithGrass = plugin.getConfig().getBoolean("replace-dirt-with-grass");
		Arena.lagBroadcast = plugin.getConfig().getBoolean("lag-broadcast");
	}

	public static void removeItems(Collection<Arena> arenas) {
		for(Arena ar : arenas) {
			ar.removeItems();
		}
	}

}

