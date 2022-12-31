#version 300 es
precision mediump float;

layout(location = 0) out highp vec4 A;
layout(location = 1) out highp vec4 B;
layout(location = 2) out highp vec4 C;
layout(location = 3) out highp vec4 D;

in mediump vec2 UV;
in mediump vec4 color;
in highp vec3 Position;
in mediump vec3 screenPos;
in mediump vec3 prevScreenPos;
in mediump vec3 Normal;
in mediump vec3 CamDir;
in mediump float fogFactor;
in mediump vec3 Tangent;
in mediump vec3 BiTangent;
in mediump float depth;
in highp vec3 eyeSpacePos;

void main(){

    if(length(Position) > 0.01){
        A = vec4(normalize(Position) * 0.5 + 0.5, length(Position));
    }else{
        A = vec4(0.0);
    }
    vec2 velocity = 0.5*((prevScreenPos.xy/prevScreenPos.z) - (screenPos.xy/prevScreenPos.z));
    if(length(velocity) > 0.01){
        B = vec4(normalize(velocity)*0.5 + 0.5, length(velocity), 0.0);
    }else{
        B = vec4(0.0);
    }
    C = vec4(eyeSpacePos.xy, -eyeSpacePos.z,0.0);
    D = vec4(0.0);
}