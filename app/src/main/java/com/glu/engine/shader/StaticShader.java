package com.glu.engine.shader;

import android.opengl.GLES30;
import android.util.Log;

import com.glu.engine.Scene.Camera;
import com.glu.engine.utils.Maths;
import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector2f;
import com.glu.engine.vectors.Vector3f;

import java.util.ServiceLoader;

public class StaticShader extends ShaderProgram{

    private int TRANSFORMATION_MATRIX;
    private int ROTATION_MATRIX;
    private int PROJECTION_MATRIX;
    private int VIEW_MATRIX;
    private int JITTER;
    private int EMISSION_INTESITY;
    private int ROUGHNESS;
    private int COLOR;
    private int MAP_ROUGHNESS;
    private int MAP_COLOR;
    private int MAP_NORMAL;
    private int MATERIAL;
    private int ALPHA_CUT;

    public StaticShader(String VERTCODE, String FRAGCODE){ super(VERTCODE, FRAGCODE);}

    @Override
    public void bindAttributes(){
        super.bindAttribute(0,"position");
        super.bindAttribute(1,"i_UV");
        super.bindAttribute(2,"i_Normal");
        super.bindAttribute(3,"tangent");
        super.bindAttribute(4,"bitangent");
    }

    @Override
    public void getAllUniforms(){
        TRANSFORMATION_MATRIX = super.getUniformLocation("transformationMatrix");
        ROTATION_MATRIX = super.getUniformLocation("rotationMatrix");
        PROJECTION_MATRIX = super.getUniformLocation("projectionMatrix");
        VIEW_MATRIX = super.getUniformLocation("viewMatrix");
        JITTER = super.getUniformLocation("jitter");
        EMISSION_INTESITY = super.getUniformLocation("emissionIntensity");
        ROUGHNESS = super.getUniformLocation("roughness");
        COLOR = super.getUniformLocation("color");
        MAP_ROUGHNESS = super.getUniformLocation("mapRoughness");
        MAP_COLOR = super.getUniformLocation("mapColor");
        MAP_NORMAL = super.getUniformLocation("mapNormal");
        MATERIAL = super.getUniformLocation("materialType");
        ALPHA_CUT = super.getUniformLocation("alphaCut");

        loadTexture("colorSampler",0);
        loadTexture("normalMap",1);
    }

    public void loadTransformationMatrix(Matrix4f matrix){
        super.loadUniformMatix(TRANSFORMATION_MATRIX,matrix);
    }

    public void loadRotationMatrix(Matrix4f matrix){
        super.loadUniformMatix(ROTATION_MATRIX,matrix);
    }

    public void loadProjectionMatrix(Matrix4f matrix){
        super.loadUniformMatix(PROJECTION_MATRIX,matrix);
    }

    public void loadViewMatrix(Camera camera){
        super.loadUniformMatix(VIEW_MATRIX, camera.getViewMat());
    }

    public void loadMaterial(Material material){
        super.loadUniformBoolean(MAP_ROUGHNESS,material.isRoughnessMapped);
        super.loadUniformBoolean(MAP_COLOR, material.isColorTextured);
        super.loadUniformBoolean(MAP_NORMAL, material.isNormalMapped);
        super.loadUniformBoolean(ALPHA_CUT, material.alphaClip);
        super.loadUniformFloat(ROUGHNESS, material.roughness);
        super.loadUniformVector(COLOR, material.color);
        super.loadUniformFloat(EMISSION_INTESITY, material.emissionIntensity);
        super.loadUniformInt(MATERIAL,material.type);
    }

    public void loadTexture(String uniformName, int textureUnit){
        super.loadUniformTexture(super.getUniformLocation(uniformName),textureUnit);
    }

    public void loadJitter(Vector2f jit){super.loadUniformVector(JITTER,jit);}

    public StaticShader copy(){
        StaticShader s = new StaticShader(vertCode,fragCode);
        s.TRANSFORMATION_MATRIX = TRANSFORMATION_MATRIX;
        s.ROTATION_MATRIX = ROTATION_MATRIX;
        s.PROJECTION_MATRIX = PROJECTION_MATRIX;
        s.VIEW_MATRIX = VIEW_MATRIX;
        s.EMISSION_INTESITY = EMISSION_INTESITY;
        s.ROUGHNESS = ROUGHNESS;
        s.COLOR = COLOR;
        s.MAP_ROUGHNESS = MAP_ROUGHNESS;
        s.MAP_COLOR = MAP_COLOR;
        s.MAP_NORMAL = MAP_NORMAL;
        s.MATERIAL = MATERIAL;
        s.ALPHA_CUT = ALPHA_CUT;
        s.programID = programID;
        s.vertexShader = vertexShader;
        s.fragmentShader = fragmentShader;
        return s;
    }
}
