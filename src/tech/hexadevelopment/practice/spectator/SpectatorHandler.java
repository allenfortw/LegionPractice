package tech.hexadevelopment.practice.spectator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.events.PlayerStartSpectatingEvent;
import tech.hexadevelopment.practice.events.PlayerStopSpectatingEvent;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.botduel.BotDuel;
import tech.hexadevelopment.practice.fights.duel.Duel;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyFFA;
import tech.hexadevelopment.practice.fights.party.partyfights.PartySplit;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsBots;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsParty;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.hostedevents.lms.LMSCommand;
import tech.hexadevelopment.practice.matchrecorder.playback.PlaybackFight;
import tech.hexadevelopment.practice.party.Party;
import tech.hexadevelopment.practice.scoreboard.ScoreboardManager;
import tech.hexadevelopment.practice.utils.ItemStackUtil;

public class SpectatorHandler {

	public static String SPECTATOR_META = "LegionPracticeSpectator";

	HashMap<UUID, Long> stayAwayMessages = new HashMap<UUID, Long>();
	private HashMap<UUID, Fight> spectatingFight = new HashMap<UUID, Fight>();
	private boolean stayAway;
	boolean hideOtherSpectators;
	//private Inventory spectateMatches;
	private LegionPractice plugin;
	private boolean enabled;
	private BukkitRunnable task;

	public SpectatorHandler(LegionPractice plugin) {
		this.plugin = plugin;
	}

	public void register() {
		stayAway = plugin.getConfig().getBoolean("spectator-stay-away");
		hideOtherSpectators = plugin.getConfig().getBoolean("hide-other-spectators");
		long period = plugin.getConfig().getLong("spectator-timer-period");
		if(period < 30 && LegionPractice.performanceMode) {
			period = 30;
		}
		if(period < 1) {
			period = 40;
			Bukkit.getLogger().warning("Spectator task period was set to 40 because it was under 1 in the config!");
		}
		enabled = true;
		task = new BukkitRunnable() {

			@Override
			public void run() {
				if(isEnabled()) {
					int tenCounter = 0;
					tenCounter++;
					if(!LegionPractice.performanceMode || tenCounter == 10) {
						tenCounter = 0;
					}
					for(Player p : Bukkit.getOnlinePlayers()) {
						if(p.hasMetadata(SPECTATOR_META)) {
							p.setHealth(20);
							p.setFoodLevel(20);
							p.setFireTicks(0);
							for(Player pl : Bukkit.getOnlinePlayers()) {
								if(Fight.isInFight(pl, plugin) || (PvPEvent.isInEvent(pl) && (!isSpectator(pl) || hideOtherSpectators))
										|| (isSpectator(pl) && hideOtherSpectators)) {
									if(pl.canSee(p)) {
										new BukkitRunnable() {
											
											@Override
											public void run() {
												pl.hidePlayer(p);
											}
										}.runTask(plugin);
									}
								}
							}
							if(stayAway && (!LegionPractice.performanceMode || tenCounter % 3 == 0)) {
								for(Entity ent : p.getNearbyEntities(5, 5, 5)) {
									if(ent instanceof Player) {
										Player np = (Player)ent;
										if(p.canSee(np) && !p.getUniqueId().equals(np.getUniqueId()) && (Fight.isInFight(np, plugin)
												|| (PvPEvent.isInEvent(np) && !isSpectator(np)))) {
											double Ax = p.getLocation().getX();
											double Ay = p.getLocation().getY();
											double Az = p.getLocation().getZ();
											double Bx = np.getLocation().getX();
											double By = np.getLocation().getY();
											double Bz = np.getLocation().getZ();
											double x = Ax - Bx;
											double y = Ay - By;
											double z = Az - Bz;
											Vector v = new Vector(x, y, z).normalize().multiply(1).setY(0.3);
											new BukkitRunnable() {
												
												@Override
												public void run() {
													p.setVelocity(v);
													if(p.getLocation().distanceSquared(np.getLocation()) < 3*3) {
														p.teleport(p.getLocation().add(0, 4, 0));
													}
													UUID uuid = p.getUniqueId();
													if(stayAwayMessages.containsKey(uuid)) {
														if(stayAwayMessages.get(uuid)+3000 < System.currentTimeMillis()) {
															stayAwayMessages.put(uuid, System.currentTimeMillis());
															p.sendMessage(plugin.translateMessage(p, "stay-away-from-other-players"));
														}
													}
													else stayAwayMessages.put(uuid, System.currentTimeMillis());
												}
											}.runTask(plugin);
										}
									}
								}
							}
						}
					}
				}
			}
		};
		if(LegionPractice.ASYNC_EVERYTHING) {
			task.runTaskTimerAsynchronously(plugin, 0, period);
		}
		else {
			task.runTaskTimer(plugin, 0, period);
		}
	}

