#version 300 es
precision mediump float;

in vec2 UV;
const int WEIGHT_NUMBER = 1;
flat in float fD;
flat in float fDB;

out vec4 Fragment;

uniform sampler2D texture1;
uniform sampler2D texture2;
uniform sampler2D texture3;

uniform mediump float a;
uniform mediump float b;

uniform bool is;
uniform bool isB;

void main(){

    vec4 finalColor = texture(texture1,UV);
    if(is && a < 0.5){
        float depth;
        if(!isB){
            depth = texture(texture2, UV).r - fD;
            depth = depth / fD;
            finalColor.a = max(depth,0.0);
            finalColor.rgb *= max(min(depth, 1.0),0.0);
        }else{
            depth = texture(texture2, UV).r - fD;
            depth = depth/fD;
            finalColor.a = max(-depth,0.0);
        }
    }

    if(is && isB && a > 0.5){
        vec2 STexel = 1.0/vec2( textureSize(texture1,0) );
        //finalColor = texture(texture1,UV);

        finalColor.rgb += texture(texture1, UV + vec2(STexel.x, 0.0) ).rgb * 0.5;
        finalColor.rgb += texture(texture1, UV + vec2(-STexel.x, 0.0) ).rgb * 0.5;
        finalColor.rgb += texture(texture1, UV + vec2(0.0, STexel.y) ).rgb * 0.5;
        finalColor.rgb += texture(texture1, UV + vec2(0.0, -STexel.y) ).rgb * 0.5;

        finalColor.rgb = finalColor.rgb/3.0;

        finalColor.a += texture(texture1, UV + vec2(STexel.x, 0.0) ).a * 0.25;
        finalColor.a += texture(texture1, UV + vec2(-STexel.x, 0.0) ).a * 0.25;
        finalColor.a += texture(texture1, UV + vec2(0.0, STexel.y) ).a * 0.25;
        finalColor.a += texture(texture1, UV + vec2(0.0, -STexel.y) ).a * 0.25;
    }
    if(!is && !isB){
        vec2 STexel = 1.0/vec2( textureSize(texture1,0) );
        //finalColor = texture(texture1,UV);

        finalColor.rgb += texture(texture1, UV + vec2(STexel.x, 0.0) ).rgb * 0.5;
        finalColor.rgb += texture(texture1, UV + vec2(-STexel.x, 0.0) ).rgb * 0.5;
        finalColor.rgb += texture(texture1, UV + vec2(0.0, STexel.y) ).rgb * 0.5;
        finalColor.rgb += texture(texture1, UV + vec2(0.0, -STexel.y) ).rgb * 0.5;

        finalColor.rgb = finalColor.rgb/3.0;
    }

    if(isB && !is){
        vec4 clearColor = texture(texture3,UV);
        vec4 blurColorBehind = texture(texture2,UV);
        float depthBehind = min(blurColorBehind.a/b,1.0);
        float depthFront = min(finalColor.a/(b*2.0),1.0);
        depthBehind = pow(depthBehind,5.0);
        depthFront = pow(depthFront,5.0);

        finalColor = mix( mix(clearColor, blurColorBehind, depthBehind), finalColor, depthFront);
    }

    if(isinf(finalColor.r) || isinf(finalColor.g) || isinf(finalColor.b) || isinf(finalColor.a) || isnan(finalColor.r) || isnan(finalColor.g) || isnan(finalColor.b) || isnan(finalColor.a)){
        finalColor = vec4(0.0);
    }

    Fragment = finalColor;

}