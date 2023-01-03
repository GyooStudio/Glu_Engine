package com.glu.engine.Postprocessing;

import android.opengl.GLES30;
import android.opengl.GLES32;
import android.opengl.Matrix;
import android.util.Log;

import com.glu.engine.Objects.GTexture;
import com.glu.engine.Objects.SkyBox;
import com.glu.engine.Scene.Camera;
import com.glu.engine.Scene.Light;
import com.glu.engine.Scene.Ressources;
import com.glu.engine.Scene.Scene;
import com.glu.engine.Scene.SunLight;
import com.glu.engine.shader.DeffRenderShader;
import com.glu.engine.shader.PPShader;
import com.glu.engine.utils.Loader;
import com.glu.engine.utils.Maths;
import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector2f;
import com.glu.engine.vectors.Vector3f;
import com.glu.engine.vectors.Vector4f;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL;

public class PostProcessing {

    public enum effect{
        NONE,
        GAMMA_CORRECT,
        GAUSSIAN_BLUR,
        BLOOM,
        FOCUS,
        AO,
        DEFFERED_RENDERING,
        TAA, //antialiasing
        SSR,  //screen-space reflexion
        DITHERING, // doubles the resolution, costing in temporal blur
        PIXELISE   // makes the view pixelated and sharp
    }

    public ArrayList<effect> effects = new ArrayList<>();
    public ArrayList<Float> A = new ArrayList<>();
    public ArrayList<Float> B = new ArrayList<>();
    private Loader loader;
    private Ressources ressources;
    public PPShader[] ppShaders = new PPShader[10];
    public DeffRenderShader deffShader;

    public FrameBuffer frameBufferA;
    public FrameBuffer frameBufferB;
    public FrameBuffer halfA;
    public FrameBuffer halfB;
    public FrameBuffer quartA;
    public FrameBuffer quartB;
    public FrameBuffer scene;
    public FrameBuffer scene2;
    public FrameBuffer sceneRendered;
    public FrameBuffer TAAColor;
    public FrameBuffer AOColor;
    public FrameBuffer SSRColor;
    public FrameBuffer DitherColor;
    public FrameBuffer Shadow;
    public FrameBuffer lastFrameExposure;
    public SkyBox skybox;
    public SunLight sunLight;

    public PPQuad screen;

    private boolean usingA = true;
    private boolean readyForRendering = false;
    public boolean isSetup = false;

    public float downSizeFactor;

    public int inc = 0;

    private Vector4f BackgroundColor = new Vector4f(0.5f,0.5f,0.5f,1.0f);

    public Camera camera;
    public ArrayList<Light> lights;
    public Matrix4f PROJMAT;
    public float depthRange;
    public float FOV;

    long timerA;
    long timerB;
    int frameCount = 0;

    public PostProcessing(PostProcessing pp){
        this.effects = (ArrayList<effect>) pp.effects.clone();
        this.A = (ArrayList<Float>) pp.A.clone();
        this.B = (ArrayList<Float>) pp.B.clone();
        this.loader = pp.loader;
        this.ppShaders = pp.ppShaders;
        this.deffShader = pp.deffShader;
        this.frameBufferA = pp.frameBufferA;
        this.frameBufferB = pp.frameBufferB;
        this.halfA = pp.halfA;
        this.halfB = pp.halfB;
        this.quartA = pp.quartA;
        this.quartB = pp.quartB;
        this.scene = pp.scene;
        this.scene2 = pp.scene2;
        this.sceneRendered = pp.sceneRendered;
        this.TAAColor = pp.TAAColor;
        this.AOColor = pp.AOColor;
        this.SSRColor = pp.SSRColor;
        this.lastFrameExposure = pp.lastFrameExposure;
        this.screen = pp.screen;
        this.downSizeFactor = pp.downSizeFactor;
        this.camera = pp.camera;
        this.lights = pp.lights;
        this.depthRange = pp.depthRange;
        this.Shadow = pp.Shadow;
        this.FOV = pp.FOV;
        this.skybox = pp.skybox;
        this.sunLight = pp.sunLight;
        this.PROJMAT = pp.PROJMAT;
        ressources = Ressources.getRessources();
    }

    public PostProcessing(){
        ressources = Ressources.getRessources();
        loader = Loader.getLoader();
        downSizeFactor = 1f;
    }

