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
in mediump float depth;
in mediump vec3 screenPos;
in mediump vec3 prevScreenPos;
in highp vec3 eyeSpacePos;

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

const highp vec3 envColor = vec3(0.0,0.1,0.2) * 0.0;

float pack(vec2 v){
    const float p = 128.0;
    v *= 0.95;
    v = floor(v * p);
    return v.x + (v.y * p);
}

void main(){
    if(materialType == 1){
        vec4 albedo;
        if(mapColor){
            albedo = texture(colorSampler, UV);
        }else{
            albedo = vec4(color,1.0);
        }

        if(alphaCut && albedo.a < 0.5f){
            discard;
        }

        vec3 normal = Normal;
        vec4 normTex;
        if(mapNormal){
            mat3 TBN = transpose(mat3(Tangent,BiTangent,Normal));
            normTex = (texture(normalMap,UV) * 2.0) - vec4(1.0);
            normal = normalize(normTex.xyz*TBN);
        }
        normal = (normal * 0.5) + 0.5;

        float r;
        if(mapRoughness){
            r = normTex.w;
        }else{
            r = roughness;
        }

        A = vec4(albedo.rgb,materialType);
        B = vec4(normal,r);
        if(length(Position) > 0.01){
            C = vec4(normalize(Position) * 0.5 + 0.5, length(Position));
        }else{
            C = vec4(0.0);
        }
        D = vec4(depth);
    }else if(materialType == 2){
        vec4 emission;
        if(mapColor){
            emission = texture(colorSampler, UV);
        }else{
            emission = vec4(color,1.0);
        }

        if(alphaCut && emission.a < 0.5f){
            discard;
        }

        A = vec4(emission.rgb * emissionIntensity,materialType);
        B = vec4(0.0);
        if(length(Position) > 0.01){
            C = vec4(normalize(Position) * 0.5 + 0.5, length(Position));
        }else {
            C = vec4(0.0);
        }
        D = vec4(depth);
    }else if(materialType == 3){
        vec4 albedo;
        if(mapColor){
            albedo = texture(colorSampler, UV);
        }else{
            albedo = vec4(color,1.0);
        }

        if(alphaCut && albedo.a < 0.5f){
            discard;
        }

        vec3 normal = Normal;
        vec4 normTex;
        if(mapNormal){
            mat3 TBN = transpose(mat3(Tangent,BiTangent,Normal));
            normTex = (texture(normalMap,UV) * 2.0) - vec4(1.0);
            normal = normalize(normTex.xyz*TBN);
        }
        normal = (normal * 0.5) + 0.5;

        float r;
        if(mapRoughness){
            r = normTex.w;
        }else{
            r = roughness;
        }

        A = vec4(albedo.rgb,materialType);
        B = vec4(normal,r);
        if(length(Position) > 0.01){
            C = vec4(normalize(Position) * 0.5 + 0.5, length(Position));
        }else{
            C = vec4(0.0);
        }
        D = vec4(depth);
    }
    /*vec4 albedo = color;
    albedo = texture(textureSampler,UV);
    if(albedo.a < 0.5){
        discard;
    }

    //normal
    vec3 normal = Normal;

    mat3 TBN = transpose(mat3(Tangent,BiTangent,Normal));
    vec3 normTex = (texture(normalMap,UV).rgb * 2.0) - vec3(1.0);
    normal = normalize(normTex*TBN);

    normal = (normal * 0.5) + 0.5;

    A = vec4(pack(albedo.rg),pack(vec2(albedo.b,roughness)),pack(vec2(Metaliness,specular)), IOR);
    B = vec4(normal,0.0);
    C = vec4(emission * emissionIntensity,0.0);
    if(length(Position) > 0.01){
        D = vec4(normalize(Position) * 0.5 + 0.5, length(Position));
    }else{
        D = vec4(0.0);
    }*/
}