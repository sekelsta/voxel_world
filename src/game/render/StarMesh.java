package sekelsta.game.render;

import java.nio.ByteBuffer;
import java.util.Random;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import sekelsta.engine.render.mesh.Mesh;
import shadowfox.math.Vector3f;

public class StarMesh extends Mesh {
    public StarMesh(Random random) {
        int numStars = 6000 + random.nextInt(2000) + random.nextInt(2000) + random.nextInt(2000) + random.nextInt(4000);

        // 3 position floats, 1 uv byte, 3 color bytes
        int stride = 3 * Float.BYTES + 1 + 3;
        ByteBuffer vertexBuffer = MemoryUtil.memAlloc(4 * stride * numStars);

        for (int i = 0; i < numStars; ++i) {
            byte u0 = 0;
            byte v0 = 0;
            int u1 = 15 << 28;
            int v1 = 15 << 24;
            float brightness = random.nextInt(256) / 255f;
            int red = chooseColor(random, brightness);
            int green = chooseColor(random, brightness);
            int blue = chooseColor(random, brightness);
            int rgb = (red << 16) | (green << 8) | blue;
            //float size = brightness * (random.nextFloat() + random.nextFloat()) / 180;
            float size = brightness * random.nextFloat() / 120;
            Vector3f pos0 = Vector3f.randomNonzero(random).normalize();
            Vector3f pos0yawless = new Vector3f(0, (float)Math.sqrt(pos0.x * pos0.x + pos0.y * pos0.y), pos0.z);
            Vector3f pos2yawless = Vector3f.rotate(0, size, 0, pos0yawless, new Vector3f());
            float scale2 = pos2yawless.y / pos0yawless.y;
            Vector3f pos2 = new Vector3f(pos0.x * scale2, pos0.y * scale2, pos2yawless.z);
            Vector3f axis = Vector3f.subtract(pos2, pos0, new Vector3f()).normalize();
            Vector3f pos1 = Vector3f.rotate(size, axis.x, axis.y, axis.z, pos0, new Vector3f());
            Vector3f pos3 = Vector3f.rotate(size, axis.x, axis.y, axis.z, pos2, new Vector3f());
            // Write next 4 vertices
            vertexBuffer.putFloat(pos0.x);
            vertexBuffer.putFloat(pos0.y);
            vertexBuffer.putFloat(pos0.z);
            vertexBuffer.putInt(u0 | v0 | rgb);
            vertexBuffer.putFloat(pos1.x);
            vertexBuffer.putFloat(pos1.y);
            vertexBuffer.putFloat(pos1.z);
            vertexBuffer.putInt(u1 | v0 | rgb);
            vertexBuffer.putFloat(pos2.x);
            vertexBuffer.putFloat(pos2.y);
            vertexBuffer.putFloat(pos2.z);
            vertexBuffer.putInt(u0 | v1 | rgb);
            vertexBuffer.putFloat(pos3.x);
            vertexBuffer.putFloat(pos3.y);
            vertexBuffer.putFloat(pos3.z);
            vertexBuffer.putInt(u1 | v1 | rgb);
        }
        vertexBuffer.flip();
        GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, VBO);
        GL20.glBufferData(GL20.GL_ARRAY_BUFFER, vertexBuffer, GL20.GL_STATIC_DRAW);
        MemoryUtil.memFree(vertexBuffer);

        bufferQuads(numStars);
        this.numIndices = 6 * numStars;

        // 0 = Position
        GL20.glVertexAttribPointer(0, 3, GL20.GL_FLOAT, false, stride, 0);
        GL20.glEnableVertexAttribArray(0);
        // 1 = UV RGB
        GL30.glVertexAttribIPointer(1, 1, GL20.GL_INT, stride, 3 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);
    }

    private int chooseColor(Random random, float brightness) {
        return (int)((210 + random.nextInt(16) + random.nextInt(16) + random.nextInt(16)) * brightness) & 255;
    }

    @Override
    protected int getVertexBufferStride() {
        // Vertex buffer is not made of floats as this func expects
        throw new UnsupportedOperationException();
    }
}
