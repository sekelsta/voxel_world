#version 330 core
out vec4 fragColor;

in vec2 uv;

uniform sampler2D color_sampler;
uniform sampler2D depth_sampler;
uniform float near;
uniform float far;

void main()
{
    float z = texture(depth_sampler, uv).r;
    float distance = (2 * far * near) / (far + near - z * (far - near));
    vec3 color = texture(color_sampler, uv).rgb;
    color *= 0.2;
    color += distance / far * 0.8;
    fragColor = vec4(color, 1);
}
