package com.glu.engine.Objects;

import android.util.Log;

import com.glu.engine.shader.Material;
import com.glu.engine.shader.StaticShader;
import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector3f;

import java.util.ArrayList;

public class Entity {
    private ArrayList<Vector3f> position = new ArrayList<>();
    private ArrayList<Vector3f> rotation = new ArrayList<>();
    private ArrayList<Vector3f> scale = new ArrayList<>();

    private ArrayList<Vector3f> prevPosition = new ArrayList<>();
    private ArrayList<Vector3f> prevRotation = new ArrayList<>();
    private ArrayList<Vector3f> prevScale = new ArrayList<>();

    private ArrayList<Boolean> hasChanged = new ArrayList<>();

    private ArrayList<Matrix4f> translationMatrix = new ArrayList<>();
    private ArrayList<Matrix4f> rotationMatrix = new ArrayList<>();
    private ArrayList<Matrix4f> scaleMatrix = new ArrayList<>();
    private ArrayList<Matrix4f> prevTranslationMatrix = new ArrayList<>();
    private ArrayList<Matrix4f> prevRotationMatrix = new ArrayList<>();
    private ArrayList<Matrix4f> prevScaleMatrix = new ArrayList<>();

    public ArrayList<Collider> colliders = new ArrayList<>();
    public BoundingBox boundingBox = new BoundingBox();

    public ArrayList<Material> material = new ArrayList<>();

    public int ID = -1;

    public final RawModel model;

    public int instanceCount = 0;

    public ArrayList<Boolean> show = new ArrayList<>();
    public ArrayList<Boolean> castShadow = new ArrayList<>();

    public String name = "name";

    public Entity(RawModel model){
        this.model = model;

        position.add(new Vector3f(0,0,0));
        rotation.add(new Vector3f(0,0,0));
        scale.add(new Vector3f(1,1,1));
        prevPosition.add(new Vector3f(0,0,0));
        prevRotation.add(new Vector3f(0,0,0));
        prevScale.add(new Vector3f(1,1,1));
        hasChanged.add(true);
        translationMatrix.add(new Matrix4f());
        prevTranslationMatrix.add(new Matrix4f());
        prevRotationMatrix.add(new Matrix4f());
        rotationMatrix.add(new Matrix4f());
        scaleMatrix.add(new Matrix4f());
        prevScaleMatrix.add(new Matrix4f());
        colliders.add(null);
        show.add(true);
        castShadow.add(true);
        material.add(new Material());
        instanceCount++;

        translationMatrix.get(0).setIdentity();
        prevTranslationMatrix.get(0).setIdentity();
        rotationMatrix.get(0).setIdentity();
        prevRotationMatrix.get(0).setIdentity();
        scaleMatrix.get(0).setIdentity();
        prevScaleMatrix.get(0).setIdentity();
        Log.w("Entity", "Created Entity of " + model.vertCount + " vertices");
    }

    public Entity(Entity entity){
        this.model = entity.model;
        boundingBox = entity.boundingBox.copy();

        position = (ArrayList<Vector3f>) entity.position.clone();
        rotation = (ArrayList<Vector3f>) entity.rotation.clone();
        scale = (ArrayList<Vector3f>) entity.scale.clone();
        hasChanged = (ArrayList<Boolean>) entity.hasChanged.clone();
        translationMatrix = (ArrayList<Matrix4f>) entity.translationMatrix.clone();
        prevTranslationMatrix = (ArrayList<Matrix4f>) entity.prevTranslationMatrix.clone();
        rotationMatrix = (ArrayList<Matrix4f>) entity.rotationMatrix.clone();
        prevRotationMatrix = (ArrayList<Matrix4f>) entity.prevRotationMatrix.clone();
        scaleMatrix = (ArrayList<Matrix4f>) entity.scaleMatrix.clone();
        prevScaleMatrix = (ArrayList<Matrix4f>) entity.prevScaleMatrix.clone();
        colliders =  (ArrayList<Collider>) entity.colliders.clone();
        show = (ArrayList<Boolean>) entity.show.clone();
        castShadow = (ArrayList<Boolean>) entity.castShadow.clone();
        material = (ArrayList<Material>) entity.material.clone();
        instanceCount = entity.instanceCount;
        name = entity.name;
        Log.w("Entity", "Created Entity");
    }

