package tech.hexadevelopment.practice.misc;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import tech.hexadevelopment.practice.arena.Arena;
import tech.hexadevelopment.practice.LegionPractice;

public class ChunkKeeper implements Listener{

	private static HashSet<Coords> chunks = new HashSet<Coords>();
	private static LegionPractice plugin;


	public ChunkKeeper(LegionPractice plugin) {
		ChunkKeeper.plugin = plugin;
	}

	public static void setArenaChunks() {
		int radius = Bukkit.getViewDistance();
		for(Arena ar : plugin.arenas) {
			if(!ar.isUsing() && ar.getCenter() != null && ar.getLoc1() != null && ar.getLoc2() != null) {
				Location l = ar.getCenter();
				for(int x = 0; x < radius+radius; x++) {
					for(int z = 0; z < radius+radius; z++) {
						Chunk c = l.getWorld().getChunkAt(l.getChunk().getX()-radius+x, l.getChunk().getZ()-radius+z);
						if(c != null) {
							chunks.add(new Coords(c));
							c.load();
						}
					}
				}
				l = ar.getLoc1();
				for(int x = 0; x < radius+radius; x++) {
					for(int z = 0; z < radius+radius; z++) {
						Chunk c = l.getWorld().getChunkAt(l.getChunk().getX()-radius+x, l.getChunk().getZ()-radius+z);
						if(c != null) {
							chunks.add(new Coords(c));
							c.load();
						}
					}
				}
				l = ar.getLoc2();
				for(int x = 0; x < radius+radius; x++) {
					for(int z = 0; z < radius+radius; z++) {
						Chunk c = l.getWorld().getChunkAt(l.getChunk().getX()-radius+x, l.getChunk().getZ()-radius+z);
						if(c != null) {
							chunks.add(new Coords(c));
							c.load();
						}
					}
				}
			}
		}
	}


	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent e) {
		Coords coords = new Coords(e.getChunk());
		for(Coords c : chunks) {
			if(coords.equals(c)) {
				e.setCancelled(true);
			}
		}
	}
}
class Coords {

	private int x, z;

	public Coords(Chunk c) {
		x = c.getX();
		z = c.getZ();
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Coords) {
			return ((Coords) obj).getX() == getX() && ((Coords) obj).getZ() == getZ();
		}
		return super.equals(obj);
	}
}
