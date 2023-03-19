#version 300 es
precision mediump float;

in vec4 position;

out vec2 UV;
flat out float fD;
flat out float fDB;

const int W_N = 1;

uniform sampler2D texture2;

float linearize(float d){
    float zNear = 1.0;
    float zFar = 100.0;
    return zNear * zFar / (zFar + pow(10.0,d) * (zNear - zFar));
}

void main(){

    fD = texture(texture2, vec2(0.5,0.5)).r;
    fD += float(fD < 0.01) * 99999.0;
    fDB = 1.0/fD;

    UV = vec2(position.x*0.5 + 0.5,position.y*0.5 + 0.5);
    gl_Position = position;
}