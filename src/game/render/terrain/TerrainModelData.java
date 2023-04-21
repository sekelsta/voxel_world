package sekelsta.game.render.terrain;

import java.util.*;
import sekelsta.tools.Vertex;
import shadowfox.math.Vector2f;
import shadowfox.math.Vector3f;

public class TerrainModelData {
    protected HashMap<Vertex, Integer> vertexMap = new HashMap<>();
    protected List<Vertex> vertices = new ArrayList<>();
    public List<int[]> faces = new ArrayList<>();
    public List<int[]> quads = new ArrayList<>();
    protected Map<Vertex, Set<Vertex>> connectivity = null;

    private List<Vertex[]> outOfBoundsFaces = new ArrayList<>();

    public List<Vector2f[]> uvTriOverrides = new ArrayList<>();
    public List<Vector2f[]> uvQuadOverrides = new ArrayList<>();

    int trimmedVertexLength = -1;
    int trimmedFaceLength = -1;
    int trimmedQuadLength = -1;

    public List<Vertex> getVertices() {
        return vertices;
    }

    public void calcNormals() {
        // Initialize all normals
        for (Vertex vertex : vertices) {
            vertex.normal = new Vector3f(0, 0, 0);
        }
        // For each face, calculate surface normal and add to each vertex
        addPartialNormals(faces);
        addPartialNormals(quads);
        // Normalize all normals
        for (Vertex vertex : vertices) {
            vertex.normal.normalize();
        }
    }

    private void addPartialNormals(List<int[]> faces) {
        for (int[] face : faces) {
            Vector3f prev = vertices.get(face[face.length - 1]).position;
            Vector3f pos = vertices.get(face[0]).position;
            Vector3f a = Vector3f.subtract(pos, prev, new Vector3f());
            a.normalize();
            
            for (int i = 0; i < face.length; ++i) {
                Vector3f next = vertices.get(face[(i + 1) % face.length]).position;
                Vector3f b = Vector3f.subtract(next, pos, new Vector3f());
                b.normalize();
                Vector3f normal = Vector3f.cross(a, b, new Vector3f());
                // Weight by angle
                float w = (float)Math.acos(b.dot(a));
                vertices.get(face[i]).normal.addWeighted(normal, w);

                prev = pos;
                pos = next;
                a = b;
            }
        }
    }

    // Triangulate plus override UVs
    public ArrayList<Vertex> finalizeToVertexList() {
        ArrayList<Vertex> vertexList = new ArrayList<>();
        for (int i = 0; i < faces.size(); ++i) {
            int[] face = faces.get(i);
            assert(face.length == 3);
            for (int f : face) {
                vertexList.add(vertices.get(f));
            }
            if (i < uvTriOverrides.size() && uvTriOverrides.get(i) != null) {
                int size = vertexList.size();
                for (int j = 0; j < 3; ++j) {
                    vertexList.set(size - 3 + j, copyUV(vertexList.get(size - 3 + j), uvTriOverrides.get(i)[j]));
                }
            }
        }

        for (int i = 0; i < quads.size(); ++i) {
            int[] face = quads.get(i);
            assert(face.length == 4);
            Vertex v0 = vertices.get(face[0]);
            Vertex v1 = vertices.get(face[1]);
            Vertex v2 = vertices.get(face[2]);
            Vertex v3 = vertices.get(face[3]);
            if (i < uvQuadOverrides.size() && uvQuadOverrides.get(i) != null) {
                v0 = copyUV(v0, uvQuadOverrides.get(i)[0]);
                v1 = copyUV(v1, uvQuadOverrides.get(i)[1]);
                v2 = copyUV(v2, uvQuadOverrides.get(i)[2]);
                v3 = copyUV(v3, uvQuadOverrides.get(i)[3]);
            }

            float distance02 = Vector3f.subtract(v0.position, v2.position, new Vector3f()).length();
            float distance13 = Vector3f.subtract(v1.position, v3.position, new Vector3f()).length();
            if (distance02 < distance13) {
                vertexList.add(v0);
                vertexList.add(v1);
                vertexList.add(v2);
                vertexList.add(v2);
                vertexList.add(v3);
                vertexList.add(v0);
            }
            else {
                vertexList.add(v3);
                vertexList.add(v0);
                vertexList.add(v1);
                vertexList.add(v1);
                vertexList.add(v2);
                vertexList.add(v3);
            }
        }

        return vertexList;
    }

    private Vertex copyUV(Vertex v, Vector2f uv) {
        Vertex n = new Vertex();
        n.position = v.position;
        n.normal = v.normal;
        n.texture = uv;
        n.type = v.type;
        return n;
    }

