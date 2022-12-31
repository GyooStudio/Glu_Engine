#version 300 es
precision mediump float;

in vec4 position;

out vec2 UV;
flat out float aParam;

const int W_N = 2;

flat out int WEIGHT_NUMBER;

float GaussianFunction(float x){
    return exp( -(x*x) );
}

void main(){

    WEIGHT_NUMBER = W_N;

    float a = 0.0;

    for(int i = -W_N; i < W_N+1; i++){
        a += GaussianFunction( ( float(i) / float(W_N) ) * 2.0 );
    }

    aParam = 1.0 / a;

    UV = position.xy*0.5 + 0.5;
    gl_Position = position;
}