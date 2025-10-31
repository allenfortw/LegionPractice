package tech.hexadevelopment.practice.utils;

import org.bukkit.Material;

public class MaterialIdRemake {

	
	
	public static int getFakeId(Material material) {
		int i = 0;
		for(Material m : Material.values()) {
			i++;
			if(m == material) {
				return i;
			}
		}
		return 0;
	}
	
	public static Material fromFakeId(int id) {
		int i = 0;
		for(Material m : Material.values()) {
			i++;
			if(i == id) {
				return m;
			}
		}
		return Material.AIR;
	}
}
