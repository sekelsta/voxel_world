package sekelsta.game.render;

import java.util.Random;

import sekelsta.engine.render.*;
import sekelsta.engine.render.mesh.RigidMesh;
import sekelsta.game.World;
import sekelsta.tools.ObjParser;
import shadowfox.math.Matrix4f;
import shadowfox.math.Vector3f;

public class SkyRenderer {
    private final float SAFETY_FACTOR = 0.999f;

    private final World world;
    private final StarMesh stars;
    private final Texture starTexture = new Texture("star.png");
    private final RigidMesh moon = new RigidMesh("moon.obj");
    private final Texture moonTexture = new Texture("moon.png");
    private final Texture sunTexture = starTexture;

    private final float[] quadVertices = {
        // Position, normal, texture
        1, 0, 1, 0, -1, 0, 1, 1,
        -1, 0, 1, 0, -1, 0, 0, 1,
        -1, 0, -1, 0, -1, 0, 0, 0,
        1, 0, -1, 0, -1, 0, 1, 0};
    private final int[] quadFaces = {0, 1, 2, 2, 3, 0};
    private final RigidMesh quadMesh = new RigidMesh(quadVertices, quadFaces);

    public SkyRenderer(World world) {
        this.world = world;
        this.stars = new StarMesh(new Random(world.getSeed()));
    }

    public void renderStars(MatrixStack matrixStack, float lerp, float viewDistance) {
        matrixStack.push();
        matrixStack.center();
        matrixStack.scale(viewDistance * SAFETY_FACTOR);
        float starRotation = (float)Math.PI * 2 * world.getPlanetaryRotation(lerp);
        matrixStack.rotate(starRotation, 0, -1, 0);
        starTexture.bind();
        stars.render();
        matrixStack.pop();
    }

    public void renderMoon(MatrixStack matrixStack, float lerp, float viewDistance) {
        matrixStack.push();
        matrixStack.center();
        // Half a degree on the celestial sphere
        float moonScale = (float)Math.sin(Math.PI / 360);
        // Artificially make it look bigger
        moonScale *= 2;
        matrixStack.transform(new Matrix4f(world.getMoonRotation(lerp)));
        matrixStack.scale(moonScale * viewDistance * SAFETY_FACTOR);
        matrixStack.translate(0, 0, -1 / moonScale);
        moonTexture.bind();
        Textures.BLACK.bindEmission();
        moon.render();
        matrixStack.pop();
    }

    public void renderSun(MatrixStack matrixStack, float lerp, float viewDistance) {
        Vector3f lightPos = world.getSunPosition(lerp);
        // Roughly 1% of the real-life radius, 700,000 km
        float sunRadius = 7000000;
        // Make bigger to fit the texture's extra glow
        sunRadius *= 512f / 212;
        // Artificially make it look bigger
        sunRadius *= 2;

        float sunDistance = lightPos.length();
        float scale = viewDistance * SAFETY_FACTOR / sunDistance;

        matrixStack.push();
        matrixStack.center();
        matrixStack.translate(lightPos.x * scale, lightPos.y * scale, lightPos.z * scale);
        matrixStack.scale(sunRadius * scale);
        matrixStack.billboard();
        Textures.TRANSPARENT.bind();
        sunTexture.bindEmission();
        quadMesh.render();
        matrixStack.pop();
    }

    public void clean() {
        stars.clean();
        starTexture.clean();
    }
}
