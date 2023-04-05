package sekelsta.game.render;

import java.nio.ByteBuffer;
import java.util.Random;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import sekelsta.engine.render.mesh.Mesh;
import shadowfox.math.Vector3f;

public class AtmosphereMesh extends Mesh {
    private static AtmosphereMesh instance = null;

    public static AtmosphereMesh getInstance() {
        if (instance == null) {
            instance = new AtmosphereMesh();
        }
        return instance;
    }

    private AtmosphereMesh() {
        this.numIndices = 6;
        float[] vertices = {-1, 1, 1, 1, -1, -1, 1, -1};
        bufferVertexData(vertices);
        bufferQuads(numIndices);

        // 0 = position
        GL20.glVertexAttribPointer(0, 2, GL20.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(0);
    }

    @Override
    public void clean() {
        super.clean();
        assert(this == instance);
        instance = null;
    }

    @Override
    public int getVertexBufferStride() {
        return 2;
    }
}