    public void addInstance(Vector3f p, Vector3f r, Vector3f s){
        position.add(p);
        rotation.add(r);
        scale.add(s);
        prevPosition.add(p);
        prevRotation.add(r);
        prevScale.add(s);
        hasChanged.add(true);
        translationMatrix.add(new Matrix4f());
        prevTranslationMatrix.add(new Matrix4f());
        rotationMatrix.add(new Matrix4f());
        prevRotationMatrix.add(new Matrix4f());
        scaleMatrix.add(new Matrix4f());
        prevScaleMatrix.add(new Matrix4f());
        colliders.add(null);
        show.add(true);
        castShadow.add(true);
        material.add(new Material());
        instanceCount++;

        translationMatrix.get(translationMatrix.size() - 1).setIdentity();
        prevTranslationMatrix.get(prevTranslationMatrix.size() - 1).setIdentity();
        rotationMatrix.get(rotationMatrix.size() - 1).setIdentity();
        prevRotationMatrix.get(prevRotationMatrix.size() - 1).setIdentity();
        scaleMatrix.get(scaleMatrix.size() - 1).setIdentity();
        prevScaleMatrix.get(prevScaleMatrix.size() - 1).setIdentity();
        Log.w("addInstance", "Instance added. Instance count: "+instanceCount);
    }

    public void instance(int index){
        position.add(position.get(index).copy());
        rotation.add(rotation.get(index).copy());
        scale.add(scale.get(index).copy());
        prevPosition.add(prevPosition.get(index).copy());
        prevRotation.add(prevRotation.get(index).copy());
        prevScale.add(prevScale.get(index).copy());
        hasChanged.add(hasChanged.get(index));
        translationMatrix.add(translationMatrix.get(index).copy());
        prevTranslationMatrix.add(prevTranslationMatrix.get(index).copy());
        rotationMatrix.add(rotationMatrix.get(index).copy());
        prevRotationMatrix.add(prevRotationMatrix.get(index).copy());
        scaleMatrix.add(scaleMatrix.get(index).copy());
        prevScaleMatrix.add(prevScaleMatrix.get(index).copy());
        colliders.add(colliders.get(index).copy());
        show.add(show.get(index));
        castShadow.add(castShadow.get(index));
        material.add(new Material());
        instanceCount++;
        Log.w("addInstance", "Instance added. Instance count: "+instanceCount);
    }

    public void removeInstance(int index){
        position.remove(index);
        rotation.remove(index);
        scale.remove(index);
        prevPosition.remove(index);
        prevRotation.remove(index);
        prevScale.remove(index);
        hasChanged.remove(index);
        translationMatrix.remove(index);
        prevTranslationMatrix.remove(index);
        rotationMatrix.remove(index);
        prevRotationMatrix.remove(index);
        scaleMatrix.remove(index);
        prevScaleMatrix.remove(index);
        colliders.remove(index);
        show.remove(index);
        castShadow.remove(index);
        material.remove(index);
        instanceCount--;
        Log.w("removeInstance", "Instance removed. Instance count" + instanceCount);
    }

    public void callNewFrame(){
        prevPosition = (ArrayList<Vector3f>) position.clone();
        prevRotation = (ArrayList<Vector3f>) rotation.clone();
        prevScale = (ArrayList<Vector3f>) scale.clone();
        prevTranslationMatrix = (ArrayList<Matrix4f>) translationMatrix.clone();
        prevRotationMatrix = (ArrayList<Matrix4f>) rotationMatrix.clone();
        prevScaleMatrix = (ArrayList<Matrix4f>) scaleMatrix.clone();
        for (boolean a : hasChanged) {
            a = true;
        }
    }

    public void setPosition(Vector3f pos, int index) {
        position.set(index,pos.copy());
        translationMatrix.get(index).setIdentity();
        translationMatrix.get(index).translate(pos);
        hasChanged.set(index,true);
    }

    public void setScale(Vector3f sc, int index) {
        scale.set(index,sc.copy());
        scaleMatrix.get(index).setIdentity();
        scaleMatrix.get(index).scale(sc);
        hasChanged.set(index,true);
    }

    public void setRotation(Vector3f rot, int index) {
        rotation.set(index,rot.copy());
        rotationMatrix.get(0).setIdentity();
        rotationMatrix.get(0).rotate(rot);
        hasChanged.set(index,true);
    }

    public Vector3f getPosition(int index) {
        return position.get(index);
    }

    public Vector3f getRotation(int index) {
        return rotation.get(index);
    }

    public Vector3f getScale(int index) {
        return scale.get(index);
    }

    public Vector3f getPrevPosition(int index) {
        return prevPosition.get(index);
    }

    public Vector3f getPrevRotation(int index) {
        return prevRotation.get(index);
    }

    public Vector3f getPrevScale(int index) {
        return prevScale.get(index);
    }

    public Matrix4f getTransformMatrix(int index) {
        return Matrix4f.MultiplyMM(translationMatrix.get(index),Matrix4f.MultiplyMM(rotationMatrix.get(index),scaleMatrix.get(index)));
    }

