package com.glu.engine.shader.compute;

import android.opengl.GLES30;

import com.glu.engine.Scene.Light;
import com.glu.engine.shader.compute.ComputeVertexShader;
import com.glu.engine.utils.Loader;
import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector3f;

public class ComputeLights extends ComputeVertexShader {

    public int MAT;
    public int A;
    public int CAMPOS;

    public ComputeLights(String code){
        super(code);
    }

    @Override
    public void bindAttributes() {
        super.bindAttribute(0,"pos");
        super.bindAttribute(1,"rad");
    }

    @Override
    public void getAllUniforms() {
        MAT = super.getUniformLocation("mat");
        A = super.getUniformLocation("a");
        CAMPOS = super.getUniformLocation("campos");
    }

    public void loadUniforms(Matrix4f mat, float a, Vector3f campos){
        super.loadUniformMatix(MAT,mat);
        super.loadUniformFloat(A,a);
        super.loadUniformVector(CAMPOS,campos);
    }

    private void loadLights(Light[] lights){
        float[] pos = new float[lights.length * 3];
        float[] rad = new float[lights.length];
        for (int i = 0; i < lights.length; i++) {
            pos[i * 3 + 0] = lights[i].position.x;
            pos[i * 3 + 1] = lights[i].position.y;
            pos[i * 3 + 2] = lights[i].position.z;
            rad[i] = lights[i].radius;
        }

        super.loadVaryingVector3f(0,pos);
        super.loadVaryingFloat(1,rad);
    }

    public float[] compute(Light[] lights){
        prepareToCompute(2,lights.length * 4);
        loadLights(lights);
        Render(lights.length);
        return retreiveDataAsFloat();
    }
}
