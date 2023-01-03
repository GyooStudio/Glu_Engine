package com.glu.engine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;

import com.glu.engine.GUI.Text.TextBox;
import com.glu.engine.Objects.Collider;
import com.glu.engine.Objects.Entity;
import com.glu.engine.Postprocessing.PostProcessing;
import com.glu.engine.Scene.Light;
import com.glu.engine.Scene.Scene;
import com.glu.engine.utils.Loader;
import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector2f;
import com.glu.engine.vectors.Vector3f;

import java.util.ArrayList;

@SuppressLint("ViewConstructor")
public final class GluSurfaceView extends GLSurfaceView implements Runnable {

    final com.glu.engine.Renderer renderer;
    public long startTime;
    public boolean hasInit = false;
    public boolean hasSetTouch = true;

    public Scene scene;

    public long fpsTimer;
    public int fpsCounter;

    private boolean doRun = true;
    private final Thread renderThread = new Thread(this);
    private final Loader loader;

    private TextBox fpsText;

    @SuppressLint("ClickableViewAccessibility")
    public void onConfigurationChanged(Configuration config){
        super.onConfigurationChanged(config);
    }

    @SuppressLint("ClickableViewAccessibility")
    public GluSurfaceView(Context context, AppCompatActivity main) {
        super(context);
        startTime = System.currentTimeMillis();
        loader = Loader.getLoader();

        setEGLContextClientVersion(3);
        renderer = new com.glu.engine.Renderer(main);
        setRenderer(renderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
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
                        scene.actionManager.addPoint(motionEvent.getPointerId(i), motionEvent.getX(Index), motionEvent.getY(Index));
                        //Log.w("onTouchListener", "action Moved");
                    }
                    return true;
                }

