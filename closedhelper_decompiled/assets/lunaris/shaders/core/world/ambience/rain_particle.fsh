#version 330

#moj_import <minecraft:dynamictransforms.glsl>

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

void main() {
    vec2 p = texCoord0 - vec2(0.5);
    float core = 1.0 - smoothstep(0.09, 0.28, abs(p.x));
    float glow = (1.0 - smoothstep(0.22, 0.50, abs(p.x))) * 0.22;
    float tip = smoothstep(0.0, 0.12, texCoord0.y)
              * (1.0 - smoothstep(0.84, 1.0, texCoord0.y));
    float alpha = (core * 0.70 + glow) * tip * vertexColor.a;
    if (alpha < 0.01) discard;
    vec3 color = mix(vertexColor.rgb * 0.82, vec3(0.88, 0.95, 1.0), core * 0.32);
    fragColor = vec4(color, alpha) * ColorModulator;
}
