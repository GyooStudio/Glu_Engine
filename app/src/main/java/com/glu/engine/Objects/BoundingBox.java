package com.glu.engine.Objects;

import com.glu.engine.vectors.Vector3f;

public class BoundingBox {
    private Vector3f center;
    private Vector3f diameter;

    public BoundingBox(Vector3f radius){
        center = new Vector3f(0);
        this.diameter = radius;
    }

    public BoundingBox(){
        center = new Vector3f(0);
        this.diameter = new Vector3f(0);
    }

    public Vector3f getCenter() {
        return center;
    }

    public Vector3f getDiameter() {
        return diameter;
    }

    public Vector3f getMax(){return Vector3f.add(center, Vector3f.scale(diameter, 0.5f) );}

    public Vector3f getMin(){return Vector3f.sub(center, Vector3f.scale(diameter, 0.5f) );}

    public void setDiameter(Vector3f diameter) {
        this.diameter = diameter;
    }

    public void setCenter(Vector3f pos){
        center = pos.copy();
    }

    public BoundingBox copy(){
        BoundingBox b = new BoundingBox();
        b.center = center.copy();
        b.diameter = diameter.copy();
        return b;
    }
}
