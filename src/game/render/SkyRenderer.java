package sekelsta.game.render;

import java.util.Random;

import sekelsta.engine.render.*;
import sekelsta.engine.render.mesh.RigidMesh;
import sekelsta.tools.ObjParser;

public class SkyRenderer {
    private final float SAFETY_FACTOR = 0.999f;

    private final StarMesh stars = new StarMesh(new Random(0));
    private final Texture starTexture = new Texture("star.png");

    public void renderBackground(MatrixStack matrixStack, float viewDistance) {
        matrixStack.push();
        matrixStack.center();
        matrixStack.scale(viewDistance * SAFETY_FACTOR);
        starTexture.bind();
        stars.render();
        matrixStack.pop();
    }

    public void clean() {
        stars.clean();
        starTexture.clean();
    }
}
