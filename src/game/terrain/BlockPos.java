package sekelsta.game.terrain;

import java.util.Objects;

public class BlockPos {
    public int x;
    public int y;
    public int z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof BlockPos)) {
            return false;
        }
        BlockPos other = (BlockPos)o;
        return this.x == other.x && this.y == other.y && this.z == other.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    public ChunkPos toChunkPos() {
        return new ChunkPos(Math.floorDiv(x, Chunk.SIZE), Math.floorDiv(y, Chunk.SIZE), Math.floorDiv(z, Chunk.SIZE));
    }

    @Override
    public String toString() {
        return "BlockPos{" + x + ", " + y + ", " + z  + "}";
    }
}
