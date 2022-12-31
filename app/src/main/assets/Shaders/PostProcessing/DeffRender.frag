#version 300 es
precision mediump float;

layout(location = 0) out highp vec4 Fragment;
layout(location = 1) out highp vec4 SkyColor;

uniform sampler2D A;
uniform sampler2D B;
uniform sampler2D C;
uniform sampler2D D;
uniform sampler2D ShadowMap;

uniform float skyboxStrength;
uniform float shadowSoftness;

uniform highp vec3 CamPos;

const lowp int MAX_LIGHTS = 5;
flat in int lID1s[MAX_LIGHTS];
uniform mediump vec3 LightPos[10];
uniform mediump vec3 LightColor[10];
uniform mediump float LightIntensity[10];
uniform mediump vec4 LightInfo[10];
uniform mediump float lightClipDistance;
uniform mediump vec2 screenDiff;

flat in int lNum1;

in vec2 I_UV;

uniform vec3 SunDir;
uniform vec3 SunColor;
uniform float SunIntensity;

uniform mat4 SunViewMat;
uniform mat4 SunProjMat;

const mediump vec2 poissonDisk[] = vec2[](
    vec2(-0.9420164, -0.39906216),
    vec2(0.94558609, -0.76890725),
    vec2(-0.094184101, -0.92938870),
    vec2(0.34495938, 0.29387750),
    vec2(-0.91588581, 0.45771432),
    vec2(-0.81544232, -0.87912464),
    vec2(-0.38277543, 0.27676845),
    vec2(0.97484398, 0.75648379),
    vec2(0.44323325, -0.97511554),
    vec2(0.53742981, -0.47373420),
    vec2(-0.26496911, -0.41893023),
    vec2(0.79197514, 0.19090188),
    vec2(-0.24188840, 0.99706507),
    vec2(-0.81409955, 0.91437590),
    vec2(0.19984126, 0.78641367),
    vec2(0.14383161, -0.14100790)
);

