package com.glu.engine.shader;

import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector2f;
import com.glu.engine.vectors.Vector4f;

public class ColorShader extends ShaderProgram{
    private int SCREEN_DIM;
    private int TRANS_MAT;
    private int COLOR;
    private int RADIUS;

    @Override
    public void bindAttributes() {
        super.bindAttribute(0,"position");
    }

    @Override
    public void getAllUniforms() {
        SCREEN_DIM = super.getUniformLocation("screenDimensions");
        TRANS_MAT = super.getUniformLocation("transformationMatrix");
        COLOR = super.getUniformLocation("Color");
        RADIUS = super.getUniformLocation("Radius");
    }

    public void loadScreenDimensions(Vector2f dim){
        super.loadUniformVector(SCREEN_DIM,dim);
    }

    public void loadTransformationMatrix(Matrix4f mat){
        super.loadUniformMatix(TRANS_MAT,mat);
    }

    public void loadColor(Vector4f color){ super.loadUniformVector(COLOR,color);}

    public void loadRadius(float r){super.loadUniformFloat(RADIUS,r);}

    public ColorShader(String vertCode, String fragCode){
        super(vertCode,fragCode);
    }
}
