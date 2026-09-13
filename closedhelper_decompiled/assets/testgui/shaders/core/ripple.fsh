#version 150

in vec2 fragCoord;
out vec4 fragColor;

layout(std140) uniform RippleConfig {
    vec2 size;          // width, height
    vec2 center;        // click origin in px
    float radius;       // corner radius of container
    float currentRadius;// current expanding wave radius in px
    float waveThickness;// width of the ripple wave ring
    vec4 waveColor;     // rgba
    float progress;     // 0.0 -> 1.0
    float alpha;        // global alpha
};

float sdRoundedBox(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + r;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

void main() {
    vec2 pos = fragCoord * size;
    vec2 halfSize = size * 0.5;
    vec2 p = pos - halfSize;

    // Bounds clipping
    float dist = sdRoundedBox(p, halfSize, radius);
    float aa = fwidth(dist) * 0.7;
    if (aa <= 0.0) aa = 1.0;
    float clipMask = 1.0 - smoothstep(-aa, 0.0, dist);

    // Distance from ripple center
    float dToCenter = length(pos - center);

    // Ripple wave shape
    float waveDist = abs(dToCenter - currentRadius);
    float waveAlpha = 1.0 - smoothstep(0.0, waveThickness, waveDist);
    
    // Fill interior of ripple gently
    float innerGlow = (1.0 - smoothstep(0.0, currentRadius, dToCenter)) * 0.35;
    float totalWave = max(waveAlpha, innerGlow);

    // Fade out over progress
    float fade = (1.0 - progress);

    vec4 col = waveColor;
    col.a *= totalWave * fade * clipMask * alpha;

    if (col.a <= 0.001) {
        discard;
    }

    fragColor = col;
}
