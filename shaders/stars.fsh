#version 330 core
out vec4 fragColor;

in vec2 texture_coord;
in vec3 color;

uniform float brightness;
uniform sampler2D texture_sampler;

void main()
{
    vec4 tex_color = texture(texture_sampler, texture_coord);
    if (tex_color.a < 0.01) {
        discard;
    }

    fragColor = vec4(color.rgb * tex_color.rgb * brightness, tex_color.a);
}
