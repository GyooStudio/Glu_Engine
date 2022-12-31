#version 300 es
precision mediump float;

out vec4 Fragment;

in vec2 UV;

uniform vec4 Color;
uniform float Radius;

void main(){

    vec4 finalColor = Color;
    float r = max(min(Radius,0.5),0.0);

    if(UV.x > 1.0 - r && UV.y > 1.0 - r){
        if(distance(UV,vec2(1.0-r,1.0-r)) > r){
            finalColor.a = 0.0;
        }
    }

    if(UV.x < 0.0 + r && UV.y > 1.0 - r){
        if(distance(UV,vec2(0.0+r,1.0-r)) > r){
            finalColor.a = 0.0;
        }
    }

    if(UV.x > 1.0 - r && UV.y < 0.0 + r){
        if(distance(UV,vec2(1.0-r,0.0+r)) > r){
            finalColor.a = 0.0;
        }
    }

    if(UV.x < 0.0 + r && UV.y < 0.0 + r){
        if(distance(UV,vec2(0.0+r,0.0+r)) > r){
            finalColor.a = 0.0;
        }
    }

    Fragment = finalColor;
}