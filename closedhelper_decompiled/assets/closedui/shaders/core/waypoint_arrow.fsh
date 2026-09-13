#version 330

layout(std140) uniform ArrowConfig {
    vec2 Size;
    vec4 PrimaryColor;
    vec4 SecondaryColor;
    vec4 HighlightColor;
    float GlobalAlpha;
};

in vec2 localUv;

out vec4 fragColor;

float sdPolygon4(vec2 p, vec2 v0, vec2 v1, vec2 v2, vec2 v3) {
    vec2 v[4];
    v[0] = v0;
    v[1] = v1;
    v[2] = v2;
    v[3] = v3;

    float d = dot(p - v[0], p - v[0]);
    float s = 1.0;

    for (int i = 0, j = 3; i < 4; j = i++) {
        vec2 e = v[j] - v[i];
        vec2 w = p - v[i];
        vec2 b = w - e * clamp(dot(w, e) / dot(e, e), 0.0, 1.0);
        d = min(d, dot(b, b));
        bvec3 c = bvec3(p.y >= v[i].y, p.y < v[j].y, e.x * w.y > e.y * w.x);
        if (all(c) || all(not(c))) s = -s;
    }
    return s * sqrt(d);
}

void main() {
    // Map localUv (0..1) to centered range (-1.2..1.2) to allow anti-aliasing margins
    vec2 p = (localUv - 0.5) * 2.4;

    // Vector Stealth Chevron vertices
    vec2 v0 = vec2(0.0, -0.85);   // Apex
    vec2 v1 = vec2(0.65, 0.65);   // Right wing tip
    vec2 v2 = vec2(0.0, 0.22);    // Center notch
    vec2 v3 = vec2(-0.65, 0.65);  // Left wing tip

    float dist = sdPolygon4(p, v0, v1, v2, v3);

    // Dynamic pixel-accurate Anti-Aliasing
    float aa = fwidth(dist) * 1.1;
    if (aa < 0.035) aa = 0.045;

    float shapeAlpha = 1.0 - smoothstep(-aa, aa, dist);

    if (shapeAlpha <= 0.005) {
        discard;
    }

    // 3D Faceted Stealth Shading
    // Left facet: PrimaryColor
    // Right facet: SecondaryColor
    vec4 baseColor = (p.x < 0.0) ? PrimaryColor : SecondaryColor;

    // Center spine highlight
    float spineDist = abs(p.x);
    float spineGleam = (1.0 - smoothstep(0.0, 0.15, spineDist)) * 0.25;
    baseColor = mix(baseColor, HighlightColor, spineGleam);

    // Tip apex white shine
    float tipFactor = (1.0 - smoothstep(-0.85, -0.40, p.y)) * (1.0 - smoothstep(0.0, 0.22, abs(p.x)));
    vec4 finalColor = mix(baseColor, HighlightColor, tipFactor * 0.95);

    fragColor = vec4(finalColor.rgb, finalColor.a * shapeAlpha * GlobalAlpha);
}
