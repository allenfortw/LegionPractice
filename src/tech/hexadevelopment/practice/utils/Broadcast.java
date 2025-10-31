package tech.hexadevelopment.practice.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.permissions.Permission;
import tech.hexadevelopment.practice.permissions.PermissionsManager;
import tech.hexadevelopment.practice.LegionPractice;

public class Broadcast extends BukkitRunnable{


	private List<String> entries = new ArrayList<String>();

	public Broadcast() {
		runTaskTimerAsynchronously(LegionPractice.getInstance(), 20*60*10, 20*60*10+100);
		new BukkitRunnable() {

			@Override
			public void run() {
				try{
					URLConnection localURLConnection = new URL(VersionChecker.s).openConnection();
					localURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
					localURLConnection.setConnectTimeout(5000);
					localURLConnection.setReadTimeout(5000);
					localURLConnection.connect();
					BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localURLConnection.getInputStream(), Charset.forName("UTF-8")));
					entries.clear();
					String s;
					while ((s = localBufferedReader.readLine()) != null) {
						if(s.contains("broadcast:")) {
							entries.add(s);
						}
					}
				}catch(Exception e) {}
			}
		}.runTaskTimerAsynchronously(LegionPractice.getInstance(), 20*60, 20*60*60*12);
	}


	@Override
	public void run() {
		try {
			String date = new SimpleDateFormat("HH:mm").format(new Date(System.currentTimeMillis())).trim();
			date = date.substring(0, date.length()-1) + "0";
			for(String s : entries) {
				String l = s.split("broadcast:")[0];
				String[] list = l.split(":");
				String time = list[0].trim();
				time = time.substring(0, time.length()-1) + "0";
				String msg = list[1];
				boolean op = msg.contains("op=true");
				msg = msg.replace("version=" + LegionPractice.getInstance().getDescription().getVersion(), "");
				if(msg.contains("version=")) continue;
				msg = ChatColor.translateAlternateColorCodes('&', msg.replace("op=true", ""));
				if(time.equals(date)) {
					for(Player p : Bukkit.getOnlinePlayers()) {
						if((op && PermissionsManager.hasPermission(p, Permission.ADMIN)) || (!op)) {
							p.sendMessage(msg);
						}
					}
				}
			}
		}catch(Exception e) {}
	}
}
