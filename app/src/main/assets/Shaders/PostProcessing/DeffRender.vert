#version 300 es
precision mediump float;

in vec4 position;

uniform float tileSize;
uniform sampler2D A;

uniform mediump vec2 screenDiff;

out vec2 I_UV;

out float ID;

void main(){

    I_UV = position.xy * 0.5 + 0.5;

    gl_Position = vec4(position.xyz,1.0);
}