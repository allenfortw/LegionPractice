package tech.hexadevelopment.practice.matchrecorder;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.Vector;

import tech.hexadevelopment.practice.utils.SerializableLocation;
import tech.hexadevelopment.practice.utils.SerializableProjectile;
import tech.hexadevelopment.practice.utils.SimpleItem;

@SerializableAs("RecordedPlayer")
public class RecordedPlayer implements ConfigurationSerializable{

	private List<Location> locations = new ArrayList<Location>();
	private List<Vector> velocities = new ArrayList<Vector>();
	private HashMap<Integer, Damage> tookDamage = new HashMap<Integer, Damage>();
	private List<Integer> swung = new ArrayList<Integer>();
	private HashMap<Integer, HashMap<Location, SimpleItem>> blocksPlaced = new HashMap<Integer, HashMap<Location, SimpleItem>>(); 
	private HashMap<Integer, Entry<Location, Integer>> blocksBroken = new HashMap<Integer, Entry<Location, Integer>>();
	private HashMap<Integer, SimpleItem> itemsInHand = new HashMap<Integer, SimpleItem>();
	private HashMap<Integer, SimpleItem> helmets = new HashMap<Integer, SimpleItem>();
	private HashMap<Integer, SimpleItem> chestplates = new HashMap<Integer, SimpleItem>();
	private HashMap<Integer, SimpleItem> leggings = new HashMap<Integer, SimpleItem>();
	private HashMap<Integer, SimpleItem> boots = new HashMap<Integer, SimpleItem>();
	private HashMap<Integer, Boolean> sneaks = new HashMap<Integer, Boolean>();
	private HashMap<Integer, Boolean> sprints = new HashMap<Integer, Boolean>();
	private HashMap<Integer, SerializableProjectile> projectiles = new HashMap<Integer, SerializableProjectile>();
	private int death;
	private UUID uuid;
	private String name;


	public RecordedPlayer(UUID uuid, String name) {
		this.uuid = uuid;
		this.name = name;
	}

