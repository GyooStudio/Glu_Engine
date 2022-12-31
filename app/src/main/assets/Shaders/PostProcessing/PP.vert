#version 300 es
precision mediump float;

in vec4 position;

out vec2 UV;

void main(){
    UV = vec2(position.x*0.5 + 0.5,position.y*0.5 + 0.5);
    gl_Position = position;
}