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

    float focalDepth = texture(texture3,vec2(0.5,0.5)).r;
    float depth = texture(texture3,UV).r;
    float factor = min(abs(depth - focalDepth) * (1.0/4.0),1.0);

    vec4 finalColor;

    vec2 STexel = 1.0/vec2( textureSize(texture1,0) );

    float div = 0.0;
    float original = 1.0;
    for(float x = -1.0; x <= 1.0; x++){
        for(float y = -1.0; y <= 1.0; y++){
            if( x > -0.1 && x < 0.1 && y > -0.1 && y < 0.1){
                finalColor.rgb += texture(texture1,UV).rgb;
                div += 1.0;
            }else {
                float depthO = texture(texture3, UV + vec2(STexel.x * x, STexel.y * y)).r;
                vec3 colorO = texture(texture1, UV + vec2(STexel.x * x, STexel.y * y)).rgb;
                float factorO = min(abs(depthO - focalDepth) * (1.0/4.0), 1.0);

                if (depthO < depth && abs(depthO - depth) > 0.1){
                    finalColor.rgb += colorO * factorO * (0.25/ length(vec2(x,y)));
                    div += factorO * (0.25/ length(vec2(x,y)));
                }else if (abs(depthO - depth) < 0.1){
                    finalColor.rgb += colorO * factor * (0.25/ length(vec2(x,y)));
                    div += factor * (0.25/ length(vec2(x,y)));
                }
            }
        }
    }

    finalColor.rgb = finalColor.rgb/div;
    original = (div-1.0)/div;
    finalColor.a = 1.0 - ( (1.0 - texture(texture1,UV).a) * (1.0 - original));

    /*if(GlowCut && !isB){
        finalColor.rgb = max(finalColor.rgb - vec3(threshold), vec3(0.0));
    }*/

    if(isB && !is){
        finalColor = mix(texture(texture2,UV), texture(texture1,UV), finalColor.a); //vec4(factor * float(focalDepth > depth), factor * float(focalDepth < depth), 0.0,0.0);
    }
    /*if(isB && !is){
        finalColor = vec4(finalColor.a);
    }*/

    Fragment = finalColor;
}