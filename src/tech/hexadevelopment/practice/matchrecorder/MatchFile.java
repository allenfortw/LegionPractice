package tech.hexadevelopment.practice.matchrecorder;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

import tech.hexadevelopment.practice.LegionPractice;

public class MatchFile {

	private LegionPractice plugin;
	private File file;
	private YamlConfiguration config;
	private String name;
	
	public MatchFile(LegionPractice plugin, String name) {
		this.plugin = plugin;
		this.name = name;
		create();
	}
	
	public MatchFile(LegionPractice plugin, File file) {
		this.plugin = plugin;
		this.file = file;
		config = YamlConfiguration.loadConfiguration(file);
	}
	
	private void create() {
		File f = new File(plugin.getDataFolder(), "matches");
		f.mkdirs();
		file = new File(plugin.getDataFolder(), "matches" + File.separator + name + ".yml");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e1) {}
		}
		config = YamlConfiguration.loadConfiguration(file);
	}
	
	public YamlConfiguration getConfig() {
		return config;
	}
	
	public File getFile() {
		return file;
	}
	
	public void save() {
		try {
			getConfig().save(getFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reloadFile() {
		File f = new File(plugin.getDataFolder(), "matches");
		f.mkdirs();
		file = new File(plugin.getDataFolder(), "matches" + File.separator + name + ".yml");
		config = YamlConfiguration.loadConfiguration(file);
	}
}
