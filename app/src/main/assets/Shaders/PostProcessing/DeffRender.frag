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

uniform int NUMBER_OF_LIGHTS;
uniform mediump vec3 LightPos[20];
uniform mediump vec3 LightColor[20];
uniform mediump float LightIntensity[20];
uniform mediump float lightClipDistance;
uniform mediump vec2 screenDiff;

//flat in int lNum1;

in vec2 I_UV;

uniform vec3 SunDir;
uniform vec3 SunColor;
uniform float SunIntensity;

uniform mat4 SunViewMat;
uniform mat4 SunProjMat;

in float ID;

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
//    vec2(-0.373396, 0.704901),
//    vec2(0.214934, 0.034412),
//    vec2(0.177880, 0.281217),
//    vec2(-0.661949, 0.690283),
//    vec2(-0.148486, 0.156874),
//    vec2(-0.405008, 0.503787),
//    vec2(-0.296872, -0.293545),
//    vec2(-0.285652, 0.336566),
//    vec2(0.350049, 0.231346),
//    vec2(0.438191, -0.119867),
//    vec2(-0.455220, -0.587768),
//    vec2(0.962927, -0.196903),
//    vec2(-0.394860, -0.416353),
//    vec2(-0.791690, 0.536556),
//    vec2(0.355259, 0.781544),
//    vec2(0.380057, -0.607947),
//    vec2(-0.634605, -0.664200),
//    vec2(-0.527966, 0.331187),
//    vec2(-0.358836, 0.174458),
//    vec2(-0.526283, -0.795576),
//    vec2(-0.797367, 0.366302),
//    vec2(0.712396, -0.164424),
//    vec2(0.622615, 0.170130),
//    vec2(-0.149817, -0.331143),
//    vec2(-0.898936, 0.243075),
//    vec2(0.845215, 0.221285),
//    vec2(-0.277564, -0.775839),
//    vec2(0.381445, -0.844895),
//    vec2(0.291014, -0.343865),
//    vec2(0.563355, -0.814429),
//    vec2(-0.803902, 0.024934),
//    vec2(0.413255, -0.440061),
//    vec2(-0.206887, -0.545120),
//    vec2(0.045632, 0.174098),
//    vec2(0.643063, 0.728214),
//    vec2(-0.539246, 0.104497),
//    vec2(-0.943239, -0.060358),
//    vec2(-0.046883, -0.742114),
//    vec2(0.252321, 0.544129),
//    vec2(0.168197, 0.756582),
//    vec2(0.415538, 0.487324),
//    vec2(-0.323211, -0.046889),
//    vec2(-0.163804, -0.018483),
//    vec2(-0.863798, -0.195940),
//    vec2(0.696512, 0.458282),
//    vec2(0.110668, -0.892524),
//    vec2(-0.021889, 0.568412),
//    vec2(-0.068823, 0.394722)
);

