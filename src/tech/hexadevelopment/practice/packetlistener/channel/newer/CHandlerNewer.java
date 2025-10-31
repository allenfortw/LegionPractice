package tech.hexadevelopment.practice.packetlistener.channel.newer;

import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.packetlistener.PacketEvent;
import tech.hexadevelopment.practice.packetlistener.listener.PacketListener;
import tech.hexadevelopment.practice.packetlistener.listener.PacketListenerManager;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class CHandlerNewer extends ChannelDuplexHandler {
	
	private Player player;

	public CHandlerNewer(Player player) {
		this.player = player;
	}
	
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		PacketEvent packetEvent = new PacketEvent(player, msg);
		try{
			for(PacketListener listener : PacketListenerManager.listeners) {
				listener.onPacketOutAsync(packetEvent);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		if(msg != null && !packetEvent.isCancelled()) {
			super.write(ctx, msg, promise);
		}

	}


	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		PacketEvent packetEvent = new PacketEvent(player, msg);
		try{
			for(PacketListener listener : PacketListenerManager.listeners) {
				listener.onPacketInAsync(packetEvent);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		if(msg != null && !packetEvent.isCancelled()) {
			super.channelRead(ctx, msg);
		}

	}

}
