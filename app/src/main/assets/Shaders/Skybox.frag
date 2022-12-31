#version 300 es
precision mediump float;

in vec3 normal;

uniform sampler2D map;
uniform float s;

layout(location = 0) out vec4 Fragment;
layout(location = 1) out vec4 B;
layout(location = 2) out vec4 C;

float pack(vec2 v){
    const float p = 128.0;
    v *= 0.95;
    v = floor(v * p);
    return v.x + (v.y * p); // + (v.z * p * p);
}

void main(){
    vec2 UV;
    vec3 n = normalize(normal);
    UV.y = 1.0-(n.y*0.5 + 0.5);
    UV.x = atan(n.x,n.z) * (1.0/3.14152) * 0.5 + 0.5;
   // vec4 color = vec4(texture(map,UV).rgb,1.0/(s+1.0));
    Fragment = vec4(texture(map,UV).rgb*s,0.0); //vec4(0.0,0.0,pack(color.rg),pack(color.ba));
    B = vec4(0.0);
    C = vec4(0.0);
}