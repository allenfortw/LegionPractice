package tech.hexadevelopment.practice.utils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;


@SerializableAs("Location")
public class SerializableLocation implements Serializable, ConfigurationSerializable {

	private static long serialVersionUID = -3174227815222499224L;
	private double x, y, z;
	private float yaw, pitch;
	private String world;

	/**
	 * Creates a SerializableLocation using the values of the given location.
	 * 
	 * @param l The location to use for values.
	 */
	public SerializableLocation(Location l) {
		this.x = l.getX();
		this.y = l.getY();
		this.z = l.getZ();
		this.yaw = l.getYaw();
		this.pitch = l.getPitch();
		if(l.getWorld() != null){
			this.world =  l.getWorld().getName();
		}
	}

	/**
	 * Creates a SerializableLocation using the given values
	 */
	public SerializableLocation(double x, double y, double z, float yaw, float pitch, String world) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
		this.world = world;
	}

	/**
	 * Creates a SerializableLocation using the given values
	 */
	public SerializableLocation(double x, double y, double z, float yaw, float pitch) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	/**
	 * Creates a SerializableLocation using the given values
	 */
	public SerializableLocation(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Creates a SerializableLocation using the given values
	 */
	public SerializableLocation(double x, double y, double z, float yaw, float pitch, World world) {
		this(x, y, z, yaw, pitch, world.getName());
	}

	/**
	 * Creates a SerializableLocation using the given values
	 */
	public SerializableLocation(double x, double y, double z, World world) {
		this(x, y, z, 0, 0, world.getName());
	}

	/**
	 * Creates a SerializableLocation using the given values
	 */
	public SerializableLocation(double x, double y, double z, String world) {
		this(x, y, z, 0, 0, world);
	}

	/**
	 * Converts this SerializableLocation to a Bukkit Location.
	 * 
	 * @return The Location, or null if the world couldn't be found.
	 */
	public Location toLocation() {
		World w = Bukkit.getServer().getWorld(world);
		return new Location(w, x, y, z, yaw, pitch);
	}

	/**
	 * Creates a SerializableLocation from a string.
	 * 
	 * @param s The string to get the values from.
	 * @return The parsed SerializableLocation
	 */
	public static SerializableLocation fromString(String s) {
		String[] ss = s.split(", ");
		double x = Double.parseDouble(ss[0]);
		double y = Double.parseDouble(ss[1]);
		double z = Double.parseDouble(ss[2]);
		if(ss.length > 5) {
			float yaw = Float.parseFloat(ss[3]);
			float pitch = Float.parseFloat(ss[4]);
			return new SerializableLocation(x, y, z, yaw, pitch, ss[5]);
		}
		else if(ss.length > 4) {
			float yaw = Float.parseFloat(ss[3]);
			float pitch = Float.parseFloat(ss[4]);
			return new SerializableLocation(x, y, z, yaw, pitch);
		}
		return new SerializableLocation(x, y, z);
	}

	/**
	 * @return A string containing the x, y, z, yaw, pitch values and the world UID.
	 */
	@Override
	public String toString() {
		if(world == null && yaw == 0 && pitch == 0) {
			return x + ", " + y + ", " + z;
		}
		if(world == null) {
			return x + ", " + y + ", " + z + ", " + yaw + ", " + pitch;
		}
		return x + ", " + y + ", " + z + ", " + yaw + ", " + pitch + ", " + world;
	}

	/**
	 * @return A human-readable string representation of this location. Contains the x, y and z values rounded to two
	 *         decimals. If you want a string with the world too, use {@link #toReadableString(Location)}.
	 */
	public String toReadableString() {
		return round(x, 2) + ", " + round(y, 2) + ", " + round(z, 2) + " @ " + world;
	}

	/**
	 * Serializes a SerializedLocation to configuration. It is not recommended to use this manually, as it is intended
	 * for the Bukkit configuration serialization system.
	 * 
	 * @return The serialized map.
	 */
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> location = new HashMap<String, Object>();
		location.put("x", x);
		location.put("y", y);
		location.put("z", z);
		location.put("yaw", yaw);
		location.put("pitch", pitch);
		if(world != null) {
			location.put("world", world);
		}
		return location;
	}

	/**
	 * Deserializes a SerializedLocation from configuration. It is not recommended to use this manually, as it is
	 * intended for the Bukkit configuration serialization system.
	 * 
	 * @param location The map containing the values.
	 */
	public SerializableLocation(Map<String, Object> location) {
		this.x = parseDouble(location.get("x"));
		this.y = parseDouble(location.get("y"));
		this.z = parseDouble(location.get("z"));
		this.yaw = parseFloat(location.get("yaw"));
		this.pitch = parseFloat(location.get("pitch"));
		if(location.containsKey("world")) {
			this.world = (String) location.get("world");
		}
	}

	/**
	 * Deserializes a SerializedLocation from configuration. It is not recommended to use this manually, as it is
	 * intended for the Bukkit configuration serialization system.
	 * 
	 * @param location The map containing the values.
	 */
	public SerializableLocation valueOf(Map<String, Object> location) {
		return new SerializableLocation(location);
	}

	/**
	 * Deserializes a SerializedLocation from configuration. It is not recommended to use this manually, as it is
	 * intended for the Bukkit configuration serialization system.
	 * 
	 * @param location The map containing the values.
	 */
	public SerializableLocation deserialize(Map<String, Object> location) {
		return new SerializableLocation(location);
	}

	/**
	 * Rounds a double to the given amount of decimal places.
	 * @return The rounded double.
	 */
	private static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	/**
	 * Parses a double from an object.
	 */
	private double parseDouble(Object o) {
		if (o instanceof Double) return (Double) o;
		else return Double.parseDouble(o.toString());
	}

	/**
	 * Parses a float from an object.
	 */
	private float parseFloat(Object o) {
		if (o instanceof Float) return (Float) o;
		else return Float.parseFloat(o.toString());
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @return the z
	 */
	public double getZ() {
		return z;
	}

	/**
	 * @return the yaw
	 */
	public float getYaw() {
		return yaw;
	}

	/**
	 * @return the pitch
	 */
	public float getPitch() {
		return pitch;
	}

	/**
	 * @return the world
	 */
	public String getWorld() {
		return world;
	}

	public void setWorld(String world) {
		this.world = world;
	}
}
