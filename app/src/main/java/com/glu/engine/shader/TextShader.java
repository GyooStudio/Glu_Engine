package com.glu.engine.shader;

import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector2f;

public class TextShader extends ShaderProgram{
    public static int TRANS_MATRIX;
    public static int SCREEN_DIM;
    public static int FONT_SAMPLER;

    public TextShader(String vertCode, String fragCode) {
        super(vertCode, fragCode);
    }

    @Override
    public void bindAttributes() {
        super.bindAttribute(0,"position");
        super.bindAttribute(1,"UV");
    }

    @Override
    public void getAllUniforms() {
        TRANS_MATRIX = super.getUniformLocation("transformationMatrix");
        SCREEN_DIM = super.getUniformLocation("screenDimensions");
        FONT_SAMPLER = super.getUniformLocation("fontSampler");
        super.loadUniformTexture(FONT_SAMPLER,0);
    }

    public void loadTransformationMatrix(Matrix4f mat){
        super.loadUniformMatix(TRANS_MATRIX,mat);
    }

    public void loadScreenDimensions(Vector2f screenDim){
        super.loadUniformVector(SCREEN_DIM,screenDim);
    }
}
