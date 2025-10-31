package tech.hexadevelopment.practice.utils.signgui;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;

public class SignGUI {

	private static HashMap<UUID, Location> editors = new HashMap<UUID, Location>();
	private static HashMap<UUID, SignFinishCallback> callbacks = new HashMap<UUID, SignFinishCallback>();


	@SuppressWarnings("deprecation")
	public static void openSignEditor(Player p, String[] text, SignFinishCallback callback){
		Location loc = new Location(p.getWorld(), p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
		if(text.length == 0) text = new String[] {"", "", "", ""};
		else if(text.length == 1) text = new String[] {text[0], "", "", ""};
		else if(text.length == 2) text = new String[] {text[0], text[1], "", ""};
		else if(text.length == 3) text = new String[] {text[0], text[1], text[2], ""};
		else if(text.length > 4) text = new String[] {text[0], text[1], text[2], text[3]};
		p.sendBlockChange(loc, Material.SIGN_POST, (byte) 0);
		p.sendSignChange(loc, text);
		LegionPractice.getInstance().getNMSAccessProvider().getAccess().openSignEditor(p, p.getLocation());
		p.sendBlockChange(loc, loc.getBlock().getType(), loc.getBlock().getData());
		editors.put(p.getUniqueId(), loc);
		callbacks.put(p.getUniqueId(), callback);
	}

	public static boolean onFinish(Player p, SignUpdate update) {
		if(p != null) {
			UUID uuid = p.getUniqueId();
			if(editors.containsKey(uuid)) {
				Location loc = editors.get(uuid);
				if(loc.getBlockX() == update.getLocation().getBlockX() && loc.getBlockY() == update.getLocation().getBlockY()
						&& loc.getBlockZ() == update.getLocation().getBlockZ() && callbacks.containsKey(uuid)) {
					Bukkit.getScheduler().runTask(LegionPractice.getInstance(), new Runnable() {

						@Override
						public void run() {
							if(callbacks.containsKey(uuid) && update.getText() != null) {
								callbacks.get(uuid).onFinish(update.getText());
								callbacks.remove(uuid);
							}
						}
					});
					return true;
				}
				editors.remove(uuid);
			}
		}
		return false;
	}

	public static HashMap<UUID, Location> getEditors() {
		return editors;
	}
}