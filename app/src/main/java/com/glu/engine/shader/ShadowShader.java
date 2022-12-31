package com.glu.engine.shader;

import com.glu.engine.Scene.Camera;
import com.glu.engine.vectors.Matrix4f;

public class ShadowShader extends ShaderProgram{

    private int TRANSFORM;
    private int VIEW;
    private int PROJ;

    public boolean hasBuilt = false;

    public ShadowShader(String vertCode, String fragCode) {
        super(vertCode, fragCode);
    }

    @Override
    public void bindAttributes() {
        super.bindAttribute(0,"position");
    }

    @Override
    public void getAllUniforms() {
        TRANSFORM = super.getUniformLocation("transform");
        VIEW = super.getUniformLocation("view");
        PROJ = super.getUniformLocation("proj");
    }

    public void loadTransformMatrix(Matrix4f mat){
        super.loadUniformMatix(TRANSFORM,mat);
    }

    public void loadViewMatrix(Matrix4f mat){
        super.loadUniformMatix(VIEW,mat);
    }

    public void loadProjectionMatrix(Matrix4f mat){
        super.loadUniformMatix(PROJ,mat);
    }

    @Override
    public void buildShader(){
        super.buildShader();
        hasBuilt = true;
    }
}
