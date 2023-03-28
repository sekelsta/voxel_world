package sekelsta.game.render;

import java.util.Scanner;

import sekelsta.engine.render.*;
import sekelsta.engine.render.mesh.RigidMesh;
import sekelsta.tools.ObjParser;

public class SkyRenderer {
    private final float SAFETY_FACTOR = 0.999f;
    private final RigidMesh skyball = new RigidMesh(
        ObjParser.parse(
            new Scanner(Renderer.class.getResourceAsStream("/assets/obj/skyball.obj"))
        )
    );
    private final Texture skyTexture = new Texture("skyball.png");

    public void renderBackground(MatrixStack matrixStack, float viewDistance) {
        matrixStack.push();
        matrixStack.center();
        matrixStack.scale(viewDistance * SAFETY_FACTOR);
        Textures.TRANSPARENT.bind();
        skyTexture.bindEmission();
        skyball.render();
        matrixStack.pop();
    }
}
