#version 330 core
out vec4 fragColor;

in vec2 texture_coord;
in vec3 color;

uniform sampler2D texture_sampler;

void main()
{
    vec4 tex_color = texture(texture_sampler, texture_coord);
    if (tex_color.a < 0.01) {
        discard;
    }

    fragColor = vec4(color.r * tex_color.r, color.g * tex_color.g, color.b * tex_color.b, tex_color.a);
}
