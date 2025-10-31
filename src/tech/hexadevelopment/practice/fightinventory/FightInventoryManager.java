package tech.hexadevelopment.practice.fightinventory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;

/**
 * Manager for FightInventories containing a few useful static methods.
 * @author Toppe5
 * @since 0.1
 */
public class FightInventoryManager implements CommandExecutor{

	private static HashMap<UUID, FightInventory> fightInventories = new HashMap<UUID, FightInventory>();
	private static HashMap<String, UUID> uuids = new HashMap<String, UUID>();
	private LegionPractice plugin;

	public FightInventoryManager(LegionPractice plugin) {
		this.plugin = plugin;
	}

	/**
	 * Save a FightInventory
	 * @param inv the inventory that will be saved.
	 */
	public void saveInventory(FightInventory inv) {
		if(inv != null && inv.getUUID() != null) {
			fightInventories.put(inv.getUUID(), inv);
			if(fightInventories.size() > 100) {
				Iterator<FightInventory> it = fightInventories.values().iterator();
				while(it.hasNext()) {
					FightInventory fi = it.next();
					if(fi.createdLongTimeAgo()) {
						uuids.remove(inv.getOwner(), inv.getUUID());
						it.remove();
					}
				}
			}
		}
		if(inv != null && inv.getOwner() != null) {
			uuids.put(inv.getOwner(), inv.getUUID());
		}
	}

	/**
	 * Gets the player's last FightInventory by the player's name.
	 * @param name the name it will search with.
	 * @return a uuid of the player's last FightInventory, null if the player doesn't have a last FightInventory.
	 */
	public UUID getInventoryUUIDByName(String name) {
		if(uuids.containsKey(name)) {
			UUID uuid = uuids.get(name);
			if(fightInventories.containsKey(uuid)) {
				return uuid;
			}
		}
		return null;
	}

	/**
	 * Gets the player's last FightInventory by the player's name.
	 * @param p the player whose last FightInventory it will search.
	 * @return a uuid of the player's last FightInventory, null if the player doesn't have a last FightInventory.
	 */
	public UUID getInventoryUUIDByName(Player p) {
		return getInventoryUUIDByName(p.getName());
	}

	/**
	 * Gets a FightInventory by its UUID.
	 * @param uuid UUID of the FightInventory.
	 * @return
	 */
	public FightInventory getFightInventory(UUID uuid) {
		return fightInventories.containsKey(uuid) ? fightInventories.get(uuid) : null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if(args.length >= 1) {
				try{
					UUID uuid =  UUID.fromString(args[0]);
					FightInventory fightInv = getFightInventory(uuid);
					if(fightInv == null) {
						p.sendMessage(plugin.translateMessage(p, "invalid-inventory"));
						return true;
					}
					p.openInventory(fightInv.build(p));
					return true;
				}catch(IllegalArgumentException e) {
					UUID uuid = getInventoryUUIDByName(args[0]);
					if(uuid == null) {
						p.sendMessage(plugin.translateMessage(p, "invalid-inventory"));
						return true;
					}
					FightInventory fightInv = getFightInventory(uuid);
					if(fightInv == null) {
						p.sendMessage(plugin.translateMessage(p, "invalid-inventory"));
						return true;
					}
					else {
						p.openInventory(fightInv.build(p));
						return true;
					}
				}
			}
		}
		return true;
	}
}
