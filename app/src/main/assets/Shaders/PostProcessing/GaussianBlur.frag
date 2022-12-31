#version 300 es
precision mediump float;

in vec2 UV;
flat in float aParam;
flat in int WEIGHT_NUMBER;

out vec4 Fragment;

uniform sampler2D texture1;

uniform mediump float a;
uniform mediump float b;

float GaussianFunction(float x){
    return aParam * exp( -(x*x) );
}

void main(){

    vec4 finalColor = texture(texture1,UV);
    /*bool Horizontal = a > 0.5;

    float offset = 0.0;
    if (Horizontal){
        float texel = 1.0 / float(textureSize(texture1, 0).x);
        for (int i = -WEIGHT_NUMBER; i < WEIGHT_NUMBER+1; i++){
            offset = texel * float(i) * b;
            finalColor.rgb += texture( texture1, vec2( max(min( UV.x + offset, 0.99 ), 0.01) , UV.y ) ).rgb * GaussianFunction((float(i) / float(WEIGHT_NUMBER)) * 2.0);
        }
    } else {
        float texel = 1.0 / float(textureSize(texture1, 0).y);
        for (int i = -WEIGHT_NUMBER + 1; i < WEIGHT_NUMBER+1; i++){
            offset = texel * float(i) * b;
            finalColor.rgb += texture( texture1, vec2( UV.x, max(min( UV.y + offset, 0.99 ) , 0.01) ) ).rgb * GaussianFunction((float(i) / float(WEIGHT_NUMBER)) * 2.0);
        }
    }*/

    Fragment = finalColor;
}