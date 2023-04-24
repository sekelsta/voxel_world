package sekelsta.test.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import sekelsta.game.terrain.Block;
import sekelsta.game.terrain.Chunk;

class TestRunChunk {
    @Test
    void setEmptyToEmpty() {
        Chunk chunk = new Chunk();
        assert(Chunk.SIZE > 3);
        int x = 1;
        int y = 2;
        int z = 3;
        chunk.setBlock(x, y, z, Block.EMPTY);
        assertEquals(Block.EMPTY, chunk.getBlock(x, y, z));
    }

    @Test
    void setEmptyToNonempty() {
        Chunk chunk = new Chunk();
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
        Chunk chunk = new Chunk();
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
        Chunk chunk = new Chunk();
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
        Chunk chunk = new Chunk();
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
        Chunk chunk = new Chunk();
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
        Chunk chunk = new Chunk();
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
