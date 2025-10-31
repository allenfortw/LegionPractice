package tech.hexadevelopment.practice.utils;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

import tech.hexadevelopment.practice.LegionPractice;

public class PremadeMaps {


	private static String[] maps = new String[]{"desert", "hungergames", "ice_plains", "savanna", "village"};


	public static void saveResources(LegionPractice plugin) {
		try {
			File weFolder = new File(plugin.getDataFolder().getParentFile(), "WorldEdit" + File.separator + "schematics");
			for(String map : maps) {
				plugin.saveResource("pre-made maps/" + map + ".schematic", true);
				if(weFolder.exists()) {
					try {
						Files.copy(new File(plugin.getDataFolder(), "pre-made maps" + File.separator + map + ".schematic"), weFolder);
					} catch (IOException e) {}
				}
			}
		}catch (Exception e) {}
	}

}
