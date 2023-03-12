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

    vec2 STexel = 1.0/vec2( textureSize(texture1,0) );
    vec4 finalColor = texture(texture1,UV);

    finalColor += texture(texture1, UV + vec2(STexel.x, 0.0) ) * 0.5;
    //finalColor += texture(texture1, UV + vec2(-STexel.x, 0.0) ) * 0.5;
    finalColor += texture(texture1, UV + vec2(0.0, STexel.y) ) * 0.5;
    //finalColor += texture(texture1, UV + vec2(0.0, -STexel.y) ) * 0.5;

    finalColor = finalColor/2.0;

    Fragment = finalColor;
}