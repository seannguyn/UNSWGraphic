// Incoming vertex position
in vec3 position;

// Incoming normal
//in vec3 normal;

// Incoming texture coordinate
//in vec2 texCoord;

//uniform mat4 model_matrix;
uniform mat4 view_matrix;
uniform mat4 proj_matrix;

out vec3 texCoordFrag;

void main(void)
{
    // The position in camera coordinates
    vec4 viewPosition = view_matrix * vec4(position, 1);

    // The position in CVV coordinates
    gl_Position = proj_matrix * viewPosition;

    // Use 3d vector to get cube map coordinates
    texCoordFrag = position;
}