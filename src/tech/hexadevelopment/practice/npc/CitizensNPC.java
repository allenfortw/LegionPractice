package tech.hexadevelopment.practice.npc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import tech.hexadevelopment.practice.fights.Fight;
import tech.hexadevelopment.practice.fights.party.actions.PartyActionsManager;
import tech.hexadevelopment.practice.matchrecorder.Damage;
import tech.hexadevelopment.practice.misc.BotSumoRunnable;
import tech.hexadevelopment.practice.utils.SoundManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.PlayerAnimation;
import net.citizensnpcs.util.Util;
import tech.hexadevelopment.practice.LegionPractice;

public class CitizensNPC {

	public static List<CitizensNPC> npcs = new ArrayList<CitizensNPC>();
	private static boolean teleportFix;

	private NPC npc;
	private boolean destroyed;
	Location spawnLocation;
	public CombatTask combatTask;


	public CitizensNPC(String name, String skin, Location loc){
		this.spawnLocation = loc;
		npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);
		if(LegionPractice.getInstance().getConfig().getBoolean("always-same-skin")) {
			npc.data().set("player-skin-name", LegionPractice.getInstance().getConfig().getString("bot-skin"));
		}
		else {
			npc.data().set("player-skin-name", skin);
		}
		if(LegionPractice.getInstance().getFileManager().getNPCFile() != null) {
			List<String> list = LegionPractice.getInstance().getFileManager().getNPCConfig().getStringList("remove");
			list.add(npc.getUniqueId().toString());
			LegionPractice.getInstance().getFileManager().getNPCConfig().set("remove", list);
			LegionPractice.getInstance().getFileManager().saveNPCFile();
		}
		if (npc.isSpawned()) {
			npc.despawn();
		}
		new BukkitRunnable() {

			int tries = 0;

			@Override
			public void run() {
				if(tries++ == 100) {
					this.cancel();
				}
				if(loc != null && loc.getWorld() != null && loc.getChunk() != null) {
					if(loc.getChunk().isLoaded()) {
						npc.spawn(loc);
						this.cancel();
					}
					else loc.getChunk().load();
				}
			}
		}.runTaskTimer(LegionPractice.getInstance(), 0, 1);
		npcs.add(this);
	}

	public static void onEnable() {
		teleportFix = LegionPractice.getInstance().getConfig().getBoolean("bot-teleport-error-fix");
		new BukkitRunnable() {

			@Override
			public void run() {
				try{
					Iterator<NPC> i = CitizensAPI.getNPCRegistry().iterator();
					List<NPC> remove = new ArrayList<NPC>();
					while(i.hasNext()) {
						NPC npc = i.next();
						if(LegionPractice.getInstance().getFileManager().getNPCFile() != null
								&& LegionPractice.getInstance().getFileManager().getNPCConfig().get("remove") != null && LegionPractice.getInstance().getFileManager().getNPCConfig()
								.getStringList("remove").contains(npc.getUniqueId().toString())) {
							remove.add(npc);
						}
					}
					for(NPC npc : remove) {
						CitizensAPI.getNPCRegistry().deregister(npc);
					}
					LegionPractice.getInstance().citizens = true;
					if(LegionPractice.getInstance().getConfig().getBoolean("always-same-skin")) {
						String skin = LegionPractice.getInstance().getConfig().getString("bot-skin");
						Bukkit.getLogger().info("Spawning a test npc '" + skin + "' to fetch npcs' skin. The npc will be removed in 10 seconds.");

						CitizensNPC testNPC = new CitizensNPC(skin, skin, LegionPractice.getInstance().arenaPvP.getLobby().subtract(0, 100, 0));
						new BukkitRunnable() {

							@Override
							public void run() {
								if(!testNPC.isDestroyed()) {
									testNPC.destroy();
								}
							}
						}.runTaskLater(LegionPractice.getInstance(), 20*5);
					}
					//add the playback and bots and start bot sumo runnable
					PartyActionsManager.getPartyActionsManager().loadIcons(LegionPractice.getInstance());
					new BotSumoRunnable(LegionPractice.getInstance());
				}catch(Exception e) {
					e.printStackTrace();
					Bukkit.getLogger().warning("Citizens was not found (or is not right version). PvPBot and match playbacks are not available!");
					LegionPractice.getInstance().getFileManager().removeNPCFile();
				}
			}
		}.runTaskLater(LegionPractice.getInstance(), 20);
	}


	public boolean isSpawned(){
		return npc.isSpawned();
	}

	public Player getBukkitEntity(){
		return (Player)npc.getEntity();
	}


	public void destroy(boolean fast) {
		npc.despawn(DespawnReason.PLUGIN);
		if(fast) {
			LegionPractice plugin = LegionPractice.getInstance();
			destroyed = true;
			if(getBukkitEntity() != null) {
				getBukkitEntity().setHealth(20);
			}
			npc.destroy();
			List<String> list = plugin.getFileManager().getNPCConfig().getStringList("remove");
			list.remove(npc.getUniqueId().toString());
			plugin.getFileManager().getNPCConfig().set("remove", list);
			plugin.getFileManager().saveNPCFile();
		}
		else destroy();
	}


	public void destroy(){
		LegionPractice plugin = LegionPractice.getInstance();
		if(!destroyed) {
			teleport(plugin.arenaPvP.getLobby().subtract(0, 100, 0));
			new BukkitRunnable() {

				@Override
				public void run() {
					destroy(true);
				}
			}.runTaskLater(plugin, 15);
		}
	}

	public void swingMainArm(){
		if(getBukkitEntity() != null) {
			PlayerAnimation.ARM_SWING.play(getBukkitEntity());
		}
	}

	public NPC getNPC() {
		return npc;
	}

	public void hurt(Damage damage) {
		getBukkitEntity().playEffect(EntityEffect.HURT);
		for(Entity ent : getBukkitEntity().getNearbyEntities(100, 100, 100)) {
			if(ent instanceof Player) {
				SoundManager.playSound((Player) ent, getBukkitEntity().getLocation(), "HURT_FLESH", (float) 0.7, 1);
			}
		}
		if(damage.isBurn()) {
			getBukkitEntity().setFireTicks(20);
		}
		else {
			Location l = getBukkitEntity().getLocation().add(0, 1, 0);
			if(damage.isCritical()) {
				for(int i = 0; i < LegionPractice.random.nextInt(5)+10; i++) {
					l.getWorld().playEffect(l, Effect.CRIT, 1);
				}
			}
			if(damage.isSharpness()) {
				for(int i = 0; i < LegionPractice.random.nextInt(5)+10; i++) {
					l.getWorld().playEffect(l, Effect.MAGIC_CRIT, 1);
				}
			}
		}
	}

	public void setVelocity(Vector v) {
		if(npc != null && !destroyed) {
			getBukkitEntity().setVelocity(v);
		}
	}

	public boolean teleport(Location loc){
		if(loc == null || getBukkitEntity() == null) return false;
		if(teleportFix && (errorTeleport(loc) || errorTeleport(getBukkitEntity().getLocation()))) return false;
		if(!loc.getChunk().isLoaded()) loc.getChunk().load();
		try{
			npc.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
			Util.assumePose((Entity)getBukkitEntity(), loc.getYaw(), loc.getPitch());
			return true;
		}catch(Exception e) {}
		return false;
	}

	private boolean errorTeleport(Location loc) {
		for(Player p : loc.getWorld().getPlayers()) {
			if(p.getLocation().distanceSquared(loc) < 200*200) {
				return false;
			}
		}
		return true;
	}

	public void setDestroyed(boolean destroyed) {
		this.destroyed = destroyed;
	}

	public boolean isDestroyed() {
		return destroyed;
	}
	
	public static double getDamage(ItemStack item) {
		if(LegionPractice.getInstance().getNMSAccessProvider().versionHasNoItemIDs) {
			return getDamageNoId(item);
		}
		return getDamageId(item);
	}

	public static double getDamageId(ItemStack item) {
		double d;
		switch(item.getTypeId()) {
		case 268:
		case 283:
			d = 4;
			break;
		case 272:
			d = 5;
			break;
		case 267:
			d = 6;
			break;
		case 276:
			d = 7;
			break;
		case 269:
		case 284:
			d = 1;
			break;
		case 273:
			d = 2;
			break;
		case 256:
			d = 3;
			break;
		case 277:
			d = 4;
			break;
		case 270:
		case 285:
			d = 2;
			break;
		case 274:
			d = 3;
			break;
		case 257:
			d = 4;
			break;
		case 278:
			d = 5;
			break;
		case 271:
		case 286:
			d = 3;
			break;
		case 275:
			d = 4;
			break;
		case 258:
			d = 5;
			break;
		case 279:
			d = 6;
			break;
		default:
			d = 1;
			break;
		}
		return d;
	}
	
	
	public static double getDamageNoId(ItemStack item) {
		double d;
		//for some reason my obfuscator changes types so using string instead
		switch(item.getType().toString()) {
		case "WOOD_SWORD":
		case "GOLD_SWORD":
			d = 4;
			break;
		case "STONE_SWORD":
			d = 5;
			break;
		case "IRON_SWORD":
			d = 6;
			break;
		case "DIAMOND_SWORD":
			d = 7;
			break;
		case "WOOD_SPADE":
		case "GOLD_SPADE":
			d = 1;
			break;
		case "STONE_SPADE":
			d = 2;
			break;
		case "IRON_SPADE":
			d = 3;
			break;
		case "DIAMOND_SPADE":
			d = 4;
			break;
		case "WOOD_PICKAXE":
		case "GOLD_PICKAXE":
			d = 2;
			break;
		case "STONE_PICKAXE":
			d = 3;
			break;
		case "IRON_PICKAXE":
			d = 4;
			break;
		case "DIAMOND_PICKAXE":
			d = 5;
			break;
		case "WOOD_AXE":
		case "GOLD_AXE":
			d = 3;
			break;
		case "STONE_AXE":
			d = 4;
			break;
		case "IRON_AXE":
			d = 5;
			break;
		case "DIAMOND_AXE":
			d = 6;
			break;
		default:
			d = 1;
			break;
		}
		return d;
	}

	public void startCombatTask(List<UUID> players, Fight fight, Difficulty difficulty) {
		this.combatTask = new CombatTask(this, players, fight, difficulty);
	}


	public enum Difficulty {
		EASY,
		NORMAL,
		HARD,
		HACKER
	}
}
