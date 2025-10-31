package tech.hexadevelopment.practice.scoreboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.epearlcooldown.PearlListener;
import tech.hexadevelopment.practice.epearlcooldown.PearlManager;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.misc.ReportHook;
import tech.hexadevelopment.practice.misc.SketchSMHook;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.placeholders.PlaceholderMode;
import tech.hexadevelopment.practice.playersettings.PlayerSettings;
import tech.hexadevelopment.practice.utils.LocationUtil;
import tech.hexadevelopment.practice.utils.SimpleLagMeter;
import tech.hexadevelopment.practice.utils.TPSUtil;

public class ScoreboardManager {

	private static boolean epearlOnlyInFight;
	private static int errors, nameTagErrors;

	private LegionPractice plugin;
	private static ScoreboardManager scoreboardManager;
	private HashMap<PlaceholderMode, List<String>> scoreboardLines = new HashMap<PlaceholderMode, List<String>>();
	private List<String> partyLines = new ArrayList<String>();
	private List<String> epearlLines = new ArrayList<String>();
	private HashMap<UUID, Board> scoreboards = new HashMap<UUID, Board>();
	private static List<ScoreboardCallback> callbacks = new ArrayList<ScoreboardCallback>();
	private static String team1Prefix;
	private static String team2Prefix;
	public ScoreboardUpdater updater;
	private String adminColor1 = ChatColor.GOLD.toString(), adminColor2 = ChatColor.YELLOW.toString();
	private List<String> disabledWorlds;
	private boolean hasDisabledWorlds;


