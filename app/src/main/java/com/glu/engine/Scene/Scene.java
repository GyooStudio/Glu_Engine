package com.glu.engine.Scene;

import android.opengl.Matrix;
import android.util.Log;

import com.glu.engine.GUI.Button;
import com.glu.engine.GUI.ColorSquare;
import com.glu.engine.GUI.Slider;
import com.glu.engine.GUI.TexQuad;
import com.glu.engine.GUI.Text.Font;
import com.glu.engine.GUI.Text.TextBox;
import com.glu.engine.Objects.CustomObjects.CustomObject;
import com.glu.engine.Objects.Entity;
import com.glu.engine.Objects.GTexture;
import com.glu.engine.Objects.Raycast;
import com.glu.engine.Objects.SkyBox;
import com.glu.engine.Postprocessing.PostProcessing;
import com.glu.engine.actionManager.ActionManager;
import com.glu.engine.actionManager.InputManager;
import com.glu.engine.utils.Loader;
import com.glu.engine.utils.Maths;
import com.glu.engine.vectors.Matrix4f;
import com.glu.engine.vectors.Vector3f;

import java.util.ArrayList;
import java.util.Collections;

public final class Scene {
    public static final float NEAR_PLANE = 0.1f;
    public static final float FAR_PLANE = 30.0f;
    public final int FOV = 70;
    public int state;
    public ArrayList<Entity> Entities = new ArrayList<>();
    public ArrayList<Entity> DirtyEntities = new ArrayList<>();
    public ArrayList<TexQuad> TexQuads = new ArrayList<>();
    public ArrayList<TexQuad> dirtyTexQuads = new ArrayList<>();
    public ArrayList<ColorSquare> dirtyColor = new ArrayList<>();
    public ArrayList<ColorSquare> ColorSquare = new ArrayList<>();
    public ArrayList<Button> DirtyButtons = new ArrayList<>();
    public ArrayList<Button> Buttons = new ArrayList<>();
    public ArrayList<Slider> DirtySliders = new ArrayList<>();
    public ArrayList<Slider> Sliders = new ArrayList<>();
    public ArrayList<TextBox> DirtyTextBoxes = new ArrayList<>();
    public ArrayList<TextBox> TextBoxes = new ArrayList<>();
    public ArrayList<Light> Lights = new ArrayList<>();
    public ArrayList<GTexture> textures = new ArrayList<>();
    public ArrayList<CustomObject> objects = new ArrayList<>();
    private SkyBox skybox;
    public PostProcessing pp;
    public Camera camera;
    public ActionManager actionManager;
    public InputManager inputManager;
    private boolean isLightSorted = true;
    private boolean isSkyboxClean = false;

    public final Matrix4f PROJECTION_MATRIX = new Matrix4f();

    public boolean renderGUIas3D = false;

    public static final int PAUSE = 0;
    public static final int RUNNING = 1;

    private int entityIncrement = 0;
    public boolean hasFirstUpdated = false;
    public boolean CleanEntities = true;

    public SunLight sunLight = new SunLight(new Vector3f(-1f,-1f,0f), new Vector3f(1f,0.9f,0.8f), 3.0f);

    private Ressources ressources;

    public Scene(){
        Loader loader = Loader.getLoader();
        ressources = Ressources.getRessources();
        this.actionManager = ActionManager.getActionManager();
        this.inputManager = new InputManager(this);

        state = 0;

        camera = new Camera();
        camera.setPosition(new Vector3f(-1,1,-1));
        camera.setRotation( Vector3f.lookAt(camera.getPosition(), new Vector3f(0,0,0), 0));

        pp = new PostProcessing();
        generateProjectionMatrix(ressources.viewport.x/ ressources.viewport.y);
    }

    public void addEntity(Entity entity){
        for(int i = 0; i < Entities.size(); i++){
            if(Entities.get(i) == null){
                Entities.set(i,entity);
                entity.ID = i;
                return;
            }
        }
        DirtyEntities.add(entity);
        entityIncrement++;
        entity.ID = entityIncrement;
    }

    public void removeEntity(Entity e){
        Entities.remove(e);
    }

    public Entity getEntity(String name){
        for (Entity entity: Entities) {
            if(entity != null && name.equals(entity.name)){
                return entity;
            }
        }
        return null;
    }

    public void addCustomObject(CustomObject object){
        objects.add(object);
    }

    public void removeCustomObject(CustomObject o){
        objects.remove(o);
    }

    public CustomObject getCustomObject(String name){
        for (CustomObject object: objects) {
            if(name.equals(object.name)){
                return object;
            }
        }
        return null;
    }

    public void addTexture(GTexture texture){
        textures.add(texture);
    }

