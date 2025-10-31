package tech.hexadevelopment.practice.matchrecorder.recorder;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.projectiles.ProjectileSource;

import tech.hexadevelopment.practice.matchrecorder.Damage;
import tech.hexadevelopment.practice.utils.LocationUtil;
import tech.hexadevelopment.practice.utils.SerializableProjectile;
import tech.hexadevelopment.practice.utils.SimpleItem;

public class PlayerRecorderListener implements Listener {


	static HashMap<UUID, PlayerRecorder> recording = new HashMap<UUID, PlayerRecorder>();


	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		if(recording.containsKey(e.getEntity().getUniqueId())) {
			PlayerRecorder recorder = recording.get(e.getEntity().getUniqueId());
			if(recorder.record.getDeath() == 0) {
				recorder.record.setDeath(recorder.tickCounter);
			}
		}
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamage(EntityDamageEvent e) {
		if(e.getEntity() instanceof Player && e.getCause() == DamageCause.ENTITY_ATTACK
				&& recording.containsKey(e.getEntity().getUniqueId())) {
			PlayerRecorder recorder = recording.get(e.getEntity().getUniqueId());
			boolean burn = e.getCause() == DamageCause.FIRE || e.getCause() == DamageCause.FIRE_TICK
					|| e.getCause() == DamageCause.LAVA;
			recorder.record.getTookDamage().put(recorder.tickCounter, new Damage(false, false, burn));
		}
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamage(EntityDamageByEntityEvent e) {
		if(e.getEntity() instanceof Player && recording.containsKey(e.getEntity().getUniqueId())) {
			PlayerRecorder recorder = recording.get(e.getEntity().getUniqueId());
			boolean sharpness = e.getDamager() instanceof Player
					&& ((Player) e.getDamager()).getItemInHand() != null &&
					((Player) e.getDamager()).getItemInHand().getEnchantmentLevel(Enchantment.DAMAGE_ALL) > 0;
					boolean critical = !e.getDamager().isOnGround();
					recorder.record.getTookDamage().put(recorder.tickCounter, new Damage(sharpness, critical, false));
		}
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onSwing(PlayerAnimationEvent e) {
		if(e.getAnimationType() == PlayerAnimationType.ARM_SWING && recording.containsKey(e.getPlayer().getUniqueId())) {
			PlayerRecorder recorder = recording.get(e.getPlayer().getUniqueId());
			if(!recorder.record.getSwung().contains(recorder.tickCounter) && !recorder.record.getSwung().contains(recorder.tickCounter-1)) {
				recorder.record.getSwung().add(recorder.tickCounter);
			}
		}
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		if(recording.containsKey(e.getPlayer().getUniqueId())) {
			PlayerRecorder recorder = recording.get(e.getPlayer().getUniqueId());
			recorder.record.getBlocksBroken().put(recorder.getLastSwung() > 0 ? recorder.getLastSwung() : 10,
					new AbstractMap.SimpleEntry<>(LocationUtil.subtractIgnoreWorld(e.getBlock().getLocation(), recorder.relativeLocation), recorder.tickCounter-recorder.getLastSwung()));
		}
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onLaunch(ProjectileLaunchEvent e) {
		ProjectileSource source = e.getEntity().getShooter();
		if(source != null && source instanceof Player) {
			Player p = (Player) source	;
			if(recording.containsKey(p.getUniqueId())) {
				PlayerRecorder recorder = recording.get(p.getUniqueId());
				recorder.record.getProjectiles().put(recorder.tickCounter, new SerializableProjectile(e.getEntity(), recorder.tickCounter));
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent e) {
		if(recording.containsKey(e.getPlayer().getUniqueId())) {
			PlayerRecorder recorder = recording.get(e.getPlayer().getUniqueId());
			HashMap<Location, SimpleItem> blocks = recorder.record.getBlocksPlaced().getOrDefault(recorder.tickCounter, new HashMap<Location, SimpleItem>());
			blocks.put(LocationUtil.subtractIgnoreWorld(e.getBlockPlaced().getLocation(), recorder.relativeLocation), new SimpleItem(e.getBlock().getType(), e.getBlock().getData()));
			recorder.record.getBlocksPlaced().put(recorder.tickCounter, blocks);
		}
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onItemChange(PlayerItemHeldEvent e) {
		if(recording.containsKey(e.getPlayer().getUniqueId())) {
			PlayerRecorder recorder = recording.get(e.getPlayer().getUniqueId());
			recorder.record.getItemsInHand().put(recorder.tickCounter, new SimpleItem(e.getPlayer().getInventory().getItem(e.getNewSlot())));
		}
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onBucketEmpty(PlayerBucketEmptyEvent e) {
		if(recording.containsKey(e.getPlayer().getUniqueId())) {
			Material type = Material.WATER;
			if(e.getBucket() == Material.LAVA_BUCKET) type = Material.LAVA;
			PlayerRecorder recorder = recording.get(e.getPlayer().getUniqueId());
			HashMap<Location, SimpleItem> blocks = recorder.record.getBlocksPlaced().getOrDefault(recorder.tickCounter, new HashMap<Location, SimpleItem>());
			Location liquidLocation = e.getBlockClicked().getRelative(e.getBlockFace()).getLocation();
			blocks.put(LocationUtil.subtractIgnoreWorld(liquidLocation, recorder.relativeLocation), new SimpleItem(type, (short) 0));
			recorder.record.getBlocksPlaced().put(recorder.tickCounter, blocks);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onBucketFill(PlayerBucketFillEvent e) {
		if(recording.containsKey(e.getPlayer().getUniqueId())) {
			PlayerRecorder recorder = recording.get(e.getPlayer().getUniqueId());
			Location loc = e.getBlockClicked().getRelative(e.getBlockFace()).getLocation();
			Entry<Location, Integer> broken = new AbstractMap.SimpleEntry<>(LocationUtil.subtractIgnoreWorld(loc, recorder.relativeLocation), 0);
			recorder.record.getBlocksBroken().put(recorder.tickCounter, broken);
		}
	}

}
