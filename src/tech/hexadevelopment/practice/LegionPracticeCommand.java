package tech.hexadevelopment.practice;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.utils.BooleanCallback;
import tech.hexadevelopment.practice.utils.FormatUtils;
import tech.hexadevelopment.practice.utils.ItemStackUtils;
import tech.hexadevelopment.practice.utils.LocationUtil;
import tech.hexadevelopment.practice.utils.SerializableLocation;
import tech.hexadevelopment.practice.utils.TextSearch;
import tech.hexadevelopment.practice.utils.world.BuildWorldDelete;
import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.fights.EloChange;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.MatchListener;
import tech.hexadevelopment.practice.fights.duel.Duel;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.knockback.KnockbackValues;
import tech.hexadevelopment.practice.language.LanguageManager;
import tech.hexadevelopment.practice.misc.GoldenHeads;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.playerdata.PlayerDataFile;
import tech.hexadevelopment.practice.playersettings.PlayerSettings;
import tech.hexadevelopment.practice.preview.Preview;
import tech.hexadevelopment.practice.stats.PlayerStats;
import tech.hexadevelopment.practice.stats.QueryManager;
import tech.hexadevelopment.practice.stats.Stats;

/**
 * @author Toppe5
 * @since 0.1
 */
public class LegionPracticeCommand implements CommandExecutor, Listener {

	private LegionPractice plugin;

