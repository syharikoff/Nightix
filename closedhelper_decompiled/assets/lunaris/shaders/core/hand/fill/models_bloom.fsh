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
    vec2 p=(gl_FragCoord.xy/max(uResolution,vec2(1.0))-0.5)*uScale; float t=uTime*uSpeed;
    float lines=0.0;
    for(int i=0;i<4;i++){ float y=(float(i)-1.5)*0.45+sin(p.x*1.7+t+float(i))*0.35; float d=abs(p.y-y); lines+=exp(-8.0*d)+exp(-70.0*d)*2.0; }
    vec3 col=uColor*(0.04+lines*(0.75+uIntensity*16.0));
    fragColor=vec4(col,uAlpha*base.a);
}
