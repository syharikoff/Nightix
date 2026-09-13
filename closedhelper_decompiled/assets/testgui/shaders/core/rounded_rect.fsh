#version 150

in vec2 fragCoord;
out vec4 fragColor;

layout(std140) uniform RectConfig {
    vec2 size;          // width, height of actual inner box
    vec2 quadSize;      // expanded mesh size (with shadow margin)
    vec2 margin;        // margin offset
    vec4 radius;        // top-left, top-right, bottom-right, bottom-left
    vec4 fillColor;     // rgba
    vec4 borderColor;   // rgba
    float borderWidth;  // border width in px
    vec4 shadowColor;   // rgba
    vec2 shadowOffset;  // ox, oy
    float shadowBlur;   // blur radius
    float alpha;        // global alpha
};

float sdRoundedBox(vec2 p, vec2 b, vec4 r) {
    r.xy = (p.x > 0.0) ? r.yz : r.xw;
    float rad = (p.y > 0.0) ? r.y : r.x;
    vec2 q = abs(p) - b + rad;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - rad;
}

void main() {
    vec2 pos = fragCoord * quadSize - margin;
    vec2 halfSize = size * 0.5;
    vec2 p = pos - halfSize;

    // Box SDF
    float dist = sdRoundedBox(p, halfSize, radius);

    // Anti-aliasing width
    float aa = fwidth(dist) * 0.7;
    if (aa <= 0.0) aa = 1.0;

    // Fill mask
    float fillAlpha = 1.0 - smoothstep(-aa, 0.0, dist);

    // Border mask
    float borderAlpha = 0.0;
    if (borderWidth > 0.0) {
        float innerDist = dist + borderWidth;
        borderAlpha = (1.0 - smoothstep(-aa, 0.0, dist)) * smoothstep(-aa, 0.0, innerDist);
    }

    // Shadow SDF
    float shadowAlpha = 0.0;
    if (shadowColor.a > 0.0 && shadowBlur > 0.0) {
        vec2 sp = pos - shadowOffset - halfSize;
        float sDist = sdRoundedBox(sp, halfSize, radius);
        shadowAlpha = 1.0 - smoothstep(-shadowBlur, shadowBlur, sDist);
    }

    // Color compositing
    vec4 col = shadowColor * (shadowAlpha * shadowColor.a);
    vec4 fill = vec4(fillColor.rgb, fillColor.a * fillAlpha);
    vec4 border = vec4(borderColor.rgb, borderColor.a * borderAlpha);

    col = mix(col, fill, fillAlpha);
    if (borderWidth > 0.0) {
        col = mix(col, border, borderAlpha);
    }

    col.a *= alpha;
    if (col.a <= 0.001) {
        discard;
    }

    fragColor = col;
}
