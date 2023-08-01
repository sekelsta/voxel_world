package sekelsta.game.render.terrain;

import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import shadowfox.render.mesh.Mesh;

public class TerrainMesh {
    protected int VAO;
    protected int VBO;
    private int intVBO;
    private int numVertices;

    public TerrainMesh(TerrainMeshData meshData) {
        this.numVertices = meshData.numVertices;

        // vertex array object
        VAO = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(VAO);
        // Vertex Buffer Object
        this.VBO = GL20.glGenBuffers();


        Mesh.bufferVertexData(meshData.vertices, VBO);
        Mesh.enablePositionNormalTexture(getVertexBufferStride() * Float.BYTES);

        this.intVBO = GL20.glGenBuffers();

        IntBuffer typeBuffer = MemoryUtil.memAllocInt(meshData.data.length);
        typeBuffer.put(meshData.data).flip();
        GL20.glBindBuffer(GL20.GL_ARRAY_BUFFER, intVBO);
        GL20.glBufferData(GL20.GL_ARRAY_BUFFER, typeBuffer, GL20.GL_STATIC_DRAW);
        MemoryUtil.memFree(typeBuffer);

        // 3 = color
        GL30.glVertexAttribIPointer(3, 1, GL20.GL_INT, 2 * Integer.BYTES, 0);
        GL20.glEnableVertexAttribArray(3);

        // 4 = type weights
        GL30.glVertexAttribIPointer(4, 1, GL20.GL_INT, 2 * Integer.BYTES, 1 * Integer.BYTES);
        GL20.glEnableVertexAttribArray(4);
    }

    protected int getVertexBufferStride() {
        return vertexBufferStride();
    }

    public static int vertexBufferStride() {
        // 3D vertex, 3D normal, 2D texture
        return 3 + 3 + 2;
    }

    // Calling function is responsible for setting the shader
    public void render() {
        GL30.glBindVertexArray(VAO);
        GL20.glDrawArrays(GL20.GL_TRIANGLES, 0, this.numVertices);
        GL30.glBindVertexArray(0);
    }

    public void clean() {
        GL30.glBindVertexArray(0);
        GL20.glDeleteBuffers(VBO);
        GL30.glDeleteVertexArrays(VAO);
        VAO = 0;
        VBO = 0;
        numVertices = 0;
    }
}
