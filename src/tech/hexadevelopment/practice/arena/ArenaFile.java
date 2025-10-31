package tech.hexadevelopment.practice.arena;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

import tech.hexadevelopment.practice.LegionPractice;

/**
 * arenas.yml file handler.
 * @author Toppe5
 * @since 0.1
 */
public class ArenaFile {

	private LegionPractice plugin;
	private File file;
	private YamlConfiguration config;
	
	/**
	 * Try to create a new file if the file doesn't already exist.
	 * @param plugin LegionPractice plugin.
	 */
	public ArenaFile(LegionPractice plugin) {
		this.plugin = plugin;
		create();
	}
	
	/**
	 * Try to create a new file if the file doesn't already exist.
	 */
	private void create() {
		//Creating a file
		file = new File(plugin.getDataFolder(), File.separator + "arenas.yml");
		//check if the file exists
		if(!file.exists()) {
			//trying to create a new file if it doesn't exist
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}
		}
		//loading YamlConfiguration for the while
		config = YamlConfiguration.loadConfiguration(file);
	}
	
	/**
	 * Gets the config of the file.
	 * @return YamlConfiguration of the arena file.
	 */
	public YamlConfiguration getConfig() {
		return config;
	}
	
	/**
	 * Gets the file where arenas are used to store.
	 * @return the file where arenas are used to store.
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * Save the file.
	 */
	public void save() {
		try {
			getConfig().save(getFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
