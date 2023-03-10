package com.glu.engine.actionManager;

import android.opengl.Matrix;
import android.util.Log;

import com.glu.engine.GUI.Button;
import com.glu.engine.GUI.Slider;
import com.glu.engine.Objects.Collider;
import com.glu.engine.Objects.CustomObjects.RubikCube;
import com.glu.engine.Objects.Entity;
import com.glu.engine.Objects.Raycast;
import com.glu.engine.Scene.Scene;
import com.glu.engine.utils.Loader;
import com.glu.engine.utils.Maths;
import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector2f;
import com.glu.engine.vectors.Vector3f;

/*
*   The goal of this class is to take interpreted action, and inputs and
*   make them interact with the scene.
*/
public class InputManager {
    final ActionManager actionManager;
    final Scene scene;
    float deltaTime = 0f;
    long prevFrameTime = 0;
    //private Vector3f camCenter = new Vector3f(0);
    int movementType = 0;
    long timeOfLastMovement = System.currentTimeMillis();

    Entity ball;
    Entity RCCube;
    Entity collider;
    int movingEntityIndex;
    float lastAngle;
    int rotationAxis = 0;
    Vector3f hitpoint;
    RubikCube cube;

    public InputManager(ActionManager actionManager, Scene scene){
        this.actionManager = actionManager;
        this.scene = scene;
    }
    
