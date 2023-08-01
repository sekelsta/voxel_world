package sekelsta.game.render;

import java.util.*;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;

import shadowfox.Particle;
import shadowfox.entity.Entity;
import shadowfox.render.*;
import shadowfox.render.entity.EntityRenderer;
import shadowfox.render.mesh.RigidMesh;
import sekelsta.game.Game;
import sekelsta.game.Ray;
import sekelsta.game.RaycastResult;
import sekelsta.game.World;
import sekelsta.game.render.gui.Overlay;
import sekelsta.game.render.terrain.TerrainRenderer;
import shadowfox.math.*;
import shadowfox.tools.ObjParser;

public class Renderer implements IFramebufferSizeListener {
    private TerrainRenderer terrainRenderer = null;
    private MaterialShader shader = MaterialShader.load("/shaders/basic.vsh", "/shaders/basic.fsh");
    private ShaderProgram shader2D = ShaderProgram.load("/shaders/2d.vsh", "/shaders/2d.fsh");
    private ShaderProgram fireShader = ShaderProgram.load("/shaders/fire.vsh", "/shaders/fire.fsh");
    private MaterialShader terrainShader = MaterialShader.load("/shaders/terrain.vsh", "/shaders/terrain.fsh");
    private ShaderProgram starShader = ShaderProgram.load("/shaders/stars.vsh", "/shaders/stars.fsh");
    private ShaderProgram atmosphereShader = ShaderProgram.load("/shaders/atmosphere.vsh", "/shaders/atmosphere.fsh");
    private Frustum frustum = new Frustum();
    private Matrix4f perspective = new Matrix4f();
    // Rotate from Y up (-Z forward) to Z up (+Y forward)
    private final Matrix4f coordinate_convert = new Matrix4f().rotate((float)(-1 * Math.PI/2), 1f, 0f, 0f);
    private final Matrix3f identity3f = new Matrix3f();
    private final Vector2f uiDimensions = new Vector2f(1, 1);

    private int frameWidth;
    private int frameHeight;

    private SkyRenderer skyRenderer = null;

    private final Texture circleTexture = new Texture("white_circle.png");

    private Texture colorTexture = null;
    private Texture depthTexture = null;

    private int FBO;

    private MatrixStack matrixStack = new MatrixStack() {
        @Override
        protected void onChange() {
            Matrix4f result = getResult();
            shader.setUniform("modelview", result);
            shader.setUniform("normal_transform", result.normalTransform());
            terrainShader.setUniform("modelview", result);
            terrainShader.setUniform("normal_transform", result.normalTransform());
            starShader.setUniform("modelview", result);
        }
    };

    private ParticleRenderer particleRenderer = new ParticleRenderer();

    public Renderer() {
        shader.use();
        shader.setInt("texture_sampler", 0);
        shader.setInt("emission_sampler", 1);
        shader.setDefaultMaterial();

        shader2D.use();
        shader2D.setInt("texture_sampler", 0);

        fireShader.use();
        fireShader.setInt("texture_sampler", 0);

        terrainShader.use();
        terrainShader.setInt("texture_sampler", 0);

        starShader.use();
        starShader.setInt("texture_sampler", 0);

        atmosphereShader.use();
        atmosphereShader.setInt("color_sampler", 0);
        atmosphereShader.setInt("depth_sampler", 1);
        SkyRenderer.setAtmosphericParams(atmosphereShader);

        frustum.setFOV(Math.toRadians(30));

        atmosphereShader.setFloat("near", frustum.getNear());
        atmosphereShader.setFloat("far", frustum.getFar());

        // Enable alpha blending (over)
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glClearColor(0.005f, 0.005f, 0.005f, 1f);

        FBO = GL30.glGenFramebuffers();
    }

    public void setWorld(World world) {
        if (skyRenderer != null) {
            skyRenderer.clean();
            skyRenderer = null;
        }
        if (terrainRenderer != null) {
            terrainRenderer.clean();
            terrainRenderer = null;
        }
        if (world != null) {
            skyRenderer = new SkyRenderer(world);
            if (world.getTerrain() != null) {
                terrainRenderer = new TerrainRenderer(world.getTerrain());
            }
        }
    }

