package tech.hexadevelopment.practice.strikecheat;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import tech.hexadevelopment.practice.LegionPractice;

public class StrikeCheatManager {


	public static String CHANNEL = "SpracCheat";
	public static String BUNGEE_CHANNEL_ADD = "SpracCheatAdd";
	public static String BUNGEE_CHANNEL_REMOVE = "SpracCheatRemove";
	public static String VAPE_CHANNEL = "LOLIMAHCKER";
	public static long TIME_TO_SEND_FIRST = 40, TIME_TO_ADD = 10;
	private List<String> channels = new ArrayList<String>();
	private HashMap<UUID, String> received = new HashMap<UUID, String>();
	private HashMap<UUID, BukkitTask> tasks = new HashMap<UUID, BukkitTask>();
	private HashMap<UUID, ArrayList<StrikeCheatCallback>> callbacks = new HashMap<UUID, ArrayList<StrikeCheatCallback>>();

	private LegionPractice plugin;

	public StrikeCheatManager(LegionPractice plugin) {
		this.plugin = plugin;
		try{
			channels.add("BLC|M");
			channels.addAll(plugin.getConfig().getStringList("anticheat-channels"));
			Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
			Bukkit.getMessenger().registerIncomingPluginChannel(plugin, CHANNEL, new MessageHandler());
			Bukkit.getMessenger().registerIncomingPluginChannel(plugin, BUNGEE_CHANNEL_REMOVE, new BungeeMessageHandler());
			Bukkit.getMessenger().registerIncomingPluginChannel(plugin, BUNGEE_CHANNEL_ADD, new BungeeMessageHandler());

			Bukkit.getMessenger().registerIncomingPluginChannel(plugin, VAPE_CHANNEL, new VapeHandler());
			Bukkit.getMessenger().registerIncomingPluginChannel(plugin, "CB|INIT", new CheatBreakerHandler());
			Bukkit.getMessenger().registerIncomingPluginChannel(plugin, "CB-Binary", new CheatBreakerHandler());
			for(String c : channels) {
				Bukkit.getMessenger().registerIncomingPluginChannel(plugin, c, new CheatBreakerHandler());
			}
			Bukkit.getPluginManager().registerEvents(new Listener() {

				@EventHandler
				public void onQuit(PlayerQuitEvent e) {
					UUID uuid = e.getPlayer().getUniqueId();
					tasks.remove(uuid);
					received.remove(uuid);
					StrikeCheatCheck.firstTimers.remove(uuid);
				}
			}, plugin);
		}catch(Exception e) {}
	}


	public void requestCheck(Player p, StrikeCheatCallback callback) {
		UUID uuid = p.getUniqueId();
		if(FutureAPI.has(uuid)) {
			callback.isLegit(true);
			return;
		}
		p.sendMessage("§8 §8 §1 §3 §3 §7 §8 ");
		String message = "checkRequest";
		byte[] bytes = null;
		try {
			bytes = message.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		p.sendPluginMessage(plugin, CHANNEL, bytes);
		ArrayList<StrikeCheatCallback> c = callbacks.getOrDefault(uuid, new ArrayList<StrikeCheatCallback>());
		c.add(callback);
		callbacks.put(uuid, c);
		new BukkitRunnable() {

			@Override
			public void run() {
				if(!received.containsKey(uuid) && callbacks.containsKey(uuid)) {
					callbacks.remove(uuid);
					if(p != null) {
						if(FutureAPI.isBanned(uuid) || !FutureAPI.has(uuid)) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("strikecheat.download")));
							return;
						}
					}
				}
			}
		}.runTaskLater(plugin, TIME_TO_SEND_FIRST);
	}

	public class MessageHandler implements PluginMessageListener {

		@Override
		public void onPluginMessageReceived(String string, Player p, byte[] bytes){
			String str = new String(bytes, StandardCharsets.UTF_8);
			//Bukkit.getLogger().info(str);
			UUID uuid = p.getUniqueId();
			String s = received.getOrDefault(uuid, "");
			s += str;
			received.put(uuid, s);
			if(tasks.containsKey(uuid)) {
				tasks.get(uuid).cancel();
			}
			BukkitTask task = new BukkitRunnable() {

				@Override
				public void run() {
					String data = received.get(uuid);
					boolean legit = new StrikeCheatCheck(uuid, data).isLegit();

					Bukkit.getScheduler().runTask(LegionPractice.getInstance(), new Runnable() {

						@Override
						public void run() {
							if(callbacks.containsKey(uuid)) {
								for(StrikeCheatCallback callback : callbacks.get(uuid)) {
									callback.isLegit(legit);
								}
								callbacks.remove(uuid);
							}
						}
					});
					received.remove(uuid);
					tasks.remove(uuid);
				}
			}.runTaskLaterAsynchronously(plugin, TIME_TO_ADD);
			tasks.put(uuid, task);
		}
	}

	public class BungeeMessageHandler implements PluginMessageListener {

		@Override
		public void onPluginMessageReceived(String string, Player p, byte[] bytes){
			String str = new String(bytes, StandardCharsets.UTF_8);
			try {
				UUID uuid = UUID.fromString(str);
				if(uuid != null) {
					if(string.equals(BUNGEE_CHANNEL_ADD)) {
						FutureAPI.cheatbreakers.add(uuid);
					}
					else {
						FutureAPI.cheatbreakers.remove(uuid);
					}
				}
			}catch(Exception e) {}
		}
	}

	public class VapeHandler implements PluginMessageListener {

		@Override
		public void onPluginMessageReceived(String string, Player p, byte[] bytes){
			UUID uuid = p.getUniqueId();
			if(callbacks.containsKey(uuid)) {
				for(StrikeCheatCallback callback : callbacks.get(uuid)) {
					callback.isLegit(false);
				}
				callbacks.remove(uuid);
				received.remove(uuid);
				tasks.remove(uuid);
			}
		}
	}

	public class CheatBreakerHandler implements PluginMessageListener {

		@Override
		public void onPluginMessageReceived(String channel, Player p, byte[] bytes){
			if (channel.equals("CB|INIT") || channel.equals("CB-Binary") || channels.contains(channel)) {
				FutureAPI.cheatbreakers.add(p.getUniqueId());
			}
		}
	}

	public HashMap<UUID, ArrayList<StrikeCheatCallback>> getCallbacks() {
		return callbacks;
	}
}
