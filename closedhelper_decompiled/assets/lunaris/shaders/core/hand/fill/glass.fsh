#version 150
in vec2 texCoord0;
out vec4 fragColor;
uniform sampler2D Sampler0;
layout(std140) uniform ShaderFogData {
    vec2 uResolution; vec2 uCameraDir; vec3 uColor; float uTime;
    float uAlpha; float uSpeed; float uScale; float uIntensity; float uFov;
};
void main() {
    vec4 base=texture(Sampler0,texCoord0); if(base.a<0.1) discard;
    vec2 uv=gl_FragCoord.xy/max(uResolution,vec2(1.0)); float t=uTime*uSpeed;
    float streak=pow(0.5+0.5*sin((uv.x+uv.y)*uScale*4.0+t),12.0);
    float edge=pow(abs(uv.x-0.5)*2.0,3.0)+pow(abs(uv.y-0.5)*2.0,3.0);
    vec3 col=uColor*(0.25+base.rgb*0.22)+vec3(1.0)*streak*(0.6+uIntensity*12.0)+uColor*edge*0.35;
    fragColor=vec4(col,clamp((uAlpha*0.55+streak*0.3)*base.a,0.0,1.0));
}
