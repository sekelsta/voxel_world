package sekelsta.game.render.terrain;

import java.util.*;
import sekelsta.tools.ModelData;
import sekelsta.tools.Vertex;
import shadowfox.math.Vector3f;

public class TerrainModelData extends ModelData {
    List<Vertex[]> outOfBoundsFaces = new ArrayList<>();

    int trimmedVertexLength = -1;
    int trimmedFaceLength = -1;
    int trimmedQuadLength = -1;

    @Override
    public Vertex getNearest(Vector3f position) {
        throw new RuntimeException("Not implemented");
    }

    public void addBoundedTriangle(Vertex v0, Vertex v1, Vertex v2, boolean inBounds) {
        assert(v0 != null);
        assert(v1 != null);
        assert(v2 != null);
        if (inBounds) {
            super.addTriangle(v0, v1, v2);
        }
        else {
            Vertex[] face = {v0, v1, v2};
            outOfBoundsFaces.add(face);
            connectivity = null;
        }
    }

    private void addOutOfBoundsQuad(Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        Vertex[] face = {v0, v1, v2, v3};
        outOfBoundsFaces.add(face);
        connectivity = null;
    }

    public void addBoundedQuad(Vertex v0, Vertex v1, Vertex v2, Vertex v3, boolean inBounds) {
        assert(v0 != null);
        assert(v1 != null);
        assert(v2 != null);
        assert(v3 != null);
        if (inBounds) {
            super.addQuad(v0, v1, v2, v3);
        }
        else {
            addOutOfBoundsQuad(v0, v1, v2, v3);
        }
    }

    public void addTrim() {
        this.trimmedVertexLength = vertices.size();
        this.trimmedFaceLength = faces.size();
        this.trimmedQuadLength = quads.size();
        for (Vertex[] face : outOfBoundsFaces) {
            if (face.length == 3) {
                super.addTriangle(face[0], face[1], face[2]);
            }
            else {
                assert(face.length == 4);
                super.addQuad(face[0], face[1], face[2], face[3]);
            }
        }
        outOfBoundsFaces = null;
    }

    public void trim() {
        faces = faces.subList(0, trimmedFaceLength);
        quads = quads.subList(0, trimmedQuadLength);

        for (int i = trimmedVertexLength; i < vertices.size(); ++i) {
            vertexMap.remove(vertices.get(i));
        }
        vertices = vertices.subList(0, trimmedVertexLength);
    }
}
