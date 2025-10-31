package tech.hexadevelopment.practice.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;

public class ClickableMessage {

	public static void sendMessage(Player p, String msg, String cmd) {
		String a = " [\"\",{\"text\":\"" + msg + " \",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + cmd + "\"}}]";
		dispatchCommand(p, a);
	}

	public static void sendMultipleMessages(Player p, LinkedHashMap<String, String> args) {
		String s = "[\"\"";
		for(Entry<String, String> e : args.entrySet()) {
			String a = ",{\"text\":\"" + e.getKey() + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + e.getValue() + "\"}}";
			s += a;
		}
		s += "]";
		dispatchCommand(p, s);
	}

	public static void sendMultipleMessages(Player p, LinkedHashMap<String, String> args, String prefix, String separator) {
		String s = "[\"\"";
		String sep = "";
		s += ",{\"text\":\"" + prefix + " \",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + "/clickedchatwithoutaction" + "\"}}";
		for(Entry<String, String> e : args.entrySet()) {
			String a = ",{\"text\":\"" + sep + e.getKey() + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + e.getValue() + "\"}}";
			s += a;
			sep = separator;
		}
		s += "]";
		dispatchCommand(p, s);
	}

	public static void sendMultipleMessages(Player p, HashMap<String, String> args) {
		String s = "[\"\"";
		for(Entry<String, String> e : args.entrySet()) {
			String a = ",{\"text\":\"" + e.getKey() + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + e.getValue() + "\"}}";
			s += a;
		}
		s += "]";
		dispatchCommand(p, s);
	}

	public static void sendMultipleMessages(Player p, HashMap<String, String> args, String prefix, String separator) {
		String s = "[\"\"";
		String sep = "";
		s += ",{\"text\":\"" + prefix + " \",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + "/clickedchatwithoutaction" + "\"}}";
		for(Entry<String, String> e : args.entrySet()) {
			String a = ",{\"text\":\"" + sep + e.getKey() + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + e.getValue() + "\"}}";
			s += a;
			sep = separator;
		}
		s += "]";
		dispatchCommand(p, s);
	}

	public static void dispatchCommand(Player p, String command) {
		LegionPractice.getInstance().getConfig().set("a", command);
		LegionPractice.getInstance().saveConfig();
		if(!Bukkit.isPrimaryThread()) {
			Bukkit.getScheduler().runTask(LegionPractice.getInstance(), new Runnable() {

				@Override
				public void run() {
					if(p != null && Bukkit.getPlayer(p.getUniqueId()) != null) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + p.getName() + " " + command);
					}
				}
			});
		}
		else if(p != null && Bukkit.getPlayer(p.getUniqueId()) != null) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + p.getName() + " " + command);
		}
	}
}