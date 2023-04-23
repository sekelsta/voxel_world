package sekelsta.game.terrain;

import java.util.*;

import sekelsta.game.Game;
import sekelsta.game.Ray;
import sekelsta.game.RaycastResult;
import sekelsta.game.Vector2i;
import sekelsta.game.render.terrain.TerrainRenderer;

public class Terrain {
    // 1 meter is this many blocks long
    public final int blockSize = 4;

    private HashMap<Vector2i, TerrainColumn> loadedColumns = new HashMap<>();
    private List<TerrainColumn> renderableColumns = new ArrayList<>();
    private TerrainGenerator generator = new TerrainGenerator();
    private Game game;
    private TerrainRenderer terrainRenderer = null;

    public Terrain(Game game) {
        this.game = game;
    }

    public void setTerrainRenderer(TerrainRenderer terrainRenderer) {
        this.terrainRenderer = terrainRenderer;
    }

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
        loadedColumns.get(pos).setBlock(x & Chunk.MASK, y & Chunk.MASK, z, block, generator, this);
        if (block != Block.EMPTY) {
            return;
        }
        for (int cx = Chunk.toChunkPos(x - 1); cx <= Chunk.toChunkPos(x + 1); ++cx) {
            for (int cy = Chunk.toChunkPos(y - 1); cy <= Chunk.toChunkPos(y + 1); ++cy) {
                if (cx != pos.x() || cy != pos.y()) {
                    TerrainColumn neighbor = loadedColumns.get(new Vector2i(cx, cy));
                    if (neighbor != null) {
                        neighbor.onChunkMaybeExposed(z, generator, this);
                    }
                }
            }
        }
    }

    // Note: Don't call setBlock() on the returned column because it won't trigger the right events
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
                TerrainColumn column = getOrLoadColumn(cx, cy);
                column.loadChunkRange(chunkZ - chunkLoadRadius, chunkZ + chunkLoadRadius, generator, this);
            }
        }
    }

    private TerrainColumn getOrLoadColumn(int chunkX, int chunkY) {
        Vector2i pos = new Vector2i(chunkX, chunkY);
        if (!loadedColumns.containsKey(pos)) {
            loadedColumns.put(pos, new TerrainColumn(chunkX, chunkY, generator, this));
            for (int cx = chunkX - 1; cx <= chunkX + 1; ++cx) {
                for (int cy = chunkY - 1; cy <= chunkY + 1; ++cy) {
                    Vector2i neighbor = new Vector2i(cx, cy);
                    if (!loadedColumns.containsKey(neighbor)) {
                        continue;
                    }
                    boolean neighborsLoaded = true;
                    for (int nx = cx - 1; nx <= cx + 1; ++nx) {
                        for (int ny = cy - 1; ny <= cy + 1; ++ny) {
                            Vector2i nn = new Vector2i(nx, ny);
                            neighborsLoaded &= loadedColumns.containsKey(nn);
                        }
                    }
                    if (neighborsLoaded) {
                        renderableColumns.add(loadedColumns.get(neighbor));
                    }
                }
            }
        }
        return loadedColumns.get(pos);
    }

    // TODO: Unload columns

    public List<TerrainColumn> getRenderableColumns() {
        return renderableColumns;
    }

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

    public void onBlockChanged(int x, int y, int z, short block) {
        if (terrainRenderer != null) {
            terrainRenderer.onBlockChanged(x, y, z, block);
        }
    }

    public void onChunkLoaded(int chunkX, int chunkY, int chunkZ) {
        if (terrainRenderer != null) {
            terrainRenderer.onChunkLoaded(chunkX, chunkY, chunkZ);
        }
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
