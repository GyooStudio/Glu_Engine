package com.glu.engine.Objects;

import com.glu.engine.vectors.Vector3f;

public class BoundingBox {
    private Vector3f center;
    private Vector3f radius;

    public BoundingBox(Vector3f radius){
        center = new Vector3f(0);
        this.radius = radius;
    }

    public BoundingBox(){
        center = new Vector3f(0);
        this.radius = new Vector3f(0);
    }

    public Vector3f getCenter() {
        return center;
    }

    public Vector3f getRadius() {
        return radius;
    }

    public Vector3f getMax(){return Vector3f.add(center, Vector3f.scale(radius, 0.5f) );}

    public Vector3f getMin(){return Vector3f.sub(center, Vector3f.scale(radius, 0.5f) );}

    public void setRadius(Vector3f radius) {
        this.radius = radius;
    }

    public void setCenter(Vector3f pos){
        center = pos.copy();
    }

    public BoundingBox copy(){
        BoundingBox b = new BoundingBox();
        b.center = center.copy();
        b.radius = radius.copy();
        return b;
    }
}
