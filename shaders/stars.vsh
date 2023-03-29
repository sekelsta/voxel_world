#version 330 core
layout (location = 0) in vec3 position;
layout (location = 1) in uint uvrgb;

uniform mat4 modelview;
uniform mat4 projection;

out vec2 texture_coord;
out vec3 color;

void main()
{
    uint u = uvrgb >> 28;
    uint v = (uvrgb >> 24) & 15u;
    texture_coord = vec2(u / 15.0, v / 15.0);
    uint red = (uvrgb >> 16) & 255u;
    uint green = (uvrgb >> 8) & 255u;
    uint blue = uvrgb & 255u;
    color = vec3(red / 255.0, green / 255.0, blue / 255.0);
    gl_Position = projection * modelview * vec4(position, 1.0);
}
