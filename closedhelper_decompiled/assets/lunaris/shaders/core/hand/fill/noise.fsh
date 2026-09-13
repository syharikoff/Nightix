#version 150
in vec2 texCoord0;
out vec4 fragColor;
uniform sampler2D Sampler0;
layout(std140) uniform ShaderFogData {
    vec2 uResolution; vec2 uCameraDir; vec3 uColor; float uTime;
    float uAlpha; float uSpeed; float uScale; float uIntensity; float uFov;
};
float hash(vec2 p){ return fract(sin(dot(p,vec2(41.7,289.3))) * 43758.5453); }
float noise(vec2 p){ vec2 i=floor(p),f=fract(p); f=f*f*(3.0-2.0*f); return mix(mix(hash(i),hash(i+vec2(1,0)),f.x),mix(hash(i+vec2(0,1)),hash(i+1.0),f.x),f.y); }
void main() {
    vec4 base=texture(Sampler0,texCoord0); if(base.a<0.1) discard;
    vec2 p=gl_FragCoord.xy/max(uResolution,vec2(1.0))*uScale+vec2(uTime*uSpeed*0.3,-uTime*uSpeed*0.2);
    float n=noise(p)+noise(p*2.03)*0.5+noise(p*4.11)*0.25;
    float ridge=pow(1.0-abs(fract(n*2.0)-0.5)*2.0,3.0);
    vec3 col=mix(uColor,uColor+vec3(0.55),ridge)*(0.22+n*0.35+uIntensity*8.0*ridge);
    fragColor=vec4(col,uAlpha*base.a);
}
