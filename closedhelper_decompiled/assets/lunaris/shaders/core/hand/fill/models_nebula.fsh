#version 150
in vec2 texCoord0;
out vec4 fragColor;
uniform sampler2D Sampler0;
layout(std140) uniform ShaderFogData {
    vec2 uResolution; vec2 uCameraDir; vec3 uColor; float uTime;
    float uAlpha; float uSpeed; float uScale; float uIntensity; float uFov;
};
float hash(vec2 p){ return fract(sin(dot(p,vec2(127.1,311.7))) * 43758.5453); }
float noise(vec2 p){ vec2 i=floor(p),f=fract(p); f=f*f*(3.0-2.0*f); return mix(mix(hash(i),hash(i+vec2(1,0)),f.x),mix(hash(i+vec2(0,1)),hash(i+1.0),f.x),f.y); }
void main() {
    vec4 base=texture(Sampler0,texCoord0); if(base.a<0.1) discard;
    vec2 p=gl_FragCoord.xy/max(uResolution,vec2(1.0))*uScale*0.8;
    float t=uTime*uSpeed*0.18; float n=noise(p+vec2(t,-t)); n+=noise(p*2.1-vec2(t))*0.5;
    vec3 pink=vec3(1.0,0.08,0.55); vec3 col=mix(uColor*0.08,pink,n*n)+uColor*n*(0.6+uIntensity*8.0);
    float star=step(0.992,hash(floor(p*35.0)))*(0.5+0.5*sin(uTime*4.0));
    fragColor=vec4(col+star,uAlpha*base.a);
}