    public Matrix4f getPrevTransformMatrix(int index) {
        return Matrix4f.MultiplyMM(prevTranslationMatrix.get(index),Matrix4f.MultiplyMM(prevRotationMatrix.get(index),prevScaleMatrix.get(index)));
    }

    public Matrix4f getRotationMatrix(int index) {
        return rotationMatrix.get(index);
    }

    public void move(Vector3f add,int index){
        position.get(index).add(add);
        translationMatrix.get(index).translate(add);
        hasChanged.set(index,true);
    }

    public void rotate(Vector3f add, int index){
        rotation.get(index).add(add);
        rotationMatrix.get(index).rotate(add);
        hasChanged.set(index,true);
    }

    public void scale(Vector3f add, int index) {
        scale.get(index).add(add);
        scaleMatrix.get(index).scale(add);
        hasChanged.set(index, true);
    }

    public Entity copy(){
        return new Entity(this);
    }

    public void setMaterial(int index, Material material){
        this.material.set(index,material);
    }

    public Raycast rayCast(int index, Raycast r){
        if(colliders.get(index) != null) {

            Matrix4f mat = getTransformMatrix(index);
            Matrix4f matb = getRotationMatrix(index);
            Matrix4f iMat = Matrix4f.inverse(mat);
            Matrix4f iMatb = Matrix4f.inverse(matb);

            Raycast res = colliders.get(index).rayCast( new Raycast( Matrix4f.MultiplyMV(iMat, r.pos), Matrix4f.MultiplyMV(iMatb, r.dir) ) );

            if(res.hit) {
                Vector3f pos1 = null;
                Vector3f pos2 = null;
                if (res.hitpos.size() > 1 && res.hitpos.get(0) != null) {
                    pos1 = res.hitpos.get(0);
                    pos1 = Matrix4f.MultiplyMV(mat, pos1);
                }
                if (res.hitpos.size() > 2 && res.hitpos.get(1) != null) {
                    pos2 = res.hitpos.get(1);
                    pos2 = Matrix4f.MultiplyMV(mat, pos2);
                }

                if (r.numberOfHits > 0) {
                    boolean putPos1 = false;
                    boolean putPos2 = false;
                    for (int i = 0; i < r.numberOfHits && !putPos2; i++) {
                        if (pos1 != null && Vector3f.distance(pos1, r.pos) < Vector3f.distance(r.hitpos.get(i), r.pos) && !putPos1) {
                            r.addHitPos(i, pos1);
                            r.addHitEntity(i, this, index);
                            i++;
                            putPos1 = true;
                        }
                        if (pos2 != null && Vector3f.distance(pos2, r.pos) < Vector3f.distance(r.hitpos.get(i), r.pos) && !putPos2) {
                            r.addHitPos(i, pos2);
                            r.addHitEntity(i, this, index);
                            putPos2 = true;
                        }
                    }
                } else {
                    if (pos1 != null) {
                        r.addHitEntity(0, this, index);
                        r.addHitPos(0, pos1);
                    }
                    if (pos2 != null) {
                        r.addHitEntity(1, this, index);
                        r.addHitPos(1, pos2);
                    }
                }
            }

            return r;
        }else{
            return r;
        }
    }

    public void calculateBoundingBox() {
        Vector3f max = new Vector3f(-Float.MAX_VALUE);
        Vector3f min = new Vector3f(Float.MAX_VALUE);
        for (int i = 0; i < model.indices.length; i++) {
            if (model.positions[model.indices[i] * 3 + 0] > max.x) {
                max.x = model.positions[model.indices[i] * 3 + 0];
            } else if (model.positions[model.indices[i] * 3 + 0] < min.x) {
                min.x = model.positions[model.indices[i] * 3 + 0];
            }

            if (model.positions[model.indices[i] * 3 + 1] > max.y) {
                max.y = model.positions[model.indices[i] * 3 + 1];
            } else if (model.positions[model.indices[i] * 3 + 1] < min.y) {
                min.y = model.positions[model.indices[i] * 3 + 1];
            }

            if (model.positions[model.indices[i] * 3 + 2] > max.z) {
                max.z = model.positions[model.indices[i] * 3 + 2];
            } else if (model.positions[model.indices[i] * 3 + 2] < min.z) {
                min.z = model.positions[model.indices[i] * 3 + 2];
            }
        }

        boundingBox = new BoundingBox(Vector3f.sub(max,min));
        boundingBox.setCenter( Vector3f.add( Vector3f.scale( boundingBox.getRadius(),0.5f ), min ) );
    }

    public void alignCollidersToBoundingBox(){
        for (Collider collider: colliders) {
            collider.radius = Vector3f.scale(boundingBox.getRadius().copy(),0.5f);
            collider.setPosition(boundingBox.getCenter().copy());
        }
    }
}
