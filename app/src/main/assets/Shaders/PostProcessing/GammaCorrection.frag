#version 300 es
precision mediump float;

in vec2 UV;

out vec4 Fragment;

uniform sampler2D texture1;
uniform sampler2D texture2;

uniform mediump float a;
uniform mediump float b;

vec3 m(vec3 a, vec3 b, float c){
    return (a*(1.0-c)) + (b*c);
}

void main(){

    vec4 finalColor = texture(texture1,UV);

    /*vec4 level = texture(texture2,vec2(0.5));
    float l = max(level.r,max(level.g,level.b));
    if(l > 0.7){
        l = 0.1*(0.5 - l) - (level.a - 1.0);
    }else if(l < 0.3){
        l = 0.1*(0.5 - l) - (level.a - 1.0);
    }else if(l <= 0.7 && l >= 0.3){
        l = -(level.a - 1.0);
    }*/
    //l =  0.1*(0.5 - l) - (level.a - 1.0);

    float exposure = a;
    finalColor.rgb = (1.3 * (finalColor.rgb * exp(exposure)-0.5) + 0.5) + 0.1;
    //finalColor.rgb = mix(vec3(dot(finalColor.rgb,vec3(0.299,0.587,0.114))),finalColor.rgb,1.2);

    /*float gamma = 2.2;
    finalColor.rgb = pow(max(finalColor.rgb,0.0),vec3(1.0/gamma));*/

    //finalColor.a = -l + 1.0;

    Fragment =  finalColor; //vec4(l,finalColor.gb,0.0); //vec4(mix(vec3(abs(l)),finalColor.rgb,float(UV.x > 0.5)), 0.0);
}