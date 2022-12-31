#version 300 es
precision mediump float;

layout(location = 0) in vec2 position;
layout(location = 1) in vec2 UV;

out vec2 O_UV;

uniform vec2 screenDimensions;
uniform mat4 transformationMatrix;

void main(){
    O_UV = UV;
    gl_Position = (transformationMatrix*vec4(position,1.0,1.0))/vec4(screenDimensions,1.0,1.0);
}