    public void update() {

        deltaTime = (float)(System.currentTimeMillis() - prevFrameTime)/1000f;
        prevFrameTime = System.currentTimeMillis();

        if(System.currentTimeMillis() - timeOfLastMovement > 50){
            movementType = 0;
        }

        actionManager.manualUpdate();
        ActionManager.actionType action = actionManager.getAction(0);

        Slider camSlider = scene.getSlider("camSlider");
        Slider s = scene.getSlider("slider");
        if (ball == null){
            ball = scene.getEntity("ball");
        }

        for (int index = 0; index < ActionManager.MAX_POINTERS; index++) {

            boolean updatedAction = false; // if true, the action was handled and won't update other stuff (like clicking a menu won't update the background game)

            for (Button button : scene.Buttons) {
                for (int i = 0; i < button.name.size(); i++) {
                    if (actionManager.pointerIndices[index]) {
                        if (actionManager.isTouching[index] && !updatedAction) {
                            if (button.hasReleased.get(i)) {
                                button.checkPassAt(actionManager.lastPoint[index], i, index);
                                button.checkClickAt(actionManager.startPosition[index], i, index);
                            }
                            button.checkReleaseOut(actionManager.lastPoint[index], i, index);
                        } else {
                            button.checkReleaseIn(actionManager.lastPoint[index], i, index);
                            button.hasClickedOff.set(i, false);
                        }
                    }
                }
            }

            for (Slider slider : scene.Sliders) {
                if (actionManager.isTouching[index] && !updatedAction) {
                    if (!slider.isClicked) {
                        slider.click(actionManager.startPosition[index], index);
                    } else {
                        slider.click(actionManager.lastPoint[index], index);
                        updatedAction = true;
                    }
                } else {
                    slider.release(index);
                }
            }

            if(!updatedAction) {
                if(actionManager.isTouching[index] && actionManager.pointerNumber == 1 && (movementType == 0 || movementType == 1)) {
                    scene.camera.setRotation(Vector3f.add(new Vector3f(actionManager.velocity[index].y * 5f * deltaTime,-actionManager.velocity[index].x * 5f * deltaTime,0f),scene.camera.getRotation()));
                    //Log.w("rotate camera","action index: " + index);
                    //Log.w("rotate camera", "index: " + index + "last point: " + actionManager.lastPoint[index].x + " " + actionManager.lastPoint[index].y + " previous point: " + actionManager.previousPoint[index].x + " " + actionManager.previousPoint[index].y + " velocity: " + actionManager.velocity[index].x + " " + actionManager.velocity[index].y);
                    movementType = 1;
                    timeOfLastMovement = System.currentTimeMillis();
                }
            }
        }
        if(actionManager.pointerNumber == 2 && (movementType == 0 || movementType == 2)) {
            float zoom = Vector2f.distance(actionManager.previousPoint[0], actionManager.previousPoint[1]) - Vector2f.distance(actionManager.lastPoint[0],actionManager.lastPoint[1]);
            //scene.CamDist += zoom * 0.25f;

            Vector2f move = Vector2f.sub(Vector2f.scale(Vector2f.add(actionManager.previousPoint[0], actionManager.previousPoint[1]), 0.5f), Vector2f.scale(Vector2f.add(actionManager.lastPoint[0], actionManager.lastPoint[1]), 0.5f));

            Matrix4f m = new Matrix4f();
            m.setIdentity();
            Matrix.translateM(m.mat,0,m.mat,0,scene.camera.getPosition().x,scene.camera.getPosition().y,scene.camera.getPosition().z);
            Matrix.rotateM(m.mat, 0, m.mat,0, scene.camera.getRotation().y,0,1,0);
            Matrix.rotateM(m.mat, 0, m.mat,0, scene.camera.getRotation().x,1,0,0);
            Matrix.rotateM(m.mat, 0, m.mat,0, scene.camera.getRotation().z,0,0,1);
            Matrix.translateM(m.mat,0,m.mat,0,move.x * deltaTime,move.y * deltaTime, zoom * deltaTime);
            scene.camera.setPosition(Matrix4f.MultiplyMV(m,new Vector3f(0)));

            movementType = 2;
            timeOfLastMovement = System.currentTimeMillis();

        }

        Vector3f pointer = new Vector3f(0,0, -1);
        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        Matrix.rotateM(mat.mat, 0, mat.mat, 0, scene.camera.getRotation().y, 0, 1, 0);
        Matrix.rotateM(mat.mat, 0, mat.mat, 0, scene.camera.getRotation().x, 1, 0, 0);
        Matrix.rotateM(mat.mat, 0, mat.mat, 0, scene.camera.getRotation().z, 0, 0, 1);
        pointer = Matrix4f.MultiplyMV( mat, pointer);
        Raycast raycast = scene.raycast(scene.camera.getPosition(),pointer);
        if (raycast.hit){
            ball.setPosition(raycast.hitpos.get(0),0);
        }

        //if(actionManager.pointerNumber == 0){
        //    snapRCCube(0.1f);
        //}

        /*Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        Matrix.rotateM(mat.mat, 0, mat.mat, 0, scene.camera.getRotation().y, 0, 1, 0);
        Matrix.rotateM(mat.mat, 0, mat.mat, 0, scene.camera.getRotation().x, 1, 0, 0);
        Matrix.rotateM(mat.mat, 0, mat.mat, 0, scene.camera.getRotation().z, 0, 0, 1);

        Vector3f pointer = new Vector3f(0,0, -1);

        pointer = Matrix4f.MultiplyMV(mat, pointer);
        Raycast raycast = scene.raycast(scene.camera.getPosition(), pointer);
        if (raycast.hit) {
            ball.setPosition(raycast.hitpos.get(0).copy(), 0);
        }*/
        //ball.setPosition(new Vector3f((float) Math.cos( (float)scene.pp.inc / 10f) * 10f,0f,(float) Math.sin((float)scene.pp.inc / 10f) * 10f) , 0);

        //Matrix4f m = new Matrix4f();
        //m.setIdentity();
        //Matrix.translateM(m.mat, 0, scene.camera.getPosition().x, scene.camera.getPosition().y, scene.camera.getPosition().z);
        //Matrix.rotateM(m.mat, 0, scene.cameraRotation.x, 0, 1, 0);
        //Matrix.rotateM(m.mat, 0, scene.cameraRotation.y, 1, 0, 0);
        //scene.camera.setPosition(Matrix4f.MultiplyMV(m, new Vector3f(0, 0, 0)));
        //scene.camera.setRotation(Vector3f.lookAt(scene.camera.getPosition(), new Vector3f(scene.cameraRotation.x,scene.cameraRotation.y,0f), 0));
        //updatedAction = true;
    }
}
