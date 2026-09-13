#version 150

out vec4 fragColor;

uniform sampler2D DepthSampler;

layout(std140) uniform FogData {
    vec2 uResolution;
    float uTime;
    float uDensity;

    vec3 uFogColor;
    float uFogHeight;

    float uThickness;
    float uFogBaseY;
    float uPad1;
    float uPad2;

    mat4 uProj;
    mat4 uView;
    mat4 uInvProj;
    mat4 uInvView;
    vec3 uCameraPos;
    float uPad3;
};

void main() {
    vec2 uv = gl_FragCoord.xy / uResolution;
    float depth = texture(DepthSampler, uv).r;


    if (depth >= 0.9999) {
        discard;
    }

    vec4 ndc = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);

    vec4 viewPos = uInvProj * ndc;
    viewPos /= viewPos.w;

    vec3 worldRelPos = (uInvView * vec4(viewPos.xyz, 0.0)).xyz;
    vec3 worldPos = uCameraPos + worldRelPos;

    // Keep the volume attached to the terrain, not to the camera.  Centering
    // these bounds on uCameraPos.y made the whole fog slab jump with the player.
    float fogMinY = uFogBaseY - 1.5;
    float fogMaxY = uFogBaseY + uFogHeight;

    vec3 rayDir = worldPos - uCameraPos;
    float totalDist = length(rayDir);

    if (totalDist <= 0.0001) {
        discard;
    }

    vec3 dir = rayDir / totalDist;
    float fogDistance = totalDist;

    if (abs(dir.y) > 0.00001) {
        float t1 = (fogMinY - uCameraPos.y) / dir.y;
        float t2 = (fogMaxY - uCameraPos.y) / dir.y;

        float tMin = max(0.0, min(t1, t2));
        float tMax = min(totalDist, max(t1, t2));

        fogDistance = max(0.0, tMax - tMin);
    } else {
        if (uCameraPos.y < fogMinY || uCameraPos.y > fogMaxY) {
            fogDistance = 0.0;
        }
    }

    float clearRadius = mix(25.0, 0.0, clamp(uThickness, 0.0, 1.0));
    fogDistance = max(0.0, fogDistance - clearRadius);

    if (fogDistance <= 0.001) {
        discard;
    }

    float opticalDensity = fogDistance * uDensity * 0.12;
    float fogFactor = clamp(1.0 - exp(-opticalDensity), 0.0, 1.0);

    fragColor = vec4(uFogColor, fogFactor);
}
