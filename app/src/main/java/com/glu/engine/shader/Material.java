package com.glu.engine.shader;

import com.glu.engine.Objects.GTexture;
import com.glu.engine.vectors.Vector3f;
public class Material {
    public int type;
    public boolean isColorTextured;
    public String textureName;
    public Vector3f color;
    public boolean isNormalMapped;
    public String normalMapName;
    public float emissionIntensity;
    public boolean isRoughnessMapped;
    public float roughness;
    public boolean alphaClip;

    public GTexture texture = null;
    public GTexture normalMap = null;

    public String name;

    public Material(String name, int type, boolean isColorTextured, String textureName, Vector3f color, boolean alphaClip, float emissionIntensity,boolean isRoughnessMapped, float roughness, boolean isNormalMapped, String normalMapName){
        this.name = name;
        this.type = type;
        this.isColorTextured = isColorTextured;
        this.textureName = textureName;
        this.color = color;
        this.alphaClip = alphaClip;
        this.isNormalMapped = isNormalMapped;
        this.normalMapName = normalMapName;
        this.emissionIntensity = emissionIntensity;
        this.isRoughnessMapped = isRoughnessMapped;
        this.roughness = roughness;
    }

    public Material(){
        this.name = "default";
        this.type = 1;
        this.isColorTextured = false;
        this.textureName = "null";
        this.color = new Vector3f(1f);
        this.alphaClip = false;
        this.isNormalMapped = false;
        this.normalMapName = "null";
        this.emissionIntensity = 0f;
        this.isRoughnessMapped = false;
        this.roughness = 0.5f;
    }

    public void setTexture(GTexture texture){
        this.texture = texture;
    }

    public void setNormal(GTexture normal){
        this.normalMap = normal;
    }
}
