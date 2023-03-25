package sekelsta.game.terrain;

import java.util.ArrayList;

// A run-length-encoded chunk
public class RunChunk implements Chunk {
    // Runs are along the Y direction. X and Z are combined into a flat array.
    private byte[][] runs;
    private short[][] blocks;

    public RunChunk() {}

    public RunChunk(ArrayChunk arrayChunk) {
        if (arrayChunk.isTriviallyEmpty()) {
            return;
        }
        init();
        for (int z = 0; z < Chunk.SIZE; ++z) {
            for (int x = 0; x < Chunk.SIZE; ++x) {
                int index = getIndex(x, z);
                ArrayList<Byte> runsList = new ArrayList<>();
                ArrayList<Short> blocksList = new ArrayList<>();
                short block = arrayChunk.getBlock(x, 0, z);
                byte count = 0;
                for (int y = 0; y < Chunk.SIZE; ++y) {
                    short nextBlock = arrayChunk.getBlock(x, y, z);
                    if (nextBlock == block) {
                        ++count;
                    }
                    else {
                        runsList.add(count);
                        blocksList.add(block);
                        block = nextBlock;
                        count = 1;
                    }
                    runsList.add(count);
                    blocksList.add(block);
                }
                assert(runsList.size() == blocksList.size());
                runs[index] = new byte[runsList.size()];
                blocks[index] = new short[runsList.size()];
                for (int i = 0; i < runs[index].length; ++i) {
                    runs[index][i] = runsList.get(i);
                    blocks[index][i] = blocksList.get(i);
                }
            }
        }
        // This should usually return false, but if this chunk is empty,
        // it'll empty our contents
        // TO_OPTIMIZE: Check if this is faster than just calling arrayChunk.isEmpty()
        // at the start
        this.isEmpty();
    }

    public void init() {
        runs = new byte[Chunk.SIZE * Chunk.SIZE][];
        blocks = new short[Chunk.SIZE * Chunk.SIZE][];
    }


    @Override
    public void setBlock(int x, int y, int z, short block) {
        throw new RuntimeException("not yet implemented");
    }


    @Override
    public short getBlock(int x, int y, int z) {
        assert(x >= 0);
        assert(x < Chunk.SIZE);
        assert(y >= 0);
        assert(y < Chunk.SIZE);
        assert(z >= 0);
        assert(z < Chunk.SIZE);
        if (blocks == null) {
            return Block.EMPTY;
        }
        int index = getIndex(x, z);
        int count = 0;
        for (int i = 0; i < runs.length; ++i) {
            count += runs[index][i];
            if (y < count) {
                return blocks[index][i];
            }
        }
        throw new IllegalArgumentException("Y value " + y + " is out of chunk bounds");
    }

    @Override
    public boolean isEmpty() {
        if (blocks == null) {
            assert(runs == null);
            return true;
        }
        for (int i = 0; i < blocks.length; ++i) {
            if (blocks[i] == null) {
                continue;
            }
            if (blocks[i].length != 1 || blocks[i][0] != Block.EMPTY) {
                return false;
            }
        }
        // It's empty. Drop the baggage.
        runs = null;
        blocks = null;
        return true;
    }

    @Override
    public boolean isTriviallyEmpty() {
        return blocks == null;
    }

    // Be sure to call init() before calling this, otherwise the arrays could be null
    public void setRuns(byte x, byte z, byte[] runs, short[] blocks) {
        int index = getIndex(x, z);
        this.runs[index] = runs;
        this.blocks[index] = blocks;
    }

    private int getIndex(int x, int z) {
        return x + Chunk.SIZE * z;
    }
}
