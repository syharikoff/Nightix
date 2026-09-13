#version 150

in vec2 texCoord;
out vec4 fragColor;

uniform sampler2D SceneSampler;
uniform sampler2D DepthSampler;

layout(std140) uniform PuddleData {
    vec2 uResolution;
    float uTime;
    float uScale;

    vec3 uWaveColor;
    float uDensity;

    float uUseRain;
    float uDropFrequency;
    float uUseWaves;
    float uWaveTheme;

    mat4 uProj;
    mat4 uView;
    mat4 uInvProj;
    mat4 uInvView;
    vec3 uCameraPos;
    float uPad0;
};

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash12(i), hash12(i + vec2(1.0, 0.0)), f.x),
               mix(hash12(i + vec2(0.0, 1.0)), hash12(i + vec2(1.0, 1.0)), f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 4; i++) {
        v += a * noise(p);
        p *= 2.0;
        a *= 0.5;
    }
    return v;
}

float rainRipples(vec2 worldXZ, float t) {
    vec2 p = worldXZ * (uDropFrequency * 0.6);
    vec2 id = floor(p);

    float h1 = hash12(id);
    float h2 = hash12(id + vec2(13.37, 71.19));

    float speed = 1.2;
    float timeCycle = t * speed + h1 * 6.28;
    float cellTimeId = floor(timeCycle);
    float progress = fract(timeCycle);

    float spawnHash = hash12(id + vec2(cellTimeId, cellTimeId * 0.5));
    if (spawnHash > 0.35) return 0.0;

    vec2 dropCenter = (vec2(h1, h2) - 0.5) * 0.6;
    vec2 g = fract(p) - 0.5 - dropCenter;

    float dist = length(g);
    float radius = progress * 0.35;

    float timeFade = smoothstep(0.0, 0.15, progress) * (1.0 - smoothstep(0.3, 1.0, progress));
    float waveDist = dist - radius;
    float circle = sin(waveDist * 25.0);
    float ringMask = exp(-abs(waveDist) * 10.0);
    float spatialFade = smoothstep(0.45, 0.1, dist);

    return circle * ringMask * timeFade * spatialFade;
}

vec3 getViewPos(vec2 uv, float depth) {
    vec4 ndc = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 viewPos = uInvProj * ndc;
    return viewPos.xyz / viewPos.w;
}

vec4 traceSSR(vec3 rayStartView, vec3 rayDirView) {
    int steps = 20;
    float maxDist = 20.0;
    float stepSize = maxDist / float(steps);

    vec3 curr = rayStartView + rayDirView * 0.15;

    for (int i = 0; i < steps; i++) {
        curr += rayDirView * stepSize;

        vec4 clip = uProj * vec4(curr, 1.0);
        if (clip.w <= 0.0) continue;

        vec3 ndc = clip.xyz / clip.w;
        vec2 uv = ndc.xy * 0.5 + 0.5;

        if (uv.x < 0.001 || uv.x > 0.999 || uv.y < 0.001 || uv.y > 0.999) break;

        float sampledDepth = texture(DepthSampler, uv).r;
        if (sampledDepth >= 0.9999) {
            return vec4(texture(SceneSampler, uv).rgb, 1.0);
        }

        vec3 sampledViewPos = getViewPos(uv, sampledDepth);
        float depthDiff = curr.z - sampledViewPos.z;

        if (depthDiff < 0.0 && depthDiff > -0.8) {
            float edgeFade = smoothstep(0.0, 0.08, uv.x) * (1.0 - smoothstep(0.92, 1.0, uv.x)) *
                             smoothstep(0.0, 0.08, uv.y) * (1.0 - smoothstep(0.92, 1.0, uv.y));
            return vec4(texture(SceneSampler, uv).rgb, edgeFade);
        }
    }
    return vec4(0.0);
}

void main() {
    vec4 sceneColor = texture(SceneSampler, texCoord);
    float depth = texture(DepthSampler, texCoord).r;

    if (depth >= 0.9999) {
        fragColor = sceneColor;
        return;
    }

    vec3 viewPos = getViewPos(texCoord, depth);
    vec4 worldRelPos = uInvView * vec4(viewPos, 0.0);
    vec3 worldPos = uCameraPos + worldRelPos.xyz;

    vec3 dX = dFdx(worldPos);
    vec3 dY = dFdy(worldPos);
    vec3 worldNormal = normalize(cross(dX, dY));
    if (worldNormal.y < 0.0) worldNormal = -worldNormal;

    if (length(viewPos) < 0.05 || worldNormal.y < 0.6) {
        fragColor = sceneColor;
        return;
    }

    vec2 puddleUV = worldPos.xz * (0.05 / max(uScale, 0.1));
    float n = fbm(puddleUV);

    float threshold = mix(0.75, 0.2, clamp(uDensity, 0.0, 1.0));
    float puddleMask = smoothstep(threshold, threshold + 0.05, n);

    if (puddleMask <= 0.001) {
        fragColor = sceneColor;
        return;
    }

    vec3 waterNormalWorld = vec3(0.0, 1.0, 0.0);
    float waveHeight = 0.0;

    if (uUseRain > 0.5) {
        float drops = rainRipples(worldPos.xz, uTime);
        waterNormalWorld.xz += vec2(drops * 0.05);
        waveHeight += abs(drops);
    }

    if (uUseWaves > 0.5) {
        float wave1 = sin(worldPos.x * 4.0 + uTime * 2.8) * 0.015;
        float wave2 = cos(worldPos.z * 4.0 + uTime * 2.2) * 0.015;
        waterNormalWorld.xz += vec2(wave1, wave2);
        waveHeight += abs(wave1 + wave2);
    }

    waterNormalWorld = normalize(waterNormalWorld);

    vec3 waterNormalView = normalize((uView * vec4(waterNormalWorld, 0.0)).xyz);
    vec3 wetGroundColor = sceneColor.rgb * 0.35;

    vec3 viewDirView = normalize(viewPos);
    vec3 reflDirView = reflect(viewDirView, waterNormalView);

    vec4 ssr = traceSSR(viewPos, reflDirView);

    float lookDownFactor = smoothstep(-0.95, -0.6, viewDirView.y);
    vec2 fallbackUV = clamp(texCoord + waterNormalWorld.xz * (0.04 * lookDownFactor), vec2(0.001), vec2(0.999));
    vec3 fallbackRefl = texture(SceneSampler, fallbackUV).rgb;

    vec3 finalReflection = mix(fallbackRefl, ssr.rgb, ssr.a);

    if (uUseWaves > 0.5 && uWaveTheme > 0.5) {
        finalReflection = mix(finalReflection, uWaveColor * 1.4, clamp(waveHeight * 1.5, 0.0, 0.4));
    }

    float NdotV = max(dot(-viewDirView, waterNormalView), 0.0);
    float fresnel = clamp(pow(1.0 - NdotV, 2.5), 0.25, 0.85);

    vec3 puddleColor = mix(wetGroundColor, finalReflection, fresnel);
    vec3 finalColor = mix(sceneColor.rgb, puddleColor, puddleMask * 0.85);

    fragColor = vec4(finalColor, sceneColor.a);
}
