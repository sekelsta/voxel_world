#version 330 core
out vec4 fragColor;

in vec3 normal;
in vec2 texture_coord;
in vec3 frag_pos;

struct Light {
    vec3 position;
    vec3 color;
};

const int MAX_LIGHTS = 2;
uniform Light lights[MAX_LIGHTS];

uniform sampler2D texture_sampler;
uniform sampler2D emission_sampler;

uniform float reflectance;
// Range: 0 < scattering <= 1
uniform float scattering = 0.05;
uniform float shininess = 16;

void main()
{
    vec4 color = texture(texture_sampler, texture_coord);
    color.r = pow(color.r, 2.2);
    color.g = pow(color.g, 2.2);
    color.b = pow(color.b, 2.2);
    vec4 emissive = texture(emission_sampler, texture_coord);
    emissive.r = pow(emissive.r, 2.2);
    emissive.g = pow(emissive.g, 2.2);
    emissive.b = pow(emissive.b, 2.2);
    float alpha = color.a + emissive.a;
    if (alpha < 0.01) {
        discard;
    }

    vec3 total_diffuse = vec3(0, 0, 0);
    vec3 total_specular = vec3(0, 0, 0);
    for (int i = 0; i < MAX_LIGHTS; ++i) {
        vec3 light_dir = normalize(lights[i].position - frag_pos);
        float diffuse_str = max(dot(normal, light_dir), 0.0);

        float specular_str = 0;
        if (dot(normal, light_dir) > 0) {
            // in view space the view pos is at the origin
            vec3 view_dir = normalize(-frag_pos);
            vec3 halfway_dir = normalize(light_dir + view_dir);

            specular_str = pow(max(dot(normal, halfway_dir), 0.0), shininess) * reflectance
                             * min(1.0, dot(normal, light_dir) / scattering);
        }

        total_diffuse += diffuse_str * lights[i].color;
        total_specular += specular_str * lights[i].color;
    }

    vec3 lit = color.rgb * total_diffuse + total_specular;

    fragColor = vec4(color.a * lit + emissive.rgb, alpha);
    fragColor.r = pow(fragColor.r, 1/2.2);
    fragColor.g = pow(fragColor.g, 1/2.2);
    fragColor.b = pow(fragColor.b, 1/2.2);
}