void main(){

    vec4 a = texelFetch(A,ivec2(vec2(textureSize(A,0)) * I_UV),0);
    vec4 b = texelFetch(B,ivec2(vec2(textureSize(B,0)) * I_UV),0);
    vec4 c = texelFetch(C,ivec2(vec2(textureSize(C,0)) * I_UV),0);

    Fragment = vec4(a);
    SkyColor = vec4(0.0);

    if(a.a > 0.9 && a.a < 1.1){
        highp vec3 pos = (c.xyz * 2.0 - 1.0) * c.w;
        vec3 albedo = a.rgb;
        vec3 normal = b.xyz * 2.0 - 1.0;
        float roughness = b.a;

        vec3 CamDir = normalize(CamPos - pos);

        //Fresnel
        float F_I = (1.45f-1.0)/(1.45f+1.0);
        F_I *= F_I;
        float F_A = pow(max(1.0-dot(normal, CamDir), 0.0), 3.0);
        float fresnel = (1.0-F_I)*F_A + F_I;
        fresnel *= 1.0-roughness;

        vec3 diffColorMultiplier;
        vec3 specColor;

        float isHit = 0.0;
        float numLight = 0.0;

        //point Lights
        for (int i = 0;i < lNum1; i++){

            vec3 lPos = LightPos[lID1s[i]];
            vec3 lCol = LightColor[lID1s[i]];
            float lI = LightIntensity[lID1s[i]];

            float LightDist = distance(lPos, pos);
            vec3 LightDir = normalize(lPos - pos);

            numLight += (0.5/4.0);

            isHit = distance(gl_FragCoord.xy * screenDiff, LightInfo[lID1s[i]].xy);
            isHit = max((LightInfo[lID1s[i]].w - isHit) / LightInfo[lID1s[i]].w,0.0);

            LightDist = 1.0/(LightDist*LightDist);

            //diffuse
            float diffLightAmount = max(dot(LightDir, normal), 0.0) * LightDist * lI * isHit;
            diffColorMultiplier += diffLightAmount  * lCol;
        }
        //shadow mapping
        vec4 p = vec4(pos,1.0);
        p = SunProjMat * SunViewMat * p;
        p.xyz = p.xyz*0.5 + 0.5;
        p.z = max(p.z,0.001);
        float shadow = float(texture(ShadowMap,p.xy).r + 0.0075 > p.z || p.x > 1.0 || p.x < 0.0 || p.y > 1.0 || p.y < 0.0 || texture(ShadowMap,p.xy).r < 0.001);

        // SunLight diffuse
        float lDiff = max(dot(-SunDir, normal), 0.0) * shadow;
        diffColorMultiplier += (lDiff * SunColor * SunIntensity);

        //skybox diffuse
        vec3 n = normal;
        n.xy =  vec2( atan(n.x, n.z) * (1.0/3.14152) * 0.5 + 0.5, 1.0 - (n.y*0.5 + 0.5) );
        vec3 skyDefColor = textureLod(D,n.xy, 8.0).rgb;
        skyDefColor = skyDefColor * skyboxStrength;
        diffColorMultiplier += skyDefColor;

        //skybox reflections
        n = reflect(-CamDir, normal);
        n.xy =  vec2( atan(n.x, n.z) * (1.0/3.14152) * 0.5 + 0.5, 1.0 - (n.y*0.5 + 0.5) );
        vec3 skySpecColor = textureLod(D,n.xy,mix(0.0,8.0,sqrt(roughness)) ).rgb;
        skySpecColor = skySpecColor * skyboxStrength;
        specColor += skySpecColor;

        Fragment = vec4( (diffColorMultiplier * albedo), 1.0);
        SkyColor = vec4(skyDefColor, 1.0);
    }
    /*if(a.a > 1.9 && a.a < 2.1){
        Fragment = vec4(a.rgb,1.0);
    }*/
    if(a.a > 2.9 && a.a < 3.1){
        highp vec3 pos = (c.xyz * 2.0 - 1.0) * c.w;
        vec3 albedo = a.rgb;
        vec3 normal = b.xyz * 2.0 - 1.0;
        float roughness = b.a;

        vec3 CamDir = normalize(CamPos - pos);

        //Fresnel
        float F_I = (1.45f-1.0)/(1.45f+1.0);
        F_I *= F_I;
        float F_A = pow(max(1.0-dot(normal, CamDir), 0.0), 3.0);
        float fresnel = (1.0-F_I)*F_A + F_I;
        fresnel *= 1.0-roughness;

        vec3 specColor;

        float isHit = 0.0;
        float numLight = 0.0;

        float r = 1.0 / max(roughness, 0.01);

        //point Lights
        for (int i = 0;i < lNum1; i++){

            vec3 lPos = LightPos[lID1s[i]];
            vec3 lCol = LightColor[lID1s[i]];
            float lI = LightIntensity[lID1s[i]];

            float LightDist = distance(lPos, pos);
            vec3 LightDir = normalize(lPos - pos);

            numLight += (0.5/4.0);

            isHit = distance(gl_FragCoord.xy * screenDiff, LightInfo[lID1s[i]].xy);
            isHit = max((LightInfo[lID1s[i]].w - isHit) / LightInfo[lID1s[i]].w,0.0);

            LightDist = 1.0/(LightDist*LightDist);

            //specular
            float specularAmount = max(dot(reflect(-LightDir, normal), CamDir), 0.0); // 4ms
            specularAmount = max(r * specularAmount + (1.0 - r), 0.0) * r * 0.1 * lI * isHit; // 3ms
            specColor += lCol * specularAmount;
        }
        //shadow mapping
        vec4 p = vec4(pos,1.0);
        p = SunProjMat * SunViewMat * p;
        p.xyz = p.xyz*0.5 + 0.5;
        p.z = max(p.z,0.001);
        float shadow = float(texture(ShadowMap,p.xy).r + 0.0075 > p.z || p.x > 1.0 || p.x < 0.0 || p.y > 1.0 || p.y < 0.0 || texture(ShadowMap,p.xy).r < 0.001);

        // SunLight
        float lSpec = max(dot(reflect(SunDir, normal), CamDir), 0.0) * shadow;
        lSpec = (max(r * lSpec + (1.0 - r), 0.0) * r) * SunIntensity;
        specColor += lSpec * SunColor;

        //skybox reflections
        vec3 n = reflect(-CamDir, normal);
        n.xy =  vec2( atan(n.x, n.z) * (1.0/3.14152) * 0.5 + 0.5, 1.0 - (n.y*0.5 + 0.5) );
        vec3 skySpecColor = textureLod(D,n.xy,mix(0.0,8.0,sqrt(roughness)) ).rgb;
        skySpecColor = skySpecColor * skyboxStrength;
        specColor += skySpecColor;

        Fragment = vec4(specColor * albedo, 1.0);
        SkyColor = vec4(skySpecColor,1.0);
    }
    /*vec3 finalColor;

    highp vec3 pos = (c.xyz * 2.0 - 1.0) * c.w;
    vec3 albedo = a.rgb;
    vec3 normal = b.xyz * 2.0 - 1.0;

    float roughness = b.a;
    float Metaliness = float(a.a > 2.9 && a.a < 3.1);
    float specular = 0.5;
    float IOR = 1.45;

    vec3 emission = a.rgb * float(a.a > 1.9 && a.a < 2.1);

    vec3 CamDir = normalize(CamPos - pos);

    //Fresnel
    float F_I = (1.45f-1.0)/(1.45f+1.0);
    F_I *= F_I;
    float F_A = pow(max(1.0-dot(normal, CamDir), 0.0), 3.0);
    float fresnel = (1.0-F_I)*F_A + F_I;
    fresnel *= 1.0-roughness;

    vec3 diffColorMultiplier;
    vec3 specColor;

    float isHit = 0.0;
    float numLight = 0.0;

    float r = 1.0 / max(roughness, 0.01);

    // 2ms

    //point Lights
    for (int i = 0;i < 5; i++){

        vec3 lPos = LightPos[lID1s[i]];
        vec3 lCol = LightColor[lID1s[i]];
        float lI = LightIntensity[lID1s[i]];
        // 3ms

        float LightDist = distance(lPos, pos);
        vec3 LightDir = normalize(lPos - pos);
        // 2ms

        numLight += (0.5/4.0);

        isHit = distance(gl_FragCoord.xy * screenDiff, LightInfo[lID1s[i]].xy);
        isHit = max((LightInfo[lID1s[i]].w - isHit) / LightInfo[lID1s[i]].w,0.0);

        LightDist = 1.0/(LightDist*LightDist);

        //diffuse
        float diffLightAmount = max(dot(LightDir, normal), 0.0) * LightDist * lI * isHit;
        diffColorMultiplier += diffLightAmount  * lCol;
        // 4ms

        //specular
        float specularAmount = max(dot(reflect(-LightDir, normal), CamDir), 0.0); // 4ms
        specularAmount = max(r * specularAmount + (1.0 - r), 0.0) * r * 0.1 * lI * isHit; // 3ms
        specColor += lCol * specularAmount;
        // 5ms
    }
    // 16ms

    //shadow mapping
    vec4 p = vec4(pos,1.0);
    p = SunProjMat * SunViewMat * p;
    p.xyz = p.xyz*0.5 + 0.5;
    p.z = max(p.z,0.001);
    float shadow = float(texture(ShadowMap,p.xy).r + 0.0075 > p.z || p.x > 1.0 || p.x < 0.0 || p.y > 1.0 || p.y < 0.0 || texture(ShadowMap,p.xy).r < 0.001);
    //Percentage closer filtering // + soft shadows
    float CamDist = distance(CamPos,pos);
    //float softness = max(linearize(p.z)-linearize(texture(ShadowMap,p.xy).r),0.0)*shadowSoftness;
    //vec2 texel = 1.0/vec2(textureSize(E,0));
    //vec3 X = normalize((texture(E,I_UV + vec2(texel.x,0.0)).xyz * 2.0 -1.0) * texture(E,I_UV + vec2(texel.x,0.0)).w - pos);
    //vec3 Y = normalize((texture(E,I_UV + vec2(0.0,texel.y)).xyz * 2.0 -1.0) * texture(E,I_UV + vec2(0.0,texel.y)).w - pos);
    vec2 STexel = 1.0/vec2(textureSize(ShadowMap,0));
    vec2 pUV = p.xy;
    for(int i = 0; i < 16; i ++){
        p = vec4(pos + (poissonDisk[i].x + poissonDisk[i].y) * shadowSoftness,1.0);
        //p = SunProjMat * SunViewMat * p;
        //p.xyz = p.xyz*0.5 + 0.5;
        p.xy = pUV + poissonDisk[i]*STexel;
        shadow += float(texture(ShadowMap,p.xy).r + 0.0075 > p.z || p.x > 1.0 || p.x < 0.0 || p.y > 1.0 || p.y < 0.0 || texture(ShadowMap,p.xy).r < 0.001);
    }
    shadow = shadow/17.0;

    // SunLight
    float lDiff = max(dot(-SunDir, normal), 0.0) * shadow;
    diffColorMultiplier += (lDiff * SunColor * SunIntensity);

    float lSpec = max(dot(reflect(SunDir, normal), CamDir), 0.0) * shadow;
    lSpec = (max(r * lSpec + (1.0 - r), 0.0) * r) * SunIntensity;
    specColor += lSpec * SunColor;

    //2 ms

    //skybox reflections
    vec3 n = reflect(-CamDir, normal);
    n.xy =  vec2( atan(n.x, n.z) * (1.0/3.14152) * 0.5 + 0.5, 1.0 - (n.y*0.5 + 0.5) );
    vec3 skySpecColor = textureLod(D,n.xy,mix(0.0,8.0,sqrt(roughness)) ).rgb;
    skySpecColor = skySpecColor * skyboxStrength;
    specColor += skySpecColor;

    n = normal;
    n.xy =  vec2( atan(n.x, n.z) * (1.0/3.14152) * 0.5 + 0.5, 1.0 - (n.y*0.5 + 0.5) );
    vec3 skyDefColor = textureLod(D,n.xy, 8.0).rgb;
    skyDefColor = skyDefColor * skyboxStrength;
    diffColorMultiplier += skyDefColor;
    // 13 ms

    finalColor = ( diffColorMultiplier * albedo ) + specColor + emission;
    //SkyColor = vec4((mix(vec3(fresnel), albedo, Metaliness) + vec3( fresnel * specular * Metaliness)) * specular,0.0);
    //finalColor = mix(finalColor,vec3(1.0,0.0,0.0),numLight);

    Fragment = vec4( finalColor, 1.0);*/
}