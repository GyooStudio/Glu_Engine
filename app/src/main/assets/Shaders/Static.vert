#version 300 es
precision mediump float;

in mediump vec3 position;
in mediump vec2 i_UV;
in mediump vec4 i_Normal;
in mediump vec3 tangent;
in mediump vec3 bitangent;

out mediump vec2 UV;
out highp vec3 Position;
out mediump vec3 screenPos;
out mediump vec3 prevScreenPos;
out mediump vec3 Normal;
out mediump vec3 CamDir;
out mediump vec3 Tangent;
out mediump vec3 BiTangent;
out mediump float depth;
out highp vec3 eyeSpacePos;

uniform mat4 transformationMatrix;
uniform mat4 rotationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

uniform mat4 prevTransformMatrix;
uniform mat4 prevViewMatrix;

uniform mediump vec2 jitter;

void main(){

    vec4 finalPos = projectionMatrix * viewMatrix * transformationMatrix * vec4(position,1.0);
    screenPos = finalPos.xyw;
    eyeSpacePos = (viewMatrix * transformationMatrix * vec4(position,1.0)).xyz;
    depth = -eyeSpacePos.z;
    prevScreenPos = (projectionMatrix * prevViewMatrix * prevTransformMatrix * vec4(position,1.0)).xyw;
    UV = vec2(i_UV.x,1.0-i_UV.y);
    Position = (transformationMatrix * vec4(position,1.0)).xyz;
    Normal = normalize((rotationMatrix*i_Normal).xyz);
    Tangent = tangent;
    BiTangent = bitangent;
    CamDir = normalize((inverse(viewMatrix) * vec4(0,0,0,1.0)).xyz - position.xyz);

    mat4 jitterMat = mat4(1.0);
    jitterMat[3][0] = jitter.x;
    jitterMat[3][1] = jitter.y;

    gl_Position = jitterMat*finalPos;
}