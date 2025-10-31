package tech.hexadevelopment.practice.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

@SerializableAs("Projectile")
public class SerializableProjectile implements ConfigurationSerializable{

	private Vector velocity;
	private EntityType type;
	private List<PotionEffect> effects = new ArrayList<PotionEffect>();
	private ItemStack item;
	private int tick;

	public SerializableProjectile(Projectile projectile, int tick) {
		this.velocity = projectile.getVelocity();
		this.type = projectile.getType();
		this.tick = tick;
		if(projectile instanceof ThrownPotion) {
			ThrownPotion pot = (ThrownPotion) projectile;
			effects.addAll(pot.getEffects());
			item = pot.getItem();
			if(item.getAmount() <= 0) {
				item.setAmount(1);
			}
		}
	}

	public SerializableProjectile(Map<String, Object> serialized) {
		if (serialized == null) return;
		if (serialized.isEmpty()) return;
		if(serialized.containsKey("velocity") 
				&& serialized.get("velocity")instanceof String) {
			String ser = ((String) serialized.get("velocity"));
			SerializableLocation serLoc = SerializableLocation.fromString(ser);
			velocity = new Vector(serLoc.getX(), serLoc.getY(), serLoc.getZ());
		}
		if(serialized.containsKey("effects") 
				&& serialized.get("effects") instanceof List<?>) {
			for(Object o : (List<?>) serialized.get("effects")) {
				if(o instanceof PotionEffect) {
					effects.add((PotionEffect) o);
				}
			}
		}
		if(serialized.containsKey("item") 
				&& serialized.get("item")instanceof ItemStack) {
			item = (ItemStack) serialized.get("item");
		}
		if(serialized.containsKey("type") && serialized.get("type") instanceof String) {
			type = EntityType.valueOf((String) serialized.get("type"));
		}
		if(serialized.containsKey("tick")) {
			tick = (int) serialized.get("tick");
		}
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> serialized = new HashMap<String, Object>();
		SerializableLocation ser = new SerializableLocation(velocity.toLocation(Bukkit.getWorlds().get(0)));
		ser.setWorld(null);
		serialized.put("velocity", ser.toString());
		if(item != null) {
			serialized.put("item", item);
		}
		serialized.put("type", type.toString());
		serialized.put("effects", effects);
		serialized.put("tick", tick);
		return serialized;
	}

	public void launch(Location l, LivingEntity entity) {
		try{
			if(type == EntityType.SPLASH_POTION) {
				Potion potion = new Potion(Potion.fromItemStack(item).getType());
				if((item.getDurability() == 16389 || item.getDurability() == 16421)) {
					potion.setType(PotionType.INSTANT_HEAL);
				}
				potion.setSplash(true);
				potion.apply(item);
				ThrownPotion thrownPotion = entity.launchProjectile(ThrownPotion.class);
				thrownPotion.setItem(potion.toItemStack(1));
				thrownPotion.setVelocity(velocity);
			}
			else {
				Entity ent = l.getWorld().spawnEntity(l, type);
				ent.setVelocity(velocity);
			}
		}catch(Exception e) {}
	}

	/**
	 * @return the velocity
	 */
	public Vector getVelocity() {
		return velocity;
	}

	/**
	 * @param velocity the velocity to set
	 */
	public void setVelocity(Vector velocity) {
		this.velocity = velocity;
	}

	/**
	 * @return the type
	 */
	public EntityType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(EntityType type) {
		this.type = type;
	}

	/**
	 * @return the effects
	 */
	public List<PotionEffect> getEffects() {
		return effects;
	}

	/**
	 * @param effects the effects to set
	 */
	public void setEffects(List<PotionEffect> effects) {
		this.effects = effects;
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
	 * @return the tick
	 */
	public int getTick() {
		return tick;
	}

	/**
	 * @param tick the tick to set
	 */
	public void setTick(int tick) {
		this.tick = tick;
	}


}
