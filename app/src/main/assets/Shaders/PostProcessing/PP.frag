#version 300 es
precision mediump float;

in vec2 UV;

out vec4 Fragment;

uniform sampler2D texture1;

uniform mediump float a;
uniform mediump float b;

void main(){

    Fragment = texture(texture1,UV);
}