	public LegionPracticeCommand(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender p, Command cmd, String label, String[] args) {
		if(args.length == 1){
			if(args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
				p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "WARNING! Reloading is not recommended. You might need to relog or/and restart to make all features work again.");
				long st = System.currentTimeMillis();
				plugin.messages.clear();
				Bukkit.getPluginManager().disablePlugin(plugin);
				Bukkit.getPluginManager().enablePlugin(plugin);
				long et = System.currentTimeMillis();
				p.sendMessage(ChatColor.GOLD + "LegionPractice was reloaded in " + (et-st) + " ms.");
				return true;
			}
			else if(args[0].equalsIgnoreCase("arenasworld")) {
				if(p instanceof Player) {
					p.sendMessage(ChatColor.GOLD + "Teleporting to the arenas world...");
					((Player) p).teleport(new Location(Bukkit.getWorld(LegionPractice.stackArenasWorld), 0, 10, 0));
				}
			}
			else if(args[0].equalsIgnoreCase("setlobby") && p instanceof Player) {
				plugin.getConfig().set("lobby", new SerializableLocation(((Player) p).getLocation()).toString());
				plugin.saveConfig();
				plugin.arenaPvP.updateLobby();
				p.sendMessage(ChatColor.GOLD + "You have set the new lobby!");
				String c1 = plugin.getConfig().getString("player-spawn-hider.spawn-corner1");
				String c2 = plugin.getConfig().getString("player-spawn-hider.spawn-corner2");
				if(c1 == null) {
					plugin.getConfig().set("player-spawn-hider.spawn-corner1", new SerializableLocation(plugin.arenaPvP.getLobby().clone().add(100, 100, 100)).toString());
					plugin.saveConfig();
				}
				if(c2 == null) {
					plugin.getConfig().set("player-spawn-hider.spawn-corner2", new SerializableLocation(plugin.arenaPvP.getLobby().clone().add(-100, -100, -100)).toString());
					plugin.saveConfig();
				}
			}
			else if(args[0].equalsIgnoreCase("block") && p instanceof Player) {
				Player pl = (Player) p;
				pl.getLocation().add(0, -1, 0).getBlock().setType(Material.GLASS);
				pl.teleport(pl.getLocation().getBlock().getLocation().add(0.5, 1, 0.5));
			}
			else if(args[0].equalsIgnoreCase("setediting") && p instanceof Player) {
				plugin.getConfig().set("editing-place", new SerializableLocation(((Player) p).getLocation()).toString());
				plugin.saveConfig();
				p.sendMessage(ChatColor.GOLD + "You have set the new editing place!");
			}
			else if(args[0].equalsIgnoreCase("savekits")) {
				plugin.getFileManager().saveKits();
				p.sendMessage(ChatColor.GOLD + "Kits have been saved!");
			}
			else if(args[0].equalsIgnoreCase("savearenas")) {
				plugin.getFileManager().saveArenas();
				p.sendMessage(ChatColor.GOLD + "Arenas have been saved!");
			}
			else if(args[0].equalsIgnoreCase("savelanguageitems")) {
				plugin.getFileManager().saveLanguageItems();
				p.sendMessage(ChatColor.GOLD + "Languageitems have been saved!");
			}
			else if(args[0].equalsIgnoreCase("savespawnitems")) {
				plugin.getFileManager().saveSpawnItems();
				p.sendMessage(ChatColor.GOLD + "Spawnitems have been saved!");
			}
			else if(args[0].equalsIgnoreCase("flatfiletomysql")) {
				if(!plugin.isMySQL || !plugin.mySQL.shouldBeConnected) {
					p.sendMessage(ChatColor.RED + "MySQL must be enabled and working to use that command!");
					return true;
				}
				Location l = p instanceof Player ? ((Entity) p).getLocation() : null;
				if(p instanceof Player) {
					p.sendMessage(ChatColor.RED + "Move to cancel! Starting convertion in 5 seconds!");
				}
				else p.sendMessage(ChatColor.RED + "Starting convertion in 5 seconds. Stop the server immediately to cancel!");
				new BukkitRunnable() {
					
					@Override
					public void run() {
						if(p == null) return;
						if(l == null || l.equals(((Player) p).getLocation())) {
							p.sendMessage(plugin.getPrefix() + ChatColor.RED + "Convertion starting... You may disconnect from the server now.");
							new BukkitRunnable() {
								
								@Override
								public void run() {
									int counter = 0;
									long started = System.currentTimeMillis();
									long lastMessage = System.currentTimeMillis();
									File folder = new File(plugin.getDataFolder(), "playerdata");
									folder.mkdirs();
									List<String> stats = Stats.allStats();
									File[] files = folder.listFiles();
									if(p != null) {
										p.sendMessage(plugin.getPrefix() + ChatColor.YELLOW + "Converting total of " + ChatColor.GOLD + files.length + ChatColor.YELLOW + " files. This might take a while.");
									}
									for(File f : files) {
										if(f.getName().endsWith(".yml")) {
											UUID uuid = UUID.fromString(f.getName().replace(".yml", ""));
											for(String stat : stats) {
												int get = Stats.getQueryManager().getStatsSync(uuid, stat, false);
												Stats.getQueryManager().setStatsSync(uuid, stat, get, true);
											}
											counter++;
											if(counter % 10 == 0) {
												if(lastMessage+10000 < System.currentTimeMillis()) {
													lastMessage = System.currentTimeMillis();
													if(p != null) {
														double percent = counter/files.length*100;
														long est = files.length/counter*(System.currentTimeMillis()-started);
														long seconds= (est/1000)%60;
														long minutes= (est-seconds)/1000/60;
														p.sendMessage(plugin.getPrefix() + ChatColor.YELLOW + "Converting files " + ChatColor.GOLD + counter + "/" + files.length + ChatColor.YELLOW + ". " + ChatColor.GOLD + percent + "%" + ChatColor.YELLOW + "ready. Estimated time remaining: " + ChatColor.GOLD + minutes + " minutes and " + seconds + " seconds" + ChatColor.YELLOW + ".");
													}
												}
											}
										}
									}
									p.sendMessage(plugin.getPrefix() + ChatColor.RED + "File convertion successfully finished!");
								}
							}.runTaskAsynchronously(plugin);
						}
						if(p != null && l != null && !l.equals(((Player) p).getLocation())) {
							p.sendMessage(plugin.getPrefix() + ChatColor.RED + "Convertion cancelled because you moved!");
						}
					}
				}.runTaskLater(plugin, 20*5);
			}
			else if(args[0].equalsIgnoreCase("mysqltoflatfile")) {
				if(!plugin.isMySQL || !plugin.mySQL.shouldBeConnected) {
					p.sendMessage(ChatColor.RED + "MySQL must be enabled and working to use that command!");
					return true;
				}
				Location l = p instanceof Player ? ((Entity) p).getLocation() : null;
				if(p instanceof Player) {
					p.sendMessage(ChatColor.RED + "Move to cancel! Starting convertion in 5 seconds!");
				}
				else p.sendMessage(ChatColor.RED + "Starting convertion in 5 seconds. Stop the server immediately to cancel!");
				new BukkitRunnable() {
					
					@Override
					public void run() {
						if(p == null) return;
						if(l == null || l.equals(((Player) p).getLocation())) {
							p.sendMessage(plugin.getPrefix() + ChatColor.RED + "Convertion starting... You may disconnect from the server now.");
							new BukkitRunnable() {
								
								@Override
								public void run() {
									int counter = 0;
									long started = System.currentTimeMillis();
									long lastMessage = System.currentTimeMillis();
									File folder = new File(plugin.getDataFolder(), "playerdata");
									folder.mkdirs();
									List<String> stats = Stats.allStats();
									File[] files = folder.listFiles();
									if(p != null) {
										p.sendMessage(plugin.getPrefix() + ChatColor.YELLOW + "Converting total of " + ChatColor.GOLD + files.length + ChatColor.YELLOW + " files. This might take a while.");
									}
									for(File f : files) {
										if(f.getName().endsWith(".yml")) {
											UUID uuid = UUID.fromString(f.getName().replace(".yml", ""));
											for(String stat : stats) {
												int get = Stats.getQueryManager().getStatsSync(uuid, stat, true);
												Stats.getQueryManager().setStatsSync(uuid, stat, get, false);
											}
											counter++;
											if(counter % 10 == 0) {
												if(lastMessage+10000 < System.currentTimeMillis()) {
													lastMessage = System.currentTimeMillis();
													if(p != null) {
														double percent = counter/files.length*100;
														long est = files.length/counter*(System.currentTimeMillis()-started);
														long seconds= (est/1000)%60;
														long minutes= (est-seconds)/1000/60;
														p.sendMessage(plugin.getPrefix() + ChatColor.YELLOW + "Converting files " + ChatColor.GOLD + counter + "/" + files.length + ChatColor.YELLOW + ". " + ChatColor.GOLD + percent + "%" + ChatColor.YELLOW + "ready. Estimated time remaining: " + ChatColor.GOLD + minutes + " minutes and " + seconds + " seconds" + ChatColor.YELLOW + ".");
													}
												}
											}
										}
									}
									p.sendMessage(plugin.getPrefix() + ChatColor.RED + "File convertion successfully finished!");
									p.sendMessage(ChatColor.RED + "Disabling MySQL now!");
									plugin.mySQL.close();
									plugin.mySQL = null;
									plugin.isMySQL = false;
									plugin.getConfig().set("database.mysql", false);
									plugin.saveConfig();
									p.sendMessage(ChatColor.RED + "MySQL disabled!");
								}
							}.runTaskAsynchronously(plugin);
						}
						if(p != null && l != null && !l.equals(((Player) p).getLocation())) {
							p.sendMessage(plugin.getPrefix() + ChatColor.RED + "Convertion cancelled because you moved!");
						}
					}
				}.runTaskLater(plugin, 20*5);
			}
			else if(args[0].equalsIgnoreCase("savedata")) {
				p.sendMessage(ChatColor.RED + "Saving player data...");
				for(Player pl : Bukkit.getOnlinePlayers()) {
					try{
						PlayerSettings.getPlayerSettings(pl).save();
						PlayerStats stats = PlayerStats.getStats(pl.getUniqueId(), false);
						if(stats != null) {
							stats.save(false);
						}
						plugin.getPlayerKitsHandler().getPlayerKits(pl).savePlayerKitsToFile();
					}catch(Exception e) {
						p.sendMessage(ChatColor.RED + "Error occurred while saving " + pl.getName() + "'s data, check console for the error.");
						e.printStackTrace();
					}
				}
				PlayerStats.top.put("global_elo", Stats.getQueryManager().topStats("global_elo", 10));
				PlayerStats.top.put(Stats.BRACKETS, Stats.getQueryManager().topStats(Stats.BRACKETS, 10));
				PlayerStats.top.put(Stats.DEATHS, Stats.getQueryManager().topStats(Stats.DEATHS, 10));
				PlayerStats.top.put(Stats.GLOBAL_ELO, Stats.getQueryManager().topStats(Stats.GLOBAL_ELO, 10));
				PlayerStats.top.put(Stats.KILLS, Stats.getQueryManager().topStats(Stats.KILLS, 10));
				PlayerStats.top.put(Stats.LMS, Stats.getQueryManager().topStats(Stats.LMS, 10));
				PlayerStats.top.put(Stats.PARTY_VS_PARTY_WINS, Stats.getQueryManager().topStats(Stats.PARTY_VS_PARTY_WINS, 10));
				p.sendMessage(ChatColor.GREEN + "Saving completed!");
			}
			else if(args[0].equalsIgnoreCase("totalfights")) {
				for(BattleKit kit : plugin.kits) {
					p.sendMessage(ChatColor.BLUE + kit.getFancyName() + ChatColor.BLUE + " (" + kit.getName() + ChatColor.RESET + "" + ChatColor.BLUE + "): " + ChatColor.GREEN + plugin.getFileManager().getFightsConfig().getInt(kit.getName()));
				}
			}
			else if(args[0].equalsIgnoreCase("findarena") && p instanceof Player) {
				double distance = 500;
				Arena found = null;
				Location loc = ((Entity) p).getLocation();
				for(Arena arena : plugin.arenas) {
					if(arena.getCorner1() != null && arena.getCorner2() != null
							&& LocationUtil.isInregion(loc, arena.getCorner1(), arena.getCorner2())) {
						p.sendMessage(ChatColor.GOLD + "Current arena: " + arena.getName());
						return true;
					}
					if(arena.getCenter() != null && arena.getCenter().getWorld().getName().equals(loc.getWorld().getName())) {
						double dis = arena.getCenter().distance(loc);
						if(dis < distance) {
							distance = dis;
							found = arena;
						}
					}
				}
				if(found == null) {
					p.sendMessage(ChatColor.RED + "Couldn't find the arena...");
				}
				else {
					p.sendMessage(ChatColor.GOLD + "Closest arena: " + found.getName() + ", distance to center: " + FormatUtils.toTwoDecimals(distance));
				}
			}
			else if(args[0].equalsIgnoreCase("spawncorner1")) {
				plugin.getConfig().set("player-spawn-hider.spawn-corner1", new SerializableLocation(((Player) p).getLocation()).toString());
				plugin.saveConfig();
				p.sendMessage(ChatColor.BLUE + "Spawncorner1 set!");;
			}
			else if(args[0].equalsIgnoreCase("spawncorner2")) {
				plugin.getConfig().set("player-spawn-hider.spawn-corner2", new SerializableLocation(((Player) p).getLocation()).toString());
				plugin.saveConfig();
				p.sendMessage(ChatColor.BLUE + "Spawncorner2 set!");
			}
			else if(args[0].equalsIgnoreCase("goldenhead")) {
				if(p instanceof Player) {
					p.sendMessage(ChatColor.GREEN + "Giving a golden head!");
					((Player) p).getInventory().addItem(GoldenHeads.goldenHeadItem());
				}
			}
			else if((args[0].equalsIgnoreCase("position") || args[0].equalsIgnoreCase("position")
					|| args[0].equalsIgnoreCase("pos") || args[0].equalsIgnoreCase("loc")) && p instanceof Player) {
				p.sendMessage(ChatColor.GOLD + "Your position: " + ChatColor.YELLOW + (new SerializableLocation(((Player) p).getLocation()).toReadableString()));
			}
			else if(args[0].equalsIgnoreCase("update") || args[0].equalsIgnoreCase("checkupdate")) {
				p.sendMessage(ChatColor.GOLD + "Checking for updates.");
				plugin.arenaPvP.update(new BooleanCallback() {

					@Override
					public void onResult(boolean b) {
						if(p != null) {
							if(b) {
								p.sendMessage(ChatColor.RED + "Found a new update:");
							}
							else p.sendMessage(ChatColor.GREEN + "Didn't find any updates!");
						}
					}
				});
			}
			else if(args[0].equalsIgnoreCase("changelog")) {
				p.sendMessage(ChatColor.RED + "Changelog is no longer built-in.");
				/*
				p.sendMessage(ChatColor.GREEN + "Changelog: 0.5.0");
				p.sendMessage(ChatColor.GRAY + "- Added /LegionPractice changelog");
				p.sendMessage(ChatColor.GRAY + "- Added 'queue-ping-limit' message (messages.yml) and 'max-ranked-queue-ping' in the config.");
				p.sendMessage(ChatColor.GRAY + "- Bot will use steve skin by default. ('always-same-skin: true' and 'bot-skin: Steve' in the config). This should fix the problems with spawning bots.");
				p.sendMessage(ChatColor.GRAY + "- Fixed some small bugs and cleaned the code.");
				p.sendMessage(ChatColor.GRAY + "- Added 'insta-soup: true' in the config. Soup heals 3.5 hearts.");
				p.sendMessage(ChatColor.GRAY + "- Improved juggernaut event system.");
				p.sendMessage(ChatColor.GRAY + "- Fixed a small bug with SignGUI system.");
				p.sendMessage(ChatColor.GRAY + "- Added 'cache-rollback-blocks: true' for very fast rollbacks. (Still haven't tested enough)");
				p.sendMessage(ChatColor.GRAY + "- Added 'rollback-on-enable-if-crashed: true'. If enabled and the server stops or crashes before an arena is rolled back it will rollback it when the server starts again. WILL NOT USE CACHED BLOCKS AND ALL BLOCKS THAT COULD HAVE BEEN PLACED WITH THE KIT WILL BE REMOVED.");
				p.sendMessage(ChatColor.GRAY + "- Fixed a bug in queue when arenas couldn't be found.");
				p.sendMessage(ChatColor.GRAY + "- Improved arena searching and prepared the smart arena search feature.");
				p.sendMessage(ChatColor.GRAY + "- Added /LegionPractice totalfights to see the number of fights with each kit.");
				p.sendMessage(ChatColor.GRAY + "- Fixed a bug with arena rollback.");
				p.sendMessage(ChatColor.GRAY + "- Added 'build-limit: 30' to prevent skybases. (arena center y - player location y)");
				p.sendMessage(ChatColor.GRAY + "- Added '<winner>' and '<loser>' placeholders for 'you-won' and 'did-not-win' messages.");
				p.sendMessage(ChatColor.GRAY + "- Added 'remove-all-drops-on-startup: true' to clear all drops on startup to prevent drops in arenas.");
				p.sendMessage(ChatColor.GRAY + "- Saved fights should now contain the playback UUID.");
				p.sendMessage(ChatColor.GRAY + "- Fixed errors with playback search and inventory.");
				p.sendMessage(ChatColor.GRAY + "- Edited spectator items will now work.");
				p.sendMessage(ChatColor.GRAY + "- Added /battlekit chestaccess <kit>, if true players can get more items when editing that kit.");
				p.sendMessage(ChatColor.GRAY + "- Started player settings system.");
				p.sendMessage(ChatColor.GRAY + "- Added 'empty-arenas-world: true' to generate empty chunks in the arenas world. Will only affect new chunks.");
				p.sendMessage(ChatColor.GRAY + "- Bot fights don't affect player stats anymore.");
				p.sendMessage(ChatColor.GRAY + "- Added 'hide-other-spectators: false' to hide other spectators when spectating.");
				p.sendMessage(ChatColor.GRAY + "- Fixed a bug with cached block change rollback system.");
				p.sendMessage(ChatColor.GRAY + "- Added /battlekit rankedcopy <kit>, to create a copy of the kit. If the kit that will be copied is editable the editors are automatically merged.");
				p.sendMessage(ChatColor.GRAY + "- Fixed a few very small bugs and added /sprac speed, /sprac world <world> (teleports) and /sprac tempworld <name> (creates a new world)");
				p.sendMessage(ChatColor.GRAY + "- Tried to improve bot's potion splashing etc (hope its better now)");
				p.sendMessage(ChatColor.GRAY + "- Added /playersettings (/psettings, /settings)");
				p.sendMessage(ChatColor.GRAY + "- Added health potions left to clickable fight inventories.");
				p.sendMessage(ChatColor.GRAY + "- Added a few placeholders.");
				p.sendMessage(ChatColor.GRAY + "- Improved things with explosions and rollbacks.");
				p.sendMessage(ChatColor.GRAY + "- player-spawn-hider has been added and /sprac spawncorner1 and spawncorner2. If enabled and set so in personal settings the players will be hidden in spawn.");
				p.sendMessage(ChatColor.GRAY + "- Added ranked-queue-inventory-title and unranked and ranked icons can now be same.");
				p.sendMessage(ChatColor.GRAY + "- FFA arena system has been added. ffa-arena-reset in messages.yml, and ffa-reset-delay in config.yml. Also /arena ffa <arena name>. (Needs restart after) and then join with /<arena name>.");
				 */
				/*
				p.sendMessage(ChatColor.GREEN + "Changelog: 1.0 (coming soon)");
				p.sendMessage(ChatColor.GRAY + "- Fixed rollback when placing blocks in water.");
				p.sendMessage(ChatColor.GRAY + "- Fixed build-limit.");
				p.sendMessage(ChatColor.GRAY + "- Added bot-fast-potions: true, old bot potting style which results in better pvp experience.");
				p.sendMessage(ChatColor.GRAY + "- Fixed kit editor chestaccess.");
				p.sendMessage(ChatColor.GRAY + "- Added death: and (under death:) disable-message: true and lightning: true in the config-");
				p.sendMessage(ChatColor.GRAY + "- Added /sprac cancel <player> to cancel their fight.");
				p.sendMessage(ChatColor.GRAY + "- Fixed a few small bugs.");
				p.sendMessage(ChatColor.GRAY + "- Fixed a bug with spectating.");
				p.sendMessage(ChatColor.GRAY + "- PlayerData loading won't cause lag anymore.");
				p.sendMessage(ChatColor.GRAY + "- Killing with a bow or other projectiles counts as a kill.");
				p.sendMessage(ChatColor.GRAY + "- Added kills-required: 10 (under ranked:). The needed number of kills before the player can join ranked queue.");
				p.sendMessage(ChatColor.GRAY + "- Added /sprac resetstats <player> [elo, kitName, deaths, kills, lms, brackets, partywins, all]");
				p.sendMessage(ChatColor.GRAY + "- Better TPS and rollback performance (a lot better with FFA arenas)");
				p.sendMessage(ChatColor.GRAY + "- Added insta-void: true in the config. Player's in match will instantly die when their y level is negative. Good for spleef, skywars or similar 'minigames'.");
				p.sendMessage(ChatColor.GRAY + "- Fixed few things and added ffa-died in the messages.yml, not used yet though.");
				p.sendMessage(ChatColor.GRAY + "- Fixed /koth leave and added wait-before-leaving-ffa-arena: 10 in the config. Player's must wait before leaving ffa arenas.");
				p.sendMessage(ChatColor.GRAY + "- Added teleporting: '&eTeleporting in <seconds> seconds...' in the messages.yml");
				p.sendMessage(ChatColor.GRAY + "- Added teleport-cancelled: '&cThe teleport cancelled because you took damage or moved' in the messages.yml");
				p.sendMessage(ChatColor.GRAY + "- Added holden-heads: true in the config (/sprac goldenhead to give a holden head)");
				p.sendMessage(ChatColor.GRAY + "- Added remove-arrows: true in the config. If enabled arrows will be removed when they land.");
				p.sendMessage(ChatColor.GRAY + "- Added /sprac savedata to manually save player data.");
				p.sendMessage(ChatColor.GRAY + "- Added a little cooldown for spawnitems (spawnitem-cooldown: 250 in the config)");
				p.sendMessage(ChatColor.GRAY + "- Added do-not-spam-things: in the messages.yml");
				p.sendMessage(ChatColor.GRAY + "- Fixed many small bugs.");
				p.sendMessage(ChatColor.GRAY + "- Fancy public party messages and fixed bugs.");
				 */
			}
			return true;
		}
		else if(args.length >= 2 && args[0].equalsIgnoreCase("flat") && p instanceof Player) {
			Player pl = (Player) p;
			int x = Integer.parseInt(args[1]);
			Location l = pl.getLocation();
			int h = -x/2;
			Material mat = args.length > 2 && Material.getMaterial(args[2].toUpperCase()) != null ? Material.getMaterial(args[2].toUpperCase()) : Material.GLASS;
			for(int i = 0; i < x; i++) {
				for(int j = 0; j < x; j++) {
					l.clone().add(h+i, 0, h+j).getBlock().setType(mat);
				}
			}
			p.sendMessage(ChatColor.GREEN + "Done!");
			return true;
		}
		else if(args.length > 1 && args[0].toLowerCase().contains("potbug")) {
			String name = args[1];
			Player target = Bukkit.getPlayer(name);
			if(target == null) p.sendMessage(ChatColor.RED + "Player not found!");
			else {
				if(target.hasMetadata(MatchListener.POT_BUG_META)) {
					target.removeMetadata(MatchListener.POT_BUG_META, plugin);
					p.sendMessage(ChatColor.GREEN + "Pot bug detector (" + name + "): false");
				}
				else {
					target.setMetadata(MatchListener.POT_BUG_META, new FixedMetadataValue(plugin, true));
					p.sendMessage(ChatColor.RED + "Pot bug detector (" + name + "): true");
					p.sendMessage(ChatColor.RED + "Check console for the errors!");
				}
			}
		}
		else if(args.length == 3 && args[0].equalsIgnoreCase("setconfig")) {
			Object object = args[2];
			try {
				object = Boolean.parseBoolean(args[2]);
			}catch(Exception e) {}
			try {
				object = Double.parseDouble(args[2]);
			}catch(Exception e) {}
			plugin.getConfig().set(args[1], object);
			plugin.saveConfig();
			p.sendMessage(ChatColor.YELLOW + "Config updated:");
			p.sendMessage(args[1] + ": " + args[2]);
			return true;
		}
		else if(args.length > 1 && (args[0].equalsIgnoreCase("placeholder") || args[0].equalsIgnoreCase("ph"))) {
			String str = "";
			for(int i = 1; i < args.length; i++) {
				str += args[i];
			}
			p.sendMessage(plugin.getPlaceholders().doPlaceholders(p instanceof Player ? (Player)p : null, str, "", true));
			return true;
		}
		else if(args.length > 1 && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("faq") || args[0].equalsIgnoreCase("ask"))) {
			List<String> keywords = new ArrayList<String>();
			for(int i = 1; i < args.length; i++) {
				String s = args[i].toLowerCase();
				keywords.add(s);
			}
			p.sendMessage(ChatColor.RED + "-----------------------------");
			p.sendMessage(TextSearch.search(keywords, ChatColor.YELLOW, true));
			return true;
		}
		else if(args.length > 1 && args[0].equalsIgnoreCase("resetelo")) {
			BattleKit kit = BattleKit.getKit(args[1]);
			if(kit == null || !kit.isElo()) {
				p.sendMessage(ChatColor.RED + "The kit was not found or is not a ranked kit!");
			}
			else {
				p.sendMessage(ChatColor.RED + "Reseting stats... This might take a while");
				Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

					@Override
					public void run() {
						String s = Stats.elo(kit);
						for(Player p : Bukkit.getOnlinePlayers()) {
							PlayerStats st = PlayerStats.getStats(p.getUniqueId());
							if(st != null) {
								HashMap<String, Integer> map = st.getElos();
								if(map.containsKey(s)) {
									map.put(s, QueryManager.startingElo);
									st.save(true);
								}
							}
						}
						for(OfflinePlayer op : Bukkit.getOfflinePlayers()) {
							PlayerStats st = PlayerStats.getStats(op.getUniqueId(), false, false);
							if(st != null) {
								HashMap<String, Integer> map = st.getElos();
								if(map.containsKey(s)) {
									map.put(s, QueryManager.startingElo);
									st.save(true);
								}
							}
						}
						if(p != null) {
							p.sendMessage(ChatColor.GREEN + kit.getName() + "'s elos have been reset.");
						}
						if(Bukkit.getConsoleSender() != p) {
							Bukkit.getLogger().info(kit.getName() + "'s elos have been reset by " + p.getName());
						}
					}
				});
			}
			return true;
		}
		else if(args.length > 1 && args[0].equalsIgnoreCase("resetstats")) {
			String statt = args.length > 2 ? args[2] : "all";
			Bukkit.getLogger().info("Logging information while reseting stats:");
			p.sendMessage(ChatColor.RED + "Loading player profile...");
			if(p != Bukkit.getConsoleSender()) {
				Bukkit.getLogger().info(ChatColor.RED + "Loading player profile...");
			}
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

				@SuppressWarnings("deprecation")
				@Override
				public void run() {
					OfflinePlayer of = Bukkit.getOfflinePlayer(args[1]);
					if(of.hasPlayedBefore()) {
						p.sendMessage(ChatColor.RED + "Reseting stats...");
						if(p != Bukkit.getConsoleSender()) {
							Bukkit.getLogger().info(ChatColor.RED + "Reseting stats...");
						}
						PlayerStats stats = PlayerStats.getStats(of.getUniqueId(), true, false);
						for(Entry<String, Integer> e : stats.getElos().entrySet()) {
							if(statt.equalsIgnoreCase("any") || statt.equalsIgnoreCase("all")
									|| statt.equalsIgnoreCase("elo") || statt.equalsIgnoreCase(e.getKey())){
								p.sendMessage(ChatColor.RED + e.getKey() + "= old: " + e.getValue() + ", new: " + QueryManager.startingElo);
								if(p != Bukkit.getConsoleSender()) {
									Bukkit.getLogger().info(ChatColor.RED + e.getKey() + "= old: " + e.getValue() + ", new: " + QueryManager.startingElo);
								}
								stats.getElos().put(e.getKey(), QueryManager.startingElo);
							}
						}
						if(statt.equalsIgnoreCase("any") || statt.equalsIgnoreCase("all")
								|| statt.equalsIgnoreCase("kills")) {
							p.sendMessage(ChatColor.RED + "Kills= old: " + stats.getKills() + ", new: " + 0);
							if(p != Bukkit.getConsoleSender()) {
								Bukkit.getLogger().info(ChatColor.RED + "Kills= old: " + stats.getKills() + ", new: " + 0);
							}
							stats.setKills(0);
						}
						if(statt.equalsIgnoreCase("any") || statt.equalsIgnoreCase("all")
								|| statt.equalsIgnoreCase("deaths")) {
							p.sendMessage(ChatColor.RED + "Deaths= old: " + stats.getDeaths() + ", new: " + 0);
							if(p != Bukkit.getConsoleSender()) {
								Bukkit.getLogger().info(ChatColor.RED + "Deaths= old: " + stats.getDeaths() + ", new: " + 0);
							}
							stats.setDeaths(0);
						}
						if(statt.equalsIgnoreCase("any") || statt.equalsIgnoreCase("all")
								|| statt.toLowerCase().contains("party")) {
							p.sendMessage(ChatColor.RED + "Party Wins= old: " + stats.getPartyVsPartyWins() + ", new: " + 0);
							if(p != Bukkit.getConsoleSender()) {
								Bukkit.getLogger().info(ChatColor.RED + "Party Wins= old: " + stats.getPartyVsPartyWins() + ", new: " + 0);
							}
							stats.setPartyVsPartyWins(0);
						}
						if(statt.equalsIgnoreCase("any") || statt.equalsIgnoreCase("all")
								|| statt.equalsIgnoreCase("brackets")) {
							p.sendMessage(ChatColor.RED + "Brackets Wins= old: " + stats.getBracketsWins() + ", new: " + 0);
							if(p != Bukkit.getConsoleSender()) {
								Bukkit.getLogger().info(ChatColor.RED + "Brackets Wins= old: " + stats.getBracketsWins() + ", new: " + 0);
							}
							stats.setBracketsWins(0);
						}
						if(statt.equalsIgnoreCase("any") || statt.equalsIgnoreCase("all")
								|| statt.equalsIgnoreCase("lms")) {
							p.sendMessage(ChatColor.RED + "LMS Wins= old: " + stats.getLMSWins() + ", new: " + 0);
							if(p != Bukkit.getConsoleSender()) {
								Bukkit.getLogger().info(ChatColor.RED + "LMS Wins= old: " + stats.getLMSWins() + ", new: " + 0);
							}
							stats.setLmsWins(0);
						}
						p.sendMessage(ChatColor.RED + "Saving stats...");
						if(p != Bukkit.getConsoleSender()) {
							Bukkit.getLogger().info(ChatColor.RED + "Saving stats...");
						}
						stats.save();
						p.sendMessage(ChatColor.RED + of.getName() + "(UUID" + of.getUniqueId() + ") '" + statt + "' stats have been reset!");
						Bukkit.getLogger().info(p.getName() + " has reset " + of.getName() + "(UUID" + of.getUniqueId() + ")'s stats: " + statt);
					}
					else p.sendMessage(ChatColor.RED + "That player has not played on this server!");
				}
			});
			return true;
		}
		if(args.length > 1 && p instanceof Player) {
			if(args[0].equalsIgnoreCase("rename") || args[0].equalsIgnoreCase("renamehand")) {
				Player player = (Player) p;
				if(player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR)) {
					p.sendMessage(ChatColor.RED + "Hold something in your hand.");
					return true;
				}
				ItemStack hand = player.getItemInHand();
				ItemMeta meta = hand.getItemMeta();
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', args[1].replace("_", " ")));
				hand.setItemMeta(meta);
				player.getInventory().setItemInHand(hand);
				player.updateInventory();
				p.sendMessage(ChatColor.GOLD + "The item in your hand has been renamed!");
				return true;
			}
			else if(args[0].toLowerCase().contains("airhorizontal")) {
				if(plugin.getKnockbackManager() == null) {
					p.sendMessage(ChatColor.RED + "Knockback is not enabled in the config!");
					return true;
				}
				double kb;
				try{
					kb = Double.parseDouble(args[1]);
				}catch(IllegalArgumentException e) {
					p.sendMessage(ChatColor.RED + "That's not a number!");
					return true;
				}
				KnockbackValues values = plugin.getKnockbackManager().getKnockback(args[0].replace("airhorizontal", ""));
				values.setAirHorizontalMultiplier(kb);
				values.save();
				p.sendMessage(ChatColor.GREEN + "KB set: " + kb);
				return true;
			}
			else if(args[0].toLowerCase().contains("airvertical")) {
				if(plugin.getKnockbackManager() == null) {
					p.sendMessage(ChatColor.RED + "Knockback is not enabled in the config!");
					return true;
				}
				double kb;
				try{
					kb = Double.parseDouble(args[1]);
				}catch(IllegalArgumentException e) {
					p.sendMessage(ChatColor.RED + "That's not a number!");
					return true;
				}
				KnockbackValues values = plugin.getKnockbackManager().getKnockback(args[0].replace("airvertical", ""));
				values.setAirVerticalMultiplier(kb);
				values.save();
				p.sendMessage(ChatColor.GREEN + "KB set: " + kb);
				return true;
			}
			else if(args[0].toLowerCase().contains("horizontal")) {
				if(plugin.getKnockbackManager() == null) {
					p.sendMessage(ChatColor.RED + "Knockback is not enabled in the config!");
					return true;
				}
				double kb;
				try{
					kb = Double.parseDouble(args[1]);
				}catch(IllegalArgumentException e) {
					p.sendMessage(ChatColor.RED + "That's not a number!");
					return true;
				}
				KnockbackValues values = plugin.getKnockbackManager().getKnockback(args[0].replace("horizontal", ""));
				values.setHorizontalMultiplier(kb);
				values.save();
				p.sendMessage(ChatColor.GREEN + "KB set: " + kb);
				return true;
			}
			else if(args[0].toLowerCase().contains("vertical")) {
				if(plugin.getKnockbackManager() == null) {
					p.sendMessage(ChatColor.RED + "Knockback is not enabled in the config!");
					return true;
				}
				double kb;
				try{
					kb = Double.parseDouble(args[1]);
				}catch(IllegalArgumentException e) {
					p.sendMessage(ChatColor.RED + "That's not a number!");
					return true;
				}
				KnockbackValues values = plugin.getKnockbackManager().getKnockback(args[0].replace("vertical", ""));
				values.setVerticalMultiplier(kb);
				values.save();
				p.sendMessage(ChatColor.GREEN + "KB set: " + kb);
				return true;
			}
			else if(args[0].toLowerCase().equalsIgnoreCase("onlycombo")) {
				if(plugin.getKnockbackManager() == null) {
					p.sendMessage(ChatColor.RED + "Knockback is not enabled in the config!");
					return true;
				}
				plugin.getKnockbackManager().setOnlyCombo(!plugin.getKnockbackManager().isOnlyCombo());
				plugin.getKnockbackManager().save();
				p.sendMessage(ChatColor.GREEN + "Only-combo: " + plugin.getKnockbackManager().isOnlyCombo());
				return true;
			}
			else if(args[0].equalsIgnoreCase("addmeta")) {
				String meta = args[1];
				((Player) p).setMetadata(meta, new FixedMetadataValue(plugin, true));
				p.sendMessage(ChatColor.GREEN + "Meta added: " + meta);
				return true;
			}
			else if(args[0].equalsIgnoreCase("removemeta")) {
				String meta = args[1];
				((Player) p).removeMetadata(meta, plugin);
				p.sendMessage(ChatColor.GREEN + "Meta removed: " + meta);
				return true;
			}
			else if(args[0].equalsIgnoreCase("config")) {
				String s = args[1];
				p.sendMessage(s + ": " + plugin.getConfig().get(s));
				return true;
			}
			else if(args[0].equalsIgnoreCase("tempworld")) {
				String name = args[1];
				p.sendMessage(ChatColor.RED + "Creating a temporary world: " + name);
				World w = new WorldCreator(name).createWorld();
				BuildWorldDelete.worldsCreated.add(w.getName());
				p.sendMessage(ChatColor.RED + "The world will be deleted when the plugin is disabled!");
				((Player) p).teleport(w.getSpawnLocation().add(0, 5, 0));
				return true;
			}
			else if(args[0].equalsIgnoreCase("cancel")) {
				Player target = Bukkit.getPlayer(args[1]);
				if(target == null) {
					p.sendMessage(ChatColor.RED + "The player is not online!");
					return true;
				}
				Fight fight = Fight.getCurrentFight(target, plugin);
				if(fight == null) {
					p.sendMessage(ChatColor.RED + "The player is not in a match.");
					return true;
				}
				String reason = "";
				for(int i = 2; i < args.length; i++) {
					reason += " " + args[i];
				}
				p.sendMessage(ChatColor.RED + "Cancelling " + target.getName() + "'s fight.");
				fight.forceEnd(reason.length() == 0 ? ChatColor.RED + "The fight was forced to end!" : ChatColor.translateAlternateColorCodes('&', reason));
				for(Player pl : Bukkit.getOnlinePlayers()) {
					if(PermissionsManager.hasPermission(pl, Permission.STAFF)) {
						pl.sendMessage(ChatColor.RED + p.getName() + " cancelled " + target.getName() + "'s fight" + (reason.length() > 0 ? ": " + reason : "."));
					}
				}
				return true;
			}
			else if(args[0].equalsIgnoreCase("world")) {
				World world = null;
				for(World w : Bukkit.getWorlds()) {
					if(w.getName().equalsIgnoreCase(args[1])) {
						world = w;
					}
				}
				if(world == null) {
					p.sendMessage(ChatColor.RED + "Invalid world!");
				}
				else {
					((Player) p).teleport(world.getSpawnLocation().add(0, 5, 0));
				}
				return true;
			}
			else if(args[0].equalsIgnoreCase("speed")) {
				try{
					double d = Integer.parseInt(args[1]);
					((Player) p).setWalkSpeed((float) (d/10));
					((Player) p).setFlySpeed((float) (d/10));
				}catch(IllegalArgumentException e) {
					p.sendMessage(ChatColor.YELLOW + "/LegionPractice speed <1-10>");
				}
				return true;
			}
			else if(args[0].equalsIgnoreCase("info")) {
				playerInfo((Player) p, args[1]);
				return true;
			}
		}
		if(args.length == 3) {
			if(args[0].equalsIgnoreCase("stackarenas")) {
				World world = Bukkit.getWorld(args[1]);
				if(world == null) p.sendMessage(ChatColor.RED + "That world doesn't exist.");
				else if(world.getPlayers().size() > 0) p.sendMessage(ChatColor.RED + "Could not unload! The world still has players.");
				else {
					String newWorldName = args[2];
					p.sendMessage(ChatColor.BLUE + "Creating the world " + newWorldName + " and stacking arenas in the world " + newWorldName);
					plugin.getWorldStacker().stack(world, newWorldName);
				}
				return true;
			}
			else if(args[0].equalsIgnoreCase("rollbackelo") || args[0].equalsIgnoreCase("revive")
					|| args[0].equalsIgnoreCase("reviveelo")) {
				Player target = Bukkit.getPlayer(args[1]);
				if(target == null) {
					Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

						@SuppressWarnings("deprecation")
						@Override
						public void run() {
							OfflinePlayer o = Bukkit.getOfflinePlayer(args[1]);
							Bukkit.getScheduler().runTask(plugin, new Runnable() {

								@Override
								public void run() {
									if(p != null) {
										UUID uuid = null;
										try{
											uuid = UUID.fromString(args[1]);
										}catch(IllegalArgumentException e) {}
										if(o.hasPlayedBefore() || uuid != null) {
											if(uuid == null) {
												uuid = o.getUniqueId();
											}
											int i = 0;
											try{
												i = Integer.parseInt(args[2]);
											}catch(IllegalArgumentException e) {
												p.sendMessage(ChatColor.RED + "/sprac rollbackelo <online player:uuid> <number of fights>");
												return;
											}
											if(!Duel.eloFights.containsKey(uuid)) {
												p.sendMessage(ChatColor.RED + "No recent ranked matchs found!");
												return;
											}
											List<EloChange> fights = Duel.eloFights.get(uuid);
											p.sendMessage(ChatColor.RED + "Reviving " + uuid + "'s " + i + " previous ranked fights...");
											ListIterator<EloChange> li = fights.listIterator(fights.size());
											PlayerStats stats = PlayerStats.getStats(uuid, true, false);
											while(li.hasPrevious() && stats != null) {
												i--;
												EloChange pr = li.previous();
												int current = stats.getElo(pr.getKit());
												int newElo = current-pr.getEloChange();
												stats.getElos().put(Stats.elo(pr.getKit()), newElo);
												p.sendMessage(ChatColor.GRAY + "Kit: " + pr.getKit().getName() + ", old elo: " + current + ", new elo: " + newElo + ", change: " + pr.getEloChange());
												if(i == 0) break;
											}
											p.sendMessage(ChatColor.GREEN + "Done. saving stats...");
											stats.save(true);
											p.sendMessage(ChatColor.GREEN + "Completed!");
										}
										else p.sendMessage(ChatColor.RED + "That player has not played on this server!");
									}
									return;
								}
							});
						}
					});
				}
				else {
					UUID uuid = target.getUniqueId();
					int i = 0;
					try{
						i = Integer.parseInt(args[2]);
					}catch(IllegalArgumentException e) {
						p.sendMessage(ChatColor.RED + "/sprac rollbackelo <online player:uuid> <number of fights>");
						return true;
					}
					if(!Duel.eloFights.containsKey(uuid)) {
						p.sendMessage(ChatColor.RED + "No recent ranked matchs found!");
						return true;
					}
					List<EloChange> fights = Duel.eloFights.get(uuid);
					p.sendMessage(ChatColor.RED + "Reviving " + uuid + "'s " + i + " previous ranked fights...");
					ListIterator<EloChange> li = fights.listIterator(fights.size());
					PlayerStats stats = PlayerStats.getStats(uuid, true, false);
					while(li.hasPrevious() && stats != null) {
						EloChange pr = li.previous();
						int current = stats.getElo(pr.getKit());
						int newElo = current+pr.getEloChange();
						stats.getElos().put(Stats.elo(pr.getKit()), newElo);
						p.sendMessage(ChatColor.GRAY + "Kit: " + pr.getKit().getName() + ", old elo: " + current + ", new elo: " + newElo);
					}
					p.sendMessage(ChatColor.GREEN + "Done, saving stats...");
					stats.save(true);
					p.sendMessage(ChatColor.GREEN + "Saving completed!");
				}
				return true;
			}
		}
		p.sendMessage(ChatColor.GOLD + "LegionPractice - Toppe5's PvP Practice Plugin. Version: " + plugin.getDescription().getVersion());
		p.sendMessage(ChatColor.YELLOW + "/sprac setlobby");
		p.sendMessage(ChatColor.YELLOW + "/sprac setediting");
		p.sendMessage(ChatColor.YELLOW + "/sprac reload");
		//p.sendMessage(ChatColor.YELLOW + "/sprac stackarenas <world to stack> " + "<new world>");
		p.sendMessage(ChatColor.YELLOW + "/sprac info <player>");
		p.sendMessage(ChatColor.YELLOW + "/sprac arenasworld");
		p.sendMessage(ChatColor.YELLOW + "/sprac renamehand <name>");
		p.sendMessage(ChatColor.YELLOW + "/sprac savearenas");
		p.sendMessage(ChatColor.YELLOW + "/sprac savekits");
		p.sendMessage(ChatColor.YELLOW + "/sprac savelanguageitems");
		p.sendMessage(ChatColor.YELLOW + "/sprac savespawnitems");
		p.sendMessage(ChatColor.YELLOW + "/sprac position");
		p.sendMessage(ChatColor.YELLOW + "/sprac checkupdate");
		//p.sendMessage(ChatColor.YELLOW + "/sprac changelog");
		p.sendMessage(ChatColor.YELLOW + "/sprac totalfights");
		p.sendMessage(ChatColor.YELLOW + "/sprac spawncorner1 and spawncorner2");
		p.sendMessage(ChatColor.YELLOW + "/sprac cancel <player>");
		p.sendMessage(ChatColor.YELLOW + "/sprac resetelo <kit> (resets everyone's elo)");
		p.sendMessage(ChatColor.YELLOW + "/sprac resetstats <player> [elo, kitName, deaths, kills, lms, brackets, partywins, all]");
		p.sendMessage(ChatColor.YELLOW + "/sprac reviveelo <player:uuid> <number of fights>");
		p.sendMessage(ChatColor.YELLOW + "/sprac savedata");
		p.sendMessage(ChatColor.YELLOW + "/sprac goldenhead");
		p.sendMessage(ChatColor.GOLD + "Knockback:");
		p.sendMessage(ChatColor.YELLOW + "/sprac onlycombo");
		p.sendMessage(ChatColor.YELLOW + "/sprac horizontal/vertical <value>");
		p.sendMessage(ChatColor.YELLOW + "/sprac airhorizontal/vertical <value>");
		p.sendMessage(ChatColor.YELLOW + "/sprac combohorizontal/vertical <value>");
		p.sendMessage(ChatColor.YELLOW + "/sprac comboairhorizontal/vertical <value>");
		return true;
	}

	public void playerInfo(Player p, String offlinePlayer) {
		Player pl = Bukkit.getPlayer(offlinePlayer);
		if(pl != null) {
			LegionPracticeCommand.this.showInfo(p, pl);
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				OfflinePlayer o = Bukkit.getOfflinePlayer(offlinePlayer);
				Bukkit.getScheduler().runTask(plugin, new Runnable() {

					@Override
					public void run() {
						if(p != null) {
							if(o.hasPlayedBefore()) {
								LegionPracticeCommand.this.showInfo(p, o);
							}
							else p.sendMessage(ChatColor.RED + "That player has not played on this server!");
						}
					}
				});
			}
		});
	}

	private void showInfo(Player p, OfflinePlayer of) {
		String name = of.getName();
		UUID uuid = of.getUniqueId();
		Inventory inv = Bukkit.createInventory(new PlayerInfoHolder(uuid), 27, ChatColor.RED + "SP Info: " + name);
		PlayerDataFile dataFile = new PlayerDataFile(plugin, uuid.toString(), false);
		YamlConfiguration conf = dataFile.getConfig();
		inv.setItem(4 ,ItemStackUtils.createItem(Material.SKULL_ITEM, ChatColor.BLUE + "Name: " + name, (byte) 3, ChatColor.GRAY + "UUID: " + uuid.toString()));
		inv.setItem(1, ItemStackUtils.createItem(Material.BOOK_AND_QUILL, conf == null ? ChatColor.RED + "Data file exists: false" : ChatColor.YELLOW + "Data file exists: true"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		inv.setItem(7, ItemStackUtils.createItem(Material.COMPASS, ChatColor.BLUE + "Played", (byte)0, Arrays.asList(ChatColor.GRAY + "First played: " + sdf.format(of.getFirstPlayed()), ChatColor.GRAY + "Last played: " + sdf.format(of.getLastPlayed()))));
		if(conf != null) {
			BattleKit kit = plugin.getPlayerKitsHandler().loadFromFile(uuid, dataFile).getCustomKit();
			inv.setItem(10, ItemStackUtils.createItem(kit.getIcon().getType(), kit.getName(), (byte) 0, Arrays.asList(ChatColor.RED + "Click to preview the player's custom kit.", ChatColor.RED + "Middle click to delete the player's custom kit.")));
			if(!of.isOnline()) inv.setItem(16, ItemStackUtils.createItem(Material.PAPER, ChatColor.BLUE + "Language: " + (conf.get("language") == null ? LanguageManager.getDefaultLanguage(null) : conf.getString("language"))));
		}
		if(of.isOnline()) {
			Player tar = (Player) of;
			inv.setItem(16, ItemStackUtils.createItem(Material.PAPER, ChatColor.BLUE + "Language: " + PlayerSettings.getPlayerSettings(tar.getUniqueId()).getLanguage()));
			if(Fight.getCurrentFight(tar, plugin) != null) {
				inv.setItem(19, ItemStackUtils.createItem(Material.DIAMOND_SWORD, ChatColor.BLUE + "In Fight: true", (byte)0, ChatColor.RED + "Click to teleport to the fight arena (Arena: " + (Fight.getCurrentFight(tar, plugin).getArena() == null ? "unknown arena" : Fight.getCurrentFight(tar, plugin).getArena().getName()) + ")"));
			}
			else if(tar.hasMetadata(plugin.IN_FIGHT)) inv.setItem(19, ItemStackUtils.createItem(Material.DIAMOND_SWORD, ChatColor.BLUE + "In Fight: true"));
			else inv.setItem(19, ItemStackUtils.createItem(Material.DIAMOND_SWORD, ChatColor.BLUE + "In Fight: false"));
			inv.setItem(22, ItemStackUtils.createItem(Material.FEATHER, ChatColor.BLUE + "Gamemode and flying", (byte) 0, Arrays.asList(ChatColor.GRAY + "Gamemode: " + tar.getGameMode(), ChatColor.GRAY + "Flying: " + tar.isFlying(), ChatColor.GRAY + "Allow Flight: " + tar.getAllowFlight())));
			Fight fight = Fight.getCurrentFight(tar, plugin);
			String opponent = "n/a";
			if(fight instanceof Duel) {
				Duel duel = (Duel) fight;
				if(duel.getP1().equals(tar.getName())) opponent = duel.getP2();
				else if(duel.getP2().equals(tar.getName())) opponent = duel.getP1();
			}
			inv.setItem(13, ItemStackUtils.createItem(Material.ENCHANTED_BOOK, ChatColor.BLUE + "LegionPractice Info:", (byte) 0, Arrays.asList(ChatColor.GRAY + "Spectator: " + plugin.getSpectatorHandler().isSpectator(tar), ChatColor.GRAY + "Max no damage ticks: " + tar.getMaximumNoDamageTicks() + " - No damage ticks: " + tar.getNoDamageTicks() + " - Kit: " + (BattleKit.getCurrentKit(tar) == null ? "n/a" : BattleKit.getCurrentKit(tar).getName()), ChatColor.GRAY + "In event: " + PvPEvent.getEventString(tar), ChatColor.GRAY + "1v1 Opponent: " + opponent)));
			Party party = Party.getParty(tar);
			if(party != null) {
				inv.setItem(25, ItemStackUtils.createItem(Material.EYE_OF_ENDER, ChatColor.BLUE + "In Party: true", (byte) 0, Arrays.asList(ChatColor.YELLOW + "Owner: " + party.getOwner(), ChatColor.GRAY + "Members: " + party.getMembers().size(), ChatColor.GRAY + "In Fight: " + (party.isInFight()), ChatColor.RED + "Click to disband")));
			}
			else inv.setItem(25, ItemStackUtils.createItem(Material.EYE_OF_ENDER, ChatColor.BLUE + "In Party: false"));
		}
		p.openInventory(inv);
	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(e.getCurrentItem() != null && e.getWhoClicked() instanceof Player 
				&& e.getInventory() != null && e.getClickedInventory() != null) {
			Player p = (Player) e.getWhoClicked();
			ItemStack item = e.getCurrentItem();
			if(e.getClickedInventory().getHolder() != null
					&& e.getClickedInventory().getHolder() instanceof PlayerInfoHolder
					&& e.getClickedInventory().getName() != null 
					&& e.getClickedInventory().getName().startsWith(ChatColor.RED + "SP Info: ")) {
				e.setCancelled(true);
				UUID uuid = ((PlayerInfoHolder) e.getClickedInventory().getHolder()).getUUID();
				if(item == null || item.getItemMeta() == null || item.getItemMeta().getLore() == null) return;
				if(e.getSlot() == 25 && !item.getItemMeta().getLore().isEmpty()) {
					String name = Bukkit.getServer().getOfflinePlayer(uuid).getName();
					Inventory inv = Bukkit.createInventory(null, 27, "Disband party: " + name);
					inv.setItem(11, ItemStackUtils.createItem(Material.WOOL, ChatColor.DARK_RED + "Disband " + name + " 's party." , (byte) 14));
					inv.setItem(15, ItemStackUtils.createItem(Material.WOOL, ChatColor.DARK_RED + "Cancel", (byte) 5));
					p.closeInventory();
					p.openInventory(inv);
				}
				else if(e.getSlot() == 19) {
					Player tar = Bukkit.getPlayer(uuid);
					if(tar == null) {
						p.closeInventory();
						p.sendMessage(ChatColor.RED + "The player is no longer online!");
					}
					Fight fight = Fight.getCurrentFight(tar, plugin);
					if(fight != null && fight.getArena() == null || fight.getArena().getCenter() == null || Bukkit.getWorld(fight.getArena().getCenter().getWorld().getName()) == null) {
						p.sendMessage(ChatColor.RED + "Invalid arena!");
					}
					else {
						p.closeInventory();
						p.sendMessage(ChatColor.GRAY + "Teleporting...");
						p.teleport(fight.getArena().getCenter());
					}
				}
				else if(e.getSlot() == 10) {
					if(e.getClick().equals(ClickType.MIDDLE)) {
						String name = Bukkit.getServer().getOfflinePlayer(uuid).getName();
						Inventory inv = Bukkit.createInventory(null, 27, "Delete kit: " + name);
						inv.setItem(11, ItemStackUtils.createItem(Material.WOOL, ChatColor.DARK_RED + "Delete " + name + " 's custom kit." , (byte) 14));
						inv.setItem(15, ItemStackUtils.createItem(Material.WOOL, ChatColor.DARK_RED + "Cancel", (byte) 5));
						p.closeInventory();
						p.openInventory(inv);
					}
					else {
						Player tar = Bukkit.getPlayer(uuid);
						if(tar != null) {
							Preview.preview(p, plugin.getPlayerKitsHandler().getPlayerKits(tar).getCustomKit(), plugin);
						}
						else {
							PlayerDataFile dataFile = new PlayerDataFile(plugin, uuid.toString(), false);
							YamlConfiguration conf = dataFile.getConfig();
							if(conf != null) {
								BattleKit kit = plugin.getPlayerKitsHandler().loadFromFile(uuid, dataFile).getCustomKit();
								Preview.preview(p, kit, plugin);
							}
						}
					}
				}
			}
			else if(e.getClickedInventory().getName().startsWith("Disband party: ")) {
				if(item.getDurability() == 5) p.closeInventory();
				else if(item.getDurability() == 14) {
					String name = e.getClickedInventory().getName().replace("Disband party: ", "");
					Player tar = Bukkit.getPlayer(name);
					if(tar != null) {
						Party party = Party.getParty(tar);
						if(party != null) {
							for(String s : party.getMembers()) {
								Player mem = Bukkit.getPlayer(s);
								mem.sendMessage(plugin.translateMessage(mem, "party-was-deleted"));
							}
							party.disbandParty();
							p.sendMessage(ChatColor.BLUE + "The party was disbanded!");
						}
						else p.sendMessage(ChatColor.RED + "That player doesn't have a party anymore.");
					}
					else p.sendMessage(ChatColor.RED + "Player not found!");
					p.closeInventory();
				}
			}
			else if(e.getClickedInventory().getName().startsWith("Delete kit: ")) {
				@SuppressWarnings("deprecation")
				UUID uuid = Bukkit.getOfflinePlayer(e.getClickedInventory().getName().replace("Delete kit: ", "")).getUniqueId();
				if(item.getDurability() == 5) p.closeInventory();
				else if(item.getDurability() == 14) {
					PlayerDataFile dataFile = new PlayerDataFile(plugin, uuid.toString(), false);
					YamlConfiguration conf = dataFile.getConfig();
					if(conf != null) {
						Player tar = Bukkit.getPlayer(uuid);
						if(tar != null) plugin.getPlayerKitsHandler().setCustomKitMeta(tar, plugin.getPlayerKitsHandler().getDefaultKit()); 
						conf.set("kit", plugin.getPlayerKitsHandler().getDefaultKit());
						Bukkit.getLogger().info(p.getName() + " has reset the custom kit of the uuid " + uuid);
						p.sendMessage(ChatColor.RED + "You have reset the player's custom kit!");
						p.closeInventory();
					}
				}
			}
		}
	}

	class PlayerInfoHolder implements InventoryHolder {

		private UUID uuid;

		public PlayerInfoHolder(UUID uuid) {
			this.uuid = uuid;
		}

		public UUID getUUID() {
			return uuid;
		}

		@Override
		public Inventory getInventory() {
			return null;
		}
	}
}
