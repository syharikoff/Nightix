#version 150

in vec2 Position;
in vec2 UV0;

out vec2 fragCoord;

layout(std140) uniform Projection {
    mat4 ProjMat;
};

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
};

void main() {
    fragCoord = UV0;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 0.0, 1.0);
}
