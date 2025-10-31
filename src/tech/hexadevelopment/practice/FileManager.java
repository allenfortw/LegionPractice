package tech.hexadevelopment.practice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import tech.hexadevelopment.practice.utils.world.BuildWorldDelete;
import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.arena.ArenaFile;
import tech.hexadevelopment.practice.battlekit.BattleKit;
import tech.hexadevelopment.practice.battlekit.KitFile;
import tech.hexadevelopment.practice.language.LanguageItem;
import tech.hexadevelopment.practice.spawnitems.SpawnItem;

public class FileManager {

	private LegionPractice plugin;
	private KitFile kitFile;
	private ArenaFile arenaFile;
	private File messagesFile;
	private YamlConfiguration messagesConfig;
	private File languageItemsFile;
	private YamlConfiguration langItems;
	private File fightsFile;
	private YamlConfiguration fightsConfig;
	private File npcFile;
	private YamlConfiguration npcConfig;
	private File spawnItemsFile;
	private YamlConfiguration spawnItemsConfig;
	private File dataFile;
	private YamlConfiguration dataConfig;
	public boolean rollbackOnCrash;

	public FileManager(LegionPractice plugin) {
		this.plugin = plugin;
	}

	public void reloadMessages() {
		createMessagesFile();
		plugin.messages.clear();
	}

	public void createMessagesFile() {
		messagesFile = new File(plugin.getDataFolder(), "messages.yml");
		if (!messagesFile.exists()) {            
			plugin.saveResource("messages.yml", false);
		}
		messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
	}