    public void setup(float downSizeFactor, Scene sc){
        float min = Math.min(ressources.viewport.x,ressources.viewport.y);
        frameBufferA = new FrameBuffer((int) (min/downSizeFactor),(int) (min/downSizeFactor),true, false,false, true);
        frameBufferB = new FrameBuffer((int) (min/downSizeFactor),(int) (min/downSizeFactor),true, false, false,true);
        sceneRendered = new FrameBuffer((int) (min/downSizeFactor), (int) (min/downSizeFactor),true, true, false,false);
        scene = new FrameBuffer((int) (min/downSizeFactor), (int) (min/downSizeFactor),true, true, false,false);
        scene2 = new FrameBuffer((int) (min/downSizeFactor), (int) (min/downSizeFactor),true, true, false,false);
        quartA = new FrameBuffer((int) (min/downSizeFactor)/4, (int) (min/downSizeFactor)/4,true, false,false,false);
        quartB = new FrameBuffer((int) (min/downSizeFactor)/4, (int) (min/downSizeFactor)/4,true, false, false,false);
        halfA = new FrameBuffer((int) (min/downSizeFactor)/2, (int) (min/downSizeFactor)/2,true, false,false,false);
        halfB = new FrameBuffer((int) (min/downSizeFactor)/2, (int) (min/downSizeFactor)/2,true, false, false,false);
        TAAColor = new FrameBuffer((int) (min/(downSizeFactor/2)), (int) (min/(downSizeFactor/2)),false, false,false,false);
        AOColor = new FrameBuffer((int) (min/downSizeFactor/4), (int) (min/downSizeFactor/4),true, false, false,false);
        SSRColor = new FrameBuffer((int) (min/downSizeFactor/2), (int) (min/downSizeFactor/2),true, false, false,false);
        DitherColor = new FrameBuffer((int) (min/(downSizeFactor/2)), (int) (min/(downSizeFactor/2)),false, false, false,false);
        lastFrameExposure = new FrameBuffer(4,4,true,false,false,false);
        Shadow = new FrameBuffer(1024,1024, false,false,true,false);
        screen = new PPQuad(ressources.viewport);
        screen.scale = ressources.viewport;
        this.downSizeFactor = downSizeFactor;
        this.camera = sc.camera;
        this.lights = sc.Lights;
        this.PROJMAT = sc.PROJECTION_MATRIX;
        this.depthRange = Scene.FAR_PLANE - Scene.NEAR_PLANE;
        this.FOV = sc.FOV;
        this.skybox = sc.getSkybox();
        this.sunLight = sc.sunLight;
        readyForRendering = false;
        isSetup = true;
    }

