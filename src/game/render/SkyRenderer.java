package sekelsta.game.render;

import java.util.Random;

import shadowfox.render.*;
import shadowfox.render.mesh.RigidMesh;
import sekelsta.game.World;
import shadowfox.tools.ObjParser;
import shadowfox.math.Matrix4f;
import shadowfox.math.Vector3f;

public class SkyRenderer {
    private final float SAFETY_FACTOR = 0.999f;

    private static final float planet_radius = 6360000;
    private static final float atmosphere_radius = 6420000;
    public static final Vector3f solar_spectrum = new Vector3f(1.6f, 1.8f, 2f);
    // Eyeballed off a chart, then scaled to just barely make a noticable difference
    private static final Vector3f ozone_absorption = new Vector3f(1f, 3f, 0.2f).scale(0.0000005f);
    private static final float rayleigh_height = 8000;
    private static final Vector3f rayleigh_scattering = new Vector3f(0.0000058f, 0.0000135f, 0.0000331f);
    private static final float mie_height = 1500;
    private static final float mie_scattering = 0.0002f;

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
        moonScale *= 4;
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
        sunRadius *= 4;

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

    public static void setAtmosphericParams(ShaderProgram atmosphereShader) {
        atmosphereShader.setFloat("planet_radius", planet_radius);
        atmosphereShader.setFloat("atmosphere_radius", atmosphere_radius);
        atmosphereShader.setUniform("solar_spectrum", solar_spectrum);
        atmosphereShader.setUniform("ozone_absorption", ozone_absorption);
        atmosphereShader.setFloat("rayleigh_height", rayleigh_height);
        atmosphereShader.setUniform("rayleigh_scattering", rayleigh_scattering);
        atmosphereShader.setFloat("mie_height", mie_height);
        // Ref used 0.0021f but that felt too large.
        atmosphereShader.setFloat("mie_scattering", mie_scattering);
        atmosphereShader.setFloat("mie_mean_cosine", 0.76f);
    }

    public Vector3f getSunColor(Vector3f camera_pos, Vector3f sun_pos) {
        // Only care about the case of looking towards the sun
        Vector3f view_dir = Vector3f.subtract(sun_pos, camera_pos, new Vector3f()).normalize();
        Vector3f planet_center = new Vector3f(camera_pos.x, camera_pos.y, -1 * planet_radius);
        float tMax = computeIntersection(planet_center.z - camera_pos.z, view_dir, atmosphere_radius);

        int num_samples = 16;
        float step = tMax / num_samples;
        float t = 0.5f * step;
        float rayleigh_optical_depth = 0;
        float mie_optical_depth = 0;
        for (int i = 0; i < num_samples; ++i) {
            Vector3f sample_pos = new Vector3f(camera_pos.x + view_dir.x * t, camera_pos.y + view_dir.y * t, camera_pos.z + view_dir.z * t);
            float height = Vector3f.subtract(sample_pos, planet_center, new Vector3f()).length() - planet_radius;
            rayleigh_optical_depth += (float)Math.exp(-height / rayleigh_height) * step;
            mie_optical_depth += (float)Math.exp(-height / mie_height) * step;
            t += step;
        }

        Vector3f attenuation = new Vector3f();
        attenuation.x = rayleigh_scattering.x * rayleigh_optical_depth 
            + mie_scattering * 1.1f * mie_optical_depth + ozone_absorption.x * tMax;
        attenuation.x = (float)Math.exp(-attenuation.x);
        attenuation.y = rayleigh_scattering.y * rayleigh_optical_depth 
            + mie_scattering * 1.1f * mie_optical_depth + ozone_absorption.y * tMax;
        attenuation.y = (float)Math.exp(-attenuation.y);
        attenuation.z = rayleigh_scattering.z * rayleigh_optical_depth 
            + mie_scattering * 1.1f * mie_optical_depth + ozone_absorption.z * tMax;
        attenuation.z = (float)Math.exp(-attenuation.z);

        Vector3f light_color = new Vector3f(attenuation.x * solar_spectrum.x, attenuation.y * solar_spectrum.y, attenuation.z * solar_spectrum.z);
        return light_color;
    }

    // Assumes the start point is within the sphere, returns distance at the end point
    private float computeIntersection(float z, Vector3f direction, float radius) {
        float l = Math.abs(z);
        float ray_to_middle = z * direction.z;
        float center_to_middle_squared = l * l - ray_to_middle * ray_to_middle;
        float radius_squared = radius * radius;
        if (center_to_middle_squared < radius_squared) {
            float d = (float)Math.sqrt(radius_squared - center_to_middle_squared);
            return ray_to_middle + d;
        }
        // No intersection
        return 0;
    }

    public void clean() {
        stars.clean();
        starTexture.clean();
    }
}
