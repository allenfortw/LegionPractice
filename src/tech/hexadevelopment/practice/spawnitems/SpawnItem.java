package tech.hexadevelopment.practice.spawnitems;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SerializableAs("SpawnItem")
public class SpawnItem implements ConfigurationSerializable{
	
	
	private ItemStack item;
	private String command;
	private String name;
	private String permission;
	private int slot;
	private boolean party;
	private boolean partyOwnerOnly;
	private boolean queue;
	
	public SpawnItem(Player p, String name, String command) {
		this.item = p.getItemInHand();
		this.slot = p.getInventory().getHeldItemSlot();
		this.name = name;
		if(!command.startsWith("/")) {
			this.command = "/" + command;
		}
		else this.command = command;
	}
	
	public SpawnItem(Map<String, Object> serialized) {
		if (serialized == null) return;
		if (serialized.isEmpty()) return;
		if(serialized.containsKey("name") 
				&& serialized.get("name") instanceof String) { 
			name = ((String) serialized.get("name")).replace(" ", "");
		}
		if(serialized.containsKey("permission") && serialized.get("permission") instanceof String) {
			permission = (String) serialized.get("permission");
		}
		if(serialized.containsKey("slot") 
				&& serialized.get("slot") instanceof Integer) { 
			slot = (Integer) serialized.get("slot");
		}
		if(serialized.containsKey("item") 
				&& serialized.get("item") instanceof ItemStack) { 
			item = (ItemStack) serialized.get("item");
		}
		if(serialized.containsKey("command") 
				&& serialized.get("command") instanceof String) { 
			command = (String) serialized.get("command");
		}
		if(serialized.containsKey("party") 
				&& serialized.get("party") instanceof Boolean) { 
			party = (Boolean) serialized.get("party");
		}
		if(serialized.containsKey("party-owner-only") 
				&& serialized.get("party-owner-only") instanceof Boolean) { 
			partyOwnerOnly = (Boolean) serialized.get("party-owner-only");
		}
		if(serialized.containsKey("queue") 
				&& serialized.get("queue") instanceof Boolean) { 
			queue = (Boolean) serialized.get("queue");
		}
		if(name == null) name = command.replace("/", "").replace(" ", "");
	}
	
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> serialized = new HashMap<String, Object>();
		serialized.put("name", name);
		serialized.put("item", item);
		serialized.put("slot", slot);
		serialized.put("command", command);
		if(permission != null) serialized.put("permission", permission);
		if(party)serialized.put("party", party);
		if(partyOwnerOnly)serialized.put("party-owner-only", partyOwnerOnly);
		if(queue)serialized.put("queue", queue);
		return serialized;
	}
	
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean hasPermission() {
		return permission != null;
	}

	public String getPermission() {
		return permission;
	}
	
	public void setPermission(String permission) {
		this.permission = permission;
	}
	
	/**
	 * @return the queue
	 */
	public boolean isQueue() {
		return queue;
	}

	/**
	 * @param queue the queue to set
	 */
	public void setQueue(boolean queue) {
		this.queue = queue;
	}

	/**
	 * @return the partyOwnerOnly
	 */
	public boolean isPartyOwnerOnly() {
		return partyOwnerOnly;
	}

	/**
	 * @param partyOwnerOnly the partyOwnerOnly to set
	 */
	public void setPartyOwnerOnly(boolean partyOwnerOnly) {
		this.partyOwnerOnly = partyOwnerOnly;
	}

	/**
	 * @return the item
	 */
	public ItemStack getItem() {
		return item;
	}
	
	/**
	 * @param item the item to set
	 */
	public void setItem(ItemStack item) {
		this.item = item;
	}
	
	/**
	 * @return the party
	 */
	public boolean isParty() {
		return party;
	}

	/**
	 * @param party the party to set
	 */
	public void setParty(boolean party) {
		this.party = party;
	}

	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}
	
	/**
	 * @param command the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}
	
	/**
	 * @return the slot
	 */
	public int getSlot() {
		return slot;
	}
	
	/**
	 * @param slot the slot to set
	 */
	public void setSlot(int slot) {
		this.slot = slot;
	}
	
}
