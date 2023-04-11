package sekelsta.game.terrain;

import java.util.*;

public class Surface {
    private final short[] blocks;
    private final int[] heights;

    public Surface(short[] blocks, int[] heights) {
        this.blocks = blocks;
        this.heights = heights;
    }

    public short getBlock(int bx, int by, int z) {
        int index = (bx << Chunk.TWO_POWER_SIZE) | by;
        int surfaceHeight = heights[index];
        if (z < surfaceHeight) {
            return Block.OCCUPIED;
        }
        else if (z == surfaceHeight) {
            return blocks[index];
        }
        else {
            assert(z < surfaceHeight);
            return Block.OCCUPIED;
        }
    }

    public int getHeight(int bx, int by) {
        int index = (bx << Chunk.TWO_POWER_SIZE) | by;
        return heights[index];
    }

    public void setBlockAndChunkify(int bx, int by, int z, short block, Map<Integer, Chunk> loadedChunks, 
            int chunkX, int chunkY, TerrainGenerator generator) {
        throw new RuntimeException("TODO");
    }
}
