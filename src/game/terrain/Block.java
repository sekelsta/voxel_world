package sekelsta.game.terrain;

public class Block {
    // The default block must match the default short
    public static final short EMPTY = 0;
    // Used when exact terrain value is unknown but should be treated as solid
    public static final short OCCUPIED = 1;

    public static final short STONE = 1;
    public static final short DIRT = 2;
    public static final short SAND = 3;
    public static final short GRASS = 4;
    public static final short SNOW = 5;
    public static final short SANDSTONE = 6;

    public static boolean isOpaque(short block) {
        return block != EMPTY;
    }
}
