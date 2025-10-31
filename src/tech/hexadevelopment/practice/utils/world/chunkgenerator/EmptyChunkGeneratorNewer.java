package tech.hexadevelopment.practice.utils.world.chunkgenerator;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.MaterialData;

import net.minecraft.server.v1_7_R4.ChunkMap;

public class EmptyChunkGeneratorNewer extends ChunkGenerator {
	
    private static EmptyChunkGeneratorNewer emptyChunkGenerator = new EmptyChunkGeneratorNewer();

    private byte[] buf = new byte[0x10000];
    
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
    	return new ChunkData() {
			
			public void setRegion(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6, int arg7) {}
			
			public void setRegion(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, int arg6) {}
			
			public void setRegion(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, MaterialData arg6) {}
			
			public void setRegion(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, Material arg6) {}
			
			public void setBlock(int arg0, int arg1, int arg2, int arg3, byte arg4) {}
			
			public void setBlock(int arg0, int arg1, int arg2, int arg3) {}
			
			public void setBlock(int arg0, int arg1, int arg2, MaterialData arg3) {}
			
			public void setBlock(int arg0, int arg1, int arg2, Material arg3) {}
			
			public int getTypeId(int arg0, int arg1, int arg2) {
				return 0;
			}
			
			public MaterialData getTypeAndData(int arg0, int arg1, int arg2) {
				return null;
			}
			
			public Material getType(int arg0, int arg1, int arg2) {
				return null;
			}
			
			public int getMaxHeight() {
				return 0;
			}
			
			public byte getData(int arg0, int arg1, int arg2) {
				return 0;
			}
		};
    }
    
    
    
    public static EmptyChunkGeneratorNewer getEmptychunkgenerator() {
		return emptyChunkGenerator;
	}
}