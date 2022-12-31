#version 300 es
precision mediump float;

out vec4 Fragment;
in vec2 O_UV;

uniform sampler2D fontSampler;

void main(){
    vec4 finalColor = texture(fontSampler,O_UV);
    /*if(finalColor.a < 0.5){
        finalColor = vec4(1,0.5,0.5,0.5);
    }*/
    Fragment = finalColor;
}