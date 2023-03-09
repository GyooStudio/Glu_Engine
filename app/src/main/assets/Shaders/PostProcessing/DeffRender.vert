#version 300 es
precision mediump float;

in vec4 position;

uniform float tileSize;
uniform sampler2D A;

const lowp int MAX_LIGHTS = 5;
uniform int NUMBER_OF_LIGHTS;
flat out int lID1s[MAX_LIGHTS];
uniform mediump vec3 LightPos[10];
uniform mediump vec3 LightColor[10];
uniform mediump float LightIntensity[10];
//uniform mediump vec4 LightInfo[10];

uniform mediump vec2 screenDiff;
uniform mediump float lightClipDistance;

flat out int lNum1;

out vec2 I_UV;

out float ID;

float ZFar = 200.0;

void main(){

    I_UV = position.xy * 0.5 + 0.5;

    gl_Position = vec4(position.xyz,1.0);
}