package com.glu.engine.Scene;

import android.util.Log;

import com.glu.engine.GUI.Text.Font;
import com.glu.engine.Objects.GTexture;
import com.glu.engine.Objects.RawModel;
import com.glu.engine.shader.ColorShader;
import com.glu.engine.shader.ShadowShader;
import com.glu.engine.shader.SkyboxShader;
import com.glu.engine.shader.StaticShader;
import com.glu.engine.shader.TexQuadShader;
import com.glu.engine.shader.TextShader;
import com.glu.engine.utils.Loader;
import com.glu.engine.vectors.Vector2f;

import java.util.ArrayList;

public class Ressources {
    private static Ressources ressources = null;

    private final Loader loader;

    private final ArrayList<RawModel> meshes = new ArrayList<>();
    private final ArrayList<String> meshNames = new ArrayList<>();
    private final ArrayList<String> meshDirectories = new ArrayList<>();
    private final ArrayList<Boolean> meshIsLoaded = new ArrayList<>();
    private final ArrayList<Integer> meshIndex = new ArrayList<>();

    private final ArrayList<GTexture> textures = new ArrayList<>();
    private final ArrayList<String> textureNames = new ArrayList<>();
    private final ArrayList<String> textureDirectories = new ArrayList<>();
    private final ArrayList<Boolean> textureIsLoaded = new ArrayList<>();
    private final ArrayList<Integer> textureIndex = new ArrayList<>();

    private final ArrayList<Font> fonts = new ArrayList<>();
    private final ArrayList<String> fontNames = new ArrayList<>();
    private final ArrayList<String> fontDirectories = new ArrayList<>();
    private final ArrayList<Boolean> fontIsLoaded = new ArrayList<>();
    private final ArrayList<Integer> fontIndex = new ArrayList<>();
    private final ArrayList<Integer> fontSize = new ArrayList<>();
    private final ArrayList<Integer> fontPadding = new ArrayList<>();
    private final ArrayList<int[]> fontRanges = new ArrayList<>();

    public TexQuadShader texQuadShader;
    public TextShader textShader;
    public ColorShader colorShader;
    public StaticShader staticShader;
    public SkyboxShader skyboxShader;
    public ShadowShader shadowShader;

    public Vector2f viewport;

    public static Ressources getRessources(){
        if(ressources == null){
            ressources = new Ressources();
        }
        return ressources;
    }

    private Ressources(){
        loader = Loader.getLoader();
        texQuadShader = new TexQuadShader(loader.loadAssetText("Shaders/TexQuad.vert"), loader.loadAssetText("Shaders/TexQuad.frag"));
        colorShader = new ColorShader(loader.loadAssetText("Shaders/Color.vert"), loader.loadAssetText("Shaders/Color.frag"));
        textShader = new TextShader(loader.loadAssetText("Shaders/Text.vert"), loader.loadAssetText("Shaders/Text.frag"));
        staticShader = new StaticShader(loader.loadAssetText("Shaders/Static.vert"), loader.loadAssetText("Shaders/Static.frag"));
        skyboxShader = new SkyboxShader(loader.loadAssetText("Shaders/Skybox.vert"), loader.loadAssetText("Shaders/Skybox.frag"));
        shadowShader = new ShadowShader(loader.loadAssetText("Shaders/ShadowMap.vert"),loader.loadAssetText("Shaders/ShadowMap.frag"));
        viewport = new Vector2f(1000f,300f);
    }

    public void addMesh(String name, String directory){
        meshNames.add(name);
        meshDirectories.add(directory);
        meshIsLoaded.add(false);
        meshIndex.add(0);
    }

    public void addTexture(String name, String directory){
        textureNames.add(name);
        textureDirectories.add(directory);
        textureIsLoaded.add(false);
        textureIndex.add(0);
    }

    public void addFont(String name, String directory, int size, int padding, int[] ranges){
        fontNames.add(name);
        fontDirectories.add(directory);
        fontIsLoaded.add(false);
        fontIndex.add(0);
        fontSize.add(size);
        fontPadding.add(padding);
        fontRanges.add(ranges);
    }