	public static ItemStack item(Fight fight, Player p) {
		String skull = fight.getClass().getSimpleName();
		if(fight instanceof BotDuel) {
			skull = "Bot 1vs1 (" + p.getName() + ")";
		}
		else if(fight instanceof Duel) {
			skull = "1vs1 (" + ((Duel) fight).getP1() + " vs " + ((Duel) fight).getP2() + ")";
		}
		else if(!(fight instanceof PartyVsParty)){
			Party party = Party.getParty(p);
			if(party != null) {
				if(fight instanceof PartyFFA) skull = "Party FFA";
				else if(fight instanceof PartySplit) skull = "Party Split";
				else if(fight instanceof PartyVsBots) skull = "Party Vs Bots";
				skull += " (" + party.getOwner() + "'s party)";
			}
		}
		else if(fight instanceof PartyVsParty) {
			PartyVsParty pvp = (PartyVsParty) fight;
			skull = "Party Vs Party (" + pvp.getParty1().getOwner() + "'s party vs " + pvp.getParty2().getOwner() + "'s party)";
		}
		if(fight.getKit() != null && fight.getKit().getIcon() != null) {
			ItemStack item = fight.getKit().getIcon().clone();
			ItemMeta im = item.getItemMeta();
			im.setDisplayName(ChatColor.AQUA + skull);
			List<String> lore = new ArrayList<String>();
			if(fight.getKit() != null) {
				lore.add(ChatColor.GOLD + "Kit: " + ChatColor.YELLOW + fight.getKit().getFancyName());
			}
			if(fight.getArena() != null) {
				lore.add(ChatColor.GOLD + "Arena: " + ChatColor.YELLOW + fight.getArena().getDisplayName());
			}
			lore.add(ChatColor.GOLD + "Duration: " + ChatColor.YELLOW + new SimpleDateFormat("mm:ss").format(new Date(fight.getDuration())));
			im.setLore(lore);
			item.setItemMeta(im);
			return item;
		}
		ItemStack item = ItemStackUtil.createPlayerSkull(skull);
		SkullMeta im = (SkullMeta) item.getItemMeta();
		im.setOwner(skull);
		im.setDisplayName(ChatColor.AQUA + skull);
		List<String> lore = new ArrayList<String>();
		if(fight.getKit() != null) {
			lore.add(ChatColor.GOLD + "Kit: " + ChatColor.YELLOW + fight.getKit().getFancyName());
		}
		if(fight.getArena() != null) {
			lore.add(ChatColor.GOLD + "Arena: " + ChatColor.YELLOW + fight.getArena().getDisplayName());
		}
		lore.add(ChatColor.GOLD + "Duration: " + ChatColor.YELLOW + new SimpleDateFormat("mm:ss").format(new Date(fight.getDuration())));
		im.setLore(lore);
		item.setItemMeta(im);
		return item;
	}


	public void addSpectator(Player p) {
		//fixing ambiguous error
		Player target = null;
		addSpectator(p, target);
	}

	public void addSpectator(Player p, Player target) {
		addSpectator(p, target == null ? null : Fight.getCurrentFight(target, plugin));
	}

