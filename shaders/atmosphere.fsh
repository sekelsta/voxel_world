#version 330 core

#define PI 3.1415926538

out vec4 fragColor;

in vec2 uv;
in vec3 ray_start;
in vec3 ray_end;

uniform sampler2D color_sampler;
uniform sampler2D depth_sampler;
uniform float near;
uniform float far;
uniform vec3 planet_center;
uniform vec3 sun_pos;

uniform float planet_radius;
uniform float atmosphere_radius;
uniform vec3 solar_spectrum;
uniform vec3 ozone_absorption;
uniform float rayleigh_height;
uniform vec3 rayleigh_scattering;
uniform float mie_height;
uniform float mie_scattering;
uniform float mie_mean_cosine;

struct Intersection {
    float t0;
    float t1;
};

Intersection compute_intersection(vec3 to_center, vec3 direction, float radius) {
    float l = length(to_center);
    float ray_to_middle = dot(direction, to_center);
    float center_to_middle_squared = l * l - ray_to_middle * ray_to_middle;
    Intersection intersection;
    intersection.t0 = 0;
    intersection.t1 = 0;
    float radius_squared = radius * radius;
    if (center_to_middle_squared < radius_squared) {
        float d = sqrt(radius_squared - center_to_middle_squared);
        if (ray_to_middle > 0 && l > radius) {
            intersection.t0 = ray_to_middle - d;
        }
        intersection.t1 = ray_to_middle + d;
    }
    return intersection;
}

void main()
{
    vec3 view_dir = ray_end - ray_start;
    float ray_length = length(view_dir);
    view_dir /= ray_length;

    float z = texture(depth_sampler, uv).r;
    float distance = ray_length * 2 * near / (far + near - z * (far - near));
    vec3 background_color = texture(color_sampler, uv).rgb;

    vec3 sun_dir = normalize(sun_pos - ray_start);
    float mu = dot(view_dir, sun_dir);
    float rayleigh_phase = 3 / (16 * PI) * (1 + mu * mu);
    float g = mie_mean_cosine;
    float mie_phase = 3 / (8 * PI) * (1 - g * g) * (1 + mu * mu) / (2 + g * g) / pow(1 + g * g - 2 * g * mu, 1.5);

    Intersection intersection = compute_intersection(planet_center - ray_start, view_dir, atmosphere_radius);
    float tMin = intersection.t0;
    float tMax = intersection.t1;
    if (distance > 0.95 * far) {
        distance = 1.0 / 0;
    }
    tMax = min(tMax, distance);

    int num_samples = 5;
    int num_light_samples = 2;
    float step = (tMax - tMin) / num_samples;
    float tCurrent = tMin + 0.5 * step;
    float rayleigh_optical_depth = 0;
    float mie_optical_depth = 0;
    vec3 rayleigh_sum = vec3(0, 0, 0);
    vec3 mie_sum = vec3(0, 0, 0);
    for (int i = 0; i < num_samples; ++i) {
        vec3 sample_pos = ray_start + view_dir * tCurrent;
        vec3 sample_sun_dir = normalize(sun_pos - sample_pos);
        float height = length(sample_pos - planet_center) - planet_radius;
        float hr = exp(-height / rayleigh_height) * step;
        float hm = exp(-height / mie_height) * step;
        rayleigh_optical_depth += hr;
        mie_optical_depth += hm;
        Intersection light_intersection = compute_intersection(planet_center - sample_pos, sample_sun_dir, atmosphere_radius);
        float light_step = (light_intersection.t1 - light_intersection.t0) / num_light_samples;
        float t_light = light_intersection.t0 + 0.5 * light_step;
        float rayleigh_light_optical_depth = 0;
        float mie_light_optical_depth = 0;
        for (int j = 0; j < num_light_samples; ++j) {
            vec3 light_sample_pos = sample_pos + sample_sun_dir * t_light;
            float light_height = length(light_sample_pos - planet_center) - planet_radius;
            rayleigh_light_optical_depth += exp(-light_height / rayleigh_height) * light_step;
            mie_light_optical_depth += exp(-light_height / mie_height) * light_step;
            t_light += light_step;
        }
        vec3 attenuation = rayleigh_scattering * (rayleigh_optical_depth + rayleigh_light_optical_depth)
            + mie_scattering * 1.1 * (mie_optical_depth + mie_light_optical_depth)
            + ozone_absorption * (i * step + light_intersection.t1 - light_intersection.t0);
        attenuation = exp(-attenuation);
        rayleigh_sum += attenuation * hr;
        mie_sum += attenuation * hm;
        tCurrent += step;
    }

    vec3 light_color = rayleigh_sum * rayleigh_scattering * rayleigh_phase + mie_sum * mie_scattering * mie_phase;
    light_color *= solar_spectrum;
    light_color = vec3(pow(light_color.r, 0.3), pow(light_color.g, 0.3), pow(light_color.b, 0.3));
    light_color = max(light_color, 0);
    light_color = min(light_color, 1);
    vec3 extinction = rayleigh_scattering * rayleigh_optical_depth
        + mie_scattering * 1.1 * mie_optical_depth + ozone_absorption * (tMax - tMin);
    extinction = exp(-extinction);
    vec3 final_color = light_color + extinction * background_color;
    fragColor = vec4(final_color, 1);
}
