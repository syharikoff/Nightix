#version 330

uniform sampler2D Sampler0;

layout(std140) uniform TextureConfig {
    vec4 Shape;
    vec4 Tint;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec2 size = Shape.xy;
    float radius = min(Shape.z, min(size.x, size.y) * 0.5);
    vec2 point = texCoord * size;
    vec2 centered = abs(point - size * 0.5) - (size * 0.5 - vec2(radius));
    float distanceToEdge = length(max(centered, vec2(0.0)))
        + min(max(centered.x, centered.y), 0.0) - radius;
    float edgeAlpha = 1.0 - smoothstep(-0.75, 0.75, distanceToEdge);

    float blur = Shape.w;
    vec4 sampled;
    if (blur > 0.01) {
        float blockSize = max(1.0, blur * 1.5);
        vec2 blocks = max(vec2(3.0), size / blockSize);
        vec2 pUv = (floor(texCoord * blocks) + 0.5) / blocks;

        vec2 d = vec2(blur * 0.0035);
        sampled = texture(Sampler0, pUv) * 0.28
                + texture(Sampler0, pUv + vec2(d.x, 0.0)) * 0.18
                + texture(Sampler0, pUv - vec2(d.x, 0.0)) * 0.18
                + texture(Sampler0, pUv + vec2(0.0, d.y)) * 0.18
                + texture(Sampler0, pUv - vec2(0.0, d.y)) * 0.18;
    } else {
        sampled = texture(Sampler0, texCoord);
    }

    fragColor = vec4(sampled.rgb * Tint.rgb, sampled.a * Tint.a * edgeAlpha);
}
