package sekelsta.game.terrain;

import java.util.HashMap;

import sekelsta.game.Ray;
import sekelsta.game.RaycastResult;
import sekelsta.game.Vector2i;

public class Terrain {
    // 1 meter is this many blocks long
    public final int blockSize = 4;

    private HashMap<Vector2i, TerrainColumn> loadedColumns = new HashMap<>();
    private TerrainGenerator generator = new TerrainGenerator();

    public short getBlockIfLoaded(int x, int y, int z) {
        Vector2i pos = new Vector2i(Chunk.toChunkPos(x), Chunk.toChunkPos(y));
        TerrainColumn column = loadedColumns.get(pos);
        if (column == null) {
            return Block.EMPTY;
        }
        return column.getBlockIfLoaded(x & Chunk.MASK, y & Chunk.MASK, z);
    }

    public void setBlock(int x, int y, int z, short block) {
        Vector2i pos = new Vector2i(Chunk.toChunkPos(x), Chunk.toChunkPos(y));
        loadedColumns.get(pos).setBlock(x & Chunk.MASK, y & Chunk.MASK, z, block);
    }

    public TerrainColumn getColumnIfLoaded(int chunkX, int chunkY) {
        return loadedColumns.get(new Vector2i(chunkX, chunkY));
    }

    public void loadNear(int x, int y, int z, int chunkLoadRadius) {
        int chunkX = x >> Chunk.TWO_POWER_SIZE;
        int chunkY = y >> Chunk.TWO_POWER_SIZE;
        int chunkZ = z >> Chunk.TWO_POWER_SIZE;
        // TO_OPTIMIZE: Load the nearest chunks first
        // TO_OPTIMIZE: Load a cylinder or sphere of chunks instead of a rectangle
        for (int cx = chunkX - chunkLoadRadius; cx <= chunkX + chunkLoadRadius; ++cx) {
            for (int cy = chunkY - chunkLoadRadius; cy <= chunkY + chunkLoadRadius; ++cy) {
                Vector2i pos = new Vector2i(cx, cy);
                TerrainColumn column = loadedColumns.computeIfAbsent(pos, (p) -> new TerrainColumn(chunkX, chunkY));
                column.loadChunkRange(chunkZ - chunkLoadRadius, chunkZ + chunkLoadRadius, generator);
            }
        }
    }

    // TODO: Unload columns

    public RaycastResult findHit(Ray ray) {
        float s = blockSize;
        float originX = ray.origin().x * s;
        float originY = ray.origin().y * s;
        float originZ = ray.origin().z * s;
        float directionX = ray.direction().x * s;
        float directionY = ray.direction().y * s;
        float directionZ = ray.direction().z * s;

        // In case the camera is inside a solid block, the ray needs to pass through a non-solid block
        // before it can be counted
        boolean foundValidStart = false;
        int blockX = (int)Math.floor(originX);
        int blockY = (int)Math.floor(originY);
        int blockZ = (int)Math.floor(originZ);

        int stepX = directionX > 0? 1 : -1;
        int stepY = directionY > 0? 1 : -1;
        int stepZ = directionZ > 0? 1 : -1;

        float tDeltaX = stepX / directionX;
        float tDeltaY = stepY / directionY;
        float tDeltaZ = stepZ / directionZ;

        float tMaxX = getMaxStep(originX, directionX);
        float tMaxY = getMaxStep(originY, directionY);
        float tMaxZ = getMaxStep(originZ, directionZ);

        Direction hitDirection = null;
        Direction xDir = directionX > 0? Direction.WEST : Direction.EAST;
        Direction yDir = directionY > 0? Direction.SOUTH: Direction.NORTH;
        Direction zDir = directionZ > 0? Direction.DOWN: Direction.UP;
        while (blockX * stepX < (originX + directionX) * stepX
                && blockY * stepY < (originY + directionY) * stepY
                && blockZ * stepZ < (originZ + directionZ) * stepZ)
        {
            short block = getBlockIfLoaded(blockX, blockY, blockZ);
            if (foundValidStart && canPointAt(block)) {
                return new RaycastResult(blockX, blockY, blockZ, hitDirection);
            }
            foundValidStart = foundValidStart || !canPointAt(block);

            if (tMaxX < tMaxY && tMaxX < tMaxZ) {
                blockX += stepX;
                tMaxX += tDeltaX;
                hitDirection = xDir;
            }
            else if (tMaxY < tMaxZ) {
                blockY += stepY;
                tMaxY += tDeltaY;
                hitDirection = yDir;
            }
            else {
                blockZ += stepZ;
                tMaxZ += tDeltaZ;
                hitDirection = zDir;
            }
        }
        return null;
    }

    private boolean canPointAt(short block) {
        return block != Block.EMPTY;
    }

    private float getMaxStep(float origin, float direction) {
        if (direction > 0) {
            return (float)(Math.ceil(origin) - origin) / direction;
        }
        else {
            return (float)(Math.floor(origin) - origin) / direction;
        }
    }
}