    public GTexture getTexture(String name){
        for (GTexture texture: textures) {
            if(name.equals(texture.name)){
                return texture;
            }
        }
        return null;
    }

    public void addTexQuad(TexQuad quad){
        dirtyTexQuads.add(quad);
    }

    public TexQuad getTexQuad(String name){
        for (TexQuad quad: TexQuads) {
            if(name.equals(quad.name)){
                return quad;
            }
        }
        return null;
    }

    public void addColorSquare(ColorSquare quad){
        dirtyColor.add(quad);
    }

    public ColorSquare getColorSquare(String name){
        for (ColorSquare quad: ColorSquare) {
            if(name.equals(quad.name)){
                return quad;
            }
        }
        return null;
    }

    public void addButton(Button button){
        Buttons.add(button);
    }

    public Button getButton(String name){
        for (Button button: Buttons) {
            if(name.equals(button.name)){
                return button;
            }
        }
        return null;
    }

    public void addSlider(Slider slider){
        DirtySliders.add(slider);
    }

    public Slider getSlider(String name){
        for (Slider s: Sliders) {
            if(s.name.equals(name)){
                return s;
            }
        }
        return null;
    }

    public void addLight(Light l){
        Lights.add(l);
        isLightSorted = false;
    }

    public Light getLight(String name){
        for (Light l : Lights) {
            if (l.name.equals(name)){
                return l;
            }
        }
        return null;
    }

    public void addTextBox(TextBox textBox){
        DirtyTextBoxes.add(textBox);
    }

    public TextBox getTextBox(String name){
        for (TextBox t : TextBoxes) {
            if (t.name.equals(name)) {
                return t;
            }
        }
        return null;
    }

    public void setSkybox(SkyBox skybox){
        this.skybox = skybox;
        isSkyboxClean = false;
    }

    public SkyBox getSkybox(){
        return skybox;
    }

    public void generateProjectionMatrix(float aspectRatio){
        PROJECTION_MATRIX.setIdentity();
        Matrix.perspectiveM(PROJECTION_MATRIX.mat,0,FOV,aspectRatio,NEAR_PLANE,FAR_PLANE);
    }

    public void cleanEntities(){
        if (DirtyEntities.size() > 0 && CleanEntities) {
            long timer = System.currentTimeMillis();
            if (DirtyEntities.get(0).model.isDirty) {
                DirtyEntities.get(0).model.makeModel();
            }
            Entities.add(DirtyEntities.get(0));
            String name = DirtyEntities.get(0).name;
            DirtyEntities.remove(0);
            Log.w("cleanEntity", (System.currentTimeMillis() - timer) + " milliseconds to clean Entity "+ name);
        }
    }

    public void cleanTextures(){
        long timer = System.currentTimeMillis();
        for (int i = 0; i < textures.size(); i++) {
            if (textures.get(i) != null && textures.get(i).isDirty) {
                textures.get(i).makeTexture();
                Log.w("cleanTextures",textures.get(i).name + " cleaned");
                Log.w("cleanTextures",(System.currentTimeMillis()-timer) + " milliseconds to clean textures");
            }
        }
    }

    public void cleanGUI(){

        if(dirtyTexQuads.size() > 0) {
            long timer = System.currentTimeMillis();
            TexQuad quad = dirtyTexQuads.get(0);
            quad.makeModel();
            TexQuads.add(quad);
            dirtyTexQuads.remove(0);
            Log.w("cleanGUI", (System.currentTimeMillis() - timer) + " milliseconds to clean " + quad.name + " , quad #" + TexQuads.size());
        }

        if(dirtyColor.size() > 0){
            long timer = System.currentTimeMillis();
            ColorSquare quad = dirtyColor.get(0);
            quad.makeModel();
            ColorSquare.add(quad);
            dirtyColor.remove(0);
            Log.w("cleanGUI", (System.currentTimeMillis() - timer) + " milliseconds to clean " + quad.name + ", quad #" + ColorSquare.size());
        }

        if(DirtySliders.size() > 0){
            long timer = System.currentTimeMillis();
            Slider slider = DirtySliders.get(0);
            slider.button.makeModel();
            slider.slider.makeModel();
            if(slider.slider instanceof TexQuad){
                TexQuads.add((TexQuad) slider.slider);
            }else if(slider.slider instanceof ColorSquare){
                ColorSquare.add((ColorSquare) slider.slider);
            }
            slider.button.makeModel();
            if(slider.button instanceof TexQuad){
                TexQuads.add((TexQuad) slider.button);
            }else if(slider.button instanceof ColorSquare){
                ColorSquare.add((ColorSquare) slider.button);
            }
            Sliders.add(slider);
            DirtySliders.remove(0);
            Log.w("cleanGUI", (System.currentTimeMillis() - timer) + " milliseconds to clean slider " + Sliders.size());
        }

        if(DirtyTextBoxes.size() > 0){
            TextBox textBox = DirtyTextBoxes.get(0);
            textBox.makeText();
            TextBoxes.add(textBox);
            DirtyTextBoxes.remove(0);
        }

    }

