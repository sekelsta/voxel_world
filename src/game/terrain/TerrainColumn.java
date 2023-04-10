package sekelsta.game.terrain;

import java.util.*;

public class TerrainColumn {
    private final int chunkX;
    private final int chunkY;

    private Map<Integer, Chunk> loadedChunks = new HashMap<>();

    public TerrainColumn(int chunkX, int chunkY) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
    }

    public short getBlockIfLoaded(int bx, int by, int z) {
        int chunkZ = z >> Chunk.TWO_POWER_SIZE;
        if (loadedChunks.containsKey(chunkZ)) {
            return loadedChunks.get(chunkZ).getBlock(bx, by, z & Chunk.MASK);
        }
        return Block.EMPTY;
    }

    // Deliberately package-private
    // Expects x and y to be in chunk coords, z in block coords
    void setBlock(int bx, int by, int z, short block, TerrainGenerator generator) {
        assert(bx >= 0);
        assert(bx < Chunk.SIZE);
        assert(by >= 0);
        assert(by < Chunk.SIZE);

        int chunkZ = z >> Chunk.TWO_POWER_SIZE;
        if (loadedChunks.containsKey(chunkZ)) {
            loadedChunks.get(chunkZ).setBlock(bx, by, z & Chunk.MASK, block);
            return;
        }
    }

    public Chunk getChunk(int chunkZ) {
        return loadedChunks.get(chunkZ);
    }

    // TO_OPTIMIZE: Ideally if startZ is greated than stopZ, the chunks should be returned in reverse order
    public List<Integer> getLoadedChunkLocations(int zStart, int zStop) {
        int zMin = zStart;
        int zMax = zStop;
        if (zMin > zMax) {
            zMin = zStop;
            zMax = zStart;
        }
        List<Integer> chunkLocations = new ArrayList<Integer>();
        for (int z : loadedChunks.keySet()) {
            if (z >= zMin && z <= zMax) {
                chunkLocations.add(z);
            }
        }
        return chunkLocations;
    }

    public void loadChunkRange(int minChunk, int maxChunk, TerrainGenerator generator) {
        // TODO: Don't always assume the highest chunk is the only one needed
        int chunkZ = generator.getHighestChunk(chunkX, chunkY);
        if (!loadedChunks.containsKey(chunkZ)) {
            loadedChunks.put(chunkZ, generator.generateChunk(chunkX, chunkY, chunkZ));
        }
    }

    // TODO: Unload chunks
}
