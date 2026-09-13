#version 150
in vec2 texCoord0;
out vec4 fragColor;
uniform sampler2D Sampler0;
layout(std140) uniform ShaderFogData {
    vec2 uResolution; vec2 uCameraDir; vec3 uColor; float uTime;
    float uAlpha; float uSpeed; float uScale; float uIntensity; float uFov;
};
void main() {
    vec4 base = texture(Sampler0, texCoord0); if (base.a < 0.1) discard;
    vec2 p = (gl_FragCoord.xy / max(uResolution,vec2(1.0)) - 0.5) * uScale;
    float t = uTime * uSpeed;
    float c = abs(sin(p.x * 4.0 + sin(p.y * 5.0 + t)) + sin(p.y * 4.5 - t * 0.8));
    c = pow(clamp(1.0 - c * 0.45, 0.0, 1.0), 5.0);
    fragColor = vec4(uColor * (0.16 + c * (1.5 + uIntensity * 18.0)), uAlpha * base.a);
}
