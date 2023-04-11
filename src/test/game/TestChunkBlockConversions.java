package sekelsta.test.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import sekelsta.game.terrain.Chunk;

class TestChunkBlockConversions {
    @Test
    void toChunkPos() {
        int x = 3;
        int chunkX = x / Chunk.SIZE;
        assertEquals(chunkX, Chunk.toChunkPos(x));
    }

    @Test
    void toChunkPosNegative() {
        int x = -3;
        int chunkX = (int)Math.floor(x / (double)Chunk.SIZE);
        assertEquals(chunkX, Chunk.toChunkPos(x));
    }

    @Test
    void toInnerPos() {
        int x = 137;
        int bx = x % Chunk.SIZE;
        assertEquals(bx, Chunk.toInnerPos(x));
    }

    @Test
    void toInnerPosNegative() {
        int x = -137;
        int bx = ((x % Chunk.SIZE) + Chunk.SIZE) % Chunk.SIZE;
        assertEquals(bx, Chunk.toInnerPos(x));
    }

    @Test
    void toBlockPos() {
        int chunkX = 3;
        int bx = 5;
        int x = Chunk.SIZE * chunkX + bx;
        assertEquals(x, Chunk.toBlockPos(chunkX, bx));
    }

    @Test
    void toBlockPosNegative() {
        int chunkX = -3;
        int bx = 5;
        int x = Chunk.SIZE * chunkX + bx;
        assertEquals(x, Chunk.toBlockPos(chunkX, bx));
    }
}
