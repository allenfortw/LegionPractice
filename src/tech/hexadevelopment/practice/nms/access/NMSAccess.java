package tech.hexadevelopment.practice.nms.access;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import tech.hexadevelopment.practice.tablist.TabEntry;
import tech.hexadevelopment.practice.utils.signgui.SignUpdate;

public interface NMSAccess {

	public int getPing(Player p);

	public Object getChannel(Player p);

	public void openSignEditor(Player p, Location l);

	public SignUpdate getSignUpdate(Object packet);

	public void breakBlockWithAnimation(Block b, int ticks);

	public void clearTabList(Player p, List<TabEntry> entries);

	public void setupTabEntry(TabEntry entry);

	public int getVersion(Player p);

	public void strikeLightning(Player p, Location loc);

	public String getLanguage(Player p);

	public void sendKnockback(Player p, double x, double y, double z);
}
