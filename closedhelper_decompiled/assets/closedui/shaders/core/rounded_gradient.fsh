#version 330

layout(std140) uniform GradConfig {
    vec2 Size;
    vec4 Radius;
    vec4 ColorTL;
    vec4 ColorTR;
    vec4 ColorBR;
    vec4 ColorBL;
    float GlobalAlpha;
};

in vec2 localUv;

out vec4 fragColor;

float roundedBoxSDF(vec2 p, vec2 halfSize, float r) {
    vec2 q = abs(p) - halfSize + vec2(r);
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

float selectRadius(vec2 p) {
    if (p.x < 0.0) {
        return p.y < 0.0 ? Radius.x : Radius.w;
    } else {
        return p.y < 0.0 ? Radius.y : Radius.z;
    }
}

void main() {
    vec2 halfSize = Size * 0.5;
    vec2 p = localUv * Size - halfSize;
    float aa = max(fwidth(length(p)), 1.0);

    float r = selectRadius(p);
    float dist = roundedBoxSDF(p, halfSize, r);
    float rectAlpha = 1.0 - smoothstep(-aa, aa, dist);

    vec2 uv = localUv;
    vec4 topColor = mix(ColorTL, ColorTR, uv.x);
    vec4 bottomColor = mix(ColorBL, ColorBR, uv.x);
    vec4 gradColor = mix(topColor, bottomColor, uv.y);

    float alpha = rectAlpha * gradColor.a * GlobalAlpha;
    if (alpha < 0.002) discard;

    fragColor = vec4(gradColor.rgb, alpha);
}