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
    if(is && isB){
        finalColor = textureLod(texture1, UV, 8.0);
    }else{
        finalColor = texture(texture1, UV);
    }
    //vec4 finalColor = texture(texture1, UV);
    bool GlowCut = is;
    bool GlowAdd = isB;

    if(GlowCut && !isB){
        finalColor.rgb = max(finalColor.rgb - vec3(threshold), vec3(0.0));
    }

    if(GlowAdd && !is){
        finalColor.rgb = texture(texture2,UV).rgb + (finalColor.rgb * 0.5);
    }

    Fragment = vec4(finalColor);
}