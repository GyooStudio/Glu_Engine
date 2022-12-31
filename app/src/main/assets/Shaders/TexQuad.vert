#version 300 es
precision mediump float;

in vec2 position;
out vec2 UV;

uniform vec2 screenDimensions;
uniform mat4 transformationMatrix;

void main(){
    UV = vec2(position.x*0.5 + 0.5,1.0-(position.y*0.5 + 0.5));
    gl_Position = vec4(transformationMatrix*vec4(position,1,1)/vec4(screenDimensions,1,1));
}