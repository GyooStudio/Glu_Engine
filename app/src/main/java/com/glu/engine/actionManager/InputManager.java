package com.glu.engine.actionManager;

import android.opengl.Matrix;
import android.util.Log;

import com.glu.engine.GUI.Button;
import com.glu.engine.GUI.ColorSquare;
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

/**
 *   The goal of this class is to take interpreted action, and inputs and
 *   make them interact with the scene.
 */
public class InputManager {
    final ActionManager actionManager;
    final Scene scene;
    float deltaTime = 0f;
    long prevFrameTime = 0;
    private Vector3f camCenter = new Vector3f(0);
    private float camZoom = 5f;
    int movementType = 0;
    long timeOfLastMovement = System.currentTimeMillis();

    //Entity ball;
    int movingEntityIndex;
    float lastAngle;
    int rotationAxis = 0;
    Vector3f hitpoint;
    RubikCube cube;

    Button button;

    public InputManager(Scene scene){
        this.actionManager = ActionManager.getActionManager();
        this.scene = scene;
    }

    public void update() {

        deltaTime = (float)(System.currentTimeMillis() - prevFrameTime)/1000f;

        if(System.currentTimeMillis() - timeOfLastMovement > 50){
            movementType = 0;
        }

        actionManager.manualUpdate();
        ActionManager.actionType action = actionManager.getAction(0);

        //ColorSquare base = new ColorSquare(,);
        //button = new Button()

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

            for (Button button : scene.Buttons) {
                for (int i = 0; i < button.name.size(); i++) {
                    if (actionManager.pointerIndices[index]) {
                        if (actionManager.isTouching[index] && !updatedAction){
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

            if(!updatedAction && cube != null){
                updatedAction = cube.update(scene);
            }

            if(!updatedAction) {
                if(actionManager.isTouching[index] && actionManager.pointerNumber == 1 && (movementType == 0 || movementType == 1)) {
                    scene.camera.setRotation(Vector3f.add(new Vector3f(actionManager.velocity[index].y * 5f * deltaTime,-actionManager.velocity[index].x * 5f * deltaTime,0f),scene.camera.getRotation()));
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

        Matrix4f m = new Matrix4f();
        Matrix.translateM(m.mat,0,m.mat,0,camCenter.x,camCenter.y,camCenter.z);
        Matrix.rotateM(m.mat, 0, m.mat,0, scene.camera.getRotation().y,0,1,0);
        Matrix.rotateM(m.mat, 0, m.mat,0, scene.camera.getRotation().x,1,0,0);
        Matrix.rotateM(m.mat, 0, m.mat,0, scene.camera.getRotation().z,0,0,1);
        Matrix.translateM(m.mat,0,m.mat,0,0,0,camZoom);
        scene.camera.setPosition(Matrix4f.MultiplyMV(m,new Vector3f(0)));

        prevFrameTime = System.currentTimeMillis();
    }
}