                if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                    //stopAction lets the program know that an action has stopped
                    scene.actionManager.stopAction(0);
                    Log.w("onTouchListener", "first Action stopped");
                    return true;
                }

                if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    //addAction lets the program know to start a knew array to store Pointers positions
                    scene.actionManager.addAction(0);
                    scene.actionManager.addPoint(0, motionEvent.getX(0), motionEvent.getY(0));
                    Log.w("onTouchListener", "first Action!");
                    return true;
                }
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
                    scene.actionManager.addAction(ID);
                    scene.actionManager.addPoint(ID, motionEvent.getX(index), motionEvent.getY(index));
                    Log.w("onTouchListener", "new Action! " + ID);
                    return true;
                }
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
                    scene.actionManager.stopAction(ID);
                    Log.w("onTouchListener", "action " + ID + " has stopped.");
                    return true;
                }
            return false;
        });

        renderThread.setDaemon(true);
        renderThread.start();

    }

    @Override
    synchronized public void run(){
        long timer = System.currentTimeMillis();
        boolean hasGivenPP = false;
        boolean hasStartedInitThread = false;
        while (true) {
            while (doRun) {

                if(scene != null && !hasStartedInitThread) {
                    Thread thread = new Thread() {

                        @Override
                        public synchronized void run() {
                            long timer = System.currentTimeMillis();

                            scene = loader.loadScene("Scenes/Scene.json", new Vector2f(1));
                            renderer.setScene(scene);

                            //scene.pp.addEffect(PostProcessing.effect.NONE, 0, 0);
                            scene.pp.addEffect(PostProcessing.effect.DEFFERED_RENDERING,0,0);
                            scene.pp.addEffect(PostProcessing.effect.SSR,1.0f,0.1f);
                            scene.pp.addEffect(PostProcessing.effect.AO,1.0f,5f);
                            scene.pp.addEffect(PostProcessing.effect.GAMMA_CORRECT,-1.0f, 2.2f);
                            //scene.pp.addEffect(PostProcessing.effect.GAUSSIAN_BLUR,4.0f,1.0f);
                            //scene.pp.addEffect(PostProcessing.effect.FOCUS,2f,5.0f);
                            scene.pp.addEffect(PostProcessing.effect.BLOOM,4f, 10f);
                            //scene.pp.addEffect(PostProcessing.effect.DITHERING,0f,0f);
                            //scene.pp.addEffect(PostProcessing.effect.TAA,4.0f, 2.2f);
                            //scene.pp.addEffect(PostProcessing.effect.PIXELISE,0f,0f);
                            scene.pp.setBackgroundColor(renderer.color);

                            scene.sunLight.shadowDist = 30f;

                            while(fpsText == null){
                                fpsText = scene.getTextBox("FPS");
                            }

                            hasInit = true;

                            Log.w("OnCreate", (float) (System.currentTimeMillis() - timer) / 1000.0f + " seconds to initialize");
                        }
                    };
                    thread.start();
                    hasStartedInitThread = true;

                    Thread LightThread = new Thread(){
                        @Override
                        synchronized public void run(){
                            while(!hasInit){
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            for (int i = 0 ; i < 10; i++) {
                                Light light = new Light(new Vector3f(((float) Math.random() * 2f -1f) * 100f, (float) Math.random() * 100f, ((float) Math.random() * 2f -1f) * 100f), new Vector3f((float) Math.random(), (float) Math.random(), (float) Math.random()), (float) Math.random() * 600f);
                                scene.addLight(light);
                            }

                            ArrayList<Light> lights = scene.Lights;
                            Vector3f[] velocities = new Vector3f[lights.size()];
                            for (int i = 0; i < velocities.length; i++) {
                                velocities[i] = new Vector3f(0);
                            }
                            while (true){
                                for (int i = 0; i < velocities.length; i++) {
                                    double c = Math.random();
                                    if (c > 0.99) {
                                        velocities[i].add(Vector3f.scale(new Vector3f((float) Math.random() * 2f - 1f, (float) Math.random() * 2f - 1f, (float) Math.random() * 2f - 1f), 0.0002f));
                                    }
                                    velocities[i].add(Vector3f.scale(lights.get(i).position.negative(), 0.00000000001f * lights.get(i).position.length() * lights.get(i).position.length()));
                                    velocities[i].add(Vector3f.scale(lights.get(i).position, 0.0001f / (lights.get(i).position.length() * lights.get(i).position.length())));
                                    velocities[i].add(Vector3f.scale(velocities[i].negative(),0.00005f));
                                    lights.get(i).position.add(velocities[i]);
                                    lights.get(i).position.y = Math.abs(lights.get(i).position.y);
                                }
                            }
                        }
                    };
                    //LightThread.start();
                }

                try{
                    Thread.sleep(14);
                }catch(Exception e){
                    Log.e("oups!",e.getMessage());
                }

                if (renderer.hasRendered && hasInit) {
                    try {

                        scene.inputManager.update();
                        //shadowTest.texture = scene.pp.Shadow.texture;

                        Matrix4f m = new Matrix4f();
                        m.setIdentity();
                        Matrix.rotateM(m.mat, 0, 0.025f * Math.max(scene.sunLight.direction.y,0.1f) * 10f, 1, 0, 0);
                        scene.sunLight.direction = Matrix4f.MultiplyMV(m, scene.sunLight.direction);
                        scene.getSkybox().strength = Math.max(-scene.sunLight.direction.y * 1.5f,0.1f);
                        scene.sunLight.intensity = Math.max(-scene.sunLight.direction.y * 3.0f,0f);
                        /*if(scene.sunLight.direction.y > -0.2f){
                            for (int i = 0; i < lamps.length; i++) {
                                lamps[i].intensity = 3f;
                            }
                        }else{
                            for (int i = 0; i < lamps.length; i++) {
                                lamps[i].intensity = 0f;
                            }
                        }*/

                        fpsCounter++;
                        if(System.currentTimeMillis() - fpsTimer > 5000){
                            Log.w("Game", "Time : " + ((float)(System.currentTimeMillis() - fpsTimer)/(float) fpsCounter) + "ms | FPS : " + ((float)fpsCounter/((float) (System.currentTimeMillis() - fpsTimer)/1000f)));
                            fpsText.setText("GameLoop \n Time : " + ((float)(System.currentTimeMillis() - fpsTimer)/(float) fpsCounter) + "ms \n FPS : " + ((float)fpsCounter/((float) (System.currentTimeMillis() - fpsTimer)/1000f)) + "\nDraw \n" + renderer.FPS,4,5, TextBox.Alignment.LEFT);
                            fpsCounter = 0;
                            fpsTimer = System.currentTimeMillis();
                        }

                    } catch (Exception e) {
                        //Log.e("update", e.getMessage());
                        e.printStackTrace();
                        break;
                    }
                }else{
                    break;
                }
            }
        }
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
