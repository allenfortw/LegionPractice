package tech.hexadevelopment.practice.battlekit;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

import tech.hexadevelopment.practice.LegionPractice;

/**
 * kits.yml file handler.
 * @author Toppe5
 * @since 0.1
 */
public class KitFile {

	private LegionPractice plugin;
	private YamlConfiguration config;
	private File file;
	
	/**
	 * Try to create a new file if the file doesn't already exist.
	 * @param plugin LegionPractice plugin
	 */
	public KitFile(LegionPractice plugin) {
		this.plugin = plugin;
		create();
	}

	/**
	 * Try to create a new file if the file doesn't already exist.
	 * @param plugin LegionPractice plugin.
	 */
	private void create() {
		file = new File(plugin.getDataFolder(), "kits.yml");
		if (!file.exists()) {            
			plugin.saveResource("kits.yml", false);
		}
		config = YamlConfiguration.loadConfiguration(file);
	}

	/**
	 * Gets the file where BattleKits are used to store.
	 * @return the file where BattleKits are used to store.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Gets the config of the file.
	 * @return YamlConfiguration of the kits.yml file.
	 */
	public YamlConfiguration getConfig() {
		return config;
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