	public RecordedPlayer(Map<String, Object> serialized) {
		if (serialized == null) return;
		if (serialized.isEmpty()) return;
		if(serialized.containsKey("name") && serialized.get("name") instanceof String) {
			this.name = (String) serialized.get("name");
		}
		if(serialized.containsKey("uuid") && serialized.get("uuid") instanceof String) {
			uuid = UUID.fromString((String) serialized.get("uuid"));
		}
		World defaultWorld = Bukkit.getWorlds().get(0);
		if(serialized.containsKey("locations") 
				&& serialized.get("locations") instanceof List<?>) {
			for(Object o : (List<?>) serialized.get("locations")) {
				if(o instanceof String) {
					SerializableLocation ser = SerializableLocation.fromString(((String) o));
					ser.setWorld(defaultWorld.getName());
					locations.add(ser.toLocation());
				}
			}
		}
		if(serialized.containsKey("velocities")
				&& serialized.get("velocities") instanceof List<?>) {
			for(Object o : (List<?>) serialized.get("velocities")) {
				if(o instanceof String) {
					SerializableLocation ser = SerializableLocation.fromString(((String) o));
					velocities.add(new Vector(ser.getX(), ser.getY(), ser.getZ()));
				}
			}
		}
		if(serialized.containsKey("swung")
				&& serialized.get("swung") instanceof List<?>) {
			for(Object o : (List<?>) serialized.get("swung")) {
				if(o instanceof Integer) {
					swung.add((Integer) o);
				}
			}
		}
		if(serialized.containsKey("took-damage")
				&& serialized.get("took-damage") instanceof List<?>) {
			for(Object o : (List<?>) serialized.get("took-damage")) {
				if(o instanceof String) {
					String[] s = ((String) o).split("-");
					if(s.length == 2) {
						tookDamage.put(Integer.parseInt(s[0]), Damage.fromString((String) s[1]));
					}
					else tookDamage.put(Integer.parseInt((String) o), new Damage(false, false, false));
				}
			}
		}
		if(serialized.containsKey("blocks-placed")
				&& serialized.get("blocks-placed") instanceof List<?>) {
			for(Object o : (List<?>) serialized.get("blocks-placed")) {
				if(o instanceof String) {
					String string = (String) o;
					String[] s = string.split(":");
					int tick = Integer.parseInt(s[0]);
					Material m = Material.getMaterial(s[1]);
					short sh = Short.parseShort(s[2]);
					if(!blocksPlaced.containsKey(tick)) {
						HashMap<Location, SimpleItem> blocks = new HashMap<Location, SimpleItem>();
						blocks.put(SerializableLocation.fromString(s[3]).toLocation(), new SimpleItem(m, sh));
						blocksPlaced.put(tick, blocks);	
					}
					else {
						HashMap<Location, SimpleItem> blocks = blocksPlaced.get(tick);
						blocks.put(SerializableLocation.fromString(s[3]).toLocation(), new SimpleItem(m, sh));
						blocksPlaced.put(tick, blocks);	
					}
				}
			}
		}
		if(serialized.containsKey("blocks-broken")
				&& serialized.get("blocks-broken") instanceof List<?>) {
			for(Object o : (List<?>) serialized.get("blocks-broken")) {
				if(o instanceof String) {
					String string = (String) o;
					String[] s = string.split(":");
					blocksBroken.put(Integer.parseInt(s[0]),
							new AbstractMap.SimpleEntry<>(SerializableLocation.fromString(s[1])
									.toLocation(), Integer.parseInt(s[2])));
				}
			}
		}
		if(serialized.containsKey("hands")
				&& serialized.get("hands") instanceof List<?>) {
			for(Object o : (List<?>) serialized.get("hands")) {
				if(o instanceof String) {
					String string = (String) o;
					String[] s = string.split(":");
					int tick = Integer.parseInt(s[0]);
					Material m = Material.getMaterial(s[1]);
					boolean enchanted = Boolean.parseBoolean(s[2]);
					itemsInHand.put(tick, new SimpleItem(m, enchanted, Short.parseShort(s[3])));
				}
			}
		}
		if(serialized.containsKey("helmets")
				&& serialized.get("helmets") instanceof List<?>) {
			for(Object o : (List<?>) serialized.get("helmets")) {
				if(o instanceof String) {
					String string = (String) o;
					String[] s = string.split(":");
					int tick = Integer.parseInt(s[0]);
					Material m = Material.getMaterial(s[1]);
					boolean enchanted = Boolean.parseBoolean(s[2]);
					helmets.put(tick, new SimpleItem(m, enchanted,  Short.parseShort(s[3])));
				}
			}
		}
		if(serialized.containsKey("chestplates")
				&& serialized.get("chestplates") instanceof List<?>) {
			for(Object o : (List<?>) serialized.get("chestplates")) {
				if(o instanceof String) {
					String string = (String) o;
					String[] s = string.split(":");
					int tick = Integer.parseInt(s[0]);
					Material m = Material.getMaterial(s[1]);
					boolean enchanted = Boolean.parseBoolean(s[2]);
					chestplates.put(tick, new SimpleItem(m, enchanted,  Short.parseShort(s[3])));
				}
			}
		}
		if(serialized.containsKey("leggings")
				&& serialized.get("leggings") instanceof List<?>) {
			for(Object o : (List<?>) serialized.get("leggings")) {
				if(o instanceof String) {
					String string = (String) o;
					String[] s = string.split(":");
					int tick = Integer.parseInt(s[0]);
					Material m = Material.getMaterial(s[1]);
					boolean enchanted = Boolean.parseBoolean(s[2]);
					leggings.put(tick, new SimpleItem(m, enchanted,  Short.parseShort(s[3])));
				}
			}
		}
		if(serialized.containsKey("boots")
				&& serialized.get("boots") instanceof List<?>) {
			for(Object o : (List<?>) serialized.get("boots")) {
				if(o instanceof String) {
					String string = (String) o;
					String[] s = string.split(":");
					int tick = Integer.parseInt(s[0]);
					Material m = Material.getMaterial(s[1]);
					boolean enchanted = Boolean.parseBoolean(s[2]);
					boots.put(tick, new SimpleItem(m, enchanted,  Short.parseShort(s[3])));
				}
			}
		}
		if(serialized.containsKey("sneaks")
				&& serialized.get("sneaks") instanceof List<?>) {
			for(Object o : (List<?>) serialized.get("sneaks")) {
				if(o instanceof String) {
					String string = (String) o;
					String[] s = string.split(":");
					int tick = Integer.parseInt(s[0]);
					boolean sneaking = Boolean.parseBoolean(s[1]);
					sneaks.put(tick, sneaking);
				}
			}
		}
		if(serialized.containsKey("sprints")
				&& serialized.get("sprints") instanceof List<?>) {
			for(Object o : (List<?>) serialized.get("sprints")) {
				if(o instanceof String) {
					String string = (String) o;
					String[] s = string.split(":");
					int tick = Integer.parseInt(s[0]);
					boolean sprinting = Boolean.parseBoolean(s[1]);
					sprints.put(tick, sprinting);
				}
			}
		}
		if(serialized.containsKey("projectiles")
				&& serialized.get("projectiles") instanceof List<?>) {
			for(Object o : (List<?>) serialized.get("projectiles")) {
				if(o instanceof SerializableProjectile) {
					SerializableProjectile pr = (SerializableProjectile) o;
					projectiles.put(pr.getTick(), pr);
				}
			}
		}
		if(serialized.containsKey("death")
				&& serialized.get("death") instanceof Integer) {
			death = (int) serialized.get("death");
		}
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> serialized = new HashMap<String, Object>();
		serialized.put("name", name);
		serialized.put("uuid", uuid.toString());
		List<String> hand = new ArrayList<String>();
		for(Entry<Integer, SimpleItem> o : itemsInHand.entrySet()) {
			hand.add(o.getKey() + ":" + o.getValue().getMaterial() + ":" + o.getValue().isEnchanted() + ":" + o.getValue().getDurability());
		}
		serialized.put("hands", hand);
		List<String> h = new ArrayList<String>();
		for(Entry<Integer, SimpleItem> o : helmets.entrySet()) {
			h.add(o.getKey() + ":" + o.getValue().getMaterial() + ":" + o.getValue().isEnchanted() + ":" + o.getValue().getDurability());
		}
		serialized.put("helmets", h);
		List<String> c = new ArrayList<String>();
		for(Entry<Integer, SimpleItem> o : chestplates.entrySet()) {
			c.add(o.getKey() + ":" + o.getValue().getMaterial() + ":" + o.getValue().isEnchanted() + ":" + o.getValue().getDurability());
		}
		serialized.put("chestplates", c);
		List<String> l = new ArrayList<String>();
		for(Entry<Integer, SimpleItem> o : leggings.entrySet()) {
			l.add(o.getKey() + ":" + o.getValue().getMaterial() + ":" + o.getValue().isEnchanted() + ":" + o.getValue().getDurability());
		}
		serialized.put("leggings", l);
		List<String> b = new ArrayList<String>();
		for(Entry<Integer, SimpleItem> o : boots.entrySet()) {
			b.add(o.getKey() + ":" + o.getValue().getMaterial() + ":" + o.getValue().isEnchanted() + ":" + o.getValue().getDurability());
		}
		serialized.put("boots", b);
		List<String> locs = new ArrayList<String>();
		List<String> sn = new ArrayList<String>();
		for(Entry<Integer, Boolean> o : sneaks.entrySet()) {
			sn.add(o.getKey() + ":" + o.getValue());
		}
		serialized.put("sneaks", sn);
		List<String> sp = new ArrayList<String>();
		for(Entry<Integer, Boolean> o : sprints.entrySet()) {
			sp.add(o.getKey() + ":" + o.getValue());
		}
		serialized.put("sprints", sp);
		for(Location loc : locations) {
			SerializableLocation ser = new SerializableLocation(loc);
			ser.setWorld(null);
			locs.add(ser.toString());
		}
		serialized.put("locations", locs);
		serialized.put("projectiles", projectiles.values().toArray());
		List<String> vels = new ArrayList<String>();
		for(Vector loc : velocities) {
			SerializableLocation ser = new SerializableLocation(loc.toLocation(locations.get(0).getWorld()));
			ser.setWorld(null);
			vels.add(ser.toString());
		}
		serialized.put("velocities", vels);
		List<String> blocksBrokenStrings = new ArrayList<String>();
		for(Entry<Integer, Entry<Location, Integer>> blockSet : blocksBroken.entrySet()) {
			blocksBrokenStrings.add(blockSet.getKey() + ":" + new SerializableLocation(blockSet.getValue().getKey()).toString() + ":" + blockSet.getValue().getValue());
		}
		serialized.put("blocks-broken", blocksBrokenStrings);
		List<String> blocksPlacedStrings = new ArrayList<String>();
		for(Entry<Integer, HashMap<Location, SimpleItem>> blockSet : blocksPlaced.entrySet()) {
			for(Entry<Location, SimpleItem> ml : blockSet.getValue().entrySet()) {
				String str = blockSet.getKey() + ":" + ml.getValue().getMaterial() + ":" + ml.getValue().getDurability() + ":" + new SerializableLocation(ml.getKey()).toString();
				blocksPlacedStrings.add(str);
			}
		}
		serialized.put("blocks-placed", blocksPlacedStrings);
		serialized.put("swung", swung);
		List<String> tookDamageList = new ArrayList<String>();
		for(Entry<Integer, Damage> e : tookDamage.entrySet()) {
			tookDamageList.add(e.getKey() + "-" + e.getValue().toString());
		}
		serialized.put("took-damage", tookDamageList);
		serialized.put("death", death);
		return serialized;
	}