    public int addEffect(effect eff, float a, float b){

        if(ppShaders[0] == null){
            ppShaders[0] = new PPShader(loader.loadAssetText("Shaders/PostProcessing/PP.vert"),loader.loadAssetText("Shaders/PostProcessing/PP.frag"));
            readyForRendering = false;
        }

        switch (eff){
            case NONE:
                break;
            case GAMMA_CORRECT:
                if(ppShaders[1] == null){
                    ppShaders[1] = new PPShader(loader.loadAssetText("Shaders/PostProcessing/PP.vert"),loader.loadAssetText("Shaders/PostProcessing/GammaCorrection.frag"));
                    readyForRendering = false;
                }
                break;
            case GAUSSIAN_BLUR:
                if(ppShaders[2] == null){
                    ppShaders[2] = new PPShader(loader.loadAssetText("Shaders/PostProcessing/GaussianBlur.vert"),loader.loadAssetText("Shaders/PostProcessing/GaussianBlur.frag"));
                    readyForRendering = false;
                }
                break;
            case BLOOM:
                if(ppShaders[3] == null){
                    ppShaders[3] = new PPShader(loader.loadAssetText("Shaders/PostProcessing/Bloom.vert"),loader.loadAssetText("Shaders/PostProcessing/Bloom.frag"));
                    readyForRendering = false;
                }
                break;
            case FOCUS:
                if(ppShaders[4] == null){
                    ppShaders[4] = new PPShader(loader.loadAssetText("Shaders/PostProcessing/Focus.vert"), loader.loadAssetText("Shaders/PostProcessing/Focus.frag"));
                    readyForRendering = false;
                }
                break;
            case AO:
                if(ppShaders[5] == null){
                    ppShaders[5] = new PPShader(loader.loadAssetText("Shaders/PostProcessing/AO.vert"), loader.loadAssetText("Shaders/PostProcessing/AO.frag"));
                    readyForRendering = false;
                }
                break;
            case DEFFERED_RENDERING:
                if (deffShader == null){
                    deffShader = new DeffRenderShader(loader.loadAssetText("Shaders/PostProcessing/DeffRender.vert"), loader.loadAssetText("Shaders/PostProcessing/DeffRender.frag"));
                    readyForRendering = false;
                }
                break;
            case TAA:
                if(ppShaders[6] == null){
                    ppShaders[6] = new PPShader(loader.loadAssetText("Shaders/PostProcessing/TAA.vert"), loader.loadAssetText("Shaders/PostProcessing/TAA.frag"));
                    readyForRendering = false;
                }
                break;
            case SSR:
                if(ppShaders[7] == null){
                    ppShaders[7] = new PPShader(loader.loadAssetText("Shaders/PostProcessing/SSR.vert"), loader.loadAssetText("Shaders/PostProcessing/SSR.frag"));
                    readyForRendering = false;
                }
                break;
            case DITHERING:
                if(ppShaders[8] == null){
                    ppShaders[8] = new PPShader(loader.loadAssetText("Shaders/PostProcessing/Dithering.vert"),loader.loadAssetText("Shaders/PostProcessing/Dithering.frag"));
                    readyForRendering = false;
                }
                break;
            case PIXELISE:
                if(ppShaders[9] == null){
                    ppShaders[9] = new PPShader(loader.loadAssetText("Shaders/PostProcessing/Pixelise.vert"), loader.loadAssetText("Shaders/PostProcessing/Pixelise.frag"));
                    readyForRendering = false;
                }
                break;
            default:
                Log.e("setting effect","effect is not a valid value");
                break;
        }

        effects.add(eff);
        A.add(a);
        B.add(b);

        return effects.size()-1;
    }

    public void removeEffect(int index){
        effects.remove(index);
    }

