package tech.hexadevelopment.practice.nms.access;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import tech.hexadevelopment.practice.LegionPractice;
import tech.hexadevelopment.practice.tablist.TabEntry;
import tech.hexadevelopment.practice.utils.FieldUtils;
import tech.hexadevelopment.practice.utils.signgui.SignUpdate;
import net.minecraft.server.v1_7_R4.EntityLightning;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import net.minecraft.server.v1_7_R4.NetworkManager;
import net.minecraft.server.v1_7_R4.PacketPlayInUpdateSign;
import net.minecraft.server.v1_7_R4.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityVelocity;
import net.minecraft.server.v1_7_R4.PacketPlayOutOpenSignEditor;
import net.minecraft.server.v1_7_R4.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_7_R4.PacketPlayOutSpawnEntityWeather;
import net.minecraft.server.v1_7_R4.PacketPlayOutWorldEvent;
import net.minecraft.server.v1_7_R4.PlayerConnection;
import net.minecraft.server.v1_7_R4.PlayerInteractManager;
import net.minecraft.util.com.mojang.authlib.GameProfile;

public class NMS_1_7_R3 implements NMSAccess {

	@Override
	public int getPing(Player p) {
		CraftPlayer cp = (CraftPlayer) p;
		return cp.getHandle().ping;
	}
	
	@Override
	public Object getChannel(Player p) {
		Field f = FieldUtils.getField(NetworkManager.class, "m");
		CraftPlayer cp = (CraftPlayer) p;
		NetworkManager nm = cp.getHandle().playerConnection.networkManager;
		Object o = FieldUtils.get(f, nm);
		return o;
	}
	
	@Override
	public void openSignEditor(Player p, Location l) {
		PacketPlayOutOpenSignEditor packet = new PacketPlayOutOpenSignEditor(l.getBlockX(), l.getBlockY(), l.getBlockZ());
		PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
		connection.sendPacket(packet);
	}
	
	@Override
	public SignUpdate getSignUpdate(Object packet) {
		PacketPlayInUpdateSign signPacket = (PacketPlayInUpdateSign) packet;
		return new SignUpdate(new Location(Bukkit.getWorlds().get(0), signPacket.c(), signPacket.d(), signPacket.e()), signPacket.f());
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void breakBlockWithAnimation(Block b, int ticks) {
		if(ticks < 1) {
			for(int i = 0; i < 10; i++){
				((CraftServer) Bukkit.getServer()).getHandle().sendPacketNearby(
						b.getLocation().getX(), b.getLocation().getY(), b.getLocation().getZ(), 120,
						((CraftWorld) b.getWorld()).getHandle().dimension, new PacketPlayOutBlockBreakAnimation(1, b.getX(), b.getY(), b.getZ(), i));
			}
			((CraftServer) Bukkit.getServer()).getHandle().sendPacketNearby(
					b.getLocation().getX(), b.getLocation().getY(), b.getLocation().getZ(), 120,
					((CraftWorld) b.getWorld()).getHandle().dimension, new PacketPlayOutWorldEvent(2001, b.getX(), b.getY(), b.getZ(), b.getTypeId(), false));
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
								b.getLocation().getX(), b.getLocation().getY(), b.getLocation().getZ(), 120,
								((CraftWorld) b.getWorld()).getHandle().dimension, new PacketPlayOutBlockBreakAnimation(1, b.getX(), b.getY(), b.getZ(), (int) counter));
						counter += toDo;
					}
					if(counter >= 10) {
						((CraftServer) Bukkit.getServer()).getHandle().sendPacketNearby(
								b.getLocation().getX(), b.getLocation().getY(), b.getLocation().getZ(), 120,
								((CraftWorld) b.getWorld()).getHandle().dimension, new PacketPlayOutWorldEvent(2001, b.getX(), b.getY(), b.getZ(), b.getTypeId(), false));
						b.setType(Material.AIR);
						this.cancel();
					}
				}
			}.runTaskTimer(LegionPractice.getInstance(), 0, 1);
		}
	}
	
	@Override
    public void clearTabList(Player p, List<TabEntry> entries) {
        for (TabEntry entry : entries) {
            if (entry.getNMS() != null) {
            	new PacketPlayOutPlayerInfo();
                PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
                ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
            }
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
        	PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
            ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
        }
        entries.clear();
    }
	
	
	@Override
	public void setupTabEntry(TabEntry entry){
		Player player = entry.getCustomTab().getPlayer();
		CraftPlayer craftplayer = (CraftPlayer)player;
		entry.setNMS(new EntityPlayer(MinecraftServer.getServer(), ((CraftWorld)player.getWorld()).getHandle(), new GameProfile(UUID.randomUUID(), ChatColor.translateAlternateColorCodes('&', entry.getText())),
				new PlayerInteractManager(((CraftWorld)player.getWorld()).getHandle())));
		PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
		PacketPlayOutPlayerInfo packet2 = new PacketPlayOutPlayerInfo();
		craftplayer.getHandle().playerConnection.sendPacket(packet);
		craftplayer.getHandle().playerConnection.sendPacket(packet2);
		Team team = entry.getCustomTab().getScoreboard().registerNewTeam(UUID.randomUUID().toString().substring(0, 16));
		entry.setTeam(team);
		team.addEntry(((EntityPlayer) entry.getNMS()).getName());
	}
	
	@Override
	public int getVersion(Player p) {
		return ((CraftPlayer) p).getHandle().playerConnection.networkManager.getVersion();
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
