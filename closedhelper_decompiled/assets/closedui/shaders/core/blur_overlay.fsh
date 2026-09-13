#version 330

uniform sampler2D BlurSampler;
uniform sampler2D MainSampler;

layout(std140) uniform CompositeConfig {
    float MixAmount;
    float DimAmount;
    vec4 BoxRect;
    vec2 BoxRadius;
};

in vec2 texCoord;

out vec4 fragColor;

float roundedBoxMask(vec2 uv, vec4 box, vec2 r) {
    vec2 center = (box.xy + box.zw) * 0.5;
    vec2 halfSize = (box.zw - box.xy) * 0.5;
    vec2 p = abs(uv - center) - halfSize + r;
    float dist = length(max(p, vec2(0.0))) + min(max(p.x, p.y), 0.0) - min(r.x, r.y);
    vec2 fw = fwidth(uv);
    float aa = max(fw.x, fw.y);
    return 1.0 - smoothstep(-aa, aa, dist);
}

void main() {
    vec3 sharp = texture(MainSampler, texCoord).rgb;
    vec3 blur  = texture(BlurSampler, texCoord).rgb;

    float mask = (BoxRect.z > BoxRect.x)
        ? roundedBoxMask(texCoord, BoxRect, BoxRadius)
        : 1.0;
    float localMix = MixAmount * mask;

    blur *= 1.0 - DimAmount * localMix;
    fragColor = vec4(mix(sharp, blur, localMix), 1.0);
}