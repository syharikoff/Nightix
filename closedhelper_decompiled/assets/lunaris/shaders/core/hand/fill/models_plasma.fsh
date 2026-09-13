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
    float v=sin(p.x*2.0+t)+sin(p.y*2.4-t*0.7)+sin((p.x+p.y)*1.5+t*0.4);
    v=0.5+0.5*sin(v*2.2+t); vec3 alt=vec3(uColor.b,uColor.r,uColor.g);
    vec3 col=mix(uColor,alt,v)*(0.35+v*(1.1+uIntensity*15.0));
    fragColor=vec4(col,uAlpha*base.a);
}
