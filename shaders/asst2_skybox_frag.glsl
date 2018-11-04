struct Sunlight {
    vec3 intensity;
    vec3 direction;

    float time;
    float daytime;
    float nighttime;
};
uniform Sunlight sunlight;

uniform samplerCube tex;
in vec3 texCoordFrag;

out vec4 outputColor;

#define PI 3.1415926535897932384626433832795

void main(void)
{
    float period;
    vec3 intensity;

    if (sunlight.time < sunlight.daytime) {
        period = (sunlight.time / sunlight.daytime) * PI;
        intensity.r = max(sunlight.intensity.r * sin(period), 0.18);
        intensity.g = max(sunlight.intensity.g * sin(period), 0.12);
        intensity.b = max(sunlight.intensity.b * sin(period), 0.10);
    } else {
        period = ((sunlight.time - sunlight.daytime) / sunlight.nighttime) * PI;
        intensity.r = max(0.18 - sin(period), 0.1);
        intensity.g = max(0.12 - sin(period), 0.1);
        intensity.b = max(0.10 - sin(period), 0.1);
    }

    outputColor = vec4(intensity, 1.0) * texture(tex, texCoordFrag);
}