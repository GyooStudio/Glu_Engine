#version 300 es
precision mediump float;

layout(location = 0) out highp vec4 A;
layout(location = 1) out highp vec4 B;
layout(location = 2) out highp vec4 C;
layout(location = 3) out highp vec4 D;
in mediump vec2 UV;
in highp vec3 Position;
in mediump vec3 Normal;
//in mediump vec3 CamDir;
in mediump vec3 Tangent;
in mediump vec3 BiTangent;
in mediump float depth;
in mediump vec3 screenPos;
in mediump vec3 prevScreenPos;
//in highp vec3 eyeSpacePos;

mediump float IOR = 1.45;

uniform mat4 projectionMatrix;

uniform mediump float emissionIntensity;
uniform mediump float roughness;
uniform mediump vec3 color;
uniform mediump sampler2D colorSampler;
uniform mediump sampler2D normalMap;
uniform bool mapRoughness;
uniform bool mapColor;
uniform bool mapNormal;
uniform int materialType;
uniform bool alphaCut;

uniform vec3 SunDir;
uniform vec3 SunColor;
uniform float SunInt;

uniform sampler2D sky;
uniform float skyStrength;

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

    vec3 diffuseSkyColor = skyStrength * textureLod(sky, vec2( atan(normal.x,normal.z) * (1.0/3.14152) * 0.5 + 0.5, 1.0 - (normal.y*0.5 + 0.5) ), 7.5 ).rgb;

    vec4 finalColor = vec4( max( dot(normal,-SunDir), 0.0 ) * albedo.rgb * SunColor + diffuseSkyColor * albedo.rgb, albedo.a );

    A = finalColor; //vec4(normal,1.0);//texture(colorSampler,UV);

}