package com.glu.engine.Objects;

import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector2f;
import com.glu.engine.vectors.Vector3f;

public class Collider {
    public enum Shape{
        BOX,
        SPHERE
    }

    public Shape shape;
    public Vector3f radius;
    public Vector3f position;
    public Vector3f rotation;
    public Vector3f scale;
    public Matrix4f translationMat = new Matrix4f();
    public Matrix4f rotationMat =  new Matrix4f();
    public Matrix4f scaleMat = new Matrix4f();
    public Matrix4f transformMat = new Matrix4f();
    private boolean hasChanged = true;

    public Collider(){
        translationMat.setIdentity();
        rotationMat.setIdentity();
        scaleMat.setIdentity();
        setShapeBox(new Vector3f(1));
    }

    public void setPosition(Vector3f pos){
        position = pos.copy();
        translationMat.setIdentity();
        translationMat.translate(position);
        hasChanged = true;
    }

    public void setRotation(Vector3f rot){
        rotation = rot.copy();
        rotationMat.setIdentity();
        rotationMat.rotate(rot);
        hasChanged = true;
    }

    public void setScale(Vector3f sc){
        scale = sc.copy();
        scaleMat.setIdentity();
        scaleMat.scale(scale);
        hasChanged = true;
    }

    public void translate(Vector3f add){
        position.add(add);
        translationMat.translate(add);
        hasChanged = true;
    }

    public void rotate(Vector3f rot){
        rotation.add(rot);
        rotationMat.rotate(rot);
        hasChanged = true;
    }

    public void scale(Vector3f sc){
        scale.add(sc);
        scaleMat.scale(sc);
        hasChanged = true;
    }

    public Matrix4f getTransformMat(){
        if(hasChanged) {
            transformMat = Matrix4f.MultiplyMM(translationMat, Matrix4f.MultiplyMM(rotationMat, scaleMat));
        }

        return transformMat;
    }

    public void setShapeSphere(Vector3f radius){
        shape = Shape.SPHERE;
        this.radius = radius;
    }

    public void setShapeBox(Vector3f radius){
        shape = Shape.BOX;
        this.radius = radius;
    }

