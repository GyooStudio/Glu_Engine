package com.glu.engine.Objects;

import android.util.Log;

import com.glu.engine.utils.Loader;

public class RawModel {
    public int vaoID;
    public int vertCount;
    public String name = "name";
    public boolean isDirty = true;

    public float[] positions;
    public float[] uv;
    public float[] norm;
    public int[] indices;

    public RawModel(int vaoID, int vertCount, float[] positions, float[] uv, float[] norm, int[] indices){
        this.vaoID = vaoID;
        this.vertCount = vertCount;
        isDirty = false;
        this.positions = positions;
        this.uv = uv;
        this.norm = norm;
        this.indices = indices;
    }

    public RawModel(float[] positions, float[] uv, float[] norm, int[] indices){
        this.positions = positions.clone();
        this.uv = uv.clone();
        this.norm = norm.clone();
        this.indices = indices.clone();
        vertCount = indices.length;
        vaoID = 0;
        isDirty = true;
    }

    public void makeModel(){
        RawModel model = Loader.loadToVAO(positions,uv,norm,indices);
        vaoID = model.vaoID;
        isDirty = false;
        this.positions = null;
        this.uv = null;
        this.norm = null;
        this.indices = null;
    }

}
