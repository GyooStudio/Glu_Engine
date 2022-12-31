package com.glu.engine.shader;

import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector2f;

public class TexQuadShader extends ShaderProgram{

    private int SCREEN_DIM;
    private int TRANS_MAT;
    private int TRANSPARENT;

    @Override
    public void bindAttributes() {
        super.bindAttribute(0,"position");
    }

    @Override
    public void getAllUniforms() {
        SCREEN_DIM = super.getUniformLocation("screenDimensions");
        TRANS_MAT = super.getUniformLocation("transformationMatrix");
        TRANSPARENT = super.getUniformLocation("transparency");
        super.loadUniformTexture(super.getUniformLocation("textureS"),0);
    }

    public void loadScreenDimensions(Vector2f dim){
        super.loadUniformVector(SCREEN_DIM,dim);
    }

    public void loadTransformationMatrix(Matrix4f mat){
        super.loadUniformMatix(TRANS_MAT,mat);
    }

    public void loadTransparency(boolean i){
        super.loadUniformBoolean(TRANSPARENT,i);
    }

    public TexQuadShader(String vertCode, String fragCode){
        super(vertCode,fragCode);
    }
}
