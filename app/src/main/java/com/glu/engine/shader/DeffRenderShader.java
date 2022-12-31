package com.glu.engine.shader;

import com.glu.engine.Scene.SunLight;
import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector2f;
import com.glu.engine.vectors.Vector3f;
import com.glu.engine.vectors.Vector4f;

public class DeffRenderShader extends ShaderProgram {

    public int A;
    public int B;
    public int C;
    public int D;
    public int E;
    public int F;
    public int SHADOW_MAP;
    public int SKYBOX_STRENGTH;
    public int CAM_POS;
    public int NUMBER_OF_LIGHTS;
    public int TILE_SIZE;
    public int SCREEN_DIFF;
    public int LIGHT_CLIP_DIST;
    public int SUNDIR;
    public int SUNCOL;
    public int SUNINT;
    public int SUN_VIEW_MAT;
    public int SUN_PROJ_MAT;
    public int SHADOW_SOFTNESS;

    public boolean isDirty = true;

    @Override
    public void bindAttributes() {
        super.bindAttribute(0,"position");
    }

    @Override
    public void getAllUniforms() {
        A = super.getUniformLocation("A");
        B = super.getUniformLocation("B");
        C = super.getUniformLocation("C");
        D = super.getUniformLocation("D");
        E = super.getUniformLocation("E");
        F = super.getUniformLocation("F");
        SHADOW_MAP = super.getUniformLocation("ShadowMap");
        SUNDIR = super.getUniformLocation("SunDir");
        SUNCOL = super.getUniformLocation("SunColor");
        SUNINT = super.getUniformLocation("SunIntensity");
        SKYBOX_STRENGTH = super.getUniformLocation("skyboxStrength");
        CAM_POS = super.getUniformLocation("CamPos");
        NUMBER_OF_LIGHTS = super.getUniformLocation("NUMBER_OF_LIGHTS");
        TILE_SIZE = super.getUniformLocation("tileSize");
        SCREEN_DIFF = super.getUniformLocation("screenDiff");
        LIGHT_CLIP_DIST = super.getUniformLocation("lightClipDistance");
        SUN_VIEW_MAT = super.getUniformLocation("SunViewMat");
        SUN_PROJ_MAT = super.getUniformLocation("SunProjMat");
        SHADOW_SOFTNESS = super.getUniformLocation("shadowSoftness");
        loadTextures();
    }

    public void loadTextures(){
        super.loadUniformTexture(A, 0);
        super.loadUniformTexture(B, 1);
        super.loadUniformTexture(C, 2);
        super.loadUniformTexture(D, 3);
        super.loadUniformTexture(SHADOW_MAP,4);
    }

    public DeffRenderShader(String vertCode, String fragCode){
        super(vertCode,fragCode);
    }

    public void loadCamPos(Vector3f pos){
        super.loadUniformVector(CAM_POS,pos);
    }

    public void loadLight(int i, Vector3f position, Vector3f color, float intensity, Vector4f info){
        super.loadUniformVector(super.getUniformLocation("LightPos["+i+"]"), position);
        super.loadUniformVector(super.getUniformLocation("LightColor["+i+"]"), color);
        super.loadUniformFloat(super.getUniformLocation("LightIntensity["+i+"]"), intensity);
        super.loadUniformVector(super.getUniformLocation("LightInfo["+i+"]"), info);
    }

    public void loadNumberOfLights(int num){super.loadUniformInt(NUMBER_OF_LIGHTS,num);}

    public void loadTileSize(float size){super.loadUniformFloat(TILE_SIZE,size);}

    public void loadScreenDiff(Vector2f diff){super.loadUniformVector(SCREEN_DIFF,diff);}

    public void loadLightCilpDistance(float dist){super.loadUniformFloat(LIGHT_CLIP_DIST,dist);}

    public void loadSkyboxStrength(float s){super.loadUniformFloat(SKYBOX_STRENGTH,s);}

    public void loadSunLight(SunLight sunLight){
        super.loadUniformVector(SUNDIR,sunLight.direction);
        super.loadUniformVector(SUNCOL,sunLight.color);
        super.loadUniformFloat(SUNINT,sunLight.intensity);
        super.loadUniformMatix(SUN_VIEW_MAT,sunLight.view);
        super.loadUniformMatix(SUN_PROJ_MAT, sunLight.proj);
    }

    public void loadShadowSoftness(float s){
        super.loadUniformFloat(SHADOW_SOFTNESS,s);
    }

    @Override
    public void buildShader(){
        super.buildShader();
        isDirty = false;
    }
}
