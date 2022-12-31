package com.glu.engine.shader;

import android.opengl.GLES30;

import com.glu.engine.Scene.Camera;
import com.glu.engine.utils.Maths;
import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector2f;
import com.glu.engine.vectors.Vector3f;

public class StaticShader2 extends ShaderProgram{

    private int TRANSFORMATION_MATRIX;
    private int ROTATION_MATRIX;
    private int PROJECTION_MATRIX;
    private int VIEW_MATRIX;
    private int JITTER;
    private int PREV_TRANSFORMATION_MATRIX;
    private int PREV_VIEW_MATRIX;

    private int IOR;
    private int ROUGHNESS;
    private int METALINESS;
    private int SPECULAR;
    private int EMISSION;

    public boolean hasBuilt = false;

    public StaticShader2(String VERTCODE, String FRAGCODE){ super(VERTCODE, FRAGCODE);}

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
        PREV_TRANSFORMATION_MATRIX = super.getUniformLocation("prevTransformMatrix");
        PREV_VIEW_MATRIX = super.getUniformLocation("prevViewMatrix");
    }

    public void loadTransformationMatrix(Matrix4f matrix){
        super.loadUniformMatix(TRANSFORMATION_MATRIX,matrix);
    }

    public void loadPrevTransformationMatrix(Matrix4f matrix){
        super.loadUniformMatix(PREV_TRANSFORMATION_MATRIX,matrix);
    }

    public void loadProjectionMatrix(Matrix4f matrix){
        super.loadUniformMatix(PROJECTION_MATRIX,matrix);
    }

    public void loadViewMatrix(Camera camera){
        super.loadUniformMatix(VIEW_MATRIX, camera.getViewMat());
        super.loadUniformMatix(PREV_VIEW_MATRIX,camera.getPrevViewMat());
    }

    public void loadJitter(Vector2f jit){super.loadUniformVector(JITTER,jit);}

    @Override
    public void buildShader(){
        super.buildShader();
        hasBuilt = true;
    }

    public StaticShader2 copy(){
        StaticShader2 s = new StaticShader2(vertCode,fragCode);
        s.TRANSFORMATION_MATRIX = TRANSFORMATION_MATRIX;
        s.ROTATION_MATRIX = ROTATION_MATRIX;
        s.PROJECTION_MATRIX = PROJECTION_MATRIX;
        s.VIEW_MATRIX = VIEW_MATRIX;
        s.IOR = IOR;
        s.ROUGHNESS = ROUGHNESS;
        s.METALINESS = METALINESS;
        s.SPECULAR = SPECULAR;
        s.EMISSION = EMISSION;
        s.programID = programID;
        s.vertexShader = vertexShader;
        s.fragmentShader = fragmentShader;
        return s;
    }
}
