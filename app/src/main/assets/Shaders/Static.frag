#version 300 es
precision mediump float;

layout(location = 0) out highp vec4 A;
layout(location = 1) out highp vec4 B;
layout(location = 2) out highp vec4 C;
layout(location = 3) out highp vec4 D;
in mediump vec2 UV;
in highp vec3 Position;
in mediump vec3 Normal;
in mediump vec3 CamDir;
in mediump vec3 Tangent;
in mediump vec3 BiTangent;
//in mediump float depth;
in mediump vec3 screenPos;
//in mediump vec3 prevScreenPos;
//in highp vec3 eyeSpacePos;

mediump float IOR = 1.45;

uniform mat4 projectionMatrix;

//uniform mediump float emissionIntensity;
uniform mediump float roughness;
uniform mediump vec3 color;
uniform mediump sampler2D colorSampler;
uniform mediump sampler2D normalMap;
//uniform bool mapRoughness;
uniform bool mapColor;
uniform bool mapNormal;
uniform int materialType;
//uniform bool alphaCut;

uniform vec3 SunDir;
uniform vec3 SunColor;
uniform float SunInt;

const int MAX_LIGHTS = 20;
uniform vec3 LightPos[MAX_LIGHTS];
uniform vec3 LightColor[MAX_LIGHTS];
uniform float LightIntensity[MAX_LIGHTS];
uniform int LightNumber;

uniform sampler2D sky;
uniform float skyStrength;

uniform sampler2D shadow;
uniform mat4 SunProjViewMat;

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

float pack(vec2 v){
    const float p = 128.0;
    v *= 0.95;
    v = floor(v * p);
    return v.x + (v.y * p);
}

void main(){

    vec4 albedo = vec4(color,1.0);
    if(mapColor){
        albedo = texture(colorSampler, UV);// * vec4(color,1.0);
        if (albedo.a < 0.5){
            albedo.a = 0.0;
        }
    }

    //normal
    vec3 normal = Normal;

    if(mapNormal){
        mat3 TBN = transpose(mat3(Tangent, BiTangent, Normal));
        vec3 normTex = (texture(normalMap, UV).rgb * 2.0) - vec3(1.0);
        normal = normalize(normTex*TBN);
    }

    //Fresnel
    float F_I = (1.45f-1.0)/(1.45f+1.0);
    F_I *= F_I;
    float F_A = pow(max(1.0-dot(normal, CamDir), 0.0), 5.0);
    float fresnel = ((1.0-F_I)*F_A + F_I) * (1.0 - 0.5);

    vec3 diffuseSkyColor = skyStrength * textureLod(sky, vec2( atan(normal.x,normal.z) * (1.0/3.14152) * 0.5 + 0.5, 1.0 - (normal.y*0.5 + 0.5) ), 7.5 ).rgb;

    vec3 diffuseLights = vec3(0.0);
    //vec3 specularLights = vec3(0.0);
    for(int i = 0; i < LightNumber; i++){
        vec3 dir = normalize(Position - LightPos[i]);
        diffuseLights += LightColor[i] * LightIntensity[i] * max(dot(-dir,normal),0.0) * (1.0/ ( 1.0 + 0.5 * distance(Position,LightPos[i]) * distance(Position,LightPos[i]) ) );

        //float specularAmount = max( 5.0 * ( dot(reflect(dir, normal), CamDir) ) - 4.0, 0.0) * LightIntensity[i]; // * isHit;
        //specularLights += LightColor[i] * specularAmount * fresnel * 0.5;
    }

    vec4 p = vec4(Position,1.0);
    p = SunProjViewMat * p;
    p.xyz = p.xyz*0.5 + 0.5;
    p.z = max(p.z,0.001);
    float shadowMultiplier = float(texture(shadow,p.xy).r + 0.0075 > p.z || p.x > 1.0 || p.x < 0.0 || p.y > 1.0 || p.y < 0.0 || texture(shadow,p.xy).r < 0.001);
    /*vec2 STexel = 1.0/vec2(textureSize(shadow,0));
    vec2 pUV = p.xy;
    for(int i = 0; i < 2; i ++){
        int offset = int(mod( gl_FragCoord.x * gl_FragCoord.y * 76.52 + float(i), 16.0 ) );
        p.xy = pUV + poissonDisk[offset]*STexel * 1.0;
        float tex = texture(shadow,p.xy).r;
        shadowMultiplier += float(tex + 0.0075 > p.z || p.x > 1.0 || p.x < 0.0 || p.y > 1.0 || p.y < 0.0 || tex < 0.001);
    }
    shadowMultiplier = shadowMultiplier/2.0;*/

    vec4 finalColor = vec4( max( dot(normal,-SunDir), 0.0 ) * albedo.rgb * SunColor * SunInt * shadowMultiplier + diffuseSkyColor * albedo.rgb + diffuseLights * albedo.rgb, albedo.a );

    A = finalColor; //vec4(diffuseLights + specularLights,1.0);

}