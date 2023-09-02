package com.glu.engine.Scene;

import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector3f;

public class Camera {
    private Vector3f position = new Vector3f(0,0,0);
    private Vector3f rotation = new Vector3f(0,0,0);

    private Matrix4f translationMat = new Matrix4f();
    private Matrix4f rotationMat = new Matrix4f();
    private Matrix4f viewMat = new Matrix4f();

    boolean hasChanged = false;

    public Camera(){}

    public Camera(Camera c){
        this.position = c.position.copy();
        this.rotation = c.rotation.copy();
        this.translationMat = c.translationMat.copy();
        this.rotationMat = c.rotationMat.copy();
        this.viewMat = c.viewMat.copy();
        this.hasChanged = c.hasChanged;
    }
    public Camera copy(){return new Camera(this);}

    public void move(Vector3f p){
        position.add(p);
        translationMat.translate(p.negative());
        hasChanged = true;
    }

    public void rotate(Vector3f r){
        rotation.add(r);
        rotationMat.rotate(r.negative());
        hasChanged = true;
    }

    public void callNewFrame(){
        /*prevPosition = position.copy();
        prevRotation = rotation.copy();
        prevTranslationMat = translationMat.copy();
        prevRotationMat = rotationMat.copy();*/
        hasChanged = false;
    }

    public void setPosition(Vector3f pos){
        position = pos.copy();
        translationMat.setIdentity();
        translationMat.translate(pos.negative());
        hasChanged = true;
    }

    public void setRotation(Vector3f rot){
        rotation = rot.copy();
        rotationMat.setIdentity();
        rotationMat.rotate(rot.negative());
        hasChanged = true;
    }

    public Vector3f getPosition(){return position;}

    public Vector3f getRotation(){return rotation;}

    public Matrix4f getViewMat(){
        if(hasChanged){
            viewMat = Matrix4f.MultiplyMM(rotationMat,translationMat);
        }
        return viewMat;
    }

    public Matrix4f getRotationMat(){
        return rotationMat;
    }
}
