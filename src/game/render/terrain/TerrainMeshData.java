package sekelsta.game.render.terrain;

import java.util.ArrayList;

import shadowfox.render.mesh.Mesh;
import shadowfox.tools.Vertex;

public class TerrainMeshData {
    public final int numVertices;
    public final float[] vertices;
    public final int[] data;

    public TerrainMeshData(ArrayList<Vertex> vertexList) {
        this.numVertices = vertexList.size();
        this.vertices = Mesh.buildVertices(vertexList, TerrainMesh.vertexBufferStride());

        this.data = new int[2 * vertexList.size()];
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
