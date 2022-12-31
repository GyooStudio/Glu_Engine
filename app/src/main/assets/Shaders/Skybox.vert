#version 300 es
precision mediump float;

in vec4 position;

uniform mat4 transformMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

out vec3 normal;

void main(){
    normal = position.xyz;
    mat4 vMat = viewMatrix;
    vMat[3][0] = 0.0;
    vMat[3][1] = 0.0;
    vMat[3][2] = 0.0;
    gl_Position = projectionMatrix * vMat * transformMatrix * position;
}