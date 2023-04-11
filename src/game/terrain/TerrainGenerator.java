package sekelsta.game.terrain;

public class TerrainGenerator {
    public int getHighestChunk(int chunkX, int chunkY) {
        return 0;
    }

    public Chunk generateChunk(int chunkX, int chunkY, int chunkZ) {
        return new RunChunk();
/*
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
                blocks[0] = (short)1;
                chunk.setRuns(x, z, runs, blocks);
            }
        }
        chunk.isEmpty();
        return chunk;*/
    }

    public Surface generateSurface(int chunkX, int chunkY) {
        short[] blocks = new short[Chunk.SIZE * Chunk.SIZE];
        int[] heights = new int[Chunk.SIZE * Chunk.SIZE];
        for (int i = 0; i < Chunk.SIZE * Chunk.SIZE; ++i) {
            blocks[i] = Block.STONE;
        }
        return new Surface(blocks, heights);
    }
}
