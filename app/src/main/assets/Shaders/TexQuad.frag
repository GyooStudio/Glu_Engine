#version 300 es
precision mediump float;

in vec2 UV;

out vec4 Fragment;

uniform sampler2D textureS;
uniform bool transparency;

void main(){
    Fragment = vec4( texture(textureS,UV).rgb, mix( 1.0, texture(textureS,UV).a, float(transparency) ) );
}