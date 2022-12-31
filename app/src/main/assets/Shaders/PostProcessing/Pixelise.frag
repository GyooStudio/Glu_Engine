#version 300 es
precision mediump float;

in vec2 UV;

out vec4 Fragment;

uniform sampler2D texture1;

uniform mediump float a;
uniform mediump float b;

void main(){

    vec4 color = vec4(0.0);
    ivec2 pixel = ivec2(vec2(textureSize(texture1,0)) * UV);
    color +=  texelFetch(texture1,pixel, 0);
    Fragment = color;
}