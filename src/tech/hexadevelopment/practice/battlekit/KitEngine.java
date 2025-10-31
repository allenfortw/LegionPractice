package tech.hexadevelopment.practice.battlekit;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.utils.ItemStackUtil;
import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.stats.Callback;

public class KitEngine {

	private static String KIT_FILE_NAME = "kitdatabase";
	private static int lastIndex;
	private static String lastName;

	private List<BattleKit> kits = new ArrayList<BattleKit>();
	private static String[] twoPosKits = new String[] {"skywars", "sumo", "bridge", "bedwars", "sky"};

	public void load(Callback cb) {
		new BukkitRunnable() {

			@Override
			public void run() {
				try {
					YamlConfiguration conf = YamlConfiguration.loadConfiguration(new InputStreamReader(LegionPractice.getInstance().getResource(KIT_FILE_NAME + ".dat"), StandardCharsets.UTF_8));
					for(Object o : conf.getList("kits")) {
						if(o instanceof BattleKit) {
							kits.add((BattleKit) o);
						}
					}
					new BukkitRunnable() {

						@Override
						public void run() {
							try {
								cb.onSuccess(kits.size());
							}catch(Exception e) {}
						}
					}.runTask(LegionPractice.getInstance());
				}catch(Exception e) {}
			}
		}.runTaskAsynchronously(LegionPractice.getInstance());
	}

	public BattleKit findKit(String name) {
		try {
			if(lastName != null && !name.equals(lastName)) {
				lastIndex = 0;
			}
			lastName = name;
			List<BattleKit> selectFrom = new ArrayList<BattleKit>();
			for(BattleKit kit : kits) {
				if(kit.getName().equalsIgnoreCase(name)) {
					kit.setElo(name.contains("ranked") || name.contains("elo"));
					if(!selectFrom.contains(kit)) {
						selectFrom.add(kit);
					}
				}
				String kitName = kit.getName().replace("1v1elo", "").replace("elo", "").replace("ranked", "");
				if(kitName.contains(name) || name.contains(kitName)) {
					kit.setElo(name.contains("ranked") || name.contains("elo"));
					if(!selectFrom.contains(kit)) {
						selectFrom.add(kit);
					}
				}
			}
			for(BattleKit kit : kits) {
				name = name.replace("1v1elo", "").replace("elo", "").replace("ranked", "");
				String kitName = kit.getName().replace("1v1elo", "").replace("elo", "").replace("ranked", "");
				if(kitName.equalsIgnoreCase(name)) {
					kit.setElo(name.contains("ranked") || name.contains("elo"));
					if(!selectFrom.contains(kit)) {
						selectFrom.add(kit);
					}
				}
			}
			for(BattleKit kit : kits) {
				String kitName = kit.getName().replace("1v1elo", "").replace("elo", "").replace("ranked", "");
				if(kitName.contains(name) || name.contains(kitName)) {
					kit.setElo(name.contains("ranked") || name.contains("elo"));
					if(!selectFrom.contains(kit)) {
						selectFrom.add(kit);
					}
				}
			}
			if(selectFrom.size() > 1) {
				if(selectFrom.size() <= lastIndex) {
					lastIndex = 0;
				}
				BattleKit result = selectFrom.get(lastIndex);
				lastIndex++;
				return result;
			}
			if(!selectFrom.isEmpty()) {
				return selectFrom.get(0);
			}
			return null;
		}catch(Exception e) {
			return null;
		}
	}

	public BattleKit apply(String name, BattleKit template) {
		try {
			String iconName = name.length() > 1 ? name.substring(0, 1).toUpperCase() + name.substring(1) : name.toUpperCase();
			template.setIcon(ItemStackUtil.createItem(template.getIcon() != null && template.getIcon().getType() != Material.AIR ? template.getIcon().getType() : Material.DIAMOND_SWORD, ChatColor.BLUE + "" + ChatColor.BOLD + iconName));
			if(!template.isElo()) {
				template.setMergedEditor(null);
			}
			if(template.isEditable()) {
				template.setMergedEditor(null);	
			}
			Map<String, Object> serialized = template.serialize();
			serialized.put("name", name);
			return new BattleKit(serialized);
		}catch(Exception e) {
			return template;
		}
	}

	public static boolean isTwoPosKit(String name) {
		for(String s : twoPosKits) {
			if(name.toLowerCase().contains(s.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
}
