#version 300 es
precision mediump float;

in highp vec2 UV;

uniform sampler2D texture1;
uniform sampler2D texture2;
uniform sampler2D texture3;
uniform float a;

out vec4 Fragment;

void main(){
    int inc = int(mod(a,4.0));
    vec2 offset = vec2(1.0-float(inc == 1 || inc == 2), 1.0-float (inc == 2 || inc == 3));
    vec2 texel = vec2(textureSize(texture1,0));
    texel = UV * texel - offset;

    float is = float(mod(texel.x,2.0) <= 0.5 && mod(texel.y,2.0) <= 0.5);

    vec4 prev = texture(texture1,UV) ;
    vec4 current = texture(texture2,UV);

    float tex3 = texture(texture3,UV).z;

    prev = mix( prev, current, max(min( tex3 * 20.0, 1.0 ),0.0) );

    Fragment = mix( prev, current, is);
}