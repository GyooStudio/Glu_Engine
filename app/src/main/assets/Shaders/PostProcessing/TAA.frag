#version 300 es
precision mediump float;

in vec2 UV;

out vec4 Fragment;

uniform sampler2D texture1;
uniform sampler2D texture2;
uniform sampler2D texture3;

uniform mediump float a;
uniform mediump float b;

void main(){

    vec2 texel = 1.0/vec2(textureSize(texture1,0));
    vec3 tex1 = texture(texture1,UV).rgb;
    vec3 up = texture(texture1,UV + vec2(0.0,texel.y)).rgb;
    vec3 down = texture(texture1,UV + vec2(0.0,-texel.y)).rgb;
    vec3 left = texture(texture1,UV + vec2(-texel.x,0.0)).rgb;
    vec3 right = texture(texture1,UV + vec2(texel.x,0.0)).rgb;
    vec2 offset = texture(texture3,UV).xy * texture(texture3,UV).z;

    vec3 tex2 = texture(texture2,UV + offset).rgb;

    vec3 minCol = min(up, min(down, min(left,right) ) );
    vec3 maxCol = max(up, max(down, max(left,right) ) );
    tex2 = max(minCol,min(maxCol,tex2));

    float tex3 = texture(texture3,UV).z;

    tex2 = mix(tex2,tex1,max(min( tex3 * 20.0, 0.9 ),0.0));

    vec4 finalColor = vec4(mix(tex1,tex2,0.9 ),0.0);
    Fragment = finalColor;
}