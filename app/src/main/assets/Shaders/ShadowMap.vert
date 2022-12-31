#version 300 es
precision mediump float;

in vec4 position;

uniform mat4 transform;
uniform mat4 view;
uniform mat4 proj;

void main(){
    gl_Position = proj * view * transform * position;
}