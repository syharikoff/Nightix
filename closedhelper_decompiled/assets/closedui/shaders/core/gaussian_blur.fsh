#version 330

uniform sampler2D InSampler;

layout(std140) uniform BlurConfig {
    vec2 InSize;
    vec2 Direction;
    float Radius;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec2 texel = (Direction * Radius) / InSize;

    // Continuous multi-tap Gaussian with hardware bilinear filtering.
    // Taps are placed at sub-texel offsets with overlapping linear interpolation footprints.
    // Eliminates all discrete sampling grid and checkerboard artifacts.
    vec4 sum = texture(InSampler, texCoord) * 0.19648255;

    sum += (texture(InSampler, texCoord + texel * 1.41176470) + texture(InSampler, texCoord - texel * 1.41176470)) * 0.29690696;
    sum += (texture(InSampler, texCoord + texel * 3.29411765) + texture(InSampler, texCoord - texel * 3.29411765)) * 0.09447039;
    sum += (texture(InSampler, texCoord + texel * 5.17647059) + texture(InSampler, texCoord - texel * 5.17647059)) * 0.01038136;

    fragColor = sum;
}