	public ScoreboardManager(LegionPractice plugin) {
		this.plugin = plugin;
		disabledWorlds = plugin.getConfig().getStringList("scoreboard.disabled-worlds");
		hasDisabledWorlds = !disabledWorlds.isEmpty();
		adminColor1 = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("admin-scoreboard-color1"));
		adminColor2 = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("admin-scoreboard-color2"));
		epearlOnlyInFight = plugin.getConfig().getBoolean("scoreboard.enderpearl-cooldown-only-in-fight");
		scoreboardManager = this;
		for(PlaceholderMode type : PlaceholderMode.values()) {
			List<String> list = plugin.getConfig().getStringList("scoreboard." + type.toString());
			int counter = 0;
			for(String s : list) {
				String test = s;
				int length = 46;
				for(String str : test.split("<")) {
					for(String str2 : str.split(">")) {
						if(!str2.contains("[") && !str2.contains("]")) {
							test = test.replace("<" + str2 + ">", "true");
							while(test.contains("[display=true]")) {
								test = test.replace("[display=true]", "");
								length += 14;
							}
							while(test.contains("[display=!true]")) {
								test = test.replace("[display=!true]", "");
								length += 15;
							}
						}
					}
				}
				for(String str2 : test.split("%")) {
					if(!str2.contains("[") && !str2.contains("]")) {
						test = test.replace("%" + str2 + "%", "true");
						while(test.contains("[display=true]")) {
							test = test.replace("[display=true]", "");
							length += 14;
						}
						while(test.contains("[display=!true]")) {
							test = test.replace("[display=!true]", "");
							length += 15;
						}
					}
				}
				while(s.length() <= length) {
					s += ChatColor.values()[LegionPractice.random.nextInt(ChatColor.values().length)].toString();
				}
				list.set(counter, ChatColor.translateAlternateColorCodes('&', s));
				counter++;
			}
			scoreboardLines.put(type, list);
		}
		for(String s : plugin.getConfig().getStringList("scoreboard.party-addition")) {
			while(s.length() <= 46) {
				s += ChatColor.values()[LegionPractice.random.nextInt(ChatColor.values().length)].toString();
			}
			partyLines.add(ChatColor.translateAlternateColorCodes('&', s));
		}
		for(String s : plugin.getConfig().getStringList("scoreboard.enderpearl-cooldown-addition")) {
			while(s.length() <= 46) {
				s += ChatColor.values()[LegionPractice.random.nextInt(ChatColor.values().length)].toString();
			}
			epearlLines.add(ChatColor.translateAlternateColorCodes('&', s));
		}
		team1Prefix = ChatColor.translateAlternateColorCodes('&', LegionPractice.getInstance().getConfig().getString("team1-prefix"));
		team2Prefix = ChatColor.translateAlternateColorCodes('&', LegionPractice.getInstance().getConfig().getString("team2-prefix"));
		updater = new ScoreboardUpdater(plugin);
		Bukkit.getPluginManager().registerEvents(new ScoreboardListener(), plugin);
		new BukkitRunnable() {

			@Override
			public void run() {
				updateScoreboards();	
			}
		}.runTaskLater(plugin, 10);
	}

	public void updateScoreboards() {
		for(Player p : Bukkit.getOnlinePlayers()) {
			setScoreboard(p, PlaceholderMode.getCurrentMode(p));
		}
		updateTags();
	}

	public void updateTags() {
		try{
			Iterator<ScoreboardCallback> iterator = new ArrayList<ScoreboardCallback>(callbacks).iterator();
			while(iterator.hasNext()) {
				ScoreboardCallback cb = iterator.next();
				for(Player p : Bukkit.getOnlinePlayers()) {
					cb.onScoreboardUpdate(p);
				}
				iterator.remove();
			}
		}catch(Exception e) {
			nameTagErrors++;
			if(nameTagErrors > 10) {
				Bukkit.getLogger().warning("Failed to handle nametag colors!");
				e.printStackTrace();
			}
		}
	}

	public PlaceholderMode updateScoreboard(Player p) {
		PlaceholderMode mode = null;
		if(ScoreboardUpdater.needsMode) {
			mode = PlaceholderMode.getCurrentMode(p);
		}
		PlaceholderMode finalMode = mode;
		if(ScoreboardUpdater.enhancedPerformance) {
			Bukkit.getScheduler().runTaskAsynchronously(LegionPractice.getInstance(), new Runnable() {

				@Override
				public void run() {
					if(hasDisabledWorlds && disabledWorlds.contains(p.getWorld().getName().toLowerCase())
							&& scoreboards.containsKey(p.getUniqueId())) {
						Bukkit.getScheduler().runTask(LegionPractice.getInstance(), new Runnable() {

							@Override
							public void run() {
								disableScoreboard(p);
							}
						});
					}
					else {
						setScoreboard(p, finalMode != null ? finalMode : PlaceholderMode.getCurrentMode(p));
					}
				}
			});
		}
		else {
			if(hasDisabledWorlds && disabledWorlds.contains(p.getWorld().getName().toLowerCase())
					&& scoreboards.containsKey(p.getUniqueId())) {
				disableScoreboard(p);
			}
			else {
				setScoreboard(p, mode = PlaceholderMode.getCurrentMode(p));
			}
		}
		return mode;
	}

	public void disableScoreboard(Player p) {
		if(p != null) {
			Board board = scoreboards.get(p.getUniqueId());
			if(board != null) {
				board.removeAll();
			}
			if(p.getScoreboard() == board.getScoreboard()) {
				p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			}
			scoreboards.remove(p.getUniqueId());
		}
	}
	
	public void handleDisabledWorlds(PlayerTeleportEvent e) {
		if(hasDisabledWorlds && disabledWorlds.contains(e.getTo().getWorld().getName().toLowerCase())
				&& scoreboards.containsKey(e.getPlayer().getUniqueId())) {
			if(Bukkit.isPrimaryThread()) disableScoreboard(e.getPlayer());
			else new BukkitRunnable() {
				
				@Override
				public void run() {
					disableScoreboard(e.getPlayer());
				}
			}.runTask(plugin);
		}
	}


	private void setScoreboard(Player p, PlaceholderMode holderMode) {
		try{
			if(ScoreboardListener.tooEarly(p)) return;
			Board board;
			if(PlayerSettings.getPlayerSettings(p).isScoreboardDisabled()) {
				if(p.getScoreboard() == Bukkit.getScoreboardManager().getMainScoreboard()) {
					Bukkit.getScheduler().runTask(LegionPractice.getInstance(), new Runnable() {

						@Override
						public void run() {
							if(p != null) {
								p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
							}
						}
					});
				}
				if(scoreboards.containsKey(p.getUniqueId())) {
					board = scoreboards.get(p.getUniqueId());
					if(board != null) {
						board.removeAll();
					}
					scoreboards.remove(p.getUniqueId());
				}
				return;
			}
			if(scoreboards.containsKey(p.getUniqueId())) {
				board = scoreboards.get(p.getUniqueId());
			}
			else {
				board = new Board(p, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("scoreboard.title")));
				scoreboards.put(p.getUniqueId(), board);
			}
			if(!board.created) return;
			board.addPlayer(p);
			boolean removeAll = false;
			if(board.getPlaceholderMode() != holderMode) {
				removeAll = true;
			}		
			List<String> sbLines = new ArrayList<String>(scoreboardLines.get(holderMode));
			if(Party.getParty(p) != null && holderMode == PlaceholderMode.DEFAULT) {
				if(!board.party) {
					removeAll = true;
					board.party = true;
				}
				sbLines.addAll(partyLines);
			}
			else if(board.party) {
				board.party = false;
				removeAll = true;
			}
			if(p.hasMetadata(PearlListener.COOLDOWN_META) && (!epearlOnlyInFight || Fight.getCurrentFight(p, plugin) != null)) {
				MetadataValue m = plugin.getMetadata(p, PearlListener.COOLDOWN_META);
				if(m != null && m.value() != null) {
					double i = PearlManager.getCooldown()*1000;
					double l = m.asLong();
					double c = System.currentTimeMillis();
					if(l+i > c) {
						sbLines.addAll(epearlLines);
						if(!board.epearlCooldown) {
							removeAll = true;
							board.epearlCooldown = true;
						}
					}
					else {
						p.removeMetadata(PearlListener.COOLDOWN_META, plugin);
						board.epearlCooldown = false;
						removeAll = true;
					}
				}
			}
			else if(board.epearlCooldown) {
				p.removeMetadata(PearlListener.COOLDOWN_META, plugin);
				board.epearlCooldown = false;
				removeAll = true;
			}
			if(PlayerSettings.getPlayerSettings(p).isAdminScoreboard()) {
				if(!board.adminScoreboard) {
					board.adminScoreboard = true;
					removeAll = true;
				}
				if(ReportHook.lastReport != null) {
					sbLines.add(ChatColor.GOLD + "Last report: " + ChatColor.YELLOW + "<last_report>");
				}
				long l = SimpleLagMeter.getDelay()-1000;
				sbLines.add(adminColor1 + "Lag: " + adminColor2 + TPSUtil.get1MinTPSRounded(2) + " tps (" + (l > 0 ? l : 0) + " ms)");
				sbLines.add(adminColor1 + "Staff members online:" + adminColor2 + " <online_staff>");
				sbLines.add(adminColor1 + "Players online:" + adminColor2 + " <players>/<max_players>");
				sbLines.add(adminColor1 + "Available Arenas:" + adminColor2 + " <free_arenas>/<arenas>");
				if(Arena.needsMoreArenas != null) {
					if(!board.needsArenas) {
						board.needsArenas = true;
						removeAll = true;
					}
					sbLines.add(adminColor1 + "Needs arenas: " + adminColor2 + Arena.needsMoreArenas.getName());
				}
				else if(board.needsArenas) {
					board.needsArenas = false;
					removeAll = true;
				}
				Fight fight = Fight.getCurrentFight(p, plugin);
				if(fight != null) {
					String arena = fight.getArena().getName();
					if(arena.length() > 12) {
						sbLines.add(adminColor1 + "Arena:");
						sbLines.add(adminColor2 + arena);
					}
					else {
						sbLines.add(adminColor1 + "Arena:" + adminColor2 + arena);
					}
					sbLines.add(adminColor1 + " Kit: " + adminColor2 + fight.getKit().getName());
				}
				else {
					double distance = 100;
					Arena found = null;
					Location loc = ((Entity) p).getLocation();
					for(Arena arena : plugin.arenas) {
						if(arena.getCorner1() != null && arena.getCorner2() != null
								&& LocationUtil.isInregion(loc, arena.getCorner1(), arena.getCorner2())) {
							String arenaName = arena.getName();
							if(arenaName.length() > 12) {
								sbLines.add(adminColor1 + "Arena:");
								sbLines.add(adminColor2 + arenaName);
							}
							else {
								sbLines.add(adminColor1 + "Arena:" + adminColor2 + arenaName);
							}
						}
						if(arena.getCenter() != null && arena.getCenter().getWorld().getName().equals(loc.getWorld().getName())) {
							double dis = arena.getCenter().distance(loc);
							if(dis < distance) {
								distance = dis;
								found = arena;
							}
						}
					}
					if(found != null) {
						sbLines.add(adminColor1 + "Arena:");
						sbLines.add(adminColor2 + found.getName() + " (" + (int)distance + " blocks away)");
					}
				}
				if(SketchSMHook.hasSketchSM()) {
					if(fight == null) {
						sbLines.add(adminColor1 + "In Staff Mode: " + adminColor2 + SketchSMHook.isInStaffMode(p));
						sbLines.add(adminColor1 + "Currently Vanished: " + adminColor2 + SketchSMHook.isVanished(p));
					}
					else {
						if(SketchSMHook.isInStaffMode(p)) {
							sbLines.add(adminColor1 + "In Staff Mode: " + adminColor2 + "true");
						}
						if(SketchSMHook.isVanished(p)) {
							sbLines.add(adminColor1 + "Currently Vanished: " + adminColor2 + "true");
						}
					}
				}
			}
			else if(board.adminScoreboard) {
				removeAll = true;
				board.adminScoreboard = false;
			}
			board.setPlaceholderMode(holderMode);
			if(ScoreboardUpdater.enhancedPerformance) {
				if(ScoreboardUpdater.avoidErrors) {
					ListIterator<String> it = sbLines.listIterator();
					while(it.hasNext()) {
						String s = it.next();
						s = plugin.getPlaceholders().doPlaceholders(p, s, "", true);
						if(s.contains("[display=false]") || s.contains("[display=!true]")) {
							it.remove();
							continue;
						}
						s = s.replace("[display=true]", "").replace("[display=!false]", "");
						s = s.replace("[display=false]", "").replace("[display=!true]", "");
						if(s.length() > 48) {
							s = s.substring(0, 48);
						}
						char[] c = s.toCharArray();
						if(c[c.length-1] == '§') {
							s = s.substring(0, s.length()-1);
						}
						it.set(s);
					}
					boolean fRemove = removeAll;
					Bukkit.getScheduler().runTask(LegionPractice.getInstance(), new Runnable() {

						@Override
						public void run() {
							if(p != null) {
								boolean removeAll = fRemove;
								int counter = 0;
								for(String str : sbLines) {
									if(removeAll || board.getEntry(str) == null) {
										board.set(sbLines.size()-counter, str, removeAll);
										removeAll = false;
									}
									counter++;
									if(counter == 15) break;
								}
							}
						}
					});
					return;
				}
				else {
					boolean b = removeAll;
					Bukkit.getScheduler().runTask(LegionPractice.getInstance(), new Runnable() {

						@Override
						public void run() {
							if(p != null) {
								boolean removeAll = b;
								ListIterator<String> it = sbLines.listIterator();
								while(it.hasNext()) {
									String s = it.next();
									s = plugin.getPlaceholders().doPlaceholders(p, s, "", true);
									if(s.contains("[display=false]") || s.contains("[display=!true]")) {
										it.remove();
										continue;
									}
									s = s.replace("[display=true]", "").replace("[display=!false]", "");
									s = s.replace("[display=false]", "").replace("[display=!true]", "");
									if(s.length() > 48) {
										s = s.substring(0, 48);
									}
									char[] c = s.toCharArray();
									if(c[c.length-1] == '§') {
										s = s.substring(0, s.length()-1);
									}
									it.set(s);
								}
								int counter = 0;
								for(String str : sbLines) {
									if(removeAll || board.getEntry(str) == null) {
										board.set(sbLines.size()-counter, str, removeAll);
										removeAll = false;
									}
									counter++;
									if(counter == 15) break;
								}
							}
						}
					});
					return;
				}
			}
			ListIterator<String> it = sbLines.listIterator();
			while(it.hasNext()) {
				String s = it.next();
				s = plugin.getPlaceholders().doPlaceholders(p, s, "", true);
				if(s.contains("[display=false]") || s.contains("[display=!true]")) {
					it.remove();
					continue;
				}
				s = s.replace("[display=true]", "").replace("[display=!false]", "");
				s = s.replace("[display=false]", "").replace("[display=!true]", "");
				if(s.length() > 48) {
					s = s.substring(0, 48);
				}
				char[] c = s.toCharArray();
				if(c[c.length-1] == '§') {
					s = s.substring(0, s.length()-1);
				}
				it.set(s);
			}
			int counter = 0;
			for(String str : sbLines) {
				if(removeAll || board.getEntry(str) == null) {
					board.set(sbLines.size()-counter, str, removeAll);
					removeAll = false;
				}
				counter++;
				if(counter == 15) break;
			}
		}catch(Exception e) {
			Bukkit.getLogger().warning("Failed to update scoreboard for " + p.getName());
			errors++;
			if(errors < 10) {
				e.printStackTrace();
			}
		}
	}

	private static void addCallback(ScoreboardCallback scoreboardCallback) {
		callbacks.add(scoreboardCallback);
		if(callbacks.size() > 100) {
			callbacks.remove(0);
		}
	}

	public static void setTag(String player, String team, Collection<UUID> uuids) {
		if(team1Prefix == null || team2Prefix == null) {
			team1Prefix = ChatColor.translateAlternateColorCodes('&', LegionPractice.getInstance().getConfig().getString("team1-prefix"));
			team2Prefix = ChatColor.translateAlternateColorCodes('&', LegionPractice.getInstance().getConfig().getString("team2-prefix"));
		}
		if(isEnabled() && ScoreboardUpdater.async) {
			addCallback(new ScoreboardCallback() {

				@Override
				public void onScoreboardUpdate(Player p) {
					if(uuids.contains(p.getUniqueId())) {
						if(ScoreboardListener.tooEarly(p)) return;
						Scoreboard board = p.getScoreboard();
						if(board == Bukkit.getScoreboardManager().getMainScoreboard()) {
							addCallback(this);
							return;
						}
						Team color = board.getTeam(team);
						if (color == null) {
							color = board.registerNewTeam(team);
						}
						if(team.equals("team1")) {
							color.setPrefix(team1Prefix);
						}
						else if(team.equals("team2")) {
							color.setPrefix(team2Prefix);
						}
						color.addEntry(player);
					}
				}
			});
		}
		else {
			for(UUID uuid : uuids) {
				Player pl = Bukkit.getPlayer(uuid);
				if(pl != null && !ScoreboardListener.tooEarly(pl)) {
					Scoreboard board = pl.getScoreboard();
					if(board == Bukkit.getScoreboardManager().getMainScoreboard()) {
						if(!Bukkit.isPrimaryThread()) {
							Bukkit.getScheduler().runTask(LegionPractice.getInstance(), new Runnable() {

								@Override
								public void run() {
									Scoreboard board = pl.getScoreboard();
									if(board == Bukkit.getScoreboardManager().getMainScoreboard()) {
										board = Bukkit.getScoreboardManager().getNewScoreboard();
										pl.setScoreboard(board);
									}
									Team color = board.getTeam(team);
									if (color == null) {
										color = board.registerNewTeam(team);
									}
									if(team.equals("team1")) {
										color.setPrefix(team1Prefix);
									}
									else if(team.equals("team2")) {
										color.setPrefix(team2Prefix);
									}
									color.addEntry(player);
								}
							});
							return;
						}
						board = Bukkit.getScoreboardManager().getNewScoreboard();
						pl.setScoreboard(board);
					}
					Team color = board.getTeam(team);
					if (color == null) {
						color = board.registerNewTeam(team);
					}
					if(team.equals("team1")) {
						color.setPrefix(team1Prefix);
					}
					else if(team.equals("team2")) {
						color.setPrefix(team2Prefix);
					}
					color.addEntry(player);
				}
			}
		}
	}

	public static void removeTag(String player) {
		if(isEnabled() && ScoreboardUpdater.async) {
			addCallback(new ScoreboardCallback() {

				@Override
				public void onScoreboardUpdate(Player p) {
					if(ScoreboardListener.tooEarly(p)) return;
					Scoreboard board = p.getScoreboard();
					if(board == Bukkit.getScoreboardManager().getMainScoreboard()) {
						addCallback(this);
						return;
					}
					Team color1 = board.getTeam("team1");
					Team color2 = board.getTeam("team2");
					if(color1 != null && color1.getEntries().contains(player)) {
						color1.removeEntry(player);
					}
					if(color2 != null && color2.getEntries().contains(player)) {
						color2.removeEntry(player);
					}
				}
			});
		}
		else {
			for(Player pl : Bukkit.getOnlinePlayers()) {
				if(!ScoreboardListener.tooEarly(pl)) {
					Scoreboard board = pl.getScoreboard();
					if(board == Bukkit.getScoreboardManager().getMainScoreboard()) {
						if(!Bukkit.isPrimaryThread()) {
							Bukkit.getScheduler().runTask(LegionPractice.getInstance(), new Runnable() {

								@Override
								public void run() {
									Scoreboard board = pl.getScoreboard();
									if(board == Bukkit.getScoreboardManager().getMainScoreboard()) {
										board = Bukkit.getScoreboardManager().getNewScoreboard();
										pl.setScoreboard(board);
									}
									Team color1 = board.getTeam("team1");
									Team color2 = board.getTeam("team2");
									if(color1 != null && color1.getEntries().contains(player)) {
										color1.removeEntry(player);
									}
									if(color2 != null && color2.getEntries().contains(player)) {
										color2.removeEntry(player);
									}
								}
							});
							return;
						}
						board = Bukkit.getScoreboardManager().getNewScoreboard();
						pl.setScoreboard(board);
					}
					Team color1 = board.getTeam("team1");
					Team color2 = board.getTeam("team2");
					if(color1 != null && color1.getEntries().contains(player)) {
						color1.removeEntry(player);
					}
					if(color2 != null && color2.getEntries().contains(player)) {
						color2.removeEntry(player);
					}
				}
			}
		}
	}

	/**
	 * @return the scoreboardManager
	 */
	public static ScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}

	public static boolean isEnabled() {
		return scoreboardManager != null;
	}

	public HashMap<UUID, Board> getScoreboards() {
		return scoreboards;
	}

}
