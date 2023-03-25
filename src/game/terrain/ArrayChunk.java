package sekelsta.game.terrain;

public class ArrayChunk implements Chunk {
    // Array of block type identifiers, a flat array to avoid memory overhead
    private short[] blocks = null;

    @Override
    public void setBlock(int x, int y, int z, short block) {
        if (blocks == null) {
            if (block == Block.EMPTY) {
                return;
            }
            blocks = new short[SIZE * SIZE * SIZE];
        }
        if (blocks[getIndex(x, y, z)] == block) {
            return;
        }
        blocks[getIndex(x, y, z)] = block;
    }

    @Override
    public short getBlock(int x, int y, int z) {
        if (blocks == null) {
            return Block.EMPTY;
        }
        return blocks[getIndex(x, y, z)];
    }

    @Override
    public boolean isEmpty() {
        if (blocks == null) {
            return true;
        }
        for (int i = 0; i < Chunk.SIZE; ++i) {
            for (int j = 0; j < Chunk.SIZE; ++j) {
                for (int k = 0; k  < Chunk.SIZE; ++k) {
                    if (getBlock(i, j, k) != Block.EMPTY) {
                        return false;
                    }
                }
            }
        }
        // It is empty, but still has the array
        blocks = null;
        return true;
    }

    @Override
    public boolean isTriviallyEmpty() {
        return blocks == null;
    }

    private int getIndex(int x, int y, int z) {
        return x + SIZE * y + SIZE * SIZE * z;
    }
}
