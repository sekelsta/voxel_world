package sekelsta.game.terrain;

import java.util.Map;

public class TerrainGenerator {
    public void loadChunkRange(int chunkX, int chunkY, Map<Integer, Chunk> loadedChunks,
            int minChunkZ, int maxChunkZ, Terrain terrain) {
        // Do nothing (only generate surfaces)
    }

    public void loadSurfaceChunks(int chunkX, int chunkY, Map<Integer, Chunk> loadedChunks, Terrain terrain) {
        int chunkZ = 0;
        Chunk chunk = generateChunk(chunkX, chunkY, chunkZ);
        loadedChunks.put(chunkZ, chunk);
        terrain.onChunkLoaded(chunkX, chunkY, chunkZ);
    }

    public Chunk generateChunk(int chunkX, int chunkY, int chunkZ) {
        Chunk chunk = new Chunk();
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
}
