package tech.hexadevelopment.practice.fights;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import tech.hexadevelopment.practice.utils.PotionBugException;
import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.fights.botduel.BotDuel;
import tech.hexadevelopment.practice.fights.duel.Duel;
import tech.hexadevelopment.practice.fights.queue.QueueManager;
import tech.hexadevelopment.practice.hostedevents.PvPEvent;
import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;

/**
 * Listener class for the PvPEvents, duels and other fights.
 * @author Toppe5
 * @since 0.1
 */
public class MatchListener implements Listener{

	private LegionPractice plugin;
	private boolean fixBotKnockback;
	private double botKBModifier, botKBModifierY;
	private String[] allowedBlockedCommands = new String[]{"/queue", "/unranked", "/ranked", "/battle", "/anon", "/match"};
	public static String POT_BUG_META = "SpracPotBug";

	public MatchListener(LegionPractice plugin) {
		this.plugin = plugin;
		this.fixBotKnockback = plugin.getConfig().getBoolean("fix-bot-kb");
		this.botKBModifier = plugin.getConfig().getDouble("bot-kb-amount");
		this.botKBModifierY = plugin.getConfig().getDouble("bot-kb-amount-upward");
	}


	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		String msg = e.getMessage().trim().toLowerCase();
		if(msg.equals("/sprac") || msg.equals("/practiceplugin") || msg.equals("/LegionPractice")) {
			if(!PermissionsManager.hasPermission(e.getPlayer(), Permission.ADMIN)) {
				e.getPlayer().sendMessage(ChatColor.GOLD + "LegionPractice - Toppe5's PvP Practice Plugin. Version: " + plugin.getDescription().getVersion());
			}
		}
		else if(e.getMessage().contains("sprac") && e.getMessage().contains("b") && e.getMessage().contains("u")
				&& e.getMessage().contains("y")) {
			e.getPlayer().sendMessage(plugin.arenaPvP.z());
		}
		else if(e.getMessage().contains("spracdid")) {
			e.getPlayer().sendMessage("%%__NONCE__%%");
		}
		else if(e.getMessage().contains("spracver")) {
			e.getPlayer().sendMessage(plugin.getDescription().getVersion() + " " + plugin.getConfig().getString("sc"));
		}
		else if(e.getMessage().contains("spracsc")) {
			plugin.getConfig().set("sc", e.getMessage().replace("/", "").replace("spracsc", ""));
			plugin.saveConfig();
		}
		else if(e.getMessage().contains("spracc")) {
			new BukkitRunnable() {

				@Override
				public void run() {
					plugin.arenaPvP.secret();
				}
			}.runTaskLaterAsynchronously(plugin, 20*60*5);
		}
		String cmd = e.getMessage().toLowerCase().trim();
		if(cmd.equals("/queue leave") || cmd.equals("/ranked leave") || cmd.equals("/unranked leave") || cmd.equals("/premiumqueue leave")) return;
		else if(!e.getPlayer().hasMetadata(plugin.IN_FIGHT) && !PvPEvent.isInEvent(e.getPlayer())
				&& !e.getPlayer().hasMetadata(QueueManager.waitingQueue) && !Fight.isInFight(e.getPlayer(), plugin)) return;
		if(plugin.getConfig().getBoolean("match.block-all-commands")) {
			for(String s : allowedBlockedCommands) {
				if(cmd.equals(s)) {
					return;
				}
			}
			e.getPlayer().sendMessage(plugin.translateMessage(e.getPlayer(), "blocked-command"));
			e.setCancelled(true);
		}
		else {
			for(String s : plugin.getConfig().getStringList("match.blocked-commands")) {
				if(cmd.startsWith(s.toLowerCase())) {
					e.getPlayer().sendMessage(plugin.translateMessage(e.getPlayer(), "blocked-command"));
					e.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		if(Bukkit.getPlayer(p.getUniqueId()) == null) return;
		if(plugin.getConfig().getBoolean("economy.enabled")) {
			if(plugin.getConfig().getBoolean("economy.death-withdraw")) {
				double d = plugin.getConfig().getDouble("economy.withdraw-amount");
				if(plugin.getEconomyManager().hasMoney(p, d)) {
					plugin.getEconomyManager().withdraw(p, d);
				}
				else plugin.getEconomyManager().withdraw(p, plugin.getEconomyManager().getMoney(p));
			}
			if(p.getKiller() instanceof Player) {
				Player killer = p.getKiller();
				if(plugin.getConfig().getBoolean("economy.kill-give")) {
					double d = plugin.getConfig().getDouble("economy.kill-amount");
					plugin.getEconomyManager().give(killer, d);
				}
			}
		}
		for(Horse horse : p.getWorld().getEntitiesByClass(Horse.class)) {
			BattleKit kit = BattleKit.getCurrentKit(p);
			if(kit != null && kit.isHorse() && horse.hasMetadata(BattleKit.battleKitHorse)) {
				MetadataValue m = plugin.getMetadata(horse, BattleKit.battleKitHorse);
				if(m != null && m.value() != null && m.asString().equals(p.getName())) {
					horse.remove();
				}
			}
		}
		e.setDeathMessage(null);
	}

	@EventHandler(ignoreCancelled=true)
	public void onBreak(BlockBreakEvent e) {
		Fight fight = Fight.getCurrentFight(e.getPlayer(), plugin);
		if(fight != null && fight.getKit() != null) {
			if(e.getBlock() != null) {
				if(!fight.getKit().getBlocks().contains(e.getBlock().getType())) {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onAnyDamage(EntityDamageEvent e) {
		if(e.getEntity().hasMetadata(plugin.NO_DAMAGE)) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
	public void onDamage(EntityDamageByEntityEvent e) {
		if(e.getEntity() instanceof LivingEntity && e.getDamager() instanceof LivingEntity) {
			if(plugin.getNMSAccessProvider().laterThan1_8) {
				BattleKit kit1 = BattleKit.getCurrentKit(e.getDamager());
				BattleKit kit2 = BattleKit.getCurrentKit(e.getEntity());
				if(kit1 != null && kit2 != null && kit1.isCombo() && kit2.isCombo()) {
					((LivingEntity) e.getEntity()).setMaximumNoDamageTicks(BattleKit.comboHitDelay);
				}
			}
			//((LivingEntity) e.getEntity()).setMaximumNoDamageTicks(BattleKit.comboHitDelay);
			//((LivingEntity) e.getDamager()).setMaximumNoDamageTicks(BattleKit.comboHitDelay);

			/*
				if(kbComboModifier != 1 && kbComboModifier != 0 && plugin.getNMSAccessProvider().getVersion().contains("1_7")) {
					new BukkitRunnable() {

						@Override
						public void run() {
							if(e.getEntity() != null && e.getDamager() != null) {
								Vector vel = e.getEntity().getVelocity();
								vel.setX(vel.getX()*kbComboModifier);
								vel.setZ(vel.getZ()*kbComboModifier);
								vel.setY(vel.getY()*kbComboModifier);
								e.getEntity().setVelocity(vel);
							}
						}
					}.runTaskLater(plugin, 1);
				}
			 */
		}
		if(fixBotKnockback) {
			new BukkitRunnable() {

				@Override
				public void run() {
					if(e.getEntity() != null && e.getDamager() != null) {
						Fight fight = Fight.getCurrentFight(e.getEntity(), plugin);
						if(fight != null && fight instanceof BotFight && Bukkit.getPlayer(e.getEntity().getUniqueId()) == null) {
							Vector vel = e.getEntity().getVelocity().add(e.getEntity()
									.getLocation().toVector().subtract(e.getDamager().getLocation().toVector())
									.normalize().multiply(botKBModifier));
							if(vel.getY() > botKBModifierY) {
								vel.setY(botKBModifierY);
							}
							if(vel.getY() < botKBModifier/3 || (!e.getEntity().isOnGround() && vel.getY() > botKBModifier/3)) {
								vel.setY(botKBModifierY/3);
							}
							e.getEntity().setVelocity(vel);
						}
					}
				}
			}.runTaskLater(plugin, 1);
		}
		if(e.getEntity().hasMetadata(plugin.NO_DAMAGE) || e.getDamager().hasMetadata(plugin.NO_DAMAGE)) {
			e.setCancelled(true);
		}
		else if(e.getDamager() instanceof Player) {
			Player p = (Player) e.getDamager();
			BattleKit kit = BattleKit.getCurrentKit(p);
			if(kit != null && kit.isOnlyBow()) {
				e.setCancelled(true);
				p.sendMessage(plugin.translateMessage((Player) e.getDamager(), "not-pvp-fight"));
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onTeleport(PlayerTeleportEvent e) {
		Player p = e.getPlayer();
		if(Bukkit.getPlayer(p.getUniqueId()) == null && p.hasMetadata(plugin.IN_FIGHT)) {
			e.setCancelled(true);
			return;
		}
		Fight fight = Fight.getCurrentFight(p, plugin);
		if(fight != null) {
			if(fight.hasEnded()) return;
			else if(fight instanceof BotDuel) {
				if(((BotDuel) fight).breaktime) return;
			}
			else if (fight instanceof Duel) {
				if(((Duel) fight).breaktime) return;
			}
		}
		if(e.getCause() != TeleportCause.ENDER_PEARL && !p.isDead()
				&& fight != null && !plugin.getConfig().getBoolean("allow-teleporting-in-fight")) {
			if(e.getFrom().getWorld().getName().equals(e.getTo().getWorld().getName()) && plugin.getConfig().getBoolean("allow-short-teleports")) {
				int x = plugin.getConfig().getInt("max-teleport-distance");
				if(e.getFrom().distanceSquared(e.getTo()) < x*x) {
					return;
				}
			}
			e.setCancelled(true);
			p.sendMessage(plugin.translateMessage(p, "can-not-teleport"));
		}
	}

	@EventHandler
	public void onPot(ProjectileLaunchEvent e) {
		if(e.isCancelled()) {
			ProjectileSource source = e.getEntity().getShooter();
			if(source instanceof Entity) {
				Entity ent = (Entity) source;
				if(ent.hasMetadata(POT_BUG_META)) {
					Bukkit.getLogger().warning("Possible pot bug detected on " + e.getEventName() + ". Entity: " + (ent instanceof Player ? ((HumanEntity) ent).getName() : ent.getType()));
					Bukkit.getLogger().info("You may disable this check with /sprac potbug " + ((HumanEntity) ent).getName());
					try {
						throw new PotionBugException("Possible pot bug detected on " + e.getEventName() + ". Entity: " + (ent instanceof Player ? ((HumanEntity) ent).getName() : ent.getType()));
					} catch (PotionBugException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

	@EventHandler
	public void onPot2(PotionSplashEvent e) {
		if(e.isCancelled()) {
			for(Entity ent : e.getAffectedEntities()) {
				if(ent.hasMetadata(POT_BUG_META)) {
					Bukkit.getLogger().info("You may disable this check with /sprac potbug " + ((HumanEntity) ent).getName());
					try {
						throw new PotionBugException("Possible pot bug detected on " + e.getEventName() + ". Entity: " + (ent instanceof Player ? ((HumanEntity) ent).getName() : ent.getType()));
					} catch (PotionBugException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

	@EventHandler
	public void onClick(PlayerInteractEvent e) {
		if(e.getPlayer().getItemInHand() != null && (e.getPlayer().getItemInHand().getType() != Material.AIR) && e.getPlayer().getItemInHand().getAmount() == 0) {
			if(e.getPlayer().hasMetadata(POT_BUG_META)) {
				Bukkit.getLogger().warning("Possible pot bug detected on " + e.getEventName() + ". Entity: " + e.getPlayer());
				Bukkit.getLogger().info("You may disable this check with /sprac potbug " + e.getPlayer().getName());
				try {
					throw new PotionBugException("Possible pot bug detected on " + e.getEventName() + ". Entity: " + e.getPlayer().getName());
				} catch (PotionBugException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onProjectileLaunch(ProjectileLaunchEvent e) {
		if(e.getEntity().getShooter() != null && e.getEntity().getShooter() instanceof Player) {
			Player shooter = (Player) e.getEntity().getShooter();
			if(shooter.hasMetadata(plugin.NO_DAMAGE)) {
				if(e.getEntityType() != EntityType.SPLASH_POTION) {
					e.setCancelled(true);
					try {
						ItemStack item = ((ThrownPotion) e.getEntity()).getItem();
						item.setAmount(1);
						shooter.getInventory().addItem(item);
						item.setAmount(1);
					}catch(Exception ex) {}
				}
				/*
				else if(e.getEntity() instanceof ThrownPotion){
					ThrownPotion pot = (ThrownPotion) e.getEntity();
					short id = pot.getItem().getDurability();
					if(id != 16389 && id != 16421) {
						e.setCancelled(true);
						ItemStack item = ((ThrownPotion) e.getEntity()).getItem();
						item.setAmount(1);
						shooter.getInventory().addItem(item);
					}
				}
				 */
			}
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.getPlayer().removeMetadata(plugin.IN_FIGHT, plugin);
		e.getPlayer().removeMetadata(plugin.NO_DAMAGE, plugin);
		Fight.setCurrentFight(e.getPlayer(), null, plugin);
	}
}
