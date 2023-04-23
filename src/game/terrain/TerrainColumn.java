package sekelsta.game.terrain;

import java.util.*;

public class TerrainColumn {
    public final int chunkX;
    public final int chunkY;

    private int surfaceChunkZ;
    private Map<Integer, Chunk> loadedChunks = new HashMap<>();

    public TerrainColumn(int chunkX, int chunkY, TerrainGenerator generator, Terrain terrain) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        generator.loadSurfaceChunks(chunkX, chunkY, loadedChunks, terrain);
    }

    public void loadChunkRange(int minChunk, int maxChunk, TerrainGenerator generator, Terrain terrain) {
        generator.loadChunkRange(chunkX, chunkY, loadedChunks, minChunk, maxChunk, terrain);
    }

    public short getBlockIfLoaded(int bx, int by, int z) {
        int chunkZ = z >> Chunk.TWO_POWER_SIZE;
        if (loadedChunks.containsKey(chunkZ)) {
            return loadedChunks.get(chunkZ).getBlock(bx, by, z & Chunk.MASK);
        }
        if (chunkZ < surfaceChunkZ) {
            return Block.OCCUPIED;
        }
        return Block.EMPTY;
    }

    // Deliberately package-private
    // Expects x and y to be in chunk coords, z in block coords
    void setBlock(int bx, int by, int z, short block, TerrainGenerator generator, Terrain terrain) {
        assert(bx >= 0);
        assert(bx < Chunk.SIZE);
        assert(by >= 0);
        assert(by < Chunk.SIZE);

        int chunkZ = z >> Chunk.TWO_POWER_SIZE;
        assert(loadedChunks.containsKey(chunkZ));

        boolean changed = loadedChunks.get(chunkZ).setBlock(bx, by, z & Chunk.MASK, block);
        if (!changed) {
            return;
        }
        terrain.onBlockChanged(Chunk.toBlockPos(chunkX, bx), Chunk.toBlockPos(chunkY, by), z, block);
        onChunkMaybeExposed(z - 1, generator, terrain);
        onChunkMaybeExposed(z + 1, generator, terrain);
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

    // TODO: Unload chunks

    public void onChunkMaybeExposed(int z, TerrainGenerator generator, Terrain terrain) {
        int chunkZ = Chunk.toChunkPos(z);

        if (chunkZ >= surfaceChunkZ) {
            return;
        }
        if (loadedChunks.containsKey(chunkZ)) {
            return;
        }
        Chunk chunk = generator.generateChunk(chunkX, chunkY, chunkZ);
        loadedChunks.put(chunkZ, chunk);
        while (loadedChunks.containsKey(surfaceChunkZ - 1)) {
            surfaceChunkZ -= 1;
        }
        terrain.onChunkLoaded(chunkX, chunkY, chunkZ);
    }
}
