package com.glu.engine;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.View;

//import androidx.appcompat.app.AppCompatActivity;

import com.glu.engine.GUI.ColorSquare;
import com.glu.engine.GUI.TexQuad;
import com.glu.engine.GUI.Text.TextBox;
import com.glu.engine.Objects.Entity;
import com.glu.engine.Objects.RawModel;
import com.glu.engine.Objects.SkyBox;
import com.glu.engine.Postprocessing.PostProcessing;
import com.glu.engine.Scene.Ressources;
import com.glu.engine.Scene.Scene;
import com.glu.engine.utils.Loader;
import com.glu.engine.utils.Maths;
import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector2f;
import com.glu.engine.vectors.Vector3f;
import com.glu.engine.vectors.Vector4f;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public final class Renderer implements GLSurfaceView.Renderer {

    public final Loader loader;
    public Ressources ressources;
    public final Vector4f color = new Vector4f(0.1f,0.2f,0.5f,1);
    private long fpsTime = System.currentTimeMillis();
    private short fpsCount = 0;
    private short inc = 0;

    public boolean hasRendered = true;

    public boolean toUpdateViewport = false;

    public String FPS;

    private Scene scene;

    Renderer(MainActivity main){
        loader = Loader.getLoader();
        scene = new Scene();

        ressources = Ressources.getRessources();
    }

    public Scene getScene(){
        Log.w("getScene","Trying to get scene...");
        while (true){
            if(hasRendered){
                Log.w("getScene","Succeeded to get scene.");
                return scene;
            }
        }
    }

    public synchronized void setScene(Scene scene){
        Log.w("setScene","Trying to set scene...");
        boolean succeeded = false;
        while (!succeeded){
            if(!ressources.isRendering){
                succeeded = true;
                Log.w("setScene","Succeeded to set scene.");
                this.scene = scene;
                toUpdateViewport = true;
            }
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA,GLES30.GL_ONE_MINUS_SRC_ALPHA);
        int[] ints = new int[2];
        GLES30.glGetIntegerv(GLES30.GL_MAX_FRAGMENT_UNIFORM_VECTORS,ints, 1);
        GLES30.glGetIntegerv(GLES30.GL_MAX_DRAW_BUFFERS,ints, 0);
        Log.w("init", "max combined texture units : "  + ints[1]);
        Log.w("init", "max draw buffers : " + ints[0]);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int w, int h) {
        ressources.viewport = new Vector2f((float) w, (float) h);
        scene.updateViewportSize();
        GLES30.glCullFace(GLES30.GL_BACK);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {

        //wait until whatever is modifying the scene stops.
        /*while (ressources.isModifyingScene){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }*/

        //ressources.isRendering = true;

        //if the shaders aren't built, it will build them.
        ressources.buildShaders();

        //update the scene's viewport size if it just got set.
        if(toUpdateViewport){
            scene.updateViewportSize();
            toUpdateViewport = false;
        }

        //GLES30.glViewport(0,0,(int) ressources.viewport.x,(int) ressources.viewport.y);
        //GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,0);

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        //GLES30.glClearColor(color.x, color.y,color.z,color.w);
        GLES30.glClearColor(0f,0f,0f,0f);

        scene.updateGraphics();

        //Scene scene = this.scene.copy();

        //TAA
        /*inc = (short) Math.floorMod(scene.pp.inc + 1,4);
        Vector2f jitter = new Vector2f(0);
        if(scene.pp.effects.contains(PostProcessing.effect.TAA)) {
            jitter = new Vector2f(((float) Math.random() - 0.5f) / (Math.min(ressources.viewport.x, ressources.viewport.y) / scene.pp.downSizeFactor), ((float) Math.random() - 0.5f) / (Math.min(ressources.viewport.x, ressources.viewport.y) / scene.pp.downSizeFactor));
        }
       //Dithering
        if(scene.pp.effects.contains(PostProcessing.effect.DITHERING)) {
            if (inc == 1 || inc == 2) {
                jitter.add(new Vector2f(0.5f * scene.pp.downSizeFactor / Math.min(ressources.viewport.x, ressources.viewport.y), 0));
            }
            if (inc == 2 || inc == 3) {
                jitter.add(new Vector2f(0f, 0.5f * scene.pp.downSizeFactor / Math.min(ressources.viewport.x, ressources.viewport.y)));
            }
        }*/

        scene.pp.prepareForRender();
        render(scene);

        /*Matrix4f[] mats = Maths.generateSunTransformMatrix(scene);
        scene.sunLight.view = mats[0];
        scene.sunLight.proj = mats[1];
        scene.pp.prepareShadowPass();*/
        //render(scene, new Vector2f(0f),true,2);

        scene.pp.renderEffects();
        //GLES30.glViewport(0,0,(int) ressources.viewport.x,(int) ressources.viewport.y);
        //render(scene, new Vector2f(0f), false,0);

        scene.callNewFrame();

        //ressources.isRendering = false;

        fpsCount ++;
        if (System.currentTimeMillis()-fpsTime > 5000){
            FPS = " Time : " + ((float) (System.currentTimeMillis()-fpsTime)/(float) fpsCount) + "ms \n " + (int)fpsCount/((int) (System.currentTimeMillis()-fpsTime)/1000) + " FPS";
            Log.w("DRAW", FPS);
            fpsTime = System.currentTimeMillis();
            fpsCount = 0;
            GLES30.glFinish();
            GLES30.glFlush();
        }
    }

    public void render(Scene scene){// 15 ms

        //if (pass3D) { // 15 ms
            if(scene.getSkybox() != null ) {
                GLES30.glDisable(GLES30.GL_DEPTH_TEST);
                GLES30.glDisable(GLES30.GL_CULL_FACE);

                SkyBox skyBox = scene.getSkybox();
                GLES30.glBindVertexArray(skyBox.model.vaoID);
                GLES30.glEnableVertexAttribArray(0);

                GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, skyBox.HDRI.ID);

                ressources.skyboxShader.start();

                ressources.skyboxShader.loadViewMatrix(scene.camera.getViewMat());
                ressources.skyboxShader.loadProjectionMatrix(scene.PROJECTION_MATRIX);
                ressources.skyboxShader.loadTransformMatrix(Maths.createTransformationMatrix(new Vector3f(0), new Vector3f(0), new Vector3f(Scene.FAR_PLANE * 0.9f)));
                ressources.skyboxShader.loadStrength(skyBox.strength);

                GLES30.glDrawElements(GLES30.GL_TRIANGLES, skyBox.model.vertCount, GLES30.GL_UNSIGNED_INT, 0);

                ressources.skyboxShader.stop();

                GLES30.glDisableVertexAttribArray(0);
                GLES30.glBindVertexArray(0);
            }

            GLES30.glEnable(GLES30.GL_DEPTH_TEST);

            for (Entity entity : scene.Entities) {
                RawModel model = entity.model;

                ressources.staticShader.start();

                GLES30.glBindVertexArray(model.vaoID);
                GLES30.glEnableVertexAttribArray(0);
                GLES30.glEnableVertexAttribArray(1);
                GLES30.glEnableVertexAttribArray(2);
                GLES30.glEnableVertexAttribArray(3);
                GLES30.glEnableVertexAttribArray(4);

                if(entity.material.get(0).isColorTextured) {
                    GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, entity.material.get(0).texture.ID);
                }
                if(entity.material.get(0).isNormalMapped){
                    GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
                    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, entity.material.get(0).normalMap.ID);
                }
                if(scene.getSkybox() != null){
                    GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
                    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, scene.getSkybox().HDRI.ID);
                }
                ressources.staticShader.loadMaterial(entity.material.get(0));

                ressources.staticShader.loadSunLight(scene.sunLight);
                ressources.staticShader.loadSkyStrength(scene.getSkybox().strength);

                ressources.staticShader.loadTransformationMatrix( Matrix4f.MultiplyMM( scene.PROJECTION_MATRIX,Matrix4f.MultiplyMM( scene.camera.getViewMat(),entity.getTransformMatrix(0) ) ) );
                ressources.staticShader.loadRotationMatrix( entity.getRotationMatrix(0) );

                GLES30.glDrawElements(GLES30.GL_TRIANGLES,model.vertCount,GLES30.GL_UNSIGNED_INT,0);

                GLES30.glDisableVertexAttribArray(4);
                GLES30.glDisableVertexAttribArray(3);
                GLES30.glDisableVertexAttribArray(2);
                GLES30.glDisableVertexAttribArray(1);
                GLES30.glDisableVertexAttribArray(0);
                GLES30.glBindVertexArray(0);

                ressources.staticShader.stop();
            }
        //}

        /*if(pass == 0){ ressources.staticShader.stop(); }
        if(pass == 2){ ressources.shadowShader.stop(); }

        //TODO optimize matrix calculation on UI
        if (!pass3D || scene.renderGUIas3D) {

            GLES30.glDisable(GLES30.GL_CULL_FACE);
            GLES30.glDisable(GLES30.GL_DEPTH_TEST);
            GLES30.glEnable(GLES30.GL_BLEND);
            GLES30.glViewport(0,0,(int) ressources.viewport.x,(int) ressources.viewport.y);
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,0);

            for (TexQuad quad : scene.TexQuads) {
                ressources.texQuadShader.start();

                GLES30.glBindVertexArray(quad.model.vaoID);
                GLES30.glEnableVertexAttribArray(0);

                GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, quad.texture.ID);

                ressources.texQuadShader.loadScreenDimensions(ressources.viewport);
                ressources.texQuadShader.loadTransparency(quad.texture.hasTransparency);
                for (int i = 0; i < quad.position.size(); i++) {
                    if (quad.show.get(i)) {
                        ressources.texQuadShader.loadTransformationMatrix(Maths.createTransformationMatrix(quad.position.get(i), quad.rotation.get(i).floatValue(), quad.scale.get(i)));
                        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, quad.model.vertCount);
                    }
                }

                GLES30.glDisableVertexAttribArray(0);
                ressources.texQuadShader.stop();
            }

            for (ColorSquare quad : scene.ColorSquare) {
                ressources.colorShader.start();

                GLES30.glBindVertexArray(quad.model.vaoID);
                GLES30.glEnableVertexAttribArray(0);

                ressources.colorShader.loadScreenDimensions(ressources.viewport);
                for (int i = 0; i < quad.number; i++) {
                    if (quad.show.get(i)) {
                        ressources.colorShader.loadCornerRadius(quad.cornerRadius);
                        ressources.colorShader.loadColor(quad.color.get(i));
                        ressources.colorShader.loadTransformationMatrix(Maths.createTransformationMatrix(quad.position.get(0), quad.rotation.get(0), quad.scale.get(0)));
                        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, quad.model.vertCount);
                    }
                }

                GLES30.glDisableVertexAttribArray(0);
                ressources.colorShader.stop();
            }

            for (TextBox textBox : scene.TextBoxes) {
                if (textBox.show) {
                    ressources.textShader.start();

                    GLES30.glBindVertexArray(textBox.textModel.vaoID);
                    GLES30.glEnableVertexAttribArray(0);
                    GLES30.glEnableVertexAttribArray(1);

                    GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textBox.font.textureAtlas.ID);

                    ressources.textShader.loadTransformationMatrix(Maths.createTransformationMatrix(textBox.position, textBox.rotation, textBox.scale));
                    ressources.textShader.loadScreenDimensions(ressources.viewport);

                    GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, textBox.textModel.vertCount);

                    GLES30.glDisableVertexAttribArray(1);
                    GLES30.glDisableVertexAttribArray(0);
                    GLES30.glBindVertexArray(0);

                    ressources.textShader.stop();
                }
            }

            GLES30.glDisable(GLES30.GL_BLEND);
        }*/
    }
}