    public void prepareForRender(){
        //int error = GLES30.glGetError();
        if(!readyForRendering){
            if(screen.isDirty){
                screen.makeModel();
            }
            for (int i = 0; i < ppShaders.length; i++) {
                if(ppShaders[i] != null && ppShaders[i].isDirty){
                    ppShaders[i].buildShader();
                }
            }
            if(deffShader != null && deffShader.isDirty){
                deffShader.buildShader();
            }

            readyForRendering = true;

        }

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,scene.ID);
        GLES30.glViewport(0,0,scene.width,scene.height);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT|GLES30.GL_DEPTH_BUFFER_BIT);
        usingA = true;
    }

    public void prepareForSecondPass(){

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,scene2.ID);
        GLES30.glViewport(0,0,scene2.width,scene2.height);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT|GLES30.GL_DEPTH_BUFFER_BIT);
    }

    public void prepareShadowPass(){
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,Shadow.ID);
        GLES30.glViewport(0,0,Shadow.width,Shadow.height);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT|GLES30.GL_DEPTH_BUFFER_BIT);
    }

    private void makeMipmaps(){
        if (frameBufferA.isMipmaped){
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,frameBufferA.texture.ID);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        }
        if (frameBufferB.isMipmaped){
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,frameBufferB.texture.ID);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        }
        if (halfA.isMipmaped){
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,halfA.texture.ID);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        }
        if (halfB.isMipmaped){
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,halfB.texture.ID);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        }
        if (halfB.isMipmaped){
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,halfB.texture.ID);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        }
        if (quartA.isMipmaped){
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,quartA.texture.ID);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        }
        if (quartB.isMipmaped){
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,quartB.texture.ID);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        }
        if (TAAColor.isMipmaped){
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,TAAColor.texture.ID);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        }
        if (AOColor.isMipmaped){
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,AOColor.texture.ID);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        }
        if (SSRColor.isMipmaped){
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,SSRColor.texture.ID);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        }
        if(scene.isMipmaped){
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,scene.texture.ID);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,scene.B.ID);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,scene.C.ID);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,scene.D.ID);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        }
        if(scene2.isMipmaped){
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,scene2.texture.ID);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,scene2.B.ID);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,scene2.C.ID);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,scene2.D.ID);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        }
    }

    public void renderEffects(){

        boolean isLastEffect = false;
        inc++;
        if(inc > 1000){
            inc = 0;
        }

        GLES30.glBindVertexArray(screen.model.vaoID);
        GLES30.glEnableVertexAttribArray(0);
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        
        GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER,scene.ID);
        GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER,frameBufferB.ID);
        GLES30.glBlitFramebuffer(0,0,frameBufferA.width,frameBufferA.height,0,0,frameBufferA.width,frameBufferA.height,GLES30.GL_COLOR_BUFFER_BIT,GLES30.GL_NEAREST);

        for (int i = 0; i < effects.size(); i++) {

            //Log.w("effects", "rendering effect: " + effects.get(i).name());

            if(i+1 == effects.size()){
                isLastEffect = true;
            }
            if(effects.get(i) == effect.DEFFERED_RENDERING){

                if(isLastEffect){
                    GLES30.glViewport(0,0, (int) (ressources.viewport.x), (int) (ressources.viewport.y));
                    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,0);
                }else {
                    GLES30.glViewport(0,0, scene.width, scene.height);
                    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,sceneRendered.ID);
                }

                deffShader.start();

                GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,scene.texture.ID);
                GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,scene.B.ID);
                GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,scene.C.ID);
                if(skybox != null && !skybox.HDRI.isDirty) {
                    GLES30.glActiveTexture(GLES30.GL_TEXTURE3);
                    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, skybox.HDRI.ID);
                }
                GLES30.glActiveTexture(GLES30.GL_TEXTURE4);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,Shadow.texture.ID);

                deffShader.loadCamPos(camera.getPosition());
                deffShader.loadSunLight(sunLight);
                deffShader.loadShadowSoftness(sunLight.softness);
                if(skybox != null) {
                    deffShader.loadSkyboxStrength(skybox.strength);
                }

                float offX;
                float offY;
                if(ressources.viewport.x < ressources.viewport.y){
                    offX = 1f;
                    offY = ressources.viewport.y/ressources.viewport.x;
                }else{
                    offX = ressources.viewport.x/ressources.viewport.y;
                    offY = 1f;
                }

                Vector3f[] lightPos = new Vector3f[lights.size()];
                float[] lightRad = new float[lights.size()];
                Matrix4f Mat = camera.getViewMat().copy();
                Matrix.multiplyMM(Mat.mat, 0, PROJMAT.mat, 0, Mat.mat, 0);
                float a = (float) (1.0 / Math.tan(Math.toRadians(FOV) / 2f));

                for (int j = 0; j < lightPos.length; j++) {
                    float[] pos = new float[]{lights.get(j).position.x, lights.get(j).position.y, lights.get(j).position.z, 1f};
                    float rad = lights.get(j).radius;
                    Matrix.multiplyMV(pos,0,Mat.mat,0,pos,0);
                    lightPos[j] = new Vector3f((pos[0]/Math.abs(pos[3]) * 0.5f + 0.5f) * scene.width * offX, (pos[1]/Math.abs(pos[3]) * 0.5f + 0.5f) * scene.height * offY, pos[2]);
                    lightRad[j] = (a * rad / (float) Math.sqrt(Math.max(Vector3f.distance(camera.getPosition(),lights.get(j).position)*Vector3f.distance(camera.getPosition(),lights.get(j).position) - rad*rad,0.1f))) * Math.min(scene.width * offX, scene.height * offY) * 0.5f;
                }

                float tileSize = 8f;

                deffShader.loadNumberOfLights(lightPos.length);
                for (int j = 0; j < lightPos.length; j++) {
                    if(lightPos[j].z + lights.get(j).radius > 0f) {
                        if(Vector3f.distance(lights.get(j).position, camera.getPosition()) > lights.get(j).radius){
                            deffShader.loadLight(j, lights.get(j).position, lights.get(j).color, lights.get(j).intensity, new Vector4f(lightPos[j].x, lightPos[j].y, lightPos[j].z, lightRad[j]));
                            deffShader.loadLightCilpDistance(lights.get(j).radius);
                        }else{
                            deffShader.loadLight(j, lights.get(j).position, lights.get(j).color, lights.get(j).intensity, new Vector4f(lightPos[j].x, lightPos[j].y, lightPos[j].z, 541f));
                            deffShader.loadLightCilpDistance(lights.get(j).radius);
                        }
                    }else{
                        deffShader.loadLight(j, new Vector3f(0f), new Vector3f(0f), 0f, new Vector4f(0f));
                        deffShader.loadLightCilpDistance(0f);
                    }
                }
                deffShader.loadTileSize(tileSize);
                deffShader.loadScreenDiff(new Vector2f(offX,offY));

                GLES30.glDrawArraysInstanced(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount, (int)( Math.ceil(scene.width/tileSize) * Math.ceil(scene.height/tileSize) ) );

                deffShader.stop();

                if (!isLastEffect) {
                    GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, sceneRendered.ID);
                    GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER,frameBufferB.ID);
                    GLES30.glBlitFramebuffer(0,0,sceneRendered.width,sceneRendered.height,0,0,frameBufferB.width,frameBufferB.height,GLES30.GL_COLOR_BUFFER_BIT,GLES30.GL_NEAREST);
                }


                usingA = true;

            }else{
                FrameBuffer writeBuffer;
                FrameBuffer readBuffer;
                if(usingA){
                    writeBuffer = frameBufferA;
                    readBuffer = frameBufferB;
                }else{
                    writeBuffer = frameBufferB;
                    readBuffer = frameBufferA;
                }
                switch (effects.get(i)){
                    case NONE:
                        //only there to collapse the code in the IDE
                        if(true) {
                            if (isLastEffect) {
                                GLES30.glViewport(0, 0, (int) (ressources.viewport.x), (int) (ressources.viewport.y));
                                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                            } else {
                                GLES30.glViewport(0, 0, writeBuffer.width, writeBuffer.height);
                                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, writeBuffer.ID);
                            }


                            ppShaders[0].start();

                            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, readBuffer.texture.ID);

                            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount);

                            ppShaders[0].stop();
                        }
                        break;
                    case GAMMA_CORRECT:
                        //only there to collapse the code in the IDE
                        if(true) {
                            if (isLastEffect) {
                                GLES30.glViewport(0, 0, (int) (ressources.viewport.x), (int) (ressources.viewport.y));
                                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                            } else {
                                GLES30.glViewport(0, 0, writeBuffer.width, writeBuffer.height);
                                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, writeBuffer.ID);
                            }

                            ppShaders[1].start();

                            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, readBuffer.texture.ID);
                            GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, lastFrameExposure.texture.ID);

                            ppShaders[1].loadUniformBlock(A.get(i), B.get(i));

                            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount);

                            ppShaders[1].stop();

                        }
                        break;
                    case GAUSSIAN_BLUR:
                        //only there to collapse the code in the IDE
                        if(true) {
                            if (A.get(i) != 0) {
                                ppShaders[0].start();

                                boolean lastPass = false;

                                for (int j = 0; j < A.get(i); j++) {

                                    if((float)j + 1f == A.get(i)){
                                        lastPass = true;
                                    }

                                    makeMipmaps();

                                    GLES30.glViewport(0, 0, quartA.width, quartA.height);

                                    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, quartA.ID);


                                    GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                                    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, readBuffer.texture.ID);

                                    GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount);

                                    if(!lastPass) {
                                        GLES30.glViewport(0, 0, readBuffer.width, readBuffer.height);
                                        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, readBuffer.ID);
                                    }else if(!isLastEffect){
                                        GLES30.glViewport(0, 0, writeBuffer.width, writeBuffer.height);
                                        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, writeBuffer.ID);
                                    }else{
                                        GLES30.glViewport(0, 0, (int) ressources.viewport.x, (int) ressources.viewport.y);
                                        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                                    }


                                    GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                                    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, quartA.texture.ID);

                                    GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount);
                                }

                                ppShaders[0].stop();

                            } else {
                                if (isLastEffect) {
                                    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                                    GLES30.glViewport(0, 0, (int) (ressources.viewport.x), (int) (ressources.viewport.y));
                                } else {
                                    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, writeBuffer.ID);
                                    GLES30.glViewport(0, 0, scene.width, scene.height);
                                }


                                ppShaders[0].start();

                                GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, readBuffer.texture.ID);

                                GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount);

                                ppShaders[0].stop();
                            }
                        }
                        break;
                    case BLOOM:
                        //only there to collapse the code in the IDE
                        if (true) {
                            if (A.get(i) >= 1f) {

                                GLES30.glViewport(0, 0, writeBuffer.width, writeBuffer.height);
                                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,writeBuffer.ID);

                                ppShaders[3].start();

                                GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, readBuffer.texture.ID);

                                ppShaders[3].loadBoolean(true);
                                ppShaders[3].loadBooleanB(false);

                                GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount);

                                boolean lastPass = false;

                                for (int j = 0; j < A.get(i); j++) {

                                    if((float)j + 1f == A.get(i)){
                                        lastPass = true;
                                    }

                                    makeMipmaps();

                                    GLES30.glViewport(0, 0, quartA.width, quartA.height);
                                    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, quartA.ID);

                                    GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                                    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, writeBuffer.texture.ID);

                                    ppShaders[3].loadBoolean(true);
                                    ppShaders[3].loadBooleanB(true);

                                    GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount);

                                    if(!lastPass) {
                                        GLES30.glViewport(0, 0, writeBuffer.width, writeBuffer.height);
                                        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, writeBuffer.ID);
                                        ppShaders[3].loadBoolean(false);
                                        ppShaders[3].loadBooleanB(false);
                                    }else if(!isLastEffect){
                                        GLES30.glViewport(0, 0, writeBuffer.width, writeBuffer.height);
                                        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, writeBuffer.ID);
                                        ppShaders[3].loadBoolean(false);
                                        ppShaders[3].loadBooleanB(true);
                                    }else{
                                        GLES30.glViewport(0, 0, (int) ressources.viewport.x, (int) ressources.viewport.y);
                                        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                                        ppShaders[3].loadBoolean(false);
                                        ppShaders[3].loadBooleanB(true);
                                    }


                                    GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                                    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, quartA.texture.ID);
                                    GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
                                    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, readBuffer.texture.ID);

                                    GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount);
                                }
                                ppShaders[3].stop();
                            } else {
                                if (isLastEffect) {
                                    GLES30.glViewport(0, 0, (int) (ressources.viewport.x), (int) (ressources.viewport.y));
                                    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                                } else {
                                    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, writeBuffer.ID);
                                }

                                //GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

                                ppShaders[0].start();

                                GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, readBuffer.texture.ID);

                                GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount);

                                ppShaders[0].stop();
                            }
                        }
                        break;
                    case FOCUS:
                        //only there to collapse the code in the IDE
                        if(true) {
                            if (A.get(i) != 0) {
                                makeMipmaps();

                                boolean lastOne = false;
                                boolean isFirst = true;

                                GLES30.glViewport(0, 0, quartA.width, quartA.height);
                                GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
                                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, scene.texture.ID);
                                GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, readBuffer.texture.ID);

                                for (int j = 0; j < (int) Math.ceil(A.get(i)); j++) {

                                    if (j + 1 == (int) Math.ceil(A.get(i))) {
                                        lastOne = true;
                                    }

                                    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, quartB.ID);

                                    ppShaders[4].start();

                                    if (isFirst) {
                                        isFirst = false;
                                    } else {
                                    }

                                    ppShaders[4].loadUniformBlock(10f, B.get(i));
                                    ppShaders[4].loadBoolean(false);
                                    ppShaders[4].loadBooleanB(false);

                                    GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount);

                                    if (isLastEffect && lastOne) {
                                        GLES30.glViewport(0, 0, (int) (ressources.viewport.x), (int) (ressources.viewport.y));
                                        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                                    } else if (lastOne) {
                                        GLES30.glViewport(0, 0, frameBufferA.width, frameBufferA.height);
                                        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, writeBuffer.ID);
                                    } else {
                                        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, quartA.ID);
                                    }

                                    GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                                    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, quartB.texture.ID);

                                    ppShaders[4].loadUniformBlock(10f, B.get(i));
                                    ppShaders[4].loadBoolean(true);
                                    if (lastOne) {
                                        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
                                        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, readBuffer.texture.ID);
                                        ppShaders[4].loadBooleanB(true);
                                    } else {
                                        ppShaders[4].loadBooleanB(false);
                                    }

                                    GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount);
                                }
                                ppShaders[4].stop();
                            } else {
                                if (isLastEffect) {
                                    GLES30.glViewport(0, 0, (int) (ressources.viewport.x), (int) (ressources.viewport.y));
                                    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                                } else {
                                    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, writeBuffer.ID);
                                }

                                //GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

                                ppShaders[0].start();

                                GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, readBuffer.texture.ID);

                                GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount);

                                ppShaders[0].stop();
                            }
                        }
                        break;
                    case AO:
                        //only there to collapse the code in the IDE
                        if(true) {
                            makeMipmaps();

                            GLES30.glViewport(0, 0, quartA.width, quartA.height);
                            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, quartA.ID);

                            ppShaders[5].start();

                            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, AOColor.texture.ID);
                            GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, scene.B.ID);
                            GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, scene.C.ID);

                            ppShaders[5].loadUniformBlock(inc, B.get(i));
                            ppShaders[5].loadBoolean(true);
                            ppShaders[5].loadMat(camera.getRotationMat());
                            ppShaders[5].loadMatB(PROJMAT);
                            ppShaders[5].loadMatC(camera.getViewMat());

                            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount);

                            GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER,quartA.ID);
                            GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER,AOColor.ID);
                            GLES30.glBlitFramebuffer(0,0,quartA.width,quartA.height,0,0,AOColor.width,AOColor.height,GLES30.GL_COLOR_BUFFER_BIT,GLES30.GL_NEAREST);

                            if (isLastEffect) {
                                GLES30.glViewport(0, 0, (int) ressources.viewport.x, (int) ressources.viewport.y);
                                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                            } else {
                                GLES30.glViewport(0, 0, (int) scene.width, (int) scene.height);
                                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, writeBuffer.ID);
                            }


                            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, quartA.texture.ID);
                            GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, readBuffer.texture.ID);
                            GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, AOColor.texture.ID);

                            ppShaders[5].loadUniformBlock(inc, B.get(i));
                            ppShaders[5].loadBoolean(false);

                            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount);

                            ppShaders[5].stop();
                        }
                        break;
                    case TAA:
                        //only there to collapse the code in the IDE
                        if (true){
                            //makeMipmaps();
                            if (isLastEffect) {
                                GLES30.glViewport(0, 0, (int) (ressources.viewport.x), (int) (ressources.viewport.y));
                                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                            } else {
                                GLES30.glViewport(0, 0, writeBuffer.width, writeBuffer.height);
                                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, writeBuffer.ID);
                            }

                            ppShaders[6].start();

                            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, DitherColor.texture.ID);
                            GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, TAAColor.texture.ID);
                            GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, scene2.B.ID);

                            ppShaders[6].loadUniformBlock(A.get(i), B.get(i));

                            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount);

                            ppShaders[6].stop();

                            if (isLastEffect) {
                                GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER,0);
                                GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER,TAAColor.ID);
                                GLES30.glBlitFramebuffer(0,0,(int) (ressources.viewport.x),(int) (ressources.viewport.y),0,0,TAAColor.width,TAAColor.height,GLES30.GL_COLOR_BUFFER_BIT,GLES30.GL_NEAREST);
                                GLES30.glViewport(0, 0, (int) (ressources.viewport.x), (int) (ressources.viewport.y));
                                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                            } else {
                                GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER,writeBuffer.ID);
                                GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER,TAAColor.ID);
                                GLES30.glBlitFramebuffer(0,0,writeBuffer.width,writeBuffer.height,0,0,TAAColor.width,TAAColor.height,GLES30.GL_COLOR_BUFFER_BIT,GLES30.GL_NEAREST);
                                GLES30.glViewport(0, 0, (int) scene.width, (int) scene.height);
                                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBufferA.ID);
                            }
                        }
                        break;
                    case SSR:
                        //only there to collapse the code in the IDE
                        if(true) {
                            makeMipmaps();

                            GLES30.glViewport(0, 0, halfA.width, halfA.height);
                            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, halfA.ID);

                            ppShaders[7].start();

                            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, readBuffer.texture.ID);
                            GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, scene.texture.ID);
                            GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, scene.B.ID);
                            GLES30.glActiveTexture(GLES30.GL_TEXTURE3);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, scene.C.ID);
                            GLES30.glActiveTexture(GLES30.GL_TEXTURE4);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, SSRColor.texture.ID);

                            ppShaders[7].loadUniformBlock(inc, B.get(i));
                            ppShaders[7].loadBoolean(true);
                            ppShaders[7].loadBooleanB(false);
                            ppShaders[7].loadMat(camera.getRotationMat());
                            ppShaders[7].loadMatB(PROJMAT);
                            ppShaders[7].loadMatC(camera.getViewMat());

                            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount);

                            GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER,halfA.ID);
                            GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER,SSRColor.ID);
                            GLES30.glBlitFramebuffer(0,0,halfA.width,halfA.height,0,0,SSRColor.width,SSRColor.height,GLES30.GL_COLOR_BUFFER_BIT,GLES30.GL_NEAREST);

                            if (isLastEffect) {
                                GLES30.glViewport(0, 0, (int) ressources.viewport.x, (int) ressources.viewport.y);
                                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                            } else {
                                GLES30.glViewport(0, 0, (int) scene.width, (int) scene.height);
                                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, writeBuffer.ID);
                            }

                            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, halfA.texture.ID);
                            GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, readBuffer.texture.ID);
                            GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, sceneRendered.B.ID);

                            ppShaders[7].loadBoolean(false);
                            ppShaders[7].loadBooleanB(true);

                            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount);

                            ppShaders[7].stop();
                        }
                        break;
                    case DITHERING:
                        //only there to collapse the code in the IDE
                        if(true) {
                            GLES30.glViewport(0,0,DitherColor.width,DitherColor.height);
                            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,DitherColor.ID);

                            ppShaders[8].start();

                            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, DitherColor.texture.ID);
                            GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, readBuffer.texture.ID);
                            GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, scene2.B.ID);

                            ppShaders[8].loadUniformBlock(inc,0f);

                            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount);

                            ppShaders[8].stop();

                            if (isLastEffect) {
                                GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER,DitherColor.ID);
                                GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER,0);
                                GLES30.glBlitFramebuffer(0,0,DitherColor.width,DitherColor.height,0,0,(int) (ressources.viewport.x),(int) (ressources.viewport.y),GLES30.GL_COLOR_BUFFER_BIT,GLES30.GL_LINEAR);
                                GLES30.glViewport(0, 0, (int) (ressources.viewport.x), (int) (ressources.viewport.y));
                                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                            } else {
                                GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, DitherColor.ID);
                                GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER,writeBuffer.ID);
                                GLES30.glBlitFramebuffer(0,0,DitherColor.width,DitherColor.height,0,0,writeBuffer.width,writeBuffer.height,GLES30.GL_COLOR_BUFFER_BIT,GLES30.GL_LINEAR);
                                GLES30.glViewport(0, 0, (int) scene.width, (int) scene.height);
                                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, writeBuffer.ID);
                            }
                        }
                        break;
                    case PIXELISE:
                        //only there to collapse the code in the IDE
                        if (true){
                            if (isLastEffect) {
                                GLES30.glViewport(0, 0, (int) (ressources.viewport.x), (int) (ressources.viewport.y));
                                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                            } else {
                                GLES30.glViewport(0, 0, writeBuffer.width, writeBuffer.height);
                                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, writeBuffer.ID);
                            }


                            ppShaders[9].start();

                            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, readBuffer.texture.ID);

                            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount);

                            ppShaders[9].stop();
                        }
                        break;
                    default:
                        Log.e("setting effect","effect is not a valid value");
                        break;
                }

                if(effects.get(i) == effect.GAMMA_CORRECT){
                    makeMipmaps();
                    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,lastFrameExposure.ID);
                    GLES30.glViewport(0,0,lastFrameExposure.width,lastFrameExposure.height);

                    ppShaders[0].start();

                    GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,writeBuffer.texture.ID);

                    GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, screen.model.vertCount);

                    ppShaders[0].stop();

                    GLES30.glViewport(0,0,(int) ressources.viewport.x,(int) ressources.viewport.y);
                    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,0);
                }

                usingA = !usingA;
            }
        }
        GLES30.glDisableVertexAttribArray(0);
    }

    public void setBackgroundColor(Vector4f color) {
        BackgroundColor = color;
        readyForRendering = false;
    }

}
