package com.glu.engine.Objects.CustomObjects;

import android.opengl.Matrix;
import android.util.Log;

import com.glu.engine.Objects.BoundingBox;
import com.glu.engine.Objects.Collider;
import com.glu.engine.Objects.Entity;
import com.glu.engine.Objects.Raycast;
import com.glu.engine.Scene.Ressources;
import com.glu.engine.Scene.Scene;
import com.glu.engine.actionManager.ActionManager;
import com.glu.engine.utils.Maths;
import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector2f;
import com.glu.engine.vectors.Vector3f;

public class RubikCube extends CustomObject{
    private Ressources ressources;

    private Entity[] blocs = new Entity[26];
    private Collider collider = new Collider();
    private BoundingBox BB = new BoundingBox();
    //private Entity ball;
    private byte catchedBlocIndex;
    private Vector2f cachedPointerPos = new Vector2f(0);
    private int catchedPointerIndex = -1;
    private State state = State.IDLE;
    private int rotAxis = 0;
    private Vector3f hitPos;
    private float prevAngle = 0;
    private float firstAngle = 0;
    private float angularVelocity = 0;
    private boolean snappedBlocs = false;

    private long lastFrameTime = 0;
    private float deltaTime = 0f;
    private float mixAmount = 1f;
    private boolean doneMixing = false;

    private int[] Xlayer = new int[26];
    private int[] Ylayer = new int[26];
    private int[] Zlayer = new int[26];

    private Vector3f prevPos;

    private enum State{
        IDLE,
        CATCHED,
        MOVING
    }

    public RubikCube(Entity[] blocs){
        super();
        ressources = Ressources.getRessources();
        if(blocs.length == 26) {
            this.blocs = blocs;
        }else {
            Log.e("RubikCube", "You need to give exactly 26 entities. You gave " + blocs.length);
            System.exit(-1);
        }

        Vector3f max = new Vector3f(-Float.MAX_VALUE);
        Vector3f min = new Vector3f(Float.MAX_VALUE);

        for (int i = 0; i < blocs.length; i++) {
            if(blocs[i].boundingBox.getMax().x * blocs[i].getScale(0).x > max.x){
                max.x = blocs[i].boundingBox.getMax().x * blocs[i].getScale(0).x;
            }
            if(blocs[i].boundingBox.getMax().y * blocs[i].getScale(0).y > max.y){
                max.y = blocs[i].boundingBox.getMax().y * blocs[i].getScale(0).y;
            }
            if(blocs[i].boundingBox.getMax().z * blocs[i].getScale(0).z > max.z){
                max.z = blocs[i].boundingBox.getMax().z * blocs[i].getScale(0).z;
            }

            if(blocs[i].boundingBox.getMin().x * blocs[i].getScale(0).x < min.x){
                min.x = blocs[i].boundingBox.getMin().x * blocs[i].getScale(0).x;
            }
            if(blocs[i].boundingBox.getMin().y * blocs[i].getScale(0).y < min.y){
                min.y = blocs[i].boundingBox.getMin().y * blocs[i].getScale(0).y;
            }
            if(blocs[i].boundingBox.getMin().z * blocs[i].getScale(0).z < min.z){
                min.z = blocs[i].boundingBox.getMin().z * blocs[i].getScale(0).z;
            }
        }

        BB.setDiameter(Vector3f.sub(max,min));
        BB.setCenter( Vector3f.add( Vector3f.scale( BB.getDiameter(),0.5f ), min ) );

        collider.radius = Vector3f.scale(BB.getDiameter(),0.5f);
        collider.setPosition(BB.getCenter().copy());

        for (int i = 0; i < blocs.length; i++) {
            char X = blocs[i].model.name.charAt(blocs[i].model.name.length() - 1 - 2);
            char Y = blocs[i].model.name.charAt(blocs[i].model.name.length() - 1 - 1);
            char Z = blocs[i].model.name.charAt(blocs[i].model.name.length() - 1 - 0);
            if (X == '+'){
                Xlayer[i] = 1;
            }else if( X == '0'){
                Xlayer[i] = 0;
            }else if( X == '-'){
                Xlayer[i] = -1;
            }

            if (Y == '+'){
                Ylayer[i] = 1;
            }else if( Y == '0'){
                Ylayer[i] = 0;
            }else if( Y == '-'){
                Ylayer[i] = -1;
            }

            if (Z == '+'){
                Zlayer[i] = 1;
            }else if( Z == '0'){
                Zlayer[i] = 0;
            }else if( Z == '-'){
                Zlayer[i] = -1;
            }
        }
    }

