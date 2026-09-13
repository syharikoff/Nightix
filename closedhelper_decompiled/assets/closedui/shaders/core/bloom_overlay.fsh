#version 330

uniform sampler2D BloomSampler;

layout(std140) uniform BloomConfig {
    float BloomIntensity;
};

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 col = texture(BloomSampler, texCoord);
    fragColor = vec4(col.rgb * BloomIntensity, col.a);
}