	public void addSpectator(Player p, Fight fight) {
		PlayerStartSpectatingEvent event = new PlayerStartSpectatingEvent(p, fight);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) return;
		if(fight != null) {
			spectatingFight.put(p.getUniqueId(), fight);
			for(Player pl : Bukkit.getOnlinePlayers()) {
				Fight f2 = Fight.getCurrentFight(pl, plugin);
				if(f2 == fight) p.showPlayer(pl);
			}
		}
		else {
			for(Player pl : Bukkit.getOnlinePlayers()) {
				p.showPlayer(pl);
			}
		}
		new BukkitRunnable() {

			@Override
			public void run() {
				if(p == null || !isSpectator(p)) return;
				p.getInventory().clear();
				p.setAllowFlight(true);
				p.setFlying(true);
				p.setFireTicks(0);
				p.setVelocity(new Vector(0, 1, 0));
				for(Player pl : Bukkit.getOnlinePlayers()) {
					if(Fight.isInFight(pl, plugin) || (PvPEvent.isInEvent(pl) && (!isSpectator(pl) || hideOtherSpectators))
							|| (isSpectator(pl) && hideOtherSpectators)) {
						pl.hidePlayer(p);
					}
					else {
						pl.showPlayer(p);
						p.showPlayer(pl);
					}
				}
				String teleportItemString = plugin.getConfig().getString("spectator-teleport-item");
				String tpMaterial = teleportItemString.contains(":") ? teleportItemString.split(":")[0] : teleportItemString;
				String tpID = teleportItemString.contains(":") ? teleportItemString.split(":")[1] : null;
				Material mat = Material.getMaterial(tpMaterial);
				ItemStack tpItem = new ItemStack(mat == null ? Material.AIR : mat);
				if(tpID != null) {
					try{
						tpItem.setDurability(Short.parseShort(tpID));
					}catch(IllegalArgumentException e) {}
				}
				if(tpItem.getType() != Material.AIR) {
					ItemMeta tpMeta = tpItem.getItemMeta();
					tpMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("spectator-teleport-name")));
					tpItem.setItemMeta(tpMeta);
					p.getInventory().setItem(4, tpItem);
				}
				if(!PvPEvent.isInEvent(p)) {
					String leaveItemString = plugin.getConfig().getString("spectator-leave-item");
					String leaveMaterial = leaveItemString.contains(":") ? leaveItemString.split(":")[0] : leaveItemString;
					String leaveID = leaveItemString.contains(":") ? leaveItemString.split(":")[1] : null;
					Material mat2 = Material.getMaterial(leaveMaterial);
					ItemStack leaveItem = new ItemStack(mat2 == null ? Material.AIR : mat2);
					if(leaveID != null) {
						try{
							leaveItem.setDurability(Short.parseShort(leaveID));
						}catch(IllegalArgumentException e) {}
					}
					ItemMeta leaveMeta = leaveItem.getItemMeta();
					String name = plugin.getConfig().getString("spectator-leave-name");
					if(name != null) {
						leaveMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
						leaveItem.setItemMeta(leaveMeta);
					}
					p.getInventory().setItem(8, leaveItem);
				}
				p.updateInventory();
				ScoreboardManager.getScoreboardManager().updateScoreboard(p);
			}
		}.runTaskLater(plugin, 2);
		plugin.clear(p, false, false);
		p.setVelocity(new Vector(0, 1, 0));
		p.setAllowFlight(true);
		p.setFlying(true);
		p.setMetadata(SPECTATOR_META, new FixedMetadataValue(plugin, true));
		if(!LegionPractice.performanceMode) {
			for(Player pl : Bukkit.getOnlinePlayers()) {
				if(Fight.isInFight(pl, plugin) || (PvPEvent.isInEvent(pl) && (!isSpectator(pl) || hideOtherSpectators))
						|| (isSpectator(pl) && hideOtherSpectators)) {
					pl.hidePlayer(p);
				}
				else {
					pl.showPlayer(p);
					p.showPlayer(pl);
				}
			}
		}
		if(fight != null) {
			for(Player pl : Bukkit.getOnlinePlayers()) {
				Fight f = Fight.getCurrentFight(pl, plugin);
				if(f != null && f == fight) {
					String message = plugin.translateMessage(pl, "started-spectating").replace("<player>", p.getName());
					if(!message.equals("false")) {
						pl.sendMessage(message);
					}
				}
			}
			/*
			if(fight instanceof Duel) {
				Player pl = Bukkit.getPlayer(((Duel) fight).getP1());
				pl.sendMessage(plugin.translateMessage(pl, "started-spectating").replace("<player>", p.getName()));
				pl = Bukkit.getPlayer(((Duel) fight).getP2());
				pl.sendMessage(plugin.translateMessage(pl, "started-spectating").replace("<player>", p.getName()));
			}
			else if(fight instanceof BotDuel) {
				Player pl = Bukkit.getPlayer(((BotDuel) fight).getP1());
				pl.sendMessage(plugin.translateMessage(pl, "started-spectating").replace("<player>", p.getName()));
			}
			else if(fight instanceof PartyFFA) {

			}
			else if(fight instanceof PartySplit) {

			}
			else if(fight instanceof PartyVsParty) {

			}
			 */
		}
	}

	public boolean isSpectator(Player p) {
		return p.hasMetadata(SPECTATOR_META);	
	}

	public List<UUID> getSpectating(Fight fight) {
		List<UUID> uuids = new ArrayList<UUID>();
		for(Entry<UUID, Fight> e : spectatingFight.entrySet()) {
			if(e.getValue() == fight) {
				uuids.add(e.getKey());
			}
		}
		return uuids;
	}

	public void removeSpectator(Player p, boolean clearAndTp) {
		Fight fight = spectatingFight.getOrDefault(p.getUniqueId(), null);
		PlayerStopSpectatingEvent event = new PlayerStopSpectatingEvent(p, fight);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) return;
		p.setAllowFlight(false);
		p.setFlying(false);
		p.removeMetadata(SPECTATOR_META, plugin);
		p.getInventory().clear();
		if(clearAndTp) {
			plugin.clear(p, true, true);
		}
		for(Player pl : Bukkit.getOnlinePlayers()) {
			pl.showPlayer(p);
		}
		PlaybackFight.removeSpectator(p);
		p.updateInventory();
		spectatingFight.remove(p.getUniqueId());
		if(LMSCommand.lms != null) {
			LMSCommand.lms.spectators.remove(p.getUniqueId());
		}
		if(fight != null) {
			for(Player pl : Bukkit.getOnlinePlayers()) {
				Fight f = Fight.getCurrentFight(pl, plugin);
				if(f != null && f == fight) {
					String message = plugin.translateMessage(pl, "stopped-spectating").replace("<player>", p.getName());
					if(!message.equals("false")) {
						pl.sendMessage(message);
					}
				}
			}
		}
		ScoreboardManager.getScoreboardManager().updateScoreboard(p);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public HashMap<UUID, Fight> getSpectatingFight() {
		return spectatingFight;
	}

	public void stop() {
		enabled = false;
		task.cancel();
	}

	public Inventory getSpecateMatchesInventory() {
		Inventory spectateMatches = Bukkit.createInventory(null, 54, ChatColor.GREEN + "Spectate");
		int counter = 0;
		int tenCounter = 0;
		List<Fight> fights = new ArrayList<Fight>();
		for(Player pl : Bukkit.getOnlinePlayers()) {
			Fight fight = Fight.getCurrentFight(pl, plugin);
			if(fight != null && fights.size() < 54 && !fights.contains(fight)) {
				fights.add(fight);
			}
		}
		Collections.sort(fights, new Comparator<Fight>() {

			@Override
			public int compare(Fight x, Fight y) {
				return Long.compare(x.getDuration(), y.getDuration());
			}
		});
		for(Fight fight : fights) {
			Player p = null;
			if(fight != null) {
				for(Player pl : Bukkit.getOnlinePlayers()) {
					Fight f = Fight.getCurrentFight(pl, plugin);
					if(f != null && f == fight) {
						p = pl;
					}
				}
				spectateMatches.setItem(counter, item(fight, p));
				counter++;
			}
		}
		while(counter < 54) {
			spectateMatches.setItem(counter, new ItemStack(Material.AIR));
			counter++;
		}
		return spectateMatches;
	}
}
