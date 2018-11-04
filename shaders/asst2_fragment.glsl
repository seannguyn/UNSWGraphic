uniform vec4 input_color;
uniform mat4 view_matrix;

// Global light properties
uniform vec3 ambientIntensity;

struct DirLight {
    vec3 intensity;
    vec3 direction;
};

struct Sunlight {
    vec3 intensity;
    vec3 direction;

    float time;
    float daytime;
    float nighttime;
};
uniform Sunlight sunlight;

struct Spotlight {
    // spotlight properties
    vec3 intensity;
    vec3 position;
    vec3 direction;

    // inner and outer cut off
    float phi;
    float gamma;

    // attenuation coefficients
    float constant;
    float linear;
    float quadratic;
};
uniform Spotlight spot;

struct Pointlight {
    vec3 intensity;
    vec3 position;

    // attenuation coefficients
    float constant;
    float linear;
    float quadratic;
};
//uniform Pointlight point;

// Material properties
uniform vec3 ambientCoeff;
uniform vec3 diffuseCoeff;
uniform vec3 specularCoeff;
uniform float phongExp;

uniform sampler2D tex;
uniform bool useTexture;

in vec4 viewPosition;
in vec2 texCoordFrag;
in vec3 m; // Interpolated normal

out vec4 outputColor;

#define M_PI 3.1415926535897932384626433832795
void calcSunlight(inout vec3 diffuse, inout vec3 specular, vec3 n, vec3 v);
void calcSpotlight(inout vec3 diffuse, inout vec3 specular, vec3 n, vec3 v);

void main()
{
    // Interpolated normal from the vertex shader
    vec3 n = normalize(m);

    // Vector from position to the viewer
    vec3 v = normalize(-viewPosition.xyz);

    // Define the three components for total light intensity
    vec3 ambient, diffuse, specular;

    ambient  = ambientIntensity * ambientCoeff;

    // Calculate the intensity contributed by light sources
    calcSunlight(diffuse, specular, n, v);
    calcSpotlight(diffuse, specular, n, v);

    if (useTexture)
        outputColor = vec4(specular, 1) +
                      vec4(ambient + diffuse, 1) * input_color *
                      texture(tex, texCoordFrag);
    else
        outputColor = vec4(ambient + diffuse + specular, 1) * input_color;
}


void calcSunlight(inout vec3 diffuse, inout vec3 specular, vec3 n, vec3 v)
{
    vec3 s = normalize(view_matrix * vec4(sunlight.direction,0)).xyz;
    vec3 r = normalize(reflect(-s,n));

    // add a bit of night cause light coming horizontally can still light scene
    float dusk = sunlight.daytime + (sunlight.nighttime / 3.0);

    vec3 intensity = vec3(0);
    if (sunlight.time < dusk) {
        // adjust color of the sunlight to reflect time of day
        float red   = sunlight.intensity.r;
        float green = red - (red * sunlight.time / (3.0 * sunlight.daytime));
        float blue  = red - (red * sunlight.time / (2.5 * sunlight.daytime));
        intensity = vec3(red, green, blue);
    }

    diffuse  = max(intensity * diffuseCoeff * dot(n,s), 0.0);
    specular = vec3(0);

    // Only show specular reflections for the front face
    if (dot(n,s) > 0)
        specular = max(sunlight.intensity * specularCoeff * pow(dot(r,v), phongExp), 0.0);
}


/*
 * For reference: https://learnopengl.com/Lighting/Light-casters
 */
void calcSpotlight(inout vec3 diffuse, inout vec3 specular, vec3 n, vec3 v)
{
    if (length(spot.direction) == 0) return;

    // Get the position of the spotlight in view coordinates
    vec4 lightPos = view_matrix * vec4(spot.position,1);

    // Vector from fragment to light source
    vec3 s = normalize(lightPos - viewPosition).xyz;

    // Angle between vector s and where the light is pointing
    vec3 lightDir = normalize(view_matrix * vec4(spot.direction,0)).xyz;
    float theta   = dot(s, -lightDir);

    // distance attenuation
    float distance    = length(lightPos - viewPosition);
    float attenuation = 1.0 / (spot.constant + spot.linear * distance +
                    spot.quadratic * (distance * distance));

    // spread attenuation
    float epsilon  = spot.phi - spot.gamma; // difference between cosine of inner and outer cut off
    vec3 intensity = clamp((theta - spot.gamma) / epsilon, 0.0, 1.0) * spot.intensity;

    // If fragment outside of light cone, intensity will be negative, so 0
    diffuse += max(intensity * attenuation * diffuseCoeff * dot(n,s), 0.0);
    if (dot(n,s) > 0) {
        vec3 r = normalize(reflect(-s,n));
        specular += max(intensity * attenuation * specularCoeff * pow(dot(r,v), phongExp), 0.0);
    }
}
