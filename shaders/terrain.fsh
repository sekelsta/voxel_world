#version 330 core
out vec4 fragColor;

in vec3 normal;
in vec2 texture_coord;
in vec3 frag_pos;
in vec3 color;
flat in ivec3 texture_indices;
in vec3 type_weights;

uniform vec3 light_pos;

uniform sampler2DArray texture_sampler;
uniform sampler2DArray emission_sampler;

uniform float reflectance;
// Range: 0 < scattering <= 1
uniform float scattering = 0.05;
uniform float shininess = 16;

void main()
{
    vec3 light_dir = normalize(light_pos - frag_pos);
    float diffuse_str = max(dot(normal, light_dir), 0.0);

    float specular_str = 0;
    if (dot(normal, light_dir) > 0) {
        // in view space the view pos is at the origin
        vec3 view_dir = normalize(-frag_pos);
        vec3 halfway_dir = normalize(light_dir + view_dir);

        specular_str = pow(max(dot(normal, halfway_dir), 0.0), shininess) * reflectance
                         * min(1.0, dot(normal, light_dir) / scattering);
    }

    float ambient_str = 0.1;
    vec4 tex_a = texture(texture_sampler, vec3(texture_coord, texture_indices.x));
    vec4 tex_b = texture(texture_sampler, vec3(texture_coord, texture_indices.y));
    vec4 tex_c = texture(texture_sampler, vec3(texture_coord, texture_indices.z));
    vec4 tex_color = tex_a * type_weights.x + tex_b * type_weights.y + tex_c * type_weights.z;
    vec4 em_a = texture(emission_sampler, vec3(texture_coord, texture_indices.x));
    vec4 em_b = texture(emission_sampler, vec3(texture_coord, texture_indices.y));
    vec4 em_c = texture(emission_sampler, vec3(texture_coord, texture_indices.z));
    vec4 emissive = em_a * type_weights.x + em_b * type_weights.y + em_c * type_weights.z;

    float alpha = tex_color.a + emissive.a;
    if (alpha < 0.01) {
        discard;
    }
    tex_color.rgb *= color.rgb;
    vec3 lit = tex_color.rgb * (ambient_str + diffuse_str) + vec3(1, 1, 1) * specular_str;
    // OpenGL automatically clamps color components to the range [0, 1]
    fragColor = vec4(tex_color.a * lit + emissive.rgb, alpha);
}