    public void render(float lerp, Camera camera, Game game, Overlay overlay) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        if (camera != null) {
            renderWorld(lerp, camera, game);
        }
        renderOverlay(overlay);
    }

    private void renderWorld(float lerp, Camera camera, Game game) {
        GL30.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, FBO);
        assert(GL30.glCheckFramebufferStatus(GL30C.GL_FRAMEBUFFER) == GL30C.GL_FRAMEBUFFER_COMPLETE);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // Render world
        matrixStack.push();

        World world = game.getWorld();
        if (world.isPaused()) {
            lerp = 0;
        }

        Vector3f sunPos = world.getSunPosition(lerp);
        Vector3f moonPos = world.getMoonPosition(lerp);

        // Move to camera coords
        camera.transform(matrixStack, lerp);
        Vector3f cameraspace_sun = matrixStack.getResult().transform(new Vector4f(sunPos)).toVec3();
        Vector3f cameraspace_moon = matrixStack.getResult().transform(new Vector4f(moonPos)).toVec3();

        Vector3f sun_to_camera = new Vector3f(0, 0, 0);
        Vector3f.subtract(cameraspace_sun, sun_to_camera, sun_to_camera);
        sun_to_camera.normalize();
        Vector3f camera_to_moon = new Vector3f(0, 0, 0);
        Vector3f.subtract(camera_to_moon, cameraspace_moon, camera_to_moon);
        camera_to_moon.normalize();

        Vector3f sunColor = skyRenderer.getSunColor(new Vector3f(camera.getX(lerp), camera.getY(lerp), camera.getZ(lerp)), sunPos);
        float moonFullness = (1 + sun_to_camera.dot(camera_to_moon)) / 2;
        Vector3f moonColor = skyRenderer.getSunColor(new Vector3f(camera.getX(lerp), camera.getY(lerp), camera.getZ(lerp)), moonPos).scale(0.136f * moonFullness);

        starShader.use();
        starShader.setUniform("projection", perspective);
        float sunBrightness = (sunColor.x + sunColor.y + sunColor.z) / 3;
        starShader.setFloat("brightness", 1 / (1 + 255 * sunBrightness));
        // TO_OPTIMIZE: Sort the stars by distance instead of changing blend func
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        skyRenderer.renderStars(matrixStack, lerp, frustum.getFar());
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        terrainShader.use();
        terrainShader.setUniform("projection", perspective);
        terrainShader.setLight(0, cameraspace_sun, sunColor);
        terrainShader.setLight(1, cameraspace_moon, moonColor);
        terrainRenderer.render(matrixStack, frustum, lerp);

        shader.use();
        shader.setUniform("projection", perspective);

        shader.setLight(0, cameraspace_sun, SkyRenderer.solar_spectrum);
        shader.setLight(1, cameraspace_moon, new Vector3f(0, 0, 0));
        shader.setReflectance(0);
        skyRenderer.renderMoon(matrixStack, lerp, frustum.getFar());
        shader.setDefaultMaterial();
        shader.setUniform("lights[0].color", sunColor);
        shader.setUniform("lights[1].color", moonColor);

        // Render entities
        for (Entity entity : world.getMobs()) {
            renderEntity(entity, lerp, matrixStack, shader);
            shader.setDefaultMaterial();
        }

        // Render particles
        List<Particle> particles = world.getParticles();

        fireShader.use();
        fireShader.setUniform("projection", perspective);
        circleTexture.bind();
        particleRenderer.render(camera, matrixStack, particles, fireShader, 0.3f, lerp);

        shader.use();
        skyRenderer.renderSun(matrixStack, lerp, frustum.getFar());

        Ray ray = game.getPointerRay();
        RaycastResult hit = world.getTerrain().findHit(ray);
        if (hit != null) {
            shader.setReflectance(0);
            matrixStack.push();
            float s = 1.5f;
            matrixStack.scale(s / world.getTerrain().blockSize);
            matrixStack.translate((.5f + hit.x()) / s, (0.5f + hit.y()) / s, (0.5f + hit.z()) / s);
            enterWireframe();
            Textures.BLACK.bind();
            Textures.BLACK.bindEmission();
            Meshes.cube().render();
            exitWireframe();
            matrixStack.pop();
            shader.setDefaultMaterial();
        }

        atmosphereShader.use();
        atmosphereShader.setUniform("sun_pos", sunPos);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL30.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);
        colorTexture.bind(GL13.GL_TEXTURE0);
        depthTexture.bind(GL13.GL_TEXTURE1);
        AtmosphereMesh.getInstance().render(matrixStack.getResult(), perspective);

        matrixStack.pop();
    }

    private void renderOverlay(Overlay overlay) {
        // Set up for two-dimensional rendering
        shader2D.use();

        // This is the size of UI's canvas, so the scale is inversly proportional to actual element size
        float uiScale = overlay.getScale();
        uiDimensions.x = frameWidth * uiScale;
        uiDimensions.y = frameHeight * uiScale;
        shader2D.setUniform("dimensions", uiDimensions);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        overlay.render(uiDimensions);
    }

    @SuppressWarnings("unchecked")
    private <T extends Entity> void renderEntity(T entity, float lerp, MatrixStack matrixStack, MaterialShader shader) {
        assert(entity != null);
        assert(entity.getType() != null);
        assert(entity.getType().getRenderer() != null);
        // Unchecked cast
        EntityRenderer<? super T> renderer = (EntityRenderer<? super T>)(entity.getType().getRenderer());
        renderer.render(entity, lerp, matrixStack, shader);
    }

    public void setJointTransforms(Matrix4f[] joints) {
        for (int i = 0; i < joints.length; ++i) {
            shader.setUniform("bone_matrices[" + i + "]", joints[i]);
        }
    }

    // The length of the ray returned is the distance to the other side of the view frustum
    public Ray rayFromPointer(double xPos, double yPos, Camera camera, float lerp) {
        double xClipSpace = (xPos / frameWidth) * 2 - 1;
        double yClipSpace = -1 * ((yPos / frameHeight) * 2 - 1);
        Vector4f origin4 = new Vector4f((float)xClipSpace, (float)yClipSpace, -1, 1);
        Vector4f destination = new Vector4f((float)xClipSpace, (float)yClipSpace, 1, 1);

        MatrixStack matrixStack = new MatrixStack();
        matrixStack.push();
        camera.transform(matrixStack, lerp);
        Matrix4f matrix = matrixStack.getResult();
        matrixStack.pop();

        Matrix4f.mul(perspective, matrix, matrix);
        matrix.invert();

        matrix.transform(origin4);
        matrix.transform(destination);

        Vector3f origin = origin4.toVec3();
        return new Ray(origin, destination.toVec3().subtract(origin));
    }

    @Override
    public void windowResized(int width, int height) {
        // Ban 0 width or height
        frameWidth = Math.max(width, 1);
        frameHeight = Math.max(height, 1);

        frustum.setAspectRatio(frameWidth, frameHeight);
        frustum.calcMatrix(perspective);
        Matrix4f.mul(coordinate_convert, perspective, perspective);

        if (colorTexture != null) {
            colorTexture.clean();
        }
        if (depthTexture != null) {
            depthTexture.clean();
        }
        colorTexture = new Texture(width, height, GL11.GL_RGB, GL11.GL_RGB);
        depthTexture = new Texture(width, height, GL14C.GL_DEPTH_COMPONENT24, GL11.GL_DEPTH_COMPONENT);
        GL30.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, FBO);
        GL30.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, colorTexture.getHandle(), 0);
        GL30.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, GL30C.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, depthTexture.getHandle(), 0);
        GL30.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);
    }

    public void enterWireframe() {
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
    }

    public void exitWireframe() {
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
    }

    public void clean() {
        setWorld(null);
        terrainRenderer.clean();
        shader.delete();
        shader2D.delete();
        fireShader.delete();
        terrainShader.delete();
        particleRenderer.clean();
        starShader.delete();
        atmosphereShader.delete();
        GL30.glDeleteFramebuffers(FBO);
        colorTexture.clean();
        depthTexture.clean();
    }
}
