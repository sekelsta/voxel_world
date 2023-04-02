package sekelsta.engine.render.mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Scanner;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import sekelsta.tools.ModelData;
import sekelsta.tools.ObjParser;

public class RigidMesh extends Mesh {
    private static final String OBJ_LOCATION = "/assets/obj/";

    public RigidMesh(ModelData data) {
        float[] vertices = buildVertices(data);
        int[] faces = buildFaces(data);
        init(vertices, faces);
    }

    public RigidMesh(String name) {
        this(ObjParser.parse(new Scanner(RigidMesh.class.getResourceAsStream(OBJ_LOCATION + name))));
    }

    public RigidMesh(float[] vertices, int[] faces) {
        init(vertices, faces);
    }

    @Override
    protected int getVertexBufferStride() {
        // 3D vertex, 3D normal, 2D texture
        return 3 + 3 + 2;
    }

    private void init(float[] vertices, int[] faces) {
        bufferVertexData(vertices);
        bufferFaceElements(faces);
        enablePositionNormalTexture();
    }
}
