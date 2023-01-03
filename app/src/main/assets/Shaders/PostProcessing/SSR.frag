#version 300 es
precision mediump float;

in vec2 UV;

out vec4 Fragment;

uniform sampler2D texture1;
uniform sampler2D texture2;
uniform sampler2D texture3;
uniform sampler2D texture4;
uniform sampler2D texture5;

uniform mediump float a;
uniform mediump float b;

uniform bool is;
uniform bool isB;

uniform mat4 mat;
uniform mat4 matB;
uniform mat4 matC;

const float STEP_SIZE = 0.1;

vec2 unPack(float v){
    const float p = 128.0;
    vec2 r;
    r.x = mod(v,p);
    r.y = floor(v/p);
    //r.z = floor(v/(p*p));
    return r/p;
}

float rand(highp vec3 pos, float seed){
    return (fract( sin( pos.x + pos.y + pos.z + seed) * seed ) * 2.0) - 1.0;
}

void main(){

    if(is && !isB){
        vec4 finalColor;

        vec4 A = texelFetch(texture1,ivec2(vec2(textureSize(texture1,0)) * UV),0);
        vec4 B = texelFetch(texture2,ivec2(vec2(textureSize(texture2,0)) * UV),0);
        vec4 C = texelFetch(texture3,ivec2(vec2(textureSize(texture3,0)) * UV),0);
        vec4 D = texelFetch(texture4,ivec2(vec2(textureSize(texture4,0)) * UV),0);
        vec4 E = texelFetch(texture5,ivec2(vec2(textureSize(texture5,0)) * UV),0);

        vec3 pos = (D.xyz * 2.0 - 1.0) * D.w;
        pos = (matC * vec4(pos,1.0)).xyz;
        vec3 norm = (mat*vec4((C.xyz * 2.0 - 1.0),1.0)).xyz;
        float roughness = C.w;
        float Metaliness = float(B.a > 2.9 && B.a < 3.1);
        float specular = 0.5;
        vec3 albedo = B.rgb;

        int RAY_STEP = 64;

        //fresnel
        float F_I = (1.45-1.0)/(1.45+1.0);
        F_I *= F_I;
        float F_A = pow(max(1.0-dot(norm,vec3(0.0,0.0,-1.0)),0.01),3.0);
        float fresnel = (1.0-F_I)*F_A + F_I;
        fresnel *= 1.0-roughness;
        fresnel = mix(fresnel,1.0,Metaliness);
        fresnel *= float( !(B.a > 1.9 && B.a < 2.1) );

        float add = E.a;

        vec3 ray;
        vec3 rayI;
        highp float depthSample;
        highp vec4 sampleUV;
        highp float depthGeo;
        vec3 specColor = vec3(0.0);
        vec3 diffColor = vec3(0.0);
        float rayLength = 0.0;
        bool isHit = false;

        float k = 0.0;
        if(length(D) > 0.01 && !(B.a > 1.9 && B.a < 2.1)){
            ray = reflect( normalize(pos) ,norm );
            float diffI = dot(ray,norm);
            rayLength = max((STEP_SIZE * float(RAY_STEP)) - (2.0 * STEP_SIZE) + add,STEP_SIZE);
            for (int j = 1; j < RAY_STEP+1 && !isHit; j++){
                vec3 rayPos = (ray * float(j) * STEP_SIZE) + pos + (ray * add);
                sampleUV = matB * vec4(rayPos, 1.0);
                sampleUV.xyz = sampleUV.xyz/max(sampleUV.w, 0.01);
                if (sampleUV.x < 0.99 && sampleUV.x > -0.99 && sampleUV.y < 0.99 && sampleUV.y > -0.99 && sampleUV.z > 0.0 && sampleUV.z < 1.0){
                    highp vec4 tex4fetch = texture(texture4,sampleUV.xy * 0.5 + 0.5);
                    depthGeo = ( matC * vec4( ( tex4fetch.xyz * 2.0 - 1.0 ) * tex4fetch.w, 1.0) ).z;
                    depthSample = rayPos.z;

                    if (depthGeo > depthSample
                    && length(tex4fetch.xyz) > 0.01
                    && depthGeo - depthSample < 1.0
                    ){
                        rayLength = max((STEP_SIZE * float(j)) - (2.0 * STEP_SIZE) + add,0.0);
                        specColor = textureLod(texture1, sampleUV.xy*0.5 + 0.5, 0.0).rgb * specular * fresnel + 1.0;
                        isHit = rayLength > 0.01;
                    }
                } else {
                    j = RAY_STEP+1;
                    specColor = vec3(0.0);
                    rayLength = 0.0;
                }
            }
        }else{
            specColor = vec3(0.0);
            rayLength = 0.0;
        }

        vec3 color = specColor;
        color *= float(rayLength > 0.01);

        rayLength *= float(mod(a,5.0) > 2.0 || isHit);
        rayLength *= float(mod(a,240.0) > 2.0);

        Fragment = vec4(color,rayLength);
    }else if(!isB){
        /*vec3 color = texture(texture1,UV).rgb;
        vec3 prev = texture(texture2,UV).rgb;

        vec2 texel = 1.0/vec2(textureSize(texture1,0));
        vec3 a = texture(texture1,UV + vec2(texel.x,0.0)).rgb;
        vec3 b = texture(texture1,UV + vec2(-texel.x,0.0)).rgb;
        vec3 c = texture(texture1,UV + vec2(0.0,texel.y)).rgb;
        vec3 d = texture(texture1,UV + vec2(0.0,-texel.y)).rgb;
        vec3 Min = min(a,min(b,min(c,d)));
        vec3 Max = max(a,max(b,max(c,d)));

        //Min = min(Max,max(Min,prev));

        color = ((a+b+c+d) * 0.25 * 0.5) + (color * 0.5);

        float bp = max(prev.r, max(prev.g,prev.b));
        float bc = max(color.r, max(color.g,color.b));

        color = mix(color,prev,float(bp > bc) * 0.75);

        float length = texelFetch(texture1,ivec2(vec2(textureSize(texture1,0)) * UV),0).a;*/

        Fragment = texture(texture1,UV);
    }else{

        vec4 tex1 = texelFetch(texture1,ivec2(vec2(textureSize(texture1,0)) * UV),0);
        tex1.xyz = tex1.xyz - 1.0;
        vec4 tex2 = texelFetch(texture2,ivec2(vec2(textureSize(texture2,0)) * UV),0);
        vec4 tex3 = texelFetch(texture3,ivec2(vec2(textureSize(texture3,0)) * UV),0);

        float lum = min(max(tex1.r,max(tex1.g,tex1.b)),1.0);

        vec4 ref = (tex2 - tex3) + tex1;

        ref = mix(tex2,ref,float( lum > 0.01 ));

        Fragment = ref;
    }
}