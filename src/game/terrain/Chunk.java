package sekelsta.game.terrain;

import java.util.function.Consumer;

public interface Chunk {
    static final int TWO_POWER_SIZE = 5;
    // The number of blocks high, deep, and wide a chunk is. The total
    // number of blocks in a chunk is SIZE cubed.
    static final int SIZE = 1 << TWO_POWER_SIZE;
    static final int MASK = SIZE - 1;

    static int toChunkPos(int b) {
        return b >> TWO_POWER_SIZE;
    }

    void setBlock(int x, int y, int z, short block);

    short getBlock(int x, int y, int z);

    boolean isEmpty();

    boolean isTriviallyEmpty();
}
