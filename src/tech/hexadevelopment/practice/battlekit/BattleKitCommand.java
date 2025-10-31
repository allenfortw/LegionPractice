package tech.hexadevelopment.practice.battlekit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import tech.hexadevelopment.practice.utils.ClickableMessage;
import tech.hexadevelopment.practice.utils.ItemStackUtil;
import tech.hexadevelopment.practice.utils.StringUtil;
import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.fights.queue.QueueManager;
import tech.hexadevelopment.practice.playerkits.PlayerKits;
import tech.hexadevelopment.practice.preview.Preview;
import tech.hexadevelopment.practice.stats.Callback;
import tech.hexadevelopment.practice.LegionPractice;

/**
 * Main command for handling BattleKits.
 * @author Toppe5
 * @since 0.1
 */
public class BattleKitCommand implements CommandExecutor {

	private LegionPractice plugin;

	/**
	 * 
	 * @param plugin LegionPractice plugin.
	 */
	public BattleKitCommand(LegionPractice plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String labe, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if(args.length == 0) {
				p.sendMessage(ChatColor.GRAY + "Remember to reload the plugin or restart the server to to take effect with queue system.");
				p.sendMessage(ChatColor.GOLD + "/Battlekit create <kit name>");
				p.sendMessage(ChatColor.GOLD + "/Battlekit delete <kit name>");
				p.sendMessage(ChatColor.GOLD + "/Battlekit setIcon <kit name>");
				p.sendMessage(ChatColor.GOLD + "/Battlekit setinv <kit name>");
				p.sendMessage(ChatColor.GOLD + "/Battlekit bow <kit name>");
				p.sendMessage(ChatColor.GOLD + "/Battlekit elo <kit name>");
				p.sendMessage(ChatColor.GOLD + "/Battlekit horse <kit name>");
				p.sendMessage(ChatColor.GOLD + "/Battlekit combo <kit name>");
				p.sendMessage(ChatColor.GOLD + "/Battlekit nohunger <kit name>");
				p.sendMessage(ChatColor.GOLD + "/Battlekit editable <kit name>");
				p.sendMessage(ChatColor.GOLD + "/Battlekit types <kit name> <any|duel|bot|queue|party-ffa|party-split|party-vs-party|premium>");
				p.sendMessage(ChatColor.GOLD + "/Battlekit types <kit name> clear");
				p.sendMessage(ChatColor.GOLD + "/Battlekit extramaterial <kit name> [material] - Add extra rollback material (hold in hand or type in the command)");
				p.sendMessage(ChatColor.GOLD + "/Battlekit extramaterial <kit name> clear");
				p.sendMessage(ChatColor.GOLD + "/Battlekit info <kit name>");
				p.sendMessage(ChatColor.GOLD + "/Battlekit preview <kit name>");
				p.sendMessage(ChatColor.GOLD + "/Battlekit list");
				p.sendMessage(ChatColor.GOLD + "/Battlekit give <kit name> player");
				p.sendMessage(ChatColor.GOLD + "/Battlekit deleteeditedkits <kit name>");
				p.sendMessage(ChatColor.GOLD + "/Battlekit mergeeditedkits <kit name> <editable kit>");
				p.sendMessage(ChatColor.GOLD + "/Battlekit mergeeditedkits <kit name> clear");
				p.sendMessage(ChatColor.GOLD + "/Battlekit chestaccess <kit name>");
				p.sendMessage(ChatColor.GOLD + "/Battlekit rankedcopy <kit name> [1v1|premium]");
				//<1v1:2v2:3v3>
				p.sendMessage(ChatColor.GOLD + "/Battlekit moveup <kit name>");
				p.sendMessage(ChatColor.GOLD + "/Battlekit movedown <kit name>");
				//p.sendMessage(ChatColor.GOLD + "/Battlekit strikecheat <kit name>");
				p.sendMessage(ChatColor.GOLD + "/Battlekit stickspawn <kit name>");
				p.sendMessage(ChatColor.RED + "NEW: /Battlekit anticheat <kit name>");
				p.sendMessage(ChatColor.RED + "NEW: /Battlekit healthbar <kit name>");
				p.sendMessage(ChatColor.RED + "NEW: /Battlekit bedwars <kit name>");
			}
			else if(args.length > 1) {
				if(args[0].equalsIgnoreCase("redo")) {
					String name = args[1].toLowerCase();
					BattleKit kit = BattleKit.getKit(name);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					else {
						plugin.kits.remove(kit);
					}
					if(BattleKit.getKit(name) != null) {
						p.sendMessage(ChatColor.RED + "That kit already exists.");
						return true;
					}
					if(!StringUtil.isAlphaNumeric(args[1])) {
						p.sendMessage(ChatColor.RED + "The name must be alphanumeric!");
						return true;
					}
					p.sendMessage(ChatColor.BLUE + "Creating a new loadout: " + args[1]);
					KitEngine engine = new KitEngine();
					engine.load(new Callback() {

						@Override
						public void onSuccess(int result) {
							BattleKit template = engine.findKit(name);
							if(template != null) {
								BattleKit kit = new BattleKit(name);
								kit.setHelmet(p.getInventory().getHelmet());
								kit.setChestplate(p.getInventory().getChestplate());
								kit.setLeggings(p.getInventory().getLeggings());
								kit.setBoots(p.getInventory().getBoots());
								kit.getInventory().clear();
								kit.setInventory(new ArrayList<ItemStack>(Arrays.asList(ItemStackUtil.getContents(p))));
								kit.setPotions(p.getActivePotionEffects());
								kit = engine.apply(name, template);
								kit.giveKit(p);
								kit.saveForLegionPractice();
								p.updateInventory();
								p.sendMessage(ChatColor.YELLOW + "--------------------------------");
								p.sendMessage(ChatColor.GREEN + "Automatically created a kit based on its name. You can edit it with /Battlekit commands.");
								p.chat("/battlekit info " + name);
								ClickableMessage.sendMessage(p, ChatColor.RED + "" + ChatColor.BOLD + "Click here to get generate a new loadout", "/battlekit redo " + name);
								ClickableMessage.sendMessage(p, ChatColor.GREEN + "Click here to save the loadout.", "/battlekit setinv " + name);
								if(!kit.isElo()) {
									ClickableMessage.sendMessage(p, ChatColor.GOLD + "Or click here to create ranked version of the kit.", "/battlekit rankedcopy " + name);
								}
							}
						}
					});
				}
				else if(args[0].equalsIgnoreCase("create")) {
					String name = args[1].toLowerCase();
					if(BattleKit.getKit(name) != null) {
						p.sendMessage(ChatColor.RED + "That kit already exists.");
						return true;
					}
					if(!StringUtil.isAlphaNumeric(args[1])) {
						p.sendMessage(ChatColor.RED + "The name must be alphanumeric!");
						return true;
					}
					p.sendMessage(ChatColor.BLUE + "Creating a new kit: " + args[1]);
					KitEngine engine = new KitEngine();
					engine.load(new Callback() {

						@Override
						public void onSuccess(int result) {
							BattleKit template = engine.findKit(name);
							if(template != null) {
								BattleKit kit = new BattleKit(name);
								kit.setHelmet(p.getInventory().getHelmet());
								kit.setChestplate(p.getInventory().getChestplate());
								kit.setLeggings(p.getInventory().getLeggings());
								kit.setBoots(p.getInventory().getBoots());
								kit.getInventory().clear();
								kit.setInventory(new ArrayList<ItemStack>(Arrays.asList(ItemStackUtil.getContents(p))));
								kit.setPotions(p.getActivePotionEffects());
								kit = engine.apply(name, template);
								kit.giveKit(p);
								kit.saveForLegionPractice();
								p.updateInventory();
								p.sendMessage(ChatColor.YELLOW + "--------------------------------");
								p.sendMessage(ChatColor.GREEN + "Automatically created a kit based on its name. You can edit it with /Battlekit commands.");
								p.chat("/battlekit info " + name);
								ClickableMessage.sendMessage(p, ChatColor.RED + "" + ChatColor.BOLD + "Click here to get generate a new loadout", "/battlekit redo " + name);
								ClickableMessage.sendMessage(p, ChatColor.GOLD + "Click here to save the loadout.", "/battlekit setinv " + name);
								if(!kit.isElo()) {
									ClickableMessage.sendMessage(p, ChatColor.GOLD + "Or click here to create ranked version of the kit.", "/battlekit rankedcopy " + name);
								}
								if(name.contains("build")) {
									kit.setBuild(true);
									p.sendMessage(ChatColor.RED + "Automatically set 'build: true' because the kit name contains 'build'.");
								}
								if(name.contains("bedwars")) {
									kit.setBedwars(true);
									p.sendMessage(ChatColor.RED + "Automatically set 'bedwars: true' because the kit name contains 'bedwars'.");
								}
							}
						}
					});
				}
				else if(args[0].equalsIgnoreCase("mergeeditedkits")) {
					if(args.length > 2) {
						if(args[2].equalsIgnoreCase("clear")) {
							BattleKit kit = BattleKit.getKit(args[1]);
							if(kit == null) {
								p.sendMessage(ChatColor.RED + "Kit not found.");
								return true;
							}
							kit.setMergedEditor(null);
							p.sendMessage(ChatColor.BLUE + "Merged Kit editor cleared!");
							return true;
						}
						BattleKit kit = BattleKit.getKit(args[1]);
						if(kit == null) {
							p.sendMessage(ChatColor.RED + "Kit not found.");
							return true;
						}
						BattleKit editableKit = BattleKit.getKit(args[2]);
						if(editableKit == null) {
							p.sendMessage(ChatColor.RED + "Kit not found.");
							return true;
						}
						kit.setMergedEditor(editableKit.getName());
						p.sendMessage(ChatColor.BLUE + "Edited versions of '" + editableKit.getName() + "' will load when giving '" + kit.getName() + "'.");
					}
					else {
						p.sendMessage(ChatColor.GOLD + "/Battlekit mergeeditedkits <kit name> <editable kit>");
					}
				}
				else if(args[0].equalsIgnoreCase("deleteeditedkits")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					p.sendMessage(ChatColor.GOLD + "Starting to delete edited kits...");
					Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

						int counter = 0;

						@Override
						public void run() {

							for(OfflinePlayer of : Bukkit.getOfflinePlayers()) {
								PlayerKits pk = plugin.getPlayerKitsHandler().loadFromFile(of.getUniqueId());
								if(pk.removeEditedKit(kit.getName())) {
									pk.savePlayerKitsToFile();
									UUID uuid = pk.getPlayer();
									if(uuid != null) {
										Player target = Bukkit.getPlayer(uuid);
										if(target != null) {
											plugin.getPlayerKitsHandler().setPlayerKitsMeta(p, pk);
										}
									}
									counter++;
								}
							}

							Bukkit.getScheduler().runTask(plugin, new Runnable() {

								@Override
								public void run() {
									if(p != null) {
										p.sendMessage(ChatColor.GOLD + "Succesfully deleted " + counter + " edited kits!");
									}
								}
							});
						}
					});
				}
				else if(args[0].equalsIgnoreCase("extramaterial")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					if(args.length >= 3 && args[2].equalsIgnoreCase("clear")) {
						kit.getRollbackExtraMaterials().clear();
						p.sendMessage(ChatColor.RED + "Extra materials cleared!");
					}
					else if(args.length >= 3 && args[2].equalsIgnoreCase("all")) {
						for(Material m : Material.values()) {
							kit.getRollbackExtraMaterials().add(m);
						}
						p.sendMessage(ChatColor.BLUE + "All current extra rollback materials of the kit: " + kit.getRollbackExtraMaterials().toString().replace("[", "").replace("]", ""));
						p.sendMessage(ChatColor.RED + "All blocks added!");
					}
					else if(args.length >= 3) {
						Material mat = Material.valueOf(args[2].toUpperCase());
						if(mat != null) {
							kit.addExtraRollbackMaterial(mat);
							p.sendMessage(ChatColor.BLUE + "The extra materials for '" + mat + "' has been added!");
							p.sendMessage(ChatColor.BLUE + "All current extra rollback materials of the kit: " + kit.getRollbackExtraMaterials().toString().replace("[", "").replace("]", ""));
						}
						else if(p.getItemInHand() == null || p.getItemInHand().getType().equals(Material.AIR)) {
							p.sendMessage(ChatColor.RED + "Hold something in your hand or use /battlekit extramaterials <material>");
							p.sendMessage(ChatColor.BLUE + "All current extra rollback materials of the kit: " + kit.getRollbackExtraMaterials().toString().replace("[", "").replace("]", ""));
						}
					}
					else if(p.getItemInHand() == null || p.getItemInHand().getType().equals(Material.AIR)) {
						p.sendMessage(ChatColor.RED + "Hold something in your hand");
						p.sendMessage(ChatColor.BLUE + "All current extra rollback materials of the kit: " + kit.getRollbackExtraMaterials().toString().replace("[", "").replace("]", ""));
					}
					else {
						kit.addExtraRollbackMaterial(p.getItemInHand().getType());
						p.sendMessage(ChatColor.BLUE + "The extra materials for '" + p.getItemInHand().getType().toString() + "' has been added!");
						p.sendMessage(ChatColor.BLUE + "All current extra rollback materials of the kit: " + kit.getRollbackExtraMaterials().toString().replace("[", "").replace("]", ""));
					}
				}
				else if(args[0].equalsIgnoreCase("moveup")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					int counter = 0, current = 0;
					for(BattleKit bk : plugin.kits) {
						if(kit.equals(bk)) {
							current = counter;
						}
						counter++;
					}
					int newSlot = current-1;
					if(current < 0 || current >= plugin.kits.size() || newSlot < 0 || newSlot >= plugin.kits.size()) {
						p.sendMessage(ChatColor.RED + "Can not move the kit anymore, it already has the first position!");
					}
					else {
						Collections.swap(plugin.kits, current, newSlot);
						p.sendMessage(ChatColor.GOLD + "Moved the kit from " + current + " to " + newSlot + "!");
						QueueManager.loadQueueInventories(plugin);
					}
				}
				else if(args[0].equalsIgnoreCase("movedown")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					int counter = 0, current = 0;
					for(BattleKit bk : plugin.kits) {
						if(kit.equals(bk)) {
							current = counter;
						}
						counter++;
					}
					int newSlot = current+1;
					if(current < 0 || current >= plugin.kits.size() || newSlot < 0 || newSlot >= plugin.kits.size()) {
						p.sendMessage(ChatColor.RED + "Can not move the kit anymore, it already has the last position!");
					}
					else {
						Collections.swap(plugin.kits, current, newSlot);
						p.sendMessage(ChatColor.GOLD + "Moved the kit from " + current + " to " + newSlot + "!");
						QueueManager.loadQueueInventories(plugin);
					}
				}
				else if(args[0].equalsIgnoreCase("seticon")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					if(p.getItemInHand() == null || p.getItemInHand().getType().equals(Material.AIR)) {
						p.sendMessage(ChatColor.RED + "Hold something in your hand.");
						return true;
					}
					ItemStack item = p.getItemInHand().clone();
					ItemMeta meta = item.getItemMeta();
					if(!meta.hasDisplayName()) {
						String s = kit.getName();
						if(s.length() > 1) {
							s = s.substring(0, 1).toUpperCase() + s.substring(1);
						}
						s = s.replace("uhc", "UHC").replace("_", " ").replace("ranked", "Ranked");
						meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + s);
						p.sendMessage(ChatColor.RED + "The item doesn't have a displayname. Creating default: " + meta.getDisplayName());
					}
					item.setItemMeta(meta);
					kit.setIcon(item);
					p.sendMessage(ChatColor.BLUE + "Icon set.");
				}
				else if(args[0].equalsIgnoreCase("rankedcopy") && args.length > 1) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					if(kit.getIcon() == null) {
						p.sendMessage(ChatColor.RED + "The kit doesn't have an icon.");
						return true;
					}
					String suffix = "elo";
					if(args.length > 2) {
						suffix += args[2];
					}
					String fullName = kit.getName() + suffix;
					if(BattleKit.getKit(fullName) != null) {
						p.sendMessage(ChatColor.RED + "That kit already exists!");
						return true;
					}
					BattleKit copy = new BattleKit(fullName);
					copy.setInventory(new ArrayList<ItemStack>(kit.getInventory()));
					copy.setHelmet(kit.getHelmet() == null ? null : kit.getHelmet().clone());
					copy.setChestplate(kit.getChestplate() == null ? null : kit.getChestplate().clone());
					copy.setLeggings(kit.getLeggings() == null ? null : kit.getLeggings().clone());
					copy.setBoots(kit.getBoots() == null ? null : kit.getBoots().clone());
					copy.setTypes(kit.getTypes());
					copy.setBuild(kit.isBuild());
					copy.setChestAccess(kit.isChestAccess());
					copy.setCombo(kit.isCombo());
					copy.setHorse(kit.isHorse());
					copy.setOnlyBow(kit.isOnlyBow());
					copy.setNoHunger(kit.isNoHunger());
					copy.setElo(true);
					copy.setBestOf(kit.getBestOf());
					if(kit.isEditable()) {
						copy.setMergedEditor(kit.getName());
						p.sendMessage(ChatColor.BLUE + "Edited kits have been merged. Only " + kit.getName() + " is editable and the edited version will be given when giving " + copy.getName() + " (ranked copy).");
					}
					ItemStack clone = kit.getIcon().clone();
					ItemMeta meta = clone.getItemMeta();
					if(meta.hasDisplayName()) {
						meta.setDisplayName(kit.getIcon().getItemMeta().getDisplayName() + "");
					}
					else {
						meta.setDisplayName(copy.getFancyName());
					}
					clone.setItemMeta(meta);
					copy.setIcon(clone);
					copy.saveForLegionPractice();
					boolean arenaKits = false;
					for(Arena ar : plugin.arenas) {
						if(ar.getKits().contains(kit.getName())) {
							ar.getKits().add(fullName);
							arenaKits = true;
						}
					}
					QueueManager.loadQueueInventories(plugin);
					if(arenaKits) {
						p.sendMessage(ChatColor.GREEN + "Updated arena kits have been automatically!");
					}
					p.sendMessage(ChatColor.GREEN + "Successfully created a ranked copy of " + kit.getFancyName() + ChatColor.RESET + "" + ChatColor.GREEN + "(" + kit.getName() + ")");
				}
				else if(args[0].equalsIgnoreCase("give")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					Player tar = p;
					if(args.length > 2) {
						tar = Bukkit.getPlayer(args[2]);
					}
					if(tar == null) {
						p.sendMessage(plugin.translateMessage(p, "not-online"));
						return true;
					}
					p.sendMessage(ChatColor.RED + "Giving the kit to " + tar.getName() + ".");
					kit.giveKit(p);
				}
				else if(args[0].equalsIgnoreCase("preview")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					Preview.preview(p, kit, plugin);
				}
				else if(args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("show")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					p.sendMessage(ChatColor.GOLD + "BattleKit Info: " + kit.getName());
					p.sendMessage(ChatColor.YELLOW + "Types: " + kit.getTypes().toString().replace("[", "").replace("]", ""));
					p.sendMessage(ChatColor.YELLOW + "Merged Editor: " + kit.getMergedEditor());
					p.sendMessage(ChatColor.YELLOW + "Build: " + kit.isBuild());
					p.sendMessage(ChatColor.YELLOW + "Combo: " + kit.isCombo());
					p.sendMessage(ChatColor.YELLOW + "Horse: " + kit.isHorse());
					p.sendMessage(ChatColor.YELLOW + "Bow Only: " + kit.isOnlyBow());
					p.sendMessage(ChatColor.YELLOW + "Elo: " + kit.isElo());
					p.sendMessage(ChatColor.YELLOW + "Players can edit: " + kit.isEditable());
					p.sendMessage(ChatColor.YELLOW + "Chest access: " + kit.isChestAccess());
					p.sendMessage(ChatColor.YELLOW + "No hunger: " + kit.isNoHunger());
					p.sendMessage(ChatColor.YELLOW + "Best of: " + kit.getBestOf());
					p.sendMessage(ChatColor.YELLOW + "Stick spawn: " + kit.isStickSpawn());
					p.sendMessage(ChatColor.YELLOW + "Health bar: " + kit.isHealthbar());
					p.sendMessage(ChatColor.YELLOW + "Bedwars: " + kit.isBedwars());
				}
				else if(args[0].equalsIgnoreCase("setinv")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					else {
						kit.setHelmet(p.getInventory().getHelmet());
						kit.setChestplate(p.getInventory().getChestplate());
						kit.setLeggings(p.getInventory().getLeggings());
						kit.setBoots(p.getInventory().getBoots());
						kit.getInventory().clear();
						kit.setInventory(new ArrayList<ItemStack>(Arrays.asList(ItemStackUtil.getContents(p))));
						kit.setPotions(p.getActivePotionEffects());
						p.sendMessage(ChatColor.BLUE + "You have updated potion effects, armor and inventory contents of the kit.");
					}
				}
				else if(args[0].equalsIgnoreCase("delete")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					else {
						plugin.kits.remove(kit);
						p.sendMessage(ChatColor.BLUE + "Deleted a BattleKit.");
					}
				}
				else if(args[0].equalsIgnoreCase("bestof")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					else {
						if(args.length > 2) {
							try {
								int i = Integer.parseInt(args[2]);
								if(i < 1) {
									p.sendMessage(ChatColor.RED + "The number should be greater than 0");
								}
								else {
									kit.setBestOf(i);
									p.sendMessage(ChatColor.BLUE + "The kit is now best of " + i + " (best of only works in duels)");
								}
							}catch(Exception e) {
								p.sendMessage(ChatColor.GRAY + "/battlekit bestof <kit name> <number>");
							}
						}
						else {
							p.sendMessage(ChatColor.GRAY + "/battlekit bestof <kit name> <number>");
						}
					}
				}
				else if(args[0].equalsIgnoreCase("types")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					else {
						BattleKitType type = null;
						String t = null;
						if(args.length > 2) {
							t = args[2].toLowerCase();
							if(t.contains("clear")) {
								kit.getTypes().clear();
								p.sendMessage(ChatColor.RED + "Kit types cleared!");
								return true;
							}
							type = BattleKitType.byName(t.replace("!", ""));
						}
						else {
							p.sendMessage(ChatColor.GRAY + "/battlekit types <kit name> <any|duel|queue|party-ffa|party-split|party-vs-party|premium>");
							p.sendMessage(ChatColor.GRAY + "/battlekit types <kit name> clear");
							return true;
						}
						if(type == null) {
							p.sendMessage(ChatColor.RED + "Invalid type!");
							p.sendMessage(ChatColor.GRAY + "/battlekit types <kit name> <any|duel|queue|party-ffa|party-split|party-vs-party|premium>");
							p.sendMessage(ChatColor.GRAY + "/battlekit types <kit name> clear");
						}
						else {
							kit.getTypes().remove(BattleKitType.ANY);
							if(t.startsWith("!")) {
								for(BattleKitType types : BattleKitType.values()) {
									if(!types.equals(type) && !types.equals(BattleKitType.ANY) && !types.equals(BattleKitType.PREMIUM_QUEUE)) {
										kit.getTypes().add(types);
									}
								}
								p.sendMessage(ChatColor.BLUE + "Kit types added! All types: " + kit.getTypes().toString().replace("[", "").replace("]", ""));
							}
							else {
								kit.getTypes().add(type);
								p.sendMessage(ChatColor.BLUE + "Kit type added! All types: " + kit.getTypes().toString().replace("[", "").replace("]", ""));
							}
							if(type == BattleKitType.PREMIUM_QUEUE) {
								if(!kit.isElo()) {
									kit.setElo(true);
									p.sendMessage(ChatColor.RED + "The kit is now an elo kit. Premium matches are always ranked!");
								}
							}
							QueueManager.loadQueueInventories(plugin);
						}
					}
				}
				else if(args[0].equalsIgnoreCase("toggleeditable") || args[0].equalsIgnoreCase("editable")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					else {
						kit.setEditable(!kit.isEditable());
						p.sendMessage(ChatColor.BLUE + "Kit "+ kit.getName() + " is editable: " + kit.isEditable());
					}
				}
				else if(args[0].toLowerCase().contains("anticheat") || args[0].toLowerCase().contains("cheatbreaker") || args[0].toLowerCase().contains("strikecheat")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					else {
						kit.setAnticheatProtected(!kit.isAnticheatProtected());
						p.sendMessage(ChatColor.BLUE + "Kit "+ kit.getName() + " is StrikeCheat protected: " + kit.isAnticheatProtected());
					}
				}
				else if(args[0].equalsIgnoreCase("stickspawn") || args[0].equalsIgnoreCase("stickyspawn")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					else {
						kit.setStickSpawn(!kit.isStickSpawn());
						p.sendMessage(ChatColor.BLUE + "Kit "+ kit.getName() + " is sticky spawn: " + kit.isStickSpawn());
					}
				}
				else if(args[0].equalsIgnoreCase("bedwars")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					else {
						kit.setBedwars(!kit.isBedwars());
						p.sendMessage(ChatColor.BLUE + "Kit "+ kit.getName() + " is bedwars: " + kit.isBedwars());
						p.sendMessage(ChatColor.YELLOW + "Place beds near both spawn locations.");
					}
				}
				else if(args[0].equalsIgnoreCase("healthbar")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null ) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
					}
					else {
						kit.setHealthbar(!kit.isHealthbar());
						p.sendMessage(ChatColor.BLUE + "Kit " + kit.getName() + " has health bar activated: " + kit.isHealthbar());
					}
				}
				else if(args[0].equalsIgnoreCase("togglebow") || args[0].equalsIgnoreCase("bow")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					else {
						kit.setOnlyBow(!kit.isOnlyBow());
						p.sendMessage(ChatColor.BLUE + "Kit "+ kit.getName() + " is only bow: " + kit.isOnlyBow());
					}
				}
				else if(args[0].equalsIgnoreCase("hunger") || args[0].equalsIgnoreCase("nohunger")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					else {
						kit.setNoHunger(!kit.isNoHunger());
						p.sendMessage(ChatColor.BLUE + "Kit "+ kit.getName() + " is no-hunger: " + kit.isNoHunger());
					}
				}
				else if(args[0].equalsIgnoreCase("chestaccess") || args[0].equalsIgnoreCase("chest")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					else {
						kit.setChestAccess(!kit.isChestAccess());
						p.sendMessage(ChatColor.BLUE + "Kit "+ kit.getName() + " has chest access: " + kit.isChestAccess());
					}
				}
				else if(args[0].equalsIgnoreCase("toggleelo") || args[0].equalsIgnoreCase("elo")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					else {
						kit.setElo(!kit.isElo());
						p.sendMessage(ChatColor.BLUE + "Kit "+ kit.getName() + " is elo: " + kit.isElo());
						if(plugin.isMySQL) {
							plugin.mySQL.addKit(kit);
						}
					}
				}
				else if(args[0].equalsIgnoreCase("togglehorse") || args[0].equalsIgnoreCase("horse")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					else {
						kit.setHorse(!kit.isHorse());
						p.sendMessage(ChatColor.BLUE + "Kit "+ kit.getName() + " is horse: " + kit.isHorse());
					}
				}
				else if(args[0].equalsIgnoreCase("togglecombo") || args[0].equalsIgnoreCase("combo")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					else {
						kit.setCombo(!kit.isCombo());
						p.sendMessage(ChatColor.BLUE + "Kit "+ kit.getName() + " is combo: " + kit.isCombo());
					}
				}
				else if(args[0].equalsIgnoreCase("togglebuild") || args[0].equalsIgnoreCase("build")) {
					BattleKit kit = BattleKit.getKit(args[1]);
					if(kit == null) {
						p.sendMessage(ChatColor.RED + "Kit not found.");
						return true;
					}
					else {
						kit.setBuild(!kit.isBuild());
						p.sendMessage(ChatColor.BLUE + "Kit "+ kit.getName() + " is build: " + kit.isBuild());
					}
				}
			}
			else if(args.length == 1 && args[0].equalsIgnoreCase("list")) {
				if(plugin.kits.size() == 0) {
					p.sendMessage(ChatColor.RED + "No kits found!");
					return true;
				}
				sender.sendMessage(ChatColor.GOLD + "BattleKit List - " + "Total Kits: " + plugin.kits.size());
				for(BattleKit kit : plugin.kits) {
					sender.sendMessage(ChatColor.YELLOW + "- " + kit.getName() + ChatColor.RESET + "(" + ChatColor.RESET + kit.getFancyName() + ChatColor.RESET + ")");
					continue;
				}
			}
		}
		return true;
	}
}
