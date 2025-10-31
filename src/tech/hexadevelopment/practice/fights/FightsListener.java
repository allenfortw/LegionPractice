package tech.hexadevelopment.practice.fights;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.fights.botduel.BotDuel;
import tech.hexadevelopment.practice.fights.duel.BestOf;
import tech.hexadevelopment.practice.fights.duel.Duel;
import tech.hexadevelopment.practice.fights.party.partyfights.PartySplit;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsBots;
import tech.hexadevelopment.practice.fights.party.partyfights.PartyVsParty;
import tech.hexadevelopment.practice.fights.queue.QueueManager;
import tech.hexadevelopment.practice.npc.CitizensNPC;
import tech.hexadevelopment.practice.party.Party;
/**
 * Handles events for fights.
 * Cancels party ffa in party vs party.
 * Handles deaths also.
 * @author Toppe5
 * @since 0.1
 */
public class FightsListener implements Listener {

	public static String itemDrop = "LegionPracticeItemDrop";

	private LegionPractice plugin;
	private boolean fixBotDamage;
	private int buildLimit;

	public FightsListener(LegionPractice plugin) {
		this.plugin = plugin;
		fixBotDamage = plugin.getConfig().getBoolean("fix-bot-damage");
		buildLimit = plugin.getConfig().getInt("build-limit");
	}

	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled=true)
	public void onDamage(EntityDamageByEntityEvent e) {
		if(e.getEntity() instanceof Player) {
			if(e.getDamager() instanceof Player) {
				Player p = (Player) e.getDamager();
				Player target = (Player) e.getEntity();
				if(Party.getParty(target) != null
						&& Party.getParty(p) != null) {
					Party party = Party.getParty(p);
					Party party2 = Party.getParty(target);
					Fight fight = Fight.getCurrentFight(p, plugin);
					if(party != null && party2 != null) {
						if(party.getOwner().equals(party2.getOwner())
								&& (fight instanceof PartyVsParty || fight instanceof PartyVsBots || partySplit(p, target, party, Fight.getCurrentFight(p, plugin)))) {
							e.setCancelled(true);
						}
					}
				}
			}
			else if(e.getDamager() instanceof Projectile) {
				if(((Projectile) e.getDamager()).getShooter() instanceof Player) {
					Player p = (Player) ((Projectile) e.getDamager()).getShooter();
					Player target = (Player) e.getEntity();
					if(Party.getParty(target) != null
							&& Party.getParty(p) != null) {
						Party party = Party.getParty(p);
						Party party2 = Party.getParty(target);
						Fight fight = Fight.getCurrentFight(p, plugin);
						if(party != null && party2 != null) {
							if(party.getOwner().equals(party2.getOwner())
									&& (fight instanceof PartyVsParty || fight instanceof PartyVsBots || partySplit(p, target, party, Fight.getCurrentFight(p, plugin)))) {
								e.setCancelled(true);
							}
						}
					}
				}
			}
			if(fixBotDamage && e.getDamager() instanceof HumanEntity){
				Fight fight = Fight.getCurrentFight(e.getDamager(), plugin);
				if(fight != null && fight instanceof BotFight && Bukkit.getPlayer(e.getDamager().getUniqueId()) == null) {
					e.setDamage(e.getDamage()+CitizensNPC.getDamage(((HumanEntity) e.getDamager()).getInventory().getItemInHand()));
				}
			}
		}
	}

	private boolean partySplit(Player p, Player target, Party party, Fight fight) {
		if(fight instanceof PartySplit) {
			PartySplit split = (PartySplit) fight;
			if((split.alive.contains(p.getName()) && split.alive.contains(target.getName()))
					|| (split.alive2.contains(p.getName()) && split.alive2.contains(target.getName()))) {
				return true;
			}
		}
		return false;
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onQuit(PlayerQuitEvent e) {
		Fight fight = Fight.getCurrentFight(e.getPlayer(), plugin);
		if(fight != null) {
			if(fight instanceof Duel) {
				((Duel) fight).setBestOf(null);
			}
			else if(fight instanceof BotDuel) {
				((BotDuel) fight).setBestOf(null);	
			}
			fight.handleDeath(e.getPlayer());
			//not really needed
			//QueueManager.updateGUIs(true);
		}
		if(plugin.getTagManager().COLORED_TAGS) {
			plugin.getTagManager().removeFromTeams(e.getPlayer());
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onBreak(BlockBreakEvent e) {
		if(e.getPlayer().hasMetadata(plugin.NO_DAMAGE)) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlace(BlockPlaceEvent e) {
		Fight fight = Fight.getCurrentFight(e.getPlayer(), plugin);
		if(fight != null) {
			if(fight.getKit().isBuild()) {
				int yDiff = e.getBlock().getY()-fight.getArena().getCenter().getBlockY();
				if(yDiff > buildLimit) {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onWaterPlace(PlayerBucketEmptyEvent e) {
		Fight fight = Fight.getCurrentFight(e.getPlayer(), plugin);
		if(fight != null) {
			if(fight.getKit().isBuild()) {
				int yDiff = e.getBlockClicked().getY()-fight.getArena().getCenter().getBlockY();
				if(yDiff > buildLimit) {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		if(e.getEntity() instanceof Player && e.getFoodLevel() < 20) {
			BattleKit kit = BattleKit.getCurrentKit(e.getEntity());
			if(kit != null && kit.isNoHunger()) {
				e.setFoodLevel(20);
			}
		}
	}

	@EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
	public void onPlayerDamage(EntityDamageEvent e) {
		if(e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			Fight fight = Fight.getCurrentFight(p, plugin);
			if(fight != null) {
				if(fight.hasEnded()) {
					e.setCancelled(true);
				}
				else {
					BestOf bo = null;
					if(fight instanceof Duel) {
						bo = ((Duel) fight).getBestOf();
						if(((Duel) fight).breaktime) {
							e.setCancelled(true);
						}
					}
					if(fight instanceof BotDuel && Bukkit.getPlayer(p.getUniqueId()) != null) {
						bo = ((BotDuel) fight).getBestOf();
						if(((BotDuel) fight).breaktime) {
							e.setCancelled(true);
						}
					}
					Damageable dm = (Damageable) p;
					if(dm.getHealth()-e.getDamage() <= 0) {
						//don't handle normal fights, just in case
						if(bo != null && bo.getRounds() > 1 && !bo.endsNow(p.getUniqueId())) {
							fight.handleDeath(p);
							e.setCancelled(true);
						}
					}
				}
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onInteract(PlayerInteractEvent e) {
		if(e.getPlayer().hasMetadata(plugin.NO_DAMAGE)) {
			if(e.getPlayer().getItemInHand() != null && (e.getPlayer().getItemInHand().getDurability() == 16421
					|| e.getPlayer().getItemInHand().getDurability() == 16389)) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onChunkLoad(ChunkLoadEvent e) {
		if(e.isNewChunk()) return;
		for(Entity ent : e.getChunk().getEntities()) {
			if(ent instanceof Item && ent.hasMetadata(itemDrop)) {
				ent.remove();
			}
		}
	}
	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
	public void onDrop(PlayerDropItemEvent e) {
		Fight fight = Fight.getCurrentFight(e.getPlayer(), plugin);
		if(fight != null && fight.hasEnded()) {
			e.setCancelled(true);
		}
		else if(e.getPlayer().hasMetadata(plugin.NO_DAMAGE)) {
			e.setCancelled(true);
		}
		else if(fight != null) {
			Item i = e.getItemDrop();
			i.setMetadata(itemDrop, new FixedMetadataValue(plugin, true));
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					if(i != null) {
						i.remove();
					}
				}
			}, plugin.getConfig().getInt("remove-drops")*20);
		}
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onDeath(EntityDeathEvent e) {
		Fight fight = Fight.getCurrentFight(e.getEntity(), plugin);
		if(fight != null && e.getDrops() != null) {
			int time = plugin.getConfig().getInt("remove-drops")*20;
			List<Item> drops = new ArrayList<Item>();
			if(!LegionPractice.disabling) {
				for(ItemStack is : e.getDrops()) {
					if(is != null && is.getType() != Material.AIR) {
						Item item = e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), is);
						drops.add(item);
						item.setMetadata(itemDrop, new FixedMetadataValue(plugin, true));
					}
				}
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						for(Item item : drops) {
							if(item != null) {
								item.remove();
							}
						}
						drops.clear();
					}
				}, time);
			}
			e.getDrops().clear();
			if(fight instanceof BotFight && Bukkit.getPlayer(e.getEntity().getUniqueId()) == null) {
				if(fight instanceof BotDuel) {
					if(((BotDuel) fight).getBestOf() != null && !((BotDuel) fight).getBestOf().endsNow(e.getEntity().getUniqueId())) {
						e.getDrops().clear();
					}
				}
				((BotFight) fight).handleBotDeath(e.getEntity());
				LivingEntity ent = e.getEntity();
				new BukkitRunnable() {

					@Override
					public void run() {
						if(ent != null) {
							ent.teleport(e.getEntity().getLocation().subtract(0, 10, 0));
						}
					}
				}.runTaskLater(plugin, 10);
			}
			else if(e.getEntity() instanceof Player) {
				Player p = (Player) e.getEntity();
				if(fight.getArena() == null || fight.getArena().getCenter() == null) return;
				Location loc = fight.getArena().getCenter();
				fight.handleDeath(p);
				QueueManager.updateGUIs(true);
				if(fight.allowSpectating()) {
					int i = plugin.getConfig().getInt("wait-before-spectator")+2;
					if(i < 2) {
						i = 2;
					}
					new BukkitRunnable() {

						@Override
						public void run() {
							if(p != null) {
								plugin.getSpectatorHandler().addSpectator(p);
								p.teleport(loc);
							}
						}
					}.runTaskLater(plugin, i);
				}
				else {
					new BukkitRunnable() {

						@Override
						public void run() {
							if(p != null) {
								plugin.arenaPvP.lobby(p);	
							}
						}
					}.runTaskLater(plugin, 1);
				}
			}
		}
	}

}