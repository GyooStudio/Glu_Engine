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
uniform mediump vec4 LightInfo[10];

uniform mediump vec2 screenDiff;
uniform mediump float lightClipDistance;

flat out int lNum1;

out vec2 I_UV;

float ZFar = 200.0;

void main(){
    vec2 size = vec2(textureSize(A,0));
    vec2 pos = position.xy * 0.5 + 0.5;
    pos = pos * (tileSize/size);
    float numPerLine = ceil(size.x/tileSize);
    pos.x += (mod(float(gl_InstanceID),numPerLine) * (tileSize/size.x));
    pos.y += (floor((float(gl_InstanceID) / numPerLine)) * (tileSize/size.y));

    I_UV = pos;

    pos = (pos * 2.0 - 1.0);

    gl_Position = vec4(pos,position.z,1.0);

    int num1 = 0;
    vec2 sD = screenDiff * tileSize;
    pos.x = mod(float(gl_InstanceID),numPerLine) * sD.x;
    pos.y = floor(float(gl_InstanceID) / numPerLine) * sD.y;
    sD *= 0.5;
    for (int i = 1;i < 10 && i < NUMBER_OF_LIGHTS+1; i++){
        vec2 lPos = abs(LightInfo[NUMBER_OF_LIGHTS - i].xy - pos);
        float lRad = LightInfo[NUMBER_OF_LIGHTS - i].w;
        if (((lPos.x < sD.x + lRad && lPos.y < sD.y + lRad) && !(lPos.x > sD.x && lPos.y > sD.y)) || (distance(lPos,sD) < lRad)){
            if(num1 < MAX_LIGHTS){
                lID1s[num1] = NUMBER_OF_LIGHTS - i;
                num1 ++;
            }else{
                i = 25; //NUMBER_OF_LIGHTS;
            }
        }
    }
    lNum1 = num1;
}