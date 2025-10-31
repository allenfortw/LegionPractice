package tech.hexadevelopment.practice.nms.access;

import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.tablist.TabEntry;
import tech.hexadevelopment.practice.utils.FieldUtils;
import tech.hexadevelopment.practice.utils.signgui.SignUpdate;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.EntityLightning;
import net.minecraft.server.v1_12_R1.NetworkManager;
import net.minecraft.server.v1_12_R1.PacketPlayInUpdateSign;
import net.minecraft.server.v1_12_R1.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityVelocity;
import net.minecraft.server.v1_12_R1.PacketPlayOutOpenSignEditor;
import net.minecraft.server.v1_12_R1.PacketPlayOutSpawnEntityWeather;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldEvent;

public class NMS_1_12_R1 implements NMSAccess{

	@Override
	public int getPing(Player p) {
		CraftPlayer cp = (CraftPlayer) p;
		return cp.getHandle().ping;
	}
	
	@Override
	public Object getChannel(Player p) {
		Field f = FieldUtils.getField(NetworkManager.class, "channel");
		CraftPlayer cp = (CraftPlayer) p;
		NetworkManager nm = cp.getHandle().playerConnection.networkManager;
		Object o = FieldUtils.get(f, nm);
		return o;
	}
	
	@Override
	public void openSignEditor(Player p, Location l) {
		BlockPosition bp = new BlockPosition(((CraftPlayer) p).getHandle());         
		PacketPlayOutOpenSignEditor packet = new PacketPlayOutOpenSignEditor(bp);
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}
	
	@Override
	public SignUpdate getSignUpdate(Object packet) {
		PacketPlayInUpdateSign signPacket = (PacketPlayInUpdateSign) packet;
		BlockPosition bp = signPacket.a();
		String[] text = signPacket.b();
		return new SignUpdate(new Location(Bukkit.getWorlds().get(0), bp.getX(), bp.getY(), bp.getZ()), text);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void breakBlockWithAnimation(Block b, int ticks) {
		if(ticks < 1) {
			for(int i = 0; i < 10; i++){
				((CraftServer) Bukkit.getServer()).getHandle().sendPacketNearby(
						null, b.getLocation().getX(), b.getLocation().getY(), b.getLocation().getZ(), 120,
						((CraftWorld) b.getWorld()).getHandle().dimension, new PacketPlayOutBlockBreakAnimation(1, new BlockPosition(b.getX(), b.getY(), b.getZ()), i));
			}
			((CraftServer) Bukkit.getServer()).getHandle().sendPacketNearby(
					null, b.getLocation().getX(), b.getLocation().getY(), b.getLocation().getZ(), 120,
					((CraftWorld) b.getWorld()).getHandle().dimension, new PacketPlayOutWorldEvent(2001, new BlockPosition(b.getX(), b.getY(), b.getZ()), b.getTypeId(), false));
			b.setType(Material.AIR);
		}
		else {
			new BukkitRunnable() {

				double counter = 0;
				double toDo = ticks/10;
				
				@Override
				public void run() {
					for(int i = 0; i < toDo; i++) {
						((CraftServer) Bukkit.getServer()).getHandle().sendPacketNearby(
								null, b.getLocation().getX(), b.getLocation().getY(), b.getLocation().getZ(), 120,
								((CraftWorld) b.getWorld()).getHandle().dimension, new PacketPlayOutBlockBreakAnimation(1, new BlockPosition(b.getX(), b.getY(), b.getZ()), (int) counter));
						counter += toDo;
					}
					if(counter >= 10) {
						((CraftServer) Bukkit.getServer()).getHandle().sendPacketNearby(
								null, b.getLocation().getX(), b.getLocation().getY(), b.getLocation().getZ(), 120,
								((CraftWorld) b.getWorld()).getHandle().dimension, new PacketPlayOutWorldEvent(2001, new BlockPosition(b.getX(), b.getY(), b.getZ()), b.getTypeId(), false));
						b.setType(Material.AIR);
						this.cancel();
					}
				}
			}.runTaskTimer(LegionPractice.getInstance(), 0, 1);
		}
	}

	@Override
	public void clearTabList(Player p, List<TabEntry> entries) {}
	
	
	@Override
	public void setupTabEntry(TabEntry entry) {}
	
	@Override
	public int getVersion(Player p) {
		return 0;
	}
	
	@Override
	public void strikeLightning(Player p, Location loc) {
		PacketPlayOutSpawnEntityWeather packet = new PacketPlayOutSpawnEntityWeather(new EntityLightning(((CraftPlayer) (Player) p).getHandle().getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), true, true));
		((CraftPlayer) (Player) p).getHandle().playerConnection.sendPacket(packet);
	}
	
	@Override
	public String getLanguage(Player p) {
		return ((CraftPlayer) p).getHandle().locale;
	}
	
	
	@Override
	public void sendKnockback(Player p, double x, double y, double z){
		PacketPlayOutEntityVelocity packet = new PacketPlayOutEntityVelocity(p.getEntityId(), x, y, z);
		CraftPlayer nmsPlayer = (CraftPlayer)p;
		nmsPlayer.getHandle().playerConnection.sendPacket(packet);
	}
}