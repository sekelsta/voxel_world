package sekelsta.game.render;

import java.util.Scanner;

import sekelsta.engine.render.mesh.RigidMesh;
import sekelsta.tools.ObjParser;

public class Meshes {
    private static Meshes instance;

    private final RigidMesh CUBE;

    private Meshes() {
        CUBE = new RigidMesh(
            ObjParser.parse(
                new Scanner(Meshes.class.getResourceAsStream("/assets/obj/cube.obj"))
            )
        );
    }

    private static Meshes getInstance() {
        if (instance == null) {
            instance = new Meshes();
        }
        return instance;
    }

    public static RigidMesh cube() {
        return getInstance().CUBE;
    }
}
