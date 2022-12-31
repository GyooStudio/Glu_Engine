package com.glu.engine.Postprocessing;

import com.glu.engine.Objects.RawModel;
import com.glu.engine.shader.PPShader;
import com.glu.engine.utils.Loader;
import com.glu.engine.vectors.Vector2f;

public class PPQuad{

    public Vector2f position;
    public float rotation;
    public Vector2f scale;
    public final float[] positions = new float[]{
            -1, 1, -0.5f,
            -1,-1, -0.5f,
            1, 1, -0.5f,
            1,-1, -0.5f};
    public Vector2f screenDimensions;

    public int number = 1;

    public RawModel model;

    public PPShader shader;

    public boolean isDirty = true;

    public PPQuad(Vector2f screenDimensions){
        this.position = new Vector2f(0,0);
        this.rotation = new Float(0);
        this.scale = new Vector2f(1,1);
        this.screenDimensions = screenDimensions;
    }

    public void copy(PPQuad a){
        position = a.position.copy();
        rotation = rotation;
        scale = scale.copy();
        model = a.model;
        screenDimensions = a.screenDimensions.copy();
    }

    public void setShader(PPShader shader){
        this.shader = shader;
    }

    public void makeModel(){
        model = Loader.loadToVAO(positions);
        isDirty = false;
    }
}
