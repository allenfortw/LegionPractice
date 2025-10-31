package tech.hexadevelopment.practice.packetlistener.channel.older;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import tech.hexadevelopment.practice.packetlistener.channel.CHandlerCommon;
import net.minecraft.util.io.netty.channel.Channel;
import tech.hexadevelopment.practice.LegionPractice;

public class CManagerOlder{

	private static boolean viaVersionLogged;
	private static CManagerOlder channelManager;
	private LegionPractice plugin;

	public CManagerOlder(LegionPractice plugin) {
		this.plugin = plugin;
	}

	public static void register(LegionPractice plugin) {
		channelManager = new CManagerOlder(plugin);
		Bukkit.getPluginManager().registerEvents(channelManager.new BukkitListener(), plugin);
	}

	public void injectPlayer(Player p) {
		try {
			Channel channel = getChannel(p);
			Object c = channel.pipeline().get(CHandlerCommon.HANDLER_NAME);
			if(c == null) {
				channel.pipeline().addBefore("packet_handler", CHandlerCommon.HANDLER_NAME, new CHandlerOlder(p));
			}
		}catch(Exception e) {
			if(!viaVersionLogged && Bukkit.getPluginManager().getPlugin("ViaVersion") != null) {
				Bukkit.getLogger().warning("Some LegionPractice features might not work due to ViaVersion!");
				viaVersionLogged = true;
			}
		}
	}

	public void unregisterPlayer(Player p, boolean disabling) {
		if(disabling) return;
		Channel channel = getChannel(p);
		Object c = channel.pipeline().get(CHandlerCommon.HANDLER_NAME);
		if(c != null) {
			channel.eventLoop().execute(new Runnable() {

				@Override
				public void run() {

					channel.pipeline().remove(CHandlerCommon.HANDLER_NAME);
				}
			});
		}
	}

	public Channel getChannel(Object o) throws ClassCastException {
		return (Channel) plugin.getNMSAccessProvider().getAccess().getChannel((Player) o);
	}

	public void registerAllPlayers() {
		for(Player p : Bukkit.getOnlinePlayers()) {
			injectPlayer(p);
		}
	}

	public void unregisterAllPlayers() {
		for(Player p : Bukkit.getOnlinePlayers()) {
			unregisterPlayer(p, false);
		}
	}

	public static CManagerOlder getChannelManager() {
		return channelManager;
	}

	class BukkitListener implements Listener {

		@EventHandler
		public void onLogin(PlayerJoinEvent e) {
			getChannelManager().injectPlayer(e.getPlayer());
		}
	}
}
