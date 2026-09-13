#version 330

#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

in vec3 Position;
in vec2 UV0;

out vec2 localUv;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    localUv = UV0;
}