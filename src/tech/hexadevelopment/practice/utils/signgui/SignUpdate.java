package tech.hexadevelopment.practice.utils.signgui;

import org.bukkit.Location;

public class SignUpdate {

	
	private Location location;
	private String[] text;
	
	
	public SignUpdate(Location location, String[] text) {
		this.location = location;
		this.text = text;
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * @return the text
	 */
	public String[] getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String[] text) {
		this.text = text;
	}
	
}
