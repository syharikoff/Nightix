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
    vec2 uv = gl_FragCoord.xy / max(uResolution, vec2(1.0));
    float wave = sin((uv.x + uv.y) * uScale * 3.0 + uTime * uSpeed * 4.0) * 0.5 + 0.5;
    float caustic = pow(abs(sin(uv.x * uScale * 7.0 + sin(uv.y * 12.0 + uTime * uSpeed))), 5.0);
    vec3 color = uColor * (0.18 + wave * 0.5) + vec3(0.12,0.45,1.0) * caustic * (0.5 + uIntensity * 10.0);
    fragColor = vec4(color, uAlpha * base.a);
}
