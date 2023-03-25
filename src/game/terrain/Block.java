package sekelsta.game.terrain;

public class Block {
    // The default block must match the default short
    public static final short EMPTY = 0;
    // Used when exact terrain value is unknown but should be treated as solid
    public static final short OCCUPIED = 1;

    public static boolean isOpaque(short block) {
        return block != EMPTY;
    }
}
