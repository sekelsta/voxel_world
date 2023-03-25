package sekelsta.game.terrain;

import shadowfox.math.Vector3f;

public enum Direction {
    UP(0, 0, 1),
    DOWN(0, 0, -1),
    NORTH(0, 1, 0),
    SOUTH(0, -1, 0),
    EAST(1, 0, 0),
    WEST(-1, 0, 0)
    ;

    public int x, y, z;

    private Direction(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f vector() {
        return new Vector3f(x, y, z);
    }
}