    public boolean update(Scene scene, int pointerIndex){

        if(pointerIndex == catchedPointerIndex || catchedPointerIndex == -1) {

            deltaTime = (float) (System.currentTimeMillis() - lastFrameTime) / 1000f;
            if(deltaTime < 0.00001f){
                deltaTime = 0.00001f;
            }
            lastFrameTime = System.currentTimeMillis();
            //Log.w("cube","deltaTime : " + deltaTime + " aV : " + angularVelocity);

            ActionManager actionManager = scene.actionManager;
            Matrix4f proj = scene.PROJECTION_MATRIX;
            Matrix4f iProj = Matrix4f.inverse(proj);
            Matrix4f rView = scene.camera.getRotationMat();
            Matrix4f irView = rView.copy();
            irView.inverse();

            if (state == State.IDLE && actionManager.isTouching[pointerIndex] && actionManager.pointerNumber == 1) {

                Vector3f pointer = new Vector3f((actionManager.startPosition[pointerIndex].x / ressources.viewport.x) * 2f - 1f, (actionManager.startPosition[pointerIndex].y / ressources.viewport.y) * 2f - 1f, 0);
                pointer = Matrix4f.MultiplyMV(iProj, pointer);
                pointer = Matrix4f.MultiplyMV(irView, pointer);

                Vector3f rayPos = scene.camera.getPosition().copy();

                Raycast raycast = new Raycast(rayPos, pointer);

                raycast = collider.rayCast(raycast);
                Raycast result = new Raycast(raycast.pos, raycast.dir);
                if (raycast.hit) {
                    if (!snappedBlocs) {
                        float angle = prevAngle - firstAngle;
                        angle = ((angle / 360f) - (float) Math.floor(angle / 360f)) * 360f;
                        for (int i = 0; i < blocs.length; i++) {
                            if (rotAxis == 1 && Xlayer[catchedBlocIndex] == Xlayer[i]) {
                                int y = Ylayer[i];
                                int z = Zlayer[i];
                                if (angle > 315f) {
                                } else if (angle > 225f) {
                                    Ylayer[i] = -z;
                                    Zlayer[i] = y;
                                } else if (angle > 135f) {
                                    Ylayer[i] = -y;
                                    Zlayer[i] = -z;
                                } else if (angle > 45f) {
                                    Ylayer[i] = z;
                                    Zlayer[i] = -y;
                                } else if (angle > -45f) {
                                } else if (angle > -135f) {
                                    Ylayer[i] = -z;
                                    Zlayer[i] = y;
                                } else if (angle > -225f) {
                                    Ylayer[i] = -y;
                                    Zlayer[i] = -z;
                                } else if (angle > -315f) {
                                    Ylayer[i] = z;
                                    Zlayer[i] = -y;
                                }
                            } else if (rotAxis == 2 && Ylayer[catchedBlocIndex] == Ylayer[i]) {
                                Log.w("rotate", "rotate face");
                                int z = Zlayer[i];
                                int x = Xlayer[i];
                                if (angle > 315f) {
                                } else if (angle > 225f) {
                                    Zlayer[i] = -x;
                                    Xlayer[i] = z;
                                } else if (angle > 135f) {
                                    Zlayer[i] = -z;
                                    Xlayer[i] = -x;
                                } else if (angle > 45f) {
                                    Zlayer[i] = x;
                                    Xlayer[i] = -z;
                                } else if (angle > -45f) {
                                } else if (angle > -135f) {
                                    Zlayer[i] = -x;
                                    Xlayer[i] = z;
                                } else if (angle > -225f) {
                                    Zlayer[i] = -z;
                                    Xlayer[i] = -x;
                                } else if (angle > -315f) {
                                    Zlayer[i] = x;
                                    Xlayer[i] = -z;
                                }
                            } else if (rotAxis == 3 && Zlayer[catchedBlocIndex] == Zlayer[i]) {
                                int y = Ylayer[i];
                                int x = Xlayer[i];
                                if (angle > 315f) {
                                } else if (angle > 225f) {
                                    Ylayer[i] = x;
                                    Xlayer[i] = -y;
                                } else if (angle > 135f) {
                                    Ylayer[i] = -y;
                                    Xlayer[i] = -x;
                                } else if (angle > 45f) {
                                    Ylayer[i] = -x;
                                    Xlayer[i] = y;
                                } else if (angle > -45f) {
                                } else if (angle > -135f) {
                                    Ylayer[i] = x;
                                    Xlayer[i] = -y;
                                } else if (angle > -225f) {
                                    Ylayer[i] = -y;
                                    Xlayer[i] = -x;
                                } else if (angle > -315f) {
                                    Ylayer[i] = -x;
                                    Xlayer[i] = y;
                                }
                            }
                        }

                        snapBlocs(1.0f);
                        snappedBlocs = true;
                    }
                    for (int i = 0; i < blocs.length; i++) {
                        result = blocs[i].rayCast(0, result);
                    }

                    if(result.hit) {
                        Entity catchedBloc = result.hitEntities.get(0);
                        for (int i = 0; i < blocs.length; i++) {
                            if (blocs[i] == catchedBloc) {
                                catchedBlocIndex = (byte) i;
                            }
                        }
                        state = State.CATCHED;
                        cachedPointerPos = actionManager.lastPoint[pointerIndex];
                        catchedPointerIndex = pointerIndex;
                        hitPos = result.hitpos.get(0);
                        hitPos = Matrix4f.MultiplyMV(Matrix4f.inverse(catchedBloc.getTransformMatrix(0)), hitPos);
                        prevAngle = 0;
                        prevPos = hitPos;
                        return true;
                    }
                }
            }
            else if (state == State.CATCHED && Vector2f.distance(cachedPointerPos, actionManager.lastPoint[pointerIndex]) > 10) {
                Vector3f X = new Vector3f(1, 0, 0);
                Vector3f Y = new Vector3f(0, 1, 0);
                Vector3f Z = new Vector3f(0, 0, 1);

                X = Matrix4f.MultiplyMV(rView, X);
                X = Matrix4f.MultiplyMV(proj, X);
                Y = Matrix4f.MultiplyMV(rView, Y);
                Y = Matrix4f.MultiplyMV(proj, Y);
                Z = Matrix4f.MultiplyMV(rView, Z);
                Z = Matrix4f.MultiplyMV(proj, Z);

                Vector2f vX = Vector2f.normalize(new Vector2f(X.x, X.y));
                Vector2f vY = Vector2f.normalize(new Vector2f(Y.x, Y.y));
                Vector2f vZ = Vector2f.normalize(new Vector2f(Z.x, Z.y));

                float dotX = Math.abs(Vector2f.dot(vX, Vector2f.normalize(Vector2f.sub(actionManager.lastPoint[0], cachedPointerPos))));
                float dotY = Math.abs(Vector2f.dot(vY, Vector2f.normalize(Vector2f.sub(actionManager.lastPoint[0], cachedPointerPos))));
                float dotZ = Math.abs(Vector2f.dot(vZ, Vector2f.normalize(Vector2f.sub(actionManager.lastPoint[0], cachedPointerPos))));

                if (dotX > dotY && dotX > dotZ) {
                    if (dotY > dotZ) {
                        rotAxis = 3;
                    } else {
                        rotAxis = 2;
                    }
                } else if (dotY > dotX && dotY > dotZ) {
                    if (dotX > dotZ) {
                        rotAxis = 3;
                    } else {
                        rotAxis = 1;
                    }
                } else if (dotZ > dotX && dotZ > dotY) {
                    if (dotX > dotY) {
                        rotAxis = 2;
                    } else {
                        rotAxis = 1;
                    }
                }

                state = State.MOVING;
                snappedBlocs = false;

                return true;
            }
            else if (state == State.MOVING) {

                if (!actionManager.isTouching[pointerIndex]) {

                    for (int j = 0; j < 8; j++) {
                        float sign = Math.signum(angularVelocity);
                        angularVelocity -= Math.signum(angularVelocity) * 45f * deltaTime;
                        prevAngle += angularVelocity * deltaTime;
                        if (rotAxis == 1) {
                            for (int i = 0; i < Xlayer.length; i++) {
                                if (Xlayer[i] == Xlayer[catchedBlocIndex]) {
                                    blocs[i].rotate(new Vector3f(angularVelocity * deltaTime, 0f, 0f), 0);
                                }
                            }
                        }
                        if (rotAxis == 2) {
                            for (int i = 0; i < Ylayer.length; i++) {
                                if (Ylayer[i] == Ylayer[catchedBlocIndex]) {
                                    blocs[i].rotate(new Vector3f(0f, angularVelocity * deltaTime, 0f), 0);
                                }
                            }
                        }
                        if (rotAxis == 3) {
                            for (int i = 0; i < Zlayer.length; i++) {
                                if (Zlayer[i] == Zlayer[catchedBlocIndex]) {
                                    blocs[i].rotate(new Vector3f(0f, 0f, -angularVelocity * deltaTime), 0);
                                }
                            }
                        }
                    }

                    state = State.IDLE;
                    catchedPointerIndex = -1;
                }
                else {
                    Vector3f pos = Matrix4f.MultiplyMV(scene.camera.getViewMat(), Matrix4f.MultiplyMV(blocs[catchedBlocIndex].getTransformMatrix(0), hitPos));
                    pos = Matrix4f.MultiplyMV(proj, pos);

                    pos = new Vector3f((actionManager.lastPoint[pointerIndex].x / ressources.viewport.x) * 2f - 1f, (actionManager.lastPoint[pointerIndex].y / ressources.viewport.y) * 2f - 1f, pos.z);

                    pos = Matrix4f.MultiplyMV(iProj, pos);
                    pos = Matrix4f.MultiplyMV(Matrix4f.inverse(scene.camera.getViewMat()), pos);

                    if (rotAxis == 1) {
                        float moveAngle = (float) Math.atan2(pos.z, pos.y) * (180f / 3.1416f);

                        if (prevAngle == 0) {
                            prevAngle = moveAngle;
                            firstAngle = moveAngle;
                        }

                        float angle = moveAngle - prevAngle;

                        for (int i = 0; i < blocs.length; i++) {
                            if (Xlayer[i] == Xlayer[catchedBlocIndex]) {
                                blocs[i].rotate(new Vector3f(angle, 0, 0), 0);
                            }
                        }

                        angularVelocity = (moveAngle - prevAngle) / deltaTime;
                        prevAngle = moveAngle;
                    }
                    else if (rotAxis == 2) {
                        float moveAngle = (float) Math.atan2(pos.x, pos.z) * (180f / 3.1416f);

                        if (prevAngle == 0) {
                            prevAngle = moveAngle;
                            firstAngle = moveAngle;
                        }

                        float angle = moveAngle - prevAngle;

                        for (int i = 0; i < blocs.length; i++) {
                            if (Ylayer[i] == Ylayer[catchedBlocIndex]) {
                                blocs[i].rotate(new Vector3f(0, angle, 0), 0);
                            }
                        }

                        angularVelocity = (moveAngle - prevAngle) / deltaTime;
                        prevAngle = moveAngle;
                    }
                    else if (rotAxis == 3) {
                        float moveAngle = (float) Math.atan2(pos.x, pos.y) * (180f / 3.1416f);

                        if (prevAngle == 0) {
                            prevAngle = moveAngle;
                            firstAngle = moveAngle;
                        }

                        float angle = moveAngle - prevAngle;

                        for (int i = 0; i < blocs.length; i++) {
                            if (Zlayer[i] == Zlayer[catchedBlocIndex]) {
                                blocs[i].rotate(new Vector3f(0, 0, -angle), 0);
                            }
                        }

                        angularVelocity = (moveAngle - prevAngle) / deltaTime;
                        prevAngle = moveAngle;
                    }

                    //if (ball != null) {
                    //    ball.setPosition(pos, 0);
                    //}
                }

                return true;
            }
            else if (state == State.CATCHED) {
                if(!actionManager.isTouching[pointerIndex]){
                    catchedPointerIndex = -1;
                    state = State.IDLE;
                }
                return true;
            }
            else if (!actionManager.isTouching[pointerIndex]) {

                snapBlocs(0.01f);
                state = State.IDLE;
                catchedPointerIndex = -1;
                return false;
            }
        }
        return false;
    }

