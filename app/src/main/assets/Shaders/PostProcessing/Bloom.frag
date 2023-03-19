#version 300 es
precision mediump float;

in vec2 UV;
const int WEIGHT_NUMBER = 2;

out vec4 Fragment;

uniform sampler2D texture1;
uniform sampler2D texture2;

uniform mediump float a;
uniform mediump float b;

uniform bool is;
uniform bool isB;

float threshold = 0.8;

void main(){

    vec4 finalColor;

    vec2 STexel = 1.0/vec2( textureSize(texture1,0) );
    finalColor = texture(texture1,UV);

    finalColor += texture(texture1, UV + vec2(STexel.x, 0.0) ) * 0.5;
    finalColor += texture(texture1, UV + vec2(-STexel.x, 0.0) ) * 0.5;
    finalColor += texture(texture1, UV + vec2(0.0, STexel.y) ) * 0.5;
    finalColor += texture(texture1, UV + vec2(0.0, -STexel.y) ) * 0.5;

    finalColor = finalColor/3.0;

    bool GlowCut = is;
    bool GlowAdd = isB;

    if(GlowCut && !isB){
        finalColor.rgb = max(finalColor.rgb - vec3(threshold), vec3(0.0));
    }

    if(GlowAdd && !is){
        finalColor.rgb = texture(texture2,UV).rgb + (finalColor.rgb * 0.5);
    }

    if(isinf(finalColor.r) || isinf(finalColor.g) || isinf(finalColor.b) || isinf(finalColor.a) || isnan(finalColor.r) || isnan(finalColor.g) || isnan(finalColor.b) || isnan(finalColor.a)){
        finalColor = vec4(0.0);
    }

    Fragment = vec4(finalColor);
}