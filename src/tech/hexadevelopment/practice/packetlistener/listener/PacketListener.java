package tech.hexadevelopment.practice.packetlistener.listener;

import tech.hexadevelopment.practice.packetlistener.PacketEvent;

public abstract class PacketListener {
	
	public PacketListener() {
		PacketListenerManager.registerPacketListener(this);
	}

	public abstract void onPacketOutAsync(PacketEvent event);

	public abstract void onPacketInAsync(PacketEvent event);
	
	public void unregister() {
		PacketListenerManager.unregister(this);
	}
	
}
