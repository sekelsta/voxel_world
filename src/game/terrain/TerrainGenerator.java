package sekelsta.game.terrain;

import java.util.Map;

public class TerrainGenerator {
    public int getHighestChunk(int chunkX, int chunkY) {
        return 0;
    }

    public void loadChunkRange(int chunkX, int chunkY, Map<Integer, Chunk> loadedChunks,
            int minChunkZ, int maxChunkZ, Terrain terrain) {
        // Do nothing (only generate surfaces)
    }

    public Chunk generateChunk(int chunkX, int chunkY, int chunkZ) {
        RunChunk chunk = new RunChunk();
        if (chunkZ > 0) {
            return chunk;
        }
        chunk.init();
        for (byte z = 0; z < Chunk.SIZE; ++z) {
            for (byte x = 0; x < Chunk.SIZE; ++x) {
                byte[] runs = new byte[1];
                short[] blocks = new short[1];
                runs[0] = Chunk.SIZE;
                blocks[0] = Chunk.toBlockPos(chunkZ, z) > 0? Block.EMPTY : Block.DIRT;
                chunk.setRuns(x, z, runs, blocks);
            }
        }
        chunk.isEmpty();
        return chunk;
    }

    public short generateBlock(int x, int y, int z) {
        return z > 0? Block.EMPTY : Block.DIRT;
    }

    public Surface generateSurface(int chunkX, int chunkY) {
        short[] blocks = new short[Chunk.SIZE * Chunk.SIZE];
        int[] heights = new int[Chunk.SIZE * Chunk.SIZE];
        for (int i = 0; i < Chunk.SIZE * Chunk.SIZE; ++i) {
            blocks[i] = Block.DIRT;
        }
        return new Surface(blocks, heights);
    }
}
