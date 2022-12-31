package com.glu.engine.shader;

import com.glu.engine.vectors.Matrix4f;

public class SkyboxShader extends ShaderProgram{

    private int TRANSFORM_MAT;
    private int VIEW_MAT;
    private int PROJ_MAT;
    private int STRENGTH;

    public SkyboxShader(String vertCode, String fragCode) {
        super(vertCode, fragCode);
    }

    @Override
    public void bindAttributes() {
        super.bindAttribute(0,"position");
    }

    @Override
    public void getAllUniforms() {
        TRANSFORM_MAT = super.getUniformLocation("transformMatrix");
        VIEW_MAT = super.getUniformLocation("viewMatrix");
        PROJ_MAT = super.getUniformLocation("projectionMatrix");
        STRENGTH = super.getUniformLocation("s");
        super.loadUniformTexture(super.getUniformLocation("map"),0);
    }

    public void loadTransformMatrix(Matrix4f mat){
        super.loadUniformMatix(TRANSFORM_MAT,mat);
    }

    public void loadViewMatrix(Matrix4f mat){
        super.loadUniformMatix(VIEW_MAT,mat);
    }

    public void loadProjectionMatrix(Matrix4f mat){
        super.loadUniformMatix(PROJ_MAT, mat);
    }

    public void loadStrength(float s){super.loadUniformFloat(STRENGTH,s);}
}
