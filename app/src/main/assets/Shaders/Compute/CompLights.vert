#version 300 es
precision mediump float;

layout (xfb_offset = 16,xfb_buffer = 0) out vec4 Res;

in vec4 pos;
in float rad;

uniform mat4 mat;
uniform float a;
uniform vec3 campos;

void main(){
    pos = mat*pos;
    Res.xyz = pos.xyz/pos.w;
    Res.w =  a * rad / sqrt(distance(campos,pos)*distance(campos,pos) - rad*rad);
}