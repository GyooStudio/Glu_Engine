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

    vec4 finalColor;
    highp float depth = texture(texture2, UV).a;
    depth += float(depth < 0.01) * 99999.0;
    float aperture = 1.0/b;
    highp float focusWeight = (abs(depth - fD) * aperture) * fDB;

    bool Horizontal = is;
    bool lastOne = isB;

    float weights;
    float texel;

    if(Horizontal){
        texel = 1.0 / float(textureSize(texture2, 0).x);
    }else{
        texel = 1.0 / float(textureSize(texture2, 0).y);
    }

    for (int i = -WEIGHT_NUMBER; i < WEIGHT_NUMBER+1; i++){

        vec2 sampleCoord;
        float offset = float(i) * texel * a;
        //bool inBounds;
        if(Horizontal){
            sampleCoord = vec2(UV.x + offset, UV.y);
            //inBounds = UV.x + offset < 1.0-texel && UV.x + offset > texel;
        }else{
            sampleCoord = vec2(UV.x, UV.y + offset);
            //inBounds = UV.y + offset < 1.0-texel && UV.y + offset > texel;
        }

        //if(true){
            float gDepth = texture(texture2, sampleCoord).a;
            gDepth += float(gDepth < 0.01) * 99999.0;

            float weight;
            if (gDepth < depth){ // if it's at the back
                weight = (abs( gDepth - fD) * aperture) * fDB;
            } else { //if it's in the front
                weight = focusWeight;
            }
            finalColor += texture(texture1, sampleCoord) * weight;
            weights += weight;
        //}
    }

    if (weights > 0.01){
        finalColor.rgb = finalColor.rgb * (1.0 / weights);
    } else {
        finalColor.rgb = texture(texture1, UV).rgb;
    }

    if(lastOne){
        float w = min(focusWeight,2.5) * 0.4;
        finalColor = mix(texture(texture3,UV),texture(texture1,UV),w);
    }

    Fragment = finalColor;
}