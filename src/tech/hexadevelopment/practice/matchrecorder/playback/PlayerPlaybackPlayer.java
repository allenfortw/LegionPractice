package tech.hexadevelopment.practice.matchrecorder.playback;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.arena.CachedBlockChange;
import tech.hexadevelopment.practice.arena.RollbackListener;
import tech.hexadevelopment.practice.matchrecorder.RecordedPlayer;
import tech.hexadevelopment.practice.npc.CitizensNPC;
import tech.hexadevelopment.practice.utils.SerializableProjectile;
import tech.hexadevelopment.practice.utils.SimpleItem;
import tech.hexadevelopment.practice.utils.SoundUtil;

public class PlayerPlaybackPlayer {


	private LegionPractice plugin;
	private RecordedPlayer player;
	private CitizensNPC npc;
	private boolean playing;
	private Location center;
	private PlaybackFight fight;
	private int killCamSeconds;

	int tickCounter = 0;
	int steps = 5;

	public PlayerPlaybackPlayer(RecordedPlayer player, CitizensNPC npc, PlaybackFight playbackFight, int killCamSeconds) {
		this.player = player;
		this.npc = npc;
		this.killCamSeconds = killCamSeconds;
		this.fight = playbackFight;
		this.plugin = LegionPractice.getInstance();
	}

	public void start() {
		convertLocationsRelative(fight.getArena());
		if(playing) return;
		playing = true;
		new Player().runTaskTimer(plugin, 1, 1);
	}

	private void convertLocationsRelative(Arena arena) {
		this.center = arena.getCenter().clone();
		for(Location l : player.getLocations()) {
			l.setWorld(center.getWorld());
			l.add(center);
		}
		/*
		//TODO PlayerPlaybackPlayer.java:59 ConcurrentModificationException
		for(Entry<Integer, HashMap<Location, SimpleItem>> b : player.getBlocksPlaced().entrySet()) {
			HashMap<Location, SimpleItem> j = b.getValue();
			for(Entry<Location, SimpleItem> l : j.entrySet()) {
				Location loc = l.getKey();
				loc.setWorld(center.getWorld());
				loc.add(center);
				SimpleItem value = l.getValue();
				j.put(loc, value);
			}
			player.getBlocksPlaced().put(b.getKey(), j);
		} 
		 */
	}

	public void stop() {
		if(playing) {
			playing = false;
			fight.handleRecordEnd(this);
		}
		npc.destroy();
	}



	private class Player extends BukkitRunnable {

		int forwardTo = killCamSeconds > 0 ? player.getDeath()-(killCamSeconds*20) : 0;

		@Override
		public void run() {
			tick(this);
			if(forwardTo > tickCounter) {
				for(int i = 0; i < 9; i++) {
					tick(this);
				}
			}
			else if(forwardTo/1.5 > tickCounter) {
				for(int i = 0; i < 4; i++) {
					tick(this);
				}
			}
		}
	}

