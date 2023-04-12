package sekelsta.game.terrain;

import java.util.*;
import sekelsta.game.Game;

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
            int chunkX, int chunkY, TerrainGenerator generator, Game game) {
        int index = getIndex(bx, by);
        if (block != Block.EMPTY && heights[index] == z) {
            blocks[index] = block;
            game.onSurfaceChanged(chunkX, chunkY);
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
        assert(!loadedChunks.containsKey(chunkZ));
        Chunk chunk = generator.generateChunk(chunkX, chunkY, chunkZ);
        if (chunkZ > highestChunkZ || chunkZ < Chunk.toChunkPos(lowest)) {
            // Ignore return value for setBlock since we're calling onChunkLoaded anyway
            chunk.setBlock(bx, by, Chunk.toInnerPos(z), block);
            loadedChunks.put(chunkZ, chunk);
            game.onChunkLoaded(chunkX, chunkY, chunkZ);
            return;
        }
        overwrite(chunk, chunkZ);
        // Ignore return value for setBlock since we're calling onChunkLoaded anyway
        chunk.setBlock(bx, by, Chunk.toInnerPos(z), block);
        loadedChunks.put(chunkZ, chunk);
        game.onChunkLoaded(chunkX, chunkY, chunkZ);

        if (chunkZ < highestChunkZ) {
            assert(!loadedChunks.containsKey(highestChunkZ));
            assert(chunkZ + 1 == highestChunkZ);
            Chunk upperChunk = generator.generateChunk(chunkX, chunkY, highestChunkZ);
            overwrite(upperChunk, highestChunkZ);
            loadedChunks.put(highestChunkZ, upperChunk);
            game.onChunkLoaded(chunkX, chunkY, highestChunkZ);
        }

        int level = Chunk.toBlockPos(chunkZ, 0) - 1;
        for (int i = 0; i < heights.length; ++i) {
            if (heights[i] > level) {
                heights[i] = level;
                blocks[i] = generator.generateBlock(Chunk.toBlockPos(chunkX, bx), Chunk.toBlockPos(chunkY, by), level);
                // This algorithm won't do at all if we can't assume everything below the surface is solid
                assert(blocks[i] != Block.EMPTY);
            }
        }
        game.onSurfaceChanged(chunkX, chunkY);
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
