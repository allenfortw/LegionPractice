package tech.hexadevelopment.practice.language;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;

@SerializableAs("LanguageItem")
public class LanguageItem implements ConfigurationSerializable{
	
	private int slot;
	private ItemStack item;
	private String language;

	public LanguageItem(ItemStack item, String language, int slot) {
		this.item = item;
		this.language = language;
		this.slot = slot;
	}

	public LanguageItem(Map<String, Object> serialized) {
		if(serialized.containsKey("language") && serialized.containsKey("item")
				&& serialized.containsKey("slot")) {
			Object l = serialized.get("language");
			if(l instanceof String) this.language = (String) l;
			Object i = serialized.get("item");
			if(i instanceof ItemStack) this.item = (ItemStack) i;
			Object s = serialized.get("slot");
			if(s instanceof Integer) this.slot = (int) s;
		}
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public void setItem(ItemStack item) {
		this.item = item;
	}
	
	public void setSlot(int slot) {
		this.slot = slot;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public int getSlot() {
		return slot;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> serialized = new HashMap<String, Object>();
		serialized.put("language", language);
		serialized.put("slot", slot);
		serialized.put("item", item);
		return serialized;
	}
	
	public static LanguageItem deserialize(Map<String, Object> serialized) {
		return new LanguageItem(serialized);
	}

	public static LanguageItem valueOf(Map<String, Object> serialized) {
		return new LanguageItem(serialized);
	}
}
