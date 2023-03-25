package sekelsta.game.render.terrain;

import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import sekelsta.engine.render.mesh.Mesh;
import sekelsta.tools.ModelData;
import sekelsta.tools.Vertex;

public class TerrainMesh {
    protected int VAO;
    protected int VBO;
    private int intVBO;
    private int numVertices;

    public TerrainMesh(ModelData model) {
        // vertex array object
        VAO = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(VAO);
        // Vertex Buffer Object
        this.VBO = GL20.glGenBuffers();

        ArrayList<Vertex> vertexList = new ArrayList<>();
        for (int[] face : model.faces) {
            for (int f : face) {
                vertexList.add(model.getVertices().get(f));
            }
        }
        this.numVertices = vertexList.size();
        float[] vertices = Mesh.buildVertices(vertexList, getVertexBufferStride());
        Mesh.bufferVertexData(vertices, VBO);
        Mesh.enablePositionNormalTexture(getVertexBufferStride() * Float.BYTES);

        this.intVBO = GL20.glGenBuffers();

        int[] data = new int[2 * vertexList.size()];
        for (int i = 0; i < vertexList.size(); ++i) {
            data[2*i] = getColor(vertexList.get(i).type);
        }
        for (int i = 0; i < vertexList.size() / 3; ++i) {
            int a = getTextureIndex(vertexList.get(3*i));
            int b = getTextureIndex(vertexList.get(3*i + 1));
            int c = getTextureIndex(vertexList.get(3*i + 2));
            int t = (a << 24) | (b << 16) | (c << 8);
            data[6*i + 1] = t | 4;
            data[6*i + 3] = t | 2;
            data[6*i + 5] = t | 1;
        }

        IntBuffer typeBuffer = MemoryUtil.memAllocInt(data.length);
        typeBuffer.put(data).flip();
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

    private int getColor(short type) {
        int r = 255;
        int g = 255;
        int b = 255;
        switch (type) {
            case 1: // Stone (old rgb: 168, 165, 153)
                break;
            case 2: // Dirt (old rgb: 91, 70, 54)
                break;
            case 3: // Sand (old rgb: 216, 186, 136)
                break;
            case 4: // Grass
                r = 55;
                g = 121;
                b = 11;
                break;
            case 5: // Snow (old rgb: 239, 243, 242)
                break;
            case 6: // Sandstone
                break;
        }
        return (r << 16) | (g << 8) | b;
    }

    private int getTextureIndex(Vertex v) {
        // OpenGL 3.3 only guarantees 256 texture array layers
        assert(v.type < 256);
        int tex = 0;
        switch (v.type) {
            case 1: // Stone
                tex = 0;
                break;
            case 2: // Dirt
                tex = 1;
                break;
            case 3: // Sand
                tex = 2;
                break;
            case 4: // Grass
                tex = v.facesUpwardsOrDownwards()? 3 : 4;
                break;
            case 5: // Snow (old rgb: 239, 243, 242)
                tex = 5;
                break;
            case 6: // Sandstone
                tex = 6;
                break;
        }
        return tex;
    }
}
