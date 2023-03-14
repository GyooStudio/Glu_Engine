package com.glu.engine.actionManager;

import android.opengl.Matrix;
import android.util.Log;

import com.glu.engine.GUI.Bouton;
import com.glu.engine.GUI.ColorSquare;
import com.glu.engine.GUI.Glissoire;
import com.glu.engine.Objects.Collider;
import com.glu.engine.Objects.CustomObjects.RubikCube;
import com.glu.engine.Objects.Entity;
import com.glu.engine.Scene.Ressources;
import com.glu.engine.Scene.Scene;
import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector2f;
import com.glu.engine.vectors.Vector3f;
import com.glu.engine.vectors.Vector4f;

/**
 *   The goal of this class is to take interpreted action, and inputs and
 *   make them interact with the scene.
 */
public class InputManager {
    private final ActionManager actionManager;
    private Ressources ressources;
    public final Scene scene;
    private float deltaTime = 0f;
    private long prevFrameTime = 0;
    private Vector3f camCenter = new Vector3f(0f);
    private float camZoom = 5f;
    private int movementType = 0;
    private long timeOfLastMovement = System.currentTimeMillis();
    private Vector3f camDir = new Vector3f(0f);
    private Vector3f camUp = new Vector3f(0f);
    private Vector3f prevCamDir = new Vector3f(0f);
    private Vector3f camVel = new Vector3f(0f);
    private Vector3f camA = new Vector3f(0f);
    private float roll = 0f;

    public RubikCube cube;

    public Bouton bouton;

    public InputManager(Scene scene){
        this.actionManager = ActionManager.getActionManager();
        this.ressources = Ressources.getRessources();
        this.scene = scene;
    }

    public void update() {

        deltaTime = (float)(System.currentTimeMillis() - prevFrameTime)/1000f;

        if(System.currentTimeMillis() - timeOfLastMovement > 50){
            movementType = 0;
        }

        actionManager.manualUpdate();
        ActionManager.actionType action = actionManager.getAction(0);

        if(cube == null){
            Entity[] es = new Entity[26];
            int i = 0;
            for (Entity e : scene.Entities) {
                if(e.name.contains("Rubiks cube")){
                    e.calculateBoundingBox();
                    e.setCollider(0, Collider.Shape.BOX);
                    e.alignCollidersToBoundingBox();
                    es[i] = e;
                    i++;
                }
            }
            if(i > 25) {
                cube = new RubikCube(es);
            }
        }

        for (int index = 0; index < ActionManager.MAX_POINTERS; index++) {

            // if true, the action was handled and won't update other stuff (like clicking a menu won't update the background game)
            boolean updatedAction = false;

            for (Bouton bouton : scene.boutons) {
                if(!updatedAction) {
                    updatedAction = bouton.actualiser(index);
                }else{
                    break;
                }
            }

            for (Glissoire glissoire : scene.glissoires) {
                if(!updatedAction) {
                    updatedAction = glissoire.actualiser(index);
                }else{
                    break;
                }
            }

            if(!updatedAction && cube != null){
                updatedAction = cube.update(scene);
            }

            //Log.w("InputManager", "moving camera");
            //actionManager.log(index);
            if(!updatedAction) {
                if(actionManager.isTouching[index] && actionManager.pointerNumber == 1 && (movementType == 0 || movementType == 1)) {
                    scene.camera.setRotation(Vector3f.add(new Vector3f(actionManager.velocity[index].y * 10f * deltaTime,-actionManager.velocity[index].x * 10f * deltaTime,0f),scene.camera.getRotation()));
                    movementType = 1;
                    timeOfLastMovement = System.currentTimeMillis();
                }
            }
        }
        if(actionManager.pointerNumber == 2 && (movementType == 0 || movementType == 2)) {
            float zoom = Vector2f.distance(actionManager.previousPoint[0], actionManager.previousPoint[1]) - Vector2f.distance(actionManager.lastPoint[0],actionManager.lastPoint[1]);

            camZoom += zoom * deltaTime * 0.3f;

            movementType = 2;
            timeOfLastMovement = System.currentTimeMillis();

        }

        //Vector3f camRot = Vector3f.lookAt(new Vector3f(0f),camDir,0f);
        //scene.camera.setRotation(camRot);
        //scene.camera.rotate(new Vector3f(0f,0f,deltaTime));
        if(deltaTime < 0.5) {
            Vector3f X = new Vector3f((float) Math.cos(Math.toRadians(scene.camera.getRotation().y + 90f)), 0f,(float) Math.sin(Math.toRadians(scene.camera.getRotation().y + 90f)));
            Vector3f Z = new Vector3f((float) Math.cos(Math.toRadians(scene.camera.getRotation().y)), 0f,(float) Math.sin(Math.toRadians(scene.camera.getRotation().y)));
            Vector3f acceleration = Vector3f.add(Vector3f.scale(X, camA.x), Vector3f.scale(Z, camA.z));
            camVel.add(Vector3f.scale(acceleration, deltaTime));
            camVel.add(Vector3f.scale(camVel,0.1f).negative());
            scene.camera.move(Vector3f.add(Vector3f.scale(camVel, deltaTime), Vector3f.scale(acceleration, 0.5f * deltaTime * deltaTime)));
            //Log.w("camera", "position : x:" + scene.camera.getPosition().x + " y: " + scene.camera.getPosition().y + " z: " + scene.camera.getPosition().z + " velocité : x: " + camVel.x + " y: " + camVel.y + " z: " + camVel.z + " accélération: x: " + camA.x + " y: " + camA.y + " z: " + camA.z + " deltaTime:  " + deltaTime);
        }

        //camCenter = new Vector3f((float) Math.cos((double) System.currentTimeMillis() / 4000.0) * 3f, 0f, (float) Math.sin((double) System.currentTimeMillis() / 4000.0) * 3f );
        //Matrix4f m = new Matrix4f();
        //Matrix.translateM(m.mat,0,m.mat,0,camCenter.x,camCenter.y,camCenter.z);
        //Matrix.rotateM(m.mat, 0, m.mat,0, scene.camera.getRotation().y,0,1,0);
        //Matrix.rotateM(m.mat, 0, m.mat,0, scene.camera.getRotation().x,1,0,0);
        //Matrix.rotateM(m.mat, 0, m.mat,0, scene.camera.getRotation().z,0,0,1);
        //Matrix.translateM(m.mat,0,m.mat,0,0,0,camZoom);
        //scene.camera.setPosition(Matrix4f.MultiplyMV(m,new Vector3f(0)));

        prevFrameTime = System.currentTimeMillis();
    }

    public void onAccelerometerChanged(Vector3f dir){
        prevCamDir = camDir;
        Vector3f d = dir;
        d.normalize();
        camA = new Vector3f(d.x,0f,-d.z*1f);
        float l = camA.length();
        float h = 0.75f;
        double min = Math.min(Math.pow(l * h, 4f), 1f);
        l = (float) ( (1f - min) * Math.pow(l * h,4) + l * min);
        camA.normalize();
        camA.scale(l * 5f);
        Log.w("camera","accélération: x: " + camA.x + " y: " + camA.y + " z: " + camA.z);
        //roll = (float) Math.atan2(d.y,d.x);
        //Log.w("camDir","x: " + camDir.x + " y: " + camDir.y + " z: " + camDir.z);
        //camA.scale(0.01f);

        camUp = Vector3f.sub(camDir,prevCamDir);
        camUp.normalize();
    }
}