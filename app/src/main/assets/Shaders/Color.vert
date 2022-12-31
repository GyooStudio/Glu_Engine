#version 300 es
precision mediump float;

in vec2 position;
out vec2 UV;

uniform vec2 screenDimensions;
uniform mat4 transformationMatrix;

void main(){
    UV = (position.xy*0.5)+vec2(0.5);
    gl_Position = vec4(((transformationMatrix*vec4(position,1,1))/vec4(screenDimensions,1,1)));
}