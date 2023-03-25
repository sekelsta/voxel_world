#version 330 core
layout (location = 0) in vec3 position;
layout (location = 1) in vec3 in_normal;
layout (location = 2) in vec2 in_texture;
layout (location = 3) in uint in_color;
layout (location = 4) in uint types;

uniform mat4 modelview;
uniform mat4 projection;
uniform mat3 normal_transform;

out vec3 normal;
out vec2 texture_coord;
out vec3 frag_pos;
out vec3 color;
flat out ivec3 texture_indices;
out vec3 type_weights;

void main()
{
    normal = normalize(normal_transform * in_normal);
    texture_coord = in_texture;
    gl_Position = projection * modelview * vec4(position, 1.0);
    frag_pos = vec3(modelview * vec4(position, 1.0));
    // Unused: high byte of in_color
    uint r = (in_color >> 16) & 255u;
    uint g = (in_color >> 8) & 255u;
    uint b = in_color & 0xffu;
    color = vec3(r / 255.0, g / 255.0, b / 255.0);
    uint ai = (types >> 24) & 255u;
    uint bi = (types >> 16) & 255u;
    uint ci = (types >> 8) & 255u;
    texture_indices = ivec3(int(ai), int(bi), int(ci));
    uint aw = (types >> 2) & 1u;
    uint bw = (types >> 1) & 1u;
    uint cw = types & 1u;
    type_weights = vec3(float(aw), float(bw), float(cw));
}
