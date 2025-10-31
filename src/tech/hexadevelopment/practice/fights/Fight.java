package tech.hexadevelopment.practice.fights;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.arena.CachedBlockChange;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.fightinventory.FightInventory;
import tech.hexadevelopment.practice.fights.savedfights.SavedFight;
import tech.hexadevelopment.practice.LegionPractice;
/**
 * Class to make new fights easily.
 * Contains a few methods to register player's current fight.
 * It will automatically handle death of the player's current fight when the player dies.
 * @author Toppe5
 * @since 0.1
 */
public abstract class Fight {

	public static String currentFight = "LegionPracticeCurrentPrivateFight";
	
	private HashSet<CachedBlockChange> blockChanges = new HashSet<CachedBlockChange>();
	
	protected BattleKit kit;
	protected Arena arena;
	protected long started;
	protected long ended;
	
	/**
	 * Starts the actual fight
	 */
	public abstract void start();
	
	/**
	 * Gets if specatting is allowed in the fight.
	 * @return true if spectating is allowed in the fight, false if it's not.
	 */
	public abstract boolean allowSpectating();
	
	
	/**
	 * Gets the current arena of the fight.
	 * @return an arena or null if the arena is invalid.
	 */
	public Arena getArena() {
		return arena;
	}
	
	public void setArena(Arena arena) {
		this.arena = arena;
	}
	
	public void setKit(BattleKit kit) {
		this.kit = kit;
	}
	
	/**
	 * Gets if the fight has ended, or is ending and player's are going to be teleported soon.
	 * Block breaking and dropping items are also disabled when the fight has been ended.
	 * @return true if the fight has ended, false if it hasn't ended yet.
	 */
	public abstract boolean hasEnded();
	
	/**
	 * Gets if the fight can be started.
	 * @return true if the fight can start.
	 */
	public abstract boolean canStart();
	
	/**
	 * This will be called when the player died
	 * @param p player who died.
	 */
	public abstract void handleDeath(Player p);
	
	public abstract void forceEnd(String reason);
	
	public BattleKit getKit() {
		return kit;
	}
	
	public long getStarted() {
		return started;
	}
	
	public long getEnded() {
		return ended;
	}
	
	public void setStartedAfterCountdown() {
		started = System.currentTimeMillis()+(LegionPractice.getInstance().getConfig().getInt("countdown-time")*1000);
	}

	public long getDuration() {
		if(started > System.currentTimeMillis()) {
			return 0;
		}
		if(hasEnded()) return ended-started;
		return System.currentTimeMillis()-started;
	}
	
	public HashSet<CachedBlockChange> getBlockChanges() {
		return blockChanges;
	}

	public void addBlockChange(CachedBlockChange change) {
		for(CachedBlockChange c : blockChanges) {
			if(c.getX() == change.getX() && c.getY() == change.getY() && c.getZ() == change.getZ()) {
				return;
			}
		}
		blockChanges.add(change);
	}
	
	public abstract SavedFight saveFight(List<UUID> winners, List<UUID> losers, List<FightInventory> winnersInventories, List<FightInventory> losersInventories, UUID uuid);
	
	/**
	 * Gets the entity's current fight.
	 * @param p the entity whose fight it will get.
	 * @param plugin LegionPractice plugin
	 * @return null if the entity doesn't have a current fight, the fight if he has.
	 */
	public static Fight getCurrentFight(Metadatable metadatable, LegionPractice plugin) {
		if(metadatable.hasMetadata(currentFight)) {
			MetadataValue m = plugin.getMetadata(metadatable, currentFight);
			if(m != null && m.value() != null && m.value() instanceof Fight) {
				return (Fight) m.value();
			}
		}
		return null;
	}
	
	
	
	/**
	 * Sets the metadatable's current fight.
	 * @param metadatable metadatable whose fight.
	 * @param fight fight that will be set as metadatable's current fight.
	 * @param plugin LegionPractice plugin.
	 */
	public static void setCurrentFight(Metadatable metadatable, Fight fight, LegionPractice plugin) {
		if(fight == null) {
			metadatable.removeMetadata(currentFight, plugin);
			metadatable.removeMetadata(plugin.IN_FIGHT, plugin);
		}
		else {
			metadatable.setMetadata(currentFight, new FixedMetadataValue(plugin, fight));
		}
	}

	/**
	 * Gets if the player's current fight is valid.
	 * @param p the player to check.
	 * @param plugin LegionPractice plugin.
	 * @return true if the player's current fight is valid.
	 */
	public static boolean isInFight(Player p, LegionPractice plugin) {
		return p.hasMetadata(currentFight) && getCurrentFight(p, plugin) != null;
	}
	
}