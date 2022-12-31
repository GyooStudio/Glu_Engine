package com.glu.engine.Scene;

import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector3f;

public class Camera {
    private Vector3f position = new Vector3f(0,0,0);
    private Vector3f rotation = new Vector3f(0,0,0);
    private Vector3f prevPosition = new Vector3f(0,0,0);
    private Vector3f prevRotation = new Vector3f(0,0,0);

    private Matrix4f translationMat = new Matrix4f();
    private Matrix4f rotationMat = new Matrix4f();
    private Matrix4f prevTranslationMat = new Matrix4f();
    private Matrix4f prevRotationMat = new Matrix4f();

    public Camera(){
        translationMat.setIdentity();
        rotationMat.setIdentity();
        prevTranslationMat.setIdentity();
        prevRotationMat.setIdentity();
    }

    public void move(Vector3f p){
        position.add(p);
        translationMat.translate(p.negative());
    }

    public void rotate(Vector3f r){
        rotation.add(r);
        rotationMat.rotate(r.negative());
    }

    public void callNewFrame(){
        prevPosition = position.copy();
        prevRotation = rotation.copy();
        prevTranslationMat = translationMat.copy();
        prevRotationMat = rotationMat.copy();
    }

    public void setPosition(Vector3f pos){
        position = pos.copy();
        translationMat.setIdentity();
        translationMat.translate(pos.negative());
    }

    public void setRotation(Vector3f rot){
        rotation = rot.copy();
        rotationMat.setIdentity();
        rotationMat.rotate(rot.negative());
    }

    public Vector3f getPosition(){return position;}

    public Vector3f getRotation(){return rotation;}

    public Vector3f getPrevPosition(){return prevPosition;}

    public Vector3f getPrevRotation(){return prevRotation;}

    public Matrix4f getViewMat(){
        return Matrix4f.MultiplyMM(rotationMat,translationMat);
    }

    public Matrix4f getRotationMat(){
        return rotationMat;
    }

    public Matrix4f getPrevViewMat(){
        return Matrix4f.MultiplyMM(prevRotationMat,prevTranslationMat);
    }
}