    private void calculateConnectivity() {
        connectivity = new HashMap<>();
        addPartialConnectivity(faces);
        addPartialConnectivity(quads);
    }

    private void addPartialConnectivity(List<int[]> faces) {
        for (int[] face : faces) {
            int prev = face.length - 1;
            for (int f = 0; f < face.length; ++f) {
                Vertex prevVertex = vertices.get(face[prev]);
                Vertex currentVertex = vertices.get(face[f]);
                connectivity.computeIfAbsent(prevVertex, (k) -> new HashSet<Vertex>());
                connectivity.computeIfAbsent(currentVertex, (k) -> new HashSet<Vertex>());
                connectivity.get(prevVertex).add(currentVertex);
                connectivity.get(currentVertex).add(prevVertex);
                prev = f;
            }
        }
    }

    public void contract(List<Vector3f> originalLocations, float maxDistance, float weight) {
        if (connectivity == null) {
            calculateConnectivity();
        }

        List<Vector3f> newPositions = new ArrayList<>();
        for (int i = 0; i < vertices.size(); ++i) {
            Set<Vertex> connected = connectivity.get(vertices.get(i));
            Vector3f average = new Vector3f(vertices.get(i).position);
            average.scale(weight);
            for (Vertex v : connected) {
                average.add(v.position);
            }
            average.scale(1f/(connected.size() + weight));

            Vector3f diff = Vector3f.subtract(average, originalLocations.get(i), new Vector3f());
            if (diff.length() > maxDistance) {
                diff.normalize().scale(maxDistance);
                average = Vector3f.add(originalLocations.get(i), diff, average);
            }

            newPositions.add(average);
        }
        for (int i = 0; i < vertices.size(); ++i) {
            vertices.get(i).position.x = newPositions.get(i).x;
            vertices.get(i).position.y = newPositions.get(i).y;
            vertices.get(i).position.z = newPositions.get(i).z;
        }
    }

    protected int addVertex(Vertex v) {
        Integer i = vertexMap.get(v);
        if (i != null) {
            return i;
        }
        int l = vertices.size();
        vertexMap.put(v, l);
        vertices.add(v);
        return l;
    }

    public void addTriangle(Vertex v0, Vertex v1, Vertex v2) {
        int[] face = new int[3];
        face[0] = addVertex(v0);
        face[1] = addVertex(v1);
        face[2] = addVertex(v2);
        faces.add(face);
        connectivity = null;
    }

    public void addQuad(Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        int[] face = new int[4];
        face[0] = addVertex(v0);
        face[1] = addVertex(v1);
        face[2] = addVertex(v2);
        face[3] = addVertex(v3);
        quads.add(face);
        connectivity = null;
    }

    public void addBoundedTriangle(Vertex v0, Vertex v1, Vertex v2, boolean inBounds,
            Vector2f uv0, Vector2f uv1, Vector2f uv2) {
        assert(v0 != null);
        assert(v1 != null);
        assert(v2 != null);
        if (inBounds) {
            addTriangle(v0, v1, v2);
            Vector2f[] uvs = {uv0, uv1, uv2};
            uvTriOverrides.add(uvs);
        }
        else {
            Vertex[] face = {v0, v1, v2};
            outOfBoundsFaces.add(face);
            connectivity = null;
            // It'll get trimmed so we won't care about the UV values
        }
    }

    private void addOutOfBoundsQuad(Vertex v0, Vertex v1, Vertex v2, Vertex v3) {
        Vertex[] face = {v0, v1, v2, v3};
        outOfBoundsFaces.add(face);
        connectivity = null;
    }

    public void addBoundedQuad(Vertex v0, Vertex v1, Vertex v2, Vertex v3, boolean inBounds,
            Vector2f uv0, Vector2f uv1, Vector2f uv2, Vector2f uv3) {
        assert(v0 != null);
        assert(v1 != null);
        assert(v2 != null);
        assert(v3 != null);

        if (inBounds) {
            addQuad(v0, v1, v2, v3);
            Vector2f[] uvs = {uv0, uv1, uv2, uv3};
            uvQuadOverrides.add(uvs);
        }
        else {
            addOutOfBoundsQuad(v0, v1, v2, v3);
            // It'll get trimmed so we won't care about the UV values
        }
    }

    public void addTrim() {
        this.trimmedVertexLength = vertices.size();
        this.trimmedFaceLength = faces.size();
        this.trimmedQuadLength = quads.size();
        for (Vertex[] face : outOfBoundsFaces) {
            if (face.length == 3) {
                addTriangle(face[0], face[1], face[2]);
            }
            else {
                assert(face.length == 4);
                addQuad(face[0], face[1], face[2], face[3]);
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
