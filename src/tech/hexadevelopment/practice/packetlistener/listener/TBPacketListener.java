package tech.hexadevelopment.practice.packetlistener.listener;

import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.packetlistener.PacketEvent;
import tech.hexadevelopment.practice.packetlistener.PacketType;
import tech.hexadevelopment.practice.utils.signgui.SignGUI;
import tech.hexadevelopment.practice.utils.signgui.SignUpdate;

public class TBPacketListener extends PacketListener {

	
	private LegionPractice plugin;

	
	public TBPacketListener(LegionPractice plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onPacketOutAsync(PacketEvent e) {}


	@Override
	public void onPacketInAsync(PacketEvent e) {
		Player p = e.getPlayer();
		PacketType type = e.getType();
		if(type != null) {
			if(type.equals(PacketType.PLAY_IN_UPDATE_SIGN)) {
				SignUpdate update = plugin.getNMSAccessProvider().getAccess().getSignUpdate(e.getPacket());
				if(update != null && SignGUI.onFinish(p, update)) {
					e.setCancelled(true);
				}
			}
		}
	}
}
