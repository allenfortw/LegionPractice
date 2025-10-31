package tech.hexadevelopment.practice.arena;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import tech.hexadevelopment.practice.utils.SerializableLocation;

public class CachedBlockChange {

	public static boolean CACHE_BLOCKS;

	private Material oldMaterial;
	private byte oldData;
	private int x, y, z;
	private String world;
	private ItemStack[] chestInventory;


	public CachedBlockChange(Location location, Block oldBlock) {
		this.oldMaterial = oldBlock.getType();
		this.oldData = oldBlock.getData();
		this.world = location.getWorld().getName();
		this.x = location.getBlockX();
		this.y = location.getBlockY();
		this.z = location.getBlockZ();
		saveChest(location);
	}


	public CachedBlockChange(Location location, Material type, byte data) {
		this.oldMaterial = type;
		this.oldData = data;
		this.world = location.getWorld().getName();
		this.x = location.getBlockX();
		this.y = location.getBlockY();
		this.z = location.getBlockZ();
		saveChest(location);
	}
	
	private void saveChest(Location loc) {
		try {
			Block b = loc.getBlock();
			if(b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
				Chest chest = (Chest) b.getState();
				InventoryHolder h = chest.getInventory().getHolder();
				chestInventory = h.getInventory().getContents();
				for(ItemStack is : chestInventory) {
					if(is != null && is.getAmount() <= 0) {
						is.setAmount(1);
					}
				}
			}
		}catch(Exception e) {
			Bukkit.getLogger().info("LegionPractice >> Arena regen failed to save chest contents");
		}
	}


	public void reset() {
		Location location = getLocation();
		if(location != null && oldMaterial != null) {
			location.getBlock().setType(oldMaterial);
			location.getBlock().setData(oldData);
			location.getBlock().getState().update(false);
			if(chestInventory != null && (oldMaterial == Material.CHEST || oldMaterial == Material.TRAPPED_CHEST)) {
				Block b = location.getBlock();
				Chest chest = (Chest) b.getState();
				InventoryHolder h = chest.getInventory().getHolder();
				h.getInventory().setContents(chestInventory);
				for(ItemStack is : h.getInventory().getContents()) {
					if(is != null && is.getAmount() <= 0) {
						is.setAmount(1);
					}
				}
			}
		}
	}
	
	public Location getLocation() {
		if(world == null) return null;
		return new Location(Bukkit.getWorld(world), x, y, z);
	}

	/**
	 * @return the oldMaterial
	 */
	public Material getOldMaterial() {
		return oldMaterial;
	}


	/**
	 * @return the oldData
	 */
	public short getOldData() {
		return oldData;
	}


	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}


	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}


	/**
	 * @return the z
	 */
	public int getZ() {
		return z;
	}
	
	public static CachedBlockChange getByLocation(Location l, Collection<CachedBlockChange> list) {
		for(CachedBlockChange c : list) {
			if(l.equals(c.getLocation())) return c;
		}
		return null;
	}

	@Override
	public String toString() {
		return new SerializableLocation(getLocation()).toReadableString() + ":" + getOldMaterial() + ":" + getOldData();
	}

}