    private void cleanSkybox(){
        if(!isSkyboxClean && skybox != null){
            skybox.model.makeModel();
            skybox.HDRI.makeTexture();
            isSkyboxClean = true;
        }
    }

    public void updateTextBoxes(){
        for (TextBox textBox : TextBoxes) {
            if(textBox.toUpdate) {
                textBox.makeText();
            }
        }
    }

    public void sortLights(){
        if(!isLightSorted) {
            Collections.sort(Lights);
            isLightSorted = true;
        }
    }

    public void callNewFrame(){
        for (Entity e : Entities) {
            e.callNewFrame();
        }
        camera.callNewFrame();
    }

    public void updateGraphics(){
        if(!pp.isSetup) {
            pp.setup(3.0f, this);
        }
        cleanTextures();
        cleanEntities();
        cleanGUI();
        cleanSkybox();
        updateTextBoxes();
        //sortLights();

        hasFirstUpdated = true;
    }

    public void updateViewportSize(){

        generateProjectionMatrix(ressources.viewport.x / ressources.viewport.y);

        if(pp.isSetup) {
            PostProcessing t_pp = new PostProcessing(pp);
            pp.setup(t_pp.downSizeFactor, this);
            pp.ppShaders = t_pp.ppShaders;
            pp.deffShader = t_pp.deffShader;
            pp.effects = t_pp.effects;
            pp.A = t_pp.A;
            pp.B = t_pp.B;
        }
        Log.e("updateViewportSize","setSceneRes to " + ressources.viewport.x + " " + ressources.viewport.y);
    }

    public Matrix4f getWorldTransfomMatrix(Entity e, int indexE){
        //if(e.parentID.get(indexE) == -1){
        return Maths.createTransformationMatrix(e.getPosition(indexE),e.getRotation(indexE),e.getScale(indexE));
        /*}else{
            Matrix4f mat = getWorldTransfomMatrix(Entities.get(e.parentID.get(indexE)),e.parentInstanceID.get(indexE));
            Matrix.multiplyMM(mat.mat,0,mat.mat,0,Maths.createTransformationMatrix(e.getPosition(indexE),e.getRotation(indexE),e.getScale(indexE)).mat,0);
            return mat;
        }*/
    }

    /*public void setParent(Entity e, int indexE, Entity p, int indexP){
        e.parentID.set(indexE,p.ID);
        e.parentInstanceID.set(indexE,indexP);
        Matrix4f pMat = getWorldTransfomMatrix(p,indexE);
        Matrix4f cMat = getWorldTransfomMatrix(e,indexP);
        Matrix.invertM(pMat.mat, 0,cMat.mat,0);
        Matrix.multiplyMM(cMat.mat,0, pMat.mat, 0,cMat.mat,0);
        float[] pos = new float[]{e.getPosition(indexE).x,e.getPosition(indexE).y,e.getPosition(indexE).z,1.0f};
        Matrix.multiplyMV(pos,0, cMat.mat, 0,pos,0);
        e.setPosition(new Vector3f(pos[0],pos[1],pos[2]),indexE);
        e.setRotation(Vector3f.sub(e.getRotation(indexE), p.getRotation(indexP)),indexE);
        e.setScale(Vector3f.sub(e.getScale(indexE),p.getScale(indexP)),indexE);
    }*/

    /*public void unParent(Entity e, int index){
        if(e.parentID.get(index) != -1){
            float[] pos = new float[]{0,0,0,1};
            Matrix.multiplyMV(pos,0,getWorldTransfomMatrix(e,index).mat,0,pos,0);
            e.setPosition(new Vector3f(pos[0],pos[1],pos[2]),index);
            e.setRotation(Vector3f.add(e.getRotation(index),Entities.get(e.parentID.get(index)).getRotation(e.parentInstanceID.get(index))),index);
            e.setRotation(Vector3f.add(e.getScale(index),Entities.get(e.parentID.get(index)).getScale(e.parentInstanceID.get(index))),index);
            e.parentID.set(index,-1);
        }
    }*/

    public Raycast raycast(Vector3f pos, Vector3f dir){
        Raycast raycast = new Raycast(pos,dir);
        for (Entity e : Entities) {
            for (int i = 0; i < e.instanceCount; i++) {
                e.rayCast(i,raycast);
            }
        }
        return raycast;
    }
}
