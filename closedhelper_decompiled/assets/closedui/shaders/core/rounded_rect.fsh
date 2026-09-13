#version 330

layout(std140) uniform RectConfig {
    vec2 Size;
    vec2 QuadSize;
    vec2 ContentOffset;
    vec4 Radius;
    vec4 FillColor;
    vec4 BorderColor;
    float BorderWidth;
    vec4 ShadowColor;
    vec2 ShadowOffset;
    float ShadowBlur;
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
    vec2 localPos = localUv * QuadSize;
    vec2 p = localPos - ContentOffset - halfSize;

    bool hasShadow = ShadowColor.a > 0.0 && (ShadowBlur > 0.0 || dot(ShadowOffset, ShadowOffset) > 0.0);
    bool hasBorder = BorderWidth > 0.0;

    float aa = 0.5;
    float r = selectRadius(p);

    float dist = roundedBoxSDF(p, halfSize, r);
    float rectAlpha = clamp(0.5 - dist, 0.0, 1.0);

    float shadowAlpha = 0.0;
    if (hasShadow) {
        vec2 shadowP = p - ShadowOffset;
        float shadowDist = roundedBoxSDF(shadowP, halfSize, r);
        shadowAlpha = (1.0 - smoothstep(-aa, ShadowBlur + aa, shadowDist)) * ShadowColor.a * (1.0 - rectAlpha);
    }

    float fillMask = rectAlpha;
    float borderMask = 0.0;
    if (hasBorder) {
        vec2 innerHalfSize = max(halfSize - vec2(BorderWidth), vec2(0.0));
        float innerDist = roundedBoxSDF(p, innerHalfSize, max(r - BorderWidth, 0.0));
        float innerAlpha = clamp(0.5 - innerDist, 0.0, 1.0);
        borderMask = max(rectAlpha - innerAlpha, 0.0);
        fillMask = innerAlpha;
    }

    vec3 color = ShadowColor.rgb;
    float alpha = shadowAlpha;

    float fillA = fillMask * FillColor.a;
    if (fillA > 0.0) {
        float denom = max(alpha + fillA, 0.001);
        color = mix(color, FillColor.rgb, fillA / denom);
        alpha = alpha * (1.0 - fillA) + fillA;
    }

    if (hasBorder) {
        float borderA = borderMask * BorderColor.a;
        if (borderA > 0.0) {
            float denom = max(alpha + borderA, 0.001);
            color = mix(color, BorderColor.rgb, borderA / denom);
            alpha = alpha * (1.0 - borderA) + borderA;
        }
    }

    alpha *= GlobalAlpha;
    fragColor = vec4(color, alpha);
}