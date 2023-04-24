package sekelsta.game.terrain;

import java.util.ArrayList;

public class Chunk {
    static final int TWO_POWER_SIZE = 5;
    // The number of blocks high, deep, and wide a chunk is. The total
    // number of blocks in a chunk is SIZE cubed.
    public static final int SIZE = 1 << TWO_POWER_SIZE;
    static final int MASK = SIZE - 1;

    // Runs are along the Y direction. X and Z are combined into a flat array.
    private byte[][] runs;
    private short[][] blocks;

    public static int toChunkPos(int b) {
        return b >> TWO_POWER_SIZE;
    }

    public static int toInnerPos(int b) {
        return b & MASK;
    }

    public static int toBlockPos(int c, int b) {
        return (c << TWO_POWER_SIZE) | b;
    }

    public Chunk() {}

    public void init() {
        runs = new byte[Chunk.SIZE * Chunk.SIZE][];
        blocks = new short[Chunk.SIZE * Chunk.SIZE][];
    }

    // Returns true if the chunk was changed
    public boolean setBlock(int x, int y, int z, short block) {
        if (blocks == null) {
            if (block == Block.EMPTY) {
                return false;
            }
            init();
        }
        int index = getIndex(x, z);
        if (runs[index] == null) {
            if (block == Block.EMPTY) {
                return false;
            }
            int l = 3;
            if (y == 0 || y == Chunk.SIZE - 1) {
                l -= 1;
            }
            runs[index] = new byte[l];
            blocks[index] = new short[l];
            int i = 1;
            if (y == 0) {
                i = 0;
            }
            blocks[index][i] = block;
            runs[index][i] = 1;
            if (y == 0) {
                runs[index][1] = Chunk.SIZE - 1;
            }
            else if (y == Chunk.SIZE - 1) {
                runs[index][0] = Chunk.SIZE - 1;
            }
            else {
                runs[index][0] = (byte)y;
                runs[index][2] = (byte)(Chunk.SIZE - y - 1);
            }
            assert(runs[index].length <= Chunk.SIZE);
            return true;
        }

        assert(runs[index].length <= Chunk.SIZE);
        byte count = 0;
        for (int i = 0; i < runs[index].length; ++i) {
            count += runs[index][i];
            if (y < count) {
                if (blocks[index][i] == block) {
                    // Already set
                    return false;
                }
                boolean firstInRun = y == count - runs[index][i];
                boolean lastInRun = y == count - 1;
                if (i > 0 && firstInRun && blocks[index][i-1] == block) {
                    // Merge with previous neighbor
                    runs[index][i-1] += 1;
                    shrinkRun(index, i);
                    return true;
                }
                if (i + 1 < runs[index].length && lastInRun && blocks[index][i+1] == block) {
                    // Merge with next neighbor
                    runs[index][i+1] += 1;
                    shrinkRun(index, i);
                    return true;
                }
                if (runs[index][i] == 1) {
                    blocks[index][i] = block;
                    return true;
                }
                // Split the run
                int l = 2;
                if (lastInRun || firstInRun) {
                    l = 1;
                }
                byte[] newRuns = new byte[runs[index].length + l];
                short[] newBlocks = new short[blocks[index].length + l];
                assert(runs[index].length == blocks[index].length);
                for (int j = 0; j < runs[index].length; ++j) {
                    if (i < j) {
                        newRuns[j+l] = runs[index][j];
                        newBlocks[j+l] = blocks[index][j];
                    }
                    else if (i > j) {
                        newRuns[j] = runs[index][j];
                        newBlocks[j] = blocks[index][j];
                    }
                }
                if (firstInRun) {
                    newRuns[i] = 1;
                    newRuns[i+1] = (byte)(runs[index][i] - 1);
                    newBlocks[i] = block;
                    newBlocks[i+1] = blocks[index][i];
                }
                else if (lastInRun) {
                    newRuns[i+1] = 1;
                    newRuns[i] = (byte)(runs[index][i] - 1);
                    newBlocks[i+1] = block;
                    newBlocks[i] = blocks[index][i];
                }
                else {
                    newRuns[i] = (byte)(runs[index][i] - count + y);
                    newRuns[i+1] = 1;
                    newRuns[i+2] = (byte)(count - y - 1);
                    newBlocks[i] = blocks[index][i];
                    newBlocks[i+1] = block;
                    newBlocks[i+2] = blocks[index][i];
                }
                runs[index] = newRuns;
                blocks[index] = newBlocks;
                return true;
            }
        }
        throw new IllegalStateException("Y value " + y + " has no data");
    }

    private void shrinkRun(int index, int i) {
        runs[index][i] -= 1;
        if (runs[index][i] != 0) {
            return;
        }

        byte[] newRuns = new byte[runs[index].length - 1];
        short[] newBlocks = new short[blocks[index].length - 1];
        assert(runs[index].length == blocks[index].length);
        for (int j = 0; j < runs[index].length; ++j) {
            if (i < j) {
                newRuns[j-1] = runs[index][j];
                newBlocks[j-1] = blocks[index][j];
            }
            else if (i > j) {
                newRuns[j] = runs[index][j];
                newBlocks[j] = blocks[index][j];
            }
        }
        runs[index] = newRuns;
        blocks[index] = newBlocks;
        assert(runs[index].length <= Chunk.SIZE);
    }

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
        if (runs[index] == null) {
            return Block.EMPTY;
        }
        int count = 0;
        for (int i = 0; i < runs[index].length; ++i) {
            count += runs[index][i];
            if (y < count) {
                return blocks[index][i];
            }
        }
        throw new IllegalStateException("Y value " + y + " has no data");
    }

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