	public void createNPCFile() {
		npcFile = new File(plugin.getDataFolder(), "npc data.yml");
		if (!npcFile.exists()) {            
			try {
				npcFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		npcConfig = YamlConfiguration.loadConfiguration(npcFile);
	}

	public void createDataFile() {
		dataFile = new File(plugin.getDataFolder(), "data.dat");
		if (!dataFile.exists()) {            
			plugin.saveResource("data.dat", false);
		}
		dataConfig = YamlConfiguration.loadConfiguration(dataFile);
	}

	public void saveNPCFile() {
		if(npcConfig != null && npcFile != null) { 
			try {
				npcConfig.save(npcFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void removeNPCFile() {
		npcFile = new File(plugin.getDataFolder(), "npc data.yml");
		if(npcFile.exists()) {
			npcFile.delete();
		}
	}

	public void createFightFile() {
		fightsFile = new File(plugin.getDataFolder(), "fights.yml");
		if (!fightsFile.exists()) {            
			plugin.saveResource("fights.yml", false);
		}
		fightsConfig = YamlConfiguration.loadConfiguration(fightsFile);
	}

	public void saveKits() {
		if(plugin.kits.size() > 0 || LegionPractice.hasArenasAndKits) {
			kitFile.getConfig().set("kits", plugin.kits);
			kitFile.save();
		}
	}


	public void saveRollbackKit(Arena arena, BattleKit kit) {
		if(arena.getName().contains(":") || arena.getName().toLowerCase().contains("spleef")) return;
		HashSet<Material> blocks = kit.getBlocks();
		if(blocks.isEmpty()) return;
		String info = arena.getName() + ":" + blocks.toString().replace("[", "").replace("]", "");
		List<String> list = arenaFile.getConfig().getStringList("rollback-kits");
		list.add(info);
		if(list.isEmpty()) {
			arenaFile.getConfig().set("rollback-kits", null);
		}
		else {
			arenaFile.getConfig().set("rollback-kits", list);
		}
		arenaFile.save();
	}

	public void removeRollbackKit(Arena arena) {
		String info = arena.getName() + ":";
		List<String> list = new ArrayList<String>(arenaFile.getConfig().getStringList("rollback-kits"));
		Iterator<String> i = list.iterator();
		while (i.hasNext()) {
			String s = i.next();
			if(s.contains(info)) {
				i.remove();
			}
		}
		if(list.isEmpty()) {
			arenaFile.getConfig().set("rollback-kits", null);
		}
		else {
			arenaFile.getConfig().set("rollback-kits", list);
		}
		arenaFile.save();
	}

	private HashMap<Arena, HashSet<Material>> getRollbackMaterials() {
		HashMap<Arena, HashSet<Material>> materials = new HashMap<Arena, HashSet<Material>>();
		if(arenaFile.getConfig().get("rollback-kits") != null) {
			for(String s : arenaFile.getConfig().getStringList("rollback-kits")) {
				try{
					HashSet<Material> mats = new HashSet<Material>();
					String arena = s.split(":")[0];
					for(String mat : s.split(":")[1].split(",")) {
						try{
							Material m = Material.getMaterial(mat);
							mats.add(m);
						}catch(Exception e){}
					}
					for(Arena ar : plugin.arenas) {
						if(ar.getName().equals(arena)) {
							materials.put(ar, mats);
							continue;
						}
					}
				}catch(Exception e) {}
			}
		}
		return materials;
	}

	public void loadArenas() {
		makeArenaFileIfDoesNotExist();
		plugin.arenas.clear();
		if(arenaFile.getConfig().getList("arenas") != null) {
			for(Object o : arenaFile.getConfig().getList("arenas")) {
				if(o != null && o instanceof Arena) {
					Arena a = (Arena) o;
					if(!a.getName().contains(":")) {
						plugin.arenas.add(a);
					}
				}
			}
		}
		rollbackOnCrash = plugin.getConfig().getBoolean("rollback-on-enable-if-crashed");
		if(rollbackOnCrash) {
			for(Entry<Arena, HashSet<Material>> s : getRollbackMaterials().entrySet()) {
				if(s.getValue() != null && !s.getValue().isEmpty()) {
					s.getKey().rollbackArena(s.getValue(), false, true);
				}
				else {
					removeRollbackKit(s.getKey());
				}
			}
		}
	}

	public void loadSpawnItems() {
		plugin.spawnItems.clear();
		spawnItemsFile = new File(plugin.getDataFolder(), "spawnitems.yml");
		if (!spawnItemsFile.exists()) {            
			plugin.saveResource("spawnitems.yml", false);
		}
		spawnItemsConfig = YamlConfiguration.loadConfiguration(spawnItemsFile);
		if(spawnItemsConfig.getList("spawn-items") != null) {
			for(Object o : spawnItemsConfig.getList("spawn-items")) {
				if(o instanceof SpawnItem && !plugin.spawnItems.contains((SpawnItem) o)) {
					plugin.spawnItems.add((SpawnItem) o);
				}
			}
		}
	}

	public void saveSpawnItems() {
		getSpawnItemsConfig().set("spawn-items", plugin.spawnItems);
		try {
			getSpawnItemsConfig().save(getSpawnItemsFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveArenas() {
		if(plugin.arenas.size() > 0 || LegionPractice.hasArenasAndKits) {
			List<Arena> ars = new ArrayList<Arena>();
			for(Arena a : plugin.arenas) {
				if(a.getCenter() == null && a.getLoc1() != null && a.getLoc2() != null) {
					a.setCenter(new Location(a.getLoc1().getWorld(),
							(a.getLoc1().getBlockX()+a.getLoc2().getBlockX())/2, (a.getLoc1().getBlockY()+a.getLoc2().getBlockY()+5)/2, (a.getLoc1().getBlockZ()+a.getLoc2().getBlockZ())/2));
				}
				if(a.getLoc1() != null && a.getLoc2() != null && a.getCenter().getWorld() != null
						&& !BuildWorldDelete.worldsCreated.contains(a.getCenter().getWorld().getName())
						&& !BuildWorldDelete.worldsCreated.contains(a.getLoc1().getWorld().getName())
						&& !BuildWorldDelete.worldsCreated.contains(a.getLoc2().getWorld().getName())
						&& !a.getName().contains(":")) {
					ars.add(a);
				}
			}
			arenaFile.getConfig().set("arenas", ars);
			arenaFile.save();
		}
	}

	public void makeKitsFileIfDoesNotExist() {
		kitFile = new KitFile(plugin);
	}

	public void loadLanguageItems() {
		languageItemsFile = new File(plugin.getDataFolder(), "languageitems.yml");
		if (!languageItemsFile.exists()) {            
			plugin.saveResource("languageitems.yml", false);
		}
		langItems = YamlConfiguration.loadConfiguration(languageItemsFile);
		plugin.languageItems.clear();
		if(langItems.getList("language-items") != null) {
			for(Object o : langItems.getList("language-items")) {
				if(o instanceof LanguageItem) {
					LanguageItem li = (LanguageItem) o;
					boolean dontAdd = false;
					for(LanguageItem l : plugin.languageItems) {
						if(l.getItem() != null && li.getItem() != null && l.getItem().equals(li.getItem())
								&& l.getLanguage() != null && li.getLanguage() != null && li.getLanguage().equals(l.getLanguage())
								&& l.getSlot() == li.getSlot()) {
							dontAdd = true;
							break;
						}
					}
					if(!dontAdd) {
						plugin.languageItems.add(li);
					}
				}
			}
		}
	}

	public void saveLanguageItems() {
		if(plugin.languageItems.size() > 0) {
			langItems.set("language-items", plugin.languageItems.toArray());
			try {
				langItems.save(languageItemsFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void addFight(BattleKit kit) {
		if(LegionPractice.performanceMode) return;
		if(LegionPractice.getInstance().kits.contains(kit)) {
			if(getFightsConfig().get(kit.getName()) == null) {
				getFightsConfig().set(kit.getName(), 1);
			}
			else {
				int fights = getFightsConfig().getInt(kit.getName());
				getFightsConfig().set(kit.getName(), fights+1);
			}
			try {
				getFightsConfig().save(getFightsFile());
			} catch (IOException e) {}
		}
	}

	public void loadKits() {
		makeKitsFileIfDoesNotExist();
		plugin.kits.clear();
		if(kitFile.getConfig().getList("kits") != null) {
			for(Object o : kitFile.getConfig().getList("kits")) {
				if(o instanceof BattleKit){
					plugin.kits.add((BattleKit) o);
				}
			}
		}
	}

	public void makeArenaFileIfDoesNotExist() {
		arenaFile = new ArenaFile(plugin);
	}

	public ArenaFile getArenaFile() {
		return arenaFile;
	}

	public KitFile getKitFile() {
		return kitFile;
	}

	public YamlConfiguration getMessagesConfig() {
		return messagesConfig;
	}

	public File getMessagesFile() {
		return messagesFile;
	}

	public YamlConfiguration getFightsConfig() {
		return fightsConfig;
	}

	public File getFightsFile() {
		return fightsFile;
	}

	public YamlConfiguration getLangItems() {
		return langItems;
	}

	public File getLanguageItemsFile() {
		return languageItemsFile;
	}

	public YamlConfiguration getDataConfig() {
		return dataConfig;
	}

	public File getDataFile() {
		return dataFile;
	}

	public void saveDataFile() {
		try {
			dataConfig.save(dataFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public YamlConfiguration getSpawnItemsConfig() {
		return spawnItemsConfig;
	}

	public File getSpawnItemsFile() {
		return spawnItemsFile;
	}

	public YamlConfiguration getNPCConfig() {
		return npcConfig;
	}

	public File getNPCFile() {
		return npcFile;
	}
}