	/**
	 * @return the uuid
	 */
	public UUID getUuid() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the locations
	 */
	public List<Location> getLocations() {
		return locations;
	}

	/**
	 * @return the projectiles
	 */
	public HashMap<Integer, SerializableProjectile> getProjectiles() {
		return projectiles;
	}

	/**
	 * @param projectiles the projectiles to set
	 */
	public void setProjectiles(HashMap<Integer, SerializableProjectile> projectiles) {
		this.projectiles = projectiles;
	}

	/**
	 * @return the blocksBroken
	 */
	public HashMap<Integer, Entry<Location, Integer>> getBlocksBroken() {
		return blocksBroken;
	}

	/**
	 * @param blocksBroken the blocksBroken to set
	 */
	public void setBlocksBroken(HashMap<Integer, Entry<Location, Integer>> blocksBroken) {
		this.blocksBroken = blocksBroken;
	}

	/**
	 * @param locations the locations to set
	 */
	public void setLocations(List<Location> locations) {
		this.locations = locations;
	}

	/**
	 * @return the velocities
	 */
	public List<Vector> getVelocities() {
		return velocities;
	}

	/**
	 * @return the death
	 */
	public int getDeath() {
		return death;
	}

	/**
	 * @param death the death to set
	 */
	public void setDeath(int death) {
		this.death = death;
	}

