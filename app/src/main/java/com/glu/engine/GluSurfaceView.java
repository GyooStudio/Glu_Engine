package com.glu.engine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.view.MotionEventCompat;

import androidx.core.view.MotionEventCompat;

import com.glu.engine.GUI.Text.TextBox;
import com.glu.engine.Objects.Entity;
import com.glu.engine.Postprocessing.PostProcessing;
import com.glu.engine.Scene.Light;
import com.glu.engine.Scene.Ressources;
import com.glu.engine.Scene.Scene;
import com.glu.engine.actionManager.ActionManager;
import com.glu.engine.utils.Loader;
import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector2f;
import com.glu.engine.vectors.Vector3f;

@SuppressLint("ViewConstructor")
public final class GluSurfaceView extends GLSurfaceView {

    final com.glu.engine.Renderer renderer;
    final ActionManager actionManager = ActionManager.getActionManager();
    private Ressources ressources = Ressources.getRessources();
    public long startTime;
    public boolean hasSetTouch = true;

    public Scene scene;

    public long fpsTimer;
    public int fpsCounter;

    private boolean doRun = true;
    private final Loader loader;

    private TextBox fpsText;
    private Entity arrièrePlanA;
    private Entity arrièrePlanB;

    @SuppressLint("ClickableViewAccessibility")
    public void onConfigurationChanged(Configuration config){
        super.onConfigurationChanged(config);
    }

