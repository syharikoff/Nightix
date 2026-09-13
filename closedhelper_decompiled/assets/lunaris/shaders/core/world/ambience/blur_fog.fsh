#version 150

in vec2 texCoord;
out vec4 fragColor;

uniform sampler2D SceneSampler;
uniform sampler2D DepthSampler;

layout(std140) uniform BlurFogData {
    vec2 uResolution;
    float uTime;
    float uBlurIntensity;

    vec3 uTintColor;
    float uUseTint;

    float uStartDist;
    float uEndDist;
    float uAlpha;
    float uPad0;

    mat4 uProj;
    mat4 uView;
    mat4 uInvProj;
    mat4 uInvView;
    vec3 uCameraPos;
    float uPad1;
};

vec3 getViewPos(vec2 uv, float depth) {
    float z = (depth >= 0.9999) ? 0.998 : clamp(depth * 2.0 - 1.0, -1.0, 0.9999);
    vec4 ndc = vec4(uv * 2.0 - 1.0, z, 1.0);
    vec4 viewPos = uInvProj * ndc;
    return viewPos.xyz / viewPos.w;
}


vec3 sampleGaussianBlur(vec2 uv, float intensity) {
    vec2 texel = (1.0 / max(uResolution, vec2(1.0))) * (intensity * 2.5 + 0.5);
    vec3 col = vec3(0.0);
    float totalWeight = 0.0;

    for (int x = -2; x <= 2; x++) {
        for (int y = -2; y <= 2; y++) {
            vec2 offset = vec2(float(x), float(y)) * texel;
            float weight = exp(-0.5 * float(x*x + y*y));
            col += texture(SceneSampler, clamp(uv + offset, vec2(0.001), vec2(0.999))).rgb * weight;
            totalWeight += weight;
        }
    }
    return col / totalWeight;
}

void main() {
    vec2 uv = gl_FragCoord.xy / uResolution;
    float depth = texture(DepthSampler, uv).r;

    vec3 viewPos = getViewPos(uv, depth);
    float distToPoint = length(viewPos);

    float fogMask = smoothstep(uStartDist, uEndDist, distToPoint) * uAlpha;

    if (fogMask <= 0.001) {
        discard;
    }

    vec3 blurredColor = sampleGaussianBlur(uv, uBlurIntensity);

    if (uUseTint > 0.5) {
        blurredColor = mix(blurredColor, uTintColor, 0.5);
    }

    fragColor = vec4(blurredColor, clamp(fogMask, 0.0, 1.0));
}

