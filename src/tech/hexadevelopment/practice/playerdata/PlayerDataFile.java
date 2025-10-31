package tech.hexadevelopment.practice.playerdata;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.npc.CitizensNPC;

public class PlayerDataFile {

	private LegionPractice plugin;
	private YamlConfiguration config;
	private File file;
	private String uuid;

	public PlayerDataFile(LegionPractice plugin, String uuid) {
		this.plugin = plugin;
		this.uuid = uuid;
		load(true);
	}

	public PlayerDataFile(LegionPractice plugin, String uuid, boolean create) {
		this.plugin = plugin;	
		this.uuid = uuid;
		load(create);
	}

	public void load(boolean createFile) {
		boolean created = false;
		File f = new File(plugin.getDataFolder(), "playerdata");
		f.mkdirs();
		file = new File(plugin.getDataFolder(), "playerdata" + File.separator + uuid + ".yml");
		if(!file.exists() && createFile) {
			for(CitizensNPC npc : CitizensNPC.npcs) {
				if(uuid.equals(npc.getNPC().getUniqueId().toString())) {
					//is a citizens npc
					return;
				}
			}
			Bukkit.getLogger().info("LegionPractice >> Creating a new playerdata file. UUID: " + uuid);
			try {
				file.createNewFile();
				created = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(file.exists()) {
			config = YamlConfiguration.loadConfiguration(file);
			Player p = Bukkit.getPlayer(UUID.fromString(uuid));
			if(p != null) {
				config.set("username", p.getName());
				if(!created) {
					save();
				}
			}
			else if(!Bukkit.isPrimaryThread()) {
				
			}
			if(created) {
				config.set("scoreboard-disabled", plugin.getConfig().getBoolean("player-settings.default-values.scoreboard-disabled"));
				config.set("duel-requests-disabled", plugin.getConfig().getBoolean("player-settings.default-values.duel-requests-disabled"));
				boolean adminSB = plugin.getConfig().getBoolean("player-settings.default-values.admin-scoreboard");
				if(adminSB){
					config.set("admin-scoreboard", adminSB);
				}
				config.set("hide-other-players", plugin.getConfig().getBoolean("player-settings.default-values.hide-players"));
				save();
			}
		}
	}


	public File getFile() {
		return file;
	}

	public YamlConfiguration getConfig() {
		return config;
	}

	public void save() {
		try {
			getConfig().save(getFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
