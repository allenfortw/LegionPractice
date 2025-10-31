package tech.hexadevelopment.practice.arena;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.utils.SerializableLocation;
import tech.hexadevelopment.practice.utils.StringUtil;
import tech.hexadevelopment.practice.LegionPractice;

/**
 * Main command for handling arenas of the LegionPractice.
 * @author Toppe5
 * @since 0.1
 */
public class ArenaCommand implements CommandExecutor {


	private LegionPractice plugin;
	private HashMap<UUID, Integer> counts = new HashMap<UUID, Integer>();
	private HashMap<UUID, Long> last = new HashMap<UUID, Long>();

	/**
	 * @param plugin LegionPractice plugin
	 */
	public ArenaCommand(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if(args.length == 0) {
				p.sendMessage(ChatColor.GOLD + "/Battlearena create <arena>");
				p.sendMessage(ChatColor.GOLD + "/Battlearena delete <arena>");
				p.sendMessage(ChatColor.GOLD + "/Battlearena pos1 <arena>");
				p.sendMessage(ChatColor.GOLD + "/Battlearena pos2 <arena>");
				p.sendMessage(ChatColor.GOLD + "/Battlearena center <arena>");
				p.sendMessage(ChatColor.GOLD + "/Battlearena corner1 <arena>");
				p.sendMessage(ChatColor.GOLD + "/Battlearena corner2 <arena>");
				p.sendMessage(ChatColor.GOLD + "/Battlearena build <arena>");
				p.sendMessage(ChatColor.GOLD + "/Battlearena kits <arena> <kit>");
				p.sendMessage(ChatColor.GOLD + "/Battlearena ffa <arena>");
				p.sendMessage(ChatColor.GOLD + "/Battlearena clearkits <arena>");
				p.sendMessage(ChatColor.GOLD + "/Battlearena removekit <arena> <kit>");
				p.sendMessage(ChatColor.GOLD + "/Battlearena teleport <arena>");
				p.sendMessage(ChatColor.GOLD + "/Battlearena info <arena>");
				p.sendMessage(ChatColor.GOLD + "/Battlearena list");
				p.sendMessage(ChatColor.GOLD + "/Battlearena alerts");
				p.sendMessage(ChatColor.GOLD + "/Battlearena displayname <name> <displayname>");
				p.sendMessage(ChatColor.GOLD + "/Battlearena copypaste <name>");
				return true;
			}
			if(args.length == 1) {
				if(args[0].equalsIgnoreCase("all") || args[0].equalsIgnoreCase("list")) {
					if(plugin.arenas.size() == 0) {
						p.sendMessage(ChatColor.RED + "No arenas found!");
						return true;
					}
					sender.sendMessage(ChatColor.GOLD + "Arena List - " + "Total Arenas: " + plugin.arenas.size());
					for(Arena ar : plugin.arenas){
						sender.sendMessage(ChatColor.YELLOW + "- " + ar.getName() + " (Displayname: " + ChatColor.RESET + ar.getDisplayName() + ChatColor.YELLOW + ")");
					}
				}
				else if(args[0].toLowerCase().contains("alerts")) {
					if(p.hasMetadata("LegionPracticeRollbackRateAlerts")) {
						p.removeMetadata("LegionPracticeRollbackRateAlerts", plugin);
						p.sendMessage(ChatColor.RED + "Rollback alerts turned off.");
					}
					else {
						p.setMetadata("LegionPracticeRollbackRateAlerts", new FixedMetadataValue(plugin, true));
						p.sendMessage(ChatColor.RED + "Rollback alerts turned on.");
						Arena.debug = true;
					}
				}
			}
			if(args.length > 1) {
				Arena arena = getArenaByName(args[1], p);
				switch(args[0].toLowerCase()) {
				case "create":
					if(arena != null) {
						p.sendMessage(ChatColor.RED + "That arena already exists!");
						return true;
					}
					if(!StringUtil.isAlphaNumeric(args[1])) {
						p.sendMessage(ChatColor.RED + "The name must be alphanumeric!");
						return true;
					}
					Arena ar = new Arena(args[1].toLowerCase());
					ar.saveForLegionPractice();
					p.sendMessage(ChatColor.BLUE + "Created an arena!");
					break;
				case "delete":
				case "remove":
					if(!isValidArena(arena, args[1], sender)) {
						break;
					}
					arena.removeFromLegionPractice();
					p.sendMessage(ChatColor.RED + "Deleted the arena " + arena.getName() + ".");
					break;
				case "info":
				case "show":
					if(arena == null) {
						p.sendMessage(ChatColor.RED + "That arena does not exist!");	
						break;
					}
					p.sendMessage(ChatColor.GOLD + "Arena Info: " + arena.getName());
					p.sendMessage(ChatColor.YELLOW + "Display name: " + ChatColor.RESET + arena.getDisplayName());
					p.sendMessage(ChatColor.YELLOW + "Pos1: " + (arena.getLoc1() == null ? null : new SerializableLocation(arena.getLoc1()).toReadableString()));
					p.sendMessage(ChatColor.YELLOW + "Pos2: " + (arena.getLoc2() == null ? null : new SerializableLocation(arena.getLoc2()).toReadableString()));
					p.sendMessage(ChatColor.YELLOW + "Center: " + (arena.getCenter() == null ? null : new SerializableLocation(arena.getCenter()).toReadableString()));
					p.sendMessage(ChatColor.YELLOW + "Corner1: " + (arena.getCorner1() == null ? null : new SerializableLocation(arena.getCorner1()).toReadableString()));
					p.sendMessage(ChatColor.YELLOW + "Corner2: " + (arena.getCorner2() == null ? null : new SerializableLocation(arena.getCorner2()).toReadableString()));
					p.sendMessage(ChatColor.YELLOW + "Kits: " + arena.getKits().toString().replace("[", "").replace("]", ""));
					p.sendMessage(ChatColor.YELLOW + "Build: " + arena.isBuild());
					p.sendMessage(ChatColor.YELLOW + "FFA: " + arena.isFFA());
					p.sendMessage(ChatColor.YELLOW + "Available: " + (!arena.isUsing()) + (arena.isUsing() && arena.isBuild() && !plugin.getConfig().getBoolean("build-fights-in-main-arenas-world")
							&& arena.getCenter().getWorld().getName().equals(LegionPractice.stackArenasWorld) ?  " (config.yml has build-fights-in-main-arenas-world: false)" : ""));
					p.sendMessage(ChatColor.YELLOW + "Needs rollback: " + arena.needsRollback());
					p.sendMessage(ChatColor.YELLOW + "Rollback rate: " + (arena.getCustomMaxChangesPerTick() <= 0 ? Arena.maxChangesPerTick : arena.getCustomMaxChangesPerTick()) + " blocks/tick");
					break;
				case "loc1":
				case "pos1":
					if(!isValidArena(arena, args[1], sender)) {
						break;
					}
					if(plugin.getConfig().getBoolean("no-arenas-in-default-world") && !p.getWorld().getName().equals(LegionPractice.stackArenasWorld)) {
						LegionPractice.getInstance();
						p.sendMessage(ChatColor.RED + "Arena creation is only available in the '"
								+ LegionPractice.stackArenasWorld + "' world. If you wish to create arenas in other worlds change 'no-arenas-in-default-world' in the config.yml. You do it at your own risk!");
						break;
					}
					arena.setLoc1(p.getLocation());
					p.sendMessage(ChatColor.BLUE + "You have set the pos1 of the arena " + arena.getName() + ".");
					arena.sendPossibleWrongWorldInfo(p);
					checkLocations(p, arena);
					break;
				case "displayname":
				case "name":
					if(!isValidArena(arena, args[1], sender)) {
						break;
					}
					if(args.length > 2) {
						arena.setDisplayName(ChatColor.translateAlternateColorCodes('&', args[2]));
						p.sendMessage(ChatColor.BLUE + "You have set a new displayname: " + ChatColor.RESET + arena.getDisplayName());
					}
					else p.sendMessage(ChatColor.YELLOW + "/Battlearena displayname <arena> <displayname>");
					break;
				case "loc2":
				case "pos2":
					if(!isValidArena(arena, args[1], sender)) {
						break;
					}
					if(plugin.getConfig().getBoolean("no-arenas-in-default-world") && !p.getWorld().getName().equals(LegionPractice.stackArenasWorld)) {
						LegionPractice.getInstance();
						p.sendMessage(ChatColor.RED + "Arena creation is only available in the '"
								+ LegionPractice.stackArenasWorld + "' world. If you wish to create arenas in other worlds change 'no-arenas-in-default-world' in the config.yml. You do it at your own risk!");
						break;
					}
					arena.setLoc2(p.getLocation());
					p.sendMessage(ChatColor.BLUE + "You have set the pos2 of the arena " + args[1] + ".");
					arena.sendPossibleWrongWorldInfo(p);
					checkLocations(p, arena);
					break;
				case "forceusing":
					if(!isValidArena(arena, args[1], sender)) {
						break;
					}
					arena.setUsing(!arena.isUsing());
					p.sendMessage(ChatColor.RED + arena.getName() + " is available: " + (!arena.isUsing()));
					break;
				case "copypaste":
					if(!isValidArena(arena, args[1], sender)) {
						break;
					}
					if(arena.getCorner1() == null) {
						p.sendMessage(ChatColor.RED + "The corner1 of the arena is missing!");
					}
					else if(arena.getCorner2() == null) {
						p.sendMessage(ChatColor.RED + "The corner2 of the arena is missing!");
					}
					else if(arena.getCenter() == null) {
						p.sendMessage(ChatColor.RED + "The center of the arena is missing!");
					}
					else if(!arena.getCenter().getWorld().getName().equals(p.getWorld().getName())) {
						p.sendMessage(ChatColor.RED + "You're not in the same world!");
					}
					else {
						p.sendMessage(ChatColor.RED + "Copy pasting in 3 seconds. " + ChatColor.BOLD + "Move to cancel!");
						Location pLoc = p.getLocation();
						new BukkitRunnable() {

							@Override
							public void run() {
								if(p.getLocation().distanceSquared(pLoc) > 0.5)  {
									p.sendMessage(ChatColor.RED + "Cancelled because you moved!");
								}
								else {
									Location corner1 = arena.getCorner1();
									Location corner2 = arena.getCorner2();
									int topBlockX = (corner1.getBlockX() < corner2.getBlockX() ? corner2.getBlockX() : corner1.getBlockX());
									int bottomBlockX = (corner1.getBlockX() > corner2.getBlockX() ? corner2.getBlockX() : corner1.getBlockX());
									int topBlockY = (corner1.getBlockY() < corner2.getBlockY() ? corner2.getBlockY() : corner1.getBlockY());
									int bottomBlockY = (corner1.getBlockY() > corner2.getBlockY() ? corner2.getBlockY() : corner1.getBlockY());
									int topBlockZ = (corner1.getBlockZ() < corner2.getBlockZ() ? corner2.getBlockZ() : corner1.getBlockZ());
									int bottomBlockZ = (corner1.getBlockZ() > corner2.getBlockZ() ? corner2.getBlockZ() : corner1.getBlockZ());
									World world = corner1.getWorld();
									if(!world.getName().equals(p.getWorld().getName())) {
										p.sendMessage(ChatColor.RED + "You're not in the same world!");
									}
									else {
										p.sendMessage(ChatColor.BLUE + "Copy pasting...");
										Location l = null, l2 = null;
										for(int x = bottomBlockX; x <= topBlockX; x++){
											for(int y = bottomBlockY; y <= topBlockY; y++){
												for(int z = bottomBlockZ; z <= topBlockZ; z++){
													l = new Location(world, x, y, z);
													l2 = l.clone().subtract(arena.getCenter()).add(pLoc);
													if(l2.getBlockY() >= 0 && l.getBlock() != null && l.getBlock().getType() != null) {
														l2.getBlock().setType(l.getBlock().getType());
														l2.getBlock().setData(l.getBlock().getData());
														l2.getBlock().setBiome(l.getBlock().getBiome());
													}
												}
											}
										}
										p.sendMessage(ChatColor.BLUE + "Region pasted!");
										String name = null;
										for(int i = 0; i < 1000; i++) {
											name = arena.getName() + i;
											if(Arena.getArena(name) == null) break;
										}
										Arena newArena = new Arena(name);
										newArena.saveForLegionPractice();
										p.sendMessage(ChatColor.BLUE + "Created a new arena '" + name + "'");
										newArena.setBuild(arena.isBuild());
										pLoc.setDirection(arena.getCenter().getDirection());
										newArena.setCenter(pLoc);
										newArena.setLoc1(arena.getLoc1().clone().subtract(arena.getCenter()).add(pLoc));
										newArena.setLoc2(arena.getLoc2().clone().subtract(arena.getCenter()).add(pLoc));
										newArena.setCorner1(arena.getCorner1().clone().subtract(arena.getCenter()).add(pLoc));
										newArena.setCorner2(arena.getCorner2().clone().subtract(arena.getCenter()).add(pLoc));
										if(!arena.getDisplayName().equals(arena.getName())) {
											newArena.setDisplayName(arena.getDisplayName());
										}
										newArena.setKits(arena.getKits());
										p.sendMessage(ChatColor.BLUE + "Positions, names, correct build mode and kits set.");
										p.sendMessage(ChatColor.GOLD + "Everything done!");
									}
								}
							}
						}.runTaskLater(plugin, 20*3);
					}
					break;
				case "corner1":
					if(!isValidArena(arena, args[1], sender)) {
						break;
					}
					if(plugin.getConfig().getBoolean("no-arenas-in-default-world") && !p.getWorld().getName().equals(LegionPractice.stackArenasWorld)) {
						LegionPractice.getInstance();
						p.sendMessage(ChatColor.RED + "Arena creation is only available in the '"
								+ LegionPractice.stackArenasWorld + "' world. If you wish to create arenas in other worlds change 'no-arenas-in-default-world' in the config.yml. You do it at your own risk!");
						break;
					}
					arena.setCorner1(p.getLocation());
					p.sendMessage(ChatColor.BLUE + "You have set the corner1 of the arena " + arena.getName() + ".");
					arena.sendPossibleWrongWorldInfo(p);
					checkLocations(p, arena);
					break;
				case "corner2":
					if(!isValidArena(arena, args[1], sender)) {
						break;
					}
					if(plugin.getConfig().getBoolean("no-arenas-in-default-world") && !p.getWorld().getName().equals(LegionPractice.stackArenasWorld)) {
						LegionPractice.getInstance();
						p.sendMessage(ChatColor.RED + "Arena creation is only available in the '"
								+ LegionPractice.stackArenasWorld + "' world. If you wish to create arenas in other worlds change 'no-arenas-in-default-world' in the config.yml. You do it at your own risk!");
						break;
					}
					arena.setCorner2(p.getLocation());
					p.sendMessage(ChatColor.BLUE + "You have set the corner2 of the arena " + args[1] + ".");
					arena.sendPossibleWrongWorldInfo(p);
					checkLocations(p, arena);
					break;
				case "centre":
				case "center":
					if(!isValidArena(arena, args[1], sender)) {
						break;
					}
					if(plugin.getConfig().getBoolean("no-arenas-in-default-world") && !p.getWorld().getName().equals(LegionPractice.stackArenasWorld)) {
						LegionPractice.getInstance();
						p.sendMessage(ChatColor.RED + "Arena creation is only available in the '"
								+ LegionPractice.stackArenasWorld + "' world. If you wish to create arenas in other worlds change 'no-arenas-in-default-world' in the config.yml. You do it at your own risk!");
						break;
					}
					arena.setCenter(p.getLocation());
					p.sendMessage(ChatColor.BLUE + "You have set the center of the arena " + arena.getName() + ".");
					arena.sendPossibleWrongWorldInfo(p);
					checkLocations(p, arena);
					break;
				case "build":
				case "togglebuild":
					if(!isValidArena(arena, args[1], sender)) {
						break;
					}
					arena.setBuild(!arena.isBuild());
					if(arena.isBuild()) p.sendMessage(ChatColor.BLUE + "The arena " + arena.getName() + " is now a build arena.");
					else p.sendMessage(ChatColor.BLUE + "The arena " + arena.getName() + " is no longer a build arena.");
					break;
				case "ffa":
					if(!isValidArena(arena, args[1], sender)) {
						break;
					}
					arena.setFFA(!arena.isFFA());
					if(arena.isFFA()) {
						p.sendMessage(ChatColor.BLUE + "The arena " + arena.getName() + " is now an ffa arena.");
						p.sendMessage(ChatColor.GREEN+ "Join the arena with /" + arena.getName() + ". Remember to restart the server first!");
					}
					else p.sendMessage(ChatColor.BLUE + "The arena " + arena.getName() + " is no longer an ffa arena.");
					if(arena.getKits().isEmpty()) {
						p.sendMessage(ChatColor.RED + "Remember to add at least 1 kit with /arena kits <arena> <kit>!");
					}
					break;
				case "kits":
					if(!isValidArena(arena, args[1], sender)) {
						break;
					}
					if(args.length > 2) {
						if(!StringUtil.isAlphaNumeric(args[2])) {
							p.sendMessage(ChatColor.RED + "The name must be alphanumeric!");
							return true;
						}
						arena.getKits().add(args[2].toLowerCase());
						p.sendMessage(ChatColor.BLUE + "Current specified kits of the arena '" + arena.getName() + "': " + arena.getKits().toString().replace("[", "").replace("]", ""));
						break;
					}
					else {
						p.sendMessage(ChatColor.BLUE + "/battlearena kits <name> <kit> - add a kit");
					}
					p.sendMessage(ChatColor.BLUE + "Current specified kits of the arena '" + arena.getName() + "': " + arena.getKits().toString().replace("[", "").replace("]", ""));
					break;
				case "clearkits":
					if(!isValidArena(arena, args[1], sender)) {
						break;
					}
					arena.getKits().clear();
					p.sendMessage(ChatColor.BLUE + "The arena doesn't have specified kits anymore.");
					break;
				case "removekit":
					if(!isValidArena(arena, args[1], sender)) {
						break;
					}
					if(args.length > 2) {
						arena.getKits().remove(args[2].toLowerCase());
					}
					p.sendMessage(ChatColor.BLUE + "Current specified kits of the arena '" + arena.getName() + "': " + arena.getKits().toString().replace("[", "").replace("]", ""));
					break;
				case "teleport":
				case "tp":
					if(!isValidArena(arena, args[1], sender)) {
						break;
					}
					UUID uuid = p.getUniqueId();
					if(last.containsKey(uuid)) {
						if(last.get(uuid)+5000 > System.currentTimeMillis()) {
							int i = counts.getOrDefault(uuid, 0);
							counts.put(uuid, i++);
							if(i == 3) {
								p.sendMessage(ChatColor.GREEN + "Trying to find an specific arena? Use /sprac findarena");
							}
						}
						else {
							counts.remove(uuid);
						}
					}
					last.put(uuid, System.currentTimeMillis());
					Location loc = arena.getCenter();
					p.sendMessage("Teleporting...");
					if(loc == null || loc.getWorld() == null || Bukkit.getWorld(loc.getWorld().getName()) == null) {
						p.sendMessage(ChatColor.RED + "Invalid location!");
					}
					else p.teleport(arena.getCenter());
					break;
				}
			}
		}
		return true;
	}


	private void checkLocations(Player p, Arena arena) {
		String x = ChatColor.RED + "Do not forget to set";
		int missing = 0;
		if(arena.getCenter() == null) {
			x += " center";
			missing ++;
		}
		if(arena.getLoc1() == null) {
			x += missing > 0 ? arena.getLoc2() != null ? " and pos1" : ", pos1" : " pos1";
			missing++;
		}
		if(arena.getLoc2() == null) {
			x += missing > 0 ? " and pos2" : " pos2";
			missing++;
		}
		if(missing > 0) {
			x+="!";
			p.sendMessage(x);
		}
		else p.sendMessage(ChatColor.GREEN + "Arena successfully created!");
	}

	/**
	 * 
	 * @param arena
	 * @param sender
	 * @return true if the arena is valid, otherwise false
	 */
	private boolean isValidArena(Arena arena, String search, CommandSender sender) {
		if(arena != null) {
			return true;
		}
		sender.sendMessage(ChatColor.RED + "That arena does not exist!");
		String arenaName = search.toLowerCase();
		int counter = 0;
		for(Arena ar : plugin.arenas) {
			if(isSimilar(ar.getName().toLowerCase(), arenaName)) {
				if(counter == 0) {
					sender.sendMessage(ChatColor.GOLD + "Arenas with similar name:");
				}
				counter++;
				if(counter < 10) {
					sender.sendMessage(ChatColor.YELLOW + "- " + ar.getName());
				}
			}
		}
		return false;
	}

	private boolean isSimilar(String str, String str2) {
		if(str.contains(str2) || str2.contains(str)) {
			return true;
		}
		return StringUtil.similarity(str, str2) > 0.8;
	}

	private Arena getArenaByName(String name, CommandSender sender) {
		for(Arena ar : plugin.arenas) {
			if(ar.getName().equalsIgnoreCase(name)) {
				return ar;
			}
		}
		return null;
	}
}
