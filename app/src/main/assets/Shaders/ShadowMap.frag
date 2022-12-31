#version 300 es
precision mediump float;

layout(location = 0) out float Fragment;

void main(){
    Fragment = gl_FragCoord.z;
}