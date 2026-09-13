#version 330

#moj_import <minecraft:dynamictransforms.glsl>

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

void main() {
    vec2 p = texCoord0 * 2.0 - 1.0;
    float radius = length(p);
    float disc = 1.0 - smoothstep(0.20, 0.82, radius);
    float arms = 1.0 - smoothstep(0.04, 0.18, min(abs(p.x), abs(p.y)));
    vec2 diagonal = vec2(p.x + p.y, p.x - p.y) * 0.7071;
    arms = max(arms, 1.0 - smoothstep(0.035, 0.15, min(abs(diagonal.x), abs(diagonal.y))));
    float edge = 1.0 - smoothstep(0.62, 1.0, radius);
    float shape = max(disc * 0.68, arms * edge * 0.62);
    float alpha = shape * vertexColor.a;
    if (alpha < 0.01) discard;
    fragColor = vec4(vertexColor.rgb, alpha) * ColorModulator;
}
