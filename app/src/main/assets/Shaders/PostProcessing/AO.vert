#version 300 es
precision mediump float;

in vec4 position;

out vec2 UV;

float rand(highp vec2 pos, float seed){
    return (fract( tan( distance( pos * 1.618, pos) * seed ) * pos.x) * 2.0) - 1.0;
}

void main(){
    UV = vec2(position.x*0.5 + 0.5,position.y*0.5 + 0.5);
    gl_Position = position;
}