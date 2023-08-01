package sekelsta.game.render;

import java.nio.ByteBuffer;
import java.util.Random;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import shadowfox.render.mesh.Mesh;
import shadowfox.math.Matrix4f;
import shadowfox.math.Vector3f;
import shadowfox.math.Vector4f;

public class AtmosphereMesh extends Mesh {
    private static AtmosphereMesh instance = null;

    private float[] vertices = {
        -1, 1, 0, 0, 0, 0, 0, 0,
        1, 1, 0, 0, 0, 0, 0, 0,
        -1, -1, 0, 0, 0, 0, 0, 0,
        1, -1, 0, 0, 0, 0, 0, 0};

    public static AtmosphereMesh getInstance() {
        if (instance == null) {
            instance = new AtmosphereMesh();
        }
        return instance;
    }

    private AtmosphereMesh() {
        this.numIndices = 6;
        bufferVertexData(vertices);
        bufferQuads(numIndices);

        int stride = getVertexBufferStride() * Float.BYTES;
        // 0 = position
        GL20.glVertexAttribPointer(0, 2, GL20.GL_FLOAT, false, stride, 0);
        GL20.glEnableVertexAttribArray(0);
        // 1 = ray_start
        GL20.glVertexAttribPointer(1, 3, GL20.GL_FLOAT, false, stride, 2 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);
        // 2 = ray_end
        GL20.glVertexAttribPointer(2, 3, GL20.GL_FLOAT, false, stride, 5 * Float.BYTES);
        GL20.glEnableVertexAttribArray(2);
    }

    @Override
    public void render() {
        throw new UnsupportedOperationException();
    }

    public void render(Matrix4f modelview, Matrix4f perspective) {
        GL30.glBindVertexArray(VAO);
        Matrix4f.mul(perspective, modelview, modelview);
        modelview.invert();
        Vector4f[] corners = {
            new Vector4f(-1, 1, -1, 1), new Vector4f(-1, 1, 1, 1),
            new Vector4f(1, 1, -1, 1), new Vector4f(1, 1, 1, 1),
            new Vector4f(-1, -1, -1, 1), new Vector4f(-1, -1, 1, 1),
            new Vector4f(1, -1, -1, 1), new Vector4f(1, -1, 1, 1)};
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 2; ++j) {
                Vector3f v = modelview.transform(corners[2 * i + j]).toVec3();
                int index = 2 + 8 * i + 3 * j;
                vertices[index] = v.x;
                vertices[index + 1] = v.y;
                vertices[index + 2] = v.z;
            }
        }
        bufferVertexData(vertices);
        GL20.glDrawElements(GL20.GL_TRIANGLES, this.numIndices, GL20.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }

    @Override
    public void clean() {
        super.clean();
        assert(this == instance);
        instance = null;
    }

    @Override
    public int getVertexBufferStride() {
        // Position, ray start, ray end
        return 2 + 3 + 3;
    }
}
