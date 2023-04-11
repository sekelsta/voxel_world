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
        int index = getIndex(bx, by);
        int surfaceHeight = heights[index];
        if (z < surfaceHeight) {
            return Block.OCCUPIED;
        }
        else if (z == surfaceHeight) {
            return blocks[index];
        }
        else {
            assert(z > surfaceHeight);
            return Block.EMPTY;
        }
    }

    public int getHeight(int bx, int by) {
        int index = getIndex(bx, by);
        return heights[index];
    }

    public void setBlockAndChunkify(int bx, int by, int z, short block, Map<Integer, Chunk> loadedChunks, 
            int chunkX, int chunkY, TerrainGenerator generator) {
        int index = getIndex(bx, by);
        if (block != Block.EMPTY && heights[index] == z) {
            blocks[index] = block;
            return;
        }

        // TO_OPTIMIZE: Cache these values
        int highest = heights[0];
        int lowest = heights[0];
        for (int i = 1; i < heights.length; ++i) {
            highest = Math.max(highest, heights[i]);
            lowest = Math.min(lowest, heights[i]);
        }

        int chunkZ = Chunk.toChunkPos(z);
        int highestChunkZ = Chunk.toChunkPos(highest);

        Chunk chunk = generator.generateChunk(chunkX, chunkY, chunkZ);
        if (chunkZ > highestChunkZ || chunkZ < Chunk.toChunkPos(lowest)) {
            chunk.setBlock(bx, by, Chunk.toInnerPos(z), block);
            loadedChunks.put(chunkZ, chunk);
            return;
        }
        overwrite(chunk, chunkZ);
        chunk.setBlock(bx, by, Chunk.toInnerPos(z), block);
        loadedChunks.put(chunkZ, chunk);

        if (chunkZ < highestChunkZ) {
            assert(chunkZ + 1 == highestChunkZ);
            Chunk upperChunk = generator.generateChunk(chunkX, chunkY, highestChunkZ);
            overwrite(upperChunk, highestChunkZ);
            loadedChunks.put(highestChunkZ, upperChunk);
        }

        int level = Chunk.toBlockPos(chunkZ, 0) - 1;
        for (int i = 1; i < heights.length; ++i) {
            if (heights[i] > level) {
                heights[i] = level;
                blocks[i] = generator.generateBlock(Chunk.toBlockPos(chunkX, bx), Chunk.toBlockPos(chunkY, by), level);
                // This algorithm won't do at all if we can't assume everything below the surface is solid
                assert(blocks[i] != Block.EMPTY);
            }
        }
    }

    private int getIndex(int bx, int by) {
        return (bx << Chunk.TWO_POWER_SIZE) | by;
    }

    private void overwrite(Chunk chunk, int chunkZ) {
        for (int bx = 0; bx < Chunk.SIZE; ++bx) {
            for (int by = 0; by < Chunk.SIZE; ++by) {
                int index = getIndex(bx, by);
                if (Chunk.toChunkPos(heights[index]) != chunkZ) {
                    continue;
                }
                chunk.setBlock(bx, by, Chunk.toInnerPos(heights[index]), blocks[index]);
            }
        }
    }
}
