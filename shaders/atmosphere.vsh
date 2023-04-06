#version 330 core
layout (location = 0) in vec2 position;
layout (location = 1) in vec3 ray_start_in;
layout (location = 2) in vec3 ray_end_in;

out vec2 uv;
out vec3 ray_start;
out vec3 ray_end;

void main()
{
    uv = (position.xy + 1) / 2;
    ray_start = ray_start_in;
    ray_end = ray_end_in;
    gl_Position = vec4(position.x, position.y, 0.0, 1.0);
}
