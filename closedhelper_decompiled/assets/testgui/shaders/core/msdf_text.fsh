#version 330

uniform sampler2D Sampler0;

layout(std140) uniform TextConfig {
    vec2 AtlasSize;
    float DistanceRange;
    float Weight;
    vec4 TextColor;
};

in vec2 texCoord;

out vec4 fragColor;

float median3(vec3 value) {
    return max(min(value.r, value.g), min(max(value.r, value.g), value.b));
}

void main() {
    float dist = median3(texture(Sampler0, texCoord).rgb);
    
    // Exact screen-space vector anti-aliasing
    vec2 unitRange = vec2(DistanceRange) / AtlasSize;
    vec2 screenTexSize = vec2(1.0) / max(fwidth(texCoord), vec2(0.00001));
    float screenPxDistance = max(0.5 * dot(unitRange, screenTexSize), 1.0) * (dist - 0.5 + Weight);
    float opacity = clamp(screenPxDistance + 0.5, 0.0, 1.0);
    
    if (opacity <= 0.005) discard;
    fragColor = vec4(TextColor.rgb, TextColor.a * opacity);
}