package sekelsta.game.terrain;

import java.util.Objects;

public class ChunkPos {
    public final int x;
    public final int y;
    public final int z;

    public ChunkPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ChunkPos(ChunkPos base, Direction direction) {
        this.x = base.x + direction.x;
        this.y = base.y + direction.y;
        this.z = base.z + direction.z;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof ChunkPos)) {
            return false;
        }
        ChunkPos other = (ChunkPos)o;
        return this.x == other.x && this.y == other.y && this.z == other.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "ChunkPos{" + x + ", " + y + ", " + z  + "}";
    }
}