    public void addMesh(String name, RawModel model){
        int index = meshNames.indexOf(name);
        meshIsLoaded.set(index,true);
        meshIndex.set(index,meshes.size());
        meshes.add(model);
    }

    public void addTexture(String name, GTexture texture){
        int index = textureNames.indexOf(name);
        textureIsLoaded.set(index,true);
        textureIndex.set(index,textures.size());
        textures.add(texture);
    }

    public void addFont(String name, Font font){
        int index = fontNames.indexOf(name);
        fontIsLoaded.set(index,true);
        fontIndex.set(index,fonts.size());
        fonts.add(font);
    }

    public RawModel getMesh(String name){
        int index = meshNames.indexOf(name);
        if(meshIsLoaded.get(index)) {
            return meshes.get(meshIndex.get(index));
        }else{
            RawModel mesh = loader.loadAssetModel(meshDirectories.get(index));
            meshIndex.set(index,meshes.size());
            meshIsLoaded.set(index,true);
            meshes.add(mesh);
            return mesh;
        }
    }

    public GTexture getTexture(String name){
        int index = textureNames.indexOf(name);
        if(textureIsLoaded.get(index)) {
            return textures.get(textureIndex.get(index));
        }else{
            GTexture texture = loader.loadAssetTexture(textureDirectories.get(index));
            textureIndex.set(index,textures.size());
            textureIsLoaded.set(index,true);
            textures.add(texture);
            return texture;
        }
    }

    public Font getFont(String name){
        int index = fontNames.indexOf(name);
        if(fontIsLoaded.get(index)) {
            return fonts.get(fontIndex.get(index));
        }else{
            Font font = loader.loadAssetFont(fontDirectories.get(index), fontSize.get(index), fontPadding.get(index), fontRanges.get(index));
            fontIndex.set(index,fontIndex.size());
            fontIsLoaded.set(index,true);
            fonts.add(font);
            return font;
        }
    }

    public String getMeshDir(String name){
        return meshDirectories.get(meshNames.indexOf(name));
    }

    public void buildShaders(){
        if(!staticShader.isShaderBuilt){
            staticShader.buildShader();
        }
        if(!skyboxShader.isShaderBuilt){
            skyboxShader.buildShader();
        }
        if(!textShader.isShaderBuilt){
            textShader.buildShader();
        }
        if(!colorShader.isShaderBuilt){
            colorShader.buildShader();
        }
        if(!texQuadShader.isShaderBuilt) {
            texQuadShader.buildShader();
        }
        if(!shadowShader.isShaderBuilt){
            shadowShader.buildShader();
        }
    }

    public void log(){
        for (int i = 0; i < meshNames.size(); i ++){
            Log.w("ressources log", "mesh " + i + " name : " + meshNames.get(i));
            Log.w("ressources log", "mesh " + i + " directory : " + meshDirectories.get(i));
            Log.w("ressources log", "mesh " + i + " is loaded : " + meshIsLoaded.get(i));
            Log.w("ressources log", "mesh " + i + " index : " + meshIndex.get(i));
        }

        for (int i = 0; i < textureNames.size(); i ++){
            Log.w("ressources log", "texture " + i + " name : " + textureNames.get(i));
            Log.w("ressources log", "texture " + i + " directory : " + textureDirectories.get(i));
            Log.w("ressources log", "texture " + i + " is loaded : " + textureIsLoaded.get(i));
            Log.w("ressources log", "texture " + i + " index : " + textureIndex.get(i));
        }

        for (int i = 0; i < fontNames.size(); i ++){
            Log.w("ressources log", "font " + i + " name : " + fontNames.get(i));
            Log.w("ressources log", "font " + i + " directory : " + fontDirectories.get(i));
            Log.w("ressources log", "font " + i + " is loaded : " + fontIsLoaded.get(i));
            Log.w("ressources log", "font " + i + " index : " + fontIndex.get(i));
        }
    }
}