	/**
	 * @param velocities the velocities to set
	 */
	public void setVelocities(List<Vector> velocities) {
		this.velocities = velocities;
	}

	/**
	 * @return the tookDamage
	 */
	public HashMap<Integer, Damage> getTookDamage() {
		return tookDamage;
	}

	/**
	 * @param tookDamage the tookDamage to set
	 */
	public void setTookDamage(HashMap<Integer, Damage> tookDamage) {
		this.tookDamage = tookDamage;
	}

	/**
	 * @return the swung
	 */
	public List<Integer> getSwung() {
		return swung;
	}

	/**
	 * @param swung the swung to set
	 */
	public void setSwung(List<Integer> swung) {
		this.swung = swung;
	}

	/**
	 * @return the blocksPlaced
	 */
	public HashMap<Integer, HashMap<Location, SimpleItem>> getBlocksPlaced() {
		return blocksPlaced;
	}

	/**
	 * @param blocksPlaced the blocksPlaced to set
	 */
	public void setBlocksPlaced(HashMap<Integer, HashMap<Location, SimpleItem>> blocksPlaced) {
		this.blocksPlaced = blocksPlaced;
	}

	/**
	 * @return the itemsInHand
	 */
	public HashMap<Integer, SimpleItem> getItemsInHand() {
		return itemsInHand;
	}

	/**
	 * @param itemsInHand the itemInHand to set
	 */
	public void setItemInHand(HashMap<Integer, SimpleItem> itemInHand) {
		this.itemsInHand = itemInHand;
	}

	/**
	 * @return the helmets
	 */
	public HashMap<Integer, SimpleItem> getHelmets() {
		return helmets;
	}

	/**
	 * @param helmets the helmets to set
	 */
	public void setHelmets(HashMap<Integer, SimpleItem> helmets) {
		this.helmets = helmets;
	}

	/**
	 * @return the chestplates
	 */
	public HashMap<Integer, SimpleItem> getChestplates() {
		return chestplates;
	}

	/**
	 * @param chestplates the chestplates to set
	 */
	public void setChestplates(HashMap<Integer, SimpleItem> chestplates) {
		this.chestplates = chestplates;
	}

	/**
	 * @return the sneaks
	 */
	public HashMap<Integer, Boolean> getSneaks() {
		return sneaks;
	}

	/**
	 * @param sneaks the sneaks to set
	 */
	public void setSneaks(HashMap<Integer, Boolean> sneaks) {
		this.sneaks = sneaks;
	}

	/**
	 * @return the sprints
	 */
	public HashMap<Integer, Boolean> getSprints() {
		return sprints;
	}

	/**
	 * @param sprints the sprints to set
	 */
	public void setSprints(HashMap<Integer, Boolean> sprints) {
		this.sprints = sprints;
	}

	/**
	 * @return the leggings
	 */
	public HashMap<Integer, SimpleItem> getLeggings() {
		return leggings;
	}

	/**
	 * @param leggings the leggings to set
	 */
	public void setLeggings(HashMap<Integer, SimpleItem> leggings) {
		this.leggings = leggings;
	}

	/**
	 * @return the boots
	 */
	public HashMap<Integer, SimpleItem> getBoots() {
		return boots;
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

	/**
	 * @param boots the boots to set
	 */
	public void setBoots(HashMap<Integer, SimpleItem> boots) {
		this.boots = boots;
	}

	public static RecordedPlayer deserialize(Map<String, Object> serialized) {
		return new RecordedPlayer(serialized);
	}

	public static RecordedPlayer valueOf(Map<String, Object> serialized) {
		return new RecordedPlayer(serialized);
	}
}