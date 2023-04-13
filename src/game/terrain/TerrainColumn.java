package sekelsta.game.terrain;

import java.util.*;

public class TerrainColumn {
    private final int chunkX;
    private final int chunkY;

    private Surface surface;
    private Map<Integer, Chunk> loadedChunks = new HashMap<>();

    public TerrainColumn(int chunkX, int chunkY, TerrainGenerator generator, Terrain terrain) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.surface = generator.generateSurface(chunkX, chunkY);
        terrain.onSurfaceLoaded(chunkX, chunkY, this.surface);
    }

    public Surface getSurface() {
        return surface;
    }

    public void loadChunkRange(int minChunk, int maxChunk, TerrainGenerator generator, Terrain terrain) {
        generator.loadChunkRange(chunkX, chunkY, loadedChunks, minChunk, maxChunk, terrain);
    }

    public short getBlockIfLoaded(int bx, int by, int z) {
        int chunkZ = z >> Chunk.TWO_POWER_SIZE;
        if (loadedChunks.containsKey(chunkZ)) {
            return loadedChunks.get(chunkZ).getBlock(bx, by, z & Chunk.MASK);
        }
        return surface.getBlock(bx, by, z);
    }

    // Deliberately package-private
    // Expects x and y to be in chunk coords, z in block coords
    void setBlock(int bx, int by, int z, short block, TerrainGenerator generator, Terrain terrain) {
        assert(bx >= 0);
        assert(bx < Chunk.SIZE);
        assert(by >= 0);
        assert(by < Chunk.SIZE);

        int chunkZ = z >> Chunk.TWO_POWER_SIZE;
        if (loadedChunks.containsKey(chunkZ)) {
            boolean changed = loadedChunks.get(chunkZ).setBlock(bx, by, z & Chunk.MASK, block);
            if (changed) {
                terrain.onBlockChanged(Chunk.toBlockPos(chunkX, bx), Chunk.toBlockPos(chunkY, by), z, block);
            }
            int lowerChunk = Chunk.toChunkPos(z - 1);
            if (lowerChunk != chunkZ && !loadedChunks.containsKey(lowerChunk)) {
                surface.chunkify(z - 1, loadedChunks, chunkX, chunkY, generator, terrain);
            }
            int upperChunk = Chunk.toChunkPos(z + 1);
            if (upperChunk != chunkZ && !loadedChunks.containsKey(upperChunk)) {
                surface.chunkify(z + 1, loadedChunks, chunkX, chunkY, generator, terrain);
            }
            return;
        }

        surface.setBlockAndChunkify(bx, by, z, block, loadedChunks, chunkX, chunkY, generator, terrain);
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

    public void onSurfaceExposed(int x, int y, int z, TerrainGenerator generator, Terrain terrain) {
        // Take advantage of the fact that this is only called for chunks with a neighboring block
        int bx = Chunk.toInnerPos(x + chunkX - Chunk.toChunkPos(x));
        int by = Chunk.toInnerPos(y + chunkY - Chunk.toChunkPos(y));
        int surfaceZ = surface.getHeight(bx, by);
        if (z >= surfaceZ) {
            return;
        }
        int chunkZ = Chunk.toChunkPos(z);
        if (loadedChunks.containsKey(chunkZ)) {
            return;
        }
        surface.chunkify(z, loadedChunks, chunkX, chunkY, generator, terrain);
    }
}
