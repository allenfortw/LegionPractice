package tech.hexadevelopment.practice.utils.world.chunkgenerator;

import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;

import tech.hexadevelopment.practice.LegionPractice;

public class EmptyChunkGeneratorProvider {

	private ChunkGenerator generator;
	
	public EmptyChunkGeneratorProvider() {
		if(LegionPractice.getInstance().getNMSAccessProvider().versionHasNoItemIDs) {
			generator = new EmptyChunkGeneratorNewer();
			Bukkit.getLogger().info("Using newer empty world generator!");
		}
		else {
			generator = new EmptyChunkGeneratorOlder();
			Bukkit.getLogger().info("Using older empty world generator!");
		}
	}
	
	public ChunkGenerator getGenerator() {
		return generator;
	}
	
}