void main(){

    vec4 a = texelFetch(A,ivec2(vec2(textureSize(A,0)) * I_UV),0);
    vec4 b = texelFetch(B,ivec2(vec2(textureSize(B,0)) * I_UV),0);
    vec4 c = texelFetch(C,ivec2(vec2(textureSize(C,0)) * I_UV),0);
    //--- 1ms

    Fragment = vec4(a);
    SkyColor = vec4(0.0);

    if(a.a > 0.9 && a.a < 1.1)
    {
        //---
        highp vec3 pos = (c.xyz * 2.0 - 1.0) * c.w;
        vec3 albedo = a.rgb;
        vec3 normal = b.xyz * 2.0 - 1.0;
        //float roughness = 1.0-b.a;

        vec3 CamDir = normalize(CamPos - pos);
        //--- 1ms

        //Fresnel
        /*float F_I = (1.45f-1.0)/(1.45f+1.0);
        F_I *= F_I;
        float F_A = pow(max(1.0-dot(normal, CamDir), 0.0), 3.0);
        float fresnel = ((1.0-F_I)*F_A + F_I) * (1.0 - b.a)*/;
        //--- 1.5ms

        vec3 diffColorMultiplier;
        //vec3 specColor = vec3(1.0);

        //float isHit = 0.0;
        //float numLight = 0.0;

        //point Lights
        for (int i = 0;i < NUMBER_OF_LIGHTS; i++){

            vec3 lPos = LightPos[i];
            vec3 lCol = LightColor[i];
            float lI = LightIntensity[i];

            float LightDist = distance(lPos, pos);
            vec3 LightDir = normalize(lPos - pos);

            //numLight += (0.5/4.0);

            //isHit = distance(gl_FragCoord.xy * screenDiff, LightInfo[lID1s[i]].xy);
            //isHit = max((LightInfo[lID1s[i]].w - isHit) / LightInfo[lID1s[i]].w,0.0);

            LightDist = 1.0/(1.0 + (4.0 * LightDist) + (LightDist*LightDist));

            //diffuse
            float diffLightAmount = max(dot(LightDir, normal), 0.0) * LightDist * lI; // * isHit;
            diffColorMultiplier += diffLightAmount  * lCol;

            //specular
            /*float specularAmount = max( 10.0 * ( dot(reflect(-LightDir, normal), CamDir) ) - 9.0, 0.0) * lI; // * isHit;
            specColor += lCol * specularAmount;*/
        }
        //--- 2ms

        //shadow mapping
        vec4 p = vec4(pos,1.0);
        p = SunProjMat * SunViewMat * p;
        p.xyz = p.xyz*0.5 + 0.5;
        p.z = max(p.z,0.001);
        float shadow = float(texture(ShadowMap,p.xy).r + 0.0075 > p.z || p.x > 1.0 || p.x < 0.0 || p.y > 1.0 || p.y < 0.0 || texture(ShadowMap,p.xy).r < 0.001);
        /*vec2 STexel = 1.0/vec2(textureSize(ShadowMap,0));
        vec2 pUV = p.xy;
        for(int i = 0; i < 4; i ++){
            int offset = int(mod( gl_FragCoord.x * gl_FragCoord.y * 76.52 + float(i), 16.0 ) );
            p.xy = pUV + poissonDisk[offset]*STexel * shadowSoftness;
            float tex = texture(ShadowMap,p.xy).r;
            shadow += float(tex + 0.0075 > p.z || p.x > 1.0 || p.x < 0.0 || p.y > 1.0 || p.y < 0.0 || tex < 0.001);
        }
        shadow = shadow/4.0;*/
        //--- 4ms

        // SunLight diffuse
        float lDiff = max(dot(-SunDir, normal), 0.0) * shadow;
        diffColorMultiplier += (lDiff * SunColor * SunIntensity);
        //--- 0.5ms

        // SunLight specular
        /*float lSpec = max( 100.0 * (dot(reflect(SunDir, normal), CamDir) ) - 90.0, 0.0 ) * shadow * SunIntensity;
        specColor += lSpec * SunColor*/;
        //--- 1ms

        //skybox diffuse
        vec3 n = normal;
        n.xy =  vec2( atan(n.x, n.z) * (1.0/3.14152) * 0.5 + 0.5, 1.0 - (n.y*0.5 + 0.5) );
        vec3 skyDefColor = textureLod(D,n.xy, 7.5).rgb;
        skyDefColor = skyDefColor * skyboxStrength;
        diffColorMultiplier += skyDefColor;
        //--- 2ms

        //skybox reflections
        /*n = reflect(-CamDir, normal);
        n.xy =  vec2( atan(n.x, n.z) * (1.0/3.14152) * 0.5 + 0.5, 1.0 - (n.y*0.5 + 0.5) );
        vec3 skySpecColor = texture(D,n.xy).rgb;
        skySpecColor = skySpecColor * skyboxStrength;
        specColor += skySpecColor*/;
        //--- 2ms

        Fragment = vec4((diffColorMultiplier * albedo), 1.0);
        //SkyColor = vec4(skyDefColor, 1.0);
    }
    //if(a.a > 1.9 && a.a < 2.1){
    //    Fragment = vec4(a.rgb,1.0);
    //}
    if(a.a > 2.9 && a.a < 3.1)
    {
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

        float r = 1.0 / max(roughness, 0.01);

        //point Lights
        for (int i = 0;i < NUMBER_OF_LIGHTS; i++){

            vec3 lPos = LightPos[i];
            vec3 lCol = LightColor[i];
            float lI = LightIntensity[i];

            vec3 LightDir = normalize(lPos - pos);

            //specular
            float specularAmount = max( 10.0 * ( dot(reflect(-LightDir, normal), CamDir) ) - 9.0, 0.0) * lI * float(b.a < 0.5); // * isHit;
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
        n.xy =  vec2(atan(n.x, n.z) * (1.0/3.14152) * 0.5 + 0.5, 1.0 - (n.y*0.5 + 0.5));
        vec3 skySpecColor = textureLod(D, n.xy, mix(0.0, 8.0, sqrt(roughness))).rgb;
        skySpecColor = skySpecColor * skyboxStrength;
        specColor += skySpecColor;

        Fragment = vec4(specColor * albedo, 1.0);
        //SkyColor = vec4(skySpecColor, 1.0);
    }

    if(isinf(Fragment.r) || isinf(Fragment.g) || isinf(Fragment.b) || isinf(Fragment.a) || isnan(Fragment.r) || isnan(Fragment.g) || isnan(Fragment.b) || isnan(Fragment.a)){
        Fragment = vec4(0.0);
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

    /*Fragment = vec4(a);
    SkyColor = vec4(0.0);

    if(a.a > 0.9 && a.a < 1.1){
        highp vec3 pos = (c.xyz * 2.0 - 1.0) * c.w;
        vec3 albedo = a.rgb;
        vec3 normal = b.xyz * 2.0 - 1.0;
        float roughness = 0.1;

        vec3 CamDir = normalize(CamPos - pos);

        //Fresnel
        float F_I = (1.45f-1.0)/(1.45f+1.0);
        F_I *= F_I;
        float F_A = pow(max(1.0-dot(normal, CamDir), 0.0), 3.0);
        float fresnel = (1.0-F_I)*F_A + F_I;
        fresnel *= 0.9;

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

            //specular
            float specularAmount = max( 10.0 * ( dot(reflect(-LightDir, normal), CamDir) ) - 9.0, 0.0) * lI * isHit * float(b.a < 0.5);
            specColor += lCol * specularAmount;
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

        // SunLight specular
        float lSpec = max( 100.0 * (dot(reflect(SunDir, normal), CamDir) ) - 90.0, 0.0 ) * shadow * SunIntensity * float(b.a < 0.5);
        specColor += lSpec * SunColor;

        //skybox diffuse
        vec3 n = normal;
        n.xy =  vec2( atan(n.x, n.z) * (1.0/3.14152) * 0.5 + 0.5, 1.0 - (n.y*0.5 + 0.5) );
        vec3 skyDefColor = textureLod(D,n.xy, 8.0).rgb;
        skyDefColor = skyDefColor * skyboxStrength;
        diffColorMultiplier += skyDefColor;

        //skybox reflections
        n = reflect(-CamDir, normal);
        n.xy =  vec2( atan(n.x, n.z) * (1.0/3.14152) * 0.5 + 0.5, 1.0 - (n.y*0.5 + 0.5) );
        vec3 skySpecColor = texture(D,n.xy).rgb;
        skySpecColor = skySpecColor * skyboxStrength * float(b.a < 0.5);
        specColor += skySpecColor;

        Fragment = vec4( (diffColorMultiplier * albedo) + (specColor * fresnel), 1.0);
        SkyColor = vec4(skyDefColor + (skySpecColor * fresnel), 1.0);
    }
    //if(a.a > 1.9 && a.a < 2.1){
    //    Fragment = vec4(a.rgb,1.0);
    //}
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
        SkyColor = vec4(skySpecColor,1.0);*/
}