#version 300 es
precision mediump float;

in vec2 UV;

out vec4 Fragment;

uniform sampler2D texture1;
uniform sampler2D texture2;
uniform sampler2D texture3;

uniform mediump float a;
uniform mediump float b;

uniform bool is;

uniform mat4 mat;
uniform mat4 matB;
uniform mat4 matC;

const lowp int SAMPLES = 32;

float rand(highp vec3 pos, float seed){
    return (fract( sin( pos.x + pos.y + pos.z + seed) * seed ) * 2.0) - 1.0;
}

void main(){

    if(is){

        float size = b;
        vec4 finalColor;

        vec4 tex3 = texelFetch(texture3,ivec2(vec2(textureSize(texture3,0)) * UV),0);
        vec3 pos = (tex3.xyz * 2.0 - 1.0) * tex3.w;
        pos = (matC * vec4(pos,1.0)).xyz;
        vec3 norm = texelFetch(texture2,ivec2(vec2(textureSize(texture2,0)) * UV),0).xyz * 2.0 - 1.0;
        norm = (mat*vec4(norm,1.0)).xyz;

        highp float depthSample;
        highp float depthGeo;
        float ao;

        if(length(tex3.xyz) > 0.01){
            for (int i = 1; i < SAMPLES+1; i++){
                vec3 rayPos = normalize( normalize( vec3( rand(pos, 3.1416 + a + float(i)), rand(pos, 22.6453 + a + float(i)), rand(pos, 6.3642 + a + float(i)) ) ) + norm ) * size * (rand(pos,9.334 + a + float(i)) * 0.5 + 0.5) + pos;
                vec4 sampleUV = matB * vec4(rayPos,1.0);
                sampleUV.xy = sampleUV.xy/max(sampleUV.w,0.01);
                if(sampleUV.x < 1.0 && sampleUV.x > -1.0 && sampleUV.y < 1.0 && sampleUV.y > -1.0){
                    highp vec4 tex3fetch = texelFetch(texture3,ivec2(vec2(textureSize(texture3,0)) * (sampleUV.xy * 0.5 + 0.5)),0);
                    depthGeo = ( matC * vec4( ( tex3fetch.xyz * 2.0 - 1.0 ) * tex3fetch.w, 1.0) ).z;
                    depthSample = rayPos.z;

                    ao += float(depthGeo < depthSample // if it's in front of the geometry
                    || depthSample - depthGeo > 5.0 // if it's too far, 5.0 beeing the threshold
                    || length(tex3fetch) < 0.01      //if it's the skybox
                    );
                }else{
                    ao += 1.0;
                }
            }

            ao = ao * (1.0/ float(SAMPLES));
        }else{
            ao = 1.0;
        }
        float prev = texture(texture1,UV).r;

        float up = texelFetch(texture1, ivec2(UV * vec2(textureSize(texture1, 0)) ) + ivec2(0,1), 0).r;
        float down = texelFetch(texture1, ivec2(UV * vec2(textureSize(texture1, 0)) ) + ivec2(0,-1), 0).r;
        float left = texelFetch(texture1, ivec2(UV * vec2(textureSize(texture1, 0)) ) + ivec2(-1,0), 0).r;
        float right = texelFetch(texture1, ivec2(UV * vec2(textureSize(texture1, 0)) ) + ivec2(1,0), 0).r;

        prev = ( prev + ( (up+down+left+right) * 0.25 ) ) * 0.5;

        float power = 0.5;
        ao = max(ao * power + 1.0 - power,0.0);

        ao = mix(ao,prev,0.75);

        Fragment = vec4(ao,0.0,0.0,0.0);
    }else{
        float ao = texture(texture1,UV).r;

        Fragment = texture(texture2,UV) * ao;
    }
}