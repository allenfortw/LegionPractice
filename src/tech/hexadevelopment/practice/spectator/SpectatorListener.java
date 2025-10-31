package tech.hexadevelopment.practice.spectator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.utils.ItemStackUtil;

public class SpectatorListener implements Listener {

	private LegionPractice plugin;

	public SpectatorListener(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onTarget(EntityTargetEvent e){
		if(e.getTarget() instanceof Player) {
			Player targeted = (Player) e.getTarget();
			if(plugin.getSpectatorHandler().isSpectator(targeted)) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onInteraction(PlayerInteractEntityEvent e) {
		if(e.getRightClicked() instanceof Player
				&& Fight.getCurrentFight(e.getPlayer(), plugin) == null && plugin.getSpectatorHandler().isSpectator(e.getPlayer())) {
			if(plugin.getConfig().getBoolean("spectator-inventory-view")) {
				Player tar = ((Player) e.getRightClicked());
				if(!plugin.getSpectatorHandler().isSpectator(tar)) {
					Inventory inv = Bukkit.createInventory(null, 5*9, ChatColor.GREEN + "Spectator View");
					inv.setContents(tar.getInventory().getContents());
					for(int i = 0; i < tar.getInventory().getArmorContents().length; i++) {
						inv.setItem(4*9+i, tar.getInventory().getArmorContents()[i]);
					}
					Damageable d = (Damageable) tar;
					int health = Math.abs((int) d.getHealth());
					ItemStack is = ItemStackUtil.createItem(Material.GOLDEN_APPLE, ChatColor.GOLD + "Health: " + health);
					is.setAmount(health);
					inv.setItem(4*9+4, is);
					e.getPlayer().openInventory(inv);
				}
			}
		}
	}

	@EventHandler
	public void onVehicleEnter(VehicleEnterEvent e) {
		if(e.getEntered() instanceof Player) {
			Player p = (Player) e.getEntered();
			if(plugin.getSpectatorHandler().isSpectator(p)) {
				e.setCancelled(true);
				e.getEntered().leaveVehicle();
			}
		}
	}


	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if(plugin.getSpectatorHandler().isSpectator(e.getPlayer())) {
			plugin.getSpectatorHandler().removeSpectator(e.getPlayer(), true);
		}
		for(Player pl : Bukkit.getOnlinePlayers()) {
			if(pl.hasMetadata(SpectatorHandler.SPECTATOR_META)) {
				e.getPlayer().hidePlayer(pl);
			}
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent e) {
		if(plugin.getSpectatorHandler().isSpectator(e.getPlayer())) {
			plugin.getSpectatorHandler().removeSpectator(e.getPlayer(), true);
		}
		plugin.getSpectatorHandler().stayAwayMessages.remove(e.getPlayer().getUniqueId());
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onDeath(PlayerDeathEvent e) {
		if(plugin.getSpectatorHandler().isSpectator(e.getEntity())) {
			plugin.getSpectatorHandler().removeSpectator(e.getEntity(), true);
		}
	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if(e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if(plugin.getSpectatorHandler().isSpectator(p)) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if(e.getDamager() instanceof Player) {
			Player p = (Player) e.getDamager();
			if(plugin.getSpectatorHandler().isSpectator(p)) {
				e.setCancelled(true);
			}
		}
		if(e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if(plugin.getSpectatorHandler().isSpectator(p)) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		if(!plugin.getSpectatorHandler().isSpectator(e.getPlayer())) return;
		if(e.getMessage().equalsIgnoreCase("/leave") || e.getMessage().equalsIgnoreCase("/spectate")) {
			plugin.getSpectatorHandler().removeSpectator(e.getPlayer(), true);
			e.setCancelled(true);
		}
		else {
			if(plugin.getConfig().getBoolean("spectator-block-all-commands")) {
				e.setCancelled(true);
				e.getPlayer().sendMessage(plugin.translateMessage(e.getPlayer(), "leave-spectator-mode"));
			}
			else {
				String cmd = e.getMessage().toLowerCase();
				for(String s : plugin.getConfig().getStringList("spectator-blocked-commands")) {
					if(cmd.startsWith(s.toLowerCase())) {
						e.getPlayer().sendMessage(plugin.translateMessage(e.getPlayer(), "leave-spectator-mode"));
						e.setCancelled(true);
						return;
					}
				}
			}
		}
	}

	@EventHandler
	public void onPickup(PlayerPickupItemEvent e) {
		if(plugin.getSpectatorHandler().isSpectator(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		plugin.getSpectatorHandler().stayAwayMessages.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onClickInInventory(InventoryClickEvent e){
		if(!(e.getWhoClicked() instanceof Player)) return;
		Player p = (Player) e.getWhoClicked();
		ItemStack item = e.getCurrentItem();
		if(e.getClickedInventory() != null && (e.getClickedInventory().getName().equals(ChatColor.GREEN + "Teleport")
				|| e.getInventory().getName().equals(ChatColor.GREEN + "Spectator View")
				|| e.getInventory().getName().equals(ChatColor.GREEN + "Spectate"))) {
			e.setCancelled(true);
			if(e.getClickedInventory().getName().equals(ChatColor.GREEN + "Teleport") && item != null && item.getItemMeta() != null && item.getItemMeta().getDisplayName() != null) {
				if(plugin.getSpectatorHandler().isSpectator(p)) {
					String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
					double distance = plugin.getConfig().getDouble("spectator-teleporter-distance")*2;
					for(Entity ent : p.getNearbyEntities(distance, distance, distance)) {
						if(ent instanceof Player) {
							Player tar = (Player) ent;
							Fight fight = Fight.getCurrentFight(tar, plugin);
							if(fight != null && tar.getName().equals(name)) {
								p.teleport(tar.getLocation().add(0, 10, 0));
								new BukkitRunnable() {

									@Override
									public void run() {
										if(tar != null) {
											Fight fight = Fight.getCurrentFight(tar, plugin);
											if(fight != null) {
												p.teleport(tar.getLocation());
											}
										}
									}
								}.runTaskLater(plugin, 2);
							}
						}
					}
				}
			}
			else if(e.getClickedInventory().getName().equals(ChatColor.GREEN + "Spectate") && item != null && item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
				for(Player pl : Bukkit.getOnlinePlayers()) {
					Fight fight = Fight.getCurrentFight(pl, plugin);
					if(fight != null && sameName(item, SpectatorHandler.item(fight, pl))) {
						for(Player players : Bukkit.getOnlinePlayers()) {
							if(Fight.isInFight(players, plugin) || (plugin.getSpectatorHandler().isSpectator(players) && plugin.getSpectatorHandler().hideOtherSpectators)) {
								players.hidePlayer(p);
							}
						}
						p.teleport(fight.getArena().getCenter());
						plugin.getSpectatorHandler().addSpectator(p, pl);
						break;
					}
				}
				p.closeInventory();
			}
		}
	}

	private boolean sameName(ItemStack item, ItemStack item2) {
		return item.hasItemMeta() && item2.hasItemMeta() && item.getItemMeta().hasDisplayName()
				&& item2.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(item2.getItemMeta().getDisplayName());
	}

	@EventHandler
	public void onPotionSplash(PotionSplashEvent e) {
		Iterator<LivingEntity> i = e.getAffectedEntities().iterator();
		while(i.hasNext()) {
			LivingEntity ent = i.next();
			if(ent instanceof Player && plugin.getSpectatorHandler().isSpectator((Player) ent)) {
				i.remove();
			}
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onDrop(PlayerDropItemEvent e) {
		if(plugin.getSpectatorHandler().isSpectator(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(e.getAction() != null && e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(plugin.getSpectatorHandler().isSpectator(e.getPlayer())) {
			Player p = e.getPlayer();
			e.setCancelled(true);
			ItemStack hand = e.getItem();
			if(hand != null && hand.hasItemMeta()
					&& hand.getItemMeta().getDisplayName() != null) {
				if(hand.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("spectator-teleport-name")))) {
					List<Player> players = new ArrayList<Player>();
					double distance = plugin.getConfig().getDouble("spectator-teleporter-distance");
					for(Entity ent : p.getNearbyEntities(distance, distance, distance)) {
						if(ent instanceof Player) {
							Player pl = (Player) ent;
							Fight fight = Fight.getCurrentFight(pl, plugin);
							if(fight != null && !players.contains(pl)) {
								players.add(pl);
							}
						}
					}
					int size = 9;
					if(players.size() > 9) size = 18;
					if(players.size() > 18) size = 27;
					if(players.size() > 27) size = 36;
					if(players.size() > 36) size = 45;
					if(players.size() > 45) size = 54;
					Inventory inv = Bukkit.createInventory(null, size, ChatColor.GREEN + "Teleport");
					for(Player pl : players) {
						ItemStack item = ItemStackUtil.createPlayerSkull(pl.getName());
						SkullMeta im = (SkullMeta) item.getItemMeta();
						im.setOwner(pl.getName());
						im.setDisplayName(ChatColor.AQUA + pl.getName());
						item.setItemMeta(im);
						if(!inv.contains(item)){
							inv.addItem(item);
						}
					}
					p.openInventory(inv);
				}
				else if(hand.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("spectator-leave-name")))) {
					plugin.getSpectatorHandler().removeSpectator(e.getPlayer(), true);
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled=true) 
	public void onBreak(BlockBreakEvent e) {
		if(plugin.getSpectatorHandler().isSpectator(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
}
