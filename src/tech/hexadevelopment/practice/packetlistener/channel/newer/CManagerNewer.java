package tech.hexadevelopment.practice.packetlistener.channel.newer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import tech.hexadevelopment.practice.packetlistener.channel.CHandlerCommon;
import io.netty.channel.Channel;
import tech.hexadevelopment.practice.LegionPractice;

public class CManagerNewer{

	private static boolean viaVersionLogged;
	private static CManagerNewer channelManager;
	private LegionPractice plugin;

	public CManagerNewer(LegionPractice plugin) {
		this.plugin = plugin;
	}
	
	public static void register(LegionPractice plugin) {
		channelManager = new CManagerNewer(plugin);
		Bukkit.getPluginManager().registerEvents(channelManager.new BukkitListener(), plugin);
	}

	public void injectPlayer(Player p) {
		try {
			Channel channel = getChannel(p);
			Object c = channel.pipeline().get(CHandlerCommon.HANDLER_NAME);
			if(c == null) {
				channel.pipeline().addBefore("packet_handler", CHandlerCommon.HANDLER_NAME, new CHandlerNewer(p));
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
	
	public static CManagerNewer getChannelManager() {
		return channelManager;
	}
	
	class BukkitListener implements Listener {
		
		@EventHandler
		public void onLogin(PlayerJoinEvent e) {
			getChannelManager().injectPlayer(e.getPlayer());
		}
	}
}
