#version 330 core
layout (location = 0) in vec2 position;

out vec2 uv;

void main()
{
    uv = (position.xy + 1) / 2;
    gl_Position = vec4(position.x, position.y, 0.0, 1.0);
}