    public Raycast rayCast(Raycast ray){
        Matrix4f transform = getTransformMat();
        Matrix4f iTransform = Matrix4f.inverse(transform);
        Matrix4f iRotate = Matrix4f.inverse(rotationMat);
        
        Vector3f[] hitPoints = new Vector3f[2];
        Vector3f pos = Matrix4f.MultiplyMV(iTransform,ray.pos);
        Vector3f dir = Matrix4f.MultiplyMV(iRotate, ray.dir);
        switch (shape){
            case BOX:
                if(true) {
                    // max(|x|/a , |y|/b , |z|/c) = 1
                    // cube
                    float ca = radius.x;
                    float cb = radius.y;
                    float cc = radius.z;

                    // y = mx+b et z = mx+b
                    // droite
                    float my = dir.y/dir.x;
                    float by = pos.y - (pos.x * my);
                    float mz = dir.z/dir.x;
                    float bz = pos.z - (pos.x * mz);

                    // max(|x|/a, |my*x + by|/b , |mz*x + bz|/c) = 1
                    // 6 solutions possibles
                    float x1 = ca;
                    float x2 = -ca;
                    float x3 = (cb - by) / my;
                    float x4 = (-cb - by) / my;
                    float x5 = (cc - bz) / mz;
                    float x6 = (-cc - bz) / mz;

                    // x1-x6, remplacés dans l'équation du cube, donnent 1
                    // maintenant, ces points su la droite, sont-ils à l'intérieur du cube?

                    // une droite traversant un cube ne peut donner au plus que deux réponse (ou une infinité, ce qui équivaux à dire aucune)
                    Vector3f pos1 = null;
                    Vector3f pos2 = null;

                    Vector3f éval;

                    éval = new Vector3f(x1,x1*my + by, x1*mz + bz);
                    if( éval.x >= -ca && éval.x <= ca && éval.y >= -cb && éval.y <= cb && éval.z >= -cc && éval.z <= cc){
                        if(pos1 == null){
                            pos1 = éval;
                        }else{
                            pos2 = éval;
                        }
                    }
                    éval = new Vector3f(x2,x2*my + by, x2*mz + bz);
                    if( éval.x >= -ca && éval.x <= ca && éval.y >= -cb && éval.y <= cb && éval.z >= -cc && éval.z <= cc){
                        if(pos1 == null){
                            pos1 = éval;
                        }else{
                            pos2 = éval;
                        }
                    }
                    éval = new Vector3f(x3,x3*my + by, x3*mz + bz);
                    if( éval.x >= -ca && éval.x <= ca && éval.y >= -cb && éval.y <= cb && éval.z >= -cc && éval.z <= cc){
                        if(pos1 == null){
                            pos1 = éval;
                        }else{
                            pos2 = éval;
                        }
                    }
                    éval = new Vector3f(x4,x4*my + by, x4*mz + bz);
                    if( éval.x >= -ca && éval.x <= ca && éval.y >= -cb && éval.y <= cb && éval.z >= -cc && éval.z <= cc){
                        if(pos1 == null){
                            pos1 = éval;
                        }else{
                            pos2 = éval;
                        }
                    }
                    éval = new Vector3f(x5,x5*my + by, x5*mz + bz);
                    if( éval.x >= -ca && éval.x <= ca && éval.y >= -cb && éval.y <= cb && éval.z >= -cc && éval.z <= cc){
                        if(pos1 == null){
                            pos1 = éval;
                        }else{
                            pos2 = éval;
                        }
                    }
                    éval = new Vector3f(x6,x6*my + by, x6*mz + bz);
                    if( éval.x >= -ca && éval.x <= ca && éval.y >= -cb && éval.y <= cb && éval.z >= -cc && éval.z <= cc){
                        if(pos1 == null){
                            pos1 = éval;
                        }else{
                            pos2 = éval;
                        }
                    }

                    float distanceA = Float.POSITIVE_INFINITY;
                    float distanceB = Float.POSITIVE_INFINITY;

                    if(pos1 != null) {
                        distanceA = Vector3f.distance(pos, pos1);
                    }
                    if(pos2 != null) {
                        distanceB = Vector3f.distance(pos, pos2);
                    }

                    if(distanceA < distanceB){
                        if(pos1 != null) {
                            hitPoints[0] = Matrix4f.MultiplyMV(transform,pos1);
                        }
                        if(pos2 != null) {
                            hitPoints[1] = Matrix4f.MultiplyMV(transform,pos2);
                        }
                    }else {
                        if(pos2 != null) {
                            hitPoints[0] = Matrix4f.MultiplyMV(transform,pos2);
                        }
                        if(pos1 != null) {
                            hitPoints[1] =Matrix4f.MultiplyMV(transform,pos1);
                        }
                    }
                }
                break;
            case SPHERE:
                if(true) {
                    // (x/a)^2 + (y/b)^2 + (z/c)^2 = 1
                    // ellipse
                    float ea = radius.x;
                    float eb = radius.y;
                    float ec = radius.z;

                    // y = mx+b et z = mx+b
                    // droite
                    float my = dir.y/dir.x;
                    float by = pos.y - (pos.x * my);
                    float mz = dir.z/dir.x;
                    float bz = pos.z - (pos.x * mz);

                    // x = ( -b +- sqrt( b^2 - 4ac) ) / 2a
                    // formule quadratique
                    float qa = ea * ea * ( ec * ec * my * my + eb * eb * mz * mz) + eb * eb * ec * ec;
                    float qb = 2f * ea * ea * (ec * ec * my * by + eb * eb * mz * bz);
                    float qc = ea * ea * (ec * ec * by * by + eb * eb *bz * bz - eb * eb * ec * ec);

                    // b^2 - 4ac
                    float discriminant = qb * qb - 4f * qa * qc;

                    if (discriminant < 0f) {
                        break;
                    } else if (discriminant == 0f) {
                        float x = (-qb + (float) Math.sqrt(discriminant)) / (2f * qa);

                        hitPoints[0] = Matrix4f.MultiplyMV(transform, new Vector3f(x, my * x + by, mz * x + bz));
                    } else if (discriminant > 0f) {
                        Vector3f a = new Vector3f(0);
                        Vector3f b = new Vector3f(0);

                        float x1 = (-qb + (float) Math.sqrt(discriminant)) / (2f * qa);

                        a.x = x1;
                        a.y = my * x1 + by;
                        a.z = mz * x1 + bz;

                        float x2 = (-qb - (float) Math.sqrt(discriminant)) / (2f * qa);

                        b.x = x2;
                        b.y = my * x2 + by;
                        b.z = mz * x2 + bz;

                        float distanceA = Vector3f.distance(a, pos);
                        float distanceB = Vector3f.distance(b, pos);

                        if (distanceA < distanceB) {
                            hitPoints[0] = Matrix4f.MultiplyMV(transform,a);
                            hitPoints[1] = Matrix4f.MultiplyMV(transform,b);
                        } else {
                            hitPoints[0] = Matrix4f.MultiplyMV(transform,b);
                            hitPoints[1] = Matrix4f.MultiplyMV(transform,a);
                        }
                    }
                }
                break;
        }

        Raycast res = new Raycast();
        res.pos = ray.pos;
        res.dir = ray.dir;
        if(hitPoints[0] != null) {
            res.addHitPos(hitPoints[0]);
        }
        if(hitPoints[1] != null) {
            res.addHitPos(hitPoints[1]);
        }
        return res;
    }

    public Collider copy(){
        Collider c = new Collider();
        c.shape = shape;
        c.position = position.copy();
        c.rotation = rotation.copy();
        c.scale = scale.copy();
        c.translationMat = translationMat.copy();
        c.rotationMat = translationMat.copy();
        c.scaleMat = scaleMat.copy();
        c.radius = radius;
        return c;
    }
}
