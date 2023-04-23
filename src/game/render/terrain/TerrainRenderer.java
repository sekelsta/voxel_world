package sekelsta.game.render.terrain;

import java.awt.Color;
import java.util.*;

import sekelsta.engine.AABB;
import sekelsta.engine.Pair;
import sekelsta.engine.render.*;
import sekelsta.engine.render.mesh.RigidMesh;
import sekelsta.game.Vector2i;
import sekelsta.game.terrain.*;
import shadowfox.math.Vector3f;
import shadowfox.math.Vector4f;
import shadowfox.math.Matrix4f;

public class TerrainRenderer {
    private static final int numIterations = 1;
    private static final float weight = 5;

    protected Terrain terrain;
    protected Matrix4f frustumMatrix = new Matrix4f();

    protected TextureArray texture;

    protected HashMap<ChunkPos, TerrainMesh> meshes = new HashMap<>();

    protected int neighborhood = 2 + numIterations;

    TaskThread<ChunkPos, TerrainMeshData> chunkMeshingThread;

    public TerrainRenderer(Terrain terrain) {
        this.terrain = terrain;
        this.texture = new TextureArray("stone.png", "dirt.png", "sand.png", "checkers.png", "stripes.png", "snow.png", "sandstone.png");
        terrain.setTerrainRenderer(this);
        chunkMeshingThread = new TaskThread<>("Chunk meshing thread", (pos) -> calculateChunk(pos));
        chunkMeshingThread.start();
    }

    private TerrainMeshData calculateChunk(ChunkPos pos) {
        return new MergedOctahedrons(terrain).getMesh(numIterations, weight, pos.x, pos.y, pos.z);
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
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; ++chunkZ) {
                    chunkMeshingThread.queueTask(new ChunkPos(chunkX, chunkY, chunkZ));
                }
            }
        }
    }

    public void onChunkLoaded(int chunkX, int chunkY, int chunkZ) {
        for (int cx = chunkX - 1; cx <= chunkX + 1; ++cx) {
            for (int cy = chunkY - 1; cy <= chunkY + 1; ++cy) {
                for (int cz = chunkZ - 1; cz <= chunkZ + 1; ++cz) {
                    ChunkPos pos = new ChunkPos(cx, cy, cz);
                    chunkMeshingThread.queueTask(pos);
                }
            }
        }
    }

    public void render(MatrixStack stack, Frustum frustum, float lerp) {
        while (!chunkMeshingThread.completed.isEmpty()) {
            Pair<ChunkPos, TerrainMeshData> pair = chunkMeshingThread.completed.remove();
            TerrainMesh prev = meshes.put(pair.getKey(), new TerrainMesh(pair.getValue()));
            if (prev != null) {
                prev.clean();
            }
        }

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

        // Expand frustum by 1/2 chunk in each direction for chunk culling
        float chunkSize = (float)Chunk.SIZE / terrain.blockSize;
        frustum.grown(frustumMatrix, chunkSize * 0.87f); // 0.87 > sqrt(3)/2
        frustumMatrix.multiply(stack.getResult());

        // TO_OPTIMIZE: Sort columns front to back, more performant for opaque things
        for (TerrainColumn column : terrain.getRenderableColumns()) {
            if (column.chunkX < minX || column.chunkX > maxX || column.chunkY < minY || column.chunkY > maxY) {
                continue;
            }

            // TO_OPTIMIZE: Cull if outside of frustum
            int x = column.chunkX;
            int y = column.chunkY;
            float scale = (float)Chunk.SIZE / terrain.blockSize;

            List<Integer> chunkLocations = column.getLoadedChunkLocations(minZ, maxZ);
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

                ChunkPos chunkPos = new ChunkPos(x, y, z);
                if (meshes.containsKey(chunkPos)) {
                    stack.push();
                    stack.translate(x * scale, y * scale, z * scale);
                    meshes.get(chunkPos).render();
                    stack.pop();
                }
                else {
                    chunkMeshingThread.queueTask(chunkPos);
                }
            }
        }
    }

    public void clean() {
        chunkMeshingThread.setDone();
        for (TerrainMesh mesh : meshes.values()) {
            mesh.clean();
        }
        meshes.clear();
    }

    protected boolean isOpaque(short block) {
        return Block.isOpaque(block);
    }
}
