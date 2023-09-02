package com.glu.engine.Scene;

import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector3f;

public class SunLight {
    public Vector3f direction;
    public Vector3f color;
    public float intensity;
    public float shadowDist;
    public float softness;
    public Matrix4f proj;
    public Matrix4f view;

    public SunLight(Vector3f dir, Vector3f col, float i){
        direction = Vector3f.normalize(dir);
        color = col;
        intensity = i;
        shadowDist = 20f;
        proj = new Matrix4f();
        view = new Matrix4f();
        softness = 1f;
    }

    public SunLight(SunLight s){
        this.direction = s.direction.copy();
        this.color = s.color.copy();
        this.intensity = s.intensity;
        this.shadowDist = s.shadowDist;
        this.softness = s.softness;
        this.proj = s.proj.copy();
        this.view = s.view.copy();
    }

    public SunLight copy(){return new SunLight(this);}
}