    private void snapBlocs(float mSpeed){
        float mixSpeed = (deltaTime * (1f - mSpeed)) + mSpeed;
        if(mixAmount > 0.1f) {
            doneMixing = false;
            for (Entity bloc : blocs) {
                Vector3f deltaR = new Vector3f(0);
                Vector3f blocRotation = bloc.getRotation(0);
                float rotation = blocRotation.x;
                rotation = ((rotation / 360f) - (float) Math.floor(rotation / 360f)) * 360f;
                float snap0 = rotation;
                float snap90 = Math.abs(rotation - 90f);
                float snap180 = Math.abs(rotation - 180f);
                float snap270 = Math.abs(rotation - 270f);
                float snap360 = Math.abs(rotation - 360f);

                if (snap0 <= snap90 && snap0 <= snap180 && snap0 <= snap270 && snap0 <= snap360) {
                    rotation = (rotation * (1f - mixSpeed));
                }
                else if (snap90 <= snap0 && snap90 <= snap180 && snap90 <= snap270 && snap90 <= snap360) {
                    rotation = (rotation * (1f - mixSpeed)) + (90f * mixSpeed);
                }
                else if (snap180 <= snap0 && snap180 <= snap90 && snap180 <= snap270 && snap180 <= snap360) {
                    rotation = (rotation * (1f - mixSpeed)) + (180f * mixSpeed);
                }
                else if (snap270 <= snap0 && snap270 <= snap90 && snap270 <= snap180 && snap270 <= snap360) {
                    rotation = (rotation * (1f - mixSpeed)) + (270f * mixSpeed);
                }
                else {
                    rotation = (rotation * (1f - mixSpeed)) + (360f * mixSpeed);
                }

                deltaR.x = rotation - bloc.getRotation(0).x;

                rotation = blocRotation.y;
                rotation = ((rotation / 360f) - (float) Math.floor(rotation / 360f)) * 360f;
                snap0 = rotation;
                snap90 = Math.abs(rotation - 90f);
                snap180 = Math.abs(rotation - 180f);
                snap270 = Math.abs(rotation - 270f);
                snap360 = Math.abs(rotation - 360f);

                if (snap0 <= snap90 && snap0 <= snap180 && snap0 <= snap270 && snap0 <= snap360) {
                    rotation = (rotation * (1f - mixSpeed));
                }
                else if (snap90 <= snap0 && snap90 <= snap180 && snap90 <= snap270 && snap90 <= snap360) {
                    rotation = (rotation * (1f - mixSpeed)) + (90f * mixSpeed);
                }
                else if (snap180 <= snap0 && snap180 <= snap90 && snap180 <= snap270 && snap180 <= snap360) {
                    rotation = (rotation * (1f - mixSpeed)) + (180f * mixSpeed);
                }
                else if (snap270 <= snap0 && snap270 <= snap90 && snap270 <= snap180 && snap270 <= snap360) {
                    rotation = (rotation * (1f - mixSpeed)) + (270f * mixSpeed);
                }
                else {
                    rotation = (rotation * (1f - mixSpeed)) + (360f * mixSpeed);
                }

                deltaR.y = rotation - bloc.getRotation(0).y;

                rotation = blocRotation.z;
                rotation = ((rotation / 360f) - (float) Math.floor(rotation / 360f)) * 360f;
                snap0 = rotation;
                snap90 = Math.abs(rotation - 90f);
                snap180 = Math.abs(rotation - 180f);
                snap270 = Math.abs(rotation - 270f);
                snap360 = Math.abs(rotation - 360f);

                if (snap0 <= snap90 && snap0 <= snap180 && snap0 <= snap270 && snap0 <= snap360) {
                    rotation = (rotation * (1f - mixSpeed));
                }
                else if (snap90 <= snap0 && snap90 <= snap180 && snap90 <= snap270 && snap90 <= snap360) {
                    rotation = (rotation * (1f - mixSpeed)) + (90f * mixSpeed);
                }
                else if (snap180 <= snap0 && snap180 <= snap90 && snap180 <= snap270 && snap180 <= snap360) {
                    rotation = (rotation * (1f - mixSpeed)) + (180f * mixSpeed);
                }
                else if (snap270 <= snap0 && snap270 <= snap90 && snap270 <= snap180 && snap270 <= snap360) {
                    rotation = (rotation * (1f - mixSpeed)) + (270f * mixSpeed);
                }
                else {
                    rotation = (rotation * (1f - mixSpeed)) + (360f * mixSpeed);
                }

                deltaR.z = rotation - bloc.getRotation(0).z;

                if (deltaR.x != 0f || deltaR.y != 0f || deltaR.z != 0f) {
                    bloc.rotate(deltaR, 0);
                }
            }
        } else if (!doneMixing) {
            for (Entity bloc : blocs) {
                Vector3f deltaR = new Vector3f(0);
                Vector3f blocRotation = bloc.getRotation(0);
                float rotation = blocRotation.x;
                rotation = ((rotation / 360f) - (float) Math.floor(rotation / 360f)) * 360f;
                float snap0 = rotation;
                float snap90 = Math.abs(rotation - 90f);
                float snap180 = Math.abs(rotation - 180f);
                float snap270 = Math.abs(rotation - 270f);
                float snap360 = Math.abs(rotation - 360f);

                if (snap0 <= snap90 && snap0 <= snap180 && snap0 <= snap270 && snap0 <= snap360) {
                    rotation = 0f;
                }
                else if (snap90 <= snap0 && snap90 <= snap180 && snap90 <= snap270 && snap90 <= snap360) {
                    rotation = 90f;
                }
                else if (snap180 <= snap0 && snap180 <= snap90 && snap180 <= snap270 && snap180 <= snap360) {
                    rotation = 180f;
                }
                else if (snap270 <= snap0 && snap270 <= snap90 && snap270 <= snap180 && snap270 <= snap360) {
                    rotation = 270f;
                }
                else {
                    rotation = 360f;
                }

                deltaR.x = rotation - bloc.getRotation(0).x;

                rotation = blocRotation.y;
                rotation = ((rotation / 360f) - (float) Math.floor(rotation / 360f)) * 360f;
                snap0 = rotation;
                snap90 = Math.abs(rotation - 90f);
                snap180 = Math.abs(rotation - 180f);
                snap270 = Math.abs(rotation - 270f);
                snap360 = Math.abs(rotation - 360f);

                if (snap0 <= snap90 && snap0 <= snap180 && snap0 <= snap270 && snap0 <= snap360) {
                    rotation = 0f;
                }
                else if (snap90 <= snap0 && snap90 <= snap180 && snap90 <= snap270 && snap90 <= snap360) {
                    rotation = 90f;
                }
                else if (snap180 <= snap0 && snap180 <= snap90 && snap180 <= snap270 && snap180 <= snap360) {
                    rotation = 180f;
                }
                else if (snap270 <= snap0 && snap270 <= snap90 && snap270 <= snap180 && snap270 <= snap360) {
                    rotation = 270f;
                }
                else {
                    rotation = 360f;
                }

                deltaR.y = rotation - bloc.getRotation(0).y;

                rotation = blocRotation.z;
                rotation = ((rotation / 360f) - (float) Math.floor(rotation / 360f)) * 360f;
                snap0 = rotation;
                snap90 = Math.abs(rotation - 90f);
                snap180 = Math.abs(rotation - 180f);
                snap270 = Math.abs(rotation - 270f);
                snap360 = Math.abs(rotation - 360f);

                if (snap0 <= snap90 && snap0 <= snap180 && snap0 <= snap270 && snap0 <= snap360) {
                    rotation = 0f;
                }
                else if (snap90 <= snap0 && snap90 <= snap180 && snap90 <= snap270 && snap90 <= snap360) {
                    rotation = 90f;
                }
                else if (snap180 <= snap0 && snap180 <= snap90 && snap180 <= snap270 && snap180 <= snap360) {
                    rotation = 180f;
                }
                else if (snap270 <= snap0 && snap270 <= snap90 && snap270 <= snap180 && snap270 <= snap360) {
                    rotation = 270f;
                }
                else {
                    rotation = 360f;
                }

                deltaR.z = rotation - bloc.getRotation(0).z;

                if (deltaR.x != 0f || deltaR.y != 0f || deltaR.z != 0f) {
                    bloc.rotate(deltaR, 0);
                }
            }
            doneMixing = true;
        }

        mixAmount = (mixAmount * (1f-mixSpeed)); // + 0f*mixSpeed

        if(mSpeed == 1f){
            mixAmount = 1f;
        }
    }

}
