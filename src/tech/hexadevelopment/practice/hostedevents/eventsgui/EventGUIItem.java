package tech.hexadevelopment.practice.hostedevents.eventsgui;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SerializableAs("GUIItem")
public class EventGUIItem implements ConfigurationSerializable{

	
	private ItemStack item;
	private String command;
	private int slot;
	
	
	public EventGUIItem(ItemStack item, String command, int slot) {
		this.item = item;
		this.command = command;
		this.slot = slot;
	}
	
	public EventGUIItem(Map<String, Object> serialized) {
		if (serialized == null) return;
		if (serialized.isEmpty()) return;
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
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> serialized = new HashMap<String, Object>();
		serialized.put("item", item);
		serialized.put("slot", slot);
		serialized.put("command", command);
		return serialized;
	}
	
	public void execute(Player p) {
		if(!command.startsWith("/")) command = "/" + command;
		p.chat(command);
	}
	
	public String getCommand() {
		return command;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public int getSlot() {
		return slot;
	}
	
	public void setCommand(String command) {
		this.command = command;
	}
	
	public void setItem(ItemStack item) {
		this.item = item;
	}
	
	public void setSlot(int slot) {
		this.slot = slot;
	}
}