	private void tick(BukkitRunnable runnable) {
		try{
			if(!playing || player == null || npc == null || npc.isDestroyed() || tickCounter == player.getLocations().size()) {
				runnable.cancel();
				stop();
			}
			else if(tickCounter != 0 && player.getDeath() == tickCounter) {
				npc.getBukkitEntity().getInventory().setArmorContents(new ItemStack[]{new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
				npc.getBukkitEntity().getInventory().clear();
				npc.getBukkitEntity().setHealth(0);
				npc.destroy();
				runnable.cancel();
				stop();
			}
			else {
				if(npc.getBukkitEntity() != null) {
					if(player.getLocations().size() > tickCounter) {
						Location tpLoc = player.getLocations().get(tickCounter);
						npc.teleport(tpLoc);
					}
					if(player.getVelocities().size() > tickCounter) {
						Vector v = player.getVelocities().get(tickCounter);
						npc.setVelocity(v);
					}
					if(player.getBlocksPlaced().containsKey(tickCounter)) {
						for(Entry<Location, SimpleItem> s : player.getBlocksPlaced().get(tickCounter).entrySet()) {
							Location loc = s.getKey().clone();
							loc.setWorld(center.getWorld());
							loc.add(center);
							Material m = s.getValue().getMaterial();
							if(fight != null) {
								if(CachedBlockChange.CACHE_BLOCKS) {
									fight.getBlockChanges().add(new CachedBlockChange(loc, loc.getBlock()));
									Block b2 = loc.clone().subtract(0, 1, 0).getBlock();
									if(RollbackListener.turnsToDirt(b2)) {
										fight.addBlockChange(new CachedBlockChange(b2.getLocation(), b2));
									}
								}
								loc.getBlock().setType(m);
								loc.getBlock().setData((byte) s.getValue().getDurability());
								loc.getBlock().getState().update();
								loc.getBlock().setMetadata(RollbackListener.PLACED_IN_FIGHT, new FixedMetadataValue(plugin, fight));
							}
							if(m == Material.LAVA || m == Material.STATIONARY_LAVA || m == Material.WATER || m == Material.STATIONARY_WATER) {
								Bukkit.getPluginManager().callEvent(new PlayerBucketEmptyEvent(npc.getBukkitEntity(), loc.getBlock(), loc.getBlock().getFace(loc.getBlock().getRelative(0, 0, 0)), m, npc.getBukkitEntity().getItemInHand()));
							}
							for(Entity ent : npc.getBukkitEntity().getNearbyEntities(64, 64, 64)) {
								if(ent instanceof org.bukkit.entity.Player) {
									org.bukkit.entity.Player p = (org.bukkit.entity.Player) ent;
									SoundUtil.playPlaceSound(p, loc, m);
								}
							}
						}
					}
					if(player.getSneaks().containsKey(tickCounter)) {
						npc.getBukkitEntity().setSneaking(player.getSneaks().get(tickCounter));
					}
					if(player.getSprints().containsKey(tickCounter)) {
						npc.getBukkitEntity().setSprinting(player.getSprints().get(tickCounter));
					}
					if(!player.getSneaks().containsKey(tickCounter) && npc.getBukkitEntity().isOnGround()) {
						if(steps == 0) {
							for(Entity ent : npc.getBukkitEntity().getNearbyEntities(64, 64, 64)) {
								if(ent instanceof org.bukkit.entity.Player) {
									org.bukkit.entity.Player p = (org.bukkit.entity.Player) ent;
									SoundUtil.playStepSound(p, npc.getBukkitEntity().getLocation(), npc.getBukkitEntity().getLocation().subtract(0, 1, 0).getBlock().getType());
								}
							}
						}
						else steps = npc.getBukkitEntity().isSprinting() ? 3 : 5;
					}
					if(player.getSwung().contains(tickCounter)) {
						npc.swingMainArm();
					}
					if(player.getTookDamage().containsKey(tickCounter)) {
						npc.hurt(player.getTookDamage().get(tickCounter));
					}
					ItemStack lastHand = getLastItem(player.getItemsInHand(), tickCounter);
					npc.getBukkitEntity().setItemInHand(lastHand);	
					ItemStack lastHelmet = getLastItem(player.getHelmets(), tickCounter);
					npc.getBukkitEntity().getInventory().setHelmet(lastHelmet);
					ItemStack lastChest = getLastItem(player.getChestplates(), tickCounter);
					npc.getBukkitEntity().getInventory().setChestplate(lastChest);
					ItemStack lastLeggings = getLastItem(player.getLeggings(), tickCounter);
					npc.getBukkitEntity().getInventory().setLeggings(lastLeggings);
					ItemStack lastBoots = getLastItem(player.getBoots(), tickCounter);
					npc.getBukkitEntity().getInventory().setBoots(lastBoots);
					if(player.getProjectiles().containsKey(tickCounter)) {
						SerializableProjectile proj = player.getProjectiles().get(tickCounter);
						proj.launch(npc.getBukkitEntity().getLocation(), npc.getBukkitEntity());
					}
					if(player.getBlocksBroken().containsKey(tickCounter)) {
						Entry<Location, Integer> entry = player.getBlocksBroken().get(tickCounter);
						Location l = entry.getKey().clone();
						l.setWorld(center.getWorld());
						Block b = l.add(center).getBlock();
						if(!b.isLiquid()) {
							plugin.getNMSAccessProvider().getAccess().breakBlockWithAnimation(b, entry.getValue());
						}
						else {
							b.setType(Material.AIR);
						}
						if(entry.getValue() > 0) {
							new BukkitRunnable() {

								int counter;

								@Override
								public void run() {
									npc.swingMainArm();
									counter++;
									if(counter >= entry.getValue()) {
										this.cancel();
									}
								}
							}.runTaskTimer(plugin, 0, 1);
						}
						else npc.swingMainArm();
					}
				}
			}
			tickCounter++;
		}catch(Exception e) {
			runnable.cancel();
			stop();
			Bukkit.getLogger().warning("Player playback was stopped because an error occurred!");
			e.printStackTrace();
		}
	}

	private ItemStack getLastItem(HashMap<Integer, SimpleItem> items, int tick) {
		if(!items.isEmpty()) {
			for(int i = tick; i > 0; i--) {
				if(items.containsKey(i)) {
					return items.get(i).toItemStack();
				}
			}
		}
		return new ItemStack(Material.AIR);
	}

	/**
	 * @return the player
	 */
	public RecordedPlayer getPlayer() {
		return player;
	}

	/**
	 * @return the npc
	 */
	public CitizensNPC getNPC() {
		return npc;
	}

	/**
	 * @return the playing
	 */
	public boolean isPlaying() {
		return playing;
	}
}
