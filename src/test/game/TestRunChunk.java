package sekelsta.test.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import sekelsta.game.terrain.Block;
import sekelsta.game.terrain.Chunk;
import sekelsta.game.terrain.RunChunk;

class TestRunChunk {
    @Test
    void setEmptyToEmpty() {
        RunChunk chunk = new RunChunk();
        assert(Chunk.SIZE > 3);
        int x = 1;
        int y = 2;
        int z = 3;
        chunk.setBlock(x, y, z, Block.EMPTY);
        assertEquals(Block.EMPTY, chunk.getBlock(x, y, z));
    }

    @Test
    void setEmptyToNonempty() {
        RunChunk chunk = new RunChunk();
        assert(Chunk.SIZE > 3);
        int x = 1;
        int y = 2;
        int z = 3;
        short block = (short)1;
        chunk.setBlock(x, y, z, block);
        assertEquals(block, chunk.getBlock(x, y, z));
        assertEquals(Block.EMPTY, chunk.getBlock(x, y + 1, z));
        assertEquals(Block.EMPTY, chunk.getBlock(x, y - 1, z));
    }

    @Test
    void setEmptyAfterNonempty() {
        RunChunk chunk = new RunChunk();
        assert(Chunk.SIZE > 3);
        int x = 1;
        int y = 2;
        int z = 3;
        chunk.setBlock(2, y, z, (short)1);
        short block = Block.EMPTY;
        chunk.setBlock(x, y, z, block);
        assertEquals(block, chunk.getBlock(x, y, z));
        assertEquals(Block.EMPTY, chunk.getBlock(x, y + 1, z));
        assertEquals(Block.EMPTY, chunk.getBlock(x, y - 1, z));
    }

    @Test
    void setDifferentRuns() {
        RunChunk chunk = new RunChunk();
        assert(Chunk.SIZE > 3);
        int x = 1;
        int y = 2;
        int z = 3;
        chunk.setBlock(2, y, z, (short)1);
        short block = (short)2;
        chunk.setBlock(x, y, z, block);
        assertEquals(block, chunk.getBlock(x, y, z));
        assertEquals(Block.EMPTY, chunk.getBlock(x, y + 1, z));
        assertEquals(Block.EMPTY, chunk.getBlock(x, y - 1, z));
    }

    @Test
    void setRunStart() {
        RunChunk chunk = new RunChunk();
        assert(Chunk.SIZE > 3);
        int x = 1;
        int y = 0;
        int z = 3;
        chunk.setBlock(2, 2, z, (short)1);
        short block = (short)2;
        chunk.setBlock(x, y, z, block);
        assertEquals(block, chunk.getBlock(x, y, z));
        assertEquals(Block.EMPTY, chunk.getBlock(x, y + 1, z));
    }

    @Test
    void setDifferentTypes() {
        RunChunk chunk = new RunChunk();
        assert(Chunk.SIZE > 3);
        int x = 1;
        int y = 2;
        int z = 3;
        chunk.setBlock(x, 1, z, (short)1);
        short block = (short)2;
        chunk.setBlock(x, y, z, block);
        assertEquals(block, chunk.getBlock(x, y, z));
    }

    @Test
    void setIslands() {
        RunChunk chunk = new RunChunk();
        assert(Chunk.SIZE > 3);
        int x = 1;
        int y = 2;
        int z = 3;
        chunk.setBlock(x, 0, z, (short)1);
        short block = (short)1;
        chunk.setBlock(x, y, z, block);
        assertEquals(block, chunk.getBlock(x, y, z));
    }
}
