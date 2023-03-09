package com.glu.engine.actionManager;

import android.opengl.Matrix;

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
    private Vector3f camCenter = new Vector3f(0);
    private float camZoom = 5f;
    private int movementType = 0;
    private long timeOfLastMovement = System.currentTimeMillis();

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

        if(bouton == null) {
            ColorSquare base = new ColorSquare(new Vector4f(0.2f, 0.9f, 0.2f, 1.0f));
            ColorSquare pressed = new ColorSquare(new Vector4f(0.9f, 0.2f, 0.2f, 1.0f));
            ColorSquare survol = new ColorSquare(new Vector4f(0.9f, 0.6f, 0.2f, 1.0f));
            //scene.addColorSquare(base);
            //scene.addColorSquare(pressed);
            //scene.addColorSquare(survol);
            bouton = new Bouton(base, pressed, survol);
            //scene.addButton(bouton);
            bouton.changerTaille(new Vector2f(200f));
            bouton.changerPosition(new Vector2f(100,0));
            bouton.changerRotation(45f);
            bouton.changerComportement(Bouton.Préréglages.CONTRÔLES);
        }

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
                updatedAction = bouton.actualiser(index);
            }

            for (Glissoire glissoire : scene.glissoires) {
                updatedAction = glissoire.actualiser(index);
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

        //camCenter = new Vector3f((float) Math.cos((double) System.currentTimeMillis() / 4000.0) * 3f, 0f, (float) Math.sin((double) System.currentTimeMillis() / 4000.0) * 3f );
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