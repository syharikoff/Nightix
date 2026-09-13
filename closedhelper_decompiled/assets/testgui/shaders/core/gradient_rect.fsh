#version 150

in vec2 fragCoord;
out vec4 fragColor;

layout(std140) uniform GradConfig {
    vec2 size;          // width, height
    float radius;       // corner radius
    float alpha;        // global alpha
    vec4 colTL;         // top-left rgba
    vec4 colTR;         // top-right rgba
    vec4 colBR;         // bottom-right rgba
    vec4 colBL;         // bottom-left rgba
    float time;         // animation time
    float angle;        // gradient angle
};

float sdRoundedBox(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + r;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

void main() {
    vec2 pos = fragCoord * size;
    vec2 halfSize = size * 0.5;
    vec2 p = pos - halfSize;

    float dist = sdRoundedBox(p, halfSize, radius);
    float aa = fwidth(dist) * 0.7;
    if (aa <= 0.0) aa = 1.0;

    float mask = 1.0 - smoothstep(-aa, 0.0, dist);

    // Bilinear interpolation between 4 corner colors
    vec2 uv = fragCoord;
    
    // Optional rotational / animated offset
    if (time > 0.0) {
        float wave = sin(uv.x * 3.14159 + time * 2.0) * 0.1;
        uv.y = clamp(uv.y + wave, 0.0, 1.0);
    }

    vec4 top = mix(colTL, colTR, uv.x);
    vec4 bot = mix(colBL, colBR, uv.x);
    vec4 finalCol = mix(top, bot, uv.y);

    finalCol.a *= mask * alpha;
    if (finalCol.a <= 0.001) {
        discard;
    }

    fragColor = finalCol;
}
