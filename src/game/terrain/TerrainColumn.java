package sekelsta.game.terrain;

import java.util.*;

public class TerrainColumn {
    private final int chunkX;
    private final int chunkY;

    private Chunk highest;
    private int chunkZ;

    public TerrainColumn(int chunkX, int chunkY) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
    }

    public short getBlockIfLoaded(int bx, int by, int z) {
        if (z >> Chunk.TWO_POWER_SIZE > chunkZ) {
            return Block.EMPTY;
        }
        else if (z >> Chunk.TWO_POWER_SIZE > chunkZ) {
            return Block.OCCUPIED;
        }
        return highest.getBlock(bx, by, z & Chunk.MASK);
    }

    // Expects x and y to be in chunk coords, z in block coords
    public void setBlock(int x, int y, int z, short block) {
        assert(x >= 0);
        assert(x < Chunk.SIZE);
        assert(y >= 0);
        assert(y < Chunk.SIZE);
        throw new RuntimeException("TODO");
    }

    public Chunk getChunk(int chunkZ) {
        if (chunkZ == this.chunkZ) {
            return highest;
        }
        return null;
    }

    // If startZ is greated than stopZ, the chunks will be returned in reverse order
    public List<Integer> getLoadedChunks(int startZ, int stopZ) {
        int zMin = startZ;
        int zMax = stopZ;
        if (zMin > zMax) {
            zMin = stopZ;
            zMax = startZ;
        }
        ArrayList<Integer> result = new ArrayList<>();
        if (zMin <= chunkZ && chunkZ <= zMax) {
            result.add(chunkZ);
        }
        return result;
    }

    public void loadChunkRange(int minChunk, int maxChunk, TerrainGenerator generator) {
        // TODO
        chunkZ = generator.getHighestChunk(chunkX, chunkY);
        highest = generator.generateChunk(chunkX, chunkY, chunkZ);
    }

    // TODO: Unload chunks
}