    @SuppressLint("ClickableViewAccessibility")
    public GluSurfaceView(Context context, MainActivity main) {
        super(context);
        startTime = System.currentTimeMillis();
        loader = Loader.getLoader();

        setEGLContextClientVersion(3);
        renderer = new com.glu.engine.Renderer(main);
        setRenderer(renderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
        setDebugFlags(DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS);
        setPreserveEGLContextOnPause(true);

        scene = new Scene();

        setOnTouchListener((view, motionEvent) -> {

            hasSetTouch = false;
            int index = MotionEventCompat.getActionIndex(motionEvent);
            int ID = motionEvent.getPointerId(index);
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_MOVE) {
                //addPoint records a new Pointer's position
                for (int i = 0; i < motionEvent.getPointerCount(); i++) {
                    int Index = motionEvent.findPointerIndex(motionEvent.getPointerId(i));
                    actionManager.addPoint(motionEvent.getPointerId(i), motionEvent.getX(Index), motionEvent.getY(Index));
                    //Log.w("onTouchListener", "action Moved");
                }
                return true;
            }
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                //stopAction lets the program know that an action has stopped
                //Log.w("onTouchListener", "ACTION_UP");
                //actionManager.log();
                actionManager.stopAction( ID);
                //actionManager.log();
                Log.w("onTouchListener", "first Action stopped");
                return true;
            }
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                //addAction lets the program know to start a knew array to store Pointers positions
                //Log.w("onTouchListener", "ACTION_DOWN");
                //actionManager.log();
                actionManager.addAction(ID);
                actionManager.addPoint(ID, motionEvent.getX(ID), motionEvent.getY(ID));
                //actionManager.log();
                Log.w("onTouchListener", "first Action!");

                main.hideUI();

                return true;
            }
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
                //Log.w("onTouchListener", "ACTION_POINTER_DOWN");
                //actionManager.log();
                actionManager.addAction(ID);
                actionManager.addPoint(ID, motionEvent.getX(index), motionEvent.getY(index));
                //actionManager.log();
                Log.w("onTouchListener", "new Action! " + ID);
                return true;
            }
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
                //Log.w("onTouchListener", "ACTION_POINTER_UP");
                //actionManager.log();
                actionManager.stopAction(ID);
                //actionManager.log();
                Log.w("onTouchListener", "action " + ID + " has stopped.");
                return true;
            }
            return false;
        });

        Thread mainLoop = new Thread() {
            public long timer = System.currentTimeMillis();
            public boolean hasGivenPP = false;
            public boolean hasStartedInitThread = false;
            public boolean hasInit = false;
            @Override
            public void run() {
                while (true){
                    while (doRun) {

                        if (scene != null && !hasStartedInitThread) {
                            Thread loadScene = new Thread() {
                                @Override
                                public synchronized void run() {
                                    long timer = System.currentTimeMillis();

                                    scene = loader.loadScene("Scenes/Scene.json", new Vector2f(1));

                                    renderer.setScene(scene);

                                    scene.pp.addEffect(PostProcessing.effect.NONE, 0, 0);
                                    //scene.pp.addEffect(PostProcessing.effect.DEFFERED_RENDERING, 0, 0);
                                    //scene.pp.addEffect(PostProcessing.effect.NONE, 0, 0);
                                    //scene.pp.addEffect(PostProcessing.effect.SSR, 1.0f, 0.1f);
                                    //scene.pp.addEffect(PostProcessing.effect.AO, 1.0f, 5f);
                                    scene.pp.addEffect(PostProcessing.effect.GAMMA_CORRECT, -1.0f, 2.2f);
                                    //scene.pp.addEffect(PostProcessing.effect.NONE, 0, 0);
                                    //scene.pp.addEffect(PostProcessing.effect.GPU_BLUR,6.0f,0.0f);
                                    //scene.pp.addEffect(PostProcessing.effect.FOCUS,2f,4f);
                                    //scene.pp.addEffect(PostProcessing.effect.BLOOM, 3f, 10f);
                                    //scene.pp.addEffect(PostProcessing.effect.NONE, 0, 0);
                                    //scene.pp.addEffect(PostProcessing.effect.DITHERING,0f,0f);
                                    //scene.pp.addEffect(PostProcessing.effect.TAA,4.0f, 2.2f);
                                    //scene.pp.addEffect(PostProcessing.effect.PIXELISE,0f,0f);
                                    scene.pp.setBackgroundColor(renderer.color);

                                    scene.sunLight.shadowDist = 30f;
                                    scene.sunLight.softness = 1f;

                                    //requestRender();

                                    while(fpsText == null){
                                        fpsText = scene.getTextBox("FPS");
                                    }

                                    hasInit = true;

                                    Log.w("OnCreate", (float) (System.currentTimeMillis() - timer) / 1000.0f + " seconds to initialize");
                                }
                            };
                            loadScene.start();
                            hasStartedInitThread = true;

                            Thread LightThread = new Thread() {
                                @Override
                                synchronized public void run() {
                                    while (!hasInit) {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    for (int i = 0; i < 17; i++) {
                                        Light light = new Light(new Vector3f(((float) Math.random() * 2f - 1f) * 100f, 0f, ((float) Math.random() * 2f - 1f) * 100f), new Vector3f((float) Math.random(), (float) Math.random(), (float) Math.random()), (float) Math.random() * 2f);
                                        scene.addLight(light);
                                    }

                                    //ArrayList<Light> lights = scene.Lights;
                                    Vector3f[] velocities = new Vector3f[scene.Lights.size()];
                                    for (int i = 0; i < velocities.length; i++) {
                                        velocities[i] = new Vector3f(0);
                                    }
                                    while (true) {
                                        for (int i = 0; i < scene.Lights.size(); i++) {
                                            double c = Math.random();
                                            if (c > 0.99) {
                                                velocities[i].add(Vector3f.scale(new Vector3f((float) Math.random() * 2f - 1f, (float) Math.random() * 2f - 1f, (float) Math.random() * 2f - 1f), 0.0002f));
                                            }
                                            velocities[i].add(Vector3f.scale(scene.Lights.get(i).position.negative(), 0.00000000001f * scene.Lights.get(i).position.length() * scene.Lights.get(i).position.length()));
                                            velocities[i].add(Vector3f.scale(scene.Lights.get(i).position, 0.0001f / (scene.Lights.get(i).position.length() * scene.Lights.get(i).position.length())));
                                            //velocities[i].add(Vector3f.scale(velocities[i].negative(), 0.00005f));
                                            scene.Lights.get(i).position.add(velocities[i]);
                                            scene.Lights.get(i).position.y = Math.abs(scene.Lights.get(i).position.y);
                                        }
                                    }
                                }
                            };
                            //LightThread.start();
                        }

                        try {
                            Thread.sleep(14);
                        } catch (Exception e) {
                            Log.e("oups!", e.getMessage());
                        }

                        if (!ressources.isRendering && hasInit) {
                            try {

                                //ressources.isModifyingScene = true;

                                scene.inputManager.update();
                                //Log.w("update", "updated scene");
                                //shadowTest.texture = scene.pp.Shadow.texture;

                                Matrix4f m = new Matrix4f();
                                m.setIdentity();
                                Matrix.rotateM(m.mat, 0, 0.025f * Math.max(scene.sunLight.direction.y, 0.1f) * 10f, 1, 0, 0);
                                scene.sunLight.direction = Matrix4f.MultiplyMV(m, scene.sunLight.direction);
                                scene.getSkybox().strength = Math.max(-scene.sunLight.direction.y * 5f * (1f/0.65f), 0f);
                                scene.sunLight.intensity = Math.max(-scene.sunLight.direction.y * 7.0f * (1f/0.65f), 0f);

                                if(arrièrePlanA == null || arrièrePlanB == null){
                                    arrièrePlanA = scene.getEntity("Arrière-plan1");
                                    arrièrePlanB = scene.getEntity("Arrière-plan2");
                                }
                                if(arrièrePlanA != null && arrièrePlanB != null){
                                    arrièrePlanA.material.get(0).emissionIntensity = Math.max( -scene.sunLight.direction.y * 5f * (1f/0.65f), 0f);
                                    arrièrePlanB.material.get(0).emissionIntensity = Math.max( -scene.sunLight.direction.y * 5f * (1f/0.65f), 0f);
                                }

                                //requestRender();

                                //ressources.isModifyingScene = false;

                                fpsCounter++;
                                if (System.currentTimeMillis() - fpsTimer > 5000) {
                                    Log.w("Game", "Time : " + ((float) (System.currentTimeMillis() - fpsTimer) / (float) fpsCounter) + "ms | FPS : " + ((float) fpsCounter / ((float) (System.currentTimeMillis() - fpsTimer) / 1000f)));
                                    fpsText.setText("GameLoop \n Time : " + ((float)(System.currentTimeMillis() - fpsTimer)/(float) fpsCounter) + "ms \n FPS : " + ((float)fpsCounter/((float) (System.currentTimeMillis() - fpsTimer)/1000f)) + "\nDraw \n" + renderer.FPS,4,5, TextBox.Alignment.LEFT);
                                    fpsCounter = 0;
                                    fpsTimer = System.currentTimeMillis();
                                }

                            } catch (Exception e) {
                                //Log.e("update", e.getMessage());
                                e.printStackTrace();
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        };
        mainLoop.setDaemon(true);
        mainLoop.start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        super.surfaceCreated(holder);
    }

    @Override
    public void requestRender(){
        super.requestRender();
    }

    @Override
    public void onPause(){
        super.onPause();
        doRun = false;
    }

    @Override
    public void onResume(){
        super.onResume();
        doRun = true;
    }

}
