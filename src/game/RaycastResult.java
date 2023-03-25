package sekelsta.game;

import sekelsta.game.terrain.Direction;

// Holds info about what block a ray hit
public record RaycastResult(int x, int y, int z, Direction direction) {}
