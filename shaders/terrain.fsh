#version 330 core
out vec4 fragColor;

in vec3 normal;
in vec2 texture_coord;
in vec3 frag_pos;
in vec3 color;
flat in ivec3 texture_indices;
in vec3 type_weights;

struct Light {
    vec3 position;
    vec3 color;
};

const int MAX_LIGHTS = 2;
uniform Light lights[MAX_LIGHTS];

uniform sampler2DArray texture_sampler;

uniform float reflectance;
// Range: 0 < scattering <= 1
uniform float scattering = 0.05;
uniform float shininess = 16;

void main()
{
    vec4 tex_a = texture(texture_sampler, vec3(texture_coord, texture_indices.x));
    vec4 tex_b = texture(texture_sampler, vec3(texture_coord, texture_indices.y));
    vec4 tex_c = texture(texture_sampler, vec3(texture_coord, texture_indices.z));
    vec4 merged = tex_a * type_weights.x + tex_b * type_weights.y + tex_c * type_weights.z;
    merged.rgb *= color;
    vec3 tex_color = vec3(pow(merged.r, 2.2), pow(merged.g, 2.2), pow(merged.b, 2.2));

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

    vec3 lit = tex_color.rgb * total_diffuse + total_specular;
    // OpenGL automatically clamps color components to the range [0, 1]
    fragColor = vec4(pow(lit.r, 1/2.2), pow(lit.g, 1/2.2), pow(lit.b, 1/2.2), 1.0);
}
