package com.glu.engine.shader;

import android.opengl.GLES30;
import android.util.Log;

import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector2f;
import com.glu.engine.vectors.Vector3f;

public class PPShader extends ShaderProgram {

    private int TEXTURE;
    private int TEXTURE2;
    private int TEXTURE3;
    private int TEXTURE4;
    private int TEXTURE5;

    private int A;
    private int B;

    private int IS;
    private int ISB;

    private int CAM_POS;

    private int MAT;
    private int MATB;
    private int MATC;

    public boolean isDirty = true;

    @Override
    public void bindAttributes() {
        super.bindAttribute(0,"position");
    }

    @Override
    public void getAllUniforms() {
        TEXTURE = super.getUniformLocation("texture1");
        TEXTURE2 = super.getUniformLocation("texture2");
        TEXTURE3 = super.getUniformLocation("texture3");
        TEXTURE4 = super.getUniformLocation("texture4");
        TEXTURE5 = super.getUniformLocation("texture5");
        A = super.getUniformLocation("a");
        B = super.getUniformLocation("b");
        IS = super.getUniformLocation("is");
        ISB = super.getUniformLocation("isB");
        CAM_POS = super.getUniformLocation("CamPos");
        MAT = super.getUniformLocation("mat");
        MATB = super.getUniformLocation("matB");
        MATC = super.getUniformLocation("matC");
        loadTextures();
    }

    public void loadTextures(){
        super.loadUniformTexture(TEXTURE,0);
        super.loadUniformTexture(TEXTURE2,1);
        super.loadUniformTexture(TEXTURE3,2);
        super.loadUniformTexture(TEXTURE4,3);
        super.loadUniformTexture(TEXTURE5,4);
    }

    public void loadUniformBlock(float a, float b ){
        super.loadUniformFloat(A,a);
        super.loadUniformFloat(B,b);
    }

    public void loadBoolean(boolean a){
        super.loadUniformBoolean(IS,a);
    }
    public void loadBooleanB(boolean b){
        super.loadUniformBoolean(ISB,b);
    }

    public PPShader(String vertCode, String fragCode){
        super(vertCode,fragCode);
    }

    public void loadCamPos(Vector3f pos){
        super.loadUniformVector(CAM_POS,pos);
    }

    public void loadMat(Matrix4f mat){super.loadUniformMatix(MAT,mat);}

    public void loadMatB(Matrix4f mat){super.loadUniformMatix(MATB,mat);}

    public void loadMatC(Matrix4f mat){super.loadUniformMatix(MATC,mat);}

    @Override
    public void buildShader(){
        super.buildShader();
        isDirty = false;
    }
}
