package tech.hexadevelopment.practice.packetlistener.channel.older;

import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.packetlistener.PacketEvent;
import tech.hexadevelopment.practice.packetlistener.listener.PacketListener;
import tech.hexadevelopment.practice.packetlistener.listener.PacketListenerManager;
import net.minecraft.util.io.netty.channel.ChannelDuplexHandler;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.ChannelPromise;

public class CHandlerOlder extends ChannelDuplexHandler {

	private Player player;

	public CHandlerOlder(Player player) {
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
