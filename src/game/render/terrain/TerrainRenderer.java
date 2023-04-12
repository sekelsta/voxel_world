package sekelsta.game.render.terrain;

import java.awt.Color;
import java.util.*;

import sekelsta.engine.AABB;
import sekelsta.engine.render.*;
import sekelsta.engine.render.mesh.RigidMesh;
import sekelsta.game.Vector2i;
import sekelsta.game.terrain.*;
import shadowfox.math.Vector3f;
import shadowfox.math.Vector4f;
import shadowfox.math.Matrix4f;

public class TerrainRenderer {
    private static final int numIterations = 1;
    private static final int weight = 1;

    protected Terrain terrain;
    protected Matrix4f frustumMatrix = new Matrix4f();

    protected TextureArray texture;

    protected HashMap<ChunkPos, TerrainMesh> meshes = new HashMap<>();
    protected HashMap<Vector2i, TerrainMesh> surfaceMeshes = new HashMap<>();

    protected int neighborhood = 2;

    public TerrainRenderer(Terrain terrain) {
        this.terrain = terrain;
        this.texture = new TextureArray("stone.png", "dirt.png", "sand.png", "checkers.png", "stripes.png", "snow.png", "sandstone.png");
    }

    public void onBlockChanged(int x, int y, int z, short block) {
        int minChunkX = Chunk.toChunkPos(x - neighborhood);
        int maxChunkX = Chunk.toChunkPos(x + neighborhood);
        int minChunkY = Chunk.toChunkPos(y - neighborhood);
        int maxChunkY = Chunk.toChunkPos(y + neighborhood);
        int minChunkZ = Chunk.toChunkPos(z - neighborhood);
        int maxChunkZ = Chunk.toChunkPos(z + neighborhood);
        for (int chunkX = minChunkX; chunkX <= maxChunkX; ++chunkX) {
            for (int chunkY = minChunkY; chunkY <= maxChunkY; ++chunkY) {
                int surfaceZ = getSurfaceChunkHeight(chunkX, chunkY);
                if (surfaceZ - 1 < maxChunkZ || minChunkZ < surfaceZ + 1) {
                    surfaceMeshes.remove(new Vector2i(chunkX, chunkY));
                }
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; ++chunkZ) {
                    ChunkPos pos = new ChunkPos(chunkX, chunkY, chunkZ);
                    meshes.remove(pos);
                }
            }
        }
    }

    public void onChunkLoaded(int chunkX, int chunkY, int chunkZ) {
        for (int cx = chunkX - 1; cx <= chunkX + 1; ++cx) {
            for (int cy = chunkY - 1; cy <= chunkY + 1; ++cy) {
                int surfaceZ = getSurfaceChunkHeight(chunkX, chunkY);
                if (surfaceZ - 1 < chunkZ || chunkZ < surfaceZ + 1) {
                    surfaceMeshes.remove(new Vector2i(chunkX, chunkY));
                }
                for (int cz = chunkZ - 1; cz <= chunkZ + 1; ++cz) {
                    ChunkPos pos = new ChunkPos(cx, cy, cz);
                    meshes.remove(pos);
                }
            }
        }
    }

    public void onSurfaceChanged(int chunkX, int chunkY) {
        surfaceMeshes.remove(new Vector2i(chunkX, chunkY));
    }

    public void onSurfaceLoaded(int chunkX, int chunkY, Surface surface) {
        int surfaceZ = Chunk.toChunkPos(surface.getHeight(Chunk.SIZE / 2, Chunk.SIZE / 2));
        for (int cx = chunkX - 1; cx <= chunkX + 1; ++cx) {
            for (int cy = chunkY - 1; cy <= chunkY + 1; ++cy) {
                surfaceMeshes.remove(new Vector2i(cx, cy));
                for (int chunkZ = surfaceZ - 1; chunkZ <= surfaceZ + 1; ++chunkZ) {
                    meshes.remove(new ChunkPos(chunkX, chunkY, chunkZ));
                }
            }
        }
    }

    private int getSurfaceChunkHeight(int chunkX, int chunkY) {
        TerrainColumn column = terrain.getColumnIfLoaded(chunkX, chunkY);
        return Chunk.toChunkPos(column.getSurface().getHeight(Chunk.SIZE / 2, Chunk.SIZE / 2));
    }

    public void render(MatrixStack stack, Frustum frustum, float lerp) {
        texture.bind();

        // Z up, Y forward, X right coordinate system
        Vector4f upperNearLeft4 = new Vector4f(-1, -1, 1);
        Vector4f upperNearRight4 = new Vector4f(1, -1, 1);
        Vector4f upperFarLeft4 = new Vector4f(-1, 1, 1);
        Vector4f upperFarRight4 = new Vector4f(1, 1, 1);
        Vector4f lowerNearLeft4 = new Vector4f(-1, -1, -1);
        Vector4f lowerNearRight4 = new Vector4f(1, -1, -1);
        Vector4f lowerFarLeft4 = new Vector4f(-1, 1, -1);
        Vector4f lowerFarRight4 = new Vector4f(1, 1, -1);

        Matrix4f matrix = stack.getResult().invert().multiply(frustum.getInverse());

        // Transform from cube to world space
        Vector3f upperNearLeft = matrix.transform(upperNearLeft4).toVec3();
        Vector3f upperNearRight = matrix.transform(upperNearRight4).toVec3();
        Vector3f upperFarLeft = matrix.transform(upperFarLeft4).toVec3();
        Vector3f upperFarRight = matrix.transform(upperFarRight4).toVec3();
        Vector3f lowerNearLeft = matrix.transform(lowerNearLeft4).toVec3();
        Vector3f lowerNearRight = matrix.transform(lowerNearRight4).toVec3();
        Vector3f lowerFarLeft = matrix.transform(lowerFarLeft4).toVec3();
        Vector3f lowerFarRight = matrix.transform(lowerFarRight4).toVec3();

        AABB bound = new AABB().bound(upperNearLeft, upperNearRight, upperFarLeft, upperFarRight,
                                lowerNearLeft, lowerNearRight, lowerFarLeft, lowerFarRight);

        // Convert world space to chunk coordinates. Round so as to include all partial chunks.
        int minX = (int)Math.floor(bound.getMinX() / Chunk.SIZE * terrain.blockSize);
        int maxX = (int)Math.ceil(bound.getMaxX() / Chunk.SIZE * terrain.blockSize);
        int minY = (int)Math.floor(bound.getMinY() / Chunk.SIZE * terrain.blockSize);
        int maxY = (int)Math.ceil(bound.getMaxY() / Chunk.SIZE * terrain.blockSize);
        int minZ = (int)Math.floor(bound.getMinZ() / Chunk.SIZE * terrain.blockSize);
        int maxZ = (int)Math.ceil(bound.getMaxZ() / Chunk.SIZE * terrain.blockSize);

        // Want to sort the chunks from nearest to farthest
        Vector3f near = Vector3f.average(upperNearLeft, upperNearRight, lowerNearLeft, lowerNearRight);
        Vector3f far = Vector3f.average(upperFarLeft, upperFarRight, lowerFarLeft, lowerFarRight);
        int xStart, xStop, xDir;
        if (near.x <= far.x) {
            xStart = minX;
            xStop = maxX;
            xDir = 1;
        }
        else {
            xStart = maxX;
            xStop = minX;
            xDir = -1;
        }

        int yStart, yStop, yDir;
        if (near.y <= far.y) {
            yStart = minY;
            yStop = maxY;
            yDir = 1;
        }
        else {
            yStart = maxY;
            yStop = minY;
            yDir = -1;
        }

        int zStart, zStop, zDir;
        if (near.z <= far.z) {
            zStart = minZ;
            zStop = maxZ;
            zDir = 1;
        }
        else {
            zStart = maxZ;
            zStop = minZ;
            zDir = -1;
        }

        // Expand frustum by 1/2 chunk in each direction for chunk culling
        float chunkSize = (float)Chunk.SIZE / terrain.blockSize;
        frustum.grown(frustumMatrix, chunkSize * 0.87f); // 0.87 > sqrt(3)/2
        frustumMatrix.multiply(stack.getResult());

        for (int x = xStart; x * xDir <= xStop * xDir; x += xDir) {
            for (int y = yStart; y * yDir <= yStop * yDir; y += yDir) {
                TerrainColumn column = terrain.getColumnIfLoaded(x, y);
                if (column == null) {
                    continue;
                }

                Surface surface = column.getSurface();
                // TO_OPTIMIZE: Cull if outside of frustum
                stack.push();
                float scale = (float)Chunk.SIZE / terrain.blockSize;
                stack.translate(x * scale, y * scale, 0);
                Vector2i surfacePos = new Vector2i(x, y);
                if (!surfaceMeshes.containsKey(surfacePos)) {
                    surfaceMeshes.put(surfacePos, getSurfaceMesh(surfacePos, surface));
                }
                if (surfaceMeshes.get(surfacePos) != null) {
                    surfaceMeshes.get(surfacePos).render();
                }
                stack.pop();

                List<Integer> chunkLocations = column.getLoadedChunkLocations(zStart, zStop);
                for (int z : chunkLocations) {
                    Chunk chunk = column.getChunk(z);
                    if (chunk == null || chunk.isEmpty()) {
                        continue;
                    }
                    Vector4f chunkCenter = new Vector4f((x + 0.5f) * chunkSize, (y + 0.5f) * chunkSize, (z + 0.5f) * chunkSize);
                    Vector3f clipSpace = frustumMatrix.transform(chunkCenter).toVec3();
                    if (clipSpace.x > 1 || clipSpace.x < -1
                            || clipSpace.y > 1 || clipSpace.y < -1
                            || clipSpace.z > 1 || clipSpace.z < -1) {
                        continue;
                    }
                    // Rendering is front to back, more performant for opaque things
                    stack.push();
                    stack.translate(x * scale, y * scale, z * scale);
                    ChunkPos chunkPos = new ChunkPos(x, y, z);
                    if (!meshes.containsKey(chunkPos)) {
                        meshes.put(chunkPos, getMeshForChunk(chunkPos));
                    }
                    if (meshes.get(chunkPos) != null) {
                        meshes.get(chunkPos).render();
                    }
                    stack.pop();
                }
            }
        }
    }

    protected TerrainMesh getMeshForChunk(ChunkPos pos) {
        return new MergedOctahedrons(terrain).getMesh(numIterations, weight, pos.x, pos.y, pos.z);
    }

    protected TerrainMesh getSurfaceMesh(Vector2i pos, Surface surface) {
        return new MergedOctahedrons(terrain).getMesh(numIterations, weight, pos.x(), pos.y(), surface);
    }

    public void clean() {
        // TODO
    }

    protected boolean isOpaque(short block) {
        return Block.isOpaque(block);
    }
}
