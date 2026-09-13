#version 150

in vec2 texCoord;
in vec2 texelSize;
out vec4 fragColor;
uniform sampler2D SceneSampler;
uniform sampler2D MaskSampler;

layout(std140) uniform HandOutlineData {
    vec4 resolution;
    vec4 outlineColor;
    vec4 params;
};

float sampleMask(vec2 uv) {
    return texture(MaskSampler, clamp(uv, vec2(0.0), vec2(1.0))).r;
}

void main() {
    vec4 scene = texture(SceneSampler, texCoord);
    float centerMask = sampleMask(texCoord);
    float width = max(0.5, params.x);
    float glowPower = max(0.1, params.y);
    int mode = int(params.z);
    float totalWeight = 0.0;
    float accumulatedMask = 0.0;
    const float PI = 3.14159265359;
    for (int ring = 1; ring <= 3; ring++) {
        float radius = float(ring) * (width / 3.0);
        float weight = 1.0 - (float(ring) - 0.5) / 3.0;
        for (int i = 0; i < 8; i++) {
            float angle = float(i) * (2.0 * PI / 8.0);
            accumulatedMask += sampleMask(texCoord + vec2(cos(angle), sin(angle)) * texelSize * radius) * weight;
            totalWeight += weight;
        }
    }
    float avgMask = accumulatedMask / max(totalWeight, 1.0);
    float alphaFactor;
    if (mode == 1) {
        alphaFactor = pow(max(0.0, avgMask - centerMask * 0.25), 0.7) * glowPower * 2.5;
    } else if (mode == 2) {
        alphaFactor = smoothstep(0.01, 0.35, abs(centerMask - avgMask)) * glowPower * 1.6;
    } else {
        alphaFactor = abs(centerMask - avgMask) > 0.04 ? 1.0 : 0.0;
    }
    alphaFactor = clamp(alphaFactor * outlineColor.a, 0.0, 1.0);
    float topDistance = (resolution.y - gl_FragCoord.y) / max(resolution.y, 1.0);
    alphaFactor *= smoothstep(0.005, 0.04, topDistance);
    fragColor = vec4(clamp(mix(scene.rgb, outlineColor.rgb, alphaFactor), 0.0, 1.0), scene.a